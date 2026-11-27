package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer

class Expansion117Test {
    @Test fun requestedTotalsAreExactAndIdsRemainUnique() {
        listOf("A2", "B1").forEach { level ->
            val reading = ExtraPracticeData.passages.filter { it.level == level }
            assertEquals(10, reading.size)
            assertEquals(100, reading.sumOf { it.sentences.size })
            assertTrue(reading.all { it.sentences.size == 10 })
            val exercises = ExtraPracticeData.exercises.filter { it.level == level }
            assertEquals(20, exercises.size)
            assertEquals(100, exercises.sumOf { it.questions.size })
            assertEquals(100, exercises.flatMap { it.questions.map { q -> q.id } }.distinct().size)
        }
        assertEquals(5, ExtraPracticeData.passages.count { it.level == "A1" })
        assertEquals(30, ExtraPracticeData.exercises.filter { it.level == "A1" }.sumOf { it.questions.size })
        listOf("FO", "HK").forEach { role ->
            val lessons = HotelCurriculum.lessons(role)
            assertEquals(20, lessons.size)
            assertEquals(500, lessons.sumOf { it.phrases.size })
            assertTrue(lessons.all { it.phrases.size == 25 && it.phrases.map { p -> p.english }.distinct().size == 25 })
            val exercises = HotelCurriculum.exercises.filter { it.lessonId in lessons.map { l -> l.id } }
            assertEquals(20, exercises.size)
            assertEquals(500, exercises.sumOf { it.questions.size })
        }
        val ids = ExtraPracticeData.passages.map { it.id } + ExtraPracticeData.exercises.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }
    @Test fun savedCompletedHotelAndPassageResumeAtAppendedContent() {
        val lesson = HotelCurriculum.lessons("FO").first()
        val old = lesson.copy(phrases = lesson.phrases.take(8))
        assertEquals((1..8).map { "hotel_fo_welcome_p$it" }, old.phrases.map { it.id })
        val state = restoreLessonProgress(lesson, { _, default -> default }, { key, default -> when {
            key.endsWith("learned_phrases") -> old.phrases.joinToString("|") { it.id }
            key.endsWith("learned_turns") -> old.conversation.joinToString("|") { it.id }
            else -> default
        } })
        assertEquals("repeat", state.phase)
        assertEquals(8, state.phrase)
        val reading = ExtraPracticeData.passages.first { it.id == "passage_a2_experiences" }
        assertEquals("passage_a2_experiences_s5", reading.sentences[4].id)
        val oldExercise = ExtraPracticeData.exercises.first { it.id == "exercise_a2_plans" }
        assertEquals(6, oldExercise.questions.size)
    }
    @Test fun namesKeepAccentsAcceptEquivalentIntroductionsButRejectOtherNamesAndNegation() {
        listOf("Nghĩa", "Nguyễn Đặng Ánh", "Đỗ Mỹ Linh").forEach { name ->
            val reference = "My name is $name."
            val plain = Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").replace('Đ','D').replace('đ','d')
            listOf(reference, "My name is $plain", "I'm $plain", "I am $name", Normalizer.normalize(reference, Normalizer.Form.NFD)).forEach {
                val result = matchWords(reference, it)
                assertTrue(it, result.canContinue)
                assertTrue(it, result.matched.all { m -> m })
            }
            listOf("My name is", "My name is Alex", "I'm not $plain").forEach { assertFalse(it, matchWords(reference, it).canContinue) }
            assertEquals(reference, personalizeLesson(Lessons.a1.first(), name).phrases[2].english)
        }
        assertFalse(matchWords("My name is Nguyễn Đặng Ánh.", "My name is Nguyen Anh").canContinue)
    }
    @Test fun installedNotesCannotSilentlyRepeatAnOlderVersion() {
        val current = InstalledReleaseNotes.forVersion(BuildConfig.VERSION_NAME)
        assertTrue(current.isNotEmpty())
        assertTrue(InstalledReleaseNotes.forVersion("1.1.7").any { "500 câu" in it.second })
        assertTrue(InstalledReleaseNotes.forVersion("1.2.0").any { "Nhắc liên kết" in it.second })
        assertNotEquals(InstalledReleaseNotes.forVersion("1.1.7"), current)
        assertFalse(current.any { "Hồ sơ mới" in it.second || "450 câu" in it.second })
        assertNotEquals(InstalledReleaseNotes.forVersion("1.1.6"), current)
        assertTrue(InstalledReleaseNotes.forVersion("9.9.9").isEmpty())
    }
}
