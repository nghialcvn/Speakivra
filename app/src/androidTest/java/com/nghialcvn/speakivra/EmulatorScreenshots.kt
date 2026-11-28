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
