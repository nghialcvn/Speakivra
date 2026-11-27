package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.io.File

class HotelCurriculumTest {
    @Test fun standaloneRolesHaveTwentyCompleteTopicsAndStableIndependentIds() {
        assertEquals(105, Lessons.all.size)
        assertEquals(145, Lessons.catalogue.size)
        assertEquals(1000, HotelCurriculum.all.sumOf { it.phrases.size })
        assertEquals(240, HotelCurriculum.all.sumOf { it.conversation.size })
        listOf("FO", "HK").forEach { role ->
            val topics = HotelCurriculum.topics.filter { it.role == role }
            assertEquals(20, topics.size)
            assertTrue(topics.count { it.incident } >= 6)
            topics.forEach { topic ->
                assertEquals(role, Lessons.levelOf(topic.lesson))
                assertTrue(topicIsOpen(topic.lesson, Lessons.catalogue) { LessonProgress() })
                assertEquals(25, topic.lesson.phrases.size)
                assertEquals(6, topic.lesson.conversation.size)
                assertTrue(topic.lesson.conversation.all { it.translation.isNotBlank() && it.replyTranslation.isNotBlank() })
            }
        }
        val ids = Lessons.catalogue.map { it.id } + HotelCurriculum.all.flatMap { it.phrases.map { p -> p.id } + it.conversation.map { t -> t.id } }
        assertEquals(ids.size, ids.distinct().size)
        assertEquals(listOf("Chào đón khách", "Đặt phòng", "Nhận phòng", "Giới thiệu dịch vụ", "Hỗ trợ yêu cầu", "Thanh toán & trả phòng"), HotelCurriculum.lessons("FO").take(6).map { it.vietnamese })
        assertEquals(listOf("Xin phép vào phòng", "Hẹn giờ dọn phòng", "Bổ sung đồ dùng", "Giặt là", "Báo hỏng thiết bị", "Xử lý phàn nàn"), HotelCurriculum.lessons("HK").take(6).map { it.vietnamese })
    }
    @Test fun hotelExerciseAnswersAndIpaCoverAllStaffAndGuestLines() {
        val data = JSONObject(File("src/main/assets/hotel-ipa.json").readText())
        assertEquals(40, HotelCurriculum.exercises.size)
        HotelCurriculum.exercises.forEach { exercise ->
            assertEquals(25, exercise.questions.size)
            exercise.questions.forEach { q -> assertTrue(q.id, evaluateExercise(q, q.answer).accepted) }
        }
        HotelCurriculum.all.forEach { lesson ->
            val phrases = lesson.phrases.map { it.english }
            assertTrue(lesson.conversation.all { it.reply in phrases })
            (phrases + lesson.conversation.map { it.prompt }).forEach { text ->
                assertTrue(text, data.getJSONObject("US").optString(IpaText.key(text)).isNotBlank())
            }
        }
    }
    @Test fun hotelTranslationCompletionResumesRemainingDialogueBeforeReview() {
        val lesson = HotelCurriculum.all.first()
        val saved = mapOf("lesson.${lesson.id}.learned_phrases" to lesson.phrases.joinToString("|") { it.id },
            "lesson.${lesson.id}.learned_turns" to lesson.conversation.take(2).joinToString("|") { it.id })
        val progress = restoreLessonProgress(lesson, { _, d -> d }, { k, d -> saved[k] ?: d })
        assertEquals("chat", progress.phase)
        assertEquals(2, progress.turn)
        assertEquals(0, progress.rounds)
    }
    @Test fun addingHotelContentDoesNotUnlockCefrOrBorrowOtherRoleProgress() {
        val first = HotelCurriculum.lessons("FO").first()
        val other = HotelCurriculum.lessons("HK").first()
        val ints = mutableMapOf<String, Int>()
        val strings = mutableMapOf<String, String>()
        strings["lesson.${first.id}.learned_phrases"] = first.phrases.take(2).joinToString("|") { it.id }
        strings["lesson.${first.id}.learned_turns"] = first.conversation.take(1).joinToString("|") { it.id }
        fun load(lesson: Lesson) = restoreLessonProgress(lesson, { k, d -> ints[k] ?: d }, { k, d -> strings[k] ?: d })
        assertEquals(2, load(first).phrase)
        assertEquals(0, load(other).phrase)
        assertEquals(0, load(other).learnedTurns!!.size)
        assertFalse(topicIsOpen(Lessons.a1[3], Lessons.catalogue, ::load))
        assertTrue(topicIsOpen(HotelCurriculum.lessons("HK").last(), Lessons.catalogue, ::load))
        val extended = first.copy(phrases = first.phrases + first.phrases.first().copy(id = "future-hotel-phrase"))
        assertEquals(load(first).learnedPhrases, load(extended).learnedPhrases)
        assertEquals(2, load(extended).phrase)
    }
}
