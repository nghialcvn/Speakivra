package com.nghialcvn.speakivra

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

@RunWith(AndroidJUnit4::class)
class AdvancedCurriculumFlowTest {
    @get:Rule(order = 0) val seed = TestRule { base, description: Description -> object : Statement() {
        override fun evaluate() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
            context.getSharedPreferences("speakivra_learners", 0).edit().clear().putString("profiles",
                encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
            context.getSharedPreferences("speakivra_guest_identity", 0).edit().clear().commit()
            val prefs = context.getSharedPreferences("speakivra_progress", 0)
            val edit = prefs.edit().clear().putBoolean("dark_theme", false).putBoolean("neon_theme", false)
                .putBoolean("automatic_mode", false).putString("active_lesson", "greetings")
            val completed = when (description.methodName) {
                "completedB1OpensB2ButKeepsLaterLevelsLocked" -> listOf("B1")
                "completedPredecessorsOpenC2AndItsExtraPractice" -> listOf("B1", "B2", "C1")
                "sustainedC1ResponseHasVisibleControlsAndIpa" -> listOf("B1", "B2")
                else -> emptyList()
            }
            Lessons.all.filter { Lessons.levelOf(it) in completed }.forEach { lesson ->
                val key = "lesson.${lesson.id}."
                edit.putInt(key + "rounds", 1).putString(key + "phase", "summary")
                    .putString(key + "learned_phrases", lesson.phrases.joinToString("|") { it.id })
                    .putString(key + "learned_turns", lesson.conversation.joinToString("|") { it.id })
            }
            if (description.methodName == "restoredAdvancedSelectionCannotBypassTheGate")
                edit.putString("active_lesson", "c2_viewpoints")
            if (description.methodName == "enabledReminderMigratesToTwentyHundredWithoutLosingTheToggle")
                edit.putBoolean("study_reminder_enabled", true).putInt("study_reminder_hour", 19)
            edit.commit()
            AccountRuntime.testGateway = UnconfiguredAccountGateway()
            try { base.evaluate() } finally {
                AccountRuntime.testGateway = null
                StudyReminders.setEnabled(context, false)
            }
        }
    } }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        dismissUnrelatedLauncherAnr(instrumentation)
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        Thread.sleep(150)
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
    private fun hub() { ui.startLocalStudy(); ui.openLessonHub() }
    @Test fun progressOpensOnlyOneGroupAndPreservesTheOverviewAndHotelLabels() {
        ui.startLocalStudy()
        ui.onNodeWithText("Tiến độ", substring = false).performClick()
        ui.onNodeWithText("0/145 chủ đề đã hoàn thành").assertIsDisplayed()
        ui.onNodeWithText("Chào hỏi", substring = false).assertDoesNotExist()
        shot("100-progress-closed")
        ui.onNodeWithText("A1 • Căn bản").performScrollTo().performClick()
        ui.onNodeWithText("Chào hỏi", substring = false).assertExists()
        ui.onNodeWithText("A2 • Sơ trung cấp").performScrollTo().performClick()
        ui.onNodeWithText("Chào hỏi", substring = false).assertDoesNotExist()
        ui.onNodeWithText("Trải nghiệm", substring = false).assertExists()
        ui.onNodeWithText("B1 • Trung cấp").performScrollTo().performClick()
        ui.onNodeWithText("Trải nghiệm", substring = false).assertDoesNotExist()
        ui.onNodeWithText("Thay đổi và mục tiêu", substring = false).assertExists()
        ui.onNodeWithText("B1 • Trung cấp").performScrollTo()
        shot("101-progress-one-group")
        ui.onNodeWithText("B1 • Trung cấp").performClick()
        ui.onNodeWithText("Thay đổi và mục tiêu", substring = false).assertDoesNotExist()
        ui.onNodeWithText("Khách sạn • Lễ tân").performScrollTo().assertIsDisplayed()
        ui.onAllNodesWithText("Bài học riêng").assertCountEquals(2)
        ui.onNodeWithText("Khách sạn • Buồng phòng").performScrollTo().performClick()
        ui.onNodeWithText(HotelCurriculum.lessons("HK").first().vietnamese, substring = false).assertExists()
        shot("102-progress-hotel-independent")
    }
    @Test fun advancedLevelsAndTheirExtraPracticeAreInitiallyLocked() {
        hub()
        ui.onNodeWithText("B2 • Trung cao cấp").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithText("C1 • Cao cấp").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithText("C2 • Chuyên nghiệp").performScrollTo().assertIsNotEnabled()
        shot("103-advanced-locked")
        ui.onNodeWithText("Luyện nói theo đoạn văn", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("B2", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("10 bài • 100 câu").assertIsDisplayed()
        ui.onNodeWithText(CourseLevels.lockMessage("B2")).assertIsDisplayed()
        ui.onNodeWithText(AdvancedCurriculum.passages.first().title, substring = false).performScrollTo().assertIsNotEnabled()
        shot("104-extra-level-locked")
    }
    @Test fun completedB1OpensB2ButKeepsLaterLevelsLocked() {
        hub()
        ui.onNodeWithText("C1 • Cao cấp").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithText("B2 • Trung cao cấp").performScrollTo().assertIsEnabled().performClick()
        ui.onNodeWithText("20 chủ đề B2 • 700 câu nói • 300 lượt hội thoại").assertIsDisplayed()
        ui.onNodeWithText(AdvancedCurriculum.lessons.first().vietnamese, substring = false).assertIsEnabled()
        ui.onNodeWithText(AdvancedCurriculum.lessons[3].vietnamese, substring = false).performScrollTo().assertIsNotEnabled()
        shot("105-b2-open-after-b1")
    }
    @Test fun completedPredecessorsOpenC2AndItsExtraPractice() {
        hub()
        ui.onNodeWithText("C2 • Chuyên nghiệp").performScrollTo().assertIsEnabled().performClick()
        ui.onNodeWithText("20 chủ đề C2 • 700 câu nói • 300 lượt hội thoại").assertIsDisplayed()
        ui.onNodeWithText(AdvancedCurriculum.lessons.first { it.id.startsWith("c2_") }.vietnamese, substring = false).performClick()
        ui.onNodeWithText(AdvancedCurriculum.lessons.first { it.id.startsWith("c2_") }.phrases.first().english, substring = false).assertExists()
        shot("106-c2-professional-practice")
        ui.openLessonHub()
        ui.onNodeWithText("Tiếng Anh nói thế nào?", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("C2", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("20 bài • 100 câu").assertIsDisplayed()
        ui.onNodeWithText(AdvancedCurriculum.exercises.first { it.level == "C2" }.topic, substring = false).performScrollTo().assertIsEnabled()
    }
    @Test fun sustainedC1ResponseHasVisibleControlsAndIpa() {
        hub()
        ui.onNodeWithText("C1 • Cao cấp").performScrollTo().performClick()
        val lesson = AdvancedCurriculum.lessons.first { it.id.startsWith("c1_") }
        ui.onNodeWithText(lesson.vietnamese, substring = false).performClick()
        ui.onNodeWithText(lesson.phrases.first().english, substring = false).assertExists()
        ui.onNodeWithContentDescription("Phiên âm IPA").assertExists()
        shot("107-c1-sustained-response")
        ui.onNodeWithText("Nghe mẫu", substring = false).performScrollTo().assertIsDisplayed()
        ui.onNodeWithContentDescription("Nói và nhận dạng").performScrollTo().assertIsDisplayed()
        shot("109-c1-practice-controls")
    }
    @Test fun restoredAdvancedSelectionCannotBypassTheGate() {
        ui.startLocalStudy()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("Tiếp tục học", substring = false).performScrollTo().performClick()
        ui.onNodeWithText(Lessons.a1.first().phrases.first().english, substring = false).assertExists()
        ui.onNodeWithText(AdvancedCurriculum.lessons.first { it.id.startsWith("c2_") }.phrases.first().english, substring = false).assertDoesNotExist()
    }
    @Test fun enabledReminderMigratesToTwentyHundredWithoutLosingTheToggle() {
        ui.startLocalStudy()
        val prefs = ui.activity.getSharedPreferences("speakivra_progress", 0)
        ui.waitUntil(10000) { prefs.getInt("study_reminder_hour", 0) == 20 }
        Assert.assertTrue(StudyReminders.isEnabled(ui.activity))
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithText("Học tập", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Đang bật • khoảng 20:00").performScrollTo().assertIsDisplayed()
        shot("108-reminder-twenty-hundred")
    }
}
