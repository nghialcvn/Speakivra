package com.nghialcvn.speakivra

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Isolated local ownership fixtures; no cloud claim or account login is simulated. */
@RunWith(AndroidJUnit4::class)
class GuestLinkReminderUiTest {
    @get:Rule val ui = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val store get() = LearnerStore(context)
    private fun person(name: String) = Learner(UUID.randomUUID().toString(), name, 0)
    @Test fun guestCanContinueWithoutLinkingOrChangingProgress() {
        val learner = person("Nghĩa")
        val prefs = store.preferences(learner)
        prefs.edit().putInt("lesson.greetings.phrase", 2).commit()
        val before = prefs.all.toMap()
        var started: Learner? = null
        ui.setContent { SpeakivraTheme(false) {
            LearnerPicker(store, listOf(learner), learner.id, {}, { started = it })
        } }
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Giữ an toàn tiến độ học").assertIsDisplayed()
        ui.onNodeWithText("Liên kết tài khoản để lưu hồ sơ, tiến độ và kim cương, giúp bạn tiếp tục học khi đổi máy.").assertIsDisplayed()
        ui.onNodeWithText("Khi chưa liên kết, dữ liệu chỉ được lưu trên thiết bị này và có thể mất nếu bạn gỡ ứng dụng hoặc đổi máy.").assertIsDisplayed()
        assertNull(started)
        ui.onNodeWithText("Tiếp tục học", substring = false).performScrollTo().performClick()
        ui.runOnIdle { assertEquals(learner, started) }
        ui.onNodeWithText("Giữ an toàn tiến độ học").assertDoesNotExist()
        assertEquals(before, prefs.all)
        assertNull(GuestIdentityStore(context).peek(learner)?.ownerUid)
        // Dismissing once does not mark the profile as linked or suppress later reminders.
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Giữ an toàn tiến độ học").assertIsDisplayed()
    }
    @Test fun linkUsesTheSelectedLearnerAndDoesNotStartStudy() {
        val first = person("Nghĩa")
        val selected = person("Linh")
        var linked: Learner? = null
        var started = false
        ui.setContent { SpeakivraTheme(false) {
            LearnerPicker(store, listOf(first, selected), first.id, {}, { started = true },
                onLink = { linked = it })
        } }
        ui.onNodeWithText("Linh", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Liên kết ngay").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(selected, linked); assertFalse(started) }
        ui.onNodeWithText("Giữ an toàn tiến độ học").assertDoesNotExist()
    }
    @Test fun ownedProfileSkipsReminderButUnlinkedProfileOnSameDeviceDoesNot() {
        val owned = person("Nghĩa")
        val guest = person("Linh")
        val identities = GuestIdentityStore(context)
        val identity = identities.ensure(owned)
        identities.recordServerReceipt(owned, GuestClaimReceipt(identity.profileId, "test-owner"), "test-owner")
        var started: Learner? = null
        ui.setContent { SpeakivraTheme(false) {
            LearnerPicker(store, listOf(owned, guest), owned.id, {}, { started = it })
        } }
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.runOnIdle { assertEquals(owned, started) }
        ui.onNodeWithText("Giữ an toàn tiến độ học").assertDoesNotExist()
        ui.onNodeWithText("Linh", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Giữ an toàn tiến độ học").assertIsDisplayed()
        ui.runOnIdle { assertEquals(owned, started) }
    }
}
