package com.nghialcvn.speakivra

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest

internal const val RELEASES_URL = "https://github.com/nghialcvn/Speakivra/releases"
private const val ASSET_PREFIX = "$RELEASES_URL/download/"
private const val RELEASE_API = "https://api.github.com/repos/nghialcvn/Speakivra/releases?per_page=10"

internal data class Installation(val packageName: String, val versionCode: Long, val certificate: String)
internal data class AppRelease(
    val version: String,
    val versionCode: Long,
    val notes: String,
    val apkUrl: String,
    val packageName: String,
    val certificate: String,
    val size: Long,
    val sha256: String
) {
    fun canInstallOver(current: Installation): Boolean =
        packageName == current.packageName && certificate.equals(current.certificate, ignoreCase = true)
}

internal data class UpdateState(
    val phase: String = "idle",
    val bytes: Long = 0,
    val file: File? = null,
    val message: String = "Chưa kiểm tra cập nhật.",
    val release: AppRelease? = null
) { val checking: Boolean get() = phase == "checking" }

internal fun currentInstallation(context: Context): Installation {
    val manager = context.packageManager
    val info = manager.getPackageInfo(context.packageName,
        if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES)
    @Suppress("DEPRECATION")
    val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
    val certificate = signatures?.singleOrNull()?.toByteArray()
        ?: error("Không xác định được chữ ký ứng dụng.")
    val digest = MessageDigest.getInstance("SHA-256").digest(certificate)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    @Suppress("DEPRECATION")
    val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
    return Installation(context.packageName, code, digest)
}

internal fun officialAsset(url: String): Boolean = runCatching {
    val uri = URI(url)
    uri.scheme == "https" && uri.host == "github.com" && uri.userInfo == null && uri.port == -1 &&
        uri.rawQuery == null && uri.rawFragment == null && url.startsWith(ASSET_PREFIX) &&
        uri.rawPath == uri.path && uri.path.split('/').none { it == "." || it == ".." } &&
        uri.path.removePrefix("/nghialcvn/Speakivra/releases/download/").split('/').let {
            it.size == 2 && it.all(String::isNotBlank)
        }
}.getOrDefault(false)

internal fun releaseFromMetadata(release: JSONObject, metadata: JSONObject): AppRelease {
    require(!release.optBoolean("draft")) { "Bản phát hành chưa công bố." }
    val version = metadata.getString("versionName")
    val code = metadata.getLong("versionCode")
    val certificate = metadata.getString("signingCertificateSha256")
    val apkName = metadata.getString("apkName")
    val tag = release.getString("tag_name")
    require(tag == "v$version" || tag == "v$version-beta") { "Phiên bản không khớp bản phát hành." }
    require(code > 0 && Regex("[0-9a-fA-F]{64}").matches(certificate)) { "Thông tin cập nhật không hợp lệ." }
    require(apkName.endsWith(".apk") && '/' !in apkName) { "Tên APK không hợp lệ." }
    val assets = release.getJSONArray("assets")
    val asset = (0 until assets.length()).map { assets.getJSONObject(it) }
        .singleOrNull { it.optString("name") == apkName } ?: error("Bản phát hành chưa có APK phù hợp.")
    val apkUrl = asset.getString("browser_download_url")
    val size = asset.getLong("size")
    val sha256 = metadata.getString("apkSha256")
    require(size in 1..UpdatePolicy.MAX_APK_BYTES && Regex("[0-9a-fA-F]{64}").matches(sha256)) { "Thiếu thông tin kiểm tra APK." }
    val assetDigest = asset.optString("digest").removePrefix("sha256:")
    require(assetDigest.isBlank() || assetDigest.equals(sha256, true)) { "Mã kiểm tra APK không khớp GitHub." }
    require(officialAsset(apkUrl) && apkUrl == "$ASSET_PREFIX$tag/$apkName") { "APK không thuộc nguồn chính thức." }
    return AppRelease(version, code, release.optString("body"), apkUrl,
        metadata.getString("applicationId"), certificate, size, sha256.lowercase())
}

private fun requestJson(url: String): String? {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = 12000
    connection.readTimeout = 12000
    connection.setRequestProperty("Accept", "application/vnd.github+json")
    connection.setRequestProperty("User-Agent", "Speakivra-Android/${BuildConfig.VERSION_NAME}")
    try {
        if (connection.responseCode == 404) return null
        check(connection.responseCode in 200..299) { "GitHub HTTP ${connection.responseCode}" }
        return connection.inputStream.bufferedReader().use { reader ->
            val result = StringBuilder()
            val buffer = CharArray(4096)
            while (true) {
                val count = reader.read(buffer)
                if (count < 0) break
                result.append(buffer, 0, count)
                check(result.length <= 1_000_000) { "Phản hồi cập nhật quá lớn." }
            }
            result.toString()
        }
    } finally { connection.disconnect() }
}

internal suspend fun checkForUpdate(current: Installation): UpdateState = withContext(Dispatchers.IO) {
    // The list endpoint includes public beta releases; /latest excludes prereleases.
    val releases = JSONArray(requestJson(RELEASE_API) ?: "[]")
    val latest = (0 until releases.length()).map { releases.getJSONObject(it) }
        .firstOrNull { !it.optBoolean("draft") }
        ?: return@withContext UpdateState(message = "Chưa có bản phát hành GitHub Releases.")
    val assets = latest.optJSONArray("assets") ?: JSONArray()
    val metadataUrl = (0 until assets.length()).map { assets.getJSONObject(it) }
        .firstOrNull { it.optString("name") == "speakivra-update.json" }
        ?.optString("browser_download_url").orEmpty()
    if (!officialAsset(metadataUrl) || metadataUrl != "$ASSET_PREFIX${latest.optString("tag_name")}/speakivra-update.json") {
        return@withContext UpdateState(message = "Bản phát hành chưa có thông tin xác minh cập nhật.")
    }
    val metadata = requestJson(metadataUrl)
        ?: return@withContext UpdateState(message = "Thông tin cập nhật chưa sẵn sàng.")
    val item = releaseFromMetadata(latest, JSONObject(metadata))
    val message = when {
        item.packageName != current.packageName -> "Bản phát hành không đúng ứng dụng Speakivra."
        item.versionCode <= current.versionCode -> "Bạn đang dùng bản mới nhất hoặc mới hơn (${item.version})."
        !item.canInstallOver(current) -> "Có bản ${item.version}, nhưng khác khóa ký. Chưa thể cài đè giữ dữ liệu; đừng gỡ ứng dụng hiện tại."
        else -> "Có bản mới: v${item.version}"
    }
    UpdateState(message = message, release = item)
}
