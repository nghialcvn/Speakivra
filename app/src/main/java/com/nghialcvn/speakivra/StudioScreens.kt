package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt

internal fun lessonLabel(lesson: Lesson): String = when (lesson.id) {
    "greetings" -> "Chào hỏi"; "family" -> "Gia đình"; "routine" -> "Sinh hoạt hằng ngày"
    "food" -> "Đồ ăn & thức uống"; "shopping" -> "Mua sắm"; "directions" -> "Hỏi đường"
    "hobbies" -> "Sở thích"; "time" -> "Giờ & ngày tháng"; "travel" -> "Du lịch"
    "health" -> "Sức khỏe"; "weather" -> "Thời tiết"; "transport" -> "Phương tiện"
    "workplace" -> "Công việc & trường học"; "technology" -> "Công nghệ"
    "a2_experiences" -> "Trải nghiệm"; "a2_plans" -> "Lập kế hoạch"
    "a2_opinions" -> "Nêu ý kiến"; "a2_problems" -> "Giải quyết vấn đề"
    "a2_travel" -> "Du lịch & dịch vụ"
    else -> "Giao tiếp thường ngày"
}
internal fun speedLabel(speed: Int): String = when (speed) { 75 -> "0.75x"; 125 -> "1.25x"; else -> "1x" }

@Composable
internal fun StudioCard(modifier: Modifier = Modifier, color: Color = studio.card,
                        border: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(color)
        .then(if (border) Modifier.border(1.dp, studio.border, RoundedCornerShape(18.dp)) else Modifier)
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}
@Composable
internal fun CoralButton(label: String, modifier: Modifier = Modifier, enabled: Boolean = true,
                         chevron: Boolean = true, onClick: () -> Unit) {
    val p = studio
    val brush = Brush.horizontalGradient(listOf(Color(0xFFEE7B65), Color(0xFFD84B38)))
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
        .background(if (enabled) brush else Brush.horizontalGradient(listOf(p.border, p.border)))
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .heightIn(min = 50.dp).padding(horizontal = 20.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (enabled) Color.White else p.muted, fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (chevron) 20.dp else 0.dp))
        if (!enabled && label == "Cần nói lại") StudioIcon("lock", Modifier.size(19.dp).align(Alignment.CenterStart), p.muted)
        if (chevron) StudioIcon("chevron", Modifier.size(18.dp).align(Alignment.CenterEnd), if (enabled) Color.White else p.muted)
    }
}
@Composable
internal fun PageHeading(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.size(42.dp).offset(x = (-8).dp)) {
            Box(Modifier.semantics { contentDescription = "Quay lại" }) { StudioIcon("back") }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = if (onBack == null) 25.sp else 21.sp, fontWeight = FontWeight.ExtraBold, color = studio.ink)
            if (subtitle != null) Text(subtitle, fontSize = 14.sp, color = studio.muted)
        }
    }
}
@Composable
private fun Brand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra", Modifier.size(49.dp))
        Column {
            Text("Speakivra", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = studio.ink)
            Text("Listen. Speak. Improve.", color = studio.coral, fontStyle = FontStyle.Italic, fontSize = 11.sp)
        }
    }
}
/** Use the actual pixels of the approved artwork, without generating replacement art. */
@Composable
private fun ApprovedArtwork(headphones: Boolean, modifier: Modifier = Modifier) {
    val bitmap = ImageBitmap.imageResource(R.drawable.approved_light_demo)
    Canvas(modifier) {
        drawImage(bitmap, srcOffset = if (headphones) IntOffset(319, 271) else IntOffset(123, 773),
            srcSize = if (headphones) IntSize(79, 74) else IntSize(128, 73),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.High)
    }
}
@Composable
internal fun SectionPill(text: String, color: Color = studio.peach, ink: Color = studio.coral) {
    Text(text, color = ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal = 12.dp, vertical = 6.dp))
}
@Composable
internal fun StudioProgress(value: Float, modifier: Modifier = Modifier) {
    val progress = value.coerceIn(0f, 1f)
    Box(modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(studio.border).progressSemantics(progress)) {
        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(studio.coral, CircleShape))
    }
}
@Composable
internal fun StudioBottomNavigation(page: String, onNavigate: (String) -> Unit) {
    val p = studio
    val active = when (page) { "lessons" -> "home"; "review", "summary" -> "repeat"; "profile", "updates" -> "profile"; else -> page }
    Row(Modifier.fillMaxWidth().background(p.card).border(BorderStroke(0.5.dp, p.border))
        .padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(Triple("home", "home", "Trang chủ"), Triple("repeat", "mic", "Luyện nói"),
            Triple("chat", "chat", "Hội thoại"), Triple("profile", "bell", "Cập nhật")).forEach { (id, icon, label) ->
            val selected = active == id
            Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                .clickable(role = Role.Tab, onClick = { onNavigate(id) })
                .semantics { this.selected = selected }.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StudioIcon(icon, Modifier.size(22.dp), if (selected) p.coral else p.muted)
                Text(label, color = if (selected) p.coral else p.ink, fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
            }
        }
    }
}
@Composable
internal fun HomeScreen(lesson: Lesson, progress: LessonProgress, goal: Int, completed: Int, learnerName: String,
                        onContinue: () -> Unit, onLessons: () -> Unit, onChat: () -> Unit, onProfile: () -> Unit,
                        onA2: () -> Unit = {}) {
    val p = studio
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Brand(Modifier.clickable(onClick = onProfile))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Chào $learnerName!", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = p.ink)
            Text("Hôm nay, mình cùng luyện nói nhé.", color = p.muted, fontSize = 14.sp)
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(p.peach).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("$goal phút mỗi ngày", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = p.ink)
                Text("Luyện nghe, luyện nói,\ntiến bộ từng chút mỗi ngày.", fontSize = 14.sp, color = p.ink, lineHeight = 20.sp)
            }
            ApprovedArtwork(true, Modifier.size(94.dp, 88.dp).clip(RoundedCornerShape(14.dp)))
        }
        StudioCard(color = p.lavender, border = false) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(49.dp).clip(RoundedCornerShape(14.dp)).background(p.card.copy(alpha = 0.7f)), Alignment.Center) {
                    StudioIcon("chat", Modifier.size(30.dp), p.purple)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Bài ${Lessons.all.indexOfFirst { it.id == lesson.id } + 1} • ${lessonLabel(lesson)}", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text(lesson.title, fontSize = 14.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Câu ${progress.phrase + 1}/${lesson.phrases.size}", fontSize = 13.sp, color = p.muted)
                StudioProgress((progress.phrase + 1f) / lesson.phrases.size, Modifier.weight(1f))
            }
            CoralButton(if (progress.phase == "summary") "Xem tổng kết" else "Tiếp tục học", onClick = onContinue)
            if (completed > 0) Text("Đã hoàn thành: $completed lượt bài", color = p.muted, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.weight(1f).heightIn(min = 76.dp).clip(RoundedCornerShape(16.dp)).background(p.mint)
                .clickable(role = Role.Button, onClick = onLessons).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioIcon("layers", Modifier.size(28.dp), p.green)
                Text("15 chủ đề A1", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                StudioIcon("chevron", Modifier.size(12.dp), p.green)
            }
            Row(Modifier.weight(1f).heightIn(min = 76.dp).clip(RoundedCornerShape(16.dp)).background(p.lavender)
                .clickable(role = Role.Button, onClick = onChat).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioIcon("chat", Modifier.size(26.dp), p.purple)
                Column(Modifier.weight(1f)) {
                    Text("Hội thoại mẫu", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Mô phỏng", color = p.coral, fontSize = 11.sp,
                        modifier = Modifier.background(p.peach, RoundedCornerShape(8.dp)).padding(horizontal = 5.dp, vertical = 2.dp))
                }
                StudioIcon("chevron", Modifier.size(12.dp), p.purple)
            }
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(p.lavender)
            .border(1.dp, p.border, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onA2).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StudioIcon("book", Modifier.size(26.dp), p.purple)
            Column(Modifier.weight(1f)) {
                Text("A2 • Sơ trung cấp", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Text("5 chủ đề • 60 câu luyện nói • 25 lượt hội thoại", color = p.muted, fontSize = 12.sp)
            }
            StudioIcon("chevron", Modifier.size(16.dp), p.purple)
        }
    }
}
@Composable
internal fun LessonsScreen(selected: String, load: (Lesson) -> LessonProgress, onOpen: (Lesson) -> Unit,
                           lessons: List<Lesson> = Lessons.a1, level: String = "A1") {
    val p = studio
    val icons = listOf("handshake", "family", "calendar", "food", "bag", "map", "book", "clock", "plane", "chat")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading("Chọn bài học $level", if (level == "A1") "15 chủ đề • 300 câu nói • 100 lượt hội thoại" else "5 chủ đề A2 • 60 câu nói • 25 lượt hội thoại")
        Spacer(Modifier.height(3.dp))
        lessons.forEachIndexed { index, lesson ->
            val value = load(lesson)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (selected == lesson.id) p.peach else p.card)
                .border(1.dp, if (selected == lesson.id) Color.Transparent else p.border, RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = { onOpen(lesson) }).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(33.dp).background(p.peach, CircleShape), Alignment.Center) {
                    Text((index + 1).toString().padStart(2, '0'), color = p.coral, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                StudioIcon(icons.getOrElse(index) { "book" }, Modifier.size(28.dp), p.coral)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(lessonLabel(lesson), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(lesson.title, color = p.muted, fontSize = 11.sp)
                    if (value.rounds > 0) Text("Đã học ${value.rounds} lượt", color = p.green, fontSize = 11.sp)
                }
                if (selected == lesson.id) Text("${value.phrase + 1}/${lesson.phrases.size} câu", fontSize = 11.sp, color = p.muted)
                StudioIcon("chevron", Modifier.size(13.dp), p.muted)
            }
        }
        Text("${lessons.size} chủ đề $level • Học theo nhịp của bạn", color = p.purple, textAlign = TextAlign.Center,
            fontSize = 13.sp, fontStyle = FontStyle.Italic, modifier = Modifier.fillMaxWidth()
                .background(p.lavender, RoundedCornerShape(12.dp)).padding(10.dp))
    }
}
@Composable
internal fun TwoChoices(first: String, second: String, selectedFirst: Boolean, onSelect: (Boolean) -> Unit,
                        modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(13.dp)).background(studio.card)
        .border(1.dp, studio.border, RoundedCornerShape(13.dp))) {
        listOf(first to true, second to false).forEach { (label, value) ->
            val active = value == selectedFirst
            Box(Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
                .background(if (active) studio.coral else Color.Transparent)
                .clickable(role = Role.RadioButton, onClick = { onSelect(value) }).semantics { selected = active }
                .padding(horizontal = 12.dp, vertical = 9.dp), Alignment.Center) {
                Text(label, color = if (active) MaterialTheme.colorScheme.onPrimary else studio.ink,
                    fontSize = 13.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal)
            }
        }
    }
}
@Composable
private fun SpeakerChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(12.dp)).background(studio.peach)
        .clickable(role = Role.Button, onClick = onClick).heightIn(min = 44.dp).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        StudioIcon("speaker", Modifier.size(19.dp), studio.coral)
        Text(label, color = studio.coral, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}
@Composable
internal fun Microphone(onClick: () -> Unit, modifier: Modifier = Modifier, large: Boolean = false, active: Boolean = false) {
    val size = if (large) 96.dp else 64.dp
    Box(modifier.size(size + 14.dp).background(studio.peach.copy(alpha = 0.7f), CircleShape).padding(7.dp)
        .shadow(3.dp, CircleShape).background(Brush.linearGradient(listOf(Color(0xFFFF9B82), Color(0xFFDA4B38))), CircleShape)
        .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape).clip(CircleShape)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = if (active) "Kết thúc lượt nói" else "Nói và nhận dạng" }, Alignment.Center) {
        if (active) Box(Modifier.size(if (large) 25.dp else 19.dp).background(Color.White, RoundedCornerShape(4.dp)))
        else StudioIcon("mic", Modifier.size(if (large) 43.dp else 31.dp), Color.White)
    }
}
@Composable
private fun DecorativeWave() {
    val color = studio.coralLight.copy(alpha = 0.5f)
    Canvas(Modifier.width(64.dp).height(48.dp)) {
        listOf(.2f, .42f, .72f, 1f, .62f, .32f).forEachIndexed { i, h ->
            val x = size.width * (i + .5f) / 6
            drawLine(color, Offset(x, size.height * (1 - h) / 2), Offset(x, size.height * (1 + h) / 2),
                strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}
@Composable
internal fun VoiceSelectionChips(choice: VoiceChoice, onOpen: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        listOf(
            (if (choice.accent == "GB") "🇬🇧 English (UK)" else "🇺🇸 English (US)") to "Ngôn ngữ phát âm",
            (if (choice.gender == "male") "🔊 Giọng nam" else "🔊 Giọng nữ") to "Giọng đọc"
        ).forEach { (label, description) ->
            Row(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(studio.lavender)
                    .border(1.dp, studio.border, RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button, onClick = onOpen)
                    .semantics { contentDescription = description }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = studio.ink, maxLines = 1)
                Spacer(Modifier.width(4.dp))
                Text("⌄", color = studio.purple, fontSize = 13.sp)
            }
        }
    }
}

@Composable
internal fun PracticeScreen(lesson: Lesson, progress: LessonProgress, automatic: Boolean, translation: Boolean,
                            speed: Int, transcript: String, status: String, capturePhase: String, wordMatch: WordMatch, learner: Learner, onBack: () -> Unit,
                            onMode: (Boolean) -> Unit, onSpeed: (Int) -> Unit, onTranslation: () -> Unit,
                            onListen: () -> Unit, onSpeak: () -> Unit, onNext: () -> Unit,
                            voiceChoice: VoiceChoice = VoiceChoice(), onVoiceSettings: () -> Unit = {}) {
    val p = studio; val phrase = lesson.phrases[progress.phrase]
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeading("Luyện nghe & nói", "${lessonLabel(lesson)} • Câu ${progress.phrase + 1}/${lesson.phrases.size}", onBack)
        StudioProgress((progress.phrase + 1f) / lesson.phrases.size)
        VoiceSelectionChips(voiceChoice, onVoiceSettings)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TwoChoices("Tự động", "Thủ công", automatic, onMode, Modifier.fillMaxWidth())
            StudioCard {
                val colored = buildAnnotatedString {
                    wordMatch.tokens.forEachIndexed { index, word ->
                        if (index > 0) append(" ")
                        withStyle(SpanStyle(color = if (!wordMatch.hasResult) p.ink else if (wordMatch.matched[index]) p.green else p.coral)) { append(word) }
                    }
                }
                Text(colored, fontSize = 29.sp, lineHeight = 35.sp, textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
                if (translation) Text(phrase.vietnamese, textAlign = TextAlign.Center, color = p.muted,
                    fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                // Approved compact row: sample audio, current speed dropdown and translation.
                // Only wrap for very narrow layouts or accessibility font scaling.
                var speedMenuOpen by remember { mutableStateOf(false) }
                BoxWithConstraints {
                    val split = maxWidth < 290.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
                    val controls: @Composable () -> Unit = {
                        Box {
                            Row(
                                Modifier.clip(RoundedCornerShape(9.dp))
                                    .border(1.dp, p.border, RoundedCornerShape(9.dp))
                                    .clickable(role = Role.Button, onClick = { speedMenuOpen = true })
                                    .semantics { contentDescription = "Tốc độ đọc: ${speedLabel(speed)}" }
                                    .heightIn(min = 44.dp).padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(speedLabel(speed), color = p.ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("⌄", color = p.coral, fontSize = 16.sp)
                            }
                            DropdownMenu(expanded = speedMenuOpen, onDismissRequest = { speedMenuOpen = false }) {
                                listOf(75, 100, 125).forEach { value ->
                                    DropdownMenuItem(
                                        text = { Text(speedLabel(value), color = p.ink) },
                                        onClick = {
                                            speedMenuOpen = false
                                            onSpeed(value)
                                        },
                                        modifier = Modifier.semantics { selected = speed == value }
                                    )
                                }
                            }
                        }
                        Box(Modifier.clip(RoundedCornerShape(9.dp))
                            .border(1.dp, p.border, RoundedCornerShape(9.dp))
                            .clickable(role = Role.Button, onClick = onTranslation)
                            .heightIn(min = 44.dp).padding(horizontal = 12.dp), Alignment.Center) {
                            Text(if (translation) "Ẩn dịch" else "Hiện dịch", fontSize = 12.sp)
                        }
                    }
                    if (split) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SpeakerChip("Nghe mẫu", onListen)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                controls()
                            }
                        }
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpeakerChip("Nghe mẫu", onListen)
                            controls()
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    DecorativeWave(); Microphone(onSpeak, large = true, active = capturePhase == "listening"); DecorativeWave()
                }
                Text(captureLabel(capturePhase, if (wordMatch.hasResult) "Chạm để nói lại" else "Chạm để nói"), fontSize = 15.sp, color = p.muted)
            }
            if (capturePhase == "idle" && wordMatch.hasResult && transcript.isNotBlank()) SpokenResult(learner, transcript)
            StudioCard(color = p.mint, border = false) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudioIcon("bulb", Modifier.size(27.dp), p.green)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Mẹo luyện âm", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(phrase.tip, fontSize = 14.sp)
                    }
                }
            }
            if (wordMatch.hasResult) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa khớp ${wordMatch.incorrect}/${wordMatch.tokens.size} từ · ${wordMatch.percent}%", color = p.coral, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(if (wordMatch.canContinue) "✓ Có thể chuyển câu" else "Hãy nghe mẫu và nói lại nhé", color = if (wordMatch.canContinue) p.green else p.ink, fontSize = 13.sp, modifier = Modifier.weight(1f))
            }
            Text("Đối chiếu từ nhận dạng, chưa chấm phát âm.", fontSize = 12.sp, color = p.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (capturePhase != "idle" || (!wordMatch.hasResult && status != "Nhấn Nghe mẫu để bắt đầu")) Text(status, fontSize = 12.sp, color = p.muted, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        val allowed = wordMatch.canContinue && capturePhase == "idle"
        CoralButton(if (!allowed) "Cần nói lại" else if (progress.phrase == lesson.phrases.lastIndex) "Sang hội thoại" else "Câu tiếp theo", enabled = allowed, chevron = allowed, onClick = onNext)
    }
}
@Composable
internal fun ConversationScreen(lesson: Lesson, turnIndex: Int, translation: Boolean, transcript: String, capturePhase: String, status: String, learner: Learner,
                                nextLabel: String, onBack: () -> Unit, onPrompt: () -> Unit, onReply: () -> Unit,
                                onSpeak: () -> Unit, onTranslation: () -> Unit, onNext: () -> Unit,
                                voiceChoice: VoiceChoice = VoiceChoice(), onVoiceSettings: () -> Unit = {},
                                neon: Boolean = false) {
    if (neon) {
        NeonConversationScreen(lesson, turnIndex, translation, transcript, capturePhase, status, learner,
            nextLabel, onBack, onPrompt, onReply, onSpeak, onTranslation, onNext, voiceChoice, onVoiceSettings)
        return
    }
    val p = studio; val turn = lesson.conversation[turnIndex]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeading("Emma • Luyện hội thoại", onBack = onBack)
        VoiceSelectionChips(voiceChoice, onVoiceSettings)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { SectionPill("Mô phỏng theo kịch bản") }
        ApprovedArtwork(false, Modifier.size(128.dp, 73.dp).clip(RoundedCornerShape(28.dp)))
        StudioCard(color = p.lavender, border = false) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(turn.prompt, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    if (translation) Text(turn.translation, color = p.muted, fontSize = 14.sp)
                }
                IconButton(onClick = onPrompt, Modifier.semantics { contentDescription = "Nghe Emma" }) {
                    StudioIcon("speaker", color = p.coral)
                }
            }
        }
        StudioCard(color = p.mint, border = false) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioIcon("chat", Modifier.size(22.dp), p.green)
                Text("Gợi ý trả lời", fontWeight = FontWeight.ExtraBold)
            }
            Text(turn.reply, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            if (translation) Text(turn.replyTranslation, fontSize = 14.sp, color = p.muted)
        }
        Box(Modifier.fillMaxWidth().height(114.dp), Alignment.Center) {
            CoralButton(captureLabel(capturePhase, "Nói câu trả lời"), Modifier.widthIn(max = 270.dp).align(Alignment.BottomCenter), chevron = false, onClick = onSpeak)
            Microphone(onSpeak, Modifier.align(Alignment.TopCenter), active = capturePhase == "listening")
        }
        if (capturePhase == "idle" && transcript.isNotBlank()) SpokenResult(learner, transcript)
        Text(status, fontSize = 12.sp, color = p.muted, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            SpeakerChip("Nghe trả lời mẫu", onReply)
            Text("Lượt ${turnIndex + 1}/${lesson.conversation.size}", color = p.muted, fontSize = 13.sp)
        }
        TextButton(onClick = onTranslation) { Text(if (translation) "Ẩn dịch" else "Hiện dịch", fontSize = 12.sp) }
        CoralButton(nextLabel, onClick = onNext)
        Text("Không gửi bản ghi tới AI. Nhận dạng giọng nói có thể dùng dịch vụ của thiết bị.", color = p.muted, fontSize = 11.sp)
    }
}
@Composable
internal fun ReviewScreen(lesson: Lesson, progress: LessonProgress, selection: Int, transcript: String, capturePhase: String, status: String, learner: Learner,
                          onSelect: (Int) -> Unit, onBack: () -> Unit, onListen: () -> Unit, onSpeak: () -> Unit, onNext: () -> Unit) {
    val p = studio; val question = reviewQuestion(lesson, progress.review)
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        PageHeading("Cùng ôn lại nhé", "${lessonLabel(lesson)} • Câu ${progress.review + 1}/3", onBack)
        StudioProgress((progress.review + 1f) / 3)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StudioCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).background(p.peach, RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button, onClick = onListen).semantics { contentDescription = "Nghe lại câu" }, Alignment.Center) {
                        StudioIcon("speaker", color = p.coral)
                    }
                    Text(question.phrase.english, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text("Chọn nghĩa phù hợp", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            question.options.forEachIndexed { index, label ->
                val right = selection >= 0 && index == question.answer
                val wrong = selection == index && !right
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (right) p.mint else if (wrong) p.peach else p.card)
                    .border(1.dp, if (right) p.mint else p.border, RoundedCornerShape(16.dp))
                    .clickable(enabled = selection < 0, role = Role.RadioButton, onClick = { onSelect(index) })
                    .semantics { selected = selection == index }.heightIn(min = 54.dp).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(21.dp).border(1.5.dp, if (selection == index) p.coral else p.muted.copy(alpha = 0.5f), CircleShape).padding(4.dp)) {
                        if (selection == index) Box(Modifier.fillMaxSize().background(p.coral, CircleShape))
                    }
                    Text(label, fontSize = 15.sp, fontWeight = if (right) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    if (right) StudioIcon("check", Modifier.size(21.dp), p.green)
                    if (wrong) StudioIcon("close", Modifier.size(19.dp), p.coral)
                }
            }
            if (selection >= 0) StudioCard(color = p.mint, border = false) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    StudioIcon("check", color = p.green)
                    Column {
                        Text(if (selection == question.answer) "Đúng rồi!" else "Đáp án: ${question.phrase.vietnamese}", color = p.green, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Ôn nghĩa câu, không chấm phát âm.", color = p.muted, fontSize = 13.sp)
                    }
                }
                TextButton(onClick = onSpeak) { StudioIcon("mic", Modifier.size(19.dp), p.green); Spacer(Modifier.width(8.dp)); Text(captureLabel(capturePhase, "Nhắc lại câu này")) }
                Text(status, color = p.muted, fontSize = 12.sp)
                if (capturePhase == "idle" && transcript.isNotBlank()) SpokenResult(learner, transcript)
            }
        }
        CoralButton(if (progress.review == 2) "Hoàn thành và tổng kết" else "Tiếp tục", enabled = selection >= 0, onClick = onNext)
    }
}
@Composable
private fun SettingsRow(icon: String, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
        .heightIn(min = 54.dp).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        StudioIcon(icon, Modifier.size(24.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = studio.muted, fontSize = 14.sp)
        StudioIcon("chevron", Modifier.size(12.dp), studio.muted)
    }
}
@Composable
internal fun ProfileScreen(dark: Boolean, speed: Int, goal: Int, version: String, learner: Learner, onSwitchLearner: () -> Unit, onEditLearner: () -> Unit, onTheme: (Boolean) -> Unit,
                           onVoice: () -> Unit, onSpeed: () -> Unit, onGoal: () -> Unit, onUpdates: () -> Unit,
                           voiceChoice: VoiceChoice = VoiceChoice(),
                           neon: Boolean = false, onNeon: () -> Unit = {}) {
    if (neon) {
        NeonProfileScreen(speed, goal, version, learner, voiceChoice, onSwitchLearner, onEditLearner,
            onTheme, onNeon, onVoice, onSpeed, onGoal, onUpdates)
        return
    }
    val p = studio
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PageHeading("Cá nhân & cài đặt")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(17.dp)) {
            Box(Modifier.size(76.dp)) {
                LearnerAvatar(learner.avatar, Modifier.fillMaxSize())
                Box(Modifier.align(Alignment.BottomEnd).size(30.dp).clip(CircleShape).background(p.coral)
                    .border(2.dp, p.background, CircleShape).clickable(role = Role.Button, onClick = onEditLearner)
                    .semantics { contentDescription = "Chỉnh sửa hồ sơ" }, Alignment.Center) {
                    StudioIcon("pencil", Modifier.size(16.dp), Color.White)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(learner.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text("Học theo nhịp của bạn", color = p.muted, fontSize = 14.sp)
            }
        }
        TextButton(onClick = onSwitchLearner) { Text("Đổi người học") }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(p.card).border(1.dp, p.border, RoundedCornerShape(18.dp))) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioIcon("sun"); Text("Giao diện", Modifier.weight(1f), fontSize = 14.sp)
                TwoChoices("Sáng", "Tối", !dark, { onTheme(!it) }, Modifier.width(130.dp))
                TextButton(onClick = onNeon) {
                    Text("Neon", color = studio.coral, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            HorizontalDivider(color = p.border, modifier = Modifier.padding(horizontal = 12.dp))
            SettingsRow("speaker", "Giọng đọc & Ngôn ngữ", "${voiceChoice.accentLabel} · ${voiceChoice.genderLabel}", onVoice)
            HorizontalDivider(color = p.border, modifier = Modifier.padding(horizontal = 12.dp))
            SettingsRow("gauge", "Tốc độ mặc định", speedLabel(speed), onSpeed)
            HorizontalDivider(color = p.border, modifier = Modifier.padding(horizontal = 12.dp))
            SettingsRow("target", "Mục tiêu mỗi ngày", "$goal phút", onGoal)
        }
        StudioCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra", Modifier.size(32.dp))
                Text("Speakivra v$version", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onUpdates, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, p.coral)) {
                    Text("Kiểm tra cập nhật", color = p.coral, fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = p.border)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StudioIcon("phone", Modifier.size(18.dp), p.muted)
                Text("Tiến độ lưu trên thiết bị", color = p.muted, fontSize = 13.sp)
            }
        }
    }
}

internal fun captureLabel(phase: String, idle: String) = when (phase) {
    "listening" -> "Chạm để kết thúc"
    "processing" -> "Đang xử lý…"
    else -> idle
}

@Composable
internal fun SpokenResult(learner: Learner, text: String) {
    val p = studio
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(p.card)
        .border(1.dp, p.border, RoundedCornerShape(6.dp)).padding(8.dp)
        .semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LearnerAvatar(learner.avatar, Modifier.size(28.dp), square = true)
        Text(text, fontSize = 14.sp, fontStyle = FontStyle.Italic, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}
