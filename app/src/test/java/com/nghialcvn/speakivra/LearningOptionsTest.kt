package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class LearningOptionsTest {
    @Test fun englishModeUsesEnglishOnlyExplanationsAndRealComprehensionAnswers() {
        for (lesson in Lessons.all) {
            for (index in 0..2) {
                val review = EnglishStudy.review(lesson, index)
                assertEquals(3, review.options.size)
                assertEquals(3, review.options.distinct().size)
                assertEquals(lesson.conversation[index].prompt, review.prompt)
                assertEquals(lesson.conversation[index].reply, review.options[review.answer])
                assertTrue(review.prompt.isNotBlank())
            }
            for (phrase in lesson.phrases) {
                assertTrue(EnglishStudy.explanation(phrase.english).isNotBlank())
                assertTrue(EnglishStudy.pronunciationTip(phrase.english).isNotBlank())
            }
        }
    }

    @Test fun languageAndRepeatPreferencesHaveExplicitStableKeys() {
        assertEquals("vi", LearningOptions.VIETNAMESE)
        assertEquals("en", LearningOptions.ENGLISH)
        assertFalse(VersionPolicy.valid("1.0.10"))
        assertEquals("1.1.0", VersionPolicy.next("1.0.9"))
    }
}
