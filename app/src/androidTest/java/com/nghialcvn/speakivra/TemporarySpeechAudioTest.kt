package com.nghialcvn.speakivra

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemporarySpeechAudioTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun microphoneEndsWithoutASecondTapAndKeepsTheTemporaryRecording() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.executeShellCommand("pm grant ${context.packageName} ${Manifest.permission.RECORD_AUDIO}").use {
            java.io.FileInputStream(it.fileDescriptor).readBytes()
        }
        val phase = java.util.concurrent.atomic.AtomicReference("idle")
        val capture = SpeechCapture(context) { state, _, _ -> phase.set(state) }
        try {
            ui.runOnUiThread { capture.start() }
            assertEquals("listening", phase.get())
            // The emulator microphone supplies real PCM silence. The no-speech endpoint
            // must release it without tapping Stop, even without a recognition provider.
            ui.waitUntil(timeoutMillis = 20000) { phase.get() != "listening" }
            ui.runOnIdle {
                assertTrue("Auto-stop must keep this sentence's replay buffer", capture.hasRecording)
                assertNotEquals("listening", capture.phase)
            }
        } finally { ui.runOnUiThread { capture.cancel() } }
        assertFalse(capture.hasRecording)
    }
    @Test fun realMicrophoneBufferNeedsExplicitReplayAndIsDiscarded() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.executeShellCommand("pm grant ${context.packageName} ${Manifest.permission.RECORD_AUDIO}").use {
            java.io.FileInputStream(it.fileDescriptor).readBytes()
        }
        val audio = TemporarySpeechAudio()
        try {
            assertFalse(audio.available)
            assertFalse(audio.isPlaying)
            audio.start()
            ui.waitUntil(timeoutMillis = 5000) { audio.capturedBytes >= 6400 }
            val first = audio.finish()
            assertNotNull("A real PCM buffer is required before replay", first)
            assertTrue(audio.available)
            assertFalse("Finishing must never autoplay", audio.isPlaying)
            var error = false
            audio.play { error = true }
            assertFalse(error)
            assertTrue(audio.isPlaying)
            audio.start() // A new attempt discards the old recording and stops replay.
            assertFalse(audio.available)
            assertFalse(audio.isPlaying)
            assertTrue("Replaced PCM must be zeroed", first!!.all { it == 0.toByte() })
            ui.waitUntil(timeoutMillis = 5000) { audio.capturedBytes >= 6400 }
            audio.finish()
            assertTrue(audio.available)
            audio.clear() // Same cleanup used by navigation, exit and background.
            assertFalse(audio.available)
            audio.play { fail("Missing recording must not start a player") }
            assertFalse(audio.isPlaying)
        } finally { audio.clear() }
    }
}
