package com.nghialcvn.speakivra

import android.app.NotificationManager
import android.content.Intent
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Cross-app System UI test owns its lifecycle; no Compose ActivityScenario behind the shade. */
@RunWith(AndroidJUnit4::class)
class UpdateNotificationUiTest {
    @Test fun actualNotificationSurvivesOlderCheckAndTapResumesSameLearnerSession() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        check(device.executeShellCommand("getprop ro.kernel.qemu").trim() == "1") { "Disposable CI emulator only" }
        fun resumed(): MainActivity? {
            var result: MainActivity? = null
            instrumentation.runOnMainSync {
                val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    .filterIsInstance<MainActivity>()
                if (activities.size == 1) result = activities.single()
            }
            return result
        }
        fun closeApp() {
            instrumentation.runOnMainSync {
                listOf(Stage.RESUMED, Stage.PAUSED, Stage.STOPPED, Stage.CREATED).flatMap {
                    ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(it).toList()
                }.filterIsInstance<MainActivity>().distinct().forEach { it.finish() }
            }
            device.pressHome()
        }
        closeApp()
        context.getSharedPreferences(CURRICULUM_MIGRATIONS, 0).edit().putBoolean(RESET_108, true).commit()
        context.getSharedPreferences("speakivra_learners", 0).edit().putString("profiles",
            encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0)))).commit()
        context.getSharedPreferences("speakivra_progress", 0).edit().putBoolean("neon_theme", true)
            .putString("active_lesson", "greetings").commit()
        context.getSharedPreferences("speakivra_update_notifications", 0).edit().remove("notified_version").commit()
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            context.startActivity(requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            requireNotNull(device.wait(Until.findObject(By.text("Bắt đầu học")), 15000)).click()
            device.wait(Until.findObject(By.text("Giữ an toàn tiến độ học")), 5000)?.let {
                requireNotNull(device.wait(Until.findObject(By.text("Tiếp tục học")), 5000)).click()
            }
            assertTrue(device.wait(Until.hasObject(By.text("Chào Nghĩa!")), 10000))
            val original = requireNotNull(resumed()) { "Speakivra did not resume before the notification" }
            val current = currentInstallation(context)
            // Explicit future-release fixture exercises native posting only; no fake production update.
            val release = AppRelease("test", current.versionCode + 1,
                "- Phần thưởng tim\n- Sửa tiếp tục bài học\n- Logo trong suốt",
                "https://github.com/nghialcvn/Speakivra/releases/download/vtest/test.apk",
                current.packageName, current.certificate, 1, "0".repeat(64))
            UpdateNotifications.deliver(context, release, current)
            val deadline = SystemClock.uptimeMillis() + 5000
            while (manager.activeNotifications.none { it.id == UpdateNotifications.ID } && SystemClock.uptimeMillis() < deadline)
                SystemClock.sleep(100)
            assertTrue(manager.activeNotifications.any { it.id == UpdateNotifications.ID })
            UpdateNotifications.deliver(context, release.copy(versionCode = current.versionCode), current)
            assertTrue(device.openNotification())
            val card = device.wait(Until.findObject(By.textContains("Có bản cập nhật mới")), 10000)
            val out = File(context.getExternalFilesDir(null), "ui-screenshots").apply { mkdirs() }
            assertTrue(device.takeScreenshot(File(out, "53-native-update-notification-fixture.png")))
            device.dumpWindowHierarchy(File(out, "53-system-notification-hierarchy.xml"))
            requireNotNull(card) { "Native update notification is missing" }.click()
            assertTrue(device.wait(Until.hasObject(By.text("Cập nhật ứng dụng")), 10000))
            assertTrue(device.wait(Until.hasObject(By.textStartsWith("Phiên bản đang dùng: v")), 10000))
            val resumeDeadline = SystemClock.uptimeMillis() + 10000
            while (resumed() !== original && SystemClock.uptimeMillis() < resumeDeadline) SystemClock.sleep(100)
            assertSame("Notification must resume the original learner session", original, resumed())
            assertTrue(device.takeScreenshot(File(out, "54-notification-opened-existing-app.png")))
        } finally {
            device.executeShellCommand("cmd statusbar collapse")
            manager.cancel(UpdateNotifications.ID)
            closeApp()
        }
    }
}
