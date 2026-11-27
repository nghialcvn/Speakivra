package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/** Review the four approved screens with real controls and their current insets. */
@RunWith(AndroidJUnit4::class)
class BrandRefreshUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val learner = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
    private val lesson get() = personalizeLesson(Lessons.a1.first(), learner.name)
    private fun shot(name: String) {
        ui.waitForIdle()
        val i = InstrumentationRegistry.getInstrumentation(); dismissUnrelatedLauncherAnr(i)
        i.uiAutomation.waitForIdle(500, 5000)
        val bitmap = requireNotNull(i.uiAutomation.takeScreenshot())
        val file = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs(); file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
    @Composable private fun Frame(dark: Boolean, page: String, content: @Composable () -> Unit) {
        SpeakivraTheme(dark) {
            val palette = studio
            SideEffect { updateSystemBars(ui.activity, dark, palette.background) }
            val avatars = ImageBitmap.imageResource(R.drawable.learner_avatars)
            CompositionLocalProvider(LocalLearnerArtwork provides avatars) {
                Surface(color = studio.background, contentColor = studio.ink) {
                    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        Box(Modifier.weight(1f)) { content() }
                        StudioBottomNavigation(page) {}
                    }
                }
            }
        }
    }
    private fun home(dark: Boolean) {
        val prefs = ui.activity.getSharedPreferences("brand-ui-${if (dark) "dark" else "light"}", 0)
        val dates = (0L..9L).map { LocalDate.now().minusDays(it).toString() }.toSet()
        prefs.edit().clear().putStringSet(CHECK_IN_DATES, dates).commit()
        var opened = ""
        ui.runOnUiThread { ui.activity.setContent { Frame(dark, "home") {
            HomeScreen(lesson, LessonProgress(phrase = 2), 20, 0, learner.name, learner, prefs,
                onContinue = { opened = "practice" }, onLessons = { opened = "lessons" }, onProfile = { opened = "profile" })
        } } }
        ui.onNodeWithText("Chào Nghĩa!").assertIsDisplayed()
        ui.onNodeWithText("10 ngày").assertIsDisplayed()
        ui.onNodeWithText("✓ Đã điểm danh").assertIsNotEnabled()
        ui.onNodeWithText("Hôm nay, mình cùng luyện nói nhé.").assertIsDisplayed()
        ui.onNodeWithText("Tiếp tục học", substring = false).assertIsDisplayed()
        shot(if (dark) "131-brand-home-dark" else "130-brand-home-light")
        ui.onNodeWithContentDescription("Mở hồ sơ Nghĩa").performTouchInput { click() }
        ui.runOnIdle { assertEquals("profile", opened) }
        ui.onNodeWithText("Tiếp tục học", substring = false).performTouchInput { click() }
        ui.runOnIdle { assertEquals("practice", opened) }
        ui.onNodeWithContentDescription("Mở mục Bài học").performScrollTo().performTouchInput { click() }
        ui.runOnIdle { assertEquals("lessons", opened) }
    }
    private fun practice(dark: Boolean) {
        var next = false
        var replayed = false
        ui.runOnUiThread { ui.activity.setContent { Frame(dark, "repeat") {
            CompositionLocalProvider(LocalSpeechReplay provides SpeechReplay(true) { replayed = true }) {
                PracticeScreen(lesson, LessonProgress(phrase = 2), false, true, 100,
                    "my name is India", "", "idle", matchWords(lesson.phrases[2].english, "my name is India"), learner,
                    onBack = {}, onMode = {}, onSpeed = {}, onTranslation = {}, onListen = {}, onSpeak = {}, onNext = { next = true })
            }
        } } }
        ui.onNodeWithText("Khớp 3/4 từ · 75%").assertIsDisplayed()
        ui.onNodeWithText("✓ Có thể chuyển câu").assertIsDisplayed()
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled()
        ui.onNodeWithText("Mẹo luyện âm").assertDoesNotExist()
        shot(if (dark) "133-brand-practice-dark" else "132-brand-practice-light")
        ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").performTouchInput { click() }
        ui.runOnIdle { assertTrue(replayed) }
        ui.onNodeWithText("Câu tiếp theo").performTouchInput { click() }
        ui.runOnIdle { assertTrue(next) }
    }
    @Test fun lightHomeKeepsAttendanceAndActions() = home(false)
    @Test fun darkHomeKeepsAttendanceAndActions() = home(true)
    @Test fun lightPracticeKeepsReplayAndNext() = practice(false)
    @Test fun darkPracticeKeepsReplayAndNext() = practice(true)
}
