package com.nghialcvn.speakivra

import android.app.Instrumentation
import androidx.compose.ui.test.*
import android.view.accessibility.AccessibilityNodeInfo

/** CI system-launcher ANRs are unrelated to Speakivra. Never dismiss our app's dialogs. */
internal fun dismissUnrelatedLauncherAnr(instrumentation: Instrumentation) {
    val automation = instrumentation.uiAutomation
    val title = "Pixel Launcher isn't responding"
    val root = automation.rootInActiveWindow ?: return
    if (root.findAccessibilityNodeInfosByText(title).isEmpty()) return
    var close: AccessibilityNodeInfo? = root.findAccessibilityNodeInfosByText("Close app")
        .firstOrNull { it.text?.toString() == "Close app" }
    var clicked = false
    while (close != null && !clicked) {
        clicked = close.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (!clicked) close = close.parent
    }
    check(clicked) { "Cannot dismiss the CI Pixel Launcher dialog; screenshot review is blocked." }
    automation.waitForIdle(500, 5000)
    check(automation.rootInActiveWindow?.findAccessibilityNodeInfosByText(title).isNullOrEmpty()) {
        "CI Pixel Launcher still obscures the app; do not approve screenshots."
    }
}


/** Home entry and second tab both navigate to the same dedicated lesson hub. */
internal fun androidx.compose.ui.test.junit4.ComposeContentTestRule.openLessonHub() {
    onNode(hasText("Bài học", substring = false) and SemanticsMatcher.expectValue(
        androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Tab)).performClick()
    onNodeWithText("Chọn cấp độ hoặc dạng luyện tập").assertIsDisplayed()
}

/** Follow the visible, optional reminder when an existing journey starts local study. */
internal fun androidx.compose.ui.test.junit4.ComposeContentTestRule.startLocalStudy() {
    onNodeWithText("Bắt đầu học").performClick()
    if (onAllNodesWithText("Giữ an toàn tiến độ học").fetchSemanticsNodes().isNotEmpty()) {
        onNodeWithText("Tiếp tục học", substring = false).performScrollTo().performClick()
    }
}

/** Wait for local startup readiness before entering an existing learner journey. */
internal fun androidx.compose.ui.test.junit4.ComposeContentTestRule.selectEnglishAreaIfNeeded() {
    waitUntil(10000) { onAllNodesWithText("Ai học hôm nay?").fetchSemanticsNodes().isNotEmpty() }
    onNodeWithText("Ai học hôm nay?").assertIsDisplayed()
}

/** Capture the real display without Compose's two-second force-redraw/PixelCopy path. */
internal fun captureNativeDisplay(): android.graphics.Bitmap {
    val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
    instrumentation.waitForIdleSync()
    dismissUnrelatedLauncherAnr(instrumentation)
    // Semantics can be idle before SurfaceFlinger presents the new phase.
    // Wait for two real display frames instead of returning the previous buffer.
    val presented = java.util.concurrent.CountDownLatch(1)
    instrumentation.runOnMainSync {
        android.view.Choreographer.getInstance().postFrameCallback {
            android.view.Choreographer.getInstance().postFrameCallback { presented.countDown() }
        }
    }
    check(presented.await(5, java.util.concurrent.TimeUnit.SECONDS)) { "Android did not present a frame" }
    instrumentation.uiAutomation.waitForIdle(200, 5000)
    return requireNotNull(instrumentation.uiAutomation.takeScreenshot())
}

/** Keep pixel assertions scoped to the same visible node, using display coordinates. */
internal fun SemanticsNodeInteraction.captureDeviceRegion(): android.graphics.Bitmap {
    val bounds = fetchSemanticsNode().boundsInWindow
    val screen = captureNativeDisplay()
    val left = bounds.left.toInt().coerceIn(0, screen.width - 1)
    val top = bounds.top.toInt().coerceIn(0, screen.height - 1)
    val width = bounds.width.toInt().coerceIn(1, screen.width - left)
    val height = bounds.height.toInt().coerceIn(1, screen.height - top)
    val cropped = android.graphics.Bitmap.createBitmap(screen, left, top, width, height)
    if (cropped !== screen) screen.recycle()
    return cropped
}
