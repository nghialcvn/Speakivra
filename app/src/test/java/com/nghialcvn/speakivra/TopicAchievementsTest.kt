package com.nghialcvn.speakivra
import org.junit.Assert.*
import org.junit.Test
class TopicAchievementsTest {
    @Test fun exactBoundaries() {
        listOf(0 to 0, 59 to 0, 60 to 1, 74 to 1, 75 to 2, 94 to 2, 95 to 3, 100 to 3).forEach { (score, expected) ->
            assertEquals(expected, achievementHearts(score))
        }
    }
    @Test fun missingScoresAreNotInventedAndContentGrowthKeepsIds() {
        val lesson = Lessons.a1.first()
        val scores = lesson.phrases.associate { it.id to 100 }
        assertEquals(100, topicAccuracy(lesson) { scores[it] ?: 0 })
        val expanded = lesson.copy(phrases = lesson.phrases + lesson.phrases.first().copy(id = "new-sentence"))
        assertTrue(topicAccuracy(expanded) { scores[it] ?: 0 } < 100)
        assertEquals(0, topicAccuracy(lesson) { 0 })
        assertEquals(100, topicAccuracy(lesson.copy(phrases = lesson.phrases.reversed())) { scores[it] ?: 0 })
    }
}
