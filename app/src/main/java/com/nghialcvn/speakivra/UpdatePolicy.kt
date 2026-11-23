package com.nghialcvn.speakivra

import java.io.File
import java.io.InputStream
import java.net.URI
import java.security.MessageDigest

internal object UpdatePolicy {
    const val MAX_APK_BYTES = 200L * 1024 * 1024
    fun requireDownloadHost(url: String) {
        val uri = URI(url)
        require(uri.scheme == "https" && uri.host in setOf("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com") &&
            uri.port == -1 && uri.userInfo == null) { "Máy chủ tải cập nhật không hợp lệ." }
    }
    fun requireIdentity(packageName: String, code: Long, version: String, certificate: String,
                        current: Installation, release: AppRelease) {
        require(packageName == current.packageName && packageName == release.packageName) { "APK không phải Speakivra." }
        require(code > current.versionCode && code == release.versionCode && version == release.version) { "Phiên bản APK không khớp bản cập nhật." }
        require(certificate.isNotBlank() && certificate.equals(current.certificate, true) &&
            certificate.equals(release.certificate, true)) { "Chữ ký APK không khớp. Đã chặn cài đặt để bảo vệ dữ liệu." }
    }
    fun copyVerified(input: InputStream, target: File, size: Long, sha256: String,
                     ensureActive: () -> Unit = {}, progress: (Long) -> Unit = {}) {
        require(size in 1..MAX_APK_BYTES && Regex("[0-9a-fA-F]{64}").matches(sha256)) { "Thông tin tệp cập nhật không hợp lệ." }
        val part = File(target.parentFile, target.name + ".part")
        target.delete(); part.delete()
        try {
            val hash = MessageDigest.getInstance("SHA-256")
            var count = 0L
            part.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    ensureActive()
                    val read = input.read(buffer)
                    if (read < 0) break
                    count += read
                    require(count <= size) { "Tệp tải về lớn hơn dung lượng công bố." }
                    output.write(buffer, 0, read); hash.update(buffer, 0, read); progress(count)
                }
            }
            ensureActive()
            require(count == size) { "APK tải chưa đầy đủ. Hãy thử lại." }
            val actual = hash.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
            require(actual.equals(sha256, true)) { "Mã SHA-256 không khớp. Hãy tải lại APK." }
            check(part.renameTo(target)) { "Không lưu được tệp cập nhật." }
        } finally { part.delete() }
    }
}
