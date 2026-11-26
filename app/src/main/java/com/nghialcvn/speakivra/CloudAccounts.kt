package com.nghialcvn.speakivra

import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <T> Task<T>.cloudAwait(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task -> if (continuation.isActive) {
        if (task.isSuccessful) continuation.resume(task.result)
        else continuation.resumeWithException(task.exception ?: IllegalStateException("Chưa kết nối được dịch vụ tài khoản."))
    } }
}
internal class AccountRuntime private constructor(private val context: Context, val auth: FirebaseAuth = FirebaseAuth.getInstance(), val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    companion object {
        @Volatile private var instance: AccountRuntime? = null
        // Test injection only honored in debuggable builds; never a release feature switch.
        internal var testGateway: AccountGateway? = null
        fun installEmulated(context: Context, auth: FirebaseAuth, db: FirebaseFirestore): AccountRuntime {
            check(BuildConfig.DEBUG)
            return AccountRuntime(context.applicationContext, auth, db).also { it.noBackground = true; instance = it }
        }
        fun get(context: Context): AccountRuntime = instance ?: synchronized(this) {
            instance ?: AccountRuntime(context.applicationContext).also { instance = it }
        }
    }
    private val learnerStore = LearnerStore(context)
    private val identities = GuestIdentityStore(context)
    private val syncPrefs = context.getSharedPreferences("speakivra_cloud_sync", 0)
    private val mutex = Mutex()
    private val mutableRevision = MutableStateFlow(0L)
    val revision = mutableRevision.asStateFlow()
    val uid get() = auth.currentUser?.uid
    private var noBackground = false
    var applyingCloud = false
        private set
    init { auth.addAuthStateListener {
        changed()
        it.currentUser?.takeIf { user -> user.isEmailVerified }?.let { user ->
            syncPrefs.edit().putBoolean("restore.${user.uid}", true).apply(); schedule()
        }
    } }
    fun changed() { mutableRevision.value += 1 }
    fun lastSync(learner: Learner?) = learner?.let { syncPrefs.getLong("synced.${it.id}", 0) } ?: 0
    fun isSynced(learner: Learner?) = learner != null && lastSync(learner) > 0 && !syncPrefs.getBoolean("pending.${learner.id}", false)
    fun schedule() {
        val user = auth.currentUser ?: return
        if (noBackground) return
        if (!user.isEmailVerified || (BuildConfig.DEBUG && testGateway != null)) return
        val work = OneTimeWorkRequestBuilder<AccountSyncWorker>()
            .setInputData(workDataOf("uid" to user.uid))
            .setInitialDelay(3, TimeUnit.SECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork("speakivra-sync-${user.uid}", ExistingWorkPolicy.REPLACE, work)
    }
    fun watch(learner: Learner): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (!applyingCloud && key != null && (key.startsWith("lesson.") || key.startsWith("achievement.") || key == "extra.completed_ids" || key == "active_lesson") && identities.peek(learner)?.ownerUid == uid) {
                syncPrefs.edit().putBoolean("pending.${learner.id}", true).apply(); schedule()
            }
        }
        learnerStore.preferences(learner).registerOnSharedPreferenceChangeListener(listener)
        return listener
    }
    private fun requireUser(expected: String): String {
        check(auth.currentUser?.uid == expected && auth.currentUser?.isEmailVerified == true) { "Hãy đăng nhập và xác minh tài khoản để đồng bộ." }
        return expected
    }
    suspend fun claim(identity: GuestIdentity, proof: ByteArray): GuestClaimReceipt = mutex.withLock {
        val owner = requireNotNull(uid) { "Hãy đăng nhập trước khi liên kết." }; requireUser(owner)
        val ref = db.collection("claims").document(identity.profileId)
        val previous = ref.get(Source.SERVER).cloudAwait()
        if (previous.exists()) {
            check(previous.getString("ownerUid") == owner) { "Hồ sơ đã thuộc tài khoản khác." }
            return@withLock GuestClaimReceipt(identity.profileId, owner)
        }
        val secret = proof.toString(Charsets.UTF_8)
        require(Regex("[a-f0-9]{64}").matches(secret) && guestProfileId(proof) == identity.profileId)
        val proofRef = db.collection("accounts").document(owner).collection("claimProofs").document(identity.profileId)
        try {
            db.batch().set(ref, mapOf("schema" to 2, "ownerUid" to owner, "createdAt" to FieldValue.serverTimestamp()))
                .set(proofRef, mapOf("secret" to secret, "createdAt" to FieldValue.serverTimestamp())).commit().cloudAwait()
        } catch (failure: Exception) {
            // Another device (or a retry whose response was lost) may have won the atomic create.
            val receipt = runCatching { ref.get(Source.SERVER).cloudAwait() }.getOrNull()
            if (receipt?.getString("ownerUid") != owner) throw failure
        }
        requireUser(owner)
        // Deletion does not unlock the immutable claim. Retry can clean up a retained proof later.
        runCatching { proofRef.delete().cloudAwait() }
        GuestClaimReceipt(identity.profileId, owner)
    }
    suspend fun syncAll(expectedUid: String = requireNotNull(uid)): Unit = mutex.withLock {
        requireUser(expectedUid)
        val remote = db.collection("accounts").document(expectedUid).collection("profiles").limit(200).get(Source.SERVER).cloudAwait()
        requireUser(expectedUid)
        for (document in remote.documents) {
            val local = learnerStore.findCloud(document.id)
            if (local == null || identities.peek(local)?.ownerUid == null) {
                val value = validateSnapshot(document)
                applyImported(document.id, expectedUid, value.first, value.second, sanitizedCloudProgress(document.id, JSONObject(document.getString("progressJson") ?: "{}")))
            }
        }
        for (person in learnerStore.all().filter { identities.peek(it)?.ownerUid == expectedUid }) syncOne(person, expectedUid)
        requireUser(expectedUid)
        syncPrefs.edit().putBoolean("restore.$expectedUid", false).apply(); changed()
    }
    fun needsRestore(owner: String) = syncPrefs.getBoolean("restore.$owner", false)
    suspend fun syncPending(expectedUid: String): Unit = mutex.withLock {
        requireUser(expectedUid)
        for (person in learnerStore.all().filter { identities.peek(it)?.ownerUid == expectedUid && syncPrefs.getBoolean("pending.${it.id}", false) }) syncOne(person, expectedUid)
        requireUser(expectedUid); changed()
    }
    fun markPending(person: Learner) {
        syncPrefs.edit().putBoolean("pending.${person.id}", true).apply(); schedule()
    }
    private fun validateSnapshot(doc: DocumentSnapshot): Pair<String, Int> {
        check(doc.getLong("schema") == 1L && doc.getString("profileId") == doc.id && Regex("[a-f0-9]{64}").matches(doc.id))
        val name = requireNotNull(doc.getString("name")); val avatar = requireNotNull(doc.getLong("avatar")).toInt()
        require(name.isNotBlank() && name.length <= 30 && name.none { it.isISOControl() } && avatar in 0..5)
        check((doc.getString("progressJson") ?: error("Thiếu tiến độ.")).length <= 524288)
        return name to avatar
    }
    private suspend fun syncOne(person: Learner, owner: String) {
        requireUser(owner)
        val identity = requireNotNull(identities.peek(person)); check(identity.ownerUid == owner)
        val input = profileSyncPayload(person, identity, learnerStore.preferences(person).all).getJSONObject("progress")
        val dirtyName = syncPrefs.getBoolean("metadataDirty.${person.id}", false)
        val ref = db.collection("accounts").document(owner).collection("profiles").document(identity.profileId)
        val merged = db.runTransaction { transaction ->
            requireUser(owner)
            val old = transaction.get(ref)
            val oldMeta = if (old.exists()) validateSnapshot(old) else person.name to person.avatar
            val name = if (dirtyName) person.name else oldMeta.first
            val avatar = if (dirtyName) person.avatar else oldMeta.second
            val remote = if (old.exists()) sanitizedCloudProgress(identity.profileId, JSONObject(old.getString("progressJson") ?: "{}")) else JSONObject()
            val combined = sanitizedCloudProgress(identity.profileId, mergeCloudProgress(input, remote))
            val text = combined.toString(); check(text.length <= 524288) { "Tiến độ quá lớn để đồng bộ." }
            if (!old.exists() || name != oldMeta.first || avatar != oldMeta.second || text != old.getString("progressJson")) {
                transaction.set(ref, mapOf("schema" to 1, "profileId" to identity.profileId, "name" to name, "avatar" to avatar,
                    "progressJson" to text, "revision" to ((old.getLong("revision") ?: 0) + 1),
                    "createdAt" to (old.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()), "updatedAt" to FieldValue.serverTimestamp()))
            }
            Triple(name, avatar, combined)
        }.cloudAwait()
        requireUser(owner)
        // Learning or editing may have continued during network I/O. Keep those newer local changes.
        val current = learnerStore.all().first { it.id == person.id }
        val newerMeta = current.name != person.name || current.avatar != person.avatar
        val now = profileSyncPayload(current, identity, learnerStore.preferences(current).all).getJSONObject("progress")
        val combinedNow = sanitizedCloudProgress(identity.profileId, mergeCloudProgress(now, merged.third))
        applyImported(identity.profileId, owner, if (newerMeta) current.name else merged.first,
            if (newerMeta) current.avatar else merged.second, combinedNow)
        syncPrefs.edit().putLong("synced.${person.id}", System.currentTimeMillis())
            .putBoolean("metadataDirty.${person.id}", newerMeta)
            .putBoolean("pending.${person.id}", newerMeta || combinedNow.toString() != merged.third.toString()).apply()
        if (newerMeta || combinedNow.toString() != merged.third.toString()) schedule()
    }
    private suspend fun applyImported(profileId: String, owner: String, name: String, avatar: Int, progress: JSONObject) = withContext(Dispatchers.Main.immediate) {
        // SharedPreferences dispatches listeners synchronously on Main. This prevents cloud imports
        // from enqueueing themselves forever while preserving genuine study changes between imports.
        applyingCloud = true
        try { learnerStore.importCloud(profileId, owner, name, avatar, progress) } finally { applyingCloud = false }
    }
    fun metadataChanged(learner: Learner) {
        syncPrefs.edit().putBoolean("metadataDirty.${learner.id}", true).putBoolean("pending.${learner.id}", true).apply()
        if (identities.peek(learner)?.ownerUid == uid) schedule()
    }
}
class AccountSyncWorker(context: Context, parameters: WorkerParameters): CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val runtime = AccountRuntime.get(applicationContext)
        val expected = inputData.getString("uid") ?: return Result.success()
        if (runtime.uid != expected || runtime.auth.currentUser?.isEmailVerified != true) return Result.success()
        return try {
            if (runtime.needsRestore(expected)) runtime.syncAll(expected) else runtime.syncPending(expected)
            Result.success()
        }
        catch (_: Exception) { if (runAttemptCount < 5) Result.retry() else Result.failure() }
    }
}
