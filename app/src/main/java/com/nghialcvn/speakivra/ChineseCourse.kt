package com.nghialcvn.speakivra

import java.text.Normalizer
import java.util.Locale

internal data class ChineseLine(val id: String, val hanzi: String, val pinyin: String, val vietnamese: String)
internal data class ChineseTurn(val prompt: ChineseLine, val answerIndex: Int)
internal data class ChineseComprehension(val id: String, val prompt: ChineseLine, val options: List<String>, val answer: Int)
internal data class ChineseTopic(val id: String, val level: String, val title: String, val readingTitle: String,
    val tip: String, val phrases: List<ChineseLine>, val turns: List<ChineseTurn>) {
    // The first four authored lines form the short passage, also practised individually.
    val reading get() = phrases.take(4).mapIndexed { n, line -> line.copy(id = "${id}_r${n + 1}") }
    val exercises get() = phrases.drop(4).mapIndexed { n, line -> line.copy(id = "${id}_e${n + 1}") }
    val comprehension get() = turns.take(3).mapIndexed { n, turn ->
        val correct = phrases[turn.answerIndex].vietnamese
        val options = (listOf(correct) + phrases.map { it.vietnamese }.filter { it != correct }.distinct().take(2))
            .let { it.drop(n % 3) + it.take(n % 3) }
        ChineseComprehension("${id}_c${n + 1}", turn.prompt, options, options.indexOf(correct))
    }
    val lesson get() = Lesson(id, readingTitle, title,
        phrases.map { Phrase(it.hanzi, it.vietnamese, tip, it.id) },
        turns.map { turn -> val answer = phrases[turn.answerIndex]
            ConversationTurn(turn.prompt.hanzi, turn.prompt.vietnamese, answer.hanzi, answer.vietnamese, turn.prompt.id) })
    fun personalize(name: String): ChineseTopic = if (id != "zh_a1_greetings") this else copy(
        phrases = phrases.map { line -> if (line.id.endsWith("_p01")) line.copy(
            hanzi = "我叫$name。", pinyin = "Wǒ jiào $name.", vietnamese = "Tôi tên là $name.") else line })
}

/** Character alignment for Hanzi, whole tokens for Latin names. No Pinyin/acoustic scoring. */
internal data class ChineseMatch(val tokens: List<String>, val matched: List<Boolean>, val spokenCount: Int,
    val extra: Int, val hasResult: Boolean, val negationMismatch: Boolean) {
    val correct get() = matched.count { it }
    val denominator get() = maxOf(tokens.size, spokenCount)
    val matchPercent get() = if (denominator == 0) 0 else correct * 100 / denominator
    val exact get() = hasResult && correct == tokens.size && extra == 0 && !negationMismatch
    val canContinue get() = hasResult && denominator > 0 &&
        (denominator - correct) * 100 < denominator * 60 && !negationMismatch
}
private fun chineseNumber(value: Int): String {
    val digits = "零一二三四五六七八九"
    if (value < 10) return digits[value].toString()
    val places = listOf(1000 to "千", 100 to "百", 10 to "十", 1 to "")
    var remaining = value
    var text = ""
    var zero = false
    places.forEach { (unit, label) ->
        val digit = remaining / unit
        remaining %= unit
        if (digit > 0) {
            if (zero) text += "零"
            if (!(unit == 10 && digit == 1 && text.isEmpty())) text += digits[digit]
            text += label
            zero = false
        } else if (text.isNotEmpty() && remaining > 0) zero = true
    }
    return text
}
internal fun chineseTokens(value: String): List<String> {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
    val clocks = Regex("(?<![0-9])([01]?[0-9]|2[0-3]):([0-5][0-9])(?![0-9])").replace(normalized) { match ->
        val hour = chineseNumber(match.groupValues[1].toInt())
        val minute = match.groupValues[2].toInt()
        hour + "点" + when (minute) { 0 -> ""; 30 -> "半"; else -> chineseNumber(minute) + "分" }
    }
    val numbers = Regex("[0-9]+").replace(clocks) { match ->
        match.value.toIntOrNull()?.takeIf { it in 0..9999 && (match.value.length == 1 || !match.value.startsWith('0')) }
            ?.let(::chineseNumber) ?: match.value
    }
    val equivalentNumbers = numbers.replace(Regex("两(?=个|点|天|晚|碗|张|件|小时|百|千|人|年|月|本|位|次)"), "二")
    val folded = Normalizer.normalize(equivalentNumbers, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "").replace('đ', 'd')
    return Regex("\\p{IsHan}|[a-z]+|[0-9]+|\\p{L}+").findAll(folded).map { it.value }.take(512).toList()
}
internal fun matchChinese(reference: String, transcript: String?): ChineseMatch {
    val expected = chineseTokens(reference); val spoken = chineseTokens(transcript.orEmpty())
    val dp = Array(expected.size + 1) { IntArray(spoken.size + 1) }
    for (i in expected.indices.reversed()) for (j in spoken.indices.reversed())
        dp[i][j] = if (expected[i] == spoken[j]) 1 + dp[i + 1][j + 1] else maxOf(dp[i + 1][j], dp[i][j + 1])
    val aligned = BooleanArray(expected.size)
    var i = 0; var j = 0
    while (i < expected.size && j < spoken.size) {
        if (expected[i] == spoken[j]) { aligned[i] = true; i++; j++ }
        else if (dp[i + 1][j] >= dp[i][j + 1]) i++ else j++
    }
    val negative = setOf("不", "没", "别", "无")
    return ChineseMatch(expected, aligned.toList(), spoken.size, spoken.size - aligned.count { it },
        expected.isNotEmpty() && spoken.isNotEmpty() && !transcript.isNullOrBlank(),
        expected.filter { it in negative } != spoken.filter { it in negative })
}
