package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Local-only real progress; never guesses scores or records user speech. */
@Composable
internal fun ProgressScreen(
    lessons: List<Lesson>,
    load: (Lesson) -> LessonProgress,
    onOpen: (Lesson) -> Unit,
    achievements: (Lesson) -> Int = { 0 }
) {
    val p = studio
    val finished = lessons.count { load(it).phase == "summary" }
    val totalRounds = lessons.sumOf { load(it).rounds }
    val touched = lessons.count { lesson ->
        val progress = load(lesson)
        progress.phrase > 0 || progress.phase != "repeat" || progress.rounds > 0 || progress.learnedTurns.orEmpty().isNotEmpty()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeading("Tiến độ học tập", "Được lưu riêng trên thiết bị của bạn")
        StudioCard(color = p.lavender, border = false) {
            SingleLineInfo("$finished/${lessons.size} chủ đề đã hoàn thành", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold)
            StudioProgress(finished.toFloat() / lessons.size.coerceAtLeast(1))
            SingleLineInfo("$touched chủ đề đã bắt đầu • $totalRounds lượt bài hoàn thành",
                color = p.muted, fontSize = 13.sp)
            HorizontalDivider(color = p.muted.copy(alpha = .4f))
            AchievementSummary(lessons.sumOf(achievements))
            Text("Tổng thành tích cả khóa", color = p.muted, fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        var expanded by rememberSaveable { mutableStateOf<String?>(null) }
        val groups = CourseLevels.all + listOf("FO", "HK")
        groups.forEach { group ->
            val items = lessons.filter { Lessons.levelOf(it) == group }
            val hotel = group in listOf("FO", "HK")
            val unlocked = hotel || levelIsOpen(group, lessons, load)
            val title = if (hotel) "Khách sạn • ${HotelCurriculum.roleLabel(group)}" else CourseLevels.title(group)
            StudioCard(Modifier.semantics { contentDescription = "Nhóm tiến độ $group" },
                spacing = 10.dp, contentPadding = 12.dp) {
                Row(Modifier.fillMaxWidth().clickable(role = Role.Button) {
                    expanded = if (expanded == group) null else group
                }.semantics { stateDescription = if (expanded == group) "Đang mở" else "Đang thu gọn" },
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudioIcon(if (unlocked) "book" else "lock", Modifier.size(24.dp), p.coral)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        if (hotel) Text("Bài học riêng", color = p.coral, fontSize = 12.sp,
                            modifier = Modifier.background(p.peach, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
                        else if (!unlocked) Text(CourseLevels.lockMessage(group), color = p.muted, fontSize = 12.sp)
                    }
                    StudioIcon(if (expanded == group) "up" else "down", Modifier.size(18.dp), p.coral)
                }
                if (expanded == group) {
                    if (!hotel) Text("${items.size} chủ đề • ${items.sumOf { it.phrases.size }} câu • ${items.sumOf { it.conversation.size }} lượt hội thoại",
                        color = p.muted, fontSize = 12.sp)
                    items.forEach { item -> LessonProgressRow(item, load(item), topicIsOpen(item, lessons, load), onOpen) }
                }
            }
        }
    }
}

@Composable
private fun LessonProgressRow(lesson: Lesson, value: LessonProgress, open: Boolean, onOpen: (Lesson) -> Unit) {
    val p = studio
    Row(Modifier.fillMaxWidth()
        .then(if (p == NeonStudio) Modifier.neonGlass(15)
            else Modifier.background(p.card, RoundedCornerShape(15.dp))
                .border(1.dp, p.border, RoundedCornerShape(15.dp)))
        .clickable(enabled = open) { onOpen(lesson) }.padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioIcon(if (!open) "lock" else if (value.phase == "summary") "check" else "book",
            Modifier.size(23.dp), if (value.phase == "summary") p.green else p.coral)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(lessonLabel(lesson), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(if (!open) "Chủ đề đang khóa" else if (value.phase == "summary") "${value.rounds} lượt hoàn thành"
                 else "${value.phrase + 1}/${lesson.phrases.size} câu • ${value.phase}",
                color = p.muted, fontSize = 12.sp)
            StudioProgress(if (!open) 0f else if (value.phase == "summary") 1f else value.learnedPhrases?.size?.toFloat()?.div(lesson.phrases.size) ?: (value.phrase.toFloat() / lesson.phrases.size))
        }
        StudioIcon("chevron", Modifier.size(14.dp), p.muted)
    }
}
