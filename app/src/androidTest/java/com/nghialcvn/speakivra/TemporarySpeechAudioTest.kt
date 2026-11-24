package com.nghialcvn.speakivra

import android.Manifest
import android.os.SystemClock
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
            SystemClock.sleep(350)
            val first = audio.finish()
            assertNotNull(first)
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
            SystemClock.sleep(350)
            audio.finish()
            assertTrue(audio.available)
            audio.clear() // Same cleanup used by navigation, exit and background.
            assertFalse(audio.available)
            audio.play { fail("Missing recording must not start a player") }
            assertFalse(audio.isPlaying)
        } finally { audio.clear() }
    }
}
