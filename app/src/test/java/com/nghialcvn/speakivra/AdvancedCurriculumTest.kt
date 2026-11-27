package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import java.util.Base64

class AdvancedCurriculumTest {
    private fun level(level: String) = Lessons.catalogue.filter { Lessons.levelOf(it) == level }
    @Test fun exactCountsBilingualContentAndUniqueStableIds() {
        listOf("B2", "C1", "C2").forEach { name ->
            val topics = level(name)
            assertEquals(20, topics.size)
            assertEquals(700, topics.sumOf { it.phrases.size })
            assertEquals(300, topics.sumOf { it.conversation.size })
            assertTrue(topics.all { it.phrases.size == 35 && it.conversation.size == 15 })
            assertEquals(700, topics.flatMap { it.phrases.map { p -> p.english } }.distinct().size)
            val passages = ExtraPracticeData.passages.filter { it.level == name }
            val exercises = ExtraPracticeData.exercises.filter { it.level == name }
            assertEquals(10, passages.size); assertEquals(100, passages.sumOf { it.sentences.size })
            assertEquals(20, exercises.size); assertEquals(100, exercises.sumOf { it.questions.size })
            assertTrue(passages.all { it.vocabulary.size == 4 && it.sentences.all { s -> s.vietnamese.isNotBlank() } })
            assertTrue(exercises.all { it.questions.size == 5 && it.questions.all { q -> q.vietnamese.isNotBlank() } })
        }
        val ids = Lessons.catalogue.flatMap { listOf(it.id) + it.phrases.map { p -> p.id } + it.conversation.map { t -> t.id } } +
            ExtraPracticeData.passages.flatMap { listOf(it.id) + it.sentences.map { s -> s.id } } +
            ExtraPracticeData.exercises.flatMap { listOf(it.id) + it.questions.map { q -> q.id } }
        assertEquals(ids.size, ids.distinct().size)
        // Reproducible input for the offline phonemizer, exported from the real Kotlin data.
        val file = File("build/reports/tests/advanced-content.tsv"); file.parentFile.mkdirs()
        fun row(kind: String, level: String, id: String, en: String, vi: String) =
            listOf(kind, level, id, Base64.getEncoder().encodeToString(en.toByteArray()),
                Base64.getEncoder().encodeToString(vi.toByteArray())).joinToString("\t")
        file.writeText(buildList {
            AdvancedCurriculum.lessons.forEach { l ->
                val name = Lessons.levelOf(l)
                l.phrases.forEach { add(row("phrase", name, it.id, it.english, it.vietnamese)) }
                l.conversation.forEach { add(row("prompt", name, it.id + "_prompt", it.prompt, it.translation)); add(row("reply", name, it.id, it.reply, it.replyTranslation)) }
            }
            AdvancedCurriculum.passages.forEach { p ->
                p.sentences.forEach { add(row("reading", p.level, it.id, it.english, it.vietnamese)) }
                add(row("whole", p.level, p.id, p.english, p.sentences.joinToString(" ") { it.vietnamese }))
                p.vocabulary.forEachIndexed { i, v -> add(row("vocabulary", p.level, p.id + "_v" + i, v.english, v.vietnamese)) }
            }
            AdvancedCurriculum.exercises.forEach { e -> e.questions.forEach { add(row("exercise", e.level, it.id, it.answer, it.vietnamese)) } }
        }.joinToString("\n") + "\n")
    }
    @Test fun responseLengthsMatchApprovedProgressionWithoutInflatingC2() {
        fun mean(name: String) = level(name).flatMap { it.phrases }.map { it.english.split(Regex("\\s+")).size }.average()
        val b1 = mean("B1"); val b2 = mean("B2"); val c1 = mean("C1"); val c2 = mean("C2")
        assertTrue("B2/B1=${b2 / b1}", b2 / b1 in 1.9..2.1)
        assertTrue("C1/B2=${c1 / b2}", c1 / b2 in 2.9..3.1)
        assertTrue(c2 < c1)
        assertTrue(level("C2").all { lesson -> lesson.phrases.any { "Thành ngữ" in it.tip } })
        assertTrue(level("C2").flatMap { it.phrases }.any { "implication" in it.english })
    }
    @Test fun everyAdvancedTopicAndExtraIsLockedUntilTheEntirePredecessorIsComplete() {
        val done = mutableSetOf<String>()
        fun load(l: Lesson) = LessonProgress(rounds = if (l.id in done) 1 else 0)
        listOf("B2", "C1", "C2").forEach { name ->
            assertFalse(levelIsOpen(name, Lessons.catalogue, ::load))
            assertTrue(level(name).none { topicIsOpen(it, Lessons.catalogue, ::load) })
            assertTrue(ExtraPracticeData.passages.filter { it.level == name }.none { extraTopicOpen(it.lessonId, ::load) })
        }
        listOf("B1" to "B2", "B2" to "C1", "C1" to "C2").forEach { (previous, next) ->
            done += level(previous).dropLast(1).map { it.id }
            assertFalse(levelIsOpen(next, Lessons.catalogue, ::load))
            done += level(previous).last().id
            assertTrue(levelIsOpen(next, Lessons.catalogue, ::load))
            assertEquals(3, level(next).count { topicIsOpen(it, Lessons.catalogue, ::load) })
            assertFalse(topicIsOpen(level(next)[3], Lessons.catalogue, ::load))
        }
        // Hotel study remains independent of the general-language prerequisites.
        assertTrue(HotelCurriculum.all.all { topicIsOpen(it, Lessons.catalogue) { LessonProgress() } })
        assertFalse(levelIsOpen("C1", level("B2") + level("C1")) { LessonProgress(rounds = 1) })
    }
    @Test fun replayDoesNotEraseUnlockAndForgedLaterRoundsCannotSkipEarlierLevels() {
        val earlier = (level("B1") + level("B2") + level("C1")).map { it.id }.toSet()
        assertTrue(levelIsOpen("C2", Lessons.catalogue) { LessonProgress(rounds = if (it.id in earlier) 1 else 0).restart() })
        assertFalse(levelIsOpen("C2", Lessons.catalogue) { LessonProgress(rounds = if (Lessons.levelOf(it) != "B1") 1 else 0) })
    }
    @Test fun reminderBoundaryAndLongUtteranceBudgetAreCorrect() {
        val evening = LocalDateTime.of(2026, 11, 27, 20, 0)
        assertEquals(evening, nextStudyReminder(evening.minusSeconds(1)))
        assertEquals(evening.plusDays(1), nextStudyReminder(evening))
        assertEquals(evening.plusDays(1), nextStudyReminder(evening.plusHours(3)))
        assertEquals(60000, speechAttemptLimitMillis(false, 25))
        assertTrue(speechAttemptLimitMillis(false, 96) > 120000)
        assertTrue(speechAttemptLimitMillis(true, 600) > 600000)
        assertEquals(900000, speechAttemptLimitMillis(true, Int.MAX_VALUE))
    }
}
