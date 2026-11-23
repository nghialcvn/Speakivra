package com.nghialcvn.speakivra

import android.Manifest
import android.app.DownloadManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

private val Navy = Color(0xFF101B32)
private val SurfaceBlue = Color(0xFF20314D)
private val Violet = Color(0xFF6856E8)
private val Mint = Color(0xFF43D5A0)
private val Muted = Color(0xFFB8C7DE)

private data class Phrase(val english: String, val vietnamese: String, val tip: String)
private val phrases = listOf(
    Phrase("Hello! How are you?", "Xin chào! Bạn khỏe không?", "Nối âm tự nhiên giữa How và are."),
    Phrase("I'm fine, thank you.", "Tôi khỏe, cảm ơn bạn.", "Chú ý âm /th/ trong thank."),
    Phrase("My name is Alex.", "Tên tôi là Alex.", "Nối âm giữa name và is."),
    Phrase("Nice to meet you!", "Rất vui được gặp bạn!", "Luyện âm dài trong meet.")
)
private val aiLines = listOf(
    "Hello! What's your name?" to "My name is Alex.",
    "Nice to meet you! Where are you from?" to "I'm from Vietnam.",
    "What do you like doing?" to "I like listening to music."
)


private const val APP_VERSION = "0.1.1"
private const val RELEASE_API = "https://api.github.com/repos/nghialcvn/Speakivra/releases/latest"
private const val RELEASE_URL = "https://github.com/nghialcvn/Speakivra/releases"
private data class AppRelease(val version: String, val notes: String, val apkUrl: String)

private fun isNewer(candidate: String, current: String): Boolean {
    val a = candidate.trimStart('v', 'V').split('.').map { it.toIntOrNull() ?: 0 }
    val b = current.trimStart('v', 'V').split('.').map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(a.size, b.size)) {
        val av = a.getOrElse(i) { 0 }
        val bv = b.getOrElse(i) { 0 }
        if (av != bv) return av > bv
    }
    return false
}

private fun fetchLatestRelease(): AppRelease {
    val connection = URL(RELEASE_API).openConnection() as HttpURLConnection
    connection.connectTimeout = 12000
    connection.readTimeout = 12000
    connection.setRequestProperty("Accept", "application/vnd.github+json")
    connection.setRequestProperty("User-Agent", "Speakivra-Android")
    try {
        if (connection.responseCode == 404) return AppRelease("", "Chưa có bản phát hành GitHub Releases.", "")
        if (connection.responseCode !in 200..299) throw IllegalStateException("GitHub HTTP ${connection.responseCode}")
        val data = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        val assets = data.optJSONArray("assets")
        var apkUrl = ""
        if (assets != null) for (i in 0 until assets.length()) {
            val item = assets.getJSONObject(i)
            val url = item.optString("browser_download_url")
            if (item.optString("name").endsWith(".apk", ignoreCase = true)
                && url.startsWith("https://github.com/nghialcvn/Speakivra/releases/download/")) {
                apkUrl = url
                break
            }
        }
        return AppRelease(data.optString("tag_name"), data.optString("body"), apkUrl)
    } finally { connection.disconnect() }
}

@Composable
private fun UpdatesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("speakivra_updates", Context.MODE_PRIVATE) }
    var state by remember { mutableStateOf("Chưa kiểm tra cập nhật.") }
    var checking by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf<AppRelease?>(null) }
    var downloaded by remember { mutableLongStateOf(prefs.getLong("download_id", -1)) }
    fun installDownloaded() {
        if (downloaded < 0) { state = "Chưa tải bản cập nhật."; return }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = manager.query(DownloadManager.Query().setFilterById(downloaded))
        val ready = cursor.use { it.moveToFirst() && it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL }
        if (!ready) { state = "APK vẫn đang tải hoặc tải lỗi. Kiểm tra thông báo Tải xuống."; return }
        if (Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
            state = "Cho phép cài ứng dụng từ nguồn này trong Cài đặt, sau đó trở lại nhấn Cài đặt."
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
            return
        }
        val uri = manager.getUriForDownloadedFile(downloaded)
        if (uri == null) { state = "Không tìm thấy file APK đã tải."; return }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) }
        catch (_: ActivityNotFoundException) { state = "Không tìm thấy trình cài đặt APK trên thiết bị." }
    }
    Panel {
        Text("Cập nhật ứng dụng", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Phiên bản đang dùng: v$APP_VERSION", color = Mint)
        Text("Nguồn phát hành: github.com/nghialcvn/Speakivra", color = Muted)
        Text(state, color = Muted)
        PrimaryButton(if (checking) "Đang kiểm tra..." else "Kiểm tra bản cập nhật") {
            if (!checking) {
                checking = true
                scope.launch {
                    try {
                        val latest = withContext(Dispatchers.IO) { fetchLatestRelease() }
                        release = latest
                        state = if (latest.version.isBlank()) latest.notes
                            else if (isNewer(latest.version, APP_VERSION))
                                "Có phiên bản mới: ${latest.version}"
                            else "Bạn đang dùng phiên bản mới nhất theo GitHub Releases (${latest.version})."
                    } catch (e: Exception) {
                        state = "Không thể kiểm tra cập nhật: ${e.message ?: "Lỗi kết nối"}"
                    } finally { checking = false }
                }
            }
        }
        release?.let { item ->
            if (item.version.isNotBlank()) {
                Text("Phiên bản phát hành: ${item.version}", fontWeight = FontWeight.SemiBold)
                Text("Có gì mới:", color = Mint)
                Text(item.notes.ifBlank { "Nhà phát triển chưa ghi chú thay đổi." }, color = Muted)
                if (isNewer(item.version, APP_VERSION)) {
                    if (item.apkUrl.isNotBlank()) {
                        PrimaryButton("Tải APK cập nhật") {
                            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val request = DownloadManager.Request(Uri.parse(item.apkUrl))
                                .setTitle("Speakivra ${item.version}")
                                .setDescription("Bản cập nhật từ GitHub Releases")
                                .setMimeType("application/vnd.android.package-archive")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Speakivra-${item.version}.apk")
                            try {
                                downloaded = manager.enqueue(request)
                                prefs.edit().putLong("download_id", downloaded).apply()
                                state = "Đang tải APK. Theo dõi thông báo hệ thống."
                            } catch (e: Exception) { state = "Tải APK thất bại: ${e.message}" }
                        }
                        PrimaryButton("Cài APK đã tải") { installDownloaded() }
                    } else Text("Chưa có APK trong bản phát hành này.", color = Muted)
                }
            }
        }
        Text("Cài đè chỉ thành công khi APK mới được ký cùng khóa với bản đang cài. Android sẽ yêu cầu bạn xác nhận.", color = Muted, fontSize = 12.sp)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SpeakivraApp() }
    }
}

@Composable
private fun SpeakivraApp() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("speakivra_progress", 0) }
    var page by remember { mutableStateOf("home") }
    var index by remember { mutableIntStateOf(preferences.getInt("phrase", 0).coerceIn(0, phrases.lastIndex)) }
    var completed by remember { mutableIntStateOf(preferences.getInt("completed", 0)) }
    var automatic by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    var slow by remember { mutableStateOf(false) }
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Nhấn Nghe mẫu để bắt đầu") }
    var chatIndex by remember { mutableIntStateOf(0) }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(tts) {
        onDispose { tts.stop(); tts.shutdown() }
    }
    fun speak(text: String) {
        if (!ttsReady) {
            val result = tts.setLanguage(Locale.US)
            ttsReady = result >= TextToSpeech.LANG_AVAILABLE
        }
        if (ttsReady) {
            tts.setSpeechRate(if (slow) 0.75f else 1.0f)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speakivra")
            status = "Đang phát câu mẫu"
        } else status = "Thiết bị chưa có giọng đọc tiếng Anh-Mỹ. Kiểm tra cài đặt TTS."
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val words = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        transcript = words?.firstOrNull().orEmpty()
        status = if (transcript.isBlank()) "Chưa nhận được lời nói. Hãy thử lại." else "Đã nhận dạng lời nói. Đây chưa phải điểm phát âm."
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) status = "Đã cấp micro. Nhấn Nói để bắt đầu."
        else status = "Cần cấp quyền micro để sử dụng nhận dạng giọng nói."
    }
    fun listen() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak English")
        }
        try { speechLauncher.launch(intent) }
        catch (_: ActivityNotFoundException) { status = "Thiết bị chưa có dịch vụ nhận dạng giọng nói." }
    }
    fun next() {
        if (index == phrases.lastIndex) {
            completed += 1
            preferences.edit().putInt("completed", completed).apply()
            page = "chat"
        } else {
            index++
            preferences.edit().putInt("phrase", index).apply()
            transcript = ""
            status = "Sẵn sàng học câu mới"
            if (automatic) speak(phrases[index].english)
        }
    }
    val darkScheme = darkColorScheme(primary = Violet, secondary = Mint, background = Navy, surface = SurfaceBlue)
    MaterialTheme(colorScheme = darkScheme) {
        Column(
            modifier = Modifier.fillMaxSize().background(Navy).windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("SPEAKIVRA", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Listen. Speak. Improve.", color = Muted, fontSize = 12.sp)
                }
                Text("V0.1.1 • BETA", color = Mint, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    "home" -> {
                        Panel {
                            Text("Chào mừng đến Speakivra 🎧", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Luyện nghe và nói tiếng Anh-Mỹ mỗi ngày", color = Muted)
                            Text("Mục tiêu: 20 phút/ngày • Bài học A1", color = Mint)
                        }
                        Panel {
                            Text("Bài 01: Greetings & Introductions", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("Câu hiện tại: ${index + 1}/${phrases.size}", color = Muted)
                            PrimaryButton("Bắt đầu nghe và nhắc lại") { page = "repeat"; if (automatic) speak(phrases[index].english) }
                        }
                        Panel {
                            Text("AI Tutor (mô phỏng)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Chưa kết nối API, hội thoại theo kịch bản cố định.", color = Muted)
                            PrimaryButton("Mở hội thoại mẫu") { page = "chat" }
                        }
                        Text("Đã hoàn thành: $completed lượt bài • Lưu trên thiết bị", color = Mint)
                    }
                    "repeat" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Luyện nói • Câu ${index + 1}/${phrases.size}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(if (automatic) "Tự động" else "Thủ công", color = Mint,
                                modifier = Modifier.clickable { automatic = !automatic }.padding(8.dp))
                        }
                        LinearProgressIndicator(progress = { (index + 1f) / phrases.size }, modifier = Modifier.fillMaxWidth(), color = Mint)
                        Panel {
                            Text("NGHE VÀ NHẮC LẠI", color = Mint, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(phrases[index].english, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            if (showTranslation) Text(phrases[index].vietnamese, color = Muted, textAlign = TextAlign.Center)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { slow = !slow }) { Text(if (slow) "Tốc độ 0.75x" else "Tốc độ 1.0x") }
                                TextButton(onClick = { showTranslation = !showTranslation }) { Text(if (showTranslation) "Ẩn dịch" else "Hiện dịch") }
                            }
                            PrimaryButton("🔊 Nghe mẫu") { speak(phrases[index].english) }
                            PrimaryButton("🎙 Nói và nhận dạng") { listen() }
                        }
                        if (transcript.isNotBlank()) {
                            Panel {
                                Text("Máy nhận dạng được:", color = Mint)
                                Text(transcript, fontWeight = FontWeight.SemiBold)
                                val expected = phrases[index].english.lowercase(Locale.US).replace(Regex("[^a-z ]"), "").trim()
                                val actual = transcript.lowercase(Locale.US).replace(Regex("[^a-z ]"), "").trim()
                                Text(if (expected == actual) "Nội dung khớp câu mẫu ✓" else "Nội dung khác câu mẫu. Thử nói lại nhé.", color = Muted)
                                Text("Không chấm điểm phát âm: nhận dạng chữ không đo được chất lượng âm thanh.", color = Muted, fontSize = 12.sp)
                                Text("Mẹo: ${phrases[index].tip}", color = Mint)
                            }
                        }
                        Text(status, color = Muted, fontSize = 13.sp)
                        PrimaryButton(if (index == phrases.lastIndex) "Hoàn thành • Sang hội thoại" else "Câu tiếp theo") { next() }
                    }
                    "updates" -> { UpdatesScreen() }
                    "chat" -> {
                        Panel {
                            Text("Emma • AI Tutor", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("DEMO KỊCH BẢN • CHƯA CÓ AI", color = Color(0xFFFFC86A), fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Emma: ${aiLines[chatIndex].first}", color = Mint, fontSize = 18.sp)
                            Text("Gợi ý trả lời: ${aiLines[chatIndex].second}", color = Muted)
                            PrimaryButton("🔊 Nghe Emma") { speak(aiLines[chatIndex].first) }
                            PrimaryButton("🎙 Nói câu trả lời") { listen() }
                            if (transcript.isNotBlank()) Text("Máy nhận dạng: $transcript", color = Color.White)
                            PrimaryButton("Câu hội thoại tiếp theo") {
                                chatIndex = (chatIndex + 1) % aiLines.size
                                transcript = ""
                                if (automatic) speak(aiLines[chatIndex].first)
                            }
                        }
                        Text("Không gửi bản ghi tới AI. Nhận dạng giọng nói có thể dùng dịch vụ của thiết bị.", color = Muted, fontSize = 12.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceBlue).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("home" to "Trang chủ", "repeat" to "Luyện nói", "chat" to "Hội thoại", "updates" to "Cập nhật").forEach { (id, label) ->
                    Text(label, color = if (page == id) Mint else Muted,
                        modifier = Modifier.clickable { page = id }.padding(10.dp), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(18.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        content = content
    )
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Violet)) {
        Text(label, modifier = Modifier.padding(vertical = 6.dp), color = Color.White)
    }
}
