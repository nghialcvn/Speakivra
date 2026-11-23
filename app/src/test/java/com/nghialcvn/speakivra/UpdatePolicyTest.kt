package com.nghialcvn.speakivra

import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.CancellationException
import org.junit.Assert.*
import org.junit.Test

class UpdatePolicyTest {
    private val bytes = "verified apk fixture".toByteArray()
    private val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun withTarget(test: (File) -> Unit) {
        val folder = Files.createTempDirectory("speakivra-update-test").toFile()
        try { test(File(folder, "update.apk")) } finally { folder.deleteRecursively() }
    }
    @Test fun verifiedDownloadBecomesReadyOnlyAfterExactSizeAndHashMatch() = withTarget { target ->
        var last = 0L
        UpdatePolicy.copyVerified(bytes.inputStream(), target, bytes.size.toLong(), digest, progress = { last = it })
        assertArrayEquals(bytes, target.readBytes())
        assertEquals(bytes.size.toLong(), last)
        assertFalse(File(target.parentFile, target.name + ".part").exists())
    }
    @Test fun truncatedOversizedAndCorruptDownloadsLeaveNoInstallableFile() = withTarget { target ->
        listOf(bytes.size - 1L to digest, bytes.size + 1L to digest, bytes.size.toLong() to "ab".repeat(32)).forEach { (size, sha) ->
            assertThrows(IllegalArgumentException::class.java) { UpdatePolicy.copyVerified(bytes.inputStream(), target, size, sha) }
            assertFalse(target.exists())
            assertFalse(File(target.parentFile, target.name + ".part").exists())
        }
    }
    @Test fun cancellingDuringCopyRemovesPartialApk() = withTarget { target ->
        var checks = 0
        assertThrows(CancellationException::class.java) {
            UpdatePolicy.copyVerified(bytes.inputStream(), target, bytes.size.toLong(), digest,
                ensureActive = { if (++checks > 1) throw CancellationException() })
        }
        assertFalse(target.exists()); assertFalse(File(target.parentFile, target.name + ".part").exists())
    }
    @Test fun actualApkMustMatchInstalledIdentityAndPublishedVersion() {
        val signer = "ab".repeat(32)
        val current = Installation("com.nghialcvn.speakivra", 6, signer)
        val release = AppRelease("0.1.6", 7, "", "", current.packageName, signer, bytes.size.toLong(), digest)
        UpdatePolicy.requireIdentity(current.packageName, 7, "0.1.6", signer, current, release)
        listOf(
            { UpdatePolicy.requireIdentity("other.app", 7, "0.1.6", signer, current, release) },
            { UpdatePolicy.requireIdentity(current.packageName, 6, "0.1.6", signer, current, release) },
            { UpdatePolicy.requireIdentity(current.packageName, 8, "0.1.6", signer, current, release) },
            { UpdatePolicy.requireIdentity(current.packageName, 7, "0.1.7", signer, current, release) },
            { UpdatePolicy.requireIdentity(current.packageName, 7, "0.1.6", "cd".repeat(32), current, release) }
        ).forEach { assertThrows(IllegalArgumentException::class.java) { it() } }
    }
    @Test fun redirectsAllowOnlyHttpsGitHubAssetHosts() {
        listOf("https://github.com/a", "https://release-assets.githubusercontent.com/a?token=fixture",
            "https://objects.githubusercontent.com/a").forEach(UpdatePolicy::requireDownloadHost)
        listOf("http://github.com/a", "https://github.com.evil/a", "https://evil@github.com/a", "https://example.com/a",
            "https://github.com:8443/a").forEach { url ->
            assertThrows(IllegalArgumentException::class.java) { UpdatePolicy.requireDownloadHost(url) }
        }
    }
}
