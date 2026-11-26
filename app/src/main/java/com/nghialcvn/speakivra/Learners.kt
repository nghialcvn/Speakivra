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
    init { resetForCurriculum108(context) }
    fun all(): List<Learner> {
        val saved = registry.getString("profiles", null)
        if (saved != null) return decodeLearners(saved)
        check(registry.edit().putString("profiles", "[]").commit())
        return emptyList()
    }
    fun visible(uid: String?): List<Learner> = all().filter { GuestIdentityStore(context).peek(it)?.canOpen(uid) != false }
    fun findCloud(profileId: String): Learner? = all().firstOrNull { GuestIdentityStore(context).peek(it)?.profileId == profileId }
    @Synchronized fun importCloud(profileId: String, uid: String, name: String, avatar: Int, progress: JSONObject): Learner {
        require(name.isNotBlank() && name.length <= 30 && name.none { it.isISOControl() } && avatar in 0..5)
        val existing = findCloud(profileId)
        require(existing == null || GuestIdentityStore(context).peek(existing)?.ownerUid == null || GuestIdentityStore(context).peek(existing)?.ownerUid == uid)
        val person = existing?.copy(name = name, avatar = avatar) ?: Learner(UUID.randomUUID().toString(), name, avatar)
        // Bind before making a restored learner visible. No secret is needed to restore an already owned record.
        GuestIdentityStore(context).restoreOwned(person, profileId, uid)
        val list = all()
        check(registry.edit().putString("profiles", encodeLearners(if (existing == null) list + person else list.map { if (it.id == person.id) person else it })).commit())
        val edit = preferences(person).edit()
        val localProgress = if (existing == null) JSONObject() else profileSyncPayload(existing, requireNotNull(GuestIdentityStore(context).peek(existing)), preferences(existing).all).getJSONObject("progress")
        val combined = sanitizedCloudProgress(profileId, mergeCloudProgress(progress, localProgress))
        cloudProgressValues(combined).forEach { (key,value) -> when(value) {
            is Int -> edit.putInt(key,value)
            is String -> edit.putString(key,value)
            is Set<*> -> edit.putStringSet(key,value.filterIsInstance<String>().toSet())
        } }
        check(edit.commit())
        return person
    }
    fun preferences(learner: Learner): SharedPreferences = context.getSharedPreferences(learnerPreferenceName(learner.id), 0)
    fun add(name: String, avatar: Int): Learner {
        val clean = name.trim()
        require(clean.isNotEmpty() && clean.length <= 30 && clean.none { it.isISOControl() } && avatar in 0..5)
        val existing = all()
        require(visible(AccountRuntime.get(context).uid).none { it.name.equals(clean, ignoreCase = true) }) { "Tên người học đã có. Hãy chọn tên khác." }
        val learner = Learner(UUID.randomUUID().toString(), clean, avatar)
        check(registry.edit().putString("profiles", encodeLearners(existing + learner)).commit())
        return learner
    }
    fun update(id: String, name: String, avatar: Int) {
        val values = all()
        val visibleUpdated = updateLearnerList(visible(AccountRuntime.get(context).uid), id, name, avatar)
        val updated = values.map { old -> visibleUpdated.firstOrNull { it.id == old.id } ?: old }
        check(registry.edit().putString("profiles", encodeLearners(updated)).commit())
        AccountRuntime.get(context).metadataChanged(updated.first { it.id == id })
    }
    fun learned(learner: Learner): Pair<Int, Int> {
        val prefs = preferences(learner)
        val total = Lessons.catalogue.sumOf { it.phrases.size }
        val done = Lessons.catalogue.sumOf { lesson ->
            val progress = restoreLessonProgress(lesson, prefs::getInt) { key, default -> prefs.getString(key, default) ?: default }
            if (progress.phase != "repeat" || progress.rounds > 0) lesson.phrases.size else progress.phrase
        }
        return done to total
    }
}
