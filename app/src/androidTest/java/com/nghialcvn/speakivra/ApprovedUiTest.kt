package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File

/** Runs only on the disposable CI emulator. Capture real Compose screens, not a second mockup. */
@RunWith(AndroidJUnit4::class)
class ApprovedUiTest {
    @get:Rule(order = 0) val seed: TestRule = object : TestRule {
        override fun apply(base: Statement, description: Description) = object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("speakivra_learners", 0).edit().clear().commit()
                context.getSharedPreferences("speakivra_progress", 0).edit()
                    .putString("active_lesson", "greetings").putBoolean("dark_theme", false)
                    .putInt("daily_goal", 20).putInt("playback_speed", 100).putInt("speed_calibration", 2)
                    .putString("lesson.greetings.phase", "repeat").putInt("lesson.greetings.phrase", 2)
                    .putInt("lesson.greetings.turn", 0).putInt("lesson.greetings.review", 0)
                    .putInt("lesson.greetings.correct", 0).putInt("lesson.greetings.rounds", 0).commit()
                base.evaluate()
            }
        }
    }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // Semantics can settle before the compositor submits the new screen. PixelCopy
        // waits for a real Compose frame before the full-window screenshot is taken.
        val rendered = ui.onRoot().captureToImage()
        check(rendered.width > 0 && rendered.height > 0)
        instrumentation.waitForIdleSync()
        // System UI uses a separate surface from Compose. Wait for its window events too.
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        var bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        fun readableClock(image: Bitmap): Boolean {
            if (name != "01-home" && name != "07-dark-settings") return true
            val expectLight = name == "07-dark-settings"
            var matching = 0
            for (y in 10 until minOf(70, image.height)) for (x in 20 until image.width / 5) {
                val pixel = image.getPixel(x, y)
                val luma = (android.graphics.Color.red(pixel) + android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)) / 3
                if (if (expectLight) luma > 200 else luma < 128) matching++
            }
            return matching > 30
        }
        var attempts = 0
        while (!readableClock(bitmap) && attempts++ < 4) {
            bitmap.recycle()
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        }
        check(readableClock(bitmap)) { "System clock contrast does not match the chosen theme: $name" }
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "ui-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun approvedScreensAndThemeAreInteractiveAndKeepLessonPosition() {
        ui.onNodeWithText("Ai học hôm nay?").assertIsDisplayed()
        shot("09-choose-learner")
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("Câu 3/8").assertIsDisplayed()
        shot("01-home")
        ui.onNodeWithText("10 chủ đề A1").performClick()
        ui.onNodeWithText("Chọn bài học").assertIsDisplayed()
        shot("02-lessons")
        ui.onNodeWithText("Giao tiếp thường ngày").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Chào hỏi").performScrollTo().performClick()
        ui.onNodeWithText("My name is Alex.").assertIsDisplayed()
        shot("03-speaking")
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        ui.onNodeWithText("Máy nhận dạng được:").assertDoesNotExist()
        ui.onNodeWithText("Thủ công").performClick()
        ui.onNodeWithText("Thủ công").assertIsSelected()
        ui.onNodeWithText("1.25x").performClick()
        ui.onNodeWithText("1.25x").assertIsSelected()
        ui.onNodeWithText("1x", substring = false).performClick()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Hội thoại mẫu").performClick()
        ui.onNodeWithText("Mô phỏng theo kịch bản").assertIsDisplayed()
        shot("04-conversation")
        repeat(2) { ui.onNodeWithText("Câu hội thoại tiếp theo").performScrollTo().performClick() }
        ui.onNodeWithText("Sang ôn tập").performScrollTo().performClick()
        ui.onNodeWithText("Xin chào! Bạn khỏe không?").performClick()
        ui.onNodeWithText("Đúng rồi!").assertIsDisplayed()
        shot("05-review")
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Cập nhật", substring = false).performClick()
        ui.onNodeWithText("Cá nhân & cài đặt").assertIsDisplayed()
        shot("06-settings")
        ui.onNodeWithText("Tối", substring = false).performClick()
        ui.onNodeWithText("Tối", substring = false).assertIsSelected()
        ui.runOnIdle {
            val activity = ui.activity
            org.junit.Assert.assertFalse(androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars)
        }
        shot("07-dark-settings")
        ui.onNodeWithText("Sáng", substring = false).performClick()
        ui.onNodeWithText("Sáng", substring = false).assertIsSelected()
        ui.onNodeWithText("Kiểm tra cập nhật").performClick()
        ui.onNodeWithText("Phiên bản đang dùng: v${BuildConfig.VERSION_NAME}").assertIsDisplayed()
        shot("08-updates")
        // Neither theme changes nor navigation resets the saved phrase/review position.
        val preferences = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("speakivra_progress", 0)
        org.junit.Assert.assertEquals(2, preferences.getInt("lesson.greetings.phrase", -1))
        org.junit.Assert.assertEquals("review", preferences.getString("lesson.greetings.phase", ""))
        org.junit.Assert.assertFalse(preferences.getBoolean("dark_theme", true))
        // Add a real local learner, then verify both profiles remain isolated when switching.
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Đổi người học").performScrollTo().performClick()
        ui.onNodeWithText("Thêm người học").performScrollTo().performClick()
        ui.onNode(hasSetTextAction()).performTextInput("Linh")
        ui.onNodeWithContentDescription("Avatar 2").assertIsSelected()
        shot("10-create-learner")
        ui.onNodeWithText("Lưu và bắt đầu").performClick()
        ui.onNodeWithText("Chào Linh!").assertIsDisplayed()
        ui.onNodeWithText("Câu 1/8").assertIsDisplayed()
        val store = LearnerStore(ui.activity)
        val newLearner = store.all().single { it.name == "Linh" }
        org.junit.Assert.assertNotEquals("speakivra_progress", learnerPreferenceName(newLearner.id))
        org.junit.Assert.assertEquals(2, preferences.getInt("lesson.greetings.phrase", -1))
        ui.onNodeWithText("Cập nhật", substring = false).performClick()
        ui.onNodeWithText("Đổi người học").performScrollTo().performClick()
        ui.onNodeWithText("Nghĩa", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("Câu 3/8").assertIsDisplayed()
        org.junit.Assert.assertEquals("review", preferences.getString("lesson.greetings.phase", ""))
        // Recognition fixtures are confined to this test. The APK never supplies fake transcripts.
        val result = mutableStateOf<String?>("my name is I like")
        var nextCalls = 0
        ui.runOnUiThread {
            ui.activity.setContent {
                SpeakivraTheme(false) {
                    androidx.compose.material3.Surface {
                        PracticeScreen(Lessons.all.first(), LessonProgress(phrase = 2), false, true,
                            100, "", "", "idle", matchWords("My name is Alex.", result.value),
                            onBack = {}, onMode = {}, onSpeed = {}, onTranslation = {}, onListen = {}, onSpeak = {}, onNext = { nextCalls++ })
                    }
                }
            }
        }
        ui.onNodeWithText("Chưa khớp 1/4 từ · 25%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
        org.junit.Assert.assertEquals(1, nextCalls)
        shot("11-speaking-pass")
        ui.runOnIdle { result.value = "my family likes music" }
        ui.onNodeWithText("Chưa khớp 3/4 từ · 75%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        ui.onNodeWithText("Máy nhận dạng được:").assertDoesNotExist()
        shot("12-speaking-retry")
        ui.runOnIdle { result.value = null }
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        org.junit.Assert.assertEquals(1, nextCalls)

    }
}
