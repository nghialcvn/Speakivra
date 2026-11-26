package com.nghialcvn.speakivra

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.UUID

/** Migrate identities without changing learner IDs or any progress preference file. */
internal class GuestIdentityStore(context: Context) {
    private companion object { val identityLock = Any() }
    private val registry = context.getSharedPreferences("speakivra_guest_identity", 0)
    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    fun ensure(learner: Learner): GuestIdentity = synchronized(identityLock) {
        learnerPreferenceName(learner.id)
        registry.getString(learner.id, null)?.let { return@synchronized decode(it) }
        val alias = "speakivra-guest-v1-${UUID.randomUUID()}"
        val pair = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore").run {
            initialize(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256).build())
            generateKeyPair()
        }
        val identity = GuestIdentity(guestProfileId(pair.public.encoded),
            Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP), alias)
        if (!registry.edit().putString(learner.id, encode(identity)).commit()) {
            keyStore().deleteEntry(alias)
            error("Chưa lưu được mã hồ sơ. Tiến độ vẫn được giữ trên máy.")
        }
        identity
    }
    fun proof(learner: Learner, uid: String): ByteArray {
        val identity = ensure(learner)
        require(identity.ownerUid == null || identity.ownerUid == uid) { "Hồ sơ đã thuộc tài khoản khác." }
        val key = keyStore().getKey(identity.keyAlias, null) as? java.security.PrivateKey
            ?: error("Cần thiết bị gốc để xác nhận hồ sơ khách này.")
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(key); update(guestClaimMessage(uid, identity.profileId)); sign()
        }
    }
    // No production caller until the live backend has authenticated and confirmed ownership.
    fun recordServerReceipt(learner: Learner, receipt: GuestClaimReceipt, uid: String): GuestIdentity = synchronized(identityLock) {
        val bound = acceptGuestClaim(ensure(learner), receipt, uid)
        check(registry.edit().putString(learner.id, encode(bound)).commit())
        bound
    }
    private fun encode(value: GuestIdentity) = JSONObject().apply {
        put("profileId", value.profileId); put("publicKey", value.publicKey); put("keyAlias", value.keyAlias)
        value.ownerUid?.let { put("ownerUid", it) }
    }.toString()
    private fun decode(text: String): GuestIdentity {
        val json = JSONObject(text)
        val value = GuestIdentity(json.getString("profileId"), json.getString("publicKey"), json.getString("keyAlias"),
            if (json.has("ownerUid")) json.getString("ownerUid") else null)
        require(guestProfileId(Base64.decode(value.publicKey, Base64.NO_WRAP)) == value.profileId)
        return value // Missing private keys after a restore must never silently generate a new identity.
    }
}
