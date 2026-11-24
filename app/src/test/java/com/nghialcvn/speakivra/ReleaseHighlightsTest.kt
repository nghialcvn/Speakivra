package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class ReleaseHighlightsTest {
    @Test fun updateUiContainsTitlesWithoutMarkdownOrExplanatoryParagraphs() {
        val notes = "# Version 1.0.6\n\nLong implementation paragraph, not for the app UI.\n\n- **Logo trong suốt**: detailed implementation\n- Sửa nhận dạng: more details\n- Cập nhật gọn hơn"
        assertEquals(listOf("Logo trong suốt", "Sửa nhận dạng", "Cập nhật gọn hơn"), releaseHighlights(notes))
        assertEquals(listOf("Cải tiến và sửa lỗi ứng dụng"), releaseHighlights(""))
        assertTrue(releaseHighlights("- " + "Description ".repeat(200)).single().length <= 73)
    }
}
