package com.nghialcvn.speakivra

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.content.pm.PackageManager
import android.os.Bundle
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

private const val APP_VERSION = BuildConfig.VERSION_NAME

@Composable
private fun UpdatesScreen(state: UpdateState, installation: Installation, onCheck: () -> Unit,
                          onDownload: () -> Unit, onInstall: () -> Unit, onCancel: () -> Unit) {
    val p = studio
    StudioCard {
        Text("Cập nhật ứng dụng", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Phiên bản đang dùng: v$APP_VERSION", color = p.coral)
        Text("Nguồn chính thức: github.com/nghialcvn/Speakivra/releases", color = p.muted)
        Text(state.message, color = p.muted)
        if (state.phase in listOf("idle", "checking") && state.file == null) {
            CoralButton(if (state.checking) "Đang kiểm tra..." else "Kiểm tra bản cập nhật") {
                if (!state.checking) onCheck()
            }
        }
        state.release?.let { item ->
            Text("Bản phát hành: v${item.version}", fontWeight = FontWeight.SemiBold)
            Text("Có gì mới:", color = p.coral)
            Text(item.notes.ifBlank { "Chưa có ghi chú thay đổi." }, color = p.muted)
            when (state.phase) {
                "downloading" -> {
                    val percent = (state.bytes * 100 / item.size.coerceAtLeast(1)).coerceIn(0, 100)
                    LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth(), color = p.coral)
                    Text("Đang tải: $percent%", color = p.coral)
                    OutlinedButton(onClick = onCancel) { Text("Hủy tải") }
                }
                "verifying", "cancelling" -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = p.coral)
                "permission" -> CoralButton("Cho phép và cài cập nhật", onClick = onInstall)
                "downloaded" -> CoralButton("Cài cập nhật", onClick = onInstall)
                "idle" -> if (item.versionCode > installation.versionCode && item.canInstallOver(installation)) {
                    CoralButton("Tải và cập nhật", onClick = onDownload)
                } else Unit
                else -> Unit
            }
        }
        Text("Tải trực tiếp trong ứng dụng, kiểm tra APK và chữ ký rồi mở trình cài đặt Android. Bạn xác nhận cài đặt; không cần tìm file trong thư mục tải xuống. Không gỡ app để giữ tiến độ học.", color = p.muted, fontSize = 12.sp)
    }
}

class MainActivity : ComponentActivity() {
    private val updates: InAppUpdates by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedSystemBars()
        setContent { SpeakivraApp(updates) }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applySavedSystemBars()
    }
    private fun applySavedSystemBars() {
        val dark = getSharedPreferences("speakivra_progress", 0).getBoolean("dark_theme", false)
        updateSystemBars(this, dark, if (dark) DarkStudio.background else LightStudio.background)
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
    var automatic by rememberSaveable { mutableStateOf(preferences.getBoolean("automatic_mode", true)) }
    var showTranslation by rememberSaveable { mutableStateOf(true) }
    var playbackSpeed by rememberSaveable {
        val value = SpeechProfiles.initialSpeed(preferences.getInt("playback_speed", 100), preferences.getInt("speed_calibration", 0))
        preferences.edit().putInt("playback_speed", value).putInt("speed_calibration", 2).apply()
        mutableIntStateOf(value)
    }
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Nhấn Nghe mẫu để bắt đầu") }
    val speech = remember(context) { SpeechPlayback(context.applicationContext) { status = it } }
    var capturePhase by remember { mutableStateOf("idle") }
    var pendingMicrophone by remember { mutableStateOf(false) }
    val capture = remember(context) { SpeechCapture(context.applicationContext) { phase, words, message ->
        capturePhase = phase
        if (words != null) transcript = words
        status = message
    } }
    fun stopCapture() { pendingMicrophone = false; capture.cancel() }
    DisposableEffect(speech, capture, context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { stopCapture(); speech.stop(true) }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer); capture.cancel(); speech.close() }
    }
    fun speak(text: String) { stopCapture(); speech.speak(text, playbackSpeed) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val requested = pendingMicrophone
        pendingMicrophone = false
        if (granted && requested && (context as? LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) capture.start()
        else if (!granted) status = "Cần cấp quyền micro để sử dụng nhận dạng giọng nói."
    }
    fun listen() {
        if (capture.phase == "listening") { capture.stop(); return }
        if (capture.phase == "processing" || pendingMicrophone) return
        stopCapture(); speech.stop()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingMicrophone = true
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else capture.start()
    }
    // Changing the learning context invalidates pending permissions and old callbacks.
    DisposableEffect(page, lesson.id, progress.phrase, progress.turn, progress.review) {
        onDispose { stopCapture() }
    }
    fun next() {
        stopCapture(); speech.stop()
        saveProgress(progress.nextPhrase(lesson))
        transcript = ""
        page = progress.phase
        status = "Sẵn sàng học câu mới"
        if (automatic && page == "repeat") speak(phrases[progress.phrase].english)
    }
    fun openLesson(item: Lesson) {
        stopCapture(); speech.stop()
        transcript = ""
        status = "Nhấn Nghe mẫu để bắt đầu"
        selectedId = item.id
        preferences.edit().putString("active_lesson", item.id).apply()
        val saved = loadProgress(item)
        page = saved.phase
        if (automatic && page == "repeat") speak(item.phrases[saved.phrase].english)
    }
    var dark by rememberSaveable { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
    var goal by rememberSaveable { mutableIntStateOf(preferences.getInt("daily_goal", 20).takeIf { it in listOf(10, 15, 20, 30) } ?: 20) }
    var settingDialog by rememberSaveable { mutableStateOf("") }
    fun selectSpeed(value: Int) {
        playbackSpeed = value
        preferences.edit().putInt("playback_speed", value).apply()
    }
    fun navigate(destination: String) {
        stopCapture(); speech.stop()
        transcript = ""
        page = if (destination == "repeat") progress.phase else destination
    }
    BackHandler(enabled = page != "home") { navigate(if (page == "updates") "profile" else "home") }
    SpeakivraTheme(dark) {
        val palette = studio
        SideEffect { (context as? ComponentActivity)?.let { updateSystemBars(it, dark, palette.background) } }
        val available = updateState.release
        if (page == "home" && available != null && available.versionCode > installation.versionCode &&
            available.canInstallOver(installation) && dismissedUpdate != available.versionCode) {
            AlertDialog(onDismissRequest = { dismissedUpdate = available.versionCode },
                title = { Text("Speakivra v${available.version}") },
                text = {
                    Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                        Text("Có gì mới", fontWeight = FontWeight.Bold)
                        Text(available.notes.ifBlank { "Cải tiến và sửa lỗi ứng dụng." })
                    }
                },
                confirmButton = { TextButton(onClick = { dismissedUpdate = available.versionCode; beginUpdate() }) { Text("Cập nhật ngay") } },
                dismissButton = { TextButton(onClick = { dismissedUpdate = available.versionCode }) { Text("Để sau") } })
        }
        if (settingDialog.isNotBlank()) {
            val title = when (settingDialog) { "speed" -> "Tốc độ mặc định"; "goal" -> "Mục tiêu mỗi ngày"; else -> "Giọng mẫu" }
            AlertDialog(onDismissRequest = { settingDialog = "" }, title = { Text(title) }, text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (settingDialog) {
                        "speed" -> listOf(75, 100, 125).forEach { value ->
                            TextButton(onClick = { selectSpeed(value); settingDialog = "" }, modifier = Modifier.fillMaxWidth()) {
                                Text(speedLabel(value) + if (playbackSpeed == value) " ✓" else "")
                            }
                        }
                        "goal" -> listOf(10, 15, 20, 30).forEach { value ->
                            TextButton(onClick = { goal = value; preferences.edit().putInt("daily_goal", value).apply(); settingDialog = "" }, modifier = Modifier.fillMaxWidth()) {
                                Text("$value phút" + if (goal == value) " ✓" else "")
                            }
                        }
                        else -> Text("Anh-Mỹ • Âm thanh có sẵn trong ứng dụng. Giọng của thiết bị được dùng dự phòng nếu không phát được âm thanh mẫu.")
                    }
                }
            }, confirmButton = { TextButton(onClick = { settingDialog = "" }) { Text("Đóng") } })
        }
        Surface(color = palette.background, contentColor = palette.ink, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    // Each screen owns its scroll area; speaking/review keep the next action visible.
                    key(page, lesson.id, progress.phrase, progress.turn, progress.review) {
                        when (page) {
                            "home" -> HomeScreen(lesson, progress, goal, completed,
                                onContinue = { page = progress.phase; if (automatic && page == "repeat") speak(phrases[index].english) },
                                onLessons = { navigate("lessons") }, onChat = { navigate("chat") }, onProfile = { navigate("profile") })
                            "lessons" -> LessonsScreen(lesson.id, ::loadProgress, ::openLesson)
                            "repeat" -> PracticeScreen(lesson, progress, automatic, showTranslation, playbackSpeed, transcript, status, capturePhase,
                                onBack = { navigate("home") }, onMode = { automatic = it; preferences.edit().putBoolean("automatic_mode", it).apply() },
                                onSpeed = ::selectSpeed, onTranslation = { showTranslation = !showTranslation },
                                onListen = { speak(phrases[index].english) }, onSpeak = ::listen, onNext = ::next)
                            "chat" -> ConversationScreen(lesson, chatIndex, showTranslation, transcript, capturePhase, status,
                                nextLabel = if (chatIndex != lesson.conversation.lastIndex) "Câu hội thoại tiếp theo" else when (progress.phase) {
                                    "summary" -> "Về tổng kết"; "review" -> "Tiếp tục ôn tập"; else -> "Sang ôn tập"
                                }, onBack = { navigate("home") }, onPrompt = { speak(lesson.conversation[chatIndex].prompt) },
                                onReply = { speak(lesson.conversation[chatIndex].reply) }, onSpeak = ::listen,
                                onTranslation = { showTranslation = !showTranslation }, onNext = {
                                    stopCapture(); speech.stop()
                                    if (progress.phase in listOf("repeat", "chat")) {
                                        saveProgress(progress.copy(phase = "chat").nextTurn(lesson)); page = progress.phase
                                    } else if (chatIndex < lesson.conversation.lastIndex) saveProgress(progress.copy(turn = chatIndex + 1))
                                    else page = progress.phase
                                    transcript = ""
                                    if (automatic && page == "chat") speak(lesson.conversation[progress.turn].prompt)
                                })
                            "review" -> ReviewScreen(lesson, progress, reviewSelection, transcript, capturePhase, status,
                                onSelect = { if (reviewSelection < 0) reviewSelection = it }, onBack = { navigate("home") },
                                onListen = { speak(reviewQuestion(lesson, progress.review).phrase.english) }, onSpeak = ::listen,
                                onNext = {
                                    if (reviewSelection >= 0) {
                                        stopCapture(); speech.stop()
                                        val question = reviewQuestion(lesson, progress.review)
                                        val value = progress.answerReview(reviewSelection == question.answer)
                                        if (value.rounds > progress.rounds) completed++
                                        saveProgress(value); reviewSelection = -1; transcript = ""; page = progress.phase
                                    }
                                })
                            "summary" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PageHeading("Hoàn thành bài học", lessonLabel(lesson), onBack = { navigate("home") })
                                StudioCard(color = palette.mint, border = false) {
                                    Text(lesson.title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("${lesson.phrases.size} câu mẫu • ${lesson.conversation.size} lượt hội thoại • 3 câu ôn tập")
                                    Text("Kết quả ôn nghĩa: ${progress.correct}/3", fontWeight = FontWeight.Bold)
                                    Text("Tiến độ đã lưu trên thiết bị. Bạn có thể học lại bất cứ lúc nào.", color = palette.muted)
                                }
                                CoralButton("Học lại bài này", onClick = {
                                    stopCapture(); speech.stop(); saveProgress(progress.restart()); transcript = ""; page = "repeat"
                                    if (automatic) speak(phrases[0].english)
                                })
                                val following = Lessons.all.getOrNull(Lessons.all.indexOf(lesson) + 1)
                                if (following != null) CoralButton("Bài tiếp theo: ${lessonLabel(following)}", onClick = { openLesson(following) })
                                TextButton(onClick = { navigate("lessons") }) { Text("Về danh sách bài học") }
                            }
                            "profile" -> ProfileScreen(dark, playbackSpeed, goal, APP_VERSION,
                                onTheme = { dark = it; preferences.edit().putBoolean("dark_theme", it).apply() },
                                onVoice = { settingDialog = "voice" }, onSpeed = { settingDialog = "speed" },
                                onGoal = { settingDialog = "goal" }, onUpdates = { page = "updates"; updates.check() })
                            "updates" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PageHeading("Cập nhật ứng dụng", onBack = { navigate("profile") })
                                UpdatesScreen(updateState, installation, updates::check, ::beginUpdate, ::requestInstall, updates::cancel)
                            }
                        }
                    }
                }
                if (page != "repeat") StudioBottomNavigation(page, ::navigate)
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun updateSystemBars(activity: ComponentActivity, dark: Boolean, color: Color) {
    val transparent = android.graphics.Color.TRANSPARENT
    val style = if (dark) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
    activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    activity.window.statusBarColor = color.toArgb()
    activity.window.navigationBarColor = color.toArgb()
    WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
        isAppearanceLightStatusBars = !dark
        isAppearanceLightNavigationBars = !dark
    }
}
