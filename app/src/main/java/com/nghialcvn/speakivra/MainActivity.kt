package com.nghialcvn.speakivra

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.core.view.WindowCompat
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
            releaseHighlights(item.notes).forEach { Text("• $it", color = p.muted) }
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
        if (state.release == null && !state.checking) {
            val context = LocalContext.current
            OutlinedButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_URL))
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                runCatching { context.startActivity(intent) }
            }) { Text("Mở GitHub Releases để tải thủ công") }
        }
        Text("Cài đè bản mới để giữ hồ sơ và tiến độ.", color = p.muted, fontSize = 12.sp)
    }
}

class MainActivity : ComponentActivity() {
    private val updates: InAppUpdates by viewModels()
    private var updateRequested by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedSystemBars()
        updateRequested = intent.getBooleanExtra(UpdateNotifications.OPEN_UPDATES, false)
        UpdateNotifications.schedule(this)
        setContent { SpeakivraApp(updates, updateRequested) { updateRequested = false } }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateRequested = intent.getBooleanExtra(UpdateNotifications.OPEN_UPDATES, false)
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applySavedSystemBars()
    }
    private fun applySavedSystemBars() {
        val prefs = getSharedPreferences("speakivra_progress", 0)
        val dark = prefs.getBoolean("dark_theme", false)
        val neon = prefs.getBoolean("neon_theme", false)
        val palette = if (neon) NeonStudio else if (dark) DarkStudio else LightStudio
        updateSystemBars(this, dark || neon, palette.background)
    }
}

@Composable
private fun SpeakivraApp(updates: InAppUpdates, updateRequested: Boolean, onUpdateHandled: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { LearnerStore(context.applicationContext) }
    val cloud = remember(context) { AccountRuntime.get(context) }
    val cloudRevision by cloud.revision.collectAsState()
    var learners by remember { mutableStateOf(store.visible(cloud.uid)) }
    LaunchedEffect(cloudRevision) { learners = store.visible(cloud.uid) }
    var activeId by rememberSaveable { mutableStateOf<String?>(null) }
    var lastId by rememberSaveable { mutableStateOf<String?>(null) }
    var accountEntry by rememberSaveable { mutableStateOf(false) }
    var linkOnEntry by rememberSaveable { mutableStateOf(false) }
    var accountAuthScreen by remember { mutableStateOf(true) }
    var entryPage by rememberSaveable { mutableStateOf("home") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var resumeLearning by rememberSaveable { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        updates.check()
    }
    LaunchedEffect(Unit) {
        val settings = context.getSharedPreferences("speakivra_update_notifications", 0)
        if (android.os.Build.VERSION.SDK_INT >= 33 && !StudyReminders.hasPermission(context) &&
            !settings.getBoolean("permission_requested", false)) {
            settings.edit().putBoolean("permission_requested", true).apply()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        StudyReminders.rescheduleIfNeeded(context)
        updates.checkOnce()
    }
    val available = learners.filter { GuestIdentityStore(context).peek(it)?.canOpen(cloud.uid) != false }
    val active = available.firstOrNull { it.id == activeId }
    DisposableEffect(active?.id, cloud.uid) {
        val person = active
        val listener = person?.let { cloud.watch(it) }
        onDispose { if (person != null && listener != null) store.preferences(person).unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val avatars = ImageBitmap.imageResource(R.drawable.learner_avatars)
    CompositionLocalProvider(LocalLearnerArtwork provides avatars) {
        if (active != null) {
            key(active.id) {
                LearnerApp(updates, active, resumeLearning, updateRequested, onUpdateHandled, initialPage = entryPage, onSwitchLearner = { lastId = active.id; activeId = null }, onChooseLearner = { selected -> entryPage = "home"; activeId = selected.id; lastId = selected.id; resumeLearning = false }, onUpdateLearner = { name, avatar ->
                    try { store.update(active.id, name, avatar); learners = store.visible(cloud.uid); null }
                    catch (e: IllegalArgumentException) { e.message ?: "Tên không hợp lệ. Hãy thử lại." }
                    catch (_: Exception) { "Chưa lưu được hồ sơ. Hãy thử lại." }
                })
            }
        } else {
            val prefs = context.getSharedPreferences("speakivra_progress", 0)
            val dark = prefs.getBoolean("dark_theme", false)
            val neon = prefs.getBoolean("neon_theme", false)
            SpeakivraTheme(dark, neon = neon) {
                val palette = studio
                SideEffect { (context as? ComponentActivity)?.let { updateSystemBars(it, dark || neon, palette.background) } }
                Surface(color = if (neon) Color.Transparent else palette.background,
                    contentColor = palette.ink) {
                    Box(Modifier.fillMaxSize()) {
                        if (neon) NeonWaveBackground(Modifier.matchParentSize())
                        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                            if (accountEntry) Column(Modifier.fillMaxSize()) {
                                Box(Modifier.weight(1f)) {
                                    AccountFlow(available.firstOrNull { it.id == lastId } ?: available.firstOrNull(), linkOnEntry = linkOnEntry,
                                        onBack = { accountEntry = false }, onAuthScreen = { accountAuthScreen = it },
                                        onChooseLearner = { selected -> accountEntry = false; entryPage = "home"; activeId = selected.id; lastId = selected.id; resumeLearning = false })
                                }
                                if (!accountAuthScreen) StudioBottomNavigation("profile") { destination ->
                                    val selected = available.firstOrNull { it.id == lastId } ?: available.firstOrNull()
                                    if (selected != null) {
                                        entryPage = destination; activeId = selected.id; lastId = selected.id; resumeLearning = false
                                    }
                                    accountEntry = false
                                }
                            }
                            else if (creating) CreateLearnerScreen(onBack = { creating = false }, onSave = { name, avatar ->
                            try {
                                val learner = store.add(name, avatar)
                                learners = store.visible(cloud.uid); creating = false; entryPage = "home"; activeId = learner.id; lastId = learner.id
                                null
                            } catch (e: IllegalArgumentException) { e.message ?: "Tên không hợp lệ. Hãy thử lại." }
                            catch (_: Exception) { "Chưa lưu được hồ sơ. Hãy thử lại." }
                            }) else LearnerPicker(store, available, lastId,
                                onAdd = { creating = true }, onAccount = { linkOnEntry = false; accountAuthScreen = true; accountEntry = true },
                                onLink = { lastId = it.id; linkOnEntry = true; accountAuthScreen = true; accountEntry = true },
                                onStart = { entryPage = "home"; resumeLearning = false; activeId = it.id; lastId = it.id },
                                onResume = { entryPage = "home"; resumeLearning = true; activeId = it.id; lastId = it.id })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnerApp(updates: InAppUpdates, learner: Learner, resumeLearning: Boolean, updateRequested: Boolean, onUpdateHandled: () -> Unit, onSwitchLearner: () -> Unit, onChooseLearner: (Learner) -> Unit, onUpdateLearner: (String, Int) -> String?, initialPage: String = "home") {
    val context = LocalContext.current
    val preferences = remember(learner.id) { context.getSharedPreferences(learnerPreferenceName(learner.id), 0) }
    val appearance = remember { context.getSharedPreferences("speakivra_progress", 0) }
    var accountAuthScreen by remember { mutableStateOf(true) }
    var page by rememberSaveable { mutableStateOf(if (resumeLearning) {
        val item = Lessons.catalogue.firstOrNull { it.id == preferences.getString("active_lesson", "greetings") } ?: Lessons.all.first()
        restoreLessonProgress(item, preferences::getInt) { key, default -> preferences.getString(key, default) ?: default }.phase
    } else initialPage) }
    LaunchedEffect(updateRequested) {
        if (updateRequested) { page = "updates"; updates.check(); onUpdateHandled() }
    }
    var selectedLevel by rememberSaveable { mutableStateOf("A1") }
    var hotelRole by rememberSaveable { mutableStateOf("FO") }
    var hotelMode by rememberSaveable { mutableStateOf("repeat") }
    var hotelDirectChat by rememberSaveable { mutableStateOf(false) }
    var hotelChatTurn by rememberSaveable { mutableIntStateOf(0) }
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
    val learnerLessons = remember(learner.name) { Lessons.catalogue.map { personalizeLesson(it, learner.name) } }
    val lesson = learnerLessons.firstOrNull { it.id == selectedId && topicIsOpen(it, learnerLessons, ::loadProgress) } ?: learnerLessons.first()
    var progress by remember(lesson.id) { mutableStateOf(loadProgress(lesson)) }
    val achievements = remember(learner.id) { TopicAchievements(preferences) }
    val index = progress.phrase
    val phrases = lesson.phrases
    val chatIndex = if (HotelCurriculum.isHotel(lesson) && hotelDirectChat) hotelChatTurn else progress.turn
    var completed by remember { mutableIntStateOf(preferences.getInt("completed", 0).coerceAtLeast(0) + learnerLessons.sumOf { loadProgress(it).rounds }) }
    var reviewSelection by rememberSaveable(lesson.id, progress.review) { mutableIntStateOf(-1) }
    fun saveProgress(value: LessonProgress) {
        progress = value.sanitized(lesson)
        val prefix = "lesson.${lesson.id}."
        preferences.edit().putInt(prefix + "phrase", progress.phrase).putInt(prefix + "turn", progress.turn)
            .putInt(prefix + "review", progress.review).putInt(prefix + "correct", progress.correct)
            .putInt(prefix + "rounds", progress.rounds).putString(prefix + "phase", progress.phase)
            .putInt(prefix + "supplemental", if (progress.supplemental) 1 else 0)
            .putString(prefix + "learned_phrases", progress.learnedPhrases.orEmpty().joinToString("|"))
            .putString(prefix + "learned_turns", progress.learnedTurns.orEmpty().joinToString("|")).apply()
    }
    var automatic by rememberSaveable { mutableStateOf(preferences.getBoolean("automatic_mode", true)) }
    var showTranslation by rememberSaveable { mutableStateOf(true) }
    var playbackSpeed by rememberSaveable {
        val value = SpeechProfiles.initialSpeed(preferences.getInt("playback_speed", 100), preferences.getInt("speed_calibration", 0))
        preferences.edit().putInt("playback_speed", value).putInt("speed_calibration", 2).apply()
        mutableIntStateOf(value)
    }
    var voiceChoice by remember(learner.id) { mutableStateOf(VoiceChoices.read(preferences)) }
    var explanationLanguage by rememberSaveable(learner.id) {
        mutableStateOf(LearningOptions.language(preferences))
    }
    var sampleRepeats by rememberSaveable(learner.id) {
        mutableIntStateOf(LearningOptions.repeats(preferences))
    }
    var reminderEnabled by remember { mutableStateOf(StudyReminders.isEnabled(context)) }
    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) runCatching { StudyReminders.setEnabled(context, true) }
            .onSuccess { reminderEnabled = true }
        else reminderEnabled = false
    }
    fun toggleReminder(enabled: Boolean) {
        if (!enabled) {
            StudyReminders.setEnabled(context, false)
            reminderEnabled = false
        } else if (StudyReminders.hasPermission(context)) {
            StudyReminders.setEnabled(context, true)
            reminderEnabled = true
        } else {
            reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    var extraLevel by rememberSaveable { mutableStateOf("A1") }
    var passageId by rememberSaveable { mutableStateOf(ExtraPracticeData.passages.first().id) }
    var sentenceIndex by rememberSaveable { mutableIntStateOf(0) }
    var wholePassage by rememberSaveable { mutableStateOf(false) }
    var exerciseId by rememberSaveable { mutableStateOf(ExtraPracticeData.exercises.first().id) }
    var questionIndex by rememberSaveable { mutableIntStateOf(0) }
    var extraCompleted by remember(learner.id) {
        mutableStateOf(preferences.getStringSet("extra.completed_ids", emptySet()).orEmpty().toSet())
    }
    fun completeExtra(ids: Collection<String>) {
        extraCompleted = extraCompleted + ids
        preferences.edit().putStringSet("extra.completed_ids", extraCompleted).apply()
        HotelCurriculum.all.forEach { hotelLesson ->
            val learned = hotelLesson.phrases.filter { "exercise_${it.id}" in ids }.map { it.id }
            if (learned.isNotEmpty()) {
                val key = "lesson.${hotelLesson.id}.learned_phrases"
                val prior = preferences.getString(key, "").orEmpty().split('|').filter { it.isNotBlank() }.toSet()
                preferences.edit().putString(key, (prior + learned).joinToString("|")).apply()
            }
        }
    }
    val passage = ExtraPracticeData.passages.first { it.id == passageId }
    val sentence = passage.sentences[sentenceIndex.coerceIn(passage.sentences.indices)]
    val exercise = (ExtraPracticeData.exercises + HotelCurriculum.exercises).first { it.id == exerciseId }
    val question = exercise.questions[questionIndex.coerceIn(exercise.questions.indices)]
    LaunchedEffect(page, passageId, exerciseId) {
        if (page == "passage" && !extraTopicOpen(passage.lessonId, ::loadProgress)) page = "passages"
        if (page == "exercise" && !extraTopicOpen(exercise.lessonId, ::loadProgress)) page = "exercises"
    }
    var voiceReturnPage by rememberSaveable { mutableStateOf("profile") }
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Nhấn Nghe mẫu để bắt đầu") }
    var finalTranscript by remember(page, learner.name, voiceChoice.accent, voiceChoice.gender, voiceChoice.voiceId, lesson.id, progress.phrase, progress.turn, progress.review, hotelChatTurn, passageId, sentenceIndex, wholePassage, exerciseId, questionIndex) { mutableStateOf<String?>(null) }
    val wordMatch = matchWords(phrases[index].english, finalTranscript)
    val speech = remember(context) { SpeechPlayback(context.applicationContext) { status = it } }
    var capturePhase by remember { mutableStateOf("idle") }
    var pendingMicrophone by remember { mutableStateOf(false) }
    val captureUpdate by rememberUpdatedState(newValue = { phase: String, words: String?, message: String ->
        capturePhase = phase
        if (words != null) {
            transcript = words
            if (phase == "idle") {
                finalTranscript = words.takeIf { it.isNotBlank() }
                if (page == "repeat" && finalTranscript != null)
                    achievements.record(lesson, phrases[index].id, matchWords(phrases[index].english, finalTranscript))
                if (page == "chat" && HotelCurriculum.isHotel(lesson) && finalTranscript != null) {
                    val reply = lesson.conversation[chatIndex].reply
                    lesson.phrases.firstOrNull { it.english == reply }?.let { phrase ->
                        achievements.record(lesson, phrase.id, matchWords(phrase.english, finalTranscript))
                    }
                }
                if (page == "exercise" && finalTranscript != null) {
                    HotelCurriculum.all.firstOrNull { it.id == exercise.lessonId }?.let { hotelLesson ->
                        hotelLesson.phrases.firstOrNull { "exercise_${it.id}" == question.id }?.let { phrase ->
                            achievements.record(hotelLesson, phrase.id, matchWords(phrase.english, finalTranscript))
                        }
                    }
                }
            }
        }
        status = message
    })
    var hasRecording by remember { mutableStateOf(false) }
    val capture = remember(context) { SpeechCapture(context.applicationContext,
        recordingChanged = { hasRecording = it }) { phase, words, message -> captureUpdate(phase, words, message) } }
    fun stopCapture(discardRecording: Boolean = true) { pendingMicrophone = false; capture.cancel(discardRecording) }
    DisposableEffect(speech, capture, context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { stopCapture(); speech.stop(true) }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer); capture.cancel(); speech.close() }
    }
    fun speak(text: String) { stopCapture(false); speech.speak(text, playbackSpeed, voiceChoice, sampleRepeats) }
    fun captureBudget(): Int {
        val expected = when (page) {
            "passage" -> if (wholePassage) passage.english else sentence.english
            "exercise" -> question.answer
            "chat" -> lesson.conversation[chatIndex].reply
            else -> phrases[index].english
        }
        return speechAttemptLimitMillis(page == "passage" && wholePassage, expected.split(Regex("\\s+")).size)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val requested = pendingMicrophone
        pendingMicrophone = false
        if (granted && requested && (context as? LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) capture.start(voiceChoice.languageTag, if (page == "passage" && wholePassage) 2400 else 1400, learner.name, captureBudget())
        else if (!granted) status = "Cần cấp quyền micro để sử dụng nhận dạng giọng nói."
    }
    fun listen() {
        if (page == "passage" && !extraTopicOpen(passage.lessonId, ::loadProgress)) return
        if (page == "exercise" && !extraTopicOpen(exercise.lessonId, ::loadProgress)) return
        if (capture.phase == "listening") { capture.stop(); return }
        if (capture.phase == "processing" || pendingMicrophone) return
        stopCapture(); speech.stop()
        finalTranscript = null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingMicrophone = true
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else capture.start(voiceChoice.languageTag, if (page == "passage" && wholePassage) 2400 else 1400, learner.name, captureBudget())
    }
    // Changing the learning context invalidates pending permissions and old callbacks.
    DisposableEffect(page, voiceChoice, lesson.id, progress.phrase, progress.turn, progress.review, hotelChatTurn, passageId, sentenceIndex, wholePassage, exerciseId, questionIndex) {
        onDispose { stopCapture() }
    }
    fun clearExtra() {
        stopCapture(); speech.stop(); transcript = ""; finalTranscript = null; status = ""
    }
    fun openPassage(item: ReadingPassage) {
        if (!extraTopicOpen(item.lessonId, ::loadProgress)) return
        clearExtra(); passageId = item.id; wholePassage = false
        sentenceIndex = item.sentences.indexOfFirst { it.id !in extraCompleted }.coerceAtLeast(0)
        page = "passage"
    }
    fun openExercise(item: SpeakingExercise) {
        if (!extraTopicOpen(item.lessonId, ::loadProgress)) return
        clearExtra(); exerciseId = item.id
        questionIndex = nextUnfinishedQuestion(item, extraCompleted); page = "exercise"
    }
    fun next() {
        if (!wordMatch.canContinue || capture.phase != "idle") return
        stopCapture(); speech.stop()
        saveProgress(progress.nextPhrase(lesson))
        transcript = ""
        page = progress.phase
        status = "Sẵn sàng học câu mới"
        if (automatic && page == "repeat") speak(phrases[progress.phrase].english)
    }
    fun openLesson(source: Lesson) {
        hotelDirectChat = false
        if (HotelCurriculum.isHotel(source)) hotelRole = HotelCurriculum.role(source)
        if (!topicIsOpen(source, learnerLessons, ::loadProgress)) return
        val item = personalizeLesson(source, learner.name)
        stopCapture(); speech.stop()
        transcript = ""
        status = "Nhấn Nghe mẫu để bắt đầu"
        selectedId = item.id
        preferences.edit().putString("active_lesson", item.id).apply()
        val saved = loadProgress(item)
        if (item.id == lesson.id) saveProgress(saved)
        page = saved.phase
        if (automatic && page == "repeat") speak(item.phrases[saved.phrase].english)
    }
    fun openHotelTopic(source: Lesson) {
        hotelRole = HotelCurriculum.role(source)
        val directChat = hotelMode == "chat"
        hotelDirectChat = false
        if (hotelMode == "exercise") {
            selectedId = source.id
            openExercise(HotelCurriculum.exercises.first { it.lessonId == source.id })
        } else {
            openLesson(source)
            hotelDirectChat = directChat
            if (directChat) {
                hotelChatTurn = preferences.getInt("hotel.dialogue.${source.id}", 0)
                    .takeIf { it in source.conversation.indices } ?: 0
                page = "chat"
            }
        }
    }
    var neon by rememberSaveable { mutableStateOf(appearance.getBoolean("neon_theme", false)) }
    var dark by rememberSaveable { mutableStateOf(appearance.getBoolean("dark_theme", false)) }
    var goal by rememberSaveable { mutableIntStateOf(preferences.getInt("daily_goal", 20).takeIf { it in listOf(10, 15, 20, 30) } ?: 20) }
    var settingDialog by rememberSaveable { mutableStateOf("") }
    fun selectSpeed(value: Int) {
        playbackSpeed = value
        preferences.edit().putInt("playback_speed", value).apply()
    }
    fun changeVoice(accent: String = voiceChoice.accent, gender: String = voiceChoice.gender) {
        stopCapture(); speech.stop()
        voiceChoice = VoiceChoices.select(preferences, accent, gender)
        transcript = ""
        status = ""
    }
    fun navigate(destination: String) {
        stopCapture(); speech.stop()
        transcript = ""
        page = if (destination == "repeat") progress.phase else destination
    }
    fun openVoiceSettings() {
        voiceReturnPage = page
        navigate("voice-settings")
    }
    BackHandler(enabled = page != "home") {
        navigate(when (page) {
            "voice-settings" -> voiceReturnPage
            "updates", "edit-profile" -> "profile"
            "passage" -> "passages"
            "exercise" -> if (exercise.lessonId.startsWith("hotel_")) "hotel-topics" else "exercises"
            "account" -> "profile"
            "hotel-topics" -> "hotel"
            "hotel" -> "lessons"
            "repeat", "chat", "review", "summary" -> if (HotelCurriculum.isHotel(lesson)) "hotel-topics" else "home"
            "level-lessons", "passages", "exercises" -> "lessons"
            else -> "home"
        })
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalSpeechReplay provides SpeechReplay(
        hasRecording && capturePhase == "idle", { speech.stop(); capture.replay() }
    )) {
    SpeakivraTheme(dark, neon = neon) {
        val palette = studio
        SideEffect { (context as? ComponentActivity)?.let {
            // Same system-bar appearance policy on every screen of the selected theme.
            updateSystemBars(it, dark || neon, palette.background)
        } }
        val available = updateState.release
        if (page == "home" && available != null && available.versionCode > installation.versionCode &&
            available.canInstallOver(installation) && dismissedUpdate != available.versionCode) {
            AlertDialog(onDismissRequest = { dismissedUpdate = available.versionCode },
                title = { Text("Speakivra v${available.version}") },
                text = {
                    Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                        Text("Có gì mới", fontWeight = FontWeight.Bold)
                        releaseHighlights(available.notes).forEach { Text("• $it") }
                    }
                },
                confirmButton = { TextButton(onClick = { dismissedUpdate = available.versionCode; beginUpdate() }) { Text("Cập nhật ngay") } },
                dismissButton = { TextButton(onClick = { dismissedUpdate = available.versionCode }) { Text("Để sau") } })
        }
        if (settingDialog.isNotBlank()) {
            val title = when (settingDialog) {
                "speed" -> "Tốc độ giọng đọc"
                "goal" -> "Mục tiêu mỗi ngày"
                "study" -> "Tùy chỉnh bài học"
                "sound" -> "Âm thanh & thông báo"
                "about" -> "Về Speakivra"
                else -> "Giọng mẫu"
            }
            AlertDialog(
                onDismissRequest = { settingDialog = "" },
                title = { Text(title) },
                text = {
                    Column(Modifier.heightIn(max = 410.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        when (settingDialog) {
                            "speed" -> listOf(75, 100, 125).forEach { value ->
                                TextButton(onClick = {
                                    selectSpeed(value)
                                    settingDialog = ""
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(speedLabel(value) +
                                        if (playbackSpeed == value) " ✓" else "")
                                }
                            }
                            "goal" -> listOf(10, 15, 20, 30).forEach { value ->
                                TextButton(onClick = {
                                    goal = value
                                    preferences.edit().putInt("daily_goal", value).apply()
                                    settingDialog = ""
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("$value phút" + if (goal == value) " ✓" else "")
                                }
                            }
                            "study" -> {
                                Text("Chọn cấp độ thực hành", fontWeight = FontWeight.Bold)
                                Text("A1: 10 chủ đề • A2: 15 chủ đề • B1/B2/C1/C2: 20 chủ đề mỗi cấp", color = palette.muted,
                                    fontSize = 12.sp)
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CourseLevels.all.forEach { level ->
                                        OutlinedButton(onClick = {
                                            selectedLevel = level
                                            settingDialog = ""
                                            navigate("level-lessons")
                                        }, enabled = levelIsOpen(level, learnerLessons, ::loadProgress), modifier = Modifier.widthIn(min = 64.dp)) {
                                            Text(level)
                                        }
                                    }
                                }
                                HorizontalDivider()
                                Text("Lặp lại câu mẫu", fontWeight = FontWeight.Bold)
                                Text("Phát lại nguyên câu trong mỗi lượt nghe mẫu.",
                                    color = palette.muted, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    (1..3).forEach { count ->
                                        OutlinedButton(onClick = {
                                            sampleRepeats = count
                                            LearningOptions.setRepeats(preferences, count)
                                        }, modifier = Modifier.weight(1f)) {
                                            Text("$count lần" +
                                                if (sampleRepeats == count) " ✓" else "")
                                        }
                                    }
                                }
                                HorizontalDivider()
                                TextButton(onClick = {
                                    settingDialog = "speed"
                                }) { Text("Tốc độ đọc: ${speedLabel(playbackSpeed)}") }
                                TextButton(onClick = {
                                    settingDialog = "goal"
                                }) { Text("Mục tiêu: $goal phút") }
                            }
                            "sound" -> {
                                Text("Giọng đọc", fontWeight = FontWeight.Bold)
                                Text("${voiceChoice.accentLabel} • ${voiceChoice.genderLabel}",
                                    color = palette.muted)
                                OutlinedButton(onClick = {
                                    settingDialog = ""
                                    openVoiceSettings()
                                }) { Text("Chọn giọng và nghe thử") }
                                HorizontalDivider()
                                Text("Nhắc học cục bộ", fontWeight = FontWeight.Bold)
                                Text("Nếu bật, Speakivra nhắc học khoảng 20:00 hằng ngày. Android có thể trì hoãn để tiết kiệm pin.",
                                    color = palette.muted, fontSize = 12.sp)
                                OutlinedButton(onClick = {
                                    toggleReminder(!reminderEnabled)
                                }) { Text(if (reminderEnabled) "Tắt nhắc học" else "Bật nhắc học") }
                            }
                            "about" -> {
                                Text("Speakivra V$APP_VERSION", fontWeight = FontWeight.Bold)
                                Text("Ứng dụng học tiếng Anh A1/A2. Bài hội thoại là kịch bản có sẵn, không phải người đối thoại AI trực tuyến.")
                                Text("Âm thanh từ clip đã đóng gói và giọng TTS do điện thoại cung cấp. Nhận dạng có thể dùng dịch vụ của hệ thống.")
                                Text("Tiến độ được giữ riêng trên thiết bị. Cập nhật dùng APK ký hợp lệ và xác minh SHA-256.")
                            }
                            else -> Text("Lựa chọn giọng được lưu riêng theo người học.")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { settingDialog = "" }) { Text("Đóng") }
                }
            )
        }
        Surface(color = if (neon) Color.Transparent else palette.background,
            contentColor = palette.ink, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                // One full-screen background behind scroll content and the bottom five tabs.
                // Never attach the wave to a short scrolling panel.
                if (neon) NeonWaveBackground(Modifier.matchParentSize())
                Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    // Each screen owns its scroll area; speaking/review keep the next action visible.
                    key(page, lesson.id, progress.phrase, progress.turn, progress.review) {
                        when (page) {
                            "home" -> HomeScreen(lesson, progress, goal, completed, learner.name,
                                onContinue = { hotelDirectChat = false; page = progress.phase; if (automatic && page == "repeat") speak(phrases[index].english) },
                                onLessons = { navigate("lessons") }, onProfile = { navigate("profile") })
                            "lessons" -> LessonHubScreen(
                                onLevel = { if (levelIsOpen(it, learnerLessons, ::loadProgress)) { selectedLevel = it; navigate("level-lessons") } },
                                onChat = { navigate("chat") }, onPassages = { navigate("passages") },
                                onExercises = { navigate("exercises") }, onBack = { navigate("home") },
                                onHotel = { navigate("hotel") }, load = ::loadProgress)
                            "hotel" -> HotelHubScreen(hotelRole, onRole = { hotelRole = it },
                                onOpen = { role, mode -> hotelRole = role; hotelMode = mode; navigate("hotel-topics") },
                                onBack = { navigate("lessons") })
                            "hotel-topics" -> HotelTopicsScreen(hotelRole, ::loadProgress, achievements::hearts,
                                onOpen = ::openHotelTopic, onBack = { navigate("hotel") })
                            "passages", "exercises" -> ExtraCatalogueScreen(page == "passages", extraLevel,
                                extraCompleted, ::loadProgress, onLevel = { extraLevel = it },
                                onBack = { navigate("lessons") }, onPassage = ::openPassage, onExercise = ::openExercise)
                            "passage" -> PassagePracticeScreen(passage, sentenceIndex, wholePassage, showTranslation,
                                playbackSpeed, voiceChoice, learner, finalTranscript, transcript, capturePhase, status,
                                onBack = { navigate("passages") },
                                onSentence = { clearExtra(); sentenceIndex = it },
                                onWhole = { clearExtra(); wholePassage = it }, onSpeak = ::listen,
                                onListen = { speak(it) }, onSpeed = ::selectSpeed,
                                onTranslation = { showTranslation = !showTranslation }, onNext = {
                                    val result = matchWords(if (wholePassage) passage.english else sentence.english, finalTranscript)
                                    if (extraTopicOpen(passage.lessonId, ::loadProgress) && capturePhase == "idle" && result.canContinue) {
                                        completeExtra(if (wholePassage) passage.sentences.map { it.id } + passage.id else listOf(sentence.id))
                                        clearExtra()
                                        if (wholePassage) page = "passages"
                                        else if (sentenceIndex < passage.sentences.lastIndex) sentenceIndex++
                                        else wholePassage = true
                                    }
                                })
                            "exercise" -> SpeakingExerciseScreen(exercise, questionIndex, extraCompleted, voiceChoice,
                                learner, finalTranscript, transcript, capturePhase, status,
                                onBack = { navigate(if (exercise.lessonId.startsWith("hotel_")) "hotel-topics" else "exercises") }, onSpeak = ::listen, onListen = { speak(it) },
                                onNext = {
                                    if (extraTopicOpen(exercise.lessonId, ::loadProgress) && capturePhase == "idle" && evaluateExercise(question, finalTranscript).accepted) {
                                        completeExtra(listOf(question.id)); clearExtra()
                                        if (questionIndex < exercise.questions.lastIndex) questionIndex++
                                        else page = if (exercise.lessonId.startsWith("hotel_")) "hotel-topics" else "exercises"
                                    }
                                })
                            "level-lessons" -> LessonsScreen(lesson.id, ::loadProgress, ::openLesson,
                                learnerLessons.filter { Lessons.levelOf(it) == selectedLevel }, selectedLevel,
                                onBack = { navigate("lessons") }, hearts = achievements::hearts, accessCatalogue = learnerLessons)
                            "progress" -> ProgressScreen(learnerLessons, ::loadProgress, ::openLesson, achievements::hearts)
                            "repeat" -> PracticeScreen(lesson, progress, automatic, showTranslation, playbackSpeed, if (capturePhase == "idle") finalTranscript.orEmpty() else transcript, status, capturePhase, wordMatch, learner,
                                onBack = { navigate(if (HotelCurriculum.isHotel(lesson)) "hotel-topics" else "home") }, onMode = { automatic = it; preferences.edit().putBoolean("automatic_mode", it).apply() },
                                onSpeed = ::selectSpeed, onTranslation = { showTranslation = !showTranslation },
                                onListen = { speak(phrases[index].english) }, onSpeak = ::listen, onNext = ::next,
                                voiceChoice = voiceChoice, onVoiceSettings = ::openVoiceSettings,
                                explanationLanguage = explanationLanguage)
                            "chat" -> ConversationScreen(lesson, chatIndex, showTranslation, finalTranscript.orEmpty(), capturePhase, status, learner,
                                nextLabel = if (HotelCurriculum.isHotel(lesson) && hotelDirectChat) {
                                    if (chatIndex != lesson.conversation.lastIndex) "Lượt tiếp theo" else "Hoàn thành hội thoại"
                                } else if (chatIndex != lesson.conversation.lastIndex) "Câu hội thoại tiếp theo" else when (progress.phase) {
                                    "summary" -> "Về tổng kết"; "review" -> "Tiếp tục ôn tập"; else -> "Sang ôn tập"
                                }, onBack = { navigate(if (HotelCurriculum.isHotel(lesson)) "hotel-topics" else "home") }, onPrompt = { speak(lesson.conversation[chatIndex].prompt) },
                                onReply = { speak(lesson.conversation[chatIndex].reply) }, onSpeak = ::listen,
                                onTranslation = { showTranslation = !showTranslation }, voiceChoice = voiceChoice,
                                onVoiceSettings = ::openVoiceSettings, neon = neon, hotelSpeed = playbackSpeed, onHotelSpeed = ::selectSpeed,
                                explanationLanguage = explanationLanguage,
                                onTypedText = { typed ->
                                    stopCapture()
                                    transcript = typed
                                    finalTranscript = typed
                                    status = "Câu trả lời bạn đã nhập, chưa qua nhận dạng giọng nói."
                                }, onNext = {
                                    if (HotelCurriculum.isHotel(lesson) && hotelDirectChat) {
                                        if (capturePhase == "idle" && matchWords(lesson.conversation[chatIndex].reply, finalTranscript).canContinue) {
                                            clearExtra()
                                            val key = "lesson.${lesson.id}.learned_turns"
                                            val prior = preferences.getString(key, "").orEmpty().split('|').filter { it.isNotBlank() }.toSet()
                                            preferences.edit().putString(key, (prior + lesson.conversation[chatIndex].id).joinToString("|"))
                                                .putInt("hotel.dialogue.${lesson.id}", maxOf(preferences.getInt("hotel.dialogue.${lesson.id}", 0), chatIndex + 1)).apply()
                                            if (chatIndex < lesson.conversation.lastIndex) hotelChatTurn++ else navigate("hotel-topics")
                                        }
                                    } else {
                                    stopCapture(); speech.stop()
                                    if (progress.phase in listOf("repeat", "chat")) {
                                        saveProgress(progress.copy(phase = "chat").nextTurn(lesson)); page = progress.phase
                                    } else if (chatIndex < lesson.conversation.lastIndex) saveProgress(progress.copy(turn = chatIndex + 1))
                                    else page = progress.phase
                                    transcript = ""
                                    if (automatic && page == "chat") speak(lesson.conversation[progress.turn].prompt)
                                    }
                                })
                            "review" -> ReviewScreen(lesson, progress, reviewSelection, finalTranscript.orEmpty(), capturePhase, status, learner,
                                onSelect = { if (reviewSelection < 0) reviewSelection = it }, onBack = { navigate(if (HotelCurriculum.isHotel(lesson)) "hotel-topics" else "home") },
                                onListen = {
                                    speak(if (explanationLanguage == LearningOptions.ENGLISH)
                                        EnglishStudy.review(lesson, progress.review).prompt
                                        else reviewQuestion(lesson, progress.review).phrase.english)
                                }, onSpeak = ::listen,
                                explanationLanguage = explanationLanguage,
                                onNext = {
                                    if (reviewSelection >= 0) {
                                        stopCapture(); speech.stop()
                                        val answer = if (explanationLanguage == LearningOptions.ENGLISH)
                                            EnglishStudy.review(lesson, progress.review).answer
                                        else reviewQuestion(lesson, progress.review).answer
                                        val value = progress.answerReview(reviewSelection == answer)
                                        if (value.rounds > progress.rounds) completed++
                                        saveProgress(value); reviewSelection = -1; transcript = ""; page = progress.phase
                                    }
                                })
                            "summary" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PageHeading("Hoàn thành bài học", lessonLabel(lesson), onBack = { navigate(if (HotelCurriculum.isHotel(lesson)) "hotel-topics" else "home") })
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
                                val following = learnerLessons.getOrNull(learnerLessons.indexOf(lesson) + 1)?.takeIf {
                                    if (HotelCurriculum.isHotel(lesson)) HotelCurriculum.isHotel(it) && HotelCurriculum.role(it) == HotelCurriculum.role(lesson)
                                    else !HotelCurriculum.isHotel(it)
                                }
                                if (following != null) CoralButton("Bài tiếp theo: ${lessonLabel(following)}", onClick = { openLesson(following) })
                                TextButton(onClick = {
                                    if (HotelCurriculum.isHotel(lesson)) { hotelRole = HotelCurriculum.role(lesson); navigate("hotel-topics") }
                                    else { selectedLevel = Lessons.levelOf(lesson); navigate("level-lessons") }
                                }) { Text("Về danh sách bài học") }
                            }
                            "edit-profile" -> CreateLearnerScreen(initial = learner, onBack = { navigate("profile") }, onSave = { name, avatar ->
                                val error = onUpdateLearner(name, avatar)
                                if (error == null) navigate("profile")
                                error
                            })
                            "voice-settings" -> VoiceSettingsScreen(
                                choice = voiceChoice,
                                available = speech.installedVoices(voiceChoice.accent),
                                speed = playbackSpeed,
                                explanationLanguage = explanationLanguage,
                                message = status,
                                onBack = { navigate(voiceReturnPage) },
                                onExplanationLanguage = {
                                    LearningOptions.setLanguage(preferences, it)
                                    explanationLanguage = it
                                },
                                onSpeed = ::selectSpeed,
                                onAccent = { changeVoice(accent = it) },
                                onGender = { changeVoice(gender = it) },
                                onAssign = { id ->
                                    stopCapture(); speech.stop()
                                    if (speech.installedVoices(voiceChoice.accent).any { it.id == id }) {
                                        voiceChoice = VoiceChoices.assign(preferences, voiceChoice.accent, voiceChoice.gender, id)
                                        status = "Đã lưu giọng đọc ngoại tuyến được chọn."
                                    } else status = "Không tìm thấy giọng này trên thiết bị."
                                },
                                onPreview = { id ->
                                    stopCapture()
                                    if (speech.installedVoices(voiceChoice.accent).any { it.id == id }) {
                                        speech.speak("Hello! Welcome to Speakivra!", playbackSpeed, voiceChoice.copy(voiceId = id))
                                    } else status = "Không tìm thấy giọng này trên thiết bị."
                                },
                                onPreviewDefault = {
                                    stopCapture(); speech.speak("Hello! How are you?", playbackSpeed, VoiceChoice())
                                },
                                onResetDefault = {
                                    stopCapture(); speech.stop()
                                    voiceChoice = VoiceChoices.resetBundled(preferences)
                                    status = "Đã khôi phục giọng nữ neural Anh-Mỹ gốc."
                                }
                            )
                            "account" -> AccountFlow(learner, onBack = { navigate("profile") }, onAuthScreen = { accountAuthScreen = it }, onChooseLearner = onChooseLearner)
                            "profile" -> ProfileScreen(
                                dark = dark, speed = playbackSpeed, goal = goal,
                                version = APP_VERSION, learner = learner,
                                onEditLearner = { navigate("edit-profile") },
                                onSwitchLearner = { stopCapture(); speech.stop(); onSwitchLearner() },
                                onTheme = {
                                    dark = it; neon = false
                                    appearance.edit().putBoolean("dark_theme", it)
                                        .putBoolean("neon_theme", false).apply()
                                },
                                neon = neon,
                                onNeon = {
                                    neon = true
                                    appearance.edit().putBoolean("neon_theme", true).apply()
                                },
                                onVoice = ::openVoiceSettings,
                                onSpeed = { settingDialog = "speed" },
                                onGoal = { settingDialog = "goal" },
                                onUpdates = { page = "updates"; updates.check() },
                                voiceChoice = voiceChoice,
                                explanationLanguage = explanationLanguage,
                                repeatCount = sampleRepeats,
                                reminderEnabled = reminderEnabled,
                                onBack = { navigate("home") },
                                onAccent = { changeVoice(accent = it) },
                                onGender = { changeVoice(gender = it) },
                                onExplanationLanguage = {
                                    LearningOptions.setLanguage(preferences, it)
                                    explanationLanguage = it
                                },
                                onStudyOptions = { settingDialog = "study" },
                                onReminderToggle = ::toggleReminder,
                                onSound = { settingDialog = "sound" },
                                onAbout = { settingDialog = "about" },
                                onAccount = { navigate("account") }
                            )
                            "updates" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PageHeading("Cập nhật ứng dụng", onBack = { navigate("profile") })
                                UpdatesScreen(updateState, installation, updates::check, ::beginUpdate, ::requestInstall, updates::cancel)
                            }
                        }
                    }
                }
                // All three appearances use the same full-screen scripted conversation.
                if ((page != "account" || !accountAuthScreen) && (page !in listOf("edit-profile", "voice-settings", "chat") || (page == "chat" && HotelCurriculum.isHotel(lesson))))
                    StudioBottomNavigation(when (page) {
                    "account" -> "profile"
                    "hotel", "hotel-topics", "level-lessons", "passages", "exercises" -> "lessons"
                    "chat", "passage", "exercise" -> "repeat"
                    else -> page
                }) { dest ->
                    navigate(dest)
                }
                }
            }
        }
    }
}

}

@Suppress("DEPRECATION")
private fun updateSystemBars(activity: ComponentActivity, dark: Boolean, color: Color) {
    val transparent = android.graphics.Color.TRANSPARENT
    val style = if (dark) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
    activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    // Keep the window behind transparent Android 15 system bars in the chosen theme,
    // including when the learner picker is replaced by the learning composition.
    activity.window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(color.toArgb()))
    activity.window.decorView.setBackgroundColor(color.toArgb())
    activity.window.statusBarColor = color.toArgb()
    activity.window.navigationBarColor = if (color == NeonStudio.background) transparent else color.toArgb()
    WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
        isAppearanceLightStatusBars = !dark
        isAppearanceLightNavigationBars = !dark
    }
    activity.window.decorView.requestApplyInsets()
}
