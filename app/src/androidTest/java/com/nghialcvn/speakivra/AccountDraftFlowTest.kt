package com.nghialcvn.speakivra

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.io.File
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

@RunWith(AndroidJUnit4::class)
class AccountDraftFlowTest {
    @get:Rule(order = 0) val seed = TestRule { base, _: Description -> object : Statement() {
        override fun evaluate() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
            context.getSharedPreferences("speakivra_learners", 0).edit().clear().putString("profiles",
                encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
            context.getSharedPreferences("speakivra_guest_identity", 0).edit().clear().commit()
            context.getSharedPreferences("speakivra_progress", 0).edit().clear()
                .putBoolean("automatic_mode", false).putBoolean("dark_theme", false).putBoolean("neon_theme", false)
                .putString("active_lesson", "greetings").putString("lesson.greetings.phase", "repeat")
                .putInt("lesson.greetings.phrase", 2).putInt("lesson.greetings.rounds", 1).commit()
            base.evaluate()
        }
    } }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        dismissUnrelatedLauncherAnr(instrumentation)
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
    @Test fun oldProfileGetsAStableSerialWithoutChangingNameAvatarOrLearningData() {
        val store = LearnerStore(ui.activity); val person = store.all().single()
        val before = store.preferences(person).all.toMap()
        val identityStore = GuestIdentityStore(ui.activity)
        val workers = java.util.concurrent.Executors.newFixedThreadPool(4)
        val concurrent = try {
            workers.invokeAll((1..8).map { java.util.concurrent.Callable { GuestIdentityStore(ui.activity).ensure(person) } }).map { it.get() }
        } finally { workers.shutdownNow() }
        Assert.assertEquals(1, concurrent.toSet().size)
        val identity = identityStore.ensure(person)
        Assert.assertEquals(concurrent.first(), identity)
        ui.waitUntil(10000) { ui.onAllNodesWithText(identity.serial).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Hồ sơ khách • Chưa liên kết").assertExists()
        shot("80-guest-profile-serial")
        Assert.assertEquals(identity, GuestIdentityStore(ui.activity).ensure(person))
        store.update(person.id, "Nghĩa An", 3)
        Assert.assertEquals(identity, identityStore.ensure(store.all().single()))
        Assert.assertEquals(before, store.preferences(person).all)
        val proof = identityStore.proof(person, "account-A")
        val publicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(Base64.decode(identity.publicKey, Base64.NO_WRAP)))
        Assert.assertTrue(verifyGuestProof(publicKey, "account-A", identity.profileId, proof))
        Assert.assertFalse(verifyGuestProof(publicKey, "account-B", identity.profileId, proof))
    }
    @Test fun realLoginAndRegisterScreensShowUnavailableCloudWithoutPretendingToCreateAnAccount() {
        ui.onNodeWithText("Tài khoản & đồng bộ", substring = false).performClick()
        ui.onNodeWithText("Tiếp tục với Google").assertExists()
        ui.onNodeWithText("Tiếp tục với Microsoft").assertExists()
        shot("81-approved-login-draft")
        ui.onNodeWithText("Tiếp tục với Microsoft").performClick()
        ui.onNodeWithText("Chưa kết nối dịch vụ tài khoản. Hồ sơ và tiến độ vẫn lưu trên máy.").performScrollTo().assertExists()
        ui.onNode(hasText("Tạo tài khoản") and hasClickAction()).performScrollTo().performClick()
        ui.onNodeWithText("Nhập lại mật khẩu").assertExists()
        shot("82-register-draft")
        ui.onNode(hasText("Tạo tài khoản") and hasClickAction()).performScrollTo().performClick()
        ui.onNodeWithText("Nhập địa chỉ email hợp lệ.").performScrollTo().assertExists()
        ui.onNodeWithText("Xác minh email", substring = false).assertDoesNotExist()
        ui.onNodeWithText("Quay lại đăng nhập").performScrollTo().performClick()
        ui.onNodeWithText("Tiếp tục học trên máy này").performScrollTo().performClick()
        ui.onNodeWithText("Bắt đầu học").performClick()
        ui.onNodeWithText("Tiếp tục học", substring = false).performScrollTo().performClick()
        ui.onNodeWithText("My name is Nghĩa.").assertExists()
    }
    @Test fun bindingReceiptCannotAssignTheSameIdentityToAnotherAccount() {
        val store = LearnerStore(ui.activity); val person = store.add("Linh", 1)
        val identities = GuestIdentityStore(ui.activity); val guest = identities.ensure(person)
        val owner = identities.recordServerReceipt(person, GuestClaimReceipt(guest.profileId, "A"), "A")
        Assert.assertEquals(owner, GuestIdentityStore(ui.activity).ensure(person))
        Assert.assertFalse(owner.canOpen("B"))
        Assert.assertThrows(IllegalArgumentException::class.java) {
            identities.recordServerReceipt(person, GuestClaimReceipt(guest.profileId, "B"), "B")
        }
    }
}
