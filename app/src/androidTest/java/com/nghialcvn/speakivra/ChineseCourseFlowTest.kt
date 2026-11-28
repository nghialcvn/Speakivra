package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** UI/state tests use explicit fixtures, never claim a real acoustic recognition result. */
@RunWith(AndroidJUnit4::class)
class ChineseCourseFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val learner = Learner(ORIGINAL_LEARNER, "Linh", 0)
    private val prefs get() = ui.activity.getSharedPreferences("chinese-course-ui", 0)
    private fun render(chinese: Boolean = true, dark: Boolean = false, font: Float = 1f) {
        prefs.edit().clear().putString("study_language", if (chinese) "zh" else "en").commit()
        ui.runOnUiThread { ui.activity.setContent {
            SpeakivraTheme(dark) {
                val density = LocalDensity.current.density
                CompositionLocalProvider(LocalDensity provides Density(density, font)) {
                    Surface(color = studio.background, contentColor = studio.ink) {
                        var selected by remember { mutableStateOf<StudyLanguage?>(if (chinese) StudyLanguage.CHINESE else StudyLanguage.ENGLISH) }
                        if (selected == null) StudyLanguageDialog(StudyLanguage.CHINESE, {}, { selected = it })
                        else Column(Modifier.fillMaxSize()) {
                            StudyLanguageBar(selected!!) { selected = null }
                            Box(Modifier.weight(1f)) {
                                if (selected == StudyLanguage.CHINESE) ChineseCourseScreen(prefs, learner, {}, initialLevel = "A1")
                                else LessonHubScreen({}, {}, {}, {}, {})
                            }
                        }
                    }
                }
            }
        } }
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        val path = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        path.parentFile!!.mkdirs()
        path.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun languageSelectionAndBothLevelsUseRealCounts() {
        render(chinese = false)
        ui.onNodeWithText("🇺🇸 ⌄").assertIsDisplayed()
        ui.onNodeWithText("A1 • Cơ bản").assertIsDisplayed()
        ui.onNodeWithTag("switch-study-language").performClick()
        ui.onNodeWithTag("language-zh").performClick()
        ui.onNodeWithText("10 chủ đề • 200 câu nói • 70 lượt hội thoại").assertIsDisplayed()
        shot("160-chinese-a1-light")
        ui.onNodeWithTag("zh-level-A2").performClick()
        ui.onNodeWithText("15 chủ đề • 450 câu nói • 150 lượt hội thoại").assertIsDisplayed()
        shot("161-chinese-a2-light")
        ui.onNodeWithTag("switch-study-language").performClick()
        ui.onNodeWithTag("language-en").performClick()
        ui.onNodeWithText("A1 • Cơ bản").assertIsDisplayed()
    }
    @Test fun typedTranslationCannotCompleteUntilCheckedAndDoesNotTouchEnglish() {
        render()
        val english = "lesson.greetings.learned_phrases"
        prefs.edit().putString(english, "english-sentinel").commit()
        ui.onNodeWithTag("zh_a1_greetings").performScrollTo().performClick()
        ui.onNodeWithTag("zh-mode-exercise").performClick()
        ui.onNodeWithTag("zh-next").assertIsNotEnabled()
        ui.onNodeWithText("Câu trả lời tiếng Trung").performTextInput("再见")
        ui.onNodeWithText("Kiểm tra câu đã nhập").performScrollTo().performClick()
        ui.onNodeWithTag("zh-next").assertIsNotEnabled()
        ui.onNodeWithText("Câu trả lời tiếng Trung").performTextReplacement("你好")
        ui.onNodeWithText("Kiểm tra câu đã nhập").performScrollTo().performClick()
        ui.onNodeWithTag("zh-next").assertIsEnabled()
        shot("162-chinese-typed-translation")
        ui.onNodeWithTag("zh-next").performClick()
        assertTrue(prefs.getStringSet("extra.completed_ids", emptySet())!!.contains("zh_a1_greetings_e1"))
        assertEquals("english-sentinel", prefs.getString(english, ""))
        assertEquals(0, TopicAchievements(prefs).hearts(ChineseCurriculum.topics.first().lesson))
        ui.onNodeWithTag("switch-study-language").performClick()
        ui.onNodeWithTag("language-en").performClick()
        ui.onNodeWithTag("switch-study-language").performClick()
        ui.onNodeWithTag("language-zh").performClick()
        ui.onNodeWithTag("zh_a1_greetings").performScrollTo().performClick()
        ui.onNodeWithTag("zh-mode-exercise").performClick()
        ui.onNodeWithText("Bạn tên là gì?").assertExists()
        ui.onNodeWithTag("zh-next").assertIsNotEnabled()
    }
    @Test fun darkLargeFontConversationAndReadingHavePinyinAndVisibleNext() {
        render(dark = true, font = 1.5f)
        ui.onNodeWithTag("zh_a1_greetings").performScrollTo().performClick()
        ui.onNodeWithTag("zh-mode-chat").performClick()
        ui.onNodeWithText("Nǐ jiào shénme míngzi?").assertExists()
        ui.onNodeWithText("我叫Linh。").performScrollTo().assertIsDisplayed()
        ui.onNodeWithTag("zh-next").assertIsDisplayed().assertIsNotEnabled()
        shot("163-chinese-conversation-dark-large-font")
        ui.onNodeWithContentDescription("Quay lại").performScrollTo().performClick()
        ui.onNodeWithTag("zh-mode-read").performClick()
        ui.onNodeWithText("Luyện từng câu trong đoạn văn bên dưới.").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Wǒ jiào Linh.").assertExists()
        ui.onNodeWithTag("zh-next").assertIsDisplayed().assertIsNotEnabled()
        shot("164-chinese-reading-dark-large-font")
    }
    @Test fun readingComprehensionRequiresTheCorrectAnswerAndKeepsItsOwnProgress() {
        render()
        ui.onNodeWithTag("zh_a1_greetings").performScrollTo().performClick()
        ui.onNodeWithTag("zh-mode-review").performScrollTo().performClick()
        ui.onNodeWithTag("zh-next").assertIsNotEnabled()
        ui.onNodeWithTag("zh-answer-1").performScrollTo().performClick()
        ui.onNodeWithText("Kiểm tra đáp án").performScrollTo().performClick()
        ui.onNodeWithTag("zh-next").assertIsNotEnabled()
        ui.onNodeWithTag("zh-answer-0").performScrollTo().performClick()
        ui.onNodeWithText("Kiểm tra đáp án").performScrollTo().performClick()
        ui.onNodeWithTag("zh-next").assertIsEnabled()
        shot("166-chinese-reading-comprehension")
        ui.onNodeWithTag("zh-next").performClick()
        assertTrue(prefs.getStringSet("extra.completed_ids", emptySet())!!.contains("zh_a1_greetings_c1"))
        assertEquals(0, ChineseProgress(prefs).done(ChineseCurriculum.topics.first(), "repeat"))
    }
    @Test fun chineseMicrophoneRequestsMandarinAndClearsThePreviousResultOnNext() {
        render()
        val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        fun shell(command: String) = instrumentation.uiAutomation.executeShellCommand(command).use {
            java.io.FileInputStream(it.fileDescriptor).bufferedReader().readText().trim()
        }
        fun fixture(method: String, value: String? = null) = requireNotNull(instrumentation.targetContext.contentResolver.call(
            android.net.Uri.parse("content://com.nghialcvn.speakivra.test.recognition-fixture"), method, value, null))
        val previous = shell("settings get secure voice_recognition_service")
        shell("pm grant ${instrumentation.targetContext.packageName} android.permission.RECORD_AUDIO")
        shell("pm grant ${instrumentation.context.packageName} android.permission.RECORD_AUDIO")
        shell("settings put secure voice_recognition_service ${instrumentation.context.packageName}/com.nghialcvn.speakivra.RecognitionProtocolService")
        try {
            // Direct-mic protocol fixture avoids pretending that emulator silence is real Mandarin.
            fixture("configure", "true")
            fixture("answer", "我不叫Linh")
            ui.onNodeWithTag("zh_a1_greetings").performScrollTo().performClick()
            ui.onNodeWithTag("zh-mode-repeat").performClick()
            ui.onNodeWithTag("zh-next").assertIsNotEnabled()
            ui.onNodeWithContentDescription("Nói và nhận dạng").performScrollTo().performClick()
            ui.waitUntil(15000) { ui.onAllNodesWithText("Khác từ phủ định • Cần nói lại để giữ đúng nghĩa").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithTag("zh-next").assertIsNotEnabled()
            fixture("answer", "我叫Linh")
            ui.onNodeWithContentDescription("Nói và nhận dạng").performScrollTo().performClick()
            ui.waitUntil(15000) { ui.onAllNodesWithText("Khớp 3/3 đơn vị • 100%").fetchSemanticsNodes().isNotEmpty() }
            assertEquals("zh-CN", fixture("stats").getString("language"))
            ui.onNodeWithTag("zh-next").assertIsEnabled()
            ui.onNodeWithText("Khớp 3/3 đơn vị • 100%").performScrollTo().assertIsDisplayed()
            shot("165-chinese-recognition-protocol-fixture")
            ui.onNodeWithTag("zh-next").performClick()
            ui.onNodeWithTag("zh-next").assertIsNotEnabled()
            ui.onNodeWithContentDescription("Nghe lại giọng vừa ghi").assertDoesNotExist()
            assertTrue(prefs.getString("lesson.zh_a1_greetings.learned_phrases", "")!!.contains("zh_a1_greetings_p01"))
        } finally {
            if (previous == "null" || previous.isBlank()) shell("settings delete secure voice_recognition_service")
            else shell("settings put secure voice_recognition_service $previous")
        }
    }
    @Test fun allModesPersistAndNewContentResumesWithoutLosingEarnedDiamondsOrOtherProfile() {
        val first = prefs; first.edit().clear().commit()
        val second = ui.activity.getSharedPreferences("chinese-course-other-profile", 0)
        second.edit().clear().commit()
        val topic = ChineseCurriculum.topics.first().personalize("Linh")
        val state = ChineseProgress(first)
        ChineseProgress.modes.forEach { mode ->
            state.itemIds(topic, mode).indices.forEach item@{ n ->
                if (mode == "review") {
                    assertTrue(state.completeComprehension(topic, n, topic.comprehension[n].answer))
                    return@item
                }
                val reference = when (mode) {
                    "chat" -> topic.turns[n].reply(topic.phrases).hanzi
                    "read" -> topic.reading[n].hanzi
                    "exercise" -> topic.exercises[n].hanzi
                    else -> topic.phrases[n].hanzi
                }
                assertTrue(state.complete(topic, mode, n, matchChinese(reference, reference)))
            }
        }
        assertTrue(ChineseProgress(first).finished(topic))
        assertEquals(3, state.diamonds(topic))
        assertEquals(0, ChineseProgress(second).done(topic, "repeat"))
        val expanded = topic.copy(phrases = topic.phrases + ChineseLine("zh_a1_greetings_p09", "早上好。", "Zǎoshang hǎo.", "Chào buổi sáng."))
        assertFalse(state.finished(expanded))
        assertEquals(topic.phrases.size, state.firstPending(expanded, "repeat"))
        assertEquals(1, first.getInt("lesson.${topic.id}.rounds", 0))
        assertEquals(3, state.diamonds(topic))
    }
}
