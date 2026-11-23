package com.nghialcvn.speakivra

import java.util.Locale
import java.text.Normalizer

internal data class WordMatch(val tokens: List<String>, val matched: List<Boolean>, val hasResult: Boolean) {
    val incorrect get() = matched.count { !it }
    val percent get() = if (tokens.isEmpty()) 100 else (incorrect * 100f / tokens.size).toInt()
    // Integer arithmetic keeps the exact 60% boundary, independent of display rounding.
    val canContinue get() = hasResult && tokens.isNotEmpty() && incorrect * 100 < tokens.size * 60
}

/** Ordered word matching, not acoustic pronunciation scoring. Extra recognized words do not
 * turn a missing reference word green. Repetitions cannot match the same spoken token twice. */
internal fun matchWords(reference: String, finalTranscript: String?): WordMatch {
    val tokens = Regex("\\S+").findAll(reference).map { it.value }.toList()
    fun normalize(value: String) = Normalizer.normalize(value.lowercase(Locale.US), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "").replace('đ', 'd').replace(Regex("[^\\p{L}\\p{N}]"), "")
    val expected = tokens.map(::normalize)
    // Bound unexpected provider output before allocating the alignment matrix.
    val spoken = finalTranscript.orEmpty().lowercase(Locale.US).replace(Regex("\\bi\\s+am\\b"), "im").split(Regex("\\s+")).map(::normalize).filter { it.isNotEmpty() }.take(512)
    val dp = Array(expected.size + 1) { IntArray(spoken.size + 1) }
    for (i in expected.indices.reversed()) for (j in spoken.indices.reversed()) {
        dp[i][j] = if (expected[i].isNotEmpty() && expected[i] == spoken[j]) 1 + dp[i + 1][j + 1]
        else maxOf(dp[i + 1][j], dp[i][j + 1])
    }
    val matched = MutableList(tokens.size) { false }
    var i = 0; var j = 0
    while (i < expected.size && j < spoken.size) {
        if (expected[i].isNotEmpty() && expected[i] == spoken[j]) { matched[i] = true; i++; j++ }
        else if (dp[i + 1][j] >= dp[i][j + 1]) i++ else j++
    }
    return WordMatch(tokens, matched, !finalTranscript.isNullOrBlank() && spoken.isNotEmpty())
}
