package com.nghialcvn.speakivra

import android.content.Context

internal const val CURRICULUM_MIGRATIONS = "speakivra_migrations"
internal const val RESET_108 = "profiles_reset_v108"

/** Explicitly requested one-time reset for V1.0.8. Never tie deletion to every version or launch. */
internal fun resetForCurriculum108(context: Context) {
    synchronized(LearnerStore::class.java) {
        val migrations = context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0)
        if (migrations.getBoolean(RESET_108, false)) return
        // Include orphan learner files, but restrict deletion to the exact UUID profile namespace.
        val names = java.io.File(context.applicationInfo.dataDir, "shared_prefs").listFiles().orEmpty()
            .map { it.name.removeSuffix(".bak").removeSuffix(".xml") }
            .filter { Regex("speakivra_learner_[a-f0-9-]{36}").matches(it) }.distinct()
        names.forEach { name ->
            check(context.getSharedPreferences(name, 0).edit().clear().commit())
            context.deleteSharedPreferences(name)
        }
        // This legacy file also holds the approved appearance; only learning/identity is reset.
        val original = context.getSharedPreferences("speakivra_progress", 0)
        val editor = original.edit()
        original.all.keys.filter { it.startsWith("lesson.") || it in setOf(
            "active_lesson", "completed", "phrase", "learner_name", "learner_avatar") }.forEach(editor::remove)
        check(editor.commit())
        check(context.getSharedPreferences("speakivra_learners", 0).edit().clear().putString("profiles", "[]").commit())
        check(migrations.edit().putBoolean(RESET_108, true).commit())
    }
}
