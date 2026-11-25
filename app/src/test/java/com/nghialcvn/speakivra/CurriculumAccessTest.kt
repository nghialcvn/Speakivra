package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class CurriculumAccessTest {
    @Test fun eachLevelStartsWithExactlyThreeOpenTopics() {
        listOf(Lessons.a1, Lessons.a2, Lessons.b1).forEach { level ->
            assertEquals(3, level.count { topicIsOpen(it, Lessons.all) { LessonProgress() } })
            assertTrue(level.take(3).all { topicIsOpen(it, Lessons.all) { LessonProgress() } })
            assertFalse(topicIsOpen(level[3], Lessons.all) { LessonProgress(phase = "review", review = 2) })
        }
    }
    @Test fun thirdCompletionUnlocksFourthAndLaterTopicsRequireTheWholeChain() {
        listOf(Lessons.a1, Lessons.a2, Lessons.b1).forEach { level ->
            val states = mutableMapOf(level[2].id to LessonProgress(rounds = 1, phase = "summary"))
            fun load(item: Lesson) = states[item.id] ?: LessonProgress()
            assertTrue(topicIsOpen(level[3], Lessons.all, ::load))
            assertFalse(topicIsOpen(level[4], Lessons.all, ::load))
            states[level[3].id] = LessonProgress(rounds = 1, phase = "summary")
            assertTrue(topicIsOpen(level[4], Lessons.all, ::load))
            states[level[2].id] = LessonProgress(rounds = 1) // Repeating #3 does not relock #4.
            assertTrue(topicIsOpen(level[4], Lessons.all, ::load))
            states.remove(level[2].id)
            assertFalse(topicIsOpen(level[4], Lessons.all, ::load)) // Cannot skip the chain.
        }
    }
    @Test fun anotherLearnerAndAnotherLevelCannotBorrowUnlocks() {
        val a1 = Lessons.a1
        val completed = setOf(a1[2].id, a1[3].id)
        val first: (Lesson) -> LessonProgress = { LessonProgress(rounds = if (it.id in completed) 1 else 0) }
        assertTrue(topicIsOpen(a1[4], Lessons.all, first))
        assertFalse(topicIsOpen(a1[4], Lessons.all) { LessonProgress() })
        assertFalse(topicIsOpen(Lessons.b1[3], Lessons.all, first))
        assertFalse(topicIsOpen(a1[0].copy(id = "unknown"), Lessons.all, first))
    }
    @Test fun b1HasRealBilingualStagedContentAndWorkingReview() {
        assertEquals(20, Lessons.b1.size)
        assertEquals(700, Lessons.b1.sumOf { it.phrases.size })
        assertEquals(300, Lessons.b1.sumOf { it.conversation.size })
        Lessons.b1.forEach { lesson ->
            assertEquals("B1", Lessons.levelOf(lesson))
            assertEquals(35, lesson.phrases.map { it.english }.distinct().size)
            assertEquals(15, lesson.conversation.map { it.prompt }.distinct().size)
            val early = lesson.phrases.take(10).map { it.english.split(' ').size }.average()
            val middle = lesson.phrases.drop(10).take(10).map { it.english.split(' ').size }.average()
            val late = lesson.phrases.takeLast(10).map { it.english.split(' ').size }.average()
            assertTrue("${lesson.id}: $early -> $middle -> $late", early < middle && middle < late)
            assertTrue(lesson.phrases.all { it.vietnamese.isNotBlank() && it.tip.isNotBlank() })
            lesson.conversation.forEach { turn ->
                assertTrue(lesson.phrases.any { it.english == turn.reply && it.vietnamese == turn.replyTranslation })
            }
        }
    }
}
