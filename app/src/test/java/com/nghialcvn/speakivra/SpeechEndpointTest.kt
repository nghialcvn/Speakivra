package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class SpeechEndpointTest {
    private fun frame(amplitude: Int, millis: Int = 20): ByteArray = ByteArray(16000 * millis / 1000 * 2).also { bytes ->
        for (i in 0 until bytes.size / 2) {
            val sample = if (i % 2 == 0) amplitude else -amplitude
            bytes[i * 2] = sample.toByte(); bytes[i * 2 + 1] = (sample shr 8).toByte()
        }
    }
    private fun feed(endpoint: SpeechEndpoint, amplitude: Int, millis: Int): SpeechEndpoint.Reason? {
        val pcm = frame(amplitude)
        var result: SpeechEndpoint.Reason? = null
        repeat(millis / 20) { result = endpoint.accept(pcm, pcm.size) ?: result }
        return result
    }
    @Test fun spokenSentenceEndsAutomaticallyAfterSilence() {
        val endpoint = SpeechEndpoint()
        assertNull(feed(endpoint, 0, 2000))
        assertNull(feed(endpoint, 1200, 600))
        assertNull(feed(endpoint, 0, 1380))
        assertEquals(SpeechEndpoint.Reason.SPEECH_ENDED, feed(endpoint, 0, 20))
        assertNull(feed(endpoint, 0, 2000)) // Only one stop callback per attempt.
    }
    @Test fun pausesInsideASlowSentenceDoNotCutItOff() {
        val endpoint = SpeechEndpoint()
        assertNull(feed(endpoint, 900, 200))
        assertNull(feed(endpoint, 0, 1000))
        assertNull(feed(endpoint, 1000, 800))
        assertNull(feed(endpoint, 0, 1200))
        assertEquals(SpeechEndpoint.Reason.SPEECH_ENDED, feed(endpoint, 0, 200))
    }
    @Test fun backgroundHumAndSingleClicksDoNotBecomeSpeech() {
        val endpoint = SpeechEndpoint()
        assertNull(feed(endpoint, 100, 2000))
        assertNull(feed(endpoint, 2000, 20))
        assertNull(feed(endpoint, 80, 2000))
        assertEquals(SpeechEndpoint.Reason.NO_SPEECH, feed(endpoint, 0, 3980))
    }
    @Test fun expandedPassageCanContinueAfterOneMinuteButStillStopsAtTheBoundedLimit() {
        assertEquals(60000, speechAttemptLimitMillis(false))
        assertEquals(240000, speechAttemptLimitMillis(true))
        val endpoint = SpeechEndpoint(maximumMillis = speechAttemptLimitMillis(true))
        assertNull(feed(endpoint, 2000, 60000))
        assertNull(feed(endpoint, 2000, 179980))
        assertEquals(SpeechEndpoint.Reason.LIMIT, feed(endpoint, 2000, 20))
    }
    @Test fun longUtterancesStillHaveAFiniteLimit() {
        val endpoint = SpeechEndpoint(maximumMillis = 2000)
        assertNull(feed(endpoint, 2000, 1980))
        assertEquals(SpeechEndpoint.Reason.LIMIT, feed(endpoint, 2000, 20))
    }
    @Test fun shortGreetingsAreLongEnoughToTriggerAnEndpoint() {
        val endpoint = SpeechEndpoint()
        assertNull(feed(endpoint, 1200, 140))
        assertEquals(SpeechEndpoint.Reason.SPEECH_ENDED, feed(endpoint, 0, 1400))
    }
}
