package com.nghialcvn.speakivra

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

/** A fresh install must never have a fictitious "Nghĩa" profile preloaded. */
@RunWith(AndroidJUnit4::class)
class FreshInstallLearnerTest {
    @get:Rule(order = 0) val clean: TestRule = object : TestRule {
        override fun apply(base: Statement, description: Description) =
            object : Statement() {
                override fun evaluate() {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    context.getSharedPreferences("speakivra_learners", 0).edit().clear().commit()
                    context.getSharedPreferences("speakivra_progress", 0).edit().clear().commit()
                    base.evaluate()
                }
            }
    }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()

    @Test fun cleanInstallRequiresUserToCreateRealProfile() {
        ui.onNodeWithText("Hôm nay bạn muốn học gì?").assertIsDisplayed()
        ui.onNodeWithTag("language-en").performClick()
        ui.onNodeWithText("Ai học hôm nay?").assertIsDisplayed()
        ui.onNodeWithText("Chưa có hồ sơ người học").assertIsDisplayed()
        ui.onNodeWithText("Nghĩa", substring = false).assertDoesNotExist()
        ui.onNodeWithText("+ Tạo hồ sơ mới").performClick()
        ui.onNodeWithText("Tạo hồ sơ", substring = false).assertIsDisplayed()
        ui.onNode(hasSetTextAction()).performTextInput("Lan")
        ui.onNodeWithText("Lưu và bắt đầu").performClick()
        ui.onNodeWithText("Chào Lan!").assertIsDisplayed()
        val registry = LearnerStore(ui.activity).all()
        assertEquals(1, registry.size)
        assertEquals("Lan", registry.single().name)
        assertNotEquals(ORIGINAL_LEARNER, registry.single().id)
    }
}
