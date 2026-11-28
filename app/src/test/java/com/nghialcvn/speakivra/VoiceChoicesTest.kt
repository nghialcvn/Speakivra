package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class VoiceChoicesTest {
    @Test fun mandarinVoicesAcceptRegionalAndGenericTags() {
        for (tag in listOf("zh-CN", "zh-Hans", "zh-SG", "cmn-Hans-CN", "zh-TW"))
            assertTrue(tag, isMandarinLocale(java.util.Locale.forLanguageTag(tag)))
        for (tag in listOf("yue-HK", "zh-HK", "en-US", "vi-VN"))
            assertFalse(tag, isMandarinLocale(java.util.Locale.forLanguageTag(tag)))
    }

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
        assertEquals("en-US", british.languageTag)
        assertEquals("Giọng nam", male.genderLabel)
        assertEquals("Anh-Mỹ (US)", british.accentLabel)
    }

    @Test fun copyPreservesVoiceChoiceWithoutTouchingProgress() {
        val old = VoiceChoice()
        val changed = old.copy(accent = "GB", gender = "male", voiceId = "tested-voice")
        assertEquals("en-US", old.languageTag)
        assertEquals("en-US", changed.languageTag)
        assertEquals("tested-voice", changed.voiceId)
        assertFalse(changed.usesBundledAudio)
    }
}
