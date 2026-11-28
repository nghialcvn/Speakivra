package com.nghialcvn.speakivra

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ProfileFirstDataTest {
    @Test fun accountMarksRequireOwnershipAndDistinguishGoogleFromGmailPasswordLogin() {
        assertEquals(R.drawable.brand_logo_clear, accountBrandResource(null, "other", listOf("google.com"), "a@gmail.com"))
        assertNull(accountBrandResource("owner", "other", listOf("google.com"), "a@gmail.com"))
        assertNull(accountBrandResource("owner", null, emptyList(), null))
        assertEquals(R.drawable.account_google, accountBrandResource("owner", "owner", listOf("google.com"), "a@gmail.com"))
        assertEquals(R.drawable.account_gmail, accountBrandResource("owner", "owner", listOf("password"), "a@gmail.com"))
        assertEquals(R.drawable.account_microsoft, accountBrandResource("owner", "owner", listOf("microsoft.com"), "a@company.vn"))
        assertEquals(R.drawable.account_microsoft, accountBrandResource("owner", "owner", listOf("password"), "a@outlook.com"))
        assertNull(accountBrandResource("owner", "owner", listOf("password"), "a@gmail.com.attacker.test"))
    }
    @Test fun selectedLanguageSurvivesCloudSanitizationWithoutAcceptingUnsupportedValues() {
        val id = "a".repeat(64)
        val old = JSONObject().put("active_lesson", "greetings").put("zh.active_lesson", "zh_a1_greetings")
        val selected = sanitizedCloudProgress(id, JSONObject(old.toString()).put("study_language", "CHINESE"))
        assertEquals("CHINESE", selected.getString("study_language"))
        assertEquals("greetings", selected.getString("active_lesson"))
        assertEquals("zh_a1_greetings", selected.getString("zh.active_lesson"))
        val legacy = mergeCloudProgress(selected, old)
        assertEquals("CHINESE", legacy.getString("study_language"))
        assertFalse(sanitizedCloudProgress(id, JSONObject().put("study_language", "UNSUPPORTED")).has("study_language"))
    }
}
