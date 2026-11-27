package com.nghialcvn.speakivra

import org.json.JSONArray
import org.json.JSONObject

/** Merge stable learned IDs and best achievements; retrying the same snapshot is idempotent. */
internal fun mergeCloudProgress(local: JSONObject, remote: JSONObject): JSONObject {
    val result = JSONObject()
    (local.keys().asSequence().toSet() + remote.keys().asSequence().toSet()).sorted().forEach { key ->
        val a = local.opt(key); val b = remote.opt(key)
        when {
            key == "active_lesson" -> result.put(key, (a as? String) ?: (b as? String))
            key == CHECK_IN_DATES -> {
                fun dates(v: Any?) = (v as? JSONArray)?.let { (0 until it.length()).mapNotNull { n -> it.opt(n) as? String } }.orEmpty()
                result.put(key, JSONArray(validCheckInDates(dates(a) + dates(b)).sorted()))
            }
            key == "extra.completed_ids" -> {
                fun ids(v: Any?) = (v as? JSONArray)?.let { (0 until it.length()).mapNotNull { n -> it.opt(n) as? String } }.orEmpty()
                result.put(key, JSONArray((ids(a) + ids(b)).distinct().sorted()))
            }
            key.endsWith(".learned_phrases") || key.endsWith(".learned_turns") ->
                result.put(key, (listOfNotNull(a as? String, b as? String).flatMap { it.split('|') }).filter { it.isNotEmpty() }.distinct().sorted().joinToString("|"))
            key.endsWith(".phase") -> {
                val phases = listOf("repeat", "chat", "review", "summary")
                result.put(key, phases[maxOf(phases.indexOf(a).coerceAtLeast(0), phases.indexOf(b).coerceAtLeast(0))])
            }
            a is Number || b is Number -> result.put(key, maxOf((a as? Number)?.toInt() ?: 0, (b as? Number)?.toInt() ?: 0))
        }
    }
    return result
}
internal fun cloudProgressValues(progress: JSONObject): Map<String, Any> = buildMap {
    progress.keys().forEach { key -> when (val value = progress.get(key)) {
        is String -> put(key, value)
        is Number -> put(key, value.toInt())
        is JSONArray -> put(key, (0 until value.length()).mapNotNull { value.opt(it) as? String }.toSet())
    } }
}
/** The same export whitelist is used at the import boundary; untrusted JSON cannot change device settings. */
internal fun sanitizedCloudProgress(profileId: String, raw: JSONObject): JSONObject = profileSyncPayload(
    Learner(ORIGINAL_LEARNER, "Cloud", 0), GuestIdentity(profileId, ""), cloudProgressValues(raw)
).getJSONObject("progress")
