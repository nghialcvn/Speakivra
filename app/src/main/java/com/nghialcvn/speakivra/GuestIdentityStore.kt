package com.nghialcvn.speakivra

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** A 256-bit one-time capability, encrypted with Android Keystore. Serial alone cannot claim it. */
internal class GuestIdentityStore(context: Context) {
    private companion object { val identityLock = Any() }
    private val registry = context.getSharedPreferences("speakivra_guest_identity", 0)
    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    fun peek(learner: Learner): GuestIdentity? = registry.getString(learner.id, null)?.let { decode(JSONObject(it)) }
    fun ensure(learner: Learner): GuestIdentity = synchronized(identityLock) {
        learnerPreferenceName(learner.id)
        val previous = registry.getString(learner.id, null)?.let(::JSONObject)
        if (previous != null && previous.optInt("scheme") == 2) return@synchronized decode(previous)
        // The P-256 prototype was never released or claimed online. Never replace an owned identity.
        require(previous == null || !previous.has("ownerUid")) { "Không thể thay mã hồ sơ đã liên kết." }
        val secret = ByteArray(32).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it.toInt() and 255) }
        val alias = "speakivra-guest-v2-${UUID.randomUUID()}"
        val key = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key) }
        val encrypted = cipher.doFinal(secret.toByteArray(Charsets.UTF_8))
        val identity = GuestIdentity(guestProfileId(secret.toByteArray(Charsets.UTF_8)), alias)
        val record = encode(identity).put("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .put("ciphertext", Base64.encodeToString(encrypted, Base64.NO_WRAP))
        if (!registry.edit().putString(learner.id, record.toString()).commit()) {
            keyStore().deleteEntry(alias); error("Chưa lưu được mã hồ sơ. Tiến độ vẫn được giữ trên máy.")
        }
        identity
    }
    /** Read only for the authenticated claim. It is not a login password and never enters a progress payload. */
    fun proof(learner: Learner, uid: String): ByteArray = synchronized(identityLock) {
        val identity = ensure(learner)
        require(uid.isNotBlank() && (identity.ownerUid == null || identity.ownerUid == uid)) { "Hồ sơ đã thuộc tài khoản khác." }
        val record = JSONObject(requireNotNull(registry.getString(learner.id, null)))
        val key = keyStore().getKey(identity.keyAlias, null) as? SecretKey
            ?: error("Cần thiết bị gốc để xác nhận hồ sơ khách này.")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, Base64.decode(record.getString("iv"), Base64.NO_WRAP)))
        }
        val clear = cipher.doFinal(Base64.decode(record.getString("ciphertext"), Base64.NO_WRAP))
        check(guestProfileId(clear) == identity.profileId)
        clear
    }
    fun recordServerReceipt(learner: Learner, receipt: GuestClaimReceipt, uid: String): GuestIdentity = synchronized(identityLock) {
        val bound = acceptGuestClaim(ensure(learner), receipt, uid)
        val record = JSONObject(requireNotNull(registry.getString(learner.id, null))).put("ownerUid", bound.ownerUid)
        check(registry.edit().putString(learner.id, record.toString()).commit()); bound
    }
    /** Owned cloud restores need no one-time capability and cannot be claimed by a different account. */
    fun restoreOwned(learner: Learner, profileId: String, uid: String) = synchronized(identityLock) {
        require(Regex("[a-f0-9]{64}").matches(profileId) && uid.isNotBlank())
        val old = peek(learner)
        require(old == null || (old.profileId == profileId && (old.ownerUid == null || old.ownerUid == uid)))
        if (old == null) check(registry.edit().putString(learner.id, encode(GuestIdentity(profileId, "", uid)).toString()).commit())
        else recordServerReceipt(learner, GuestClaimReceipt(profileId, uid), uid)
    }
    private fun encode(value: GuestIdentity) = JSONObject().put("scheme", 2).put("profileId", value.profileId)
        .put("keyAlias", value.keyAlias).apply { value.ownerUid?.let { put("ownerUid", it) } }
    private fun decode(json: JSONObject): GuestIdentity {
        val id = json.getString("profileId"); require(Regex("[a-f0-9]{64}").matches(id))
        return GuestIdentity(id, json.optString("keyAlias"), if (json.has("ownerUid")) json.getString("ownerUid") else null)
    }
}
