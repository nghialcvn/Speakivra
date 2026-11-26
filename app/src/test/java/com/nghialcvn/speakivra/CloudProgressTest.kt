package com.nghialcvn.speakivra

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class CloudProgressTest {
    @Test fun mergingDoesNotLoseAchievementsOrStableLearningIdsAndIsIdempotent() {
        val lesson = Lessons.a1.first(); val a = lesson.phrases[0].id; val b = lesson.phrases[1].id
        val prefix = "lesson.${lesson.id}."
        val local = JSONObject().put(prefix + "learned_phrases", a).put(prefix + "rounds", 2).put("achievement.${lesson.id}.$a", 95)
        val remote = JSONObject().put(prefix + "learned_phrases", b).put(prefix + "rounds", 1).put("achievement.${lesson.id}.$a", 60)
        val first = mergeCloudProgress(local, remote)
        val again = mergeCloudProgress(first, remote)
        assertEquals(first.toString(), again.toString())
        assertEquals(setOf(a,b), first.getString(prefix + "learned_phrases").split('|').toSet())
        assertEquals(2, first.getInt(prefix + "rounds"))
        assertEquals(95, first.getInt("achievement.${lesson.id}.$a"))
        assertEquals(first.toString(), mergeCloudProgress(remote,local).toString())
    }
    @Test fun importDropsDeviceSettingsForeignIdsAndSecrets() {
        val id = "a".repeat(64)
        val raw = JSONObject().put("password", "secret").put("automatic_mode", true)
            .put("recording", "/private/audio").put("lesson.unknown.phase", "summary")
            .put("active_lesson", Lessons.a1.first().id).put("achievement.greetings.unknown", 100)
        val cleaned = sanitizedCloudProgress(id,raw)
        assertEquals(1, cleaned.length()); assertTrue(cleaned.has("active_lesson"))
        assertFalse(cleaned.toString().contains("secret")); assertFalse(cleaned.toString().contains("/private/audio"))
    }
    @Test fun completeCurrentCatalogueFitsOnePrivateSnapshotWithoutDroppingAchievements() {
        val values = mutableMapOf<String,Any>()
        Lessons.catalogue.forEach { lesson ->
            values["lesson.${lesson.id}.phase"] = "summary"
            values["lesson.${lesson.id}.rounds"] = 1
            values["lesson.${lesson.id}.learned_phrases"] = lesson.phrases.joinToString("|") { it.id }
            values["lesson.${lesson.id}.learned_turns"] = lesson.conversation.joinToString("|") { it.id }
            lesson.phrases.forEach { phrase -> values["achievement.${lesson.id}.${phrase.id}"] = 95 }
        }
        val exported = profileSyncPayload(Learner(ORIGINAL_LEARNER,"Nghĩa",0),GuestIdentity("a".repeat(64),""),values).getJSONObject("progress")
        assertTrue(exported.toString().length < 524288)
        assertEquals(Lessons.catalogue.sumOf { it.phrases.size }, exported.keys().asSequence().count { it.startsWith("achievement.") })
    }

}
