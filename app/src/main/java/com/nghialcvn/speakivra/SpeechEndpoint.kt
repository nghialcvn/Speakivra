package com.nghialcvn.speakivra

import kotlin.math.max
import kotlin.math.sqrt

/** Endpoint detection only: PCM energy never supplies a transcript or a pronunciation result. */
internal class SpeechEndpoint(
    private val sampleRate: Int = 16000,
    private val silenceMillis: Int = 1400,
    private val initialSilenceMillis: Int = 8000,
    private val maximumMillis: Int = 60000
) {
    enum class Reason { SPEECH_ENDED, NO_SPEECH, LIMIT }
    private var totalSamples = 0L
    private var voiceSamples = 0L
    private var quietSamples = 0L
    private var heardSpeech = false
    private var noiseFloor = 60.0
    private var ended = false

    fun accept(pcm: ByteArray, count: Int): Reason? {
        if (ended || count < 2) return null
        require(count <= pcm.size)
        val samples = count / 2
        var energy = 0.0
        for (i in 0 until samples) {
            val value = ((pcm[i * 2].toInt() and 255) or (pcm[i * 2 + 1].toInt() shl 8)).toShort().toInt()
            energy += value.toDouble() * value
        }
        val rms = sqrt(energy / samples)
        val voiced = rms >= max(180.0, noiseFloor * 2.8)
        totalSamples += samples
        if (voiced) {
            voiceSamples += samples
            quietSamples = 0
            // A click or one loud audio frame must not count as a spoken sentence.
            if (voiceSamples * 1000 >= sampleRate * 120L) heardSpeech = true
        } else {
            voiceSamples = 0
            if (heardSpeech) quietSamples += samples
            else noiseFloor = (noiseFloor * .95 + rms * .05).coerceIn(20.0, 250.0)
        }
        val reason = when {
            totalSamples * 1000 >= sampleRate * maximumMillis.toLong() -> Reason.LIMIT
            heardSpeech && quietSamples * 1000 >= sampleRate * silenceMillis.toLong() -> Reason.SPEECH_ENDED
            !heardSpeech && totalSamples * 1000 >= sampleRate * initialSilenceMillis.toLong() -> Reason.NO_SPEECH
            else -> null
        }
        if (reason != null) ended = true
        return reason
    }
}

/** Longer passages need time for ten sentences; short-utterance behaviour is unchanged. */
internal const val MAX_SPEECH_ATTEMPT_MILLIS = 900000
internal fun speechAttemptLimitMillis(wholePassage: Boolean, wordCount: Int = 0): Int =
    maxOf(if (wholePassage) 240000 else 60000,
        wordCount.coerceIn(0, 870).times(1000) + 30000).coerceAtMost(MAX_SPEECH_ATTEMPT_MILLIS)
