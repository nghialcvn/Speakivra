package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.io.File

class ExtraPracticeTest {
    @Test fun screenshotClockAnswerPassesWithoutAcceptingWrongTimesOrNegation() {
        val q = ExtraPracticeData.exercises.first { it.id == "exercise_routine" }.questions.first()
        listOf("I wake up at 7:00", "I wake up at 07:00", "I wake up at seven", "I wake up at seven o'clock", "I get up at 7:00").forEach {
            assertTrue(it, evaluateExercise(q, it).accepted)
        }
        listOf("I wake up at 8:00", "I wake up at 7:30", "I don't wake up at 7:00", "I wake at 7:00", "I wake up at 7:99").forEach {
            assertFalse(it, evaluateExercise(q, it).accepted)
        }
    }
    @Test fun equivalentTranslationsAreAcceptedButWrongExtraOrMissingWordsAreNot() {
        val q = ExtraPracticeData.exercises.first { it.lessonId == "food" }.questions[3]
        assertTrue(evaluateExercise(q, "Could I have the bill please").accepted)
        assertTrue(evaluateExercise(q, "the bill please").accepted)
        assertTrue(evaluateExercise(q, "Can I get the check please").accepted)
        assertFalse(evaluateExercise(q, "Could I not have the bill please").accepted)
        assertFalse(evaluateExercise(q, "Could I have the please").accepted)
        assertFalse(evaluateExercise(q, "").accepted)
        assertFalse(evaluateExercise(q, null).accepted)
    }
    @Test fun everyAuthoredAlternativePassesIncludingCanonicalNumberAndContractionForms() {
        ExtraPracticeData.exercises.flatMap { it.questions }.forEach { q ->
            q.answers.forEach { assertTrue("${q.id}: $it", evaluateExercise(q, it).accepted) }
        }
        val q = ExerciseQuestion("age", "Tôi 25 tuổi.", listOf("I am twenty-five years old."))
        assertTrue(evaluateExercise(q, "I'm 25 years old").accepted)
        assertFalse(evaluateExercise(q, "I'm 26 years old").accepted)
    }
    @Test fun stableIdsResumeAtTheFirstNewQuestionAndCannotBorrowTopicUnlocks() {
        val exercise = ExtraPracticeData.exercises.first()
        val done = exercise.questions.take(3).map { it.id }.toSet()
        assertEquals(3, nextUnfinishedQuestion(exercise, done))
        val inserted = exercise.copy(questions = exercise.questions.take(2) + ExerciseQuestion("new-id", "Mới", listOf("New")) + exercise.questions.drop(2))
        assertEquals(2, nextUnfinishedQuestion(inserted, done))
        assertEquals(0, nextUnfinishedQuestion(exercise, emptySet()))
        listOf("A1", "A2", "B1").forEach { level ->
            val passages = ExtraPracticeData.passages.filter { it.level == level }
            assertEquals(if (level == "A1") 5 else 10, passages.size)
            assertEquals(if (level == "A2") 6 else 3, passages.count { extraTopicOpen(it.lessonId) { LessonProgress() } })
        }
        assertFalse(extraTopicOpen("unknown") { LessonProgress(rounds = 1) })
    }
    @Test fun contentIsBilingualUniqueAndProgressivelyLongerWithFullIpaCoverage() {
        val data = JSONObject(File("src/main/assets/ipa.json").readText())
        val expanded = JSONObject(File("src/main/assets/expanded-ipa.json").readText())
        val passages = ExtraPracticeData.passages
        assertEquals(25, passages.size)
        assertEquals(230, ExtraPracticeData.exercises.sumOf { it.questions.size })
        val ids = passages.map { it.id } + passages.flatMap { it.sentences.map { it.id } } +
            ExtraPracticeData.exercises.flatMap { it.questions.map { it.id } }
        assertEquals(ids.size, ids.distinct().size)
        val averages = listOf("A1", "A2", "B1").map { level ->
            passages.filter { it.level == level }.map { it.english.split(' ').size }.average()
        }
        assertTrue("$averages", averages[0] < averages[1] && averages[1] < averages[2])
        val english = Lessons.all.flatMap { it.phrases.map { it.english } } +
            passages.flatMap { it.sentences.map { it.english } + it.vocabulary.map { it.english } } +
            ExtraPracticeData.exercises.flatMap { it.questions.flatMap { it.answers } }
        listOf("US", "GB").forEach { accent ->
            english.forEach { text ->
                assertTrue("Missing IPA $accent: $text", data.getJSONObject(accent).optString(IpaText.key(text)).isNotBlank() ||
                    expanded.getJSONObject(accent).optString(IpaText.key(text)).isNotBlank())
            }
        }
        assertTrue(passages.all { it.sentences.all { s -> s.vietnamese.isNotBlank() } && it.vocabulary.size == 4 })
    }
}
