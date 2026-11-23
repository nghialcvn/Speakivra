package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class VoiceChoicesTest {
    @Test fun defaultRetainsOriginalBundledNeuralAudio() {
        val original = VoiceChoice()
        assertEquals("en-US", original.languageTag)
        assertEquals("Anh-Mỹ (US)", original.accentLabel)
        assertTrue(original.usesBundledAudio)
    }

    @Test fun otherAccentsOrGendersNeverClaimBundledClip() {
        val male = VoiceChoice("US", "male")
        val british = VoiceChoice("GB", "female")
        val custom = VoiceChoice("US", "female", "installed-system-voice")
        assertFalse(male.usesBundledAudio)
        assertFalse(british.usesBundledAudio)
        assertFalse(custom.usesBundledAudio)
        assertEquals("en-GB", british.languageTag)
        assertEquals("Giọng nam", male.genderLabel)
        assertEquals("Anh-Anh (UK)", british.accentLabel)
    }

    @Test fun copyPreservesVoiceChoiceWithoutTouchingProgress() {
        val old = VoiceChoice()
        val changed = old.copy(accent = "GB", gender = "male", voiceId = "tested-voice")
        assertEquals("en-US", old.languageTag)
        assertEquals("en-GB", changed.languageTag)
        assertEquals("tested-voice", changed.voiceId)
        assertFalse(changed.usesBundledAudio)
    }
}
