package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class A2ExpansionTest {
    @Test fun originalA2ContentAndPositionsArePreservedAndCompleteSavesResumeOnlyNewIds() {
        A2Lessons.original.forEach { old ->
            val expanded = Lessons.a2.single { it.id == old.id }
            assertEquals(old.phrases, expanded.phrases.take(old.phrases.size))
            assertEquals(old.conversation, expanded.conversation.take(old.conversation.size))
            assertEquals(old.phrases.map { it.id }, legacyLessonContent.getValue(old.id).first)
            assertEquals(old.conversation.map { it.id }, legacyLessonContent.getValue(old.id).second)
            var resumed = restoreLessonProgress(expanded,
                { key, default -> if (key == "lesson.${old.id}.rounds") 3 else default },
                { key, default -> if (key == "lesson.${old.id}.phase") "summary" else default })
            assertEquals("repeat", resumed.phase)
            assertEquals(12, resumed.phrase)
            assertTrue(resumed.supplemental)
            assertEquals(old.phrases.map { it.id }.toSet(), resumed.learnedPhrases)
            assertEquals(old.conversation.map { it.id }.toSet(), resumed.learnedTurns)
            repeat(18) { resumed = resumed.nextPhrase(expanded) }
            assertEquals("chat", resumed.phase)
            assertEquals(5, resumed.turn)
            repeat(10) { resumed = resumed.nextTurn(expanded) }
            assertEquals("summary", resumed.phase)
            assertEquals(3, resumed.rounds)
            assertTrue(resumed.learnedPhrases!!.containsAll(expanded.phrases.map { it.id }))
            assertTrue(resumed.learnedTurns!!.containsAll(expanded.conversation.map { it.id }))
        }
    }
    @Test fun savedIdsSurviveInsertionAndNewLessonsStartUnlearned() {
        val original = A2Lessons.original.first()
        val expanded = Lessons.a2.first()
        val state = LessonProgress(phase = "summary", rounds = 2,
            learnedPhrases = original.phrases.map { it.id }.toSet(),
            learnedTurns = original.conversation.map { it.id }.toSet())
        val reordered = expanded.copy(phrases = expanded.phrases.reversed())
        val resumed = state.reconcile(reordered)
        assertEquals(expanded.phrases.last().id, reordered.phrases[resumed.phrase].id)
        assertEquals(state.learnedPhrases, resumed.learnedPhrases)
        Lessons.a2.drop(5).forEach { lesson ->
            val fresh = restoreLessonProgress(lesson, { _, default -> default }, { _, default -> default })
            assertEquals(0, fresh.phrase)
            assertEquals("repeat", fresh.phase)
            assertTrue(fresh.learnedPhrases!!.isEmpty())
            assertTrue(fresh.learnedTurns!!.isEmpty())
        }
    }
    @Test fun practiceGetsLongerInStagesWithDistinctBilingualExchanges() {
        fun words(text: String) = Regex("\\S+").findAll(text).count()
        Lessons.a2.forEach { lesson ->
            val early = lesson.phrases.take(10).map { words(it.english) }.average()
            val middle = lesson.phrases.drop(10).take(10).map { words(it.english) }.average()
            val late = lesson.phrases.takeLast(10).map { words(it.english) }.average()
            assertTrue("${lesson.id}: $early -> $middle -> $late", early < middle && middle < late)
            assertEquals(30, lesson.phrases.map { it.english }.distinct().size)
            assertEquals(15, lesson.conversation.map { it.prompt }.distinct().size)
            lesson.conversation.forEach { turn ->
                assertTrue(lesson.phrases.any { it.english == turn.reply && it.vietnamese == turn.replyTranslation })
            }
            assertTrue(lesson.phrases.all { it.tip.isNotBlank() })
        }
    }
}
