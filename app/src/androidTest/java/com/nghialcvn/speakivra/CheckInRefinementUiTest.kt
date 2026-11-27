package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CheckInRefinementUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val learner = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
    private fun shot(name: String) {
        ui.waitForIdle()
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        val file = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs(); file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
    @Composable private fun Frame(dark: Boolean, page: String? = null, content: @Composable () -> Unit) {
        SpeakivraTheme(dark) {
            val p = studio
            SideEffect { updateSystemBars(ui.activity, dark, p.background) }
            Surface(color = p.background, contentColor = p.ink) {
                Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    Box(Modifier.weight(1f)) { content() }
                    if (page != null) StudioBottomNavigation(page) {}
                }
            }
        }
    }
    @Test fun pickerUsesTwoToOneLockupAndShineOnlyChangesLetterPixels() {
        var phase by mutableStateOf(.2f)
        val store = LearnerStore(ui.activity)
        ui.runOnUiThread { ui.activity.setContent { Frame(false) {
            CompositionLocalProvider(LocalBrandSweepPhase provides phase) {
                LearnerPicker(store, listOf(learner), learner.id, {}, {})
            }
        } } }
        val logo = ui.onNodeWithTag("picker-brand-logo").fetchSemanticsNode().boundsInRoot
        val word = ui.onNodeWithTag("picker-brand-wordmark").fetchSemanticsNode().boundsInRoot
        assertEquals(2f, logo.height / word.height, .02f)
        val first = ui.onNodeWithTag("picker-brand-wordmark").captureToImage().asAndroidBitmap()
        val logoFirst = ui.onNodeWithTag("picker-brand-logo").captureToImage().asAndroidBitmap()
        shot("140-picker-shine-left")
        ui.runOnIdle { phase = .75f }
        val second = ui.onNodeWithTag("picker-brand-wordmark").captureToImage().asAndroidBitmap()
        val logoSecond = ui.onNodeWithTag("picker-brand-logo").captureToImage().asAndroidBitmap()
        assertTrue("Light band must move within the wordmark", !first.sameAs(second))
        assertTrue("Logo must stand still", logoFirst.sameAs(logoSecond))
        // The corners are outside the letter silhouettes and must keep the surface colour.
        assertEquals(first.getPixel(0, 0), second.getPixel(0, 0))
        assertEquals(first.getPixel(first.width - 1, first.height - 1), second.getPixel(second.width - 1, second.height - 1))
        first.recycle(); second.recycle(); logoFirst.recycle(); logoSecond.recycle()
        shot("141-picker-shine-right")
    }
    @Test fun grayStripPersistsCheckInAndAvatarHandsOverlapItsTop() {
        val prefs = ui.activity.getSharedPreferences("checkin-refinement-test", 0)
        prefs.edit().clear().putStringSet(CHECK_IN_DATES, (1L..9L).map { LocalDate.now().minusDays(it).toString() }.toSet()).commit()
        var profile = false
        ui.runOnUiThread { ui.activity.setContent { Frame(false, "home") {
            HomeScreen(personalizeLesson(Lessons.a1.first(), learner.name), LessonProgress(phrase = 3), 20, 0,
                learner.name, learner, prefs, {}, {}, { profile = true })
        } } }
        ui.onNodeWithContentDescription("Logo Speakivra").assertDoesNotExist()
        ui.onNodeWithText("9 ngày").assertIsDisplayed()
        val avatar = ui.onNodeWithTag("checkin-gripping-avatar").fetchSemanticsNode().boundsInRoot
        val strip = ui.onNodeWithTag("checkin-gray-strip").fetchSemanticsNode().boundsInRoot
        assertTrue(avatar.top < strip.top && avatar.bottom > strip.top)
        ui.onNodeWithTag("checkin-watermark").assertIsDisplayed()
        shot("142-gray-checkin-before")
        ui.onNodeWithText("Điểm danh", substring = false).performTouchInput { click() }
        ui.onNodeWithText("✓ Đã điểm danh").assertIsNotEnabled()
        ui.onNodeWithText("10 ngày").assertIsDisplayed()
        assertEquals(10, prefs.getStringSet(CHECK_IN_DATES, emptySet())!!.size)
        shot("143-gray-checkin-after")
        ui.onNodeWithContentDescription("Mở hồ sơ Nghĩa").performTouchInput { click() }
        ui.runOnIdle { assertTrue(profile) }
    }
    @Test fun allSixSelectedAvatarsKeepTheirHandsOnTheDarkStrip() {
        val prefs = ui.activity.getSharedPreferences("checkin-refinement-dark-test", 0)
        prefs.edit().clear().putStringSet(CHECK_IN_DATES, setOf(LocalDate.now().toString())).commit()
        var avatar by mutableStateOf(0)
        ui.runOnUiThread { ui.activity.setContent { Frame(true, "home") {
            val person = learner.copy(avatar = avatar)
            HomeScreen(personalizeLesson(Lessons.a1.first(), person.name), LessonProgress(phrase = 3), 20, 0,
                person.name, person, prefs, {}, {}, {})
        } } }
        for (index in 0..5) {
            ui.runOnIdle { avatar = index }
            ui.onNodeWithTag("checkin-gripping-avatar").assertIsDisplayed()
            ui.onNodeWithText("✓ Đã điểm danh").assertIsNotEnabled()
            shot("${144 + index}-gray-checkin-avatar-$index-dark")
        }
    }
}
