package com.nghialcvn.speakivra

import org.json.JSONArray
import org.json.JSONObject

/** Stable storage boundaries; the visible course, learner identity and scoring stay unchanged. */
internal object ChineseCloudPartitions {
    const val MAX_BYTES = 131072
    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2", "FO", "HK")
    fun documentId(level: String): String { require(level in levels); return "zh_$level" }
    private val topicLevels by lazy { ChineseCurriculum.topics.associate { it.id to it.level } }
    private val extraLevels by lazy { ChineseCurriculum.topics.flatMap { topic ->
        (topic.reading + topic.exercises).map { it.id to topic.level } + topic.comprehension.map { it.id to topic.level }
    }.toMap() }
    private val starters by lazy { ChineseStarterCurriculum.topics.associateBy { it.id } }
    private val legacyExtraIds by lazy { starters.values.flatMap { t ->
        (t.reading + t.exercises).map { it.id } + t.comprehension.map { it.id }
    }.toSet() }
    private fun lessonId(key: String): String? = if (key.startsWith("lesson.") || key.startsWith("achievement."))
        key.split('.', limit = 3).getOrNull(1) else null
    private fun ids(value: Any?): List<String> = (value as? JSONArray)?.let { a ->
        (0 until a.length()).mapNotNull { a.opt(it) as? String }
    }.orEmpty()

    /** Called on sanitized data. No resume key is spread across competing level documents. */
    fun levelProgress(progress: JSONObject, level: String): JSONObject {
        require(level in levels)
        val result = JSONObject()
        progress.keys().forEach { key ->
            if (key == "extra.completed_ids") {
                val selected = ids(progress.opt(key)).filter { extraLevels[it] == level }.distinct().sorted()
                if (selected.isNotEmpty()) result.put(key, JSONArray(selected))
            } else if (topicLevels[lessonId(key)] == level) result.put(key, progress.get(key))
        }
        return result
    }

    /** Keep the original 1.2.7 Chinese subset readable to old clients, without duplicating the full course. */
    fun rootProgress(progress: JSONObject): JSONObject {
        val result = JSONObject()
        progress.keys().forEach { key ->
            val value = progress.get(key)
            val topicId = lessonId(key)
            if (key == "extra.completed_ids") {
                result.put(key, JSONArray(ids(value).filter { it !in extraLevels || it in legacyExtraIds }.distinct().sorted()))
            } else if (topicId !in topicLevels) result.put(key, value)
            else starters[topicId]?.let { old ->
                val field = key.split('.', limit = 3)[2]
                when {
                    key.startsWith("achievement.") -> if (old.phrases.any { it.id == field }) result.put(key, value)
                    field == "phrase" -> result.put(key, (value as Number).toInt().coerceAtMost(old.phrases.size))
                    field == "turn" -> result.put(key, (value as Number).toInt().coerceAtMost(old.turns.size))
                    field in setOf("learned_phrases", "learned_turns") -> {
                        val known = if (field == "learned_phrases") old.phrases.map { it.id }.toSet() else old.turns.map { it.prompt.id }.toSet()
                        result.put(key, (value as String).split('|').filter { it in known }.distinct().sorted().joinToString("|"))
                    }
                    else -> result.put(key, value)
                }
            }
        }
        return result
    }
    fun checkedText(progress: JSONObject): String = progress.toString().also {
        check(it.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "Tiến độ cấp học quá lớn để đồng bộ." }
    }
}
