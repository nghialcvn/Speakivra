package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DailyCheckInFlowTest {
    @get:Rule(order = 0) val seed = TestRule { base, _: Description -> object : Statement() {
        override fun evaluate() {
            val c = InstrumentationRegistry.getInstrumentation().targetContext
            c.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
            c.getSharedPreferences("speakivra_learners", 0).edit().putString("profiles", encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
            c.getSharedPreferences("speakivra_guest_identity", 0).edit().clear().commit()
            c.getSharedPreferences("speakivra_progress", 0).edit().clear().putBoolean("automatic_mode", false)
                .putString("active_lesson", "greetings").putInt("lesson.greetings.phrase", 2).commit()
            AccountRuntime.testGateway = UnconfiguredAccountGateway()
            try { base.evaluate() } finally { AccountRuntime.testGateway = null }
        }
    } }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @org.junit.Before fun enterEnglishArea() { ui.selectEnglishAreaIfNeeded() }
    private fun shot(name: String) {
        ui.waitForIdle()
        val i = InstrumentationRegistry.getInstrumentation(); dismissUnrelatedLauncherAnr(i)
        i.uiAutomation.waitForIdle(500, 5000)
        val bitmap = requireNotNull(i.uiAutomation.takeScreenshot())
        val file = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs(); file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
    @Test fun attendancePersistsOnceAndBelongsToTheSelectedProfile() {
        ui.startLocalStudy()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        shot("110-attendance-before")
        ui.onNodeWithText("Điểm danh").performTouchInput { click() }
        ui.onNodeWithText("✓ Đã điểm danh").assertIsNotEnabled()
        ui.onNodeWithText("1 ngày").assertIsDisplayed()
        val store = LearnerStore(ui.activity); val learner = store.all().first()
        Assert.assertEquals(setOf(LocalDate.now().toString()), store.preferences(learner).getStringSet(CHECK_IN_DATES, emptySet()))
        val other = store.add("Linh", 1)
        Assert.assertTrue(store.preferences(other).getStringSet(CHECK_IN_DATES, emptySet()).orEmpty().isEmpty())
        shot("111-attendance-after")
        ui.onNodeWithContentDescription("Mở mục Bài học").performClick()
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.onNodeWithText("✓ Đã điểm danh").assertIsNotEnabled()
    }
    @Test fun resumeWorksByRealTouchAfterLeavingAndReturningToPicker() {
        ui.onNodeWithText("Tiếp tục bài học ›").performTouchInput { click() }
        ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
        ui.onNodeWithText(personalizeLesson(Lessons.a1.first(), "Nghĩa").phrases[2].english, substring = false).assertExists()
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("Chạm để mở một nhóm cài đặt. Nhóm trước tự thu lại.").assertIsDisplayed()
        ui.onNodeWithText("Hồ sơ", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Đổi người học").performScrollTo().performClick()
        ui.onNodeWithContentDescription("Tiếp tục học với Nghĩa").performTouchInput { click() }
        ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
        ui.onNodeWithText(personalizeLesson(Lessons.a1.first(), "Nghĩa").phrases[2].english, substring = false).assertExists()
        shot("112-profile-resumed")
    }
    @Test fun linkedLabelFitsOneLineAndResumeActionHasATouchTarget() {
        val learner = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
        val identities = GuestIdentityStore(ui.activity); val id = identities.ensure(learner)
        identities.recordServerReceipt(learner, GuestClaimReceipt(id.profileId, "owner-test"), "owner-test")
        var resumed = false
        ui.runOnUiThread { ui.activity.setContent { SpeakivraTheme(false) {
            LearnerPicker(LearnerStore(ui.activity), listOf(learner), learner.id, {}, {}, onResume = { resumed = true })
        } } }
        val text = "Hồ sơ đã liên kết tài khoản"
        ui.waitUntil(10000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        val results = mutableListOf<TextLayoutResult>()
        ui.onNodeWithText(text, useUnmergedTree = true).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(results) }
        shot("113-profile-linked-single-line")
        Assert.assertEquals(1, results.single().lineCount)
        Assert.assertFalse("Label overflow: width=${results.single().didOverflowWidth}, height=${results.single().didOverflowHeight}, size=${results.single().size}, input=${results.single().layoutInput}", results.single().hasVisualOverflow)
        ui.onNodeWithText("Tiếp tục bài học ›").performTouchInput { click() }
        ui.runOnIdle { Assert.assertTrue(resumed) }
    }
}
