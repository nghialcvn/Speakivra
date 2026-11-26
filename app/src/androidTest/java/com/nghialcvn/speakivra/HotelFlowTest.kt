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
class HotelFlowTest {
    @get:Rule(order = 0) val seed = TestRule { base, description: Description -> object : Statement() {
        override fun evaluate() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
            context.getSharedPreferences("speakivra_learners", 0).edit().clear().putString("profiles",
                encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
            context.getSharedPreferences("speakivra_progress", 0).edit().clear()
                .putBoolean("neon_theme", description.methodName != "approvedHubTabsOpenBothModesAndRepeatResetsMode")
                .putBoolean("dark_theme", false).putBoolean("automatic_mode", false)
                .putInt("speed_calibration", 2).putInt("playback_speed", 75)
                .putString("active_lesson", "greetings").commit()
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
    private fun hotel() {
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.openLessonHub()
        ui.onNodeWithText("Tiếng Anh Khách sạn").performScrollTo().performClick()
        ui.onNodeWithText("Giao tiếp với khách quốc tế").assertIsDisplayed()
    }
    @Test fun approvedHubTabsOpenBothModesAndRepeatResetsMode() {
        hotel()
        ui.onNodeWithTag("hotel-role-FO").performScrollTo().assertIsSelected()
        listOf("FO", "HK").forEach { role ->
            ui.onNodeWithTag("hotel-role-$role").performScrollTo().performClick().assertIsSelected()
            shot("64-hotel-hub-$role")
            listOf("Hội thoại phân vai", "Tiếng Anh nói thế nào?").forEach { mode ->
                ui.onNodeWithText(mode, substring = false).performScrollTo().performClick()
                ui.onNodeWithText("${HotelCurriculum.roleEnglish(role)} • 15 chủ đề").assertIsDisplayed()
                ui.onNodeWithText(HotelCurriculum.lessons(role).first().vietnamese, substring = false)
                    .performScrollTo().performClick()
                if (mode == "Hội thoại phân vai") ui.onNodeWithText("Hội thoại Khách sạn").assertIsDisplayed()
                else ui.onNodeWithText("Câu 1/8", substring = true).assertExists()
                ui.onNodeWithContentDescription("Quay lại").performClick()
                ui.onNodeWithContentDescription("Quay lại").performClick()
                ui.onNodeWithTag("hotel-role-$role").performScrollTo().assertIsSelected()
            }
            ui.onNodeWithTag("hotel-repeat-$role").performScrollTo().performClick()
            ui.onNodeWithText(HotelCurriculum.lessons(role).first().vietnamese, substring = false)
                .performScrollTo().performClick()
            ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
            ui.onNodeWithContentDescription("Quay lại").performClick()
            ui.onNodeWithContentDescription("Quay lại").performClick()
        }
    }
    @Test fun allFifteenTopicsInBothRolesOpenWithoutCefrPrerequisites() {
        hotel()
        shot("60-hotel-role-picker")
        listOf("FO", "HK").forEach { role ->
            ui.onNodeWithTag("hotel-repeat-$role").performScrollTo().performClick()
            ui.onNodeWithText("${HotelCurriculum.roleEnglish(role)} • 15 chủ đề").assertIsDisplayed()
            shot(if (role == "FO") "61-hotel-front-office" else "62-hotel-housekeeping")
            ui.onNodeWithText(HotelCurriculum.lessons(role).last().vietnamese, substring = false).performScrollTo().performClick()
            ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
            ui.onNodeWithContentDescription("Quay lại").performClick()
            ui.onNodeWithText("${HotelCurriculum.roleEnglish(role)} • 15 chủ đề").assertIsDisplayed()
            ui.onNodeWithContentDescription("Quay lại").performClick()
        }
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("A1 • Cơ bản").performScrollTo().performClick()
        ui.onNodeWithText("Chọn bài học A1").assertIsDisplayed()
        val prefs = instrumentation.targetContext.getSharedPreferences("speakivra_progress", 0)
        Assert.assertEquals(0, prefs.getInt("lesson.food.rounds", 0))
    }
    @Test fun staffConversationMatchesDemoAndResumesWithTemporaryReplay() = provider {
        hotel()
        ui.onNodeWithTag("hotel-role-HK").performScrollTo().performClick()
        ui.onNodeWithText("Hội thoại phân vai").performScrollTo().performClick()
        ui.onNodeWithText("Bổ sung đồ dùng", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Hội thoại Khách sạn").assertIsDisplayed()
        ui.onNodeWithText("Bạn là nhân viên buồng phòng").assertIsDisplayed()
        ui.onNodeWithText("Lượt 1/6").assertIsDisplayed()
        ui.onNodeWithText("0.75x", substring = false).assertIsDisplayed()
        ui.onNodeWithContentDescription("Phiên âm IPA").assertExists()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        answer("Of course. I'll bring them to your room.")
        ui.onNodeWithText("Lượt tiếp theo").assertIsEnabled()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").performScrollTo().assertIsEnabled()
        shot("63-hotel-staff-conversation")
        ui.onNodeWithText("Lượt tiếp theo").performClick()
        ui.onNodeWithText("Lượt 2/6").assertIsDisplayed()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Bổ sung đồ dùng", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Lượt 2/6").assertIsDisplayed()
        val prefs = instrumentation.targetContext.getSharedPreferences("speakivra_progress", 0)
        Assert.assertTrue(prefs.getString("lesson.hotel_hk_amenities.learned_turns", "").orEmpty().contains("hotel_hk_amenities_t1"))
        Assert.assertEquals("", prefs.getString("lesson.hotel_fo_welcome.learned_turns", ""))
    }
    @Test fun translatedStaffAnswerPreservesProgressWhenSwitchingToPhrasePractice() = provider {
        hotel()
        ui.onNodeWithTag("hotel-role-FO").performScrollTo().performClick()
        ui.onNodeWithText("Tiếng Anh nói thế nào?", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Thẻ phòng không hoạt động", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        answer("I am sorry about that. Let me help you with your key card.")
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        val prefs = instrumentation.targetContext.getSharedPreferences("speakivra_progress", 0)
        Assert.assertTrue(prefs.getStringSet("extra.completed_ids", emptySet()).orEmpty().contains("exercise_hotel_fo_key_issue_p1"))
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithTag("hotel-repeat-FO").performScrollTo().performClick()
        ui.onNodeWithText("Thẻ phòng không hoạt động", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
        ui.onNodeWithText("For security, may I verify your identification first?", substring = false).assertExists()
        Assert.assertEquals(0, prefs.getInt("lesson.food.rounds", 0))
        Assert.assertEquals("Nghĩa", LearnerStore(instrumentation.targetContext).all().single().name)
    }
}
