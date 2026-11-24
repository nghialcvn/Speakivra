package com.nghialcvn.speakivra

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File

/** CI-only real app + real configured recognizer + real AudioRecord. The host
 * injects the bundled spoken clip into the emulator mic after our ready signal.
 * No fixture recognition service, supplied transcript or production test hook. */
class RealMicrophoneUiProbeTest {
    private fun shell(command: String) = InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(command).use { java.io.FileInputStream(it.fileDescriptor).bufferedReader().readText().trim() }
    private fun output(name: String) = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), name)
    @get:Rule(order = 0) val seed = TestRule { base: Statement, _: Description -> object : Statement() {
        override fun evaluate() {
            check(shell("getprop ro.kernel.qemu") == "1") { "Disposable CI emulator only; never clear a physical learner profile" }
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences("speakivra_learners", 0).edit().clear().commit()
            context.getSharedPreferences("speakivra_progress", 0).edit().clear().putBoolean("neon_theme", true).commit()
            output("mic-probe-ready.txt").delete()
            output("mic-probe-result.json").delete()
            shell("pm grant ${context.packageName} ${Manifest.permission.RECORD_AUDIO}")
            base.evaluate()
        }
    } }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @Test fun realMicWordsEnableNextAndTemporaryReplayClearsOnNext() {
        val report = JSONObject().put("passed", false)
        var stage = "Create learner"
        try {
            ui.onNodeWithText("+ Tạo hồ sơ mới").performClick()
            ui.onNode(hasSetTextAction()).performTextInput("Probe")
            ui.onNode(hasSetTextAction()).performImeAction()
            ui.onNodeWithText("Lưu và bắt đầu").performClick()
            ui.onNodeWithText("Chào Probe!").assertIsDisplayed()
            ui.onNodeWithText("Tiếp tục học").performClick()
            ui.onNodeWithText("Hello! How are you?").assertIsDisplayed()
            ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
            stage = "Recognize real microphone input"
            ui.onNodeWithContentDescription("Nói và nhận dạng").performClick()
            ui.waitUntil(timeoutMillis = 15000) {
                ui.onAllNodes(hasText("Đang nghe…", substring = true)).fetchSemanticsNodes().isNotEmpty()
            }
            output("mic-probe-ready.txt").writeText("READY")
            ui.waitUntil(timeoutMillis = 30000) {
                ui.onAllNodes(hasText("Câu tiếp theo") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            }
            ui.onNodeWithContentDescription("Đạt đối chiếu lời nói").assertIsDisplayed()
            val text = ui.onAllNodes(hasText("", substring = true), useUnmergedTree = true).fetchSemanticsNodes()
                .flatMap { it.config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.Text) { emptyList() } }.map { it.text }.distinct()
            report.put("visible_text", JSONArray(text))
            val replayAvailable = ui.onAllNodes(hasContentDescription("Nghe lại giọng vừa ghi") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            report.put("replay_available", replayAvailable)
            shot("23-real-microphone-result")
            stage = "Replay and next sentence cleanup"
            if (replayAvailable) ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").performClick()
            ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
            ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
            ui.onNodeWithText("Chưa có lời nói nhận dạng").assertIsDisplayed()
            ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
            report.put("cleared_after_next", true).put("passed", true)
            shot("24-real-microphone-next")
        } catch (error: Throwable) {
            report.put("error", error.toString())
            runCatching { shot("23-real-microphone-failed") }
            throw error
        } finally {
            report.put("stage", stage)
            output("mic-probe-result.json").writeText(report.toString(2))
            println("REAL_MICROPHONE_RESULT=$report")
        }
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        android.os.SystemClock.sleep(400)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = output("ui-screenshots/$name.png"); file.parentFile!!.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
