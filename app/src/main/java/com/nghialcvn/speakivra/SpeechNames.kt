package com.nghialcvn.speakivra

import java.text.Normalizer

/** Provider hints improve recognition; they never replace words returned by the recognizer. */
internal fun speechNameHints(name: String?): ArrayList<String> {
    val display = name?.trim()?.takeIf { it.isNotBlank() } ?: return arrayListOf()
    val clean = Normalizer.normalize(display, Normalizer.Form.NFC)
    val folded = Normalizer.normalize(clean, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
        .replace('đ', 'd').replace('Đ', 'D')
    return arrayListOf<String>().apply {
        listOf(clean, folded).distinct().forEach {
            add(it); add("My name is $it"); add("I'm $it"); add("I am $it")
        }
    }
}

/** Select an actual provider alternative only when it resolves the complete learner name
 * in an introduction. Ordinary sentences retain the provider's first-ranked transcript. */
internal fun selectNameHypothesis(candidates: List<String>, name: String?, prefix: String = ""): String? {
    val choices = candidates.filter { it.isNotBlank() }.take(5)
    val first = choices.firstOrNull() ?: return null
    if (name.isNullOrBlank()) return first
    fun full(value: String) = listOf(prefix, value).filter { it.isNotBlank() }.joinToString(" ")
    fun introduction(value: String) = Regex("(?i)^\\s*(?:my\\s+name\\s+is|i\\s+am|i['’]?m)\\b").containsMatchIn(full(value))
    if (!introduction(first)) return first
    return choices.firstOrNull { introduction(it) && matchWords("My name is $name", full(it)).nameMatchesExactly } ?: first
}
