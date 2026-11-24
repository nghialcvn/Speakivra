package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class VersionPolicyTest {
    @Test fun versionRolloverDoesNotCreateFourthDigitPatch() {
        assertEquals("1.0.1", VersionPolicy.next("1.0.0"))
        assertEquals("1.1.0", VersionPolicy.next("1.0.9"))
        assertEquals("1.2.0", VersionPolicy.next("1.1.9"))
        assertEquals("2.0.0", VersionPolicy.next("1.9.9"))
        assertTrue(VersionPolicy.valid("1.0.0"))
        assertFalse(VersionPolicy.valid("1.0.10"))
        assertFalse(VersionPolicy.valid("1.10.0"))
        assertFalse(VersionPolicy.valid("0.1.18"))
    }
}
