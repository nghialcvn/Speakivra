package com.nghialcvn.speakivra

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioThemeTest {
    private fun contrast(first: Color, second: Color): Float {
        val a = first.luminance(); val b = second.luminance()
        return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
    }
    @Test fun recognitionFeedbackUsesGreenAndRedWithoutChangingNeonAccent() {
        for (palette in listOf(LightStudio, DarkStudio, NeonStudio)) {
            assertTrue("Matched words must look green", palette.correctWord.green > palette.correctWord.red)
            assertTrue("Unmatched words must look red", palette.incorrectWord.red > palette.incorrectWord.green && palette.incorrectWord.red > palette.incorrectWord.blue)
            assertTrue(contrast(palette.correctWord, palette.card) >= 3f)
            assertTrue(contrast(palette.incorrectWord, palette.card) >= 3f)
        }
        org.junit.Assert.assertEquals(Color(0xFF00DEFA), NeonStudio.coral)
        org.junit.Assert.assertNotEquals(NeonStudio.coral, NeonStudio.incorrectWord)
    }
    @Test fun bodyCopyRemainsReadableOnEveryCardInBothThemes() {
        for (palette in listOf(LightStudio, DarkStudio)) {
            for (background in listOf(palette.background, palette.card, palette.peach, palette.lavender, palette.mint)) {
                assertTrue("Body contrast is too low", contrast(palette.ink, background) >= 4.5f)
                assertTrue("Secondary copy contrast is too low", contrast(palette.muted, background) >= 4.5f)
            }
            assertTrue("Feedback copy must remain legible", contrast(palette.green, palette.mint) >= 4.5f)
        }
    }
}
