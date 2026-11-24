package com.nghialcvn.speakivra

import android.content.SharedPreferences

/**
 * Explanations are separate from English pronunciation accents.
 * Never touch the old lesson.*, speech_* or profile keys.
 */
internal object LearningOptions {
    const val VIETNAMESE = "vi"
    const val ENGLISH = "en"
    private const val MODE = "explanation_language"
    private const val REPEATS = "sample_repeat_count"

    fun language(prefs: SharedPreferences): String =
        if (prefs.getString(MODE, VIETNAMESE) == ENGLISH) ENGLISH else VIETNAMESE

    fun setLanguage(prefs: SharedPreferences, mode: String) {
        require(mode == VIETNAMESE || mode == ENGLISH)
        check(prefs.edit().putString(MODE, mode).commit())
    }

    fun repeats(prefs: SharedPreferences): Int =
        prefs.getInt(REPEATS, 1).coerceIn(1, 3)

    fun setRepeats(prefs: SharedPreferences, count: Int) {
        require(count in 1..3)
        check(prefs.edit().putInt(REPEATS, count).commit())
    }
}

/**
 * English-only mode provides actual English usage guidance, rather than
 * relabeling Vietnamese explanations or translating via an unverified API.
 * The original lesson sentence and scripted responses remain unchanged.
 */
internal object EnglishStudy {
    fun explanation(sentence: String): String {
        val s = sentence.trim().lowercase()
        return when {
            s.startsWith("could ") || s.startsWith("would ") ||
                s.startsWith("can i") || s.startsWith("can you") ->
                "A polite request or offer. Listen to the reply and answer naturally."
            s.startsWith("how much") || s.startsWith("how many") ->
                "A question about price or quantity. Answer with a number or an amount."
            s.startsWith("how ") ->
                "A question asking for details, a method, or someone's condition."
            s.startsWith("where ") ->
                "A question about a place or location. Answer with a clear location."
            s.startsWith("when ") || s.startsWith("what time") ->
                "A question about time. Answer with a date, day, or time."
            s.startsWith("what ") || s.startsWith("which ") || s.startsWith("who ") ->
                "An information question. Use a complete English sentence in your answer."
            s.endsWith("?") ->
                "A question. Listen for the key information and give a clear answer."
            s.startsWith("please ") || s.startsWith("turn ") || s.startsWith("go ") ->
                "An instruction or request. Use a friendly tone and clear word stress."
            s.startsWith("i'm ") || s.startsWith("i am ") || s.startsWith("i have ") ||
                s.startsWith("i ") || s.startsWith("my ") ->
                "A personal statement. Use it when talking about yourself or your experience."
            else ->
                "A useful everyday expression. Notice the context and repeat it naturally."
        }
    }

    fun pronunciationTip(sentence: String): String =
        if (sentence.trim().endsWith("?"))
            "Listen for rising or falling question intonation. Stress the key words, then repeat."
        else
            "Keep final consonants clear and link words naturally. Listen, pause, then repeat."

    /** English-only comprehension: match a question to its scripted English reply. */
    fun review(lesson: Lesson, index: Int): EnglishReview {
        require(lesson.conversation.size >= 3)
        val source = index.coerceIn(0, 2)
        val target = lesson.conversation[source]
        val distractors = lesson.conversation.asSequence().filterIndexed { i, _ -> i != source }
            .map { it.reply }.filter { it != target.reply }.distinct().take(2).toList()
        require(distractors.size == 2) { "English review needs three distinct replies." }
        val options = (listOf(target.reply) + distractors).let { values ->
            val rotation = source % values.size
            values.drop(rotation) + values.take(rotation)
        }
        return EnglishReview(target.prompt, options, options.indexOf(target.reply))
    }
}

internal data class EnglishReview(val prompt: String, val options: List<String>, val answer: Int)
