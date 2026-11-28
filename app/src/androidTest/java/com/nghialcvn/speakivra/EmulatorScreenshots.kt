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
    // Compose instrumentation may disable HardwareRenderer drawing between checks.
    // Match AndroidX's withDrawingEnabled contract, but wait for the real frame
    // commit and capture the display (including dialog windows) instead of PixelCopy.
    val enabled = androidx.test.platform.graphics.HardwareRendererCompat.isDrawingEnabled()
    try {
        androidx.test.platform.graphics.HardwareRendererCompat.setDrawingEnabled(true)
        val committed = java.util.concurrent.CountDownLatch(1)
        instrumentation.runOnMainSync {
            val windows = android.view.inspector.WindowInspector.getGlobalWindowViews()
                .filter { it.isShown && it.isAttachedToWindow }
            check(windows.isNotEmpty()) { "No visible review window" }
            windows.last().viewTreeObserver.registerFrameCommitCallback { committed.countDown() }
            windows.forEach { it.requestLayout(); it.invalidate() }
        }
        check(committed.await(10, java.util.concurrent.TimeUnit.SECONDS)) { "Android did not commit the review frame" }
        // Frame commit submits the buffer; allow SurfaceFlinger to latch it on
        // the next display tick before capturing the physical display.
        android.os.SystemClock.sleep(100)
        return requireNotNull(instrumentation.uiAutomation.takeScreenshot())
    } finally {
        androidx.test.platform.graphics.HardwareRendererCompat.setDrawingEnabled(enabled)
    }
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
