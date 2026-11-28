package com.nghialcvn.speakivra

import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.GZIPInputStream

/** Bundled, versioned course content. No network translation or speech recordings enter this catalogue. */
internal object ChineseCurriculum {
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun line(o: JSONObject) = ChineseLine(o.getString("id"), o.getString("hanzi"), o.getString("pinyin"), o.getString("vi"),
        o.optJSONArray("alternatives")?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty(),
        o.optJSONArray("alternativePinyin")?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty())
    val topics: List<ChineseTopic> by lazy {
        val stream = ChineseCurriculum::class.java.getResourceAsStream("/curriculum/chinese.json.gz")
            ?: error("Bundled Chinese curriculum is missing")
        val root = GZIPInputStream(stream).bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        val starters = ChineseStarterCurriculum.topics.associateBy { it.id }
        root.getJSONArray("topics").objects().map { o ->
            val id = o.getString("id")
            val original = starters[id]
            val sourcePhrases = o.getJSONArray("phrases").objects().map(::line)
            val phrases = original?.let { it.phrases + sourcePhrases.drop(it.phrases.size) } ?: sourcePhrases
            val sourceTurns = o.getJSONArray("turns").objects().map { turn ->
                ChineseTurn(line(turn.getJSONObject("prompt")), -1, line(turn.getJSONObject("answer")))
            }
            ChineseTopic(id, o.getString("level"), original?.title ?: o.getString("vi"), original?.readingTitle ?: o.getString("title"),
                original?.tip ?: o.getString("tip"), phrases,
                original?.let { it.turns + sourceTurns.drop(it.turns.size) } ?: sourceTurns,
                o.getJSONArray("readings").objects().map { p -> ChineseReadingPack(p.getString("id"), p.getString("title"),
                    p.getJSONArray("lines").objects().map(::line), p.getJSONArray("vocabulary").objects().map(::line)) },
                o.getJSONArray("exercises").objects().map { p -> ChineseExercisePack(p.getString("id"), p.getString("title"), p.getJSONArray("lines").objects().map(::line)) },
                starter = original != null)
        }.also { require(it.size == 145 && it.map { t -> t.id }.distinct().size == 145) }
    }
    val lessons by lazy { topics.map { it.lesson } }
    val extraIds by lazy { topics.flatMap { t -> (t.reading + t.exercises).map { it.id } + t.comprehension.map { it.id } }.toSet() }
}
