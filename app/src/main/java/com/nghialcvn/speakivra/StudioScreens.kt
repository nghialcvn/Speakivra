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
    val neon = studio == NeonStudio
    Column(modifier.fillMaxWidth()
        .then(if (neon && color == studio.mint) Modifier
            .shadow(9.dp, RoundedCornerShape(18.dp),
                ambientColor = NeonLook.cyan, spotColor = NeonLook.blue)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(
                Color(0xF0005267), Color(0xF0124B6F))))
            .border(1.5.dp, NeonLook.cyan, RoundedCornerShape(18.dp))
        else if (neon) Modifier.neonGlass(strong = border) else Modifier
            .clip(RoundedCornerShape(18.dp)).background(color)
            .then(if (border) Modifier.border(1.dp, studio.border, RoundedCornerShape(18.dp)) else Modifier))
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}
@Composable
internal fun CoralButton(label: String, modifier: Modifier = Modifier, enabled: Boolean = true,
                         chevron: Boolean = true, onClick: () -> Unit) {
    val p = studio
    val isNeon = p == NeonStudio
    val brush = if (isNeon) NeonLook.crest
        else Brush.horizontalGradient(listOf(Color(0xFFEE7B65), Color(0xFFD84B38)))
    Box(modifier.fillMaxWidth()
        .then(if (isNeon && enabled) Modifier.shadow(
            10.dp, RoundedCornerShape(26.dp),
            ambientColor = NeonLook.azure, spotColor = NeonLook.violet)
            else Modifier)
        .clip(RoundedCornerShape(26.dp))
        .background(if (enabled) brush else Brush.horizontalGradient(listOf(p.border, p.border)))
        .then(if (isNeon && enabled) Modifier.border(1.5.dp,
            NeonLook.cyan, RoundedCornerShape(26.dp)) else Modifier)
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
        if (studio == NeonStudio) NeonBrandMark(Modifier.size(54.dp))
        else Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra", Modifier.size(49.dp))
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
        Box(Modifier.fillMaxWidth(progress).fillMaxHeight()
            .background(if (studio == NeonStudio) NeonLook.crest
                else Brush.horizontalGradient(listOf(studio.coral, studio.coral)), CircleShape))
    }
}
@Composable
internal fun StudioBottomNavigation(page: String, onNavigate: (String) -> Unit) {
    val p = studio
    val active = when (page) {
        "review", "summary" -> "repeat"
        "updates", "voice-settings", "edit-profile" -> "profile"
        else -> page
    }
    Row(Modifier.fillMaxWidth().background(
        if (p == NeonStudio) p.card.copy(alpha = 0.72f) else p.card)
        .border(BorderStroke(0.5.dp, p.border))
        .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(
            Triple("home", "home", "Trang chủ"),
            Triple("lessons", "book", "Bài học"),
            Triple("repeat", "tab-mic", "Luyện nói"),
            Triple("progress", "progress", "Tiến độ"),
            Triple("profile", "settings", "Cài đặt")
        ).forEach { (id, icon, label) ->
            val selected = active == id
            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Tab, onClick = { onNavigate(id) })
                .semantics { this.selected = selected }
                .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (icon == "tab-mic" && p == NeonStudio) {
                    NeonVintageTabMicrophone(Modifier.size(24.dp))
                } else StudioIcon(icon, Modifier.size(22.dp),
                    if (selected) p.coral else p.muted)
                Text(label, color = if (selected) p.coral else p.ink,
                    fontSize = 10.sp, maxLines = 1,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
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
        Row(Modifier.fillMaxWidth()
            .then(if (p == NeonStudio) Modifier.neonGlass(strong = true)
                else Modifier.clip(RoundedCornerShape(18.dp)).background(p.peach))
            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("$goal phút mỗi ngày", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = p.ink)
                Text("Luyện nghe, luyện nói,\ntiến bộ từng chút mỗi ngày.", fontSize = 14.sp, color = p.ink, lineHeight = 20.sp)
            }
            if (p == NeonStudio) NeonHeadphones(Modifier.size(100.dp, 91.dp))
            else ApprovedArtwork(true, Modifier.size(94.dp, 88.dp).clip(RoundedCornerShape(14.dp)))
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
            Row(Modifier.weight(1f).heightIn(min = 76.dp)
                .then(if (p == NeonStudio) Modifier.neonGlass(16) else Modifier
                    .clip(RoundedCornerShape(16.dp)).background(p.mint))
                .clickable(role = Role.Button, onClick = onLessons).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioIcon("layers", Modifier.size(28.dp), p.green)
                Text("15 chủ đề A1", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                StudioIcon("chevron", Modifier.size(12.dp), p.green)
            }
            Row(Modifier.weight(1f).heightIn(min = 76.dp)
                .then(if (p == NeonStudio) Modifier.neonGlass(16) else Modifier
                    .clip(RoundedCornerShape(16.dp)).background(p.lavender))
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
        Row(Modifier.fillMaxWidth()
            .then(if (p == NeonStudio) Modifier.neonGlass(16)
                else Modifier.clip(RoundedCornerShape(16.dp)).background(p.lavender)
                .border(1.dp, p.border, RoundedCornerShape(16.dp)))
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
            Row(Modifier.fillMaxWidth()
                .then(if (p == NeonStudio) Modifier.neonGlass(16, selected == lesson.id)
                else Modifier.clip(RoundedCornerShape(16.dp))
                    .background(if (selected == lesson.id) p.peach else p.card)
                    .border(1.dp, if (selected == lesson.id) Color.Transparent else p.border, RoundedCornerShape(16.dp)))
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
    val isNeon = studio == NeonStudio
    Row(modifier.clip(RoundedCornerShape(13.dp))
        .background(if (isNeon) NeonLook.glossyPanel
            else Brush.horizontalGradient(listOf(studio.card, studio.card)))
        .border(1.dp, if (isNeon) NeonLook.violet else studio.border,
            RoundedCornerShape(13.dp))) {
        listOf(first to true, second to false).forEach { (label, value) ->
            val active = value == selectedFirst
            Box(Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
                .background(if (active && isNeon) NeonLook.crest
                    else Brush.horizontalGradient(listOf(
                        if (active) studio.coral else Color.Transparent,
                        if (active) studio.coral else Color.Transparent)))
                .clickable(role = Role.RadioButton, onClick = { onSelect(value) }).semantics { selected = active }
                .padding(horizontal = 12.dp, vertical = 9.dp), Alignment.Center) {
                Text(label, color = if (active && isNeon) Color(0xFF071B3F)
                    else if (active) MaterialTheme.colorScheme.onPrimary else studio.ink,
                    fontSize = 13.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal)
            }
        }
    }
}
@Composable
private fun SpeakerChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(12.dp))
        .background(if (studio == NeonStudio) NeonLook.glossyPanel
            else Brush.linearGradient(listOf(studio.peach, studio.peach)))
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
        .shadow(if (studio == NeonStudio) 13.dp else 3.dp, CircleShape,
            ambientColor = if (studio == NeonStudio) NeonLook.azure else Color.Transparent,
            spotColor = if (studio == NeonStudio) NeonLook.violet else Color.Transparent)
        .background(if (studio == NeonStudio) NeonLook.crest
            else Brush.linearGradient(listOf(Color(0xFFFF9B82), Color(0xFFDA4B38))), CircleShape)
        .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape).clip(CircleShape)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = if (active) "Kết thúc lượt nói" else "Nói và nhận dạng" }, Alignment.Center) {
        if (active) Box(Modifier.size(if (large) 25.dp else 19.dp).background(Color.White, RoundedCornerShape(4.dp)))
        else StudioIcon("mic", Modifier.size(if (large) 43.dp else 31.dp), Color.White)
    }
}
@Composable
private fun DecorativeWave() {
    val color = if (studio == NeonStudio) NeonLook.cyan else studio.coralLight.copy(alpha = 0.5f)
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
                Modifier.weight(1f)
                    .then(if (studio == NeonStudio) Modifier.neonGlass(16)
                          else Modifier.clip(RoundedCornerShape(16.dp))
                              .background(studio.lavender)
                              .border(1.dp, studio.border, RoundedCornerShape(16.dp)))
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
                            voiceChoice: VoiceChoice = VoiceChoice(), onVoiceSettings: () -> Unit = {},
                            explanationLanguage: String = LearningOptions.VIETNAMESE) {
    val p = studio; val phrase = lesson.phrases[progress.phrase]
    val englishOnly = explanationLanguage == LearningOptions.ENGLISH
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
                if (translation) Text(if (englishOnly) EnglishStudy.explanation(phrase.english) else phrase.vietnamese,
                    textAlign = TextAlign.Center, color = p.muted,
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
            StudioCard(color = p.mint, border = p == NeonStudio) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudioIcon("bulb", Modifier.size(27.dp), if (p == NeonStudio) NeonLook.cyan else p.green)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Mẹo luyện âm", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(if (englishOnly) EnglishStudy.pronunciationTip(phrase.english) else phrase.tip, fontSize = 14.sp)
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
                                neon: Boolean = false,
                                explanationLanguage: String = LearningOptions.VIETNAMESE,
                                onTypedText: (String) -> Unit = {}) {
    // One approved conversation component tree for Sáng, Tối and Neon.
    // Only theme colors and depth differ; navigation and speech stay unchanged.
    NeonConversationScreen(lesson, turnIndex, translation, transcript, capturePhase,
        status, learner, nextLabel, onBack, onPrompt, onReply, onSpeak,
        onTranslation, onNext, voiceChoice, onVoiceSettings, explanationLanguage,
        onTypedText, neonTheme = neon)
}
@Composable
internal fun ReviewScreen(lesson: Lesson, progress: LessonProgress, selection: Int, transcript: String, capturePhase: String, status: String, learner: Learner,
                          onSelect: (Int) -> Unit, onBack: () -> Unit, onListen: () -> Unit, onSpeak: () -> Unit, onNext: () -> Unit,
                           explanationLanguage: String = LearningOptions.VIETNAMESE) {
    val p = studio
    val english = explanationLanguage == LearningOptions.ENGLISH
    val bilingualQuestion = if (english) null else reviewQuestion(lesson, progress.review)
    val englishQuestion = if (english) EnglishStudy.review(lesson, progress.review) else null
    val reviewPrompt = englishQuestion?.prompt ?: bilingualQuestion!!.phrase.english
    val options = englishQuestion?.options ?: bilingualQuestion!!.options
    val answer = englishQuestion?.answer ?: bilingualQuestion!!.answer
    val correctLabel = options[answer]
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
                    Text(reviewPrompt, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(if (english) "Choose the best English reply" else "Chọn nghĩa phù hợp",
                fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            options.forEachIndexed { index, label ->
                val right = selection >= 0 && index == answer
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
                        Text(if (selection == answer) (if (english) "Correct!" else "Đúng rồi!")
                            else (if (english) "Correct answer: $correctLabel" else "Đáp án: $correctLabel"),
                            color = p.green, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(if (english) "English response practice, not phonetic scoring."
                            else "Ôn nghĩa câu, không chấm phát âm.", color = p.muted, fontSize = 13.sp)
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
internal fun ProfileScreen(
    dark: Boolean, speed: Int, goal: Int, version: String,
    learner: Learner, onSwitchLearner: () -> Unit, onEditLearner: () -> Unit,
    onTheme: (Boolean) -> Unit, onVoice: () -> Unit, onSpeed: () -> Unit,
    onGoal: () -> Unit, onUpdates: () -> Unit,
    voiceChoice: VoiceChoice = VoiceChoice(), neon: Boolean = false,
    onNeon: () -> Unit = {},
    explanationLanguage: String = LearningOptions.VIETNAMESE,
    repeatCount: Int = 1, reminderEnabled: Boolean = false,
    onBack: () -> Unit = {}, onAccent: (String) -> Unit = {},
    onGender: (String) -> Unit = {}, onExplanationLanguage: (String) -> Unit = {},
    onStudyOptions: () -> Unit = {}, onReminderToggle: (Boolean) -> Unit = {},
    onSound: () -> Unit = {}, onAbout: () -> Unit = {}
) {
    // No second Dark/Light tree: identical component structure across all themes.
    UnifiedSettingsScreen(
        dark = dark, neon = neon, speed = speed, goal = goal, version = version,
        learner = learner, choice = voiceChoice, language = explanationLanguage,
        repeats = repeatCount, reminder = reminderEnabled,
        onBack = onBack, onSwitch = onSwitchLearner, onEdit = onEditLearner,
        onTheme = onTheme, onNeon = onNeon, onVoice = onVoice,
        onAccent = onAccent, onGender = onGender,
        onLanguage = onExplanationLanguage,
        onStudy = onStudyOptions, onReminder = onReminderToggle,
        onSpeed = onSpeed, onGoal = onGoal, onSound = onSound,
        onUpdates = onUpdates, onAbout = onAbout
    )
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
