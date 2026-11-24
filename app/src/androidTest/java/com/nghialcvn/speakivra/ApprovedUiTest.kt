package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
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
        // UI assertions run before this call. Capture the real emulator window,
        // including system bars, via UiAutomation rather than a second Compose
        // PixelCopy redraw. PixelCopy can time out on CI even when the view exists.
        ui.onRoot().assertExists()
        instrumentation.waitForIdleSync()
        // System UI uses a separate surface from Compose. Wait for its window events too.
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        var bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
            "Android Emulator did not produce a real screenshot: $name"
        }
        check(bitmap.width > 100 && bitmap.height > 100) {
            "Android Emulator produced an invalid screenshot: $name"
        }
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
            instrumentation.waitForIdleSync()
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
                "Android Emulator lost its screenshot surface: $name"
            }
        }
        val clockReadable = readableClock(bitmap)
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "ui-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        check(clockReadable) { "System clock contrast does not match the chosen theme: $name" }
    }
    @Test fun approvedScreensAndThemeAreInteractiveAndKeepLessonPosition() {
        ui.onNodeWithText("Ai học hôm nay?").assertIsDisplayed()
        ui.onNodeWithContentDescription("Logo Speakivra").assertIsDisplayed()
        shot("09-choose-learner")
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("Câu 3/20").assertIsDisplayed()
        shot("01-home")
        // New A2 catalogue is a real navigable level, not a placeholder.
        ui.onNodeWithText("A2 • Sơ trung cấp").performScrollTo().performClick()
        ui.onNodeWithText("Chọn bài học A2").assertIsDisplayed()
        ui.onNodeWithText("Trải nghiệm").assertIsDisplayed()
        shot("17-a2-lessons")
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.onNodeWithText("15 chủ đề A1").performScrollTo().performClick()
        ui.onNodeWithText("Chọn bài học A1").assertIsDisplayed()
        shot("02-lessons")
        ui.onNodeWithText("Giao tiếp thường ngày").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Chào hỏi").performScrollTo().performClick()
        ui.onNodeWithText("I'm Nghĩa.").assertIsDisplayed()
        shot("03-speaking")
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        ui.onNodeWithText("Máy nhận dạng được:").assertDoesNotExist()
        ui.onNodeWithText("Thủ công").performClick()
        ui.onNodeWithText("Thủ công").assertIsSelected()
        ui.onNodeWithContentDescription("Tốc độ đọc: 1x").performClick()
        ui.onNodeWithText("1.25x").performClick()
        ui.onNodeWithContentDescription("Tốc độ đọc: 1.25x").assertIsDisplayed()
        ui.onNodeWithContentDescription("Tốc độ đọc: 1.25x").performClick()
        ui.onNodeWithText("1x", substring = false).performClick()
        ui.onNodeWithContentDescription("Tốc độ đọc: 1x").assertIsDisplayed()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Hội thoại mẫu").performClick()
        ui.onNodeWithText("Mô phỏng theo kịch bản").assertIsDisplayed()
        ui.onNodeWithText("I'm Nghĩa.").assertIsDisplayed()
        ui.onNodeWithText("Tôi là Nghĩa.").assertIsDisplayed()
        shot("04-conversation")
        repeat(6) { ui.onNodeWithText("Câu hội thoại tiếp theo").performScrollTo().performClick() }
        ui.onNodeWithText("Sang ôn tập").performScrollTo().performClick()
        ui.onNodeWithText("Xin chào! Bạn khỏe không?").performClick()
        ui.onNodeWithText("Đúng rồi!").assertIsDisplayed()
        shot("05-review")
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithText("Cá nhân & cài đặt").assertIsDisplayed()
        shot("06-settings")
        // Approved neon voice screen: language, voice label, preserved profile settings.
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").performScrollTo().performClick()
        ui.onNodeWithText("🇬🇧 Anh-Anh (UK)").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Anh – Việt").assertIsDisplayed()
        ui.onNodeWithText("Anh – Anh").assertIsDisplayed()
        shot("15-neon-voice-settings")
        ui.onNodeWithText("🇬🇧 Anh-Anh (UK)").performClick()
        ui.onNodeWithText("Giọng nam", substring = false).performScrollTo().performClick()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Anh-Anh (UK) · Giọng nam").performScrollTo().assertIsDisplayed()
        // Restore original voice baseline so the rest of the existing UI test is unchanged.
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").performScrollTo().performClick()
        ui.onNodeWithText("🇺🇸 Anh-Mỹ (US)").performScrollTo().performClick()
        ui.onNodeWithText("Giọng nữ", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Khôi phục giọng nữ neural gốc").performScrollTo().performClick()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.waitForIdle()
        // Diagnostic only: capture the actual screen and semantics after returning
        // from the voice picker. Do not silently skip the required settings UI.
        val debugDir = File(InstrumentationRegistry.getInstrumentation()
            .targetContext.getExternalFilesDir(null), "ui-screenshots").apply { mkdirs() }
        File(debugDir, "after-voice-return-semantics.txt").writeText(ui.onRoot().printToString())
        shot("18-after-voice-return")
        // Explicitly reselect the settings tab before theme checks.
        // This also verifies the new five-tab bottom navigation after returning
        // from the full-screen voice picker and resets the profile scroll.
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithText("Cá nhân & cài đặt").assertIsDisplayed()
        ui.onNodeWithText("Tối", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Tối", substring = false).assertIsSelected()
        ui.runOnIdle {
            val activity = ui.activity
            org.junit.Assert.assertFalse(androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars)
        }
        shot("07-dark-settings")
        ui.onNodeWithText("Sáng", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Sáng", substring = false).assertIsSelected()
        // Neon is an opt-in third appearance, keeping the original light/dark options.
        ui.onNodeWithText("Neon", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").assertIsDisplayed()
        ui.onNodeWithText("Ngôn ngữ học").assertIsDisplayed()
        ui.onNodeWithText("Tùy chỉnh bài học").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Thông báo nhắc học").performScrollTo().assertIsDisplayed()
        shot("16-neon-settings")
        ui.onNodeWithText("Sáng", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Kiểm tra cập nhật").performScrollTo().performClick()
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
        ui.onNode(hasSetTextAction()).performImeAction()
        ui.onNodeWithContentDescription("Avatar 2").assertIsSelected()
        shot("10-create-learner")
        ui.onNodeWithText("Lưu và bắt đầu").performClick()
        ui.onNodeWithText("Chào Linh!").assertIsDisplayed()
        ui.onNodeWithText("Câu 1/20").assertIsDisplayed()
        val store = LearnerStore(ui.activity)
        val newLearner = store.all().single { it.name == "Linh" }
        org.junit.Assert.assertNotEquals("speakivra_progress", learnerPreferenceName(newLearner.id))
        org.junit.Assert.assertEquals(2, preferences.getInt("lesson.greetings.phrase", -1))
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithText("Đổi người học").performScrollTo().performClick()
        ui.onNodeWithText("Nghĩa", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("Câu 3/20").assertIsDisplayed()
        org.junit.Assert.assertEquals("review", preferences.getString("lesson.greetings.phase", ""))
        // Edit the original profile in place: name and avatar change, ID/progress do not.
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithContentDescription("Chỉnh sửa hồ sơ").performClick()
        ui.onNode(hasSetTextAction()).assertTextContains("Nghĩa")
        shot("13-edit-profile")
        ui.onNode(hasSetTextAction()).performTextReplacement("Nghĩa An")
        ui.onNode(hasSetTextAction()).performImeAction()
        ui.onNodeWithContentDescription("Avatar 3").performClick()
        ui.onNodeWithText("Lưu thay đổi").performClick()
        ui.onNodeWithText("Nghĩa An", substring = false).assertIsDisplayed()
        shot("14-renamed-profile")
        val edited = store.all().first()
        org.junit.Assert.assertEquals(ORIGINAL_LEARNER, edited.id)
        org.junit.Assert.assertEquals(2, edited.avatar)
        org.junit.Assert.assertEquals(2, preferences.getInt("lesson.greetings.phrase", -1))
        org.junit.Assert.assertEquals("review", preferences.getString("lesson.greetings.phase", ""))
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.onNodeWithText("Chào Nghĩa An!").assertIsDisplayed()
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("Chào Nghĩa An!").assertIsDisplayed()
        org.junit.Assert.assertEquals(2, preferences.getInt("lesson.greetings.phrase", -1))
        // Recognition fixtures are confined to this test. The APK never supplies fake transcripts.
        val result = mutableStateOf<String?>("I’m Nghĩa.")
        var nextCalls = 0
        ui.runOnUiThread {
            ui.activity.setContent {
                SpeakivraTheme(false) {
                    androidx.compose.material3.Surface(color = studio.background, modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        PracticeScreen(personalizeLesson(Lessons.all.first(), "Nghĩa"), LessonProgress(phrase = 2), false, true,
                            100, result.value.orEmpty(), "", "idle", matchWords("I'm Nghĩa.", result.value), Learner(ORIGINAL_LEARNER, "Nghĩa", 0),
                            onBack = {}, onMode = {}, onSpeed = {}, onTranslation = {}, onListen = {}, onSpeak = {}, onNext = { nextCalls++ })
                    }
                }
            }
        }
        ui.onNodeWithText("Chưa khớp 0/2 từ · 0%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
        org.junit.Assert.assertEquals(1, nextCalls)
        ui.onNodeWithText("I’m Nghĩa.").performScrollTo().assertIsDisplayed()
        shot("11-speaking-pass")
        ui.runOnIdle { result.value = "hello there" }
        ui.onNodeWithText("Chưa khớp 2/2 từ · 100%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        ui.onNodeWithText("Máy nhận dạng được:").assertDoesNotExist()
        ui.onNodeWithText("hello there").performScrollTo().assertIsDisplayed()
        shot("12-speaking-retry")
        ui.runOnIdle { result.value = null }
        ui.onNodeWithText("hello there").assertDoesNotExist()
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        org.junit.Assert.assertEquals(1, nextCalls)

    }
}
