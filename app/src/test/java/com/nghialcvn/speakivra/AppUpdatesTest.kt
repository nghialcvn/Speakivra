package com.nghialcvn.speakivra

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AppUpdatesTest {
    private val certificate = "ab".repeat(32)
    private fun release(tag: String = "v0.1.3-beta", url: String = "https://github.com/nghialcvn/Speakivra/releases/download/$tag/Speakivra.apk") = JSONObject()
        .put("tag_name", tag).put("prerelease", true).put("body", "Cải tiến cập nhật")
        .put("assets", org.json.JSONArray().put(JSONObject().put("name", "Speakivra.apk").put("browser_download_url", url)))
    private fun metadata() = JSONObject().put("versionName", "0.1.3").put("versionCode", 4)
        .put("applicationId", "com.nghialcvn.speakivra").put("signingCertificateSha256", certificate)
        .put("apkName", "Speakivra.apk")

    @Test fun publicBetaCanBeUpdatedUsingAndroidVersionCode() {
        val item = releaseFromMetadata(release(), metadata())
        assertEquals(4L, item.versionCode)
        assertTrue(item.canInstallOver(Installation(item.packageName, 3, certificate.uppercase())))
    }
    @Test fun differentKeyOrPackageCannotInstallOverExistingData() {
        val item = releaseFromMetadata(release(), metadata())
        assertFalse(item.canInstallOver(Installation(item.packageName, 3, "cd".repeat(32))))
        assertFalse(item.canInstallOver(Installation("other.app", 3, certificate)))
    }
    @Test fun onlyExactOfficialReleaseAssetUrlsAreAccepted() {
        val good = "https://github.com/nghialcvn/Speakivra/releases/download/v0.1.3-beta/Speakivra.apk"
        assertTrue(officialAsset(good))
        listOf(good.replace("https:", "http:"), good.replace("github.com/", "github.com.evil/"),
            good.replace("github.com/", "evil@github.com/"), good + "?redirect=evil", good + "#fragment",
            good.replace("Speakivra.apk", "../Speakivra.apk"), good.replace("Speakivra.apk", "%2e%2e/other.apk"),
            good.replace("Speakivra/releases", "Another/releases")).forEach { assertFalse(it, officialAsset(it)) }
    }
    @Test fun mismatchedTagAssetOrDraftIsRejected() {
        listOf(release("v0.1.4"), release().put("draft", true),
            release(url = "https://github.com/nghialcvn/Speakivra/releases/download/v0.1.2/Speakivra.apk")).forEach {
            assertThrows(IllegalArgumentException::class.java) { releaseFromMetadata(it, metadata()) }
        }
    }
    @Test fun malformedCertificateIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            releaseFromMetadata(release(), metadata().put("signingCertificateSha256", "missing"))
        }
    }
}
