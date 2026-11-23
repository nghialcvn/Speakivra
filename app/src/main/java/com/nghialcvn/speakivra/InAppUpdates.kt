package com.nghialcvn.speakivra

import android.app.Application
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

internal class UpdateFiles(private val context: Context) {
    val directory = File(context.cacheDir, "updates")
    private val pending get() = File(directory, "pending.json")
    fun clear() { directory.listFiles()?.forEach { it.delete() } }
    fun destination(): File {
        check(directory.isDirectory || directory.mkdirs()) { "Không tạo được bộ nhớ tạm." }
        return File(directory, "speakivra-update.apk")
    }
    fun verify(file: File, release: AppRelease, current: Installation) {
        require(file.isFile && file.canonicalFile.parentFile == directory.canonicalFile) { "Tệp tạm không còn. Hãy tải lại." }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
        require(file.length() == release.size && hash.equals(release.sha256, true)) { "Tệp APK không còn nguyên vẹn. Hãy tải lại." }
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive = context.packageManager.getPackageArchiveInfo(file.absolutePath, flags) ?: error("Không đọc được APK cập nhật.")
        @Suppress("DEPRECATION")
        val signatures = if (Build.VERSION.SDK_INT >= 28) archive.signingInfo?.apkContentsSigners else archive.signatures
        val signer = signatures?.singleOrNull()?.toByteArray() ?: error("Không xác định được chữ ký APK.")
        val certificate = MessageDigest.getInstance("SHA-256").digest(signer).joinToString("") { "%02x".format(it.toInt() and 0xff) }
        @Suppress("DEPRECATION")
        val code = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        UpdatePolicy.requireIdentity(archive.packageName, code, archive.versionName.orEmpty(), certificate, current, release)
        require((archive.applicationInfo?.minSdkVersion ?: Int.MAX_VALUE) <= Build.VERSION.SDK_INT) { "APK yêu cầu phiên bản Android mới hơn." }
    }
    fun save(release: AppRelease) {
        val data = JSONObject().put("version", release.version).put("code", release.versionCode).put("notes", release.notes.take(6000))
            .put("url", release.apkUrl).put("package", release.packageName).put("certificate", release.certificate)
            .put("size", release.size).put("sha256", release.sha256)
        val temporary = File(directory, "pending.json.part")
        temporary.writeText(data.toString())
        check(temporary.renameTo(pending)) { "Không lưu được trạng thái cập nhật." }
    }
    fun restore(current: Installation): AppRelease? {
        if (!pending.isFile) { clear(); return null }
        return try {
            require(pending.length() < 64 * 1024)
            val data = JSONObject(pending.readText())
            val release = AppRelease(data.getString("version"), data.getLong("code"), data.getString("notes"), data.getString("url"),
                data.getString("package"), data.getString("certificate"), data.getLong("size"), data.getString("sha256"))
            require(officialAsset(release.apkUrl) && release.canInstallOver(current) && release.versionCode > current.versionCode)
            verify(destination(), release, current)
            release
        } catch (_: Exception) { clear(); null }
    }
    @Suppress("DEPRECATION")
    fun installerIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".updates", file)
        return Intent(Intent.ACTION_INSTALL_PACKAGE).setDataAndType(uri, "application/vnd.android.package-archive")
            .putExtra(Intent.EXTRA_RETURN_RESULT, true).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { clipData = ClipData.newRawUri("Speakivra update", uri) }
    }
    fun permissionIntent() = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.packageName))
}

internal fun downloadUpdate(release: AppRelease, file: File, active: () -> Unit, progress: (Long) -> Unit) {
    require(officialAsset(release.apkUrl)) { "APK không thuộc kho phát hành Speakivra." }
    var url = release.apkUrl
    repeat(6) { attempt ->
        active(); UpdatePolicy.requireDownloadHost(url)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000; connection.readTimeout = 20000; connection.instanceFollowRedirects = false
        connection.setRequestProperty("User-Agent", "Speakivra-Android/${BuildConfig.VERSION_NAME}")
        try {
            val code = connection.responseCode
            if (code in listOf(301, 302, 303, 307, 308)) {
                require(attempt < 5) { "Đường dẫn tải chuyển tiếp quá nhiều lần." }
                url = URL(URL(url), connection.getHeaderField("Location") ?: error("Thiếu đường dẫn tải.")).toString()
            } else {
                require(code == 200) { "Không tải được APK (HTTP $code). Hãy thử lại." }
                require(connection.contentLengthLong < 0 || connection.contentLengthLong == release.size) { "Dung lượng tải không khớp." }
                connection.inputStream.use { UpdatePolicy.copyVerified(it, file, release.size, release.sha256, active, progress) }
                return
            }
        } finally { connection.disconnect() }
    }
    error("Không mở được đường dẫn cập nhật.")
}

internal class InAppUpdates(application: Application) : AndroidViewModel(application) {
    val installation = currentInstallation(application)
    var state by mutableStateOf(UpdateState()); private set
    private val files = UpdateFiles(application)
    private val mutex = Mutex()
    private var task: Job? = null
    private var checkedOnce = false
    fun checkOnce() { if (!checkedOnce) { checkedOnce = true; check(true) } }
    fun check() = check(false)
    private fun check(restore: Boolean) {
        if (task?.isActive == true || state.file != null) return
        task = viewModelScope.launch {
            state = UpdateState(phase = "checking", message = "Đang kiểm tra cập nhật…")
            try {
                val saved = if (restore) withContext(Dispatchers.IO) { mutex.withLock { files.restore(installation) } } else null
                state = if (saved != null) UpdateState(phase = "downloaded", release = saved, file = files.destination(), bytes = saved.size,
                    message = "APK đã tải và kiểm tra. Bấm Cài cập nhật để tiếp tục.") else checkForUpdate(installation)
            } catch (cancelled: CancellationException) { throw cancelled }
              catch (_: Exception) { state = UpdateState(message = "Không kiểm tra được cập nhật. Kiểm tra kết nối và thử lại.") }
        }
    }
    fun download() {
        val release = state.release ?: return
        if (task?.isActive == true || state.file != null || !release.canInstallOver(installation) || release.versionCode <= installation.versionCode) return
        task = viewModelScope.launch {
            state = state.copy(phase = "downloading", bytes = 0, file = null, message = "Đang tải bản cập nhật…")
            try {
                val file = withContext(Dispatchers.IO) {
                    mutex.withLock {
                        files.clear(); val target = files.destination()
                        require(StatFs(files.directory.absolutePath).availableBytes > release.size + 2 * 1024 * 1024) { "Không đủ dung lượng tạm để cập nhật." }
                        val downloadContext = coroutineContext
                        var lastProgress = 0L
                        downloadUpdate(release, target, { downloadContext.ensureActive() }) { count ->
                            val now = System.currentTimeMillis()
                            if (now - lastProgress > 200 || count == release.size) {
                                lastProgress = now
                                viewModelScope.launch { if (state.phase == "downloading") state = state.copy(bytes = count) }
                            }
                        }
                        downloadContext.ensureActive()
                        withContext(Dispatchers.Main) { state = state.copy(phase = "verifying", message = "Đang kiểm tra APK và chữ ký…") }
                        files.verify(target, release, installation); files.save(release); target
                    }
                }
                state = state.copy(phase = "ready", file = file, bytes = release.size, message = "APK hợp lệ. Đang chuẩn bị cài cập nhật…")
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable + Dispatchers.IO) { mutex.withLock { files.clear() } }; throw cancelled
            } catch (error: Exception) {
                withContext(Dispatchers.IO) { mutex.withLock { files.clear() } }
                state = state.copy(phase = "idle", file = null, bytes = 0, message = error.message ?: "Không tải được cập nhật. Hãy thử lại.")
            }
        }
    }
    fun cancel() {
        if (state.phase != "downloading") return
        val previous = task
        previous?.cancel()
        state = state.copy(phase = "cancelling", message = "Đang hủy và xóa tệp tải dở…")
        task = viewModelScope.launch {
            previous?.join(); withContext(Dispatchers.IO) { mutex.withLock { files.clear() } }
            state = state.copy(phase = "idle", file = null, bytes = 0, message = "Đã hủy tải. Có thể thử lại.")
        }
    }
    fun waitingForPermission() { state = state.copy(phase = "permission", message = "Cho phép Speakivra cài ứng dụng để tiếp tục cập nhật.") }
    fun installReturned(message: String = "Nếu chưa cài xong, bấm Cài cập nhật để thử lại.") { state = state.copy(phase = "downloaded", message = message) }
    suspend fun prepareInstall(): Intent {
        val release = state.release ?: error("Chưa có bản cập nhật.")
        val file = state.file ?: error("Tệp tạm không còn. Hãy tải lại.")
        state = state.copy(phase = "verifying", message = "Đang kiểm tra APK trước khi cài…")
        try {
            withContext(Dispatchers.IO) { mutex.withLock { files.verify(file, release, installation) } }
        } catch (error: Exception) {
            withContext(NonCancellable + Dispatchers.IO) { mutex.withLock { files.clear() } }
            state = state.copy(phase = "idle", file = null, bytes = 0, message = "APK không còn hợp lệ. Hãy tải lại.")
            throw error
        }
        state = state.copy(phase = "installing", message = "Đang mở trình cài đặt Android…")
        return files.installerIntent(file)
    }
    fun permissionIntent() = files.permissionIntent()
}
