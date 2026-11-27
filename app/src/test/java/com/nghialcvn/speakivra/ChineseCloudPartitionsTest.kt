package com.nghialcvn.speakivra

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ChineseCloudPartitionsTest {
    private val profile = "a".repeat(64)
    private fun exported(values: Map<String, Any>) = profileSyncPayload(Learner(ORIGINAL_LEARNER, "Lan", 2),
        GuestIdentity(profile, ""), values).getJSONObject("progress")
    private fun completedCourse(): JSONObject {
        val values = mutableMapOf<String, Any>()
        (Lessons.catalogue + ChineseCurriculum.lessons).forEach { lesson ->
            values["lesson.${lesson.id}.learned_phrases"] = lesson.phrases.map { it.id }.sorted().joinToString("|")
            values["lesson.${lesson.id}.learned_turns"] = lesson.conversation.map { it.id }.sorted().joinToString("|")
            values["lesson.${lesson.id}.rounds"] = 1
            values["lesson.${lesson.id}.phrase"] = lesson.phrases.size
            values["lesson.${lesson.id}.turn"] = lesson.conversation.size
            lesson.phrases.forEach { values["achievement.${lesson.id}.${it.id}"] = 100 }
        }
        values["extra.completed_ids"] = (ChineseCurriculum.extraIds +
            ExtraPracticeData.passages.flatMap { it.sentences.map { line -> line.id } + it.id } +
            (ExtraPracticeData.exercises + HotelCurriculum.exercises).flatMap { it.questions.map { q -> q.id } }).toSet()
        values["active_lesson"] = Lessons.catalogue.last().id
        values["zh.active_lesson"] = ChineseCurriculum.topics.last().id
        values[CHECK_IN_DATES] = setOf("2026-11-27", "2026-11-28")
        return exported(values)
    }
    private fun assertSame(a: JSONObject, b: JSONObject) = assertEquals(cloudProgressValues(a), cloudProgressValues(b))

    @Test fun bothCompletedCoursesRoundTripWithoutAnyOversizedDocumentOrLostIds() {
        val original = completedCourse()
        assertTrue(original.toString().toByteArray().size > 524288)
        val root = ChineseCloudPartitions.rootProgress(original)
        assertTrue(root.toString().toByteArray().size <= 524288)
        var restored = root
        ChineseCloudPartitions.levels.forEach { level ->
            val part = ChineseCloudPartitions.levelProgress(original, level)
            assertTrue(ChineseCloudPartitions.checkedText(part).toByteArray().size <= 131072)
            restored = mergeCloudProgress(restored, part)
        }
        assertSame(original, sanitizedCloudProgress(profile, restored))
        val old = ChineseStarterCurriculum.topics.first()
        assertEquals(old.phrases.size, root.getInt("lesson.${old.id}.phrase"))
        assertEquals(old.turns.size, root.getInt("lesson.${old.id}.turn"))
        assertEquals(old.phrases.map { it.id }.toSet(), root.getString("lesson.${old.id}.learned_phrases").split('|').toSet())
    }
    @Test fun lateLegacyClientAndAnotherDeviceUnionProgressAndPreserveBestRewards() {
        val topic = ChineseCurriculum.topics.first()
        val old = ChineseStarterCurriculum.topics.first()
        val newlyAdded = topic.phrases.last()
        val newer = exported(mapOf("lesson.${topic.id}.learned_phrases" to newlyAdded.id,
            "achievement.${topic.id}.${newlyAdded.id}" to 90, "lesson.${topic.id}.rounds" to 1,
            "zh.active_lesson" to topic.id))
        // An old installation later writes its original eight sentences back to the root.
        val oldValues = mutableMapOf<String, Any>("lesson.${topic.id}.learned_phrases" to old.phrases.map { it.id }.sorted().joinToString("|"))
        old.phrases.forEach { oldValues["achievement.${topic.id}.${it.id}"] = 100 }
        val legacy = exported(oldValues)
        val combined = sanitizedCloudProgress(profile, mergeCloudProgress(legacy, ChineseCloudPartitions.levelProgress(newer, "A1")))
        val ids = combined.getString("lesson.${topic.id}.learned_phrases").split('|').toSet()
        assertTrue(ids.containsAll(old.phrases.map { it.id } + newlyAdded.id))
        assertEquals(3, combined.getInt("lesson.${topic.id}.diamonds_earned"))
        assertEquals(1, combined.getInt("lesson.${topic.id}.rounds"))
        assertEquals(90, combined.getInt("achievement.${topic.id}.${newlyAdded.id}"))
        assertSame(combined, sanitizedCloudProgress(profile, mergeCloudProgress(combined, combined)))
    }
    @Test fun aLevelCannotImportForeignCourseSettingsOrStealTheSharedResume() {
        val topic = ChineseCurriculum.topics.first { it.level == "C2" }
        val raw = JSONObject().put("lesson.${topic.id}.rounds", 1).put("lesson.greetings.rounds", 99)
            .put("zh.active_lesson", topic.id).put("active_lesson", "family").put("tts_voice", "device-secret")
            .put("extra.completed_ids", org.json.JSONArray(ChineseCurriculum.extraIds.toList()))
        val part = ChineseCloudPartitions.levelProgress(sanitizedCloudProgress(profile, raw), "C2")
        assertFalse(part.has("lesson.greetings.rounds")); assertFalse(part.has("active_lesson"))
        assertFalse(part.has("zh.active_lesson")); assertFalse(part.has("tts_voice"))
        assertEquals(1, part.getInt("lesson.${topic.id}.rounds"))
        val root = ChineseCloudPartitions.rootProgress(sanitizedCloudProgress(profile, raw))
        assertEquals(topic.id, root.getString("zh.active_lesson"))
        assertFalse(root.has("lesson.${topic.id}.rounds"))
    }
    @Test fun sizeLimitCountsUtf8BytesRatherThanOnlyCharacters() {
        val large = JSONObject().put("x", "中".repeat(50000))
        assertTrue(large.toString().length < ChineseCloudPartitions.MAX_BYTES)
        assertTrue(runCatching { ChineseCloudPartitions.checkedText(large) }.isFailure)
    }
}
