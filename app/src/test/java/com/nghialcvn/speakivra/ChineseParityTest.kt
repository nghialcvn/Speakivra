package com.nghialcvn.speakivra

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ChineseParityTest {
    @Test fun everyEnglishTopicHasTheSameChinesePracticeCapacityAndAllExtraPacks() {
        val zh = ChineseCurriculum.topics.associateBy { it.id }
        assertEquals(Lessons.catalogue.size, zh.size)
        Lessons.catalogue.forEach { en ->
            val level = Lessons.levelOf(en)
            val id = if (level == "A1") "zh_a1_${en.id}" else "zh_${en.id}"
            val topic = requireNotNull(zh[id]) { id }
            assertEquals(id, level, topic.level)
            assertEquals(id, en.phrases.size, topic.phrases.size)
            assertEquals(id, en.conversation.size, topic.turns.size)
            val passages = ExtraPracticeData.passages.filter { it.lessonId == en.id }
            val exercises = (ExtraPracticeData.exercises + HotelCurriculum.exercises).filter { it.lessonId == en.id }
            assertEquals(id, passages.size, topic.readingPacks.size)
            assertEquals(id, passages.sumOf { it.sentences.size }, topic.readingPacks.sumOf { it.lines.size })
            assertEquals(id, exercises.size, topic.exercisePacks.size)
            assertEquals(id, exercises.sumOf { it.questions.size }, topic.exercisePacks.sumOf { it.lines.size })
            (topic.phrases + topic.turns.flatMap { listOf(it.prompt, it.reply(topic.phrases)) } +
                topic.reading + topic.exercises + topic.readingPacks.flatMap { it.vocabulary }).forEach { line ->
                assertFalse(line.id, Regex("\\b(the|should|because|would)\\b", RegexOption.IGNORE_CASE).containsMatchIn(line.hanzi))
                assertTrue(line.id, line.vietnamese.isNotBlank() && line.pinyin.isNotBlank())
            }
        }
        ChineseStarterCurriculum.topics.forEach { old ->
            val expanded = requireNotNull(zh[old.id])
            assertEquals(old.phrases, expanded.phrases.take(old.phrases.size))
            assertEquals(old.turns, expanded.turns.take(old.turns.size))
            assertTrue(expanded.reading.map { it.id }.containsAll(old.reading.map { it.id }))
            assertTrue(expanded.exercises.map { it.id }.containsAll(old.exercises.map { it.id }))
        }
    }
    @Test fun longParagraphMatchingIncludesTheEndingAndRejectsOversizedAttempts() {
        val text = "我每天学习中文。".repeat(90)
        assertTrue(chineseTokens(text).size > 512)
        assertTrue(matchChinese(text, text).exact)
        assertFalse(matchChinese(text, text + "不").canContinue)
        val tooLong = "我".repeat(4097)
        assertFalse(matchChinese(tooLong, tooLong).exact)
        assertFalse(matchChinese(tooLong, tooLong).canContinue)
    }
    @Test fun cloudKeepsBothLanguageResumesAndEarnedRewardsButNoVoiceSettings() {
        val topic = ChineseCurriculum.topics.first()
        val reward = "lesson.${topic.id}.diamonds_earned"
        val values = mapOf<String, Any>("active_lesson" to "family", "zh.active_lesson" to topic.id,
            reward to 3, "lesson.greetings.diamonds_earned" to 3, "zh_voice_id" to "private-device-id")
        val progress = profileSyncPayload(Learner(ORIGINAL_LEARNER, "Linh", 0), GuestIdentity("profile-id", ""), values).getJSONObject("progress")
        assertEquals("family", progress.getString("active_lesson"))
        assertEquals(topic.id, progress.getString("zh.active_lesson"))
        assertEquals(3, progress.getInt(reward))
        assertFalse(progress.has("lesson.greetings.diamonds_earned"))
        assertFalse(progress.has("zh_voice_id"))
        val merged = mergeCloudProgress(progress, JSONObject().put(reward, 1))
        assertEquals(3, merged.getInt(reward))
        val wrong = sanitizedCloudProgress("profile-id", JSONObject().put("active_lesson", topic.id).put("zh.active_lesson", "family"))
        assertFalse(wrong.has("active_lesson")); assertFalse(wrong.has("zh.active_lesson"))
    }
}
