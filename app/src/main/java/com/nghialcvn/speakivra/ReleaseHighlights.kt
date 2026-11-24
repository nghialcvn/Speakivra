package com.nghialcvn.speakivra

/** Only short change titles belong in app update cards/popups. */
internal fun releaseHighlights(notes: String): List<String> {
    fun clean(line: String) = line.trim().replace(Regex("^#{1,6}\\s+|^[-*•]\\s+"), "")
        .replace("**", "").replace("`", "").replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1").trim()
    val lines = notes.lineSequence().map(String::trim).filter(String::isNotBlank).toList()
    val bullets = lines.filter { Regex("^[-*•]\\s+").containsMatchIn(it) }
    val headings = lines.filter { Regex("^#{2,6}\\s+").containsMatchIn(it) }
    val selected = headings.ifEmpty { bullets }.ifEmpty { lines.take(1) }
    return selected.map(::clean).map { title ->
        val brief = title.substringBefore(": ").substringBefore(". ").substringBefore(" — ")
        if (brief.length <= 72) brief else brief.take(72).substringBeforeLast(' ') + "…"
    }.filter(String::isNotBlank).distinct().take(5).ifEmpty { listOf("Cải tiến và sửa lỗi ứng dụng") }
}
