package com.nghialcvn.speakivra

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Two real SDK clients and independent preference stores, exclusively on local Firebase emulators. */
@RunWith(AndroidJUnit4::class)
class ChineseCloudSyncIntegrationTest {
    private data class Device(val context: Context, val app: FirebaseApp, val runtime: AccountRuntime) {
        val store = LearnerStore(context)
        val identities = GuestIdentityStore(context)
    }
    private val devices = mutableListOf<Device>()
    private val password = "Emulator-only-long-password-123"
    private fun device(seed: Boolean = false): Device {
        check(BuildConfig.DEBUG)
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val prefix = "cloud-test-${UUID.randomUUID()}-"
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = base.getSharedPreferences(prefix + name, mode)
        }
        context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
        context.getSharedPreferences("speakivra_learners", 0).edit().putString("profiles",
            encodeLearners(if (seed) listOf(Learner(ORIGINAL_LEARNER, "Lan", 2)) else emptyList())).commit()
        val app = FirebaseApp.initializeApp(base, FirebaseOptions.Builder().setProjectId("demo-speakivra-spark")
            .setApplicationId("1:123456:android:test").setApiKey("fake-api-key").build(), prefix)
        val auth = FirebaseAuth.getInstance(app).apply { useEmulator("10.0.2.2", 9099) }
        val db = FirebaseFirestore.getInstance(app).apply { useEmulator("10.0.2.2", 8080) }
        return Device(context, app, AccountRuntime.installEmulated(context, auth, db)).also { devices += it }
    }
    @After fun cleanup() { devices.forEach { d ->
        d.runtime.auth.signOut()
        runBlocking { d.runtime.db.terminate().cloudAwait() }
        d.app.delete()
    } }
    private fun request(path: String, body: JSONObject? = null): JSONObject {
        val connection = URL("http://10.0.2.2:9099/$path").openConnection() as HttpURLConnection
        connection.connectTimeout = 10000; connection.readTimeout = 10000
        if (body != null) {
            connection.requestMethod = "POST"; connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
        }
        return try { connection.inputStream.bufferedReader().use { JSONObject(it.readText()) } } finally { connection.disconnect() }
    }
    private suspend fun register(d: Device): String {
        val email = "zh-sync-${UUID.randomUUID()}@example.test"
        d.runtime.auth.createUserWithEmailAndPassword(email, password).cloudAwait()
        val user = requireNotNull(d.runtime.auth.currentUser)
        user.sendEmailVerification().cloudAwait()
        val codes = request("emulator/v1/projects/demo-speakivra-spark/oobCodes").getJSONArray("oobCodes")
        val code = (0 until codes.length()).map { codes.getJSONObject(it) }
            .last { it.optString("email") == email && it.optString("requestType") == "VERIFY_EMAIL" }.getString("oobCode")
        request("identitytoolkit.googleapis.com/v1/accounts:update?key=fake-api-key", JSONObject().put("oobCode", code))
        user.reload().cloudAwait(); user.getIdToken(true).cloudAwait(); assertTrue(user.isEmailVerified)
        return email
    }
    private suspend fun bind(d: Device): Pair<Learner, GuestIdentity> {
        val learner = d.store.all().single(); val identity = d.identities.ensure(learner)
        val owner = requireNotNull(d.runtime.uid)
        val receipt = d.runtime.claim(identity, d.identities.proof(learner, owner))
        return learner to d.identities.recordServerReceipt(learner, receipt, owner)
    }
    private fun write(prefs: SharedPreferences, values: Map<String, Any>) {
        val edit = prefs.edit()
        values.forEach { (key, value) -> when (value) {
            is String -> edit.putString(key, value)
            is Int -> edit.putInt(key, value)
            is Set<*> -> edit.putStringSet(key, value.filterIsInstance<String>().toSet())
            else -> error("Unexpected test value")
        } }; assertTrue(edit.commit())
    }
    private fun exported(d: Device, learner: Learner) = profileSyncPayload(learner, requireNotNull(d.identities.peek(learner)),
        d.store.preferences(learner).all).getJSONObject("progress")
    private fun fullValues(): Map<String, Any> = buildMap {
        (Lessons.catalogue + ChineseCurriculum.lessons).forEach { lesson ->
            put("lesson.${lesson.id}.learned_phrases", lesson.phrases.map { it.id }.sorted().joinToString("|"))
            put("lesson.${lesson.id}.learned_turns", lesson.conversation.map { it.id }.sorted().joinToString("|"))
            put("lesson.${lesson.id}.rounds", 1)
            lesson.phrases.forEach { put("achievement.${lesson.id}.${it.id}", 100) }
        }
        put("extra.completed_ids", (ChineseCurriculum.extraIds + ExtraPracticeData.passages.flatMap { it.sentences.map { line -> line.id } + it.id } +
            (ExtraPracticeData.exercises + HotelCurriculum.exercises).flatMap { it.questions.map { q -> q.id } }).toSet())
        put("active_lesson", Lessons.catalogue.last().id); put("zh.active_lesson", ChineseCurriculum.topics.last().id)
        put(CHECK_IN_DATES, setOf("2026-11-27", "2026-11-28"))
    }
    @Test fun legacyMigrationThenBothCompleteCoursesRestoreOnAFreshSdkClient() = runBlocking {
        val first = device(true); val email = register(first); val (learner, identity) = bind(first)
        val owner = requireNotNull(first.runtime.uid)
        val ref = first.runtime.db.collection("accounts").document(owner).collection("profiles").document(identity.profileId)
        val old = ChineseStarterCurriculum.topics.first()
        val legacy = JSONObject().put("lesson.${old.id}.learned_phrases", old.phrases.map { it.id }.sorted().joinToString("|"))
            .put("lesson.${old.id}.rounds", 1)
        old.phrases.forEach { legacy.put("achievement.${old.id}.${it.id}", 95) }
        ref.set(mapOf("schema" to 1, "profileId" to identity.profileId, "name" to "Lan", "avatar" to 2,
            "progressJson" to legacy.toString(), "revision" to 1, "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp())).cloudAwait()
        first.runtime.syncAll(owner)
        val migrated = first.store.preferences(learner)
        assertEquals(3, migrated.getInt("lesson.${old.id}.diamonds_earned", 0))
        assertTrue(migrated.getString("lesson.${old.id}.learned_phrases", "")!!.split('|').containsAll(old.phrases.map { it.id }))
        assertEquals(1, migrated.getInt("lesson.${old.id}.rounds", 0))
        write(migrated, fullValues())
        val expected = exported(first, learner)
        assertTrue(expected.toString().toByteArray().size > 524288)
        first.runtime.markPending(learner); first.runtime.syncPending(owner)
        assertTrue(first.runtime.isSynced(learner))
        val root = ref.get(Source.SERVER).cloudAwait()
        assertTrue(requireNotNull(root.getString("progressJson")).toByteArray().size <= 524288)
        for (level in ChineseCloudPartitions.levels) {
            val doc = ref.collection("languageProgress").document(ChineseCloudPartitions.documentId(level)).get(Source.SERVER).cloudAwait()
            assertTrue(doc.exists()); assertTrue(requireNotNull(doc.getString("progressJson")).toByteArray().size <= 131072)
        }
        val second = device()
        second.runtime.auth.signInWithEmailAndPassword(email, password).cloudAwait()
        second.runtime.syncAll(owner)
        val restored = second.store.visible(owner).single()
        assertNotEquals(learner.id, restored.id); assertEquals(identity.profileId, second.identities.peek(restored)!!.profileId)
        assertEquals("Lan", restored.name); assertEquals(2, restored.avatar)
        assertEquals(cloudProgressValues(expected), cloudProgressValues(exported(second, restored)))
        assertTrue(second.runtime.isSynced(restored))
        val revision = ref.get(Source.SERVER).cloudAwait().getLong("revision")
        second.runtime.syncAll(owner)
        assertEquals(revision, ref.get(Source.SERVER).cloudAwait().getLong("revision"))
    }
    @Test fun offlineRetryAndConcurrentIndependentClientsKeepTheUnionAndAccountPrivacy() = runBlocking {
        val first = device(true); val email = register(first); val (learner, identity) = bind(first)
        val owner = requireNotNull(first.runtime.uid)
        val a = ChineseCurriculum.topics.first { it.level == "A1" }; val b = ChineseCurriculum.topics.first { it.level == "A2" }
        val enKey = "achievement.greetings.${Lessons.a1.first().phrases.first().id}"
        write(first.store.preferences(learner), mapOf("lesson.${a.id}.learned_phrases" to a.phrases[0].id, enKey to 95))
        first.runtime.syncAll(owner)
        val second = device(); second.runtime.auth.signInWithEmailAndPassword(email, password).cloudAwait(); second.runtime.syncAll(owner)
        val otherLearner = second.store.visible(owner).single()
        second.runtime.db.disableNetwork().cloudAwait()
        write(second.store.preferences(otherLearner), mapOf("lesson.${b.id}.learned_phrases" to b.phrases[0].id,
            "achievement.${b.id}.${b.phrases[0].id}" to 90))
        second.runtime.markPending(otherLearner); assertFalse(second.runtime.isSynced(otherLearner))
        assertTrue(runCatching { withTimeout(5000) { second.runtime.syncPending(owner) } }.isFailure)
        assertFalse(second.runtime.isSynced(otherLearner))
        val ref = first.runtime.db.collection("accounts").document(owner).collection("profiles").document(identity.profileId)
        assertFalse(ref.collection("languageProgress").document("zh_A2").get(Source.SERVER).cloudAwait().exists())
        write(first.store.preferences(learner), mapOf("lesson.${a.id}.learned_phrases" to a.phrases.take(2).map { it.id }.sorted().joinToString("|")))
        first.runtime.markPending(learner); second.runtime.db.enableNetwork().cloudAwait()
        listOf(async(Dispatchers.IO) { first.runtime.syncPending(owner) }, async(Dispatchers.IO) { second.runtime.syncPending(owner) }).awaitAll()
        first.runtime.syncAll(owner); second.runtime.syncAll(owner)
        for ((d, person) in listOf(first to learner, second to otherLearner)) {
            val prefs = d.store.preferences(person)
            assertEquals(a.phrases.take(2).map { it.id }.toSet(), prefs.getString("lesson.${a.id}.learned_phrases", "")!!.split('|').toSet())
            assertEquals(b.phrases[0].id, prefs.getString("lesson.${b.id}.learned_phrases", null))
            assertEquals(95, prefs.getInt(enKey, 0)); assertTrue(d.runtime.isSynced(person))
        }
        val foreign = device(); register(foreign)
        assertTrue(runCatching { foreign.runtime.db.collection("accounts").document(owner).collection("profiles").document(identity.profileId)
            .collection("languageProgress").document("zh_A1").get(Source.SERVER).cloudAwait() }.isFailure)
    }
}
