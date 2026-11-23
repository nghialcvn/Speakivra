package com.nghialcvn.speakivra

import android.Manifest
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.activity.viewModels
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
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
    // Existing v0.1.7 keys remain intact; import the first lesson position once.
    fun loadProgress(item: Lesson) = restoreLessonProgress(item,
        { key, default -> preferences.getInt(key, default) },
        { key, default -> preferences.getString(key, default) ?: default })
    var selectedId by rememberSaveable { mutableStateOf(preferences.getString("active_lesson", "greetings") ?: "greetings") }
    val lesson = Lessons.all.firstOrNull { it.id == selectedId } ?: Lessons.all.first()
    var progress by remember(lesson.id) { mutableStateOf(loadProgress(lesson)) }
    val index = progress.phrase
    val phrases = lesson.phrases
    val chatIndex = progress.turn
    var completed by remember { mutableIntStateOf(preferences.getInt("completed", 0).coerceAtLeast(0) + Lessons.all.sumOf { loadProgress(it).rounds }) }
    var reviewSelection by rememberSaveable(lesson.id, progress.review) { mutableIntStateOf(-1) }
    fun saveProgress(value: LessonProgress) {
        progress = value.sanitized(lesson)
        val prefix = "lesson.${lesson.id}."
        preferences.edit().putInt(prefix + "phrase", progress.phrase).putInt(prefix + "turn", progress.turn)
            .putInt(prefix + "review", progress.review).putInt(prefix + "correct", progress.correct)
            .putInt(prefix + "rounds", progress.rounds).putString(prefix + "phase", progress.phase).apply()
    }
    var automatic by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    var playbackSpeed by rememberSaveable {
        val value = SpeechProfiles.initialSpeed(preferences.getInt("playback_speed", 100), preferences.getInt("speed_calibration", 0))
        preferences.edit().putInt("playback_speed", value).putInt("speed_calibration", 2).apply()
        mutableIntStateOf(value)
    }
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Nhấn Nghe mẫu để bắt đầu") }
    val speech = remember(context) { SpeechPlayback(context.applicationContext) { status = it } }
    DisposableEffect(speech, context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) speech.stop(true)
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer); speech.close() }
    }
    fun speak(text: String) { speech.speak(text, playbackSpeed) }
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
        speech.stop()
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
        speech.stop()
        saveProgress(progress.nextPhrase(lesson))
        transcript = ""
        page = progress.phase
        status = "Sẵn sàng học câu mới"
        if (automatic && page == "repeat") speak(phrases[progress.phrase].english)
    }
    fun openLesson(item: Lesson) {
        speech.stop()
        transcript = ""
        status = "Nhấn Nghe mẫu để bắt đầu"
        selectedId = item.id
        preferences.edit().putString("active_lesson", item.id).apply()
        val saved = loadProgress(item)
        page = saved.phase
        if (automatic && page == "repeat") speak(item.phrases[saved.phrase].english)
    }
    val contentScroll = rememberScrollState()
    LaunchedEffect(page, lesson.id, progress.phrase, progress.turn, progress.review) { contentScroll.scrollTo(0) }
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
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(contentScroll).padding(horizontal = 16.dp),
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
                            Text("Bài ${Lessons.all.indexOf(lesson) + 1}: ${lesson.title}", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(lesson.vietnamese, color = Muted)
                            Text("Câu hiện tại: ${index + 1}/${phrases.size}", color = Muted)
                            PrimaryButton(if (progress.phase == "summary") "Xem tổng kết bài" else "Tiếp tục bài học") { page = progress.phase; if (automatic && page == "repeat") speak(phrases[index].english) }
                        }
                        Panel {
                            Text("AI Tutor (mô phỏng)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Chưa kết nối API, hội thoại theo kịch bản cố định.", color = Muted)
                            PrimaryButton("Mở hội thoại mẫu") { page = "chat" }
                        }
                        Panel {
                            Text("10 CHỦ ĐỀ A1", color = Mint, fontWeight = FontWeight.Bold)
                            Lessons.all.forEachIndexed { position, item ->
                                val saved = loadProgress(item)
                                OutlinedButton(onClick = { openLesson(item) }, modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Text("${position + 1}. ${item.title}", color = if (item.id == lesson.id) Mint else Color.White)
                                        Text("${item.vietnamese} • " + if (saved.rounds > 0) "Đã học ${saved.rounds} lượt" else "Câu ${saved.phrase + 1}/${item.phrases.size}", color = Muted, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        Text("Đã hoàn thành: $completed lượt bài • Lưu trên thiết bị", color = Mint)
                    }
                    "repeat" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${lesson.title} • ${index + 1}/${phrases.size}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
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
                        Text("Mẹo luyện âm: ${phrases[index].tip}", color = Mint, fontSize = 13.sp)
                        if (transcript.isNotBlank()) {
                            Panel {
                                Text("Máy nhận dạng được:", color = Mint)
                                Text(transcript, fontWeight = FontWeight.SemiBold)
                                val expected = phrases[index].english.lowercase(Locale.US).replace(Regex("[^a-z ]"), "").trim()
                                val actual = transcript.lowercase(Locale.US).replace(Regex("[^a-z ]"), "").trim()
                                Text(if (expected == actual) "Nội dung khớp câu mẫu ✓" else "Nội dung khác câu mẫu. Thử nói lại nhé.", color = Muted)
                                Text("Không chấm điểm phát âm: nhận dạng chữ không đo được chất lượng âm thanh.", color = Muted, fontSize = 12.sp)
                            }
                        }
                        Text(status, color = Muted, fontSize = 13.sp)
                        PrimaryButton(if (index == phrases.lastIndex) "Hoàn thành • Sang hội thoại" else "Câu tiếp theo") { next() }
                    }
                    "review" -> {
                        val question = reviewQuestion(lesson, progress.review)
                        Text("${lesson.title} • Ôn tập ${progress.review + 1}/3", color = Mint)
                        Panel {
                            Text("NGHE VÀ HIỂU", color = Mint, fontSize = 12.sp)
                            Text(question.phrase.english, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            PrimaryButton("🔊 Nghe lại câu") { speak(question.phrase.english) }
                            Text("Chọn nghĩa phù hợp:", color = Muted)
                            question.options.forEachIndexed { choice, label ->
                                OutlinedButton(onClick = { if (reviewSelection == -1) reviewSelection = choice },
                                    modifier = Modifier.fillMaxWidth(), enabled = reviewSelection == -1) { Text(label) }
                            }
                            if (reviewSelection != -1) {
                                Text(if (reviewSelection == question.answer) "Đúng rồi ✓" else "Đáp án: ${question.phrase.vietnamese}", color = Mint)
                                Text(question.phrase.tip, color = Muted)
                                PrimaryButton("🎙 Nhắc lại câu này") { listen() }
                                if (transcript.isNotBlank()) Text("Máy nhận dạng: $transcript", color = Muted)
                                PrimaryButton(if (progress.review == 2) "Hoàn thành và tổng kết" else "Câu ôn tập tiếp theo") {
                                    speech.stop()
                                    val nextProgress = progress.answerReview(reviewSelection == question.answer)
                                    if (nextProgress.rounds > progress.rounds) completed++
                                    saveProgress(nextProgress)
                                    reviewSelection = -1
                                    transcript = ""
                                    page = progress.phase
                                }
                            }
                        }
                        Text("Ôn nghĩa câu, không phải bài chấm điểm phát âm.", color = Muted, fontSize = 12.sp)
                    }
                    "summary" -> {
                        Panel {
                            Text("Hoàn thành: ${lesson.title}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("${lesson.phrases.size} câu mẫu • ${lesson.conversation.size} lượt hội thoại • 3 câu ôn tập", color = Mint)
                            Text("Kết quả ôn nghĩa: ${progress.correct}/3", color = Muted)
                            Text("Tiến độ đã lưu trên thiết bị. Bạn có thể học lại bất cứ lúc nào.", color = Muted)
                            PrimaryButton("Học lại bài này") {
                                speech.stop(); saveProgress(progress.restart()); transcript = ""; page = "repeat"
                                if (automatic) speak(phrases[0].english)
                            }
                            val nextLesson = Lessons.all.getOrNull(Lessons.all.indexOf(lesson) + 1)
                            if (nextLesson != null) PrimaryButton("Bài tiếp theo: ${nextLesson.title}") { openLesson(nextLesson) }
                            PrimaryButton("Về danh sách bài học") { page = "home" }
                        }
                    }
                    "updates" -> { UpdatesScreen(updateState, installation, updates::check, ::beginUpdate, ::requestInstall, updates::cancel) }
                    "chat" -> {
                        val turn = lesson.conversation[chatIndex]
                        Text("${lesson.title} • Hội thoại ${chatIndex + 1}/${lesson.conversation.size}", color = Mint)
                        Panel {
                            Text("Emma • AI Tutor", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("DEMO KỊCH BẢN • CHƯA CÓ AI", color = Color(0xFFFFC86A), fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Emma: ${turn.prompt}", color = Mint, fontSize = 18.sp)
                            if (showTranslation) Text(turn.translation, color = Muted)
                            Text("Gợi ý trả lời: ${turn.reply}", color = Muted)
                            if (showTranslation) Text(turn.replyTranslation, color = Muted)
                            PrimaryButton("🔊 Nghe Emma") { speak(turn.prompt) }
                            TextButton(onClick = { speak(turn.reply) }) { Text("Nghe câu trả lời mẫu") }
                            TextButton(onClick = { showTranslation = !showTranslation }) { Text(if (showTranslation) "Ẩn dịch" else "Hiện dịch") }
                            PrimaryButton("🎙 Nói câu trả lời") { listen() }
                            if (transcript.isNotBlank()) Text("Máy nhận dạng: $transcript", color = Color.White)
                            PrimaryButton(if (chatIndex != lesson.conversation.lastIndex) "Câu hội thoại tiếp theo" else if (progress.phase == "summary") "Về tổng kết" else if (progress.phase == "review") "Tiếp tục ôn tập" else "Sang ôn tập") {
                                speech.stop()
                                if (progress.phase in listOf("repeat", "chat")) {
                                    saveProgress(progress.copy(phase = "chat").nextTurn(lesson))
                                    page = progress.phase
                                } else if (chatIndex < lesson.conversation.lastIndex) {
                                    saveProgress(progress.copy(turn = chatIndex + 1))
                                } else {
                                    page = progress.phase
                                }
                                transcript = ""
                                if (automatic && page == "chat") speak(lesson.conversation[progress.turn].prompt)
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
                        modifier = Modifier.clickable {
                            speech.stop(); transcript = ""
                            page = if (id == "repeat") progress.phase else id
                        }.padding(10.dp), fontSize = 13.sp)
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
