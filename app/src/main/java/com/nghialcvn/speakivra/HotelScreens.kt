package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HotelHubScreen(mode: String, onMode: (String) -> Unit,
                           onRole: (String) -> Unit, onBack: () -> Unit) {
    val p = studio
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeading("Tiếng Anh Khách sạn", "Giao tiếp với khách quốc tế", onBack)
        Text("Cấp độ riêng", color = p.coral, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .border(1.dp, p.coral, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 5.dp))
        listOf("FO", "HK").forEach { role ->
            StudioCard(Modifier.clickable { onRole(role) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudioIcon(if (role == "FO") "reception" else "housekeeping", Modifier.size(38.dp), p.coral)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(HotelCurriculum.roleLabel(role), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text(HotelCurriculum.roleEnglish(role), fontSize = 13.sp, color = p.muted)
                        Text("15 chủ đề • " + if (role == "FO") "Đón tiếp & hỗ trợ khách" else "Chăm sóc phòng & khách",
                            fontSize = 12.sp, color = p.muted)
                    }
                    StudioIcon("chevron", Modifier.size(16.dp), p.coral)
                }
            }
        }
        StudioCard {
            Text("Học theo tình huống", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            listOf(Triple("repeat", "Nghe & nhắc lại", "speaker"),
                Triple("chat", "Hội thoại phân vai", "family"),
                Triple("exercise", "Tiếng Anh nói thế nào?", "chat")).forEach { (value, title, icon) ->
                Row(Modifier.fillMaxWidth().clickable { onMode(value) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudioIcon(icon, Modifier.size(24.dp), p.coral)
                    Text(title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    StudioIcon(if (mode == value) "check" else "chevron", Modifier.size(18.dp), p.coral)
                }
            }
        }
        Text("Chọn nghiệp vụ để bắt đầu", textAlign = TextAlign.Center, fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().border(1.dp, p.coral, RoundedCornerShape(20.dp)).padding(12.dp))
    }
}

@Composable
internal fun HotelTopicsScreen(role: String, load: (Lesson) -> LessonProgress,
                              hearts: (Lesson) -> Int, onOpen: (Lesson) -> Unit, onBack: () -> Unit) {
    val p = studio
    val topics = HotelCurriculum.topics.filter { it.role == role }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading(HotelCurriculum.roleLabel(role), "${HotelCurriculum.roleEnglish(role)} • 15 chủ đề", onBack)
        AchievementSummary(topics.sumOf { hearts(it.lesson) })
        topics.forEachIndexed { index, topic ->
            val lesson = topic.lesson
            val value = load(lesson)
            StudioCard(Modifier.clickable { onOpen(lesson) }, contentPadding = 12.dp, spacing = 6.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text((index + 1).toString().padStart(2, '0'), color = p.coral,
                        fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    StudioIcon(topic.icon, Modifier.size(27.dp), p.coral)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(lesson.vietnamese, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Text(lesson.title, fontSize = 11.sp, color = p.muted)
                        if (topic.incident) Text("Sự cố & cách xử lý", fontSize = 10.sp, color = p.coral)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        StudioIcon("chevron", Modifier.size(13.dp), p.muted)
                        TopicDiamonds(lesson.vietnamese, hearts(lesson))
                    }
                }
                val learned = value.learnedPhrases?.size ?: value.phrase
                val spoken = value.learnedTurns.orEmpty().size
                Text(if (learned > 0 || spoken > 0) "Đã học $learned/${lesson.phrases.size} câu • $spoken/${lesson.conversation.size} lượt hội thoại"
                    else "${lesson.phrases.size} câu mẫu • ${lesson.conversation.size} lượt hội thoại", fontSize = 11.sp, color = p.muted)
                if (learned > 0 || value.rounds > 0) StudioProgress(learned.toFloat() / lesson.phrases.size)
            }
        }
    }
}

@Composable
internal fun HotelConversationScreen(lesson: Lesson, turnIndex: Int, translation: Boolean,
    transcript: String, phase: String, learner: Learner, speed: Int, nextLabel: String,
    onBack: () -> Unit, onPrompt: () -> Unit, onReply: () -> Unit, onSpeak: () -> Unit,
    onSpeed: (Int) -> Unit, onNext: () -> Unit) {
    val p = studio
    val turn = lesson.conversation[turnIndex]
    val words = matchWords(turn.reply, if (phase == "idle") transcript else null)
    val allowed = words.canContinue && phase == "idle"
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PageHeading("Hội thoại Khách sạn", "${HotelCurriculum.roleLabel(HotelCurriculum.role(lesson))} • ${lesson.vietnamese}", onBack)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { StudioProgress((turnIndex + 1f) / lesson.conversation.size) }
            Text("Lượt ${turnIndex + 1}/${lesson.conversation.size}", fontSize = 11.sp, color = p.muted)
        }
        Text("Bạn là nhân viên " + if (HotelCurriculum.role(lesson) == "FO") "lễ tân" else "buồng phòng",
            color = p.coral, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StudioCard(color = p.mint) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    StudioIcon("profile", Modifier.size(26.dp), p.coral)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(turn.prompt, fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
                        if (translation) Text(turn.translation, fontSize = 13.sp, color = p.muted)
                    }
                    IconButton(onClick = onPrompt, modifier = Modifier.size(35.dp)) { StudioIcon("speaker", color = p.coral) }
                }
            }
            StudioCard {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(wordFeedback(turn.reply, words), fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
                        if (translation) Text(turn.replyTranslation, fontSize = 13.sp, color = p.muted)
                    }
                    LearnerAvatar(learner.avatar, Modifier.size(30.dp))
                }
                IpaLine(turn.reply, modifier = Modifier.fillMaxWidth())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                OutlinedButton(onClick = onReply) { StudioIcon("speaker", Modifier.size(17.dp), p.coral); Spacer(Modifier.width(6.dp)); Text("Nghe mẫu") }
                OutlinedButton(onClick = { onSpeed(when (speed) { 75 -> 100; 100 -> 125; else -> 75 }) }) { Text(speedLabel(speed)) }
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Microphone(onSpeak, large = true, active = phase == "listening")
                Text(captureLabel(phase, if (words.hasResult) "Chạm để nói lại" else "Chạm để trả lời"), color = p.muted, fontSize = 13.sp)
            }
            SpokenResult(learner, transcript, if (phase == "idle" && words.hasResult) allowed else null,
                reference = if (phase == "idle") turn.reply else null)
        }
        CoralButton(if (allowed) nextLabel else "Cần nói lại", enabled = allowed, onClick = onNext)
    }
}
