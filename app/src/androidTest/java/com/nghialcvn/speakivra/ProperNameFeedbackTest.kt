package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** UI fixtures are actual provider transcripts from the report, not a claimed acoustic score. */
@RunWith(AndroidJUnit4::class)
class ProperNameFeedbackTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val learner = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
    private val lesson get() = personalizeLesson(Lessons.a1.first(), learner.name)
    private fun render(transcript: String?, phase: String = "idle", onNext: () -> Unit = {}) {
        ui.runOnUiThread { ui.activity.setContent { SpeakivraTheme(false) {
            PracticeScreen(lesson, LessonProgress(phrase = 2), false, true, 100,
                transcript.orEmpty(), "", phase, matchWords(lesson.phrases[2].english, transcript), learner,
                onBack = {}, onMode = {}, onSpeed = {}, onTranslation = {}, onListen = {}, onSpeak = {}, onNext = onNext)
        } } }
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        val i = InstrumentationRegistry.getInstrumentation(); dismissUnrelatedLauncherAnr(i)
        i.uiAutomation.waitForIdle(500, 5000)
        val b = requireNotNull(i.uiAutomation.takeScreenshot())
        val f = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        f.parentFile!!.mkdirs(); f.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }; b.recycle()
    }
    @Test fun screenshotSeventyFiveEnablesNextAndLeavesMisheardNameRed() {
        var continued = false
        render("my name is India", onNext = { continued = true })
        val colored = ui.onNodeWithText("My name is Nghĩa.", useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.Text].single()
        assertTrue(colored.spanStyles.any { it.item.color == LightStudio.incorrectWord && colored.text.substring(it.start, it.end).contains("Nghĩa") })
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled()
        shot("120-name-seventy-five-reference")
        ui.onNodeWithText("Khớp 3/4 từ · 75%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("✓ Có thể chuyển câu").assertIsDisplayed()
        ui.onNodeWithText("my name is India", useUnmergedTree = true).assertExists()
        shot("121-name-seventy-five-can-continue")
        ui.onNodeWithText("Câu tiếp theo").performTouchInput { click() }
        ui.runOnIdle { assertTrue(continued) }
    }
    @Test fun incompleteOrLowMatchingSpeechStaysLockedAndAccentlessNameMatches() {
        render(null)
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        render("my family likes music")
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
        render("My name is Nghia")
        ui.onNodeWithText("Khớp 4/4 từ · 100%").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Câu tiếp theo").assertIsEnabled()
        shot("122-name-accents-match")
        render("my name is India", phase = "listening")
        ui.onNodeWithText("Cần nói lại").assertIsNotEnabled()
    }
}
