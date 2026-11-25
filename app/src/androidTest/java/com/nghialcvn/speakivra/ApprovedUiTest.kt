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
                context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
                context.getSharedPreferences("speakivra_learners", 0).edit().clear()
                    .putString("profiles", encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
                context.getSharedPreferences("speakivra_progress", 0).edit()
                    .putString("active_lesson", "greetings").putBoolean("dark_theme", false)
                    .putBoolean("neon_theme", description.methodName in setOf("neonHomeAndPracticeUseApprovedReplayRow", "homeLessonEntryAndSecondTabOpenSameDedicatedHub", "approvedHeartRewardsResumeAndUpdateNotification"))
                    .putInt("daily_goal", 20).putInt("playback_speed", 100).putInt("speed_calibration", 2)
                    .putString("lesson.greetings.phase", "repeat").putInt("lesson.greetings.phrase", 2)
                    .putInt("lesson.greetings.turn", 0).putInt("lesson.greetings.review", 0)
                    .putInt("lesson.greetings.correct", 0).putInt("lesson.greetings.rounds", 0).commit()
                if (description.methodName == "approvedHeartRewardsResumeAndUpdateNotification") {
                    val prefs = context.getSharedPreferences("speakivra_progress", 0)
                    prefs.edit().apply { prefs.all.keys.filter { it.startsWith("achievement.") }.forEach { remove(it) } }.commit()
                    val rewards = TopicAchievements(prefs)
                    Lessons.a1.take(3).forEachIndexed { index, lesson ->
                        lesson.phrases.forEach { phrase ->
                            rewards.record(lesson, phrase.id, WordMatch((1..100).map { "word$it" },
                                (1..100).map { it <= listOf(95, 75, 60)[index] }, true))
                        }
                    }
                }
                base.evaluate()
            }
        }
    }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @Test fun approvedHeartRewardsResumeAndUpdateNotification() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("speakivra_progress", 0)
        val rewards = TopicAchievements(prefs)
        Lessons.a1.take(3).forEachIndexed { index, lesson ->
            lesson.phrases.forEach { phrase ->
                // Explicit score fixture for threshold UI only; production uses final ASR.
                val tokens = (1..100).map { "word$it" }
                rewards.record(lesson, phrase.id, WordMatch(tokens, (1..100).map { it <= listOf(95,75,60)[index] }, true))
            }
        }
        ui.onNodeWithText("Thành tích đã đạt: 6 ❤️").assertIsDisplayed()
        shot("50-profile-heart-achievements")
        ui.onNodeWithText("Tiếp tục bài học ›").performClick()
        ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.openLessonHub()
        ui.onNodeWithText("A1 • Cơ bản").performClick()
        ui.onNodeWithText("Thành tích đã đạt: 6 ❤️").assertIsDisplayed()
        ui.onNodeWithContentDescription("Chào hỏi: 3 trên 3 tim").assertIsDisplayed()
        ui.onNodeWithContentDescription("Gia đình: 2 trên 3 tim").assertIsDisplayed()
        ui.onNodeWithContentDescription("Sinh hoạt hằng ngày: 1 trên 3 tim").assertIsDisplayed()
        shot("51-topic-heart-achievements")
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Hội thoại mẫu").performClick()
        shot("52-conversation-clear-logo")
        ui.onNodeWithText("🇺🇸 English (US)").assertIsDisplayed()
        // Actual Android notification, with an explicit future-release fixture.
        val current = currentInstallation(context)
        context.getSharedPreferences("speakivra_update_notifications", 0).edit().remove("notified_version").commit()
        val release = AppRelease("test", current.versionCode + 1,
            "- Phần thưởng tim\n- Sửa tiếp tục bài học\n- Logo trong suốt",
            "https://github.com/nghialcvn/Speakivra/releases/download/vtest/test.apk",
            current.packageName, current.certificate, 1, "0".repeat(64))
        UpdateNotifications.deliver(context, release, current)
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        val notification = manager.activeNotifications.single { it.id == UpdateNotifications.ID }.notification
        org.junit.Assert.assertTrue(notification.extras.getCharSequence(android.app.Notification.EXTRA_TITLE).toString().contains("Có bản cập nhật mới"))
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("cmd statusbar expand-notifications").close()
        android.os.SystemClock.sleep(700)
        shot("53-native-update-notification-fixture")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("cmd statusbar collapse").close()
        notification.contentIntent.send()
        ui.waitForIdle()
        ui.onNodeWithText("Cập nhật ứng dụng").assertIsDisplayed()
        manager.cancel(UpdateNotifications.ID)
        // A retry with a worse result must not erase earned hearts.
        val lesson = Lessons.a1.first()
        rewards.record(lesson, lesson.phrases.first().id, matchWords(lesson.phrases.first().english, "unrelated"))
        org.junit.Assert.assertEquals(3, rewards.hearts(lesson))
        val other = context.getSharedPreferences("achievement-isolation-test", 0)
        other.edit().clear().commit()
        org.junit.Assert.assertEquals(0, TopicAchievements(other).total())
        // A saved UK setting migrates to US without touching progress or old voice data.
        prefs.edit().putString("speech_accent", "GB").putString("speech_voice_GB_female", "old-british-voice").commit()
        org.junit.Assert.assertEquals("US", VoiceChoices.read(prefs).accent)
        org.junit.Assert.assertEquals("old-british-voice", prefs.getString("speech_voice_GB_female", null))
        org.junit.Assert.assertEquals(3, rewards.hearts(lesson))
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // UI assertions run before this call. Capture the real emulator window,
        // including system bars, via UiAutomation rather than a second Compose
        // PixelCopy redraw. PixelCopy can time out on CI even when the view exists.
        ui.onRoot().assertExists()
        instrumentation.waitForIdleSync()
        // Semantics can be ready one display frame before SurfaceFlinger receives it.
        // Wait for two real frames, then allow the GPU submission to finish before
        // capturing. Without this, the speaking screenshot can still be the home page.
        val frames = java.util.concurrent.CountDownLatch(1)
        ui.runOnUiThread {
            ui.activity.window.decorView.invalidate()
            android.view.Choreographer.getInstance().postFrameCallback {
                android.view.Choreographer.getInstance().postFrameCallback { frames.countDown() }
            }
        }
        check(frames.await(5, java.util.concurrent.TimeUnit.SECONDS)) { "Display did not render: $name" }
        android.os.SystemClock.sleep(350)
        // System UI uses a separate surface from Compose. Wait for its window events too.
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        dismissUnrelatedLauncherAnr(instrumentation)
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
        if (name == "19-neon-speaking") {
            val home = File(directory, "18-neon-home.png")
            check(!home.readBytes().contentEquals(File(directory, "$name.png").readBytes())) {
                "Speaking capture is still the previous home frame"
            }
        }
        check(clockReadable) { "System clock contrast does not match the chosen theme: $name" }
    }
    @Test fun neonHomeAndPracticeUseApprovedReplayRow() {
        shot("26-neon-choose-learner")
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        shot("18-neon-home")
        ui.onNodeWithText("Tiếp tục học").performClick()
        ui.onNodeWithText("Luyện nghe & nói").assertIsDisplayed()
        ui.onNodeWithText("English (US)", substring = true).assertDoesNotExist()
        ui.onNodeWithText("Giọng nữ", substring = false).assertDoesNotExist()
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertIsNotEnabled()
        ui.onNodeWithText("Chưa có lời nói nhận dạng").assertIsDisplayed()
        shot("19-neon-speaking")
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
    }
    @Test fun homeLessonEntryAndSecondTabOpenSameDedicatedHub() {
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Học tập", substring = false).assertDoesNotExist()
        ui.onNodeWithText("A2 • Sơ trung cấp").assertDoesNotExist()
        ui.onNodeWithText("Luyện nói theo đoạn văn").assertDoesNotExist()
        ui.onNodeWithContentDescription("Mở mục Bài học").performScrollTo().assertIsDisplayed()
        shot("45-home-lesson-link")
        ui.onNodeWithContentDescription("Mở mục Bài học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertDoesNotExist()
        ui.onNodeWithContentDescription("Mở mục Bài học").assertDoesNotExist()
        ui.onNodeWithText("Chọn cấp độ hoặc dạng luyện tập").assertIsDisplayed()
        val lessonTab = hasText("Bài học", substring = false) and SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Tab)
        ui.onNode(lessonTab).assertIsSelected()
        listOf("A1 • Cơ bản", "A2 • Sơ trung cấp", "B1 • Trung cấp", "Hội thoại mẫu",
            "Luyện nói theo đoạn văn", "Tiếng Anh nói thế nào?").forEach { ui.onNodeWithText(it).assertExists() }
        shot("46-lesson-hub")
        listOf("A1" to "A1 • Cơ bản", "A2" to "A2 • Sơ trung cấp", "B1" to "B1 • Trung cấp").forEach { (level, title) ->
            ui.onNodeWithText(title).performScrollTo().performClick()
            ui.onNodeWithText("Chọn bài học $level").assertIsDisplayed()
            ui.onNodeWithText("Chọn cấp độ hoặc dạng luyện tập").assertDoesNotExist()
            ui.onNode(lessonTab).assertIsSelected()
            shot("47-$level-lesson-detail")
            ui.onNodeWithContentDescription("Quay lại").performClick()
            ui.onNodeWithText("Chọn cấp độ hoặc dạng luyện tập").assertIsDisplayed()
        }
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("A2 • Sơ trung cấp").assertDoesNotExist()
        ui.openLessonHub()
        ui.onNode(lessonTab).assertIsSelected()
        ui.runOnUiThread { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
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
        ui.openLessonHub()
        ui.onNodeWithText("A2 • Sơ trung cấp").performScrollTo().performClick()
        ui.onNodeWithText("Chọn bài học A2").assertIsDisplayed()
        ui.onNodeWithText("Trải nghiệm").assertIsDisplayed()
        ui.onNodeWithText("15 chủ đề A2 • 450 câu nói • 150 lượt hội thoại").assertIsDisplayed()
        shot("17-a2-lessons")
        ui.onNodeWithText("Kể chuyện & tình huống").performScrollTo().assertIsDisplayed()
        shot("29-a2-new-topics")
        ui.onNodeWithText("Kể chuyện & tình huống").assertIsNotEnabled()
        ui.onNodeWithText("Trải nghiệm").performScrollTo().performClick()
        ui.onNodeWithText("I have visited Da Nang twice.").assertIsDisplayed()
        ui.onNodeWithText("Trải nghiệm • Câu 1/30").assertIsDisplayed()
        shot("28-a2-new-lesson")
        ui.onNodeWithText("Trang chủ", substring = false).performClick()
        ui.openLessonHub()
        ui.onNodeWithText("A1 • Cơ bản").performScrollTo().performClick()
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
        ui.openLessonHub()
        ui.onNodeWithText("Hội thoại mẫu").performClick()
        ui.onNodeWithText("Mô phỏng theo kịch bản").assertIsDisplayed()
        ui.onNodeWithText("I'm Nghĩa.").assertIsDisplayed()
        ui.onNodeWithText("Tôi là Nghĩa.").assertIsDisplayed()
        shot("04-conversation")
        repeat(6) { ui.onNodeWithText("Câu hội thoại tiếp theo").performClick() }
        ui.onNodeWithText("Sang ôn tập").performClick()
        ui.onNodeWithText("Xin chào! Bạn khỏe không?").performClick()
        ui.onNodeWithText("Đúng rồi!").assertIsDisplayed()
        shot("05-review")
        ui.onNodeWithContentDescription("Quay lại").performClick()
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithText("Học tập").assertIsDisplayed()
        ui.onNodeWithText("Tùy chỉnh").assertIsDisplayed()
        ui.onNodeWithText("Hồ sơ").assertIsDisplayed()
        ui.onNodeWithText("Ứng dụng").assertIsDisplayed()
        // All four groups are collapsed at first launch.
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").assertDoesNotExist()
        shot("06-settings")
        // Only one accordion expands at a time.
        ui.onNodeWithText("Học tập").performClick()
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").performScrollTo().performClick()
        ui.onNodeWithText("🇬🇧 Anh-Anh (UK)").assertDoesNotExist()
        ui.onNodeWithText("Anh – Việt").assertIsDisplayed()
        ui.onNodeWithText("Anh – Anh").assertIsDisplayed()
        shot("15-neon-voice-settings")
        ui.onNodeWithText("Giọng nam", substring = false).performScrollTo().performClick()
        ui.onNodeWithContentDescription("Quay lại").performScrollTo().performClick()
        ui.onNodeWithText("Học tập").performScrollTo().performClick()
        ui.onNodeWithText("Anh-Mỹ (US) • Giọng nam").performScrollTo().assertIsDisplayed()
        // Restore original voice baseline so the rest of the existing UI test is unchanged.
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").performScrollTo().performClick()
        ui.onNodeWithText("🇺🇸 Anh-Mỹ (US)").performScrollTo().performClick()
        ui.onNodeWithText("Giọng nữ", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Khôi phục giọng nữ neural gốc").performScrollTo().performClick()
        ui.onNodeWithContentDescription("Quay lại").performScrollTo().performClick()
        ui.onNodeWithText("Học tập").assertIsDisplayed()
        ui.onNodeWithText("Tùy chỉnh").performScrollTo().performClick()
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").assertDoesNotExist()
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
        ui.onNodeWithText("Học tập").performScrollTo().performClick()
        ui.onNodeWithText("Giọng đọc & Ngôn ngữ").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Ngôn ngữ học").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Tùy chỉnh bài học").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Thông báo nhắc học").performScrollTo().assertIsDisplayed()
        shot("16-neon-settings")
        ui.onNodeWithText("Tùy chỉnh").performScrollTo().performClick()
        ui.onNodeWithText("Sáng", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Ứng dụng").performScrollTo().performClick()
        ui.onNodeWithText("Cập nhật thu gọn").assertDoesNotExist()
        ui.onNodeWithText("✨ Cập nhật mới", substring = false).performScrollTo().assertIsDisplayed()
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
        ui.onNodeWithText("Hồ sơ").performScrollTo().performClick()
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
        ui.onNodeWithText("Hồ sơ").performScrollTo().performClick()
        ui.onNodeWithText("Đổi người học").performScrollTo().performClick()
        ui.onNodeWithText("Nghĩa", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("Câu 3/20").assertIsDisplayed()
        org.junit.Assert.assertEquals("review", preferences.getString("lesson.greetings.phase", ""))
        // Edit the original profile in place: name and avatar change, ID/progress do not.
        ui.onNodeWithText("Cài đặt", substring = false).performClick()
        ui.onNodeWithText("Hồ sơ").performScrollTo().performClick()
        ui.onNodeWithText("Sửa hồ sơ").performScrollTo().performClick()
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
        ui.onNodeWithText("Khớp 2/2 từ · 100%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled().performClick()
        org.junit.Assert.assertEquals(1, nextCalls)
        ui.onNodeWithText("I’m Nghĩa.").performScrollTo().assertIsDisplayed()
        shot("11-speaking-pass")
        ui.runOnIdle { result.value = "hello there" }
        ui.onNodeWithText("Khớp 0/2 từ · 0%").performScrollTo().assertIsDisplayed()
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
