package com.nghialcvn.speakivra

import org.json.JSONArray
import org.json.JSONObject

/** Portable learning data only. Auth tokens, audio, device paths and TTS voice IDs cannot enter this payload. */
internal fun profileSyncPayload(learner: Learner, identity: GuestIdentity, values: Map<String, *>): JSONObject {
    val allowedLessons = Lessons.catalogue.associateBy { it.id }
    val progress = JSONObject()
    values.forEach { (key, value) ->
        if (key == "extra.completed_ids" && value is Set<*>) {
            val known = (ExtraPracticeData.passages.flatMap { p -> p.sentences.map { it.id } + p.id } +
                ExtraPracticeData.exercises.flatMap { it.questions.map { q -> q.id } } +
                HotelCurriculum.exercises.flatMap { it.questions.map { q -> q.id } }).toSet()
            progress.put(key, JSONArray(value.filterIsInstance<String>().filter { it in known }.sorted()))
        } else if (key.startsWith("lesson.")) {
            val parts = key.split('.', limit = 3)
            val lesson = allowedLessons[parts.getOrNull(1)]
            if (lesson != null) when (parts.getOrNull(2)) {
                "learned_phrases", "learned_turns" -> if (value is String) {
                    val known = if (parts[2] == "learned_phrases") lesson.phrases.map { it.id } else lesson.conversation.map { it.id }
                    progress.put(key, value.split('|').filter { it in known }.distinct().joinToString("|"))
                }
                "phrase" -> if (value is Int && value in 0..lesson.phrases.size) progress.put(key, value)
                "turn" -> if (value is Int && value in 0..lesson.conversation.size) progress.put(key, value)
                "review", "correct" -> if (value is Int && value in 0..3) progress.put(key, value)
                "rounds" -> if (value is Int && value in 0..1000000) progress.put(key, value)
                "supplemental" -> if (value is Int && value in 0..1) progress.put(key, value)
                "phase" -> if (value is String && value in setOf("repeat", "chat", "review", "summary")) progress.put(key, value)
            }
        } else if (key.startsWith("achievement.") && value is Int && value in 0..100) {
            val parts = key.split('.', limit = 3)
            val lesson = allowedLessons[parts.getOrNull(1)]
            if (lesson != null && lesson.phrases.any { it.id == parts.getOrNull(2) }) progress.put(key, value)
        }
    }
    return JSONObject().put("schema", 1).put("profileId", identity.profileId)
        .put("name", learner.name).put("avatar", learner.avatar).put("progress", progress)
}
