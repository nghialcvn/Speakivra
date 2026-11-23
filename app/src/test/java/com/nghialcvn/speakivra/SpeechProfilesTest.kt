package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class SpeechProfilesTest {
    @Test fun contextPausesPreserveEveryWordAndPunctuationInTheLesson() {
        SpeechProfiles.catalog.forEach { profile ->
            assertEquals(profile.text, profile.parts.joinToString(" ") { it.text })
            assertEquals(profile, SpeechProfiles.forText(profile.text))
            assertEquals(0L, profile.parts.last().pauseAfterMs)
            profile.parts.dropLast(1).forEach { part ->
                assertTrue(part.text.endsWith("!") || part.text.endsWith(".") || part.text.endsWith("?"))
                assertTrue(part.pauseAfterMs in 100..250)
            }
        }
    }
    @Test fun allCurrentLessonAndConversationLinesHaveDistinctBundledResources() {
        assertEquals(110, SpeechProfiles.catalog.size)
        assertEquals(110, SpeechProfiles.catalog.map { it.clip }.distinct().size)
        assertTrue(SpeechProfiles.catalog.all { it.clip?.startsWith("voice_") == true })
    }
    @Test fun playbackSpeedsStayOrderedAndPreserveTheChosenRelativeSpeed() {
        val part = SpeechPart("Test sentence.")
        assertTrue(SpeechProfiles.playbackRate(75) < SpeechProfiles.playbackRate(100))
        assertTrue(SpeechProfiles.playbackRate(100) < SpeechProfiles.playbackRate(125))
        assertEquals(1.25f, SpeechProfiles.playbackRate(125) / SpeechProfiles.playbackRate(100), 0.001f)
        assertEquals(1f, SpeechProfiles.fallbackRate(75, part), 0.001f)
        assertEquals(SpeechProfiles.playbackRate(100), SpeechProfiles.playbackRate(-1), 0.001f)
    }
    @Test fun unfamiliarTextFallsBackWithoutInventingExtraSpeech() {
        val text = "Could you repeat that, please?"
        val profile = SpeechProfiles.forText(text)
        assertNull(profile.clip)
        assertEquals(text, profile.parts.single().text)
        for (speed in listOf(75, 100, 125)) assertTrue(SpeechProfiles.fallbackRate(speed, profile.parts.single()) in 0.7f..2f)
    }
}
