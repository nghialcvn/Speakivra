package com.nghialcvn.speakivra

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
internal fun ChineseCourseScreen(preferences: SharedPreferences, learner: Learner, onBack: () -> Unit,
    initialTopic: String? = null, initialMode: String = "", initialLevel: String? = null, initialPackId: String = "") {
    val context = LocalContext.current
    val store = remember(preferences) { ChineseProgress(preferences) }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val topics = remember(learner.name) { ChineseCurriculum.topics.map { it.personalize(learner.name) } }
    var level by rememberSaveable { mutableStateOf(initialLevel ?: "A1") }
    var catalogue by rememberSaveable { mutableStateOf(if (initialLevel != null || initialTopic != null) "level" else "hub") }
    var directMode by rememberSaveable { mutableStateOf("") }
    var topicId by rememberSaveable { mutableStateOf(initialTopic) }
    LaunchedEffect(topicId) {
        val id = topicId
        if (id != null && topics.any { it.id == id && store.topicOpen(it, topics) } && preferences.getString("zh.active_lesson", null) != id)
            preferences.edit().putString("zh.active_lesson", id).apply()
    }
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var packId by rememberSaveable { mutableStateOf(initialPackId) }
    var wholeReading by rememberSaveable { mutableStateOf(false) }
    var item by rememberSaveable { mutableIntStateOf(initialTopic?.let { id -> topics.firstOrNull { it.id == id }?.let { store.firstPending(it.copy(selectedReading = if (initialPackId.isEmpty() && it.starter) it.legacyReading else it.readingPacks.firstOrNull { p -> p.id == initialPackId }?.lines, selectedExercises = if (initialPackId.isEmpty() && it.starter) it.legacyExercises else it.exercisePacks.firstOrNull { p -> p.id == initialPackId }?.lines), initialMode) } } ?: 0) }
    var primer by rememberSaveable { mutableStateOf(false) }
    var speed by rememberSaveable { mutableIntStateOf(preferences.getInt("zh_playback_speed", 75).takeIf { it in listOf(75, 100, 125) } ?: 75) }
    var showMeaning by rememberSaveable { mutableStateOf(true) }
    var showPinyin by rememberSaveable { mutableStateOf(true) }
    var transcript by remember { mutableStateOf("") }
    var finalTranscript by remember { mutableStateOf<String?>(null) }
    var phase by remember { mutableStateOf("idle") }
    var status by remember { mutableStateOf("Nhấn Nghe mẫu để bắt đầu") }
    var hasRecording by remember { mutableStateOf(false) }
    var pendingPermission by remember { mutableStateOf(false) }
    var voiceDialog by remember { mutableStateOf(false) }
    var voiceGender by remember { mutableStateOf(preferences.getString("zh_voice_gender", "female") ?: "female") }
    var automatic by rememberSaveable { mutableStateOf(preferences.getBoolean("zh_automatic_mode", false)) }
    var voiceId by remember { mutableStateOf(preferences.getString("zh_voice_id", null)) }
    val playback = remember(context) { SpeechPlayback(context.applicationContext) { status = it } }
    val update by rememberUpdatedState<(String, String?, String) -> Unit>({ nextPhase, words, message ->
        phase = nextPhase
        if (words != null) {
            transcript = words
            if (nextPhase == "idle") finalTranscript = words.takeIf { it.isNotBlank() }
        }
        status = message
    })
    val capture = remember(context) { SpeechCapture(context.applicationContext,
        recordingChanged = { hasRecording = it }) { p, words, message -> update(p, words, message) } }
    fun clearSpeech() {
        pendingPermission = false; capture.cancel(); playback.stop()
        transcript = ""; finalTranscript = null; phase = "idle"; status = "Nhấn Nghe mẫu để bắt đầu"
    }
    val owner = context as? LifecycleOwner
    DisposableEffect(capture, playback, owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) clearSpeech()
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer); capture.cancel(); playback.close() }
    }
    DisposableEffect(topicId, mode, item, learner.name) {
        onDispose { clearSpeech() }
    }
    fun back() {
        clearSpeech()
        when {
            mode.isNotEmpty() -> mode = ""
            topicId != null -> topicId = null
            primer -> primer = false
            catalogue != "hub" -> catalogue = "hub"
            else -> onBack()
        }
    }
    BackHandler { back() }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val requested = pendingPermission; pendingPermission = false
        if (granted && requested && owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true)
            capture.start(languageTag = "zh-CN", maximumMillis = if (mode == "read" && wholeReading) 240000 else 60000)
        else if (!granted && requested) status = "Cần cấp quyền micro để luyện nói tiếng Trung."
    }
    fun speak(text: String) {
        pendingPermission = false; capture.cancel(discardRecording = false)
        playback.speakMandarin(text, speed, voiceId, LearningOptions.repeats(preferences))
    }
    fun listen() {
        if (phase == "listening") { capture.stop(); return }
        if (phase != "idle" || pendingPermission) return
        clearSpeech()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            capture.start(languageTag = "zh-CN", maximumMillis = if (mode == "read" && wholeReading) 240000 else 60000)
        else { pendingPermission = true; permission.launch(Manifest.permission.RECORD_AUDIO) }
    }
    if (voiceDialog) AlertDialog(onDismissRequest = { voiceDialog = false }, title = { Text("Giọng tiếng Trung") },
        text = {
            Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                Text("Nghe thử rồi gán giọng cho lựa chọn nam/nữ. Android không đảm bảo nhãn giới tính của giọng hệ thống.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("female" to "Giọng nữ", "male" to "Giọng nam").forEach { (value, label) ->
                        FilterChip(voiceGender == value, { clearSpeech(); voiceGender = value; voiceId = selectMandarinGender(preferences, value) }, label = { Text(label) })
                    }
                }
                TextButton(onClick = { voiceId = null; preferences.edit().remove("zh_voice_id").remove("zh_voice_$voiceGender").apply(); voiceDialog = false }) { Text("Tự chọn giọng phổ thông Trung Quốc") }
                val voices = playback.mandarinVoices()
                if (voices.isEmpty()) MandarinVoiceInstall()
                voices.forEachIndexed { index, voice ->
                    Text("Giọng ${index + 1}" + if (voiceId == voice.id || (voiceId == null && index == 0)) " • Đang chọn" else "", fontSize = 14.sp)
                    Row {
                        TextButton(onClick = { playback.speakMandarin("你好，很高兴认识你。", speed, voice.id) }) { Text("Nghe thử") }
                        TextButton(onClick = { playback.stop(); voiceId = voice.id; preferences.edit().putString("zh_voice_id", voice.id).putString("zh_voice_$voiceGender", voice.id).putString("zh_voice_gender", voiceGender).apply(); voiceDialog = false }) { Text("Chọn giọng này") }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { playback.stop(); voiceDialog = false }) { Text("Đóng") } })
    CompositionLocalProvider(LocalSpeechReplay provides SpeechReplay(hasRecording && phase == "idle", { playback.stop(); capture.replay() })) {
        // Refresh cloud restores without replacing media/session state.
        val baseTopic = remember(topics, topicId, revision) { topics.firstOrNull { it.id == topicId && store.topicOpen(it, topics) } }
        val topic = baseTopic?.let { t ->
            val reading = when {
                packId.isEmpty() && t.starter -> t.legacyReading
                else -> t.readingPacks.firstOrNull { it.id == packId }?.lines ?: t.readingPacks.firstOrNull()?.lines.orEmpty()
            }
            val exercises = when {
                packId.isEmpty() && t.starter -> t.legacyExercises
                else -> t.exercisePacks.firstOrNull { it.id == packId }?.lines ?: t.exercisePacks.firstOrNull()?.lines.orEmpty()
            }
            if (mode.isEmpty()) t else t.copy(selectedReading = reading, selectedExercises = exercises)
        }
        when {
            primer -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeading("Pinyin và thanh điệu", "Làm quen trước khi học A1", ::back)
                Text("Pinyin ghi cách đọc bằng chữ Latin. Một âm tiết thường gồm thanh mẫu, vận mẫu và thanh điệu.")
                listOf("妈" to "mā • thanh 1: cao, ngang • mẹ", "麻" to "má • thanh 2: đi lên • cây gai",
                    "马" to "mǎ • thanh 3: thấp, có thể xuống rồi lên • ngựa", "骂" to "mà • thanh 4: đi xuống mạnh • mắng",
                    "吗" to "ma • thanh nhẹ: ngắn, nhẹ • trợ từ hỏi").forEach { (hanzi, description) ->
                    StudioCard { Text("$hanzi  $description"); TextButton(onClick = { speak(hanzi) }) { Text("Nghe $hanzi") } }
                }
                Text("Dấu thanh đặt trên nguyên âm chính: ưu tiên a, rồi o/e; iu/ui đặt trên nguyên âm sau. ü khác u: giữ môi tròn khi đọc ü.")
                Text("Hai thanh ba liền nhau: âm đầu thường đọc gần thanh hai (nǐ hǎo). 一 và 不 đổi thanh theo âm sau. Pinyin trong bài có ghi biến điệu phổ biến của 一/不; thanh ba vẫn ghi dạng từ điển.")
                Text("Tập nghe và bắt chước từng âm. Kết quả nhận dạng chữ chưa chứng minh bạn đọc đúng thanh điệu.", color = studio.muted)
                TextButton(onClick = { clearSpeech(); voiceDialog = true }) { Text("Chọn giọng tiếng Trung") }
                Text(status, color = studio.muted)
            }
            topic == null && catalogue == "hub" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PageHeading("Bài học", "Chọn cấp độ hoặc dạng luyện tập • Tiếng Trung", ::back)
                CourseLevels.all.forEach { value ->
                    val items = topics.filter { it.level == value }
                    val unlocked = store.levelOpen(value, topics)
                    StudioCard(Modifier.fillMaxWidth().clickable(enabled = unlocked) { level = value; directMode = ""; catalogue = "level" }.testTag("zh-level-$value")) {
                        Text(CourseLevels.title(value), fontWeight = FontWeight.ExtraBold)
                        Text("${items.size} chủ đề • ${items.sumOf { it.phrases.size }} câu luyện nói • ${items.sumOf { it.turns.size }} lượt hội thoại", color = studio.muted)
                        if (!unlocked) Text(CourseLevels.lockMessage(value), color = studio.muted)
                    }
                }
                listOf(Triple("hotel", "Tiếng Trung Khách sạn", "Bài học riêng • Lễ tân 20 chủ đề • Buồng phòng 20 chủ đề"),
                    Triple("chat", "Hội thoại mẫu", "Mô phỏng theo kịch bản"),
                    Triple("readings", "Luyện nói theo đoạn văn", "A1 • A2 • B1 • B2 • C1 • C2"),
                    Triple("exercises", "Tiếng Trung nói thế nào?", "Việt → Trung • A1–C2 • Khách sạn")).forEach { (key, title, detail) ->
                    StudioCard(Modifier.fillMaxWidth().clickable {
                        catalogue = if (key == "chat") "level" else key
                        directMode = if (key == "chat") "chat" else ""; level = "A1"
                    }.testTag("zh-catalogue-$key")) { Text(title, fontWeight = FontWeight.ExtraBold); Text(detail, color = studio.muted) }
                }
                TextButton(onClick = { primer = true }) { Text("Nhập môn Pinyin và 4 thanh điệu") }
            }
            topic == null && catalogue == "hotel" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PageHeading("Tiếng Trung Khách sạn", "Giao tiếp với khách quốc tế • Bài học riêng", ::back)
                listOf("FO", "HK").forEach { role ->
                    StudioCard {
                        Text(HotelCurriculum.roleLabel(role), fontWeight = FontWeight.ExtraBold)
                        Text(if (role == "FO") "前台" else "客房部", color = studio.muted)
                        Text("20 chủ đề • 500 câu • 120 lượt hội thoại • 500 câu Việt → Trung")
                        listOf("repeat" to "Nghe & nhắc lại", "chat" to "Hội thoại phân vai", "exercise" to "Tiếng Trung nói thế nào?").forEach { (value, title) ->
                            TextButton(onClick = { level = role; directMode = value; catalogue = "level" }, modifier = Modifier.testTag("zh-hotel-$role-$value")) { Text(title) }
                        }
                    }
                }
            }
            topic == null && catalogue in listOf("readings", "exercises") -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val reading = catalogue == "readings"
                PageHeading(if (reading) "Luyện nói theo đoạn văn" else "Tiếng Trung nói thế nào?", "Chọn cấp độ và bài học", ::back)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (CourseLevels.all + if (reading) emptyList() else listOf("FO", "HK")).forEach { value ->
                        FilterChip(level == value, { level = value }, label = { Text(if (value in listOf("FO", "HK")) HotelCurriculum.roleLabel(value) else value) })
                    }
                }
                topics.filter { it.level == level }.forEach { value ->
                    if (reading) value.readingPacks.forEach { pack ->
                        StudioCard(Modifier.fillMaxWidth().clickable(enabled = store.topicOpen(value, topics)) {
                            topicId = value.id; packId = pack.id; mode = "read"; wholeReading = false
                            item = store.firstPending(value.copy(selectedReading = pack.lines), "read")
                        }.testTag(pack.id)) {
                            Text(pack.title, fontWeight = FontWeight.Bold); Text("${value.title} • ${pack.lines.size} câu", color = studio.muted)
                            if (!store.topicOpen(value, topics)) Text("Chủ đề đang khóa", color = studio.muted)
                        }
                    } else value.exercisePacks.forEach { pack ->
                        StudioCard(Modifier.fillMaxWidth().clickable(enabled = store.topicOpen(value, topics)) {
                            topicId = value.id; packId = pack.id; mode = "exercise"
                            item = store.firstPending(value.copy(selectedExercises = pack.lines), "exercise")
                        }.testTag(pack.id)) {
                            Text(pack.title, fontWeight = FontWeight.Bold); Text("${value.title} • ${pack.lines.size} câu", color = studio.muted)
                            if (!store.topicOpen(value, topics)) Text("Chủ đề đang khóa", color = studio.muted)
                        }
                    }
                }
            }
            topic == null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeading("Tiếng Trung", "Phổ thông • Giản thể • Pinyin • Tiếng Việt", ::back)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (CourseLevels.all + listOf("FO", "HK")).forEach { value -> FilterChip(selected = level == value, onClick = { level = value }, label = { Text(value) }, modifier = Modifier.testTag("zh-level-$value")) }
                }
                Text("A1–C2 là lộ trình trong Speakivra, không phải chứng nhận HSK.", fontSize = 12.sp, color = studio.muted)
                TextButton(onClick = { primer = true }) { Text("Nhập môn Pinyin và 4 thanh điệu") }
                val levelTopics = topics.filter { it.level == level }
                Text("${levelTopics.size} chủ đề • ${levelTopics.sumOf { it.phrases.size }} câu nói • ${levelTopics.sumOf { it.turns.size }} lượt hội thoại", fontWeight = FontWeight.Bold)
                Text("Hoàn thành ${levelTopics.count { store.finished(it) }}/${levelTopics.size} chủ đề", color = studio.muted)
                AchievementSummary(levelTopics.sumOf { store.diamonds(it) })
                levelTopics.forEachIndexed { n, value ->
                    StudioCard(Modifier.fillMaxWidth().clickable(enabled = store.topicOpen(value, topics)) { topicId = value.id; packId = ""; mode = directMode; item = store.firstPending(value, directMode); preferences.edit().putString("zh.active_lesson", value.id).apply() }.testTag(value.id)) {
                        Text("${n + 1}. ${value.title}", fontWeight = FontWeight.ExtraBold)
                        Text(value.readingTitle, color = studio.muted)
                        Text(if (!store.topicOpen(value, topics)) "Khóa • ${CourseLevels.lockMessage(value.level)}" else if (store.finished(value)) "Đã hoàn thành" else "${store.done(value, "repeat")}/${value.phrases.size} câu luyện nói", color = studio.purple, fontSize = 12.sp)
                        TopicDiamonds(value.title, store.diamonds(value))
                    }
                }
            }
            mode.isEmpty() -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeading(topic.title, "Tiếng Trung ${topic.level}", ::back)
                Text(topic.tip, color = studio.muted)
                TopicDiamonds(topic.title, store.diamonds(topic))
                ChineseProgress.modes.filter { store.itemIds(topic, it).isNotEmpty() }.forEach { value ->
                    val title = modeTitle(value)
                    StudioCard(Modifier.fillMaxWidth().clickable { clearSpeech(); packId = ""; wholeReading = false; val selected = when (value) {
                            "read" -> topic.copy(selectedReading = if (topic.starter) topic.legacyReading else topic.readingPacks.firstOrNull()?.lines.orEmpty())
                            "exercise" -> topic.copy(selectedExercises = if (topic.starter) topic.legacyExercises else topic.exercisePacks.firstOrNull()?.lines.orEmpty())
                            else -> topic
                        }; item = store.firstPending(selected, value); mode = value }.testTag("zh-mode-$value")) {
                        Text(title, fontWeight = FontWeight.ExtraBold)
                        Text("${store.done(topic, value)}/${store.itemIds(topic, value).size} • Tiếp tục từ phần chưa học", color = studio.muted, fontSize = 12.sp)
                    }
                }
                topic.readingPacks.forEach { pack ->
                    StudioCard(Modifier.fillMaxWidth().clickable {
                        clearSpeech(); packId = pack.id; wholeReading = false; mode = "read"
                        item = store.firstPending(topic.copy(selectedReading = pack.lines), "read")
                    }.testTag(pack.id)) {
                        Text("Bài đọc • ${pack.title}", fontWeight = FontWeight.Bold)
                        Text("${pack.lines.size} câu • ${pack.vocabulary.size} từ vựng", color = studio.muted)
                    }
                }
                topic.exercisePacks.forEach { pack ->
                    StudioCard(Modifier.fillMaxWidth().clickable {
                        clearSpeech(); packId = pack.id; mode = "exercise"
                        item = store.firstPending(topic.copy(selectedExercises = pack.lines), "exercise")
                    }.testTag(pack.id)) {
                        Text("Việt → Trung • ${pack.title}", fontWeight = FontWeight.Bold)
                        Text("${pack.lines.size} câu", color = studio.muted)
                    }
                }
            }
            mode == "review" -> {
                val questions = topic.comprehension
                val index = item.coerceIn(questions.indices)
                val question = questions[index]
                var selection by remember(topic.id, item) { mutableIntStateOf(-1) }
                var checked by remember(topic.id, item) { mutableStateOf<Boolean?>(null) }
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PageHeading(if (topic.starter) "Đọc hiểu" else "Ôn nghĩa", "${topic.title} • ${index + 1}/${questions.size}", ::back)
                        Text(topic.readingTitle, fontWeight = FontWeight.ExtraBold)
                        if (topic.starter) Text(topic.legacyReading.joinToString("") { it.hanzi }, fontSize = 20.sp, lineHeight = 30.sp)
                        if (topic.starter) TextButton(onClick = { speak(topic.legacyReading.joinToString("") { it.hanzi }) }) { Text("Nghe cả đoạn") }
                        ChineseText(question.prompt, pinyin = showPinyin, meaning = false)
                        Text(if (topic.starter) "Chọn câu trả lời đúng theo đoạn văn:" else "Chọn nghĩa đúng của câu:", color = studio.muted)
                        question.options.forEachIndexed { n, option ->
                            OutlinedButton(onClick = { selection = n; checked = null }, modifier = Modifier.fillMaxWidth().testTag("zh-answer-$n")) {
                                Text((if (selection == n) "● " else "○ ") + option)
                            }
                        }
                        TextButton(onClick = { checked = selection == question.answer }, enabled = selection >= 0) { Text("Kiểm tra đáp án") }
                        checked?.let { Text(if (it) "Đúng nội dung đoạn văn" else "Chưa đúng. Đọc lại đoạn văn nhé.", color = if (it) studio.green else studio.incorrectWord) }
                        Text(status, color = studio.muted, fontSize = 12.sp)
                    }
                    CoralButton(if (index == questions.lastIndex) "Hoàn thành phần này" else "Câu tiếp theo",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp).testTag("zh-next"), enabled = checked == true,
                        onClick = {
                            if (store.completeComprehension(topic, index, selection)) {
                                clearSpeech(); if (index < questions.lastIndex) item++ else mode = ""
                            } else status = "Chưa lưu được câu trả lời. Hãy thử lại."
                        })
                }
            }
            else -> {
                val lines = when (mode) {
                    "chat" -> topic.turns.map { it.reply(topic.phrases) }
                    "read" -> topic.reading
                    "exercise" -> topic.exercises
                    else -> topic.phrases
                }
                val index = item.coerceIn(lines.indices)
                val line = if (mode == "read" && wholeReading) ChineseLine("whole", lines.joinToString("") { it.hanzi }, lines.joinToString(" ") { it.pinyin }, lines.joinToString(" ") { it.vietnamese }) else lines[index]
                val referenceTexts = listOf(line.hanzi) + line.alternatives
                val referenceIndex = referenceTexts.indexOfFirst { matchChinese(it, finalTranscript).exact }.coerceAtLeast(0)
                val reference = referenceTexts[referenceIndex]
                val referencePinyin = if (referenceIndex == 0) line.pinyin else line.alternativePinyin.getOrNull(referenceIndex - 1).orEmpty()
                val match = matchChinese(reference, finalTranscript)
                val accepted = phase == "idle" && if (mode == "exercise") match.exact else match.canContinue
                LaunchedEffect(topic.id, mode, item, automatic) {
                    if (automatic && mode in listOf("repeat", "chat")) speak(if (mode == "chat") topic.turns[index].prompt.hanzi else line.hanzi)
                }
                var typed by remember(topic.id, mode, item) { mutableStateOf("") }
                var typedResult by remember(topic.id, mode, item) { mutableStateOf<Boolean?>(null) }
                var reveal by remember(topic.id, mode, item) { mutableStateOf(false) }
                Column(Modifier.fillMaxSize().testTag("zh-practice")) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PageHeading(if (mode == "repeat") "Luyện nghe & nói" else modeTitle(mode), "${topic.title} • ${index + 1}/${lines.size}", ::back)
                        StudioProgress((index + 1f) / lines.size)
                        if (mode == "repeat") {
                            TwoChoices("Tự động", "Thủ công", automatic, { selected ->
                                if (automatic != selected) {
                                    clearSpeech(); automatic = selected
                                    preferences.edit().putBoolean("zh_automatic_mode", selected).apply()
                                }
                            }, Modifier.fillMaxWidth())
                        } else Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(75, 100, 125).forEach { value -> FilterChip(selected = speed == value, onClick = {
                                clearSpeech(); speed = value; preferences.edit().putInt("zh_playback_speed", value).apply()
                            }, label = { Text(speedLabel(value)) }) }
                            TextButton(onClick = { clearSpeech(); voiceDialog = true }) { Text("Giọng đọc") }
                        }
                        if (mode == "read") {
                            Text(topic.readingTitle, fontWeight = FontWeight.Bold)
                            Text(topic.reading.joinToString("") { it.hanzi }, fontSize = 18.sp, lineHeight = 29.sp)
                            TextButton(onClick = { speak(topic.reading.joinToString("") { it.hanzi }) }, enabled = phase == "idle") { Text("Nghe cả đoạn") }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(!wholeReading, { clearSpeech(); wholeReading = false }, label = { Text("Từng câu") })
                                FilterChip(wholeReading, { clearSpeech(); wholeReading = true }, label = { Text("Cả đoạn") })
                            }
                            Text("Luyện từng câu trong đoạn văn bên dưới.", fontSize = 12.sp, color = studio.muted)
                            baseTopic?.readingPacks?.firstOrNull { it.id == packId }?.vocabulary?.forEach { word ->
                                Text("${word.hanzi} • ${word.pinyin} • ${word.vietnamese}", fontSize = 13.sp, color = studio.muted)
                            }
                        }
                        if (mode == "chat") {
                            val prompt = topic.turns[index].prompt
                            StudioCard {
                                ChineseText(prompt, showPinyin, showMeaning)
                                TextButton(onClick = { speak(prompt.hanzi) }) { Text("Nghe câu hỏi") }
                            }
                            Text("Lượt trả lời của bạn", fontWeight = FontWeight.Bold)
                        }
                        if (mode == "exercise") {
                            Text("Nói hoặc nhập bằng chữ Hán:", color = studio.muted)
                            Text(line.vietnamese, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = typed, onValueChange = { typed = it; typedResult = null }, label = { Text("Câu trả lời tiếng Trung") }, modifier = Modifier.fillMaxWidth())
                            TextButton(onClick = { typedResult = (listOf(line.hanzi) + line.alternatives).any { matchChinese(it, typed).exact } }, enabled = typed.isNotBlank() && phase == "idle") { Text("Kiểm tra câu đã nhập") }
                            typedResult?.let { Text(if (it) "Khớp đáp án mẫu • Câu đã nhập" else "Chưa khớp đáp án mẫu", color = if (it) studio.green else studio.incorrectWord) }
                            TextButton(onClick = { reveal = !reveal }) { Text(if (reveal) "Ẩn đáp án gợi ý" else "Xem đáp án gợi ý") }
                        }
                        if (mode != "exercise" || reveal) StudioCard {
                            if (finalTranscript == null) ChineseText(line, showPinyin, showMeaning && mode != "exercise", centered = mode == "repeat")
                            else {
                                Text(chineseFeedback(reference, match), fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
                                if (showPinyin) Text(referencePinyin, fontSize = 18.sp, lineHeight = 26.sp, color = studio.purple)
                                if (showMeaning && mode != "exercise") Text(line.vietnamese)
                            }
                            if (mode == "repeat") PracticePlaybackControls(speed, showMeaning, { speak(line.hanzi) }, { value ->
                                playback.stop(); speed = value
                                preferences.edit().putInt("zh_playback_speed", value).apply()
                            }, { showMeaning = !showMeaning }, sampleEnabled = phase == "idle")
                        }
                        if (mode == "repeat") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                TextButton(onClick = { showPinyin = !showPinyin }) { Text(if (showPinyin) "Ẩn Pinyin" else "Hiện Pinyin") }
                                TextButton(onClick = { clearSpeech(); voiceDialog = true }) { Text("Giọng đọc") }
                            }
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                    DecorativeWave(); Microphone(onClick = ::listen, large = true, active = phase == "listening"); DecorativeWave()
                                }
                                Text(captureLabel(phase, "Chạm để nói"), color = studio.muted)
                            }
                            Text(topic.tip, color = studio.muted, fontSize = 13.sp)
                        } else {
                            if (mode == "chat") FilterChip(automatic, onClick = {
                                clearSpeech(); automatic = !automatic; preferences.edit().putBoolean("zh_automatic_mode", automatic).apply()
                            }, label = { Text("Tự động nghe mẫu") })
                            if (mode != "exercise") Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = showPinyin, onClick = { showPinyin = !showPinyin }, label = { Text("Pinyin") })
                                FilterChip(selected = showMeaning, onClick = { showMeaning = !showMeaning }, label = { Text("Nghĩa tiếng Việt") })
                            }
                            Text(topic.tip, color = studio.muted, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(onClick = { speak(line.hanzi) }, enabled = phase == "idle") { Text("Nghe mẫu") }
                                Microphone(onClick = ::listen, active = phase == "listening")
                                Text(captureLabel(phase, "Nói"), fontSize = 13.sp)
                            }
                        }
                        Text(status, color = studio.muted, fontSize = 12.sp)
                        if (finalTranscript != null) {
                            StudioCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LearnerAvatar(learner.avatar, Modifier.size(28.dp), square = true)
                                    IconButton(onClick = { playback.stop(); capture.replay() }, enabled = hasRecording && phase == "idle",
                                        modifier = Modifier.semantics { contentDescription = "Nghe lại giọng vừa ghi" }) { StudioIcon("speaker", Modifier.size(22.dp), studio.coral) }
                                    Text(chineseFeedback(transcript, matchChinese(transcript, reference)), fontStyle = FontStyle.Italic, modifier = Modifier.weight(1f))
                                }
                                if (finalTranscript != null) {
                                    Text("Khớp ${match.correct}/${match.tokens.size} đơn vị • ${match.matchPercent}%" + if (match.extra > 0) " • Thêm ${match.extra} đơn vị" else "", fontSize = 13.sp)
                                    Text(if (match.negationMismatch) "Khác từ phủ định • Cần nói lại để giữ đúng nghĩa"
                                        else if (accepted) "✓ Có thể chuyển câu" else "Cần nói lại", fontSize = 12.sp,
                                        color = if (accepted) studio.green else studio.incorrectWord)
                                }
                            }
                        }
                        Text("Đối chiếu chữ Hán và tên Latin; chưa chấm thanh điệu. Nghe/nói cần sai dưới 60% và giữ đúng từ phủ định. Bài dịch cần khớp toàn bộ đáp án mẫu.", fontSize = 12.sp, color = studio.muted)
                    }
                    CoralButton(if (mode == "repeat" && !accepted) "Cần nói lại" else if (index == lines.lastIndex) "Hoàn thành phần này" else "Câu tiếp theo",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp).testTag("zh-next"),
                        enabled = accepted || (phase == "idle" && typedResult == true), onClick = {
                            val result = if (typedResult == true && mode == "exercise") (listOf(line.hanzi) + line.alternatives).map { matchChinese(it, typed) }.first { it.exact } else match
                            if (if (mode == "read" && wholeReading) lines.indices.all { store.complete(topic, mode, it, result) } else store.complete(topic, mode, index, result)) {
                                clearSpeech()
                                if (!wholeReading && index < lines.lastIndex) item++ else mode = ""
                            } else status = "Chưa lưu được tiến độ hoặc câu chưa đạt. Hãy thử lại."
                        })
                }
            }
        }
    }
}
private fun modeTitle(mode: String) = when (mode) {
    "repeat" -> "Nghe và luyện nói"; "chat" -> "Hội thoại mẫu"; "read" -> "Đọc và luyện nói theo đoạn văn"; "review" -> "Đọc hiểu"; else -> "Việt → Trung"
}
@Composable
private fun ChineseText(line: ChineseLine, pinyin: Boolean, meaning: Boolean, centered: Boolean = false) {
    val alignment = if (centered) TextAlign.Center else TextAlign.Start
    val width = if (centered) Modifier.fillMaxWidth() else Modifier
    Text(line.hanzi, modifier = width, textAlign = alignment, fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
    if (pinyin) Text(line.pinyin, modifier = width, textAlign = alignment, color = studio.purple, fontSize = 18.sp, lineHeight = 26.sp)
    if (meaning) Text(line.vietnamese, modifier = width, textAlign = alignment, fontSize = 16.sp, lineHeight = 24.sp)
}
@Composable
private fun chineseFeedback(text: String, match: ChineseMatch): androidx.compose.ui.text.AnnotatedString {
    val palette = studio
    return buildAnnotatedString {
    // Preserve original punctuation and whitespace. Match normalization affects colour only.
    var index = 0
    Regex("\\p{IsHan}|[\\p{L}\\p{M}]+|[0-9]+|[^\\p{L}\\p{M}0-9]+").findAll(text).forEach { part ->
        val count = chineseTokens(part.value).size
        if (count == 0) append(part.value)
        else {
            val correct = (index until index + count).all { match.matched.getOrNull(it) == true }
            withStyle(SpanStyle(color = if (correct) palette.green else palette.incorrectWord)) { append(part.value) }
            index += count
        }
    }
}
}
