package com.nghialcvn.speakivra

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal data class Learner(val id: String, val name: String, val avatar: Int)
internal const val ORIGINAL_LEARNER = "original"
internal fun learnerPreferenceName(id: String): String = if (id == ORIGINAL_LEARNER) "speakivra_progress" else {
    require(Regex("[a-f0-9-]{36}").matches(id))
    "speakivra_learner_$id"
}
internal fun encodeLearners(learners: List<Learner>): String = JSONArray().apply {
    learners.forEach { put(JSONObject().put("id", it.id).put("name", it.name).put("avatar", it.avatar)) }
}.toString()
internal fun decodeLearners(value: String): List<Learner> {
    val data = JSONArray(value)
    val learners = (0 until data.length()).map { index ->
        val item = data.getJSONObject(index)
        Learner(item.getString("id"), item.getString("name"), item.getInt("avatar")).also {
            learnerPreferenceName(it.id)
            require(it.name.isNotBlank() && it.name.length <= 30 && it.avatar in 0..5)
        }
    }
    require(learners.distinctBy { it.id }.size == learners.size)
    return learners
}
/** Identity is immutable: editing a name/avatar must never select a different progress file. */
internal fun updateLearnerList(existing: List<Learner>, id: String, name: String, avatar: Int): List<Learner> {
    val clean = name.trim()
    require(clean.isNotEmpty() && clean.length <= 30 && clean.none { it.isISOControl() } && avatar in 0..5) { "Tên không hợp lệ. Hãy thử lại." }
    require(existing.any { it.id == id }) { "Không tìm thấy hồ sơ." }
    require(existing.none { it.id != id && it.name.equals(clean, ignoreCase = true) }) { "Tên người học đã có. Hãy chọn tên khác." }
    return existing.map { if (it.id == id) it.copy(name = clean, avatar = avatar) else it }
}

internal class LearnerStore(private val context: Context) {
    private val registry = context.getSharedPreferences("speakivra_learners", 0)
    fun all(): List<Learner> {
        val saved = registry.getString("profiles", null)
        if (saved != null) return decodeLearners(saved) // Never silently replace a damaged registry.
        // Existing installs have an explicit profile registry and are kept unchanged.
        // Legacy installs predating the registry had learning preferences in the
        // original file. Only those genuine legacy installs keep their old profile.
        val legacyPrefs = context.getSharedPreferences("speakivra_progress", 0)
        val hasLegacyProgress = legacyPrefs.contains("active_lesson") ||
            legacyPrefs.contains("completed") ||
            legacyPrefs.all.keys.any { it.startsWith("lesson.") }
        val first = if (hasLegacyProgress)
            listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)) else emptyList()
        check(registry.edit().putString("profiles", encodeLearners(first)).commit())
        return first // Fresh installs must explicitly create a named learner.
    }
    fun preferences(learner: Learner): SharedPreferences = context.getSharedPreferences(learnerPreferenceName(learner.id), 0)
    fun add(name: String, avatar: Int): Learner {
        val clean = name.trim()
        require(clean.isNotEmpty() && clean.length <= 30 && clean.none { it.isISOControl() } && avatar in 0..5)
        val existing = all()
        require(existing.none { it.name.equals(clean, ignoreCase = true) }) { "Tên người học đã có. Hãy chọn tên khác." }
        val learner = Learner(UUID.randomUUID().toString(), clean, avatar)
        check(registry.edit().putString("profiles", encodeLearners(existing + learner)).commit())
        return learner
    }
    fun update(id: String, name: String, avatar: Int) {
        val updated = updateLearnerList(all(), id, name, avatar)
        check(registry.edit().putString("profiles", encodeLearners(updated)).commit())
    }
    fun learned(learner: Learner): Pair<Int, Int> {
        val prefs = preferences(learner)
        val total = Lessons.all.sumOf { it.phrases.size }
        val done = Lessons.all.sumOf { lesson ->
            val progress = restoreLessonProgress(lesson, prefs::getInt) { key, default -> prefs.getString(key, default) ?: default }
            if (progress.phase != "repeat" || progress.rounds > 0) lesson.phrases.size else progress.phrase
        }
        return done to total
    }
}
