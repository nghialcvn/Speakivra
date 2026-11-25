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


/** Home choices are now behind the explicitly requested collapsed learning group. */
internal fun androidx.compose.ui.test.junit4.ComposeContentTestRule.expandHomeLearning() {
    val header = onNodeWithText("Học tập", substring = false)
    if (header.fetchSemanticsNode().config.getOrElse(
            androidx.compose.ui.semantics.SemanticsProperties.StateDescription) { "" } == "Đang thu gọn") {
        header.performScrollTo().performClick()
    }
}
