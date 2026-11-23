package com.nghialcvn.speakivra

import android.Manifest
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.viewModels
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
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CancellationException
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


private const val APP_VERSION = BuildConfig.VERSION_NAME

@Composable
private fun UpdatesScreen(state: UpdateState, installation: Installation, onCheck: () -> Unit,
                          onDownload: () -> Unit, onInstall: () -> Unit, onCancel: () -> Unit) {
    Panel {
        Text("Cập nhật ứng dụng", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Phiên bản đang dùng: v$APP_VERSION", color = Mint)
        Text("Nguồn chính thức: github.com/nghialcvn/Speakivra/releases", color = Muted)
        Text(state.message, color = Muted)
        if (state.phase in listOf("idle", "checking") && state.file == null) {
            PrimaryButton(if (state.checking) "Đang kiểm tra..." else "Kiểm tra bản cập nhật") {
                if (!state.checking) onCheck()
            }
        }
        state.release?.let { item ->
            Text("Bản phát hành: v${item.version}", fontWeight = FontWeight.SemiBold)
            Text("Có gì mới:", color = Mint)
            Text(item.notes.ifBlank { "Chưa có ghi chú thay đổi." }, color = Muted)
            when (state.phase) {
                "downloading" -> {
                    val percent = (state.bytes * 100 / item.size.coerceAtLeast(1)).coerceIn(0, 100)
                    LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth(), color = Mint)
                    Text("Đang tải: $percent%", color = Mint)
                    OutlinedButton(onClick = onCancel) { Text("Hủy tải") }
                }
                "verifying", "cancelling" -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Mint)
                "permission" -> PrimaryButton("Cho phép và cài cập nhật", onInstall)
                "downloaded" -> PrimaryButton("Cài cập nhật", onInstall)
                "idle" -> if (item.versionCode > installation.versionCode && item.canInstallOver(installation)) {
                    PrimaryButton("Tải và cập nhật", onDownload)
                } else Unit
                else -> Unit
            }
        }
        Text("Tải trực tiếp trong ứng dụng, kiểm tra APK và chữ ký rồi mở trình cài đặt Android. Bạn xác nhận cài đặt; không cần tìm file trong thư mục tải xuống. Không gỡ app để giữ tiến độ học.", color = Muted, fontSize = 12.sp)
    }
}

class MainActivity : ComponentActivity() {
    private val updates: InAppUpdates by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SpeakivraApp(updates) }
    }
}

@Composable
private fun SpeakivraApp(updates: InAppUpdates) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("speakivra_progress", 0) }
    var page by rememberSaveable { mutableStateOf("home") }
    val installation = updates.installation
    val updateState = updates.state
    val updateScope = rememberCoroutineScope()
    var dismissedUpdate by rememberSaveable { mutableLongStateOf(-1L) }
    val installLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updates.installReturned()
    }
    fun launchInstaller() {
        updateScope.launch {
            try { installLauncher.launch(updates.prepareInstall()) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (updates.state.file != null) updates.installReturned("Không mở được trình cài đặt. Bấm Cài cập nhật để thử lại.")
            }
        }
    }
    val installPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) launchInstaller()
        else updates.waitingForPermission()
    }
    fun requestInstall() {
        page = "updates"
        if (updateState.file == null) return
        if (context.packageManager.canRequestPackageInstalls()) launchInstaller()
        else {
            updates.waitingForPermission()
            try { installPermissionLauncher.launch(updates.permissionIntent()) }
            catch (_: ActivityNotFoundException) { updates.installReturned("Không mở được cài đặt quyền. Vào Cài đặt Android → Cài ứng dụng không rõ nguồn → Speakivra.") }
            catch (_: SecurityException) { updates.waitingForPermission() }
        }
    }
    fun beginUpdate() { page = "updates"; updates.download() }
    LaunchedEffect(Unit) { updates.checkOnce() }
    LaunchedEffect(updateState.phase) { if (updateState.phase == "ready") requestInstall() }
    var index by remember { mutableIntStateOf(preferences.getInt("phrase", 0).coerceIn(0, phrases.lastIndex)) }
    var completed by remember { mutableIntStateOf(preferences.getInt("completed", 0)) }
    var automatic by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    var playbackSpeed by rememberSaveable {
        mutableIntStateOf(preferences.getInt("playback_speed", 100).takeIf { it in listOf(75, 100, 125) } ?: 100)
    }
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
            // Keep the former 1x voice at the new 0.75x setting.
            // Both lesson samples and scripted conversation use this calibrated rate.
            tts.setSpeechRate(playbackSpeed / 75f)
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
        val available = updateState.release
        if (page == "home" && available != null && available.versionCode > installation.versionCode &&
            available.canInstallOver(installation) && dismissedUpdate != available.versionCode) {
            AlertDialog(
                onDismissRequest = { dismissedUpdate = available.versionCode },
                title = { Text("Speakivra v${available.version}") },
                text = {
                    Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                        Text("Có gì mới", fontWeight = FontWeight.Bold)
                        Text(available.notes.ifBlank { "Cải tiến và sửa lỗi ứng dụng." })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        dismissedUpdate = available.versionCode
                        beginUpdate()
                    }) { Text("Cập nhật ngay") }
                },
                dismissButton = {
                    TextButton(onClick = { dismissedUpdate = available.versionCode }) { Text("Để sau") }
                }
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().background(Navy).windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("SPEAKIVRA", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Listen. Speak. Improve.", color = Muted, fontSize = 12.sp)
                }
                Text("V$APP_VERSION • BETA", color = Mint, fontSize = 12.sp)
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
                                TextButton(onClick = {
                                    playbackSpeed = when (playbackSpeed) { 75 -> 100; 100 -> 125; else -> 75 }
                                    preferences.edit().putInt("playback_speed", playbackSpeed).apply()
                                }) {
                                    Text("Tốc độ " + when (playbackSpeed) { 75 -> "0.75x"; 125 -> "1.25x"; else -> "1x" })
                                }
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
                    "updates" -> { UpdatesScreen(updateState, installation, updates::check, ::beginUpdate, ::requestInstall, updates::cancel) }
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
