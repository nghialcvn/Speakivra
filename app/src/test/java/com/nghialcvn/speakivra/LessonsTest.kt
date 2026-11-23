package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class LessonsTest {
    @Test fun allApprovedTopicsHavePracticeConversationReviewAndOfflineAudio() {
        assertEquals(listOf("Greetings & Introductions", "My Family", "Daily Routine", "Food & Drinks", "Shopping", "Directions", "My Hobbies", "Time & Dates", "Travel", "Real Conversation"), Lessons.all.map { it.title })
        assertEquals(10, Lessons.all.map { it.id }.distinct().size)
        Lessons.all.forEach { lesson ->
            assertEquals(12, lesson.phrases.size)
            assertEquals(5, lesson.conversation.size)
            lesson.phrases.forEachIndexed { index, phrase ->
                assertTrue(phrase.vietnamese.isNotBlank() && phrase.tip.isNotBlank())
                if (index < 8) assertNotNull(SpeechProfiles.forText(phrase.english).clip)
            }
            lesson.conversation.forEachIndexed { index, turn ->
                assertTrue(turn.translation.isNotBlank() && turn.replyTranslation.isNotBlank())
                if (index < 3) {
                    assertNotNull(SpeechProfiles.forText(turn.prompt).clip)
                    assertNotNull(SpeechProfiles.forText(turn.reply).clip)
                }
                assertTrue(lesson.phrases.any { it.english == turn.reply })
            }
            repeat(3) { index ->
                val question = reviewQuestion(lesson, index)
                assertEquals(3, question.options.distinct().size)
                assertEquals(question.phrase.vietnamese, question.options[question.answer])
                assertEquals(1, question.options.count { it == question.phrase.vietnamese })
            }
        }
    }
    @Test fun expandedContentIsAppendOnlyAndUsesDeviceTtsForNewLines() {
        assertEquals(120, Lessons.all.sumOf { it.phrases.size })
        assertEquals(50, Lessons.all.sumOf { it.conversation.size })
        Lessons.all.forEach { lesson ->
            assertEquals(8, lesson.phrases.take(8).count { SpeechProfiles.forText(it.english).clip != null })
            assertTrue(lesson.phrases.drop(8).all { SpeechProfiles.forText(it.english).clip == null })
            assertTrue(lesson.conversation.drop(3).all { turn ->
                lesson.phrases.drop(8).any { it.english == turn.reply }
            })
        }
    }
    @Test fun oldFirstFourPhrasePositionsStillMeanTheSameContent() {
        assertEquals(listOf("Hello! How are you?", "I'm fine, thank you.", "My name is Alex.", "Nice to meet you!"), Lessons.all.first().phrases.take(4).map { it.english })
    }
    @Test fun lessonRoundOnlyCompletesAfterReviewAndCannotBeCountedTwice() {
        Lessons.all.forEach { lesson ->
            var state = LessonProgress()
            repeat(lesson.phrases.size) { state = state.nextPhrase(lesson) }
            assertEquals("chat", state.phase)
            assertEquals(0, state.rounds)
            repeat(lesson.conversation.size) { state = state.nextTurn(lesson) }
            assertEquals("review", state.phase)
            state = state.answerReview(true).answerReview(false)
            assertEquals(0, state.rounds)
            assertEquals(1, state.correct)
            state = state.answerReview(true)
            assertEquals("summary", state.phase)
            assertEquals(2, state.correct)
            assertEquals(1, state.rounds)
            assertEquals(state, state.answerReview(true))
            assertEquals(LessonProgress(rounds = 1), state.restart())
        }
    }
    @Test fun switchingLessonsAndRestoringDoesNotMutateOtherProgress() {
        val a = Lessons.all.first()
        val b = Lessons.all[1]
        val savedA = LessonProgress(phrase = 3, turn = 1, rounds = 2)
        val savedB = LessonProgress(phrase = 6, rounds = 1)
        val data = mapOf(a.id to savedA, b.id to savedB)
        assertEquals(savedA, data.getValue(a.id).sanitized(a))
        assertEquals(savedB, data.getValue(b.id).sanitized(b))
        val updated = data + (a.id to savedA.nextPhrase(a))
        assertEquals(savedB, updated[b.id])
        assertEquals(4, updated.getValue(a.id).phrase)
        val corrupted = LessonProgress(-4, 99, 99, 99, -5, "unknown").sanitized(a)
        assertEquals(0, corrupted.phrase)
        assertEquals(2, corrupted.turn)
        assertEquals(3, corrupted.correct)
        assertEquals(0, corrupted.rounds)
        assertEquals("repeat", corrupted.phase)
    }
    @Test fun legacyPreferencesMigrateOnlyIntoTheOriginalLessonWithoutOverwritingNewProgress() {
        val integers = mutableMapOf("phrase" to 3, "completed" to 7, "playback_speed" to 125)
        val strings = mutableMapOf<String, String>()
        fun load(item: Lesson) = restoreLessonProgress(item,
            { key, default -> integers[key] ?: default }, { key, default -> strings[key] ?: default })
        assertEquals(3, load(Lessons.all.first()).phrase)
        assertEquals(0, load(Lessons.all[1]).phrase)
        integers["lesson.greetings.phrase"] = 6
        strings["lesson.greetings.phase"] = "chat"
        assertEquals(6, load(Lessons.all.first()).phrase)
        assertEquals("chat", load(Lessons.all.first()).phase)
        assertEquals(3, integers.getValue("phrase"))
        assertEquals(7, integers.getValue("completed"))
        assertEquals(125, integers.getValue("playback_speed"))
    }

}
