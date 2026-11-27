package com.nghialcvn.speakivra

import java.security.MessageDigest

/** Public identifier only. Possession of this serial is never proof of profile ownership. */
internal fun guestProfileId(publicKey: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(publicKey).joinToString("") { "%02x".format(it.toInt() and 255) }
internal fun guestSerial(profileId: String): String {
    require(Regex("[a-f0-9]{64}").matches(profileId))
    return "SPK-" + profileId.take(12).uppercase().chunked(4).joinToString("-")
}
internal data class GuestIdentity(val profileId: String, val keyAlias: String,
    val ownerUid: String? = null) {
    val serial get() = guestSerial(profileId)
    fun canOpen(uid: String?) = ownerUid == null || ownerUid == uid
}
internal data class GuestClaimReceipt(val profileId: String, val ownerUid: String)
/** Used only after an authenticated server response, never on an offline button click.
 * The backend must independently enforce the immutable owner in a transaction. */
internal fun acceptGuestClaim(identity: GuestIdentity, receipt: GuestClaimReceipt, signedInUid: String): GuestIdentity {
    require(receipt.profileId == identity.profileId && receipt.ownerUid == signedInUid && signedInUid.isNotBlank())
    require(identity.ownerUid == null || identity.ownerUid == signedInUid) { "Hồ sơ đã thuộc tài khoản khác." }
    return identity.copy(ownerUid = signedInUid)
}
