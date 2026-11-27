package com.nghialcvn.speakivra

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ChineseCourseTest {
    @Test fun mandarinCourseIsCompleteAndDoesNotEnterEnglishCatalogue() {
        val topics = ChineseCurriculum.topics
        assertEquals(10, topics.count { it.level == "A1" })
        assertEquals(15, topics.count { it.level == "A2" })
        assertEquals(4450, topics.sumOf { it.phrases.size })
        assertEquals(1660, topics.sumOf { it.turns.size })
        assertEquals(620, topics.sumOf { it.reading.size })
        assertEquals(1630, topics.sumOf { it.exercises.size })
        assertEquals(435, topics.sumOf { it.comprehension.size })
        assertTrue(topics.flatMap { it.comprehension }.all { it.options.size == 3 && it.options.distinct().size == 3 && it.answer in 0..2 })
        assertTrue(Lessons.catalogue.none { it.id.startsWith("zh_") })
        assertEquals(10, Lessons.a1.size)
        assertEquals(15, Lessons.a2.size)
        val ids = topics.flatMap { t -> t.phrases + t.turns.map { it.prompt } + t.reading + t.exercises }.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        topics.forEach { topic ->
            assertTrue(topic.id.startsWith(if (topic.level in listOf("FO", "HK")) "zh_hotel_${topic.level.lowercase()}_" else "zh_${topic.level.lowercase()}_"))
            assertTrue(topic.title.isNotBlank() && topic.tip.isNotBlank())
            assertTrue(topic.turns.all { it.answer != null || it.answerIndex in topic.phrases.indices })
            (topic.phrases + topic.turns.map { it.prompt }).forEach { line ->
                assertTrue(line.id, Regex("\\p{IsHan}").containsMatchIn(line.hanzi))
                assertTrue(line.id, line.pinyin.isNotBlank() && line.vietnamese.isNotBlank())
                assertFalse(line.id, Regex("\\p{IsHan}").containsMatchIn(line.pinyin))
                assertTrue(line.id, matchChinese(line.hanzi, line.hanzi).exact)
            }
        }
    }
    @Test fun characterMatchingIgnoresSpacingButPreservesNegationsAndInsertions() {
        assertTrue(matchChinese("你好！", "你 好").exact)
        assertTrue(matchChinese("我七点起床。", "我7点起床").exact)
        assertTrue(matchChinese("现在是上午八点半。", "现在是上午8:30").exact)
        assertTrue(matchChinese("我七点起床。", "我7:00起床").exact)
        assertFalse(matchChinese("我七点起床。", "我7:30起床").exact)
        assertTrue(matchChinese("我有二十本书。", "我有20本书").exact)
        assertTrue(matchChinese("我们下午两点见。", "我们下午2点见").exact)
        assertTrue(matchChinese("我有一百零一本书。", "我有101本书").exact)
        assertTrue(matchChinese("我叫Nghĩa。", "我叫Nghia").exact)
        assertFalse(matchChinese("我喜欢喝茶。", "我不喜欢喝茶").canContinue)
        assertFalse(matchChinese("我不吃肉。", "我吃肉").canContinue)
        assertFalse(matchChinese("我没有姐姐。", "我有姐姐").canContinue)
        assertFalse(matchChinese("你好", null).canContinue)
        assertFalse(matchChinese("你好", "！？").canContinue)
        assertFalse(matchChinese("你好", "你好你好").exact)
        assertEquals(50, matchChinese("你好", "你好你好").matchPercent)
        assertFalse(matchChinese("你好", "你好привет").exact)
        assertFalse(matchChinese("我叫Linh", "我叫Nghia").exact)
        val boundary = matchChinese("我每天去上班", "我每")
        assertFalse(boundary.canContinue)
        assertFalse(matchChinese("一二三四五", "一二").canContinue)
        assertTrue(matchChinese("一二三四五", "一二三").canContinue)
    }
    @Test fun learnerNamesArePersonalizedWithoutInventingTheirPinyin() {
        val original = ChineseCurriculum.topics.first()
        val a = original.personalize("Linh"); val b = original.personalize("Nghĩa")
        assertEquals("我叫Linh。", a.phrases.first().hanzi)
        assertEquals("Wǒ jiào Nghĩa.", b.phrases.first().pinyin)
        assertEquals(original.phrases.map { it.id }, a.phrases.map { it.id })
        assertEquals("我叫小明。", original.phrases.first().hanzi)
        assertFalse(a.phrases.first().hanzi.contains("Nghĩa"))
    }
    @Test fun chineseProgressRoundTripsWithEnglishAndRejectsUnknownContent() {
        val t = ChineseCurriculum.topics.first(); val e = Lessons.a1.first()
        val phrase = "lesson.${t.id}.learned_phrases"
        val english = "lesson.${e.id}.learned_phrases"
        val values = mapOf<String, Any>(phrase to t.phrases.first().id,
            english to e.phrases.first().id,
            "lesson.${t.id}.learned_turns" to t.turns.first().prompt.id,
            "achievement.${t.id}.${t.phrases.first().id}" to 75,
            "achievement.${t.id}.invented" to 100,
            "extra.completed_ids" to setOf(t.reading.first().id, t.exercises.first().id, t.comprehension.first().id, "invented"),
            "zh_voice_id" to "device-voice", "study_language" to "zh")
        val learner = Learner(ORIGINAL_LEARNER, "Linh", 0)
        val progress = profileSyncPayload(learner, GuestIdentity("profile-id", ""), values).getJSONObject("progress")
        assertEquals(t.phrases.first().id, progress.getString(phrase))
        assertEquals(e.phrases.first().id, progress.getString(english))
        assertFalse(progress.has("zh_voice_id")); assertFalse(progress.has("study_language"))
        assertFalse(progress.has("achievement.${t.id}.invented"))
        assertEquals(3, progress.getJSONArray("extra.completed_ids").length())
        assertEquals(75, progress.getInt("achievement.${t.id}.${t.phrases.first().id}"))
        val other = JSONObject().put(phrase, t.phrases[1].id)
        val merged = sanitizedCloudProgress("profile-id", mergeCloudProgress(progress, other))
        assertEquals(setOf(t.phrases[0].id, t.phrases[1].id), merged.getString(phrase).split('|').toSet())
        assertEquals(e.phrases.first().id, merged.getString(english))
    }
}
