package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.asAndroidBitmap
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
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class LanguageAreaFlowTest {
    private lateinit var learner: Learner
    @get:Rule(order = 0) val clean: TestRule = object : TestRule {
        override fun apply(base: Statement, description: Description) = object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("speakivra_learners", 0).edit().clear().commit()
                context.getSharedPreferences("speakivra_progress", 0).edit().clear().commit()
                val store = LearnerStore(context)
                learner = store.add("Nghia", 0)
                val zh = ChineseCurriculum.topics.first()
                val en = Lessons.a1.first()
                store.preferences(learner).edit().putStringSet(CHECK_IN_DATES, setOf(LocalDate.now().toString()))
                    .putString("active_lesson", en.id).putString("zh.active_lesson", zh.id)
                    .putString("lesson.${en.id}.learned_phrases", en.phrases.take(3).joinToString("|") { it.id })
                    .putString("lesson.${en.id}.learned_turns", "").putInt("lesson.${en.id}.phrase", 3)
                    .putString("lesson.${zh.id}.learned_phrases", zh.phrases.take(3).joinToString("|") { it.id }).commit()
                base.evaluate()
            }
        }
    }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        ui.waitForIdle()
        val bitmap = captureNativeDisplay()
        val path = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        path.parentFile!!.mkdirs()
        path.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun actualStartupSeparatesAreasAndKeepsSharedProfileAttendanceAndBothResumes() {
        ui.selectEnglishAreaIfNeeded()
        shot("170-language-entry-approved")
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Tiếp tục học", substring = false).performScrollTo().performClick()
        ui.onNodeWithContentDescription("Tiếng Anh").assertIsDisplayed()
        ui.onNodeWithText("Chào Nghia!").assertIsDisplayed()
        ui.onNodeWithText("Câu 4/20").assertIsDisplayed()
        shot("171-english-area-home-approved")
        val prefs = LearnerStore(ui.activity).preferences(learner)
        val enBefore = prefs.getString("lesson.greetings.learned_phrases", "")
        val days = prefs.getStringSet(CHECK_IN_DATES, emptySet())!!.toSet()
        ui.onNodeWithTag("switch-study-language").performClick()
        ui.onNodeWithTag("language-zh").performClick()
        ui.onNodeWithContentDescription("Tiếng Trung").assertIsDisplayed()
        ui.onNodeWithText("Chào Nghia!").assertIsDisplayed()
        ui.onNodeWithText("Câu 4/20").assertIsDisplayed()
        ui.onNodeWithText("认识新朋友").assertIsDisplayed()
        shot("172-chinese-area-home-approved")
        ui.onNodeWithText("Tiếp tục học", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Chào hỏi và giới thiệu • 4/20").assertIsDisplayed()
        ui.onNodeWithTag("zh-next").assertIsNotEnabled()
        assertEquals(enBefore, prefs.getString("lesson.greetings.learned_phrases", ""))
        assertEquals(days, prefs.getStringSet(CHECK_IN_DATES, emptySet()))
        assertEquals(learner.id, LearnerStore(ui.activity).all().single().id)
    }
    @Test fun bookAndMicSwitchContentAndSelection() {
        val prefs = LearnerStore(ui.activity).preferences(learner)
        ui.runOnUiThread { ui.activity.setContent { SpeakivraTheme(false) {
            ChineseLearningArea(prefs, learner, "home", {}, {})
        } } }
        val tab = SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role,
            androidx.compose.ui.semantics.Role.Tab)
        val book = ui.onNode(tab and hasText("Bài học"))
        val mic = ui.onNode(tab and hasText("Luyện nói"))
        repeat(3) {
            book.performClick().assertIsSelected()
            ui.onNodeWithTag("zh-level-A1").assertExists()
            ui.onNodeWithTag("zh-practice").assertDoesNotExist()
            mic.performClick().assertIsSelected()
            ui.onNodeWithTag("zh-practice").assertIsDisplayed()
        }
    }
    @Test fun completeChineseCatalogueHasAllLevelsHotelRolesReadingsAndExercises() {
        val prefs = LearnerStore(ui.activity).preferences(learner)
        ui.runOnUiThread { ui.activity.setContent { SpeakivraTheme(false) {
            Surface(color = studio.background, contentColor = studio.ink) { ChineseCourseScreen(prefs, learner, {}) }
        } } }
        ui.onNodeWithTag("zh-level-B1").performScrollTo().assertIsDisplayed()
        ui.onNodeWithTag("zh-level-C2").performScrollTo().assertIsDisplayed()
        shot("173-chinese-full-level-catalogue")
        ui.onNodeWithTag("zh-catalogue-hotel").performScrollTo().performClick()
        ui.onNodeWithText("Tiếng Trung Khách sạn", substring = false).assertIsDisplayed()
        ui.onNodeWithTag("zh-hotel-FO-repeat").performScrollTo().assertIsDisplayed()
        ui.onNodeWithTag("zh-hotel-HK-chat").performScrollTo().assertIsDisplayed()
        shot("174-chinese-hotel-catalogue")
        ui.onNodeWithTag("zh-hotel-HK-exercise").performScrollTo().performClick()
        ui.onNodeWithText("20 chủ đề • 500 câu nói • 120 lượt hội thoại").assertIsDisplayed()
        ui.onNodeWithTag("zh_hotel_hk_entry").performScrollTo().performClick()
        ui.onNodeWithTag("zh-next").assertIsDisplayed().assertIsNotEnabled()
        shot("175-chinese-housekeeping-translation")
        repeat(3) { ui.onNodeWithContentDescription("Quay lại").performScrollTo().performClick() }
        ui.onNodeWithTag("zh-catalogue-readings").performScrollTo().performClick()
        val readingTopic = ChineseCurriculum.topics.first { it.level == "A1" && it.id != "zh_a1_greetings" && it.readingPacks.isNotEmpty() }
        ui.onNodeWithTag(readingTopic.readingPacks.first().id).performScrollTo().performClick()
        ui.waitForIdle()
        assertEquals(readingTopic.id, prefs.getString("zh.active_lesson", null))
        shot("176-chinese-reading-catalogue-resume")
    }
    @Test fun expandedReadingResumeSkipsFinishedStarterAndDoesNotRelockEarnedLevels() {
        val prefs = ui.activity.getSharedPreferences("zh-migration-full", 0)
        prefs.edit().clear().commit()
        val topic = ChineseCurriculum.topics.first()
        topic.phrases.take(8).forEach { prefs.edit().putInt("achievement.${topic.id}.${it.id}", 100).commit() }
        val state = ChineseProgress(prefs)
        assertEquals(3, state.diamonds(topic))
        val edit = prefs.edit()
        edit.putString("lesson.${topic.id}.learned_phrases", topic.phrases.joinToString("|") { it.id })
        edit.putString("lesson.${topic.id}.learned_turns", topic.turns.joinToString("|") { it.prompt.id })
        edit.putStringSet("extra.completed_ids", topic.legacyReading.map { it.id }.toSet()).commit()
        val next = state.resume(topic)
        assertEquals("read", next.mode)
        assertEquals(topic.readingPacks.first().id, next.packId)
        assertEquals(0, next.index)
        prefs.edit().putStringSet("extra.completed_ids", (topic.legacyReading.map { it.id } + topic.comprehension.take(2).map { it.id }).toSet()).commit()
        assertTrue(state.completeComprehension(topic, 2, topic.comprehension[2].answer))
        assertEquals(1, prefs.getInt("lesson.${topic.id}.rounds", 0))
        assertTrue(topic.readingPacks.flatMap { it.lines }.any { it.id !in prefs.getStringSet("extra.completed_ids", emptySet()).orEmpty() })
        ChineseCurriculum.topics.filter { it.level == "B1" }.forEach { edit.putInt("lesson.${it.id}.rounds", 1) }
        edit.commit()
        assertTrue(state.levelOpen("B2", ChineseCurriculum.topics))
        assertFalse(state.levelOpen("C1", ChineseCurriculum.topics))
        val firstB1 = ChineseCurriculum.topics.first { it.level == "B1" }
        prefs.edit().putString("lesson.${firstB1.id}.learned_phrases", "").commit()
        assertTrue(state.levelOpen("B2", ChineseCurriculum.topics))
    }
}
