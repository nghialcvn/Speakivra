package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.io.File

@RunWith(AndroidJUnit4::class)
class CurriculumUpgradeTest {
    private val oldId = "11111111-1111-1111-1111-111111111111"
    private val orphanId = "22222222-2222-2222-2222-222222222222"
    @get:Rule(order = 0) val seed: TestRule = object : TestRule {
        override fun apply(base: Statement, description: Description) = object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                val resetting = description.methodName.startsWith("upgradeReset")
                context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().clear()
                    .putBoolean(RESET_108, !resetting).commit()
                context.getSharedPreferences("speakivra_learners", 0).edit().clear().putString("profiles",
                    encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0), Learner(oldId, "Linh", 1)))).commit()
                context.getSharedPreferences("speakivra_progress", 0).edit().clear()
                    .putBoolean("neon_theme", true).putBoolean("dark_theme", false)
                    .putInt("completed", 9).putInt("lesson.greetings.rounds", 2)
                    .putBoolean("automatic_mode", false).putString("active_lesson", "greetings").commit()
                listOf(oldId, orphanId).forEach { id ->
                    context.getSharedPreferences(learnerPreferenceName(id), 0).edit().clear()
                        .putInt("lesson.greetings.rounds", 4).commit()
                }
                if (!resetting) context.getSharedPreferences("speakivra_progress", 0).edit()
                    .remove("completed").remove("lesson.greetings.rounds").commit()
                base.evaluate()
            }
        }
    }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()

    @Test fun upgradeResetRunsOnceAndPreservesNewProfile() {
        ui.onNodeWithText("Chưa có hồ sơ người học").assertIsDisplayed()
        ui.onNodeWithText("Nghĩa", substring = false).assertDoesNotExist()
        ui.onNodeWithText("Linh", substring = false).assertDoesNotExist()
        val context = ui.activity
        val original = context.getSharedPreferences("speakivra_progress", 0)
        assertTrue(original.getBoolean("neon_theme", false))
        assertFalse(original.contains("completed"))
        assertFalse(original.contains("lesson.greetings.rounds"))
        listOf(oldId, orphanId).forEach { id ->
            assertTrue(context.getSharedPreferences(learnerPreferenceName(id), 0).all.isEmpty())
        }
        shot("30-neon-upgrade-requires-new-profile")
        ui.onNodeWithText("+ Tạo hồ sơ mới").performClick()
        ui.onNode(hasSetTextAction()).performTextInput("Bảo")
        ui.onNodeWithText("Lưu và bắt đầu").performClick()
        ui.onNodeWithText("Chào Bảo!").assertIsDisplayed()
        val fresh = LearnerStore(context).all().single()
        LearnerStore(context).preferences(fresh).edit().putInt("lesson.greetings.phrase", 1).commit()
        ui.activityRule.scenario.recreate()
        val reopened = LearnerStore(ui.activity)
        assertEquals(listOf(fresh), reopened.all())
        assertEquals(1, reopened.preferences(fresh).getInt("lesson.greetings.phrase", 0))
        assertTrue(ui.activity.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).getBoolean(RESET_108, false))
    }

    @Test fun b1StartsWithThreeTopicsAndFinishingThirdUnlocksOnlyFourth() {
        ui.startLocalStudy()
        ui.openLessonHub()
        ui.onNodeWithText("B1 • Trung cấp").performScrollTo().performClick()
        ui.onNodeWithText("20 chủ đề B1 • 700 câu nói • 300 lượt hội thoại").assertIsDisplayed()
        Lessons.b1.take(3).forEach { ui.onNodeWithText(it.vietnamese).assertIsEnabled() }
        ui.onNodeWithText(Lessons.b1[3].vietnamese).assertIsNotEnabled()
        shot("31-neon-b1-three-open-topics")
        ui.onNodeWithText(Lessons.b1.first().vietnamese).performClick()
        ui.onNodeWithText("Thay đổi và mục tiêu • Câu 1/35").assertIsDisplayed()
        ui.onNodeWithText(Lessons.b1.first().phrases.first().english).assertIsDisplayed()
        shot("32-neon-b1-practice")
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        val third = Lessons.b1[2]
        ui.activity.getSharedPreferences("speakivra_progress", 0).edit()
            .putInt("lesson.${third.id}.rounds", 1).putString("lesson.${third.id}.phase", "summary")
            .putString("lesson.${third.id}.learned_phrases", third.phrases.joinToString("|") { it.id })
            .putString("lesson.${third.id}.learned_turns", third.conversation.joinToString("|") { it.id }).commit()
        ui.activityRule.scenario.recreate()
        if (ui.onAllNodesWithText("Bắt đầu học").fetchSemanticsNodes().isNotEmpty())
            ui.startLocalStudy()
        ui.openLessonHub()
        ui.onNodeWithText("B1 • Trung cấp").performScrollTo().performClick()
        ui.onNodeWithText(Lessons.b1[3].vietnamese).assertIsEnabled()
        ui.onNodeWithText(Lessons.b1[4].vietnamese).assertIsNotEnabled()
        shot("33-neon-b1-fourth-topic-unlocked")
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.openLessonHub()
        ui.onNodeWithText("A2 • Sơ trung cấp").performScrollTo().performClick()
        ui.onNodeWithText("Giải quyết vấn đề").assertIsNotEnabled()
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        android.os.SystemClock.sleep(500)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        dismissUnrelatedLauncherAnr(instrumentation)
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
