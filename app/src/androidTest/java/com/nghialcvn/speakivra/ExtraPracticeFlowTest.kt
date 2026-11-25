package com.nghialcvn.speakivra

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.io.File

/** Test provider exists only in the instrumentation APK. Production routes are exercised unchanged. */
@RunWith(AndroidJUnit4::class)
class ExtraPracticeFlowTest {
    @get:Rule(order = 0) val seed = TestRule { base, _: Description -> object : Statement() {
        override fun evaluate() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
            context.getSharedPreferences("speakivra_learners", 0).edit().clear().putString("profiles",
                encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
            val first = ExtraPracticeData.exercises.first { it.lessonId == "food" }
            context.getSharedPreferences("speakivra_progress", 0).edit().clear()
                .putBoolean("neon_theme", true).putBoolean("automatic_mode", false)
                .putInt("lesson.routine.rounds", 1).putString("lesson.routine.phase", "summary")
                .putInt("speed_calibration", 2).putInt("playback_speed", 100)
                .putStringSet("extra.completed_ids", first.questions.take(3).map { it.id }.toSet()).commit()
            base.evaluate()
        }
    } }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private fun shell(cmd: String) = instrumentation.uiAutomation.executeShellCommand(cmd).use {
        java.io.FileInputStream(it.fileDescriptor).bufferedReader().readText().trim()
    }
    private fun fixture(method: String, argument: String? = null) = requireNotNull(
        instrumentation.targetContext.contentResolver.call(android.net.Uri.parse(
            "content://com.nghialcvn.speakivra.test.recognition-fixture"), method, argument, null))
    private fun provider(test: () -> Unit) {
        val previous = shell("settings get secure voice_recognition_service")
        shell("pm grant ${instrumentation.targetContext.packageName} ${Manifest.permission.RECORD_AUDIO}")
        shell("pm grant ${instrumentation.context.packageName} ${Manifest.permission.RECORD_AUDIO}")
        shell("settings put secure voice_recognition_service ${instrumentation.context.packageName}/com.nghialcvn.speakivra.RecognitionProtocolService")
        try { fixture("configure", "false"); test() } finally {
            if (previous == "null" || previous.isBlank()) shell("settings delete secure voice_recognition_service")
            else shell("settings put secure voice_recognition_service $previous")
        }
    }
    private fun answer(text: String) {
        fixture("answer", text)
        ui.onNodeWithContentDescription("Nói và nhận dạng").performScrollTo().performClick()
        ui.waitUntil(15000) { ui.onAllNodesWithText(text, substring = false).fetchSemanticsNodes().isNotEmpty() &&
            ui.onAllNodesWithContentDescription("Nói và nhận dạng").fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
        Assert.assertTrue(fixture("stats").getInt("liveBytes") >= 16000)
    }
    private fun shot(name: String) {
        ui.waitForIdle(); android.os.SystemClock.sleep(500)
        dismissUnrelatedLauncherAnr(instrumentation)
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
    @Test fun paragraphUsesRealRecognitionAndClearsReplayAcrossSentenceWholeModeAndExit() = provider {
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Luyện nói theo đoạn văn").performScrollTo().performClick()
        ui.onNodeWithText("A2", substring = false).performClick()
        ui.onNodeWithText("My daily routine").performClick()
        ui.onNodeWithContentDescription("Phiên âm IPA").assertExists()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        val item = ExtraPracticeData.passages.first { it.level == "A2" }
        answer(item.sentences[0].english)
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsEnabled().performClick()
        ui.onNodeWithText("Câu tiếp theo").performClick()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        ui.onNodeWithText("Tôi chuẩn bị bữa sáng.").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Từng câu").performScrollTo()
        shot("40-neon-paragraph-ipa")
        answer(item.sentences[1].english)
        ui.onNodeWithContentDescription("Đạt đối chiếu lời nói").performScrollTo().assertIsDisplayed()
        shot("41-neon-paragraph-recognized")
        ui.onNodeWithText("Cả đoạn", substring = false).performScrollTo().performClick()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        answer(item.english)
        ui.onNodeWithText("Hoàn thành đoạn văn").assertIsEnabled().performClick()
        val context = instrumentation.targetContext
        val saved = context.getSharedPreferences("speakivra_progress", 0).getStringSet("extra.completed_ids", emptySet()).orEmpty()
        Assert.assertTrue(item.id in saved && item.sentences.all { it.id in saved })
        ui.onNodeWithText("My daily routine").performClick()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        Assert.assertEquals("Nghĩa", LearnerStore(context).all().single().name)
        Assert.assertEquals(1, context.getSharedPreferences("speakivra_progress", 0).getInt("lesson.routine.rounds", 0))
    }
    @Test fun exerciseAcceptsEquivalentAnswerButHintAndListeningNeverCompleteAQuestion() = provider {
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Tiếng Anh nói thế nào?").performScrollTo().performClick()
        ui.onNodeWithText("Ăn uống", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        ui.onNodeWithText("Gợi ý").performClick()
        ui.onNodeWithText("Nghe đáp án").performClick()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        answer("Could I not have the bill please")
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        answer("Could I have the bill, please?")
        ui.onNodeWithText("Cách diễn đạt phù hợp").performScrollTo().assertIsDisplayed()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsEnabled().performClick()
        ui.onNodeWithText("Tính tiền giúp mình.").performScrollTo()
        shot("42-neon-speaking-exercise")
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        answer("Can I pay by card?")
        ui.onNodeWithText("Câu tiếp theo").performClick()
        answer("That's all, thank you.")
        ui.onNodeWithText("Câu tiếp theo").performClick()
        val first = ExtraPracticeData.exercises.first { it.lessonId == "food" }
        val saved = instrumentation.targetContext.getSharedPreferences("speakivra_progress", 0)
            .getStringSet("extra.completed_ids", emptySet()).orEmpty()
        Assert.assertTrue(first.questions.all { it.id in saved })
        ui.activityRule.scenario.recreate()
        Assert.assertEquals("Nghĩa", LearnerStore(instrumentation.targetContext).all().single().name)
        Assert.assertTrue(instrumentation.targetContext.getSharedPreferences("speakivra_progress", 0)
            .getStringSet("extra.completed_ids", emptySet()).orEmpty().contains(first.questions.last().id))
    }
}
