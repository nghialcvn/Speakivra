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
    "a2_health" -> "Sức khỏe & sinh hoạt"; "a2_work" -> "Công việc & phối hợp"
    "a2_learning" -> "Học tập & mục tiêu"; "a2_community" -> "Cộng đồng & dịch vụ"
    "a2_stories" -> "Kể chuyện & tình huống"
    else -> if (HotelCurriculum.isHotel(lesson)) lesson.vietnamese else if (CourseLevels.all.drop(1).any { lesson.id.startsWith(it.lowercase() + "_") }) lesson.vietnamese else "Giao tiếp thường ngày"
}
internal fun speedLabel(speed: Int): String = when (speed) { 75 -> "0.75x"; 125 -> "1.25x"; else -> "1x" }

@Composable
internal fun StudioCard(modifier: Modifier = Modifier, color: Color = studio.card,
                        border: Boolean = true, contentPadding: Dp = 16.dp, spacing: Dp = 10.dp, content: @Composable ColumnScope.() -> Unit) {
    val neon = studio == NeonStudio
    Column(modifier.fillMaxWidth()
        .then(if (neon && color == studio.mint) Modifier
            .shadow(9.dp, RoundedCornerShape(18.dp),
                ambientColor = NeonLook.cyan, spotColor = NeonLook.blue)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(
                Color(0xF0005267), Color(0xF0124B6F))))
            .border(1.5.dp, NeonLook.cyan, RoundedCornerShape(18.dp))
        else if (neon) Modifier.neonGlass(strong = border) else Modifier.brandPanel(color = color, border = border))
        .padding(contentPadding), verticalArrangement = Arrangement.spacedBy(spacing), content = content)
}
@Composable
internal fun CoralButton(label: String, modifier: Modifier = Modifier, enabled: Boolean = true,
                         chevron: Boolean = true, labelColor: Color? = null, leadingPlay: Boolean = false, onClick: () -> Unit) {
    val p = studio
    val isNeon = p == NeonStudio
    val brush = if (isNeon) NeonLook.crest
        else BrandLook.action
    Box(modifier.fillMaxWidth()
        .then(if (isNeon && enabled) Modifier.neonBloom(26).shadow(
            10.dp, RoundedCornerShape(26.dp),
            ambientColor = NeonLook.azure, spotColor = NeonLook.violet)
            else Modifier)
        .clip(RoundedCornerShape(if (isNeon) 26.dp else 14.dp))
        .background(if (enabled) brush else Brush.horizontalGradient(listOf(p.border, p.border)))
        .then(if (isNeon && enabled) Modifier.border(1.dp,
            NeonLook.edge, RoundedCornerShape(26.dp)) else Modifier)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .heightIn(min = 50.dp).padding(horizontal = 20.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (leadingPlay) {
                Canvas(Modifier.size(14.dp, 17.dp)) {
                    val triangle = Path().apply { moveTo(0f, 0f); lineTo(size.width, size.height / 2); lineTo(0f, size.height); close() }
                    drawPath(triangle, Color.White)
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(label, color = if (enabled) labelColor ?: Color.White else p.muted, fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = !leadingPlay).padding(horizontal = if (chevron) 20.dp else 0.dp))
        }
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
internal fun Brand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.brand_logo_clear), "Logo Speakivra", Modifier.size(52.dp))
        Image(painterResource(R.drawable.brand_wordmark), "Speakivra",
            Modifier.width(184.dp).height(42.dp), colorFilter = ColorFilter.tint(studio.ink))
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
                else BrandLook.progress, CircleShape))
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
    Row(Modifier.fillMaxWidth()
        .then(if (p == NeonStudio) Modifier.padding(horizontal = 6.dp, vertical = 5.dp).neonGlass(22)
            else Modifier.background(p.card).border(BorderStroke(0.5.dp, p.border)))
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
                } else StudioIcon(if (selected && icon == "home" && p != NeonStudio) "home-filled" else icon, Modifier.size(22.dp),
                    if (selected) brandNavigationAccent(p) else p.muted)
                Text(label, color = if (selected) brandNavigationAccent(p) else p.ink,
                    fontSize = 10.sp, maxLines = 1,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
@Composable
internal fun HomeScreen(lesson: Lesson, progress: LessonProgress, goal: Int, completed: Int, learnerName: String,
                        learner: Learner, preferences: android.content.SharedPreferences,
                        onContinue: () -> Unit, onLessons: () -> Unit, onProfile: () -> Unit,
                        lessonHeading: String? = null, lessonNote: String? = null, courseSummary: String = "A1 • A2 • B1 • B2 • C1 • C2 • Hội thoại • Đoạn văn • Bài tập") {
    val p = studio
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(if (p == NeonStudio) 16.dp else 14.dp)) {
        DailyCheckInCard(learner, preferences, onProfile)
        Text("Hôm nay, mình cùng luyện nói nhé.", color = if (p == NeonStudio) p.muted else p.ink,
            fontWeight = if (p == NeonStudio) FontWeight.Normal else FontWeight.Bold, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth()
            .then(if (p == NeonStudio) Modifier.neonGlass(strong = true)
                else Modifier.brandPanel(color = p.peach))
            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("$goal phút mỗi ngày", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = if (p == LightStudio) p.coral else p.ink)
                Text("Luyện nghe, luyện nói,\ntiến bộ từng chút mỗi ngày.", fontSize = 14.sp, color = p.ink, lineHeight = 20.sp)
            }
            if (p == NeonStudio) NeonHeadphones(Modifier.size(100.dp, 91.dp))
            else NeonHeadphones(Modifier.size(94.dp, 88.dp))
        }
        StudioCard(color = if (p == NeonStudio) p.lavender else p.card, border = p != NeonStudio) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(49.dp).clip(RoundedCornerShape(14.dp)).background(if (p == NeonStudio) p.card.copy(alpha = 0.7f) else p.peach), Alignment.Center) {
                    StudioIcon("chat", Modifier.size(30.dp), p.purple)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(lessonHeading ?: if (HotelCurriculum.isHotel(lesson)) "Khách sạn • ${lessonLabel(lesson)}" else "Bài ${Lessons.all.indexOfFirst { it.id == lesson.id } + 1} • ${lessonLabel(lesson)}", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text(lesson.title, fontSize = 14.sp)
                    lessonNote?.let { Text(it, color = p.muted, fontSize = 11.sp) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Câu ${progress.phrase + 1}/${lesson.phrases.size}", fontSize = 13.sp, color = p.muted)
                StudioProgress((progress.phrase + 1f) / lesson.phrases.size, Modifier.weight(1f))
            }
            CoralButton(if (progress.phase == "summary") "Xem tổng kết" else "Tiếp tục học",
                chevron = p == NeonStudio || progress.phase == "summary", leadingPlay = p != NeonStudio && progress.phase != "summary", onClick = onContinue)
            if (completed > 0) Text("Đã hoàn thành: $completed lượt bài", color = p.muted, fontSize = 12.sp)
        }
        StudioCard(Modifier.clickable(role = Role.Button, onClick = onLessons)
            .semantics { contentDescription = "Mở mục Bài học" }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioIcon("book", Modifier.size(28.dp), p.coral)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Bài học", fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
                    Text(courseSummary, color = p.muted,
                        fontSize = 12.sp, lineHeight = 17.sp)
                }
                StudioIcon("chevron", Modifier.size(17.dp), p.coral)
            }
        }
    }
}

@Composable
internal fun LessonHubScreen(onLevel: (String) -> Unit, onChat: () -> Unit,
    onPassages: () -> Unit, onExercises: () -> Unit, onBack: () -> Unit, onHotel: () -> Unit = {}, load: (Lesson) -> LessonProgress = { LessonProgress() }) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeading("Bài học", "Chọn cấp độ hoặc dạng luyện tập", onBack)
        CourseLevels.all.forEach { level ->
            val items = Lessons.all.filter { Lessons.levelOf(it) == level }
            val unlocked = levelIsOpen(level, Lessons.catalogue, load)
            LessonHubCard(if (level == "A1") "A1 • Cơ bản" else CourseLevels.title(level),
                "${items.size} chủ đề • ${items.sumOf { it.phrases.size }} câu luyện nói • ${items.sumOf { it.conversation.size }} lượt hội thoại" +
                    if (unlocked) "" else "\n${CourseLevels.lockMessage(level)}",
                if (unlocked) "book" else "lock", enabled = unlocked) { onLevel(level) }
        }
        LessonHubCard("Tiếng Anh Khách sạn", "Bài học riêng • Lễ tân 20 chủ đề • Buồng phòng 20 chủ đề", "reception", onOpen = onHotel)
        LessonHubCard("Hội thoại mẫu", "Mô phỏng theo kịch bản", "chat", onOpen = onChat)
        LessonHubCard("Luyện nói theo đoạn văn", "A1 • A2 • B1 • B2 • C1 • C2", "book", onOpen = onPassages)
        LessonHubCard("Tiếng Anh nói thế nào?", "A1 • A2 • B1 • B2 • C1 • C2", "book", onOpen = onExercises)
    }
}
@Composable
private fun LessonHubCard(title: String, detail: String, icon: String, enabled: Boolean = true, onOpen: () -> Unit) {
    StudioCard(Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onOpen).semantics { if (!enabled) disabled() }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StudioIcon(icon, Modifier.size(27.dp), studio.coral)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.ExtraBold)
                Text(detail, fontSize = 12.sp, lineHeight = 17.sp, color = studio.muted)
            }
            StudioIcon("chevron", Modifier.size(17.dp), studio.coral)
        }
    }
}

@Composable
internal fun LessonsScreen(selected: String, load: (Lesson) -> LessonProgress, onOpen: (Lesson) -> Unit,
                           lessons: List<Lesson> = Lessons.a1, level: String = "A1", onBack: (() -> Unit)? = null, hearts: (Lesson) -> Int = { 0 }, accessCatalogue: List<Lesson> = Lessons.catalogue) {
    val p = studio
    val icons = listOf("handshake", "family", "calendar", "food", "bag", "map", "book", "clock", "plane", "chat")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading("Chọn bài học $level", "${lessons.size} chủ đề $level • ${lessons.sumOf { it.phrases.size }} câu nói • ${lessons.sumOf { it.conversation.size }} lượt hội thoại", onBack)
        AchievementSummary(lessons.sumOf(hearts))
        Spacer(Modifier.height(3.dp))
        lessons.forEachIndexed { index, lesson ->
            val value = load(lesson)
            val open = topicIsOpen(lesson, accessCatalogue, load)
            Row(Modifier.fillMaxWidth()
                .then(if (p == NeonStudio) Modifier.neonGlass(16, selected == lesson.id)
                else Modifier.clip(RoundedCornerShape(16.dp))
                    .background(if (selected == lesson.id) p.peach else p.card)
                    .border(1.dp, if (selected == lesson.id) Color.Transparent else p.border, RoundedCornerShape(16.dp)))
                .clickable(enabled = open, role = Role.Button, onClick = { onOpen(lesson) }).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(33.dp).background(p.peach, CircleShape), Alignment.Center) {
                    Text((index + 1).toString().padStart(2, '0'), color = p.coral, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                StudioIcon(icons.getOrElse(index) { "book" }, Modifier.size(28.dp), p.coral)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(lessonLabel(lesson), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(lesson.title, color = p.muted, fontSize = 11.sp)
                    if (!open) Text("Khóa • Hoàn thành chủ đề $index để mở", color = p.muted, fontSize = 11.sp)
                    if (value.rounds > 0) Text(if (value.phase == "summary") "Hoàn thành • ${value.rounds} lượt" else "Tiếp tục học • Đã học ${value.rounds} lượt", color = p.green, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selected == lesson.id) Text("${value.phrase + 1}/${lesson.phrases.size} câu", fontSize = 11.sp, color = p.muted)
                        StudioIcon(if (open) "chevron" else "lock", Modifier.size(13.dp), p.muted)
                    }
                    val earned = hearts(lesson)
                    TopicDiamonds(lessonLabel(lesson), earned)
                }
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
                    else if (active) BrandLook.selection else Brush.horizontalGradient(listOf(
                        if (active) studio.coral else Color.Transparent,
                        if (active) studio.coral else Color.Transparent)))
                .clickable(role = Role.RadioButton, onClick = { onSelect(value) }).semantics { selected = active }
                .padding(horizontal = 12.dp, vertical = 9.dp), Alignment.Center) {
                Text(label, color = if (active && isNeon) Color(0xFF071B3F)
                    else if (active) Color.White else studio.ink,
                    fontSize = 13.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal)
            }
        }
    }
}
@Composable
internal fun PracticePlaybackControls(speed: Int, translation: Boolean, onListen: () -> Unit,
    onSpeed: (Int) -> Unit, onTranslation: () -> Unit, sampleEnabled: Boolean = true) {
    val p = studio
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
                SpeakerChip("Nghe mẫu", onListen, enabled = sampleEnabled)
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
                SpeakerChip("Nghe mẫu", onListen, enabled = sampleEnabled)
                controls()
            }
        }
    }
}
@Composable
internal fun SpeakerChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(modifier.clip(RoundedCornerShape(12.dp))
        .background(if (studio == NeonStudio) NeonLook.glossyPanel
            else Brush.linearGradient(listOf(studio.card, studio.card)))
        .then(if (studio != NeonStudio) Modifier.border(1.dp, studio.border, RoundedCornerShape(12.dp)) else Modifier)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick).heightIn(min = 44.dp).padding(horizontal = 10.dp, vertical = 8.dp),
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
        .background(if (studio == NeonStudio) Brush.linearGradient(listOf(NeonLook.cyan, NeonLook.azure, NeonLook.violet),
            start = Offset.Zero, end = Offset(220f, 180f))
            else BrandLook.microphone, CircleShape)
        .then(if (studio == NeonStudio) Modifier.background(Brush.radialGradient(
            listOf(Color(0x66FFFFFF), Color.Transparent), center = Offset(40f, 25f), radius = 190f), CircleShape) else Modifier)
        .border(if (studio == NeonStudio) 1.2.dp else 2.dp, Color.White.copy(alpha = if (studio == NeonStudio) 0.92f else 0.85f), CircleShape).clip(CircleShape)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = if (active) "Kết thúc lượt nói" else "Nói và nhận dạng" }, Alignment.Center) {
        if (active) Box(Modifier.size(if (large) 25.dp else 19.dp).background(Color.White, RoundedCornerShape(4.dp)))
        else StudioIcon("mic", Modifier.size(if (large) 43.dp else 31.dp), Color.White)
    }
}
@Composable
internal fun DecorativeWave() {
    val neon = studio == NeonStudio
    val color = if (neon) NeonLook.cyan else BrandLook.cyan
    Canvas(Modifier.width(if (neon) 64.dp else 42.dp).height(if (neon) 48.dp else 40.dp)) {
        val bars = if (neon) listOf(.2f, .42f, .72f, 1f, .62f, .32f) else listOf(.5f, 1f, .5f)
        bars.forEachIndexed { i, h ->
            val x = size.width * (i + .5f) / bars.size
            drawLine(if (neon) listOf(NeonLook.cyan, NeonLook.cyan, NeonLook.azure, NeonLook.azure, NeonLook.lilac, NeonLook.violet)[i] else color, Offset(x, size.height * (1 - h) / 2), Offset(x, size.height * (1 + h) / 2),
                strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}
@Composable
internal fun VoiceSelectionChips(choice: VoiceChoice, onOpen: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        listOf(
            "🇺🇸 English (US)" to "Ngôn ngữ phát âm",
            (if (choice.gender == "male") "🔊 Giọng nam" else "🔊 Giọng nữ") to "Giọng đọc"
        ).forEach { (label, description) ->
            Row(
                Modifier.weight(1f)
                    .then(if (studio == NeonStudio) Modifier.neonGlass(16)
                          else Modifier.clip(RoundedCornerShape(16.dp))
                              .background(studio.lavender)
                              .border(1.dp, studio.border, RoundedCornerShape(16.dp)))
                    .then(if (description == "Giọng đọc") Modifier.clickable(role = Role.Button, onClick = onOpen) else Modifier)
                    .semantics { contentDescription = description }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = studio.ink, maxLines = 1)
                Spacer(Modifier.width(4.dp))
                if (description == "Giọng đọc") Text("⌄", color = studio.purple, fontSize = 13.sp)
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
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TwoChoices("Tự động", "Thủ công", automatic, onMode, Modifier.fillMaxWidth())
            StudioCard {
                val colored = buildAnnotatedString {
                    wordMatch.tokens.forEachIndexed { index, word ->
                        if (index > 0) append(" ")
                        withStyle(SpanStyle(color = if (!wordMatch.hasResult) p.ink else if (wordMatch.matched[index]) p.correctWord else p.incorrectWord)) { append(word) }
                    }
                }
                Text(colored, fontSize = 29.sp, lineHeight = 35.sp, textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
                IpaLine(phrase.english, voiceChoice.accent, Modifier.fillMaxWidth())
                if (translation) Text(if (englishOnly) EnglishStudy.explanation(phrase.english) else phrase.vietnamese,
                    textAlign = TextAlign.Center, color = p.muted,
                    fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
                PracticePlaybackControls(speed, translation, onListen, onSpeed, onTranslation)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    DecorativeWave(); Microphone(onSpeak, large = true, active = capturePhase == "listening"); DecorativeWave()
                }
                Text(captureLabel(capturePhase, if (wordMatch.hasResult) "Chạm để nói lại" else "Chạm để nói"), fontSize = 15.sp, color = p.muted)
            }
            val resultFooter: @Composable () -> Unit = {
                if (wordMatch.hasResult) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Khớp ${wordMatch.correct}/${wordMatch.tokens.size} từ · ${wordMatch.matchPercent}%", color = if (wordMatch.canContinue) p.correctWord else p.incorrectWord, fontSize = if (p == NeonStudio) 13.sp else 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(if (wordMatch.canContinue) "✓ Có thể chuyển câu" else "Hãy nghe mẫu và nói lại nhé", color = if (wordMatch.canContinue) p.correctWord else p.ink, fontSize = if (p == NeonStudio) 13.sp else 12.sp, modifier = Modifier.weight(1f))
                }
            }
            SpokenResult(learner, if (capturePhase == "listening" && transcript.isBlank()) "Đang ghi âm…" else transcript,
                if (wordMatch.hasResult && capturePhase == "idle") wordMatch.canContinue else null,
                reference = if (capturePhase == "idle") wordMatch.tokens.joinToString(" ") else null,
                footer = if (p != NeonStudio) resultFooter else null)
            if (p == NeonStudio || !wordMatch.canContinue) StudioCard(color = p.mint, border = p == NeonStudio) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudioIcon("bulb", Modifier.size(27.dp), if (p == NeonStudio) NeonLook.cyan else p.green)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Mẹo luyện âm", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(if (englishOnly) EnglishStudy.pronunciationTip(phrase.english) else phrase.tip, fontSize = 14.sp)
                    }
                }
            }
            if (p == NeonStudio) resultFooter()
            Text("Đối chiếu từ nhận dạng, chưa chấm phát âm.", fontSize = 12.sp, color = p.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (capturePhase != "idle" || status.contains("không cung cấp âm thanh") || (!wordMatch.hasResult && status != "Nhấn Nghe mẫu để bắt đầu")) Text(status, fontSize = 12.sp, color = p.muted, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
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
                                onTypedText: (String) -> Unit = {}, hotelSpeed: Int = 75, onHotelSpeed: (Int) -> Unit = {}) {
    if (HotelCurriculum.isHotel(lesson)) {
        HotelConversationScreen(lesson, turnIndex, translation, transcript, capturePhase, learner,
            hotelSpeed, nextLabel, onBack, onPrompt, onReply, onSpeak, onHotelSpeed, onNext)
        return
    }
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
    onSound: () -> Unit = {}, onAbout: () -> Unit = {}, onAccount: () -> Unit = {}, chinese: Boolean = false
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
        onUpdates = onUpdates, onAbout = onAbout, onAccount = onAccount, chinese = chinese
    )
}

internal fun captureLabel(phase: String, idle: String) = when (phase) {
    "listening" -> "Chạm để kết thúc"
    "processing" -> "Đang xử lý…"
    else -> idle
}

@Composable
internal fun wordFeedback(text: String, result: WordMatch?): androidx.compose.ui.text.AnnotatedString {
    val p = studio
    return buildAnnotatedString {
        var end = 0
        Regex("\\S+").findAll(text).forEachIndexed { index, token ->
            append(text.substring(end, token.range.first))
            val color = if (result?.hasResult != true) p.ink
                else if (result.matched.getOrElse(index) { false }) p.correctWord else p.incorrectWord
            withStyle(SpanStyle(color = color)) { append(token.value) }
            end = token.range.last + 1
        }
        append(text.substring(end))
    }
}

@Composable
internal fun SpokenResult(learner: Learner, text: String, passed: Boolean? = null, reference: String? = null, highlighted: androidx.compose.ui.text.AnnotatedString? = null, footer: (@Composable () -> Unit)? = null) {
    val p = studio
    val replay = LocalSpeechReplay.current
    Column(Modifier.fillMaxWidth()
        .then(if (p == NeonStudio) Modifier.neonGlass(9, strong = true) else Modifier.brandPanel(radius = 14))
        .padding(8.dp).semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LearnerAvatar(learner.avatar, Modifier.size(28.dp), square = true)
        IconButton(onClick = replay.play, enabled = replay.available,
            modifier = Modifier.size(40.dp).semantics { contentDescription = "Nghe lại giọng vừa ghi" }) {
            StudioIcon("speaker", Modifier.size(24.dp), if (replay.available) p.coral else p.muted.copy(alpha = .35f))
        }
        val display = text.ifBlank { "Chưa có lời nói nhận dạng" }
        val words = if (passed != null && reference != null && text.isNotBlank()) matchWords(text, reference) else null
        Text(highlighted ?: wordFeedback(display, words), fontSize = if (p == NeonStudio) 14.sp else 15.sp,
            fontStyle = if (p == NeonStudio) FontStyle.Italic else FontStyle.Normal,
            fontWeight = if (p == NeonStudio) FontWeight.Normal else FontWeight.Bold,
            lineHeight = 20.sp, modifier = Modifier.weight(1f))
        if (passed != null) {
            val resultModifier = Modifier.semantics { contentDescription = if (passed) "Đạt đối chiếu lời nói" else "Cần nói lại" }
            if (p == NeonStudio) Text(if (passed) "✅" else "❌", fontSize = 23.sp, modifier = resultModifier)
            else BrandResultMark(passed, resultModifier.size(23.dp))
        }
    }
    footer?.invoke()
    }
}

internal data class SpeechReplay(val available: Boolean = false, val play: () -> Unit = {})
internal val LocalSpeechReplay = androidx.compose.runtime.staticCompositionLocalOf { SpeechReplay() }
