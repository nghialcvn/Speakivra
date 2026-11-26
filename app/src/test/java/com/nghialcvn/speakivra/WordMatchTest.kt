package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class WordMatchTest {
    @Test fun matchPercentageReportsCorrectWordsWithoutChangingTheMismatchGate() {
        val all = matchWords("What do you do for work?", "what do you do for work")
        assertEquals(6, all.correct)
        assertEquals(100, all.matchPercent)
        assertEquals(0, all.incorrect)
        assertTrue(all.canContinue)
        val some = matchWords("one two three four five", "one two")
        assertEquals(2, some.correct)
        assertEquals(40, some.matchPercent)
        assertEquals(60, some.percent)
        assertFalse(some.canContinue)
        assertEquals(0, matchWords("", null).matchPercent)
    }
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
    @Test fun vietnameseNamesMatchWithoutProviderDiacriticsAndContractions() {
        assertTrue(matchWords("I'm Nghĩa.", "I am Nghia").matched.all { it })
        assertTrue(matchWords("I'm Đặng Minh.", "I'm Dang Minh").matched.all { it })
        assertFalse(matchWords("I'm Nghĩa.", "I'm Linh").matched.last())
    }
    @Test fun screenshotAgeSentenceMatchesDigitsAndExpandedContractions() {
        val reference = "I am twenty-five years old."
        assertEquals(listOf(true, true, true, true, true), matchWords(reference, "I am 25 years old").matched)
        assertEquals(listOf(true, true, true, true, true), matchWords(reference, "I'm twenty five years old").matched)
        assertEquals(listOf(true, true, true, false, true), matchWords(reference, "I'm 25-year-old").matched)
        assertTrue(matchWords(reference, "I'm 25-year-old").canContinue)
        assertEquals(listOf(true, true, true, true, false), matchWords(reference, "I am 25 years").matched)
        assertEquals(listOf(true, true, false, true, true), matchWords(reference, "I am 26 years old").matched)
    }
    @Test fun contractionsAreSymmetricAndMissingWordsStayWrong() {
        for ((short, full) in listOf("I'm" to "I am", "You're" to "You are", "It's" to "It is", "don't" to "do not", "can't" to "cannot")) {
            assertTrue(matchWords("$short here", "$full here").matched.all { it })
            assertTrue(matchWords("$full here", "$short here").matched.all { it })
        }
        assertFalse(matchWords("I'm here", "I here").matched.first())
        assertFalse(matchWords("I am here", "I here").matched[1])
    }
    @Test fun equivalentCardinalsKeepOriginalDisplayWordOwnership() {
        assertEquals(listOf(true, true, true), matchWords("twenty five dollars", "25 dollars").matched)
        assertTrue(matchWords("one hundred and five dollars", "105 dollars").matched.all { it })
        assertTrue(matchWords("one thousand two hundred dollars", "1,200 dollars").matched.all { it })
        assertTrue(matchWords("105 dollars", "one hundred five dollars").matched.all { it })
        assertEquals(1, matchWords("five five", "5").matched.count { it })
        assertFalse(matchWords("25", "twenty six").matched.single())
    }
    @Test fun clockFormattingPreservesValuesAndOriginalTokenOwnership() {
        val reference = "I wake up at seven."
        for (spoken in listOf("I wake up at 7:00", "I wake up at 07:00", "I wake up at 7", "I wake up at seven o’clock")) {
            assertTrue(spoken, matchWords(reference, spoken).matched.all { it })
            assertTrue(spoken, matchWords(spoken, reference).matched.all { it })
        }
        assertEquals(listOf(true, true, true, true, false), matchWords("I wake up at 8:00", reference).matched)
        assertFalse(matchWords("I wake up at 7:30", reference).matched.last())
        assertTrue(matchWords("At seven thirty", "At 07:30").matched.all { it })
        assertTrue(matchWords("At 07:30", "At seven thirty").matched.all { it })
        for (invalid in listOf("7:99", "25:00", "7:000")) {
            assertFalse(invalid, matchWords(invalid, "seven").matched.single())
        }
        assertFalse(matchWords("seven", "7.01").matched.single())
    }
}
