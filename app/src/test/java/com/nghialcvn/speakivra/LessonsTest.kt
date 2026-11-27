package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class LessonsTest {
    @Test fun a1KeepsTheOriginalTenTopics() {
        assertEquals(10, Lessons.a1.size)
        assertEquals(200, Lessons.a1.sumOf { it.phrases.size })
        assertEquals(70, Lessons.a1.sumOf { it.conversation.size })
        assertEquals(listOf(
            "Greetings & Introductions", "My Family", "Daily Routine", "Food & Drinks",
            "Shopping", "Directions", "My Hobbies", "Time & Dates", "Travel", "Real Conversation"
        ), Lessons.a1.take(10).map { it.title })
        Lessons.a1.take(10).forEach { lesson ->
            assertEquals(20, lesson.phrases.size)
            assertEquals(7, lesson.conversation.size)
        }
    }

    @Test fun a2IsSeparateAndOffersRealContent() {
        assertEquals(15, Lessons.a2.size)
        assertEquals(450, Lessons.a2.sumOf { it.phrases.size })
        assertEquals(150, Lessons.a2.sumOf { it.conversation.size })
        Lessons.a2.forEach { lesson ->
            assertTrue(lesson.id.startsWith("a2_"))
            assertEquals("A2", Lessons.levelOf(lesson))
            assertEquals(30, lesson.phrases.size)
            assertEquals(10, lesson.conversation.size)
        }
        assertEquals("A1", Lessons.levelOf(Lessons.a1.first()))
        assertEquals(45, Lessons.all.filter { Lessons.levelOf(it) in listOf("A1", "A2", "B1") }.map { it.id }.distinct().size)
    }

    @Test fun everyTopicHasVietnameseTranslationsPronunciationTipsAndWorkingReview() {
        Lessons.all.forEach { lesson ->
            assertTrue(lesson.title.isNotBlank() && lesson.vietnamese.isNotBlank())
            assertTrue(lesson.phrases.all { it.english.isNotBlank() && it.vietnamese.isNotBlank() && it.tip.isNotBlank() })
            assertTrue(lesson.conversation.all { turn ->
                turn.prompt.isNotBlank() && turn.translation.isNotBlank() &&
                    turn.reply.isNotBlank() && turn.replyTranslation.isNotBlank() &&
                    lesson.phrases.any { it.english == turn.reply }
            })
            repeat(3) { index ->
                val question = reviewQuestion(lesson, index)
                assertEquals(3, question.options.distinct().size)
                assertEquals(question.phrase.vietnamese, question.options[question.answer])
                assertEquals(1, question.options.count { it == question.phrase.vietnamese })
            }
        }
    }

    @Test fun legacyPhrasesAndNeuralAudioAreUnchangedAndNewTextUsesDeviceTts() {
        val firstTen = Lessons.a1.take(10)
        firstTen.forEach { lesson ->
            repeat(8) { index ->
                assertNotNull(SpeechProfiles.forText(lesson.phrases[index].english).clip)
            }
            repeat(3) { index ->
                assertNotNull(SpeechProfiles.forText(lesson.conversation[index].prompt).clip)
                assertNotNull(SpeechProfiles.forText(lesson.conversation[index].reply).clip)
            }
            assertTrue(lesson.phrases.drop(12).all { SpeechProfiles.forText(it.english).clip == null })
        }
        assertEquals("My name is Alex.", firstTen.first().phrases[2].english)
        assertEquals("I slept well, thank you.", firstTen.first().phrases[8].english)
        assertEquals("I am twenty-five years old.", firstTen.first().phrases[12].english)
        assertEquals("How did you sleep?", firstTen.first().conversation[3].prompt)
        assertEquals("Where do you live now?", firstTen.first().conversation[5].prompt)
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
        assertEquals(a.conversation.lastIndex, corrupted.turn)
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
