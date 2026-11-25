package com.nghialcvn.speakivra

import android.content.SharedPreferences

/** Best final recognition result per stable sentence ID. Never infer scores from old completion. */
internal fun achievementHearts(percent: Int): Int = when {
    percent >= 95 -> 3
    percent >= 75 -> 2
    percent >= 60 -> 1
    else -> 0
}
internal fun topicAccuracy(lesson: Lesson, score: (String) -> Int): Int =
    if (lesson.phrases.isEmpty()) 0 else lesson.phrases.sumOf { score(it.id).coerceIn(0, 100) } / lesson.phrases.size

internal class TopicAchievements(private val prefs: SharedPreferences) {
    private fun key(lesson: Lesson, phraseId: String) = "achievement.${lesson.id}.$phraseId"
    fun record(lesson: Lesson, phraseId: String, result: WordMatch) {
        if (!result.hasResult || lesson.phrases.none { it.id == phraseId }) return
        val key = key(lesson, phraseId)
        val best = maxOf(prefs.getInt(key, 0), result.matchPercent)
        prefs.edit().putInt(key, best).apply()
    }
    fun hearts(lesson: Lesson): Int = achievementHearts(topicAccuracy(lesson) { prefs.getInt(key(lesson, it), 0) })
    fun total(lessons: List<Lesson> = Lessons.all): Int = lessons.sumOf(::hearts)
}
