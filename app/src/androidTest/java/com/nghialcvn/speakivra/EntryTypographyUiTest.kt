package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class EntryTypographyUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        dismissUnrelatedLauncherAnr(instrumentation)
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun a_approvedSplashPickerAndPopupMatchAllThreeAppearances() {
        var phase by mutableStateOf(-.4f)
        var appearance by mutableStateOf(1)
        var splash by mutableStateOf(true)
        val learner = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
        val store = LearnerStore(ui.activity)
        store.preferences(learner).edit().clear().commit()
        ui.runOnUiThread { ui.activity.setContent {
            SpeakivraTheme(dark = appearance == 1, neon = appearance == 2) {
                val palette = studio
                SideEffect { updateSystemBars(ui.activity, appearance != 0, palette.background) }
                Surface(color = studio.background, contentColor = studio.ink) {
                    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        if (appearance == 2) NeonWaveBackground(Modifier.matchParentSize())
                        CompositionLocalProvider(LocalBrandSweepPhase provides phase) {
                            if (splash) ProfileSplashScreen()
                            else LearnerPicker(store, listOf(learner), learner.id, {}, {})
                        }
                    }
                }
            }
        } }
        val logo = ui.onNodeWithTag("splash-brand-logo")
        val word = ui.onNodeWithTag("entry-brand-wordmark")
        val beforeLogo = logo.captureToImage().asAndroidBitmap()
        val beforeWord = word.captureToImage().asAndroidBitmap()
        ui.runOnIdle { phase = .35f }
        val afterLogo = logo.captureToImage().asAndroidBitmap()
        val afterWord = word.captureToImage().asAndroidBitmap()
        assertFalse(beforeLogo.sameAs(afterLogo))
        assertFalse(beforeWord.sameAs(afterWord))
        assertEquals(beforeWord.getPixel(0, 0), afterWord.getPixel(0, 0))
        listOf(beforeLogo, beforeWord, afterLogo, afterWord).forEach { it.recycle() }
        shot("180-splash-dark-sweep-left")
        ui.runOnIdle { phase = .75f }
        shot("181-splash-dark-sweep-right")
        for ((theme, name) in listOf(0 to "light", 1 to "dark", 2 to "neon")) {
            ui.runOnIdle { appearance = theme; splash = true; phase = .35f }
            ui.onNodeWithText("Every language, a new horizon.").assertIsDisplayed()
            ui.onNodeWithText("Mỗi ngôn ngữ, một chân trời mới.").assertIsDisplayed()
            shot("${190 + theme * 3}-splash-$name")
            if (theme == 0) shot("182-splash-light")
            if (theme == 2) shot("183-splash-neon")
            ui.runOnIdle { splash = false }
            ui.onNodeWithText("Ai học hôm nay?").assertIsDisplayed()
            ui.onNodeWithText("Tiếp tục bài học ›").assertDoesNotExist()
            ui.onNodeWithText("Hồ sơ khách • Chưa liên kết").assertDoesNotExist()
            shot("${191 + theme * 3}-picker-$name")
            ui.onNodeWithContentDescription("Thêm ngôn ngữ học").performClick()
            ui.onNodeWithText("Ngôn ngữ mới sắp ra mắt").assertIsDisplayed()
            shot("${192 + theme * 3}-popup-$name")
            ui.onNodeWithText("Đã hiểu").performClick()
        }
    }
    @Test fun b_compactProfileKeepsIndependentLanguagesAtLargeFontsAndAfterRecreation() {
        var width by mutableStateOf(360)
        var fontScale by mutableStateOf(1f)
        val store = LearnerStore(ui.activity)
        val learner = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
        val prefs = store.preferences(learner)
        prefs.edit().clear().putString("lesson.greetings.learned_phrases", "old-english")
            .putString("lesson.zh_a1_greetings.learned_phrases", "old-chinese").commit()
        ui.runOnUiThread { ui.activity.setContent {
            SpeakivraTheme(true) {
                Surface(color = studio.background, contentColor = studio.ink) {
                    val density = LocalDensity.current.density
                    CompositionLocalProvider(LocalDensity provides Density(density, fontScale), LocalBrandSweepPhase provides .35f) {
                        Box(Modifier.width(width.dp).fillMaxHeight()) { LearnerPicker(store, listOf(learner), learner.id, {}, {}) }
                    }
                }
            }
        } }
        for (w in listOf(284, 360)) for (scale in listOf(1f, 1.5f)) {
            android.util.Log.i("ProfileStartupReview", "width=$w fontScale=$scale")
            ui.runOnIdle { width = w; fontScale = scale }
            val english = ui.onNodeWithTag("language-en")
            val chinese = ui.onNodeWithTag("language-zh")
            english.performScrollTo().assertIsDisplayed()
            chinese.performScrollTo().performClick()
            chinese.assertIsSelected()
            assertEquals(StudyLanguage.CHINESE, savedStudyLanguage(prefs))
            english.performScrollTo().performClick()
            english.assertIsSelected()
            assertEquals(StudyLanguage.ENGLISH, savedStudyLanguage(prefs))
            assertEquals("old-english", prefs.getString("lesson.greetings.learned_phrases", null))
            assertEquals("old-chinese", prefs.getString("lesson.zh_a1_greetings.learned_phrases", null))
        }
        prefs.edit().putString("study_language", "CHINESE").commit()
        ui.onNodeWithTag("language-zh").assertIsSelected()
        prefs.edit().putString("study_language", "ENGLISH").commit()
        ui.onNodeWithTag("language-en").assertIsSelected()
        shot("184-profile-large-font")
        saveStudyLanguage(prefs, StudyLanguage.CHINESE)
        assertEquals(StudyLanguage.CHINESE, savedStudyLanguage(LearnerStore(ui.activity).preferences(learner)))
        val other = LearnerStore(ui.activity).add("Tablet language check", 1)
        assertEquals(StudyLanguage.ENGLISH, savedStudyLanguage(store.preferences(other)))
        assertEquals(StudyLanguage.CHINESE, savedStudyLanguage(prefs))
    }
}
