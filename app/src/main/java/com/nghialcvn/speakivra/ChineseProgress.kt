package com.nghialcvn.speakivra

import android.content.SharedPreferences

/** Reuse the existing profile file and cloud whitelist, with unique zh_ lesson/content IDs. */
internal class ChineseProgress(private val prefs: SharedPreferences) {
    private fun ids(topic: ChineseTopic, suffix: String) = prefs.getString("lesson.${topic.id}.$suffix", "")
        .orEmpty().split('|').filter { it.isNotEmpty() }.toSet()
    private fun extras() = prefs.getStringSet("extra.completed_ids", emptySet()).orEmpty()
    fun completed(topic: ChineseTopic, mode: String): Set<String> = when (mode) {
        "repeat" -> ids(topic, "learned_phrases")
        "chat" -> ids(topic, "learned_turns")
        "read", "exercise", "review" -> extras()
        else -> emptySet()
    }
    fun itemIds(topic: ChineseTopic, mode: String): List<String> = when (mode) {
        "repeat" -> topic.phrases.map { it.id }
        "chat" -> topic.turns.map { it.prompt.id }
        "read" -> topic.reading.map { it.id }
        "exercise" -> topic.exercises.map { it.id }
        "review" -> topic.comprehension.map { it.id }
        else -> emptyList()
    }
    fun done(topic: ChineseTopic, mode: String) = itemIds(topic, mode).count { it in completed(topic, mode) }
    fun finished(topic: ChineseTopic) = modes.all { done(topic, it) == itemIds(topic, it).size }
    fun firstPending(topic: ChineseTopic, mode: String) = itemIds(topic, mode).indexOfFirst { it !in completed(topic, mode) }.coerceAtLeast(0)
    fun complete(topic: ChineseTopic, mode: String, index: Int, result: ChineseMatch): Boolean {
        if (if (mode == "exercise") !result.exact else !result.canContinue) return false
        val item = itemIds(topic, mode).getOrNull(index) ?: return false
        val edit = prefs.edit()
        when (mode) {
            "repeat", "chat" -> {
                val suffix = if (mode == "repeat") "learned_phrases" else "learned_turns"
                edit.putString("lesson.${topic.id}.$suffix", (ids(topic, suffix) + item).sorted().joinToString("|"))
                if (mode == "repeat") {
                    val key = "achievement.${topic.id}.$item"
                    edit.putInt(key, maxOf(prefs.getInt(key, 0), result.matchPercent))
                }
            }
            "read", "exercise" -> edit.putStringSet("extra.completed_ids", extras() + item)
            else -> return false
        }
        if (!edit.commit()) return false
        // Once earned, completion survives replay and future curriculum additions.
        if (finished(topic) && prefs.getInt("lesson.${topic.id}.rounds", 0) == 0)
            prefs.edit().putInt("lesson.${topic.id}.rounds", 1).apply()
        return true
    }
    fun completeComprehension(topic: ChineseTopic, index: Int, selected: Int): Boolean {
        val question = topic.comprehension.getOrNull(index) ?: return false
        if (selected != question.answer) return false
        if (!prefs.edit().putStringSet("extra.completed_ids", extras() + question.id).commit()) return false
        if (finished(topic) && prefs.getInt("lesson.${topic.id}.rounds", 0) == 0)
            prefs.edit().putInt("lesson.${topic.id}.rounds", 1).apply()
        return true
    }
    fun diamonds(topic: ChineseTopic) = TopicAchievements(prefs).hearts(topic.lesson)
    companion object { val modes = listOf("repeat", "chat", "read", "review", "exercise") }
}
