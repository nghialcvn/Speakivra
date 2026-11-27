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
internal fun StudyHubScreen(preferences: SharedPreferences, learner: Learner, onBack: () -> Unit,
    english: @Composable () -> Unit) {
    var chinese by rememberSaveable(learner.id) { mutableStateOf(preferences.getString("study_language", "en") == "zh") }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(false to "Tiếng Anh", true to "Tiếng Trung").forEach { (selected, label) ->
                FilterChip(selected = chinese == selected, onClick = {
                    chinese = selected
                    preferences.edit().putString("study_language", if (selected) "zh" else "en").apply()
                }, label = { Text(label) }, modifier = Modifier.testTag(if (selected) "language-zh" else "language-en"))
            }
        }
        Box(Modifier.weight(1f)) {
            if (chinese) key(learner.id) { ChineseCourseScreen(preferences, learner, onBack) }
            else english()
        }
    }
}

@Composable
internal fun ChineseCourseScreen(preferences: SharedPreferences, learner: Learner, onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(preferences) { ChineseProgress(preferences) }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val topics = remember(learner.name) { ChineseCurriculum.topics.map { it.personalize(learner.name) } }
    var level by rememberSaveable { mutableStateOf("A1") }
    var topicId by rememberSaveable { mutableStateOf<String?>(null) }
    var mode by rememberSaveable { mutableStateOf("") }
    var item by rememberSaveable { mutableIntStateOf(0) }
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
            else -> onBack()
        }
    }
    BackHandler { back() }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val requested = pendingPermission; pendingPermission = false
        if (granted && requested && owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true)
            capture.start(languageTag = "zh-CN")
        else if (!granted && requested) status = "Cần cấp quyền micro để luyện nói tiếng Trung."
    }
    fun speak(text: String) {
        pendingPermission = false; capture.cancel(discardRecording = false)
        playback.speakMandarin(text, speed, voiceId)
    }
    fun listen() {
        if (phase == "listening") { capture.stop(); return }
        if (phase != "idle" || pendingPermission) return
        clearSpeech()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            capture.start(languageTag = "zh-CN")
        else { pendingPermission = true; permission.launch(Manifest.permission.RECORD_AUDIO) }
    }
    if (voiceDialog) AlertDialog(onDismissRequest = { voiceDialog = false }, title = { Text("Giọng tiếng Trung") },
        text = {
            Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                Text("Nghe thử rồi chọn giọng ngoại tuyến bạn thích. Giọng hệ thống không có nhãn nam/nữ đáng tin cậy.")
                TextButton(onClick = { voiceId = null; preferences.edit().remove("zh_voice_id").apply(); voiceDialog = false }) { Text("Tự chọn giọng phổ thông Trung Quốc") }
                val voices = playback.mandarinVoices()
                if (voices.isEmpty()) Text("Chưa tìm thấy giọng ngoại tuyến. Cài dữ liệu tiếng Trung (Trung Quốc) trong cài đặt giọng nói Android, rồi mở lại ứng dụng.")
                voices.forEachIndexed { index, voice ->
                    Text("Giọng ${index + 1}" + if (voiceId == voice.id || (voiceId == null && index == 0)) " • Đang chọn" else "", fontSize = 14.sp)
                    Row {
                        TextButton(onClick = { playback.speakMandarin("你好，很高兴认识你。", speed, voice.id) }) { Text("Nghe thử") }
                        TextButton(onClick = { playback.stop(); voiceId = voice.id; preferences.edit().putString("zh_voice_id", voice.id).apply(); voiceDialog = false }) { Text("Chọn giọng này") }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { playback.stop(); voiceDialog = false }) { Text("Đóng") } })
    CompositionLocalProvider(LocalSpeechReplay provides SpeechReplay(hasRecording && phase == "idle", { playback.stop(); capture.replay() })) {
        // Refresh cloud restores without replacing media/session state.
        val topic = remember(topics, topicId, revision) { topics.firstOrNull { it.id == topicId } }
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
            topic == null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeading("Tiếng Trung", "Phổ thông • Giản thể • Pinyin • Tiếng Việt", ::back)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("A1", "A2").forEach { value -> FilterChip(selected = level == value, onClick = { level = value }, label = { Text(value) }, modifier = Modifier.testTag("zh-level-$value")) }
                }
                Text("A1/A2 là lộ trình trong Speakivra, không phải chứng nhận HSK.", fontSize = 12.sp, color = studio.muted)
                TextButton(onClick = { primer = true }) { Text("Nhập môn Pinyin và 4 thanh điệu") }
                val levelTopics = topics.filter { it.level == level }
                Text("${levelTopics.size} chủ đề • ${levelTopics.sumOf { it.phrases.size }} câu nói • ${levelTopics.sumOf { it.turns.size }} lượt hội thoại", fontWeight = FontWeight.Bold)
                Text("Hoàn thành ${levelTopics.count { store.finished(it) }}/${levelTopics.size} chủ đề", color = studio.muted)
                AchievementSummary(levelTopics.sumOf { store.diamonds(it) })
                levelTopics.forEachIndexed { n, value ->
                    StudioCard(Modifier.fillMaxWidth().clickable { topicId = value.id; mode = "" }.testTag(value.id)) {
                        Text("${n + 1}. ${value.title}", fontWeight = FontWeight.ExtraBold)
                        Text(value.readingTitle, color = studio.muted)
                        Text(if (store.finished(value)) "Đã hoàn thành" else "${store.done(value, "repeat")}/${value.phrases.size} câu luyện nói", color = studio.purple, fontSize = 12.sp)
                        TopicDiamonds(value.title, store.diamonds(value))
                    }
                }
            }
            mode.isEmpty() -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeading(topic.title, "Tiếng Trung ${topic.level}", ::back)
                Text(topic.tip, color = studio.muted)
                TopicDiamonds(topic.title, store.diamonds(topic))
                ChineseProgress.modes.forEach { value ->
                    val title = modeTitle(value)
                    StudioCard(Modifier.fillMaxWidth().clickable { clearSpeech(); item = store.firstPending(topic, value); mode = value }.testTag("zh-mode-$value")) {
                        Text(title, fontWeight = FontWeight.ExtraBold)
                        Text("${store.done(topic, value)}/${store.itemIds(topic, value).size} • Tiếp tục từ phần chưa học", color = studio.muted, fontSize = 12.sp)
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
                        PageHeading("Đọc hiểu", "${topic.title} • ${index + 1}/${questions.size}", ::back)
                        Text(topic.readingTitle, fontWeight = FontWeight.ExtraBold)
                        Text(topic.reading.joinToString("") { it.hanzi }, fontSize = 20.sp, lineHeight = 30.sp)
                        TextButton(onClick = { speak(topic.reading.joinToString("") { it.hanzi }) }) { Text("Nghe cả đoạn") }
                        ChineseText(question.prompt, pinyin = showPinyin, meaning = false)
                        Text("Chọn câu trả lời đúng theo đoạn văn:", color = studio.muted)
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
                    "chat" -> topic.turns.map { topic.phrases[it.answerIndex] }
                    "read" -> topic.reading
                    "exercise" -> topic.exercises
                    else -> topic.phrases
                }
                val index = item.coerceIn(lines.indices)
                val line = lines[index]
                val match = matchChinese(line.hanzi, finalTranscript)
                val accepted = phase == "idle" && if (mode == "exercise") match.exact else match.canContinue
                var typed by remember(topic.id, mode, item) { mutableStateOf("") }
                var typedResult by remember(topic.id, mode, item) { mutableStateOf<Boolean?>(null) }
                var reveal by remember(topic.id, mode, item) { mutableStateOf(false) }
                Column(Modifier.fillMaxSize().testTag("zh-practice")) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PageHeading(modeTitle(mode), "${topic.title} • ${index + 1}/${lines.size}", ::back)
                        StudioProgress((index + 1f) / lines.size)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(75, 100, 125).forEach { value -> FilterChip(selected = speed == value, onClick = {
                                clearSpeech(); speed = value; preferences.edit().putInt("zh_playback_speed", value).apply()
                            }, label = { Text(speedLabel(value)) }) }
                            TextButton(onClick = { clearSpeech(); voiceDialog = true }) { Text("Giọng đọc") }
                        }
                        if (mode == "read") {
                            Text(topic.readingTitle, fontWeight = FontWeight.Bold)
                            Text(topic.reading.joinToString("") { it.hanzi }, fontSize = 18.sp, lineHeight = 29.sp)
                            TextButton(onClick = { speak(topic.reading.joinToString("") { it.hanzi }) }, enabled = phase == "idle") { Text("Nghe cả đoạn") }
                            Text("Luyện từng câu trong đoạn văn bên dưới.", fontSize = 12.sp, color = studio.muted)
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
                            TextButton(onClick = { typedResult = matchChinese(line.hanzi, typed).exact }, enabled = typed.isNotBlank() && phase == "idle") { Text("Kiểm tra câu đã nhập") }
                            typedResult?.let { Text(if (it) "Khớp đáp án mẫu • Câu đã nhập" else "Chưa khớp đáp án mẫu", color = if (it) studio.green else studio.incorrectWord) }
                            TextButton(onClick = { reveal = !reveal }) { Text(if (reveal) "Ẩn đáp án gợi ý" else "Xem đáp án gợi ý") }
                        }
                        if (mode != "exercise" || reveal) StudioCard {
                            if (finalTranscript == null) ChineseText(line, showPinyin, showMeaning && mode != "exercise")
                            else {
                                Text(chineseFeedback(line.hanzi, match), fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
                                if (showPinyin) Text(line.pinyin, fontSize = 18.sp, lineHeight = 26.sp, color = studio.purple)
                                if (showMeaning && mode != "exercise") Text(line.vietnamese)
                            }
                        }
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
                        Text(status, color = studio.muted, fontSize = 12.sp)
                        if (finalTranscript != null) {
                            StudioCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LearnerAvatar(learner.avatar, Modifier.size(28.dp), square = true)
                                    IconButton(onClick = { playback.stop(); capture.replay() }, enabled = hasRecording && phase == "idle",
                                        modifier = Modifier.semantics { contentDescription = "Nghe lại giọng vừa ghi" }) { StudioIcon("speaker", Modifier.size(22.dp), studio.coral) }
                                    Text(chineseFeedback(transcript, matchChinese(transcript, line.hanzi)), fontStyle = FontStyle.Italic, modifier = Modifier.weight(1f))
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
                    CoralButton(if (index == lines.lastIndex) "Hoàn thành phần này" else "Câu tiếp theo",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp).testTag("zh-next"),
                        enabled = accepted || (phase == "idle" && typedResult == true), onClick = {
                            val result = if (typedResult == true && mode == "exercise") matchChinese(line.hanzi, typed) else match
                            if (store.complete(topic, mode, index, result)) {
                                clearSpeech()
                                if (index < lines.lastIndex) item++ else mode = ""
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
private fun ChineseText(line: ChineseLine, pinyin: Boolean, meaning: Boolean) {
    Text(line.hanzi, fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
    if (pinyin) Text(line.pinyin, color = studio.purple, fontSize = 18.sp, lineHeight = 26.sp)
    if (meaning) Text(line.vietnamese, fontSize = 16.sp, lineHeight = 24.sp)
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
