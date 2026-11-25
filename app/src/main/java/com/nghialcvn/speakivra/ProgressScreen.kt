package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onOpen: (Lesson) -> Unit
) {
    val p = studio
    val finished = lessons.count { load(it).phase == "summary" }
    val totalRounds = lessons.sumOf { load(it).rounds }
    val touched = lessons.count { lesson ->
        val progress = load(lesson)
        progress.phrase > 0 || progress.phase != "repeat" || progress.rounds > 0
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeading("Tiến độ học tập", "Được lưu riêng trên thiết bị của bạn")
        StudioCard(color = p.lavender, border = false) {
            Text("$finished/${lessons.size} chủ đề đã hoàn thành", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold)
            StudioProgress(finished.toFloat() / lessons.size.coerceAtLeast(1))
            Text("$touched chủ đề đã bắt đầu • $totalRounds lượt bài hoàn thành",
                color = p.muted, fontSize = 13.sp)
        }
        Text("A1 • Căn bản", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        lessons.filter { Lessons.levelOf(it) == "A1" }.forEach { lesson ->
            LessonProgressRow(lesson, load(lesson), topicIsOpen(lesson, lessons, load), onOpen)
        }
        Text("A2 • Sơ trung cấp", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        lessons.filter { Lessons.levelOf(it) == "A2" }.forEach { lesson ->
            LessonProgressRow(lesson, load(lesson), topicIsOpen(lesson, lessons, load), onOpen)
        }
        Text("B1 • Trung cấp", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        lessons.filter { Lessons.levelOf(it) == "B1" }.forEach { lesson ->
            LessonProgressRow(lesson, load(lesson), topicIsOpen(lesson, lessons, load), onOpen)
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
            StudioProgress((value.phrase + 1f) / lesson.phrases.size)
        }
        StudioIcon("chevron", Modifier.size(14.dp), p.muted)
    }
}
