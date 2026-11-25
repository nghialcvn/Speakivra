package com.nghialcvn.speakivra

import java.util.Locale
import java.text.Normalizer

internal data class WordMatch(val tokens: List<String>, val matched: List<Boolean>, val hasResult: Boolean) {
    val correct get() = matched.count { it }
    val matchPercent get() = if (tokens.isEmpty()) 0 else correct * 100 / tokens.size
    val incorrect get() = matched.count { !it }
    val percent get() = if (tokens.isEmpty()) 100 else (incorrect * 100f / tokens.size).toInt()
    val canContinue get() = hasResult && tokens.isNotEmpty() && incorrect * 100 < tokens.size * 60
}

private data class MatchPart(val value: String, val owner: Int)
private data class MatchAtom(val value: String, val owners: Set<Int>)
private val contractions = mapOf(
    "i'm" to "i am", "im" to "i am", "you're" to "you are", "we're" to "we are", "they're" to "they are",
    "he's" to "he is", "she's" to "she is", "it's" to "it is", "that's" to "that is", "what's" to "what is",
    "i've" to "i have", "you've" to "you have", "we've" to "we have", "they've" to "they have",
    "i'll" to "i will", "you'll" to "you will", "he'll" to "he will", "she'll" to "she will", "we'll" to "we will", "they'll" to "they will",
    "don't" to "do not", "doesn't" to "does not", "didn't" to "did not", "can't" to "can not", "cannot" to "can not",
    "won't" to "will not", "isn't" to "is not", "aren't" to "are not", "wasn't" to "was not", "weren't" to "were not",
    "haven't" to "have not", "hasn't" to "has not", "hadn't" to "had not", "couldn't" to "could not", "wouldn't" to "would not", "shouldn't" to "should not"
)
private val smallNumbers = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen").withIndex().associate { it.value to it.index.toLong() }
private val tens = mapOf("twenty" to 20L, "thirty" to 30L, "forty" to 40L, "fifty" to 50L, "sixty" to 60L, "seventy" to 70L, "eighty" to 80L, "ninety" to 90L)
private val scales = mapOf("thousand" to 1000L, "million" to 1000000L)

/** Parse only grammatical cardinal groups. "one two" stays two separate words,
 * so ordered matching and repeated-word accounting are never relaxed. */
private fun cardinal(parts: List<MatchPart>, start: Int): Pair<Long, Int>? {
    fun under100(at: Int): Pair<Long, Int>? {
        val word = parts.getOrNull(at)?.value ?: return null
        smallNumbers[word]?.let { return it to at + 1 }
        val ten = tens[word] ?: return null
        val unit = smallNumbers[parts.getOrNull(at + 1)?.value]
        return if (unit != null && unit in 1..9) ten + unit to at + 2 else ten to at + 1
    }
    fun under1000(at: Int): Pair<Long, Int>? {
        val unit = smallNumbers[parts.getOrNull(at)?.value]
        if (unit != null && unit in 1..9 && parts.getOrNull(at + 1)?.value == "hundred") {
            var end = at + 2
            val remainderStart = if (parts.getOrNull(end)?.value == "and") end + 1 else end
            val remainder = under100(remainderStart)
            if (remainder != null) end = remainder.second
            return unit * 100 + (remainder?.first ?: 0) to end
        }
        return under100(at)
    }
    var group = under1000(start) ?: return null
    var sum = 0L
    var lastScale = Long.MAX_VALUE
    while (true) {
        val scale = scales[parts.getOrNull(group.second)?.value] ?: break
        if (scale >= lastScale || group.first == 0L) break
        lastScale = scale
        sum += group.first * scale
        val end = group.second + 1
        val nextStart = if (parts.getOrNull(end)?.value == "and") end + 1 else end
        val next = under1000(nextStart) ?: return sum to end
        group = next
    }
    return sum + group.first to group.second
}

private fun canonical(tokens: List<String>): List<MatchAtom> {
    val parts = tokens.take(512).flatMapIndexed { owner, token ->
        val folded = Normalizer.normalize(token.lowercase(Locale.US), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "").replace('đ', 'd').replace('’', '\'').replace('‘', '\'')
        Regex("[0-9]{1,3}(?:,[0-9]{3})+(?:\\.[0-9]+)?|[0-9]+(?:\\.[0-9]+)?|[\\p{L}]+(?:'[\\p{L}]+)*").findAll(folded).flatMap { match ->
            (contractions[match.value] ?: match.value).split(' ').map { MatchPart(it.replace("'", ""), owner) }
        }.toList()
    }
    val atoms = mutableListOf<MatchAtom>()
    var index = 0
    while (index < parts.size && atoms.size < 512) {
        val part = parts[index]
        val digits = part.value.replace(",", "")
        if (digits.firstOrNull()?.isDigit() == true) {
            atoms += MatchAtom("number:$digits", setOf(part.owner)); index++
        } else {
            val number = cardinal(parts, index)
            if (number == null) {
                atoms += MatchAtom(part.value, setOf(part.owner)); index++
            } else {
                atoms += MatchAtom("number:${number.first}", parts.subList(index, number.second).map { it.owner }.toSet())
                index = number.second
            }
        }
    }
    return atoms
}

/** Symmetric text equivalence, then ordered alignment. Final ASR results only;
 * no fuzzy guessing, invented transcript, or acoustic pronunciation score. */
internal fun matchWords(reference: String, finalTranscript: String?): WordMatch {
    val tokens = Regex("\\S+").findAll(reference).map { it.value }.toList()
    val expected = canonical(tokens)
    val spoken = canonical(Regex("\\S+").findAll(finalTranscript.orEmpty()).map { it.value }.toList())
    val dp = Array(expected.size + 1) { IntArray(spoken.size + 1) }
    for (i in expected.indices.reversed()) for (j in spoken.indices.reversed()) {
        dp[i][j] = if (expected[i].value == spoken[j].value) 1 + dp[i + 1][j + 1]
        else maxOf(dp[i + 1][j], dp[i][j + 1])
    }
    val aligned = BooleanArray(expected.size)
    var i = 0; var j = 0
    while (i < expected.size && j < spoken.size) {
        if (expected[i].value == spoken[j].value) { aligned[i] = true; i++; j++ }
        else if (dp[i + 1][j] >= dp[i][j + 1]) i++ else j++
    }
    val matched = tokens.indices.map { owner ->
        val relevant = expected.indices.filter { owner in expected[it].owners }
        relevant.isNotEmpty() && relevant.all { aligned[it] }
    }
    return WordMatch(tokens, matched, !finalTranscript.isNullOrBlank() && spoken.isNotEmpty())
}
