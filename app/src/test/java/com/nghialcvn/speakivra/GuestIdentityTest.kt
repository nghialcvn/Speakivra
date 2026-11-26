package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec

class GuestIdentityTest {
    @Test fun serialUsesThePublicDigestAndNeverTheOneTimeClaimSecret() {
        val secret = "1".repeat(64)
        val id = guestProfileId(secret.toByteArray(Charsets.UTF_8))
        val serial = guestSerial(id)
        assertEquals(serial, guestSerial(id))
        assertTrue(Regex("SPK-(?:[A-F0-9]{4}-){4}[A-F0-9]{4}").matches(serial))
        assertNotEquals(id, guestProfileId("2".repeat(64).toByteArray(Charsets.UTF_8)))
        assertFalse(serial.contains(secret))
    }
    @Test fun bindingIsIdempotentForTheSameOwnerButCannotBeReassigned() {
        val guest = GuestIdentity("a".repeat(64), "device-key")
        assertTrue(guest.canOpen(null))
        val owned = acceptGuestClaim(guest, GuestClaimReceipt(guest.profileId, "A"), "A")
        assertEquals(owned, acceptGuestClaim(owned, GuestClaimReceipt(owned.profileId, "A"), "A"))
        assertTrue(owned.canOpen("A")); assertFalse(owned.canOpen("B")); assertFalse(owned.canOpen(null))
        assertThrows(IllegalArgumentException::class.java) { acceptGuestClaim(owned, GuestClaimReceipt(owned.profileId, "B"), "B") }
        assertThrows(IllegalArgumentException::class.java) { acceptGuestClaim(guest, GuestClaimReceipt("b".repeat(64), "A"), "A") }
        assertThrows(IllegalArgumentException::class.java) { acceptGuestClaim(guest, GuestClaimReceipt(guest.profileId, "B"), "A") }
    }
    @Test fun unavailableCloudNeverProducesFakeAuthenticationVerificationOrBinding() {
        val gateway = UnconfiguredAccountGateway()
        assertFalse(gateway.configured); assertNull(gateway.session)
        LoginProvider.entries.forEach { gateway.signIn(it, "example@example.com", "not-a-real-password") { result -> assertTrue(result.isFailure) } }
        gateway.register("example@example.com", "not-a-real-password") { assertTrue(it.isFailure) }
        gateway.resetPassword("example@example.com") { assertTrue(it.isFailure) }
        gateway.verifyEmail { assertTrue(it.isFailure) }
        gateway.refreshSession { assertTrue(it.isFailure) }
        gateway.claim(GuestIdentity("a".repeat(64), ""), byteArrayOf()) { assertTrue(it.isFailure) }
    }
    @Test fun exportedLearningPayloadCannotIncludePasswordsTokensRecordingsOrOtherProfiles() {
        val lesson = Lessons.a1.first(); val phrase = lesson.phrases.first()
        val key = "lesson.${lesson.id}.learned_phrases"
        val snapshot = profileSyncPayload(Learner(ORIGINAL_LEARNER, "Nghĩa", 0), GuestIdentity("a".repeat(64), "alias"),
            mapOf(key to "${phrase.id}|unknown-id", "achievement.${lesson.id}.${phrase.id}" to 95,
                "password" to "secret", "auth.token" to "secret", "voice_id" to "device-only", "recording" to "/private/path",
                "lesson.unknown.phase" to "summary", "lesson.${lesson.id}.phase" to "repeat"))
        val data = snapshot.getJSONObject("progress")
        assertEquals(phrase.id, data.getString(key))
        assertEquals(95, data.getInt("achievement.${lesson.id}.${phrase.id}"))
        assertEquals(3, data.length())
        listOf("secret", "device-only", "/private/path", "unknown-id").forEach { assertFalse(snapshot.toString().contains(it)) }
        assertEquals("Nghĩa", snapshot.getString("name"))
    }
    @Test fun accountInputValidationDoesNotConfuseRegistrationWithProfileCreation() {
        assertNotNull(accountFormError("invalid", "long-password"))
        assertNotNull(accountFormError("example@example.com", "short"))
        assertNotNull(accountFormError("example@example.com", "long-password", "different"))
        assertNull(accountFormError("example@example.com", "long-password", "long-password"))
    }
}
