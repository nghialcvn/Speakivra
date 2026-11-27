package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class SpeechNamesTest {
    @Test fun screenshotMismatchStillPassesAtSeventyFiveWithoutRewritingName() {
        val result = matchWords("My name is Nghĩa.", "my name is India")
        assertEquals(75, result.matchPercent)
        assertEquals(listOf(true, true, true, false), result.matched)
        assertFalse(result.nameMatchesExactly)
        assertTrue(result.canContinue)
        assertFalse(matchWords("one two three four five", "one two").canContinue)
        assertFalse(matchWords("My name is Nghĩa.", null).canContinue)
        assertTrue(matchWords("My name is Nghĩa.", "My name is Nghia").matched.all { it })
    }
    @Test fun nameHintsPreserveAccentsAndAddOnlyEquivalentSpellingFrames() {
        val hints = speechNameHints("Đặng Minh Nghĩa")
        assertTrue("Đặng Minh Nghĩa" in hints); assertTrue("Dang Minh Nghia" in hints)
        assertTrue("My name is Đặng Minh Nghĩa" in hints)
        assertTrue("I'm Dang Minh Nghia" in hints)
        assertTrue(speechNameHints(null).isEmpty())
    }
    @Test fun selectOnlyActualProviderAlternativeWithTheCompleteName() {
        assertEquals("my name is Nghia", selectNameHypothesis(listOf("my name is India", "my name is Nghia"), "Nghĩa"))
        assertEquals("I'm Đặng Minh", selectNameHypothesis(listOf("I'm Dang", "I'm Đặng Minh"), "Đặng Minh"))
        assertEquals("my name is India", selectNameHypothesis(listOf("my name is India"), "Nghĩa"))
        assertEquals("my name is Linh", selectNameHypothesis(listOf("my name is Linh", "my name is Nghia"), "Linh"))
        assertEquals("I am going home", selectNameHypothesis(listOf("I am going home", "I am going home now"), "Nghĩa"))
        assertEquals("Nghia", selectNameHypothesis(listOf("India", "Nghia"), "Nghĩa", "my name is"))
        assertNull(selectNameHypothesis(emptyList(), "Nghĩa"))
    }
}
