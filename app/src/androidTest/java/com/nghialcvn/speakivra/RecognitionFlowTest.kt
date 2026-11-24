package com.nghialcvn.speakivra

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RecognitionFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private var sawPartial = false
    private fun fixture(method: String, argument: String? = null): android.os.Bundle = requireNotNull(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver.call(
            android.net.Uri.parse("content://com.nghialcvn.speakivra.test.recognition-fixture"), method, argument, null))
    private fun shell(command: String): String = InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(command).use { java.io.FileInputStream(it.fileDescriptor).bufferedReader().readText().trim() }

    @Test fun liveRecognitionDrivesTranscriptWordColorsReplayAndNextGate() = withProvider(false) {
        val capture = installPractice()
        try {
            ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
            ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
            fixture("answer", "Hello there.")
            ui.runOnUiThread { capture.start() }
            ui.waitUntil(15000) { capture.phase == "idle" }
            assertTrue("Provider must consume live PCM while the mic is running", fixture("stats").getInt("liveBytes") >= 16000)
            assertEquals(0, fixture("stats").getInt("directStarts"))
            assertTrue("Partial words must arrive before the final result", sawPartial)
            ui.onAllNodesWithText("Hello there.").assertCountEquals(2)
            ui.onNodeWithText("Câu tiếp theo").assertIsEnabled()
            assertColors(expectedGreen = 2, expectedRed = 0)
            ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsEnabled().performClick()
            screenshot("20-neon-recognition-pass")
            // A wrong answer must also be visible, color each unmatched word and lock Next.
            fixture("answer", "Good morning.")
            ui.runOnUiThread { capture.start() }
            ui.waitUntil(15000) { capture.phase == "idle" }
            ui.onNodeWithText("Good morning.").assertIsDisplayed()
            ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
            assertColors(expectedGreen = 0, expectedRed = 2)
            screenshot("21-neon-recognition-retry")
            fixture("answer", "Hello friend.")
            ui.runOnUiThread { capture.start() }
            ui.waitUntil(15000) { capture.phase == "idle" }
            assertColors(expectedGreen = 1, expectedRed = 1)
            ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
            ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
            ui.onNodeWithText("Chưa có lời nói nhận dạng").assertIsDisplayed()
            ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        } finally { ui.runOnUiThread { capture.cancel() } }
    }
    @Test fun liveMicrophoneStopsOnSilenceWithoutASecondTap() = withProvider(false) {
        fixture("waitForEnd")
        fixture("answer", "Hello there.")
        val capture = installPractice()
        try {
            ui.runOnUiThread { capture.start() }
            ui.waitUntil(20000) { capture.phase == "idle" }
            assertTrue("Silence endpoint must preserve the current real PCM buffer", capture.hasRecording)
            ui.onAllNodesWithText("Hello there.").assertCountEquals(2)
            ui.onNodeWithText("Câu tiếp theo").assertIsEnabled()
        } finally { ui.runOnUiThread { capture.cancel() } }
    }
    @Test fun providerIgnoringExternalAudioFallsBackToTheOriginalMicrophoneRecognition() = withProvider(true) {
        val capture = installPractice()
        try {
            fixture("answer", "Hello there.")
            ui.runOnUiThread { capture.start() }
            ui.waitUntil(15000) { capture.phase == "idle" }
            assertEquals(1, fixture("stats").getInt("directStarts"))
            assertEquals("Unsupported input must not start a competing AudioRecord", 0, fixture("stats").getInt("liveBytes"))
            ui.onAllNodesWithText("Hello there.").assertCountEquals(2)
            ui.onNodeWithText("Câu tiếp theo").assertIsEnabled()
            assertColors(expectedGreen = 2, expectedRed = 0)
            ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
            screenshot("22-neon-recognition-provider-fallback")
        } finally { ui.runOnUiThread { capture.cancel() } }
    }
    private fun withProvider(ignore: Boolean, test: () -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val old = shell("settings get secure voice_recognition_service")
        val component = "${instrumentation.context.packageName}/com.nghialcvn.speakivra.RecognitionProtocolService"
        shell("pm grant ${instrumentation.targetContext.packageName} ${Manifest.permission.RECORD_AUDIO}")
        shell("settings put secure voice_recognition_service $component")
        shell("pm grant ${instrumentation.context.packageName} ${Manifest.permission.RECORD_AUDIO}")
        fixture("configure", ignore.toString())
        try { test() } finally {
            if (old.isBlank() || old == "null") shell("settings delete secure voice_recognition_service")
            else shell("settings put secure voice_recognition_service $old")
        }
    }
    private fun installPractice(): SpeechCapture {
        var phase by mutableStateOf("idle")
        var final by mutableStateOf<String?>(null)
        var live by mutableStateOf("")
        var message by mutableStateOf("")
        var recording by mutableStateOf(false)
        val capture = SpeechCapture(ui.activity, { recording = it }) { state, words, status ->
            phase = state
            if (state != "idle" && words == "Hello") sawPartial = true
            if (words != null) { live = words; if (state == "idle") final = words.takeIf(String::isNotBlank) }
            if (state == "listening" && words == "") final = null
            message = status
        }
        val lesson = Lessons.all.first().copy(phrases = listOf(Lessons.all.first().phrases.first().copy(english = "Hello there.")))
        ui.runOnUiThread {
            ui.activity.setContent {
                SpeakivraTheme(false, true) {
                    CompositionLocalProvider(LocalSpeechReplay provides SpeechReplay(recording && phase == "idle", capture::replay)) {
                        Surface(color = studio.background, modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                            PracticeScreen(lesson.copy(phrases = lesson.phrases + lesson.phrases), LessonProgress(), false, true,
                                100, if (phase == "idle") final.orEmpty() else live, message, phase,
                                matchWords("Hello there.", final), Learner("test", "Nghĩa", 0),
                                onBack = {}, onMode = {}, onSpeed = {}, onTranslation = {}, onListen = {},
                                onSpeak = { capture.start() }, onNext = { capture.cancel(); final = null; live = "" })
                        }
                    }
                }
            }
        }
        return capture
    }
    private fun assertColors(expectedGreen: Int, expectedRed: Int) {
        val annotated = ui.onAllNodesWithText("Hello there.", substring = false, useUnmergedTree = true)
            .fetchSemanticsNodes().flatMap { it.config.getOrElse(SemanticsProperties.Text) { emptyList() } }
            .first { it.spanStyles.size == 2 }
        assertEquals(expectedGreen, annotated.spanStyles.count { it.item.color == NeonStudio.green })
        assertEquals(expectedRed, annotated.spanStyles.count { it.item.color == NeonStudio.coral })
    }
    private fun screenshot(name: String) {
        ui.waitForIdle()
        android.os.SystemClock.sleep(500)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val target = File(instrumentation.targetContext.getExternalFilesDir(null), "ui-screenshots/$name.png")
        target.parentFile!!.mkdirs()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
}
