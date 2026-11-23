package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class WordMatchTest {
    @Test fun approvedExamplesColorReferenceWordsAndGateNext() {
        val pass = matchWords("My name is Alex.", "my name is I like")
        assertEquals(listOf(true, true, true, false), pass.matched)
        assertEquals(25, pass.percent)
        assertTrue(pass.canContinue)
        val fail = matchWords("My name is Alex.", "my family likes music")
        assertEquals(listOf(true, false, false, false), fail.matched)
        assertEquals(75, fail.percent)
        assertFalse(fail.canContinue)
    }
    @Test fun sixtyPercentExactlyIsBlocked() {
        assertFalse(matchWords("one two three four five", "one two").canContinue)
        assertTrue(matchWords("one two three four five", "one two three").canContinue)
        assertTrue(matchWords("one two three four", "one two").canContinue)
    }
    @Test fun noFinalResultCannotUnlockOrColorTheSentence() {
        listOf(null, "", " ", "!?").forEach {
            val result = matchWords("My name is Alex.", it)
            assertFalse(result.hasResult)
            assertFalse(result.canContinue)
        }
    }
    @Test fun punctuationAndCaseAreIgnoredButOrderAndRepetitionsMatter() {
        assertEquals(listOf(true, true, true, true), matchWords("My name is Alex.", "MY, NAME IS ALEX!").matched)
        assertEquals(1, matchWords("go go home", "go").matched.count { it })
        assertEquals(1, matchWords("one two three", "three two one").matched.count { it })
        assertFalse(matchWords("", "hello").canContinue)
    }
}
