package com.nghialcvn.speakivra

import android.content.SharedPreferences

internal data class ChineseResume(val mode: String, val packId: String, val index: Int)

/** Same retained/current reward rule, without running the progress migration on a picker read. */
internal fun chineseSummaryDiamonds(prefs: SharedPreferences, topic: ChineseTopic): Int {
    val key = "lesson.${topic.id}.diamonds_earned"
    val achievements = TopicAchievements(prefs)
    val retained = if (prefs.contains(key)) prefs.getInt(key, 0)
        else ChineseStarterCurriculum.topics.firstOrNull { it.id == topic.id }
            ?.let { achievements.hearts(it.lesson) } ?: 0
    return maxOf(retained, achievements.hearts(topic.lesson))
}

/** Reuse the existing profile file and cloud whitelist, with unique zh_ lesson/content IDs. */
internal class ChineseProgress(private val prefs: SharedPreferences) {
    init {
        // Capture already-earned starter rewards before the larger denominator is introduced.
        val edit = prefs.edit()
        var changed = false
        ChineseStarterCurriculum.topics.forEach { topic ->
            val key = "lesson.${topic.id}.diamonds_earned"
            if (!prefs.contains(key)) {
                edit.putInt(key, TopicAchievements(prefs).hearts(topic.lesson)); changed = true
            }
        }
        if (changed) edit.commit()
    }
    private fun retainReward(topic: ChineseTopic) {
        val key = "lesson.${topic.id}.diamonds_earned"
        val value = diamonds(topic)
        if (value > prefs.getInt(key, 0)) prefs.edit().putInt(key, value).apply()
    }
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
    fun finished(topic: ChineseTopic): Boolean {
        val full = topic.copy(selectedReading = null, selectedExercises = null)
        return listOf("repeat", "chat", "review").all { done(full, it) == itemIds(full, it).size }
    }
    fun firstPending(topic: ChineseTopic, mode: String) = itemIds(topic, mode).indexOfFirst { it !in completed(topic, mode) }.coerceAtLeast(0)
    fun resume(topic: ChineseTopic): ChineseResume {
        val mode = modes.firstOrNull { done(topic, it) < itemIds(topic, it).size } ?: "repeat"
        if (mode == "read") {
            if (topic.legacyReading.any { it.id !in extras() })
                return ChineseResume(mode, "", firstPending(topic.copy(selectedReading = topic.legacyReading), mode))
            topic.readingPacks.firstOrNull { p -> p.lines.any { it.id !in extras() } }?.let { p ->
                return ChineseResume(mode, p.id, firstPending(topic.copy(selectedReading = p.lines), mode))
            }
        }
        if (mode == "exercise") {
            if (topic.legacyExercises.any { it.id !in extras() })
                return ChineseResume(mode, "", firstPending(topic.copy(selectedExercises = topic.legacyExercises), mode))
            topic.exercisePacks.firstOrNull { p -> p.lines.any { it.id !in extras() } }?.let { p ->
                return ChineseResume(mode, p.id, firstPending(topic.copy(selectedExercises = p.lines), mode))
            }
        }
        return ChineseResume(mode, "", firstPending(topic, mode))
    }
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
        retainReward(topic)
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
    fun levelOpen(level: String, topics: List<ChineseTopic>): Boolean {
        val previous = CourseLevels.prerequisite(level) ?: return true
        val required = topics.filter { it.level == previous }
        return required.size == 20 && required.all { prefs.getInt("lesson.${it.id}.rounds", 0) > 0 }
    }
    fun topicOpen(topic: ChineseTopic, topics: List<ChineseTopic>): Boolean {
        if (prefs.getInt("lesson.${topic.id}.rounds", 0) > 0 || topic.starter || topic.level in listOf("FO", "HK")) return true
        if (!levelOpen(topic.level, topics)) return false
        val peers = topics.filter { it.level == topic.level }
        val n = peers.indexOfFirst { it.id == topic.id }
        return n in 0..2 || (n > 2 && peers.subList(2, n).all { prefs.getInt("lesson.${it.id}.rounds", 0) > 0 })
    }
    fun diamonds(topic: ChineseTopic) = maxOf(prefs.getInt("lesson.${topic.id}.diamonds_earned", 0), TopicAchievements(prefs).hearts(topic.lesson))
    companion object { val modes = listOf("repeat", "chat", "read", "review", "exercise") }
}
