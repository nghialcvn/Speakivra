package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val navy = Color(0xFF071126)
private val neon = Color(0xFF34B7FF)
private val white = Color(0xFFF3F6FF)
private val muted = Color(0xFFB2C1DE)
private val bubble = Color(0xFF1D315B)

@Composable
private fun NeonMessage(
    text: String, translation: String?, learner: Learner?, isReply: Boolean, onPlay: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isReply) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isReply) {
            Box(Modifier.size(40.dp).clip(CircleShape)
                .background(Color(0xFF29436A)), Alignment.Center) {
                StudioIcon("chat", Modifier.size(22.dp), neon)
            }
            Spacer(Modifier.width(9.dp))
        }
        Column(Modifier.fillMaxWidth(0.82f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isReply) listOf(Color(0xFF1973D6), Color(0xFF2556CA))
                            else listOf(bubble, Color(0xFF142747))
                        )
                    )
                    .border(1.dp, if (isReply) neon else Color(0xFF45628C), RoundedCornerShape(17.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (isReply) "Bạn trả lời" else "Câu hỏi mẫu · Mô phỏng",
                    fontSize = 11.sp, color = Color(0xFFD5E7FF), fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text, modifier = Modifier.weight(1f), fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold, color = white)
                    IconButton(onClick = onPlay, modifier = Modifier.size(38.dp)
                        .semantics { contentDescription = if (isReply) "Nghe câu trả lời" else "Nghe câu hỏi" }) {
                        StudioIcon("speaker", Modifier.size(21.dp), Color(0xFFBCE7FF))
                    }
                }
            }
            if (!translation.isNullOrBlank()) Text(
                translation, fontSize = 13.sp, color = muted,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (isReply) {
            Spacer(Modifier.width(9.dp))
            if (learner != null) LearnerAvatar(learner.avatar, Modifier.size(40.dp))
        }
    }
}

@Composable
internal fun NeonConversationScreen(
    lesson: Lesson,
    turnIndex: Int,
    translation: Boolean,
    transcript: String,
    capturePhase: String,
    status: String,
    learner: Learner,
    nextLabel: String,
    onBack: () -> Unit,
    onPrompt: () -> Unit,
    onReply: () -> Unit,
    onSpeak: () -> Unit,
    onTranslation: () -> Unit,
    onNext: () -> Unit,
    voiceChoice: VoiceChoice,
    onVoiceSettings: () -> Unit
) {
    val turn = lesson.conversation[turnIndex]
    Column(
        Modifier.fillMaxSize().background(navy).padding(horizontal = 17.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.semantics { contentDescription = "Quay lại" }) {
                StudioIcon("back", color = white)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Speakivra", color = white, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text("Hội thoại theo kịch bản", color = muted, fontSize = 12.sp)
            }
            Spacer(Modifier.width(48.dp))
        }
        Text(lessonLabel(lesson), color = white, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Color(0xFF26406D))) {
            Box(Modifier.fillMaxWidth((turnIndex + 1f) / lesson.conversation.size)
                .fillMaxHeight().background(Brush.horizontalGradient(listOf(neon, Color(0xFF597CFF))), CircleShape))
        }
        Text("Lượt ${turnIndex + 1}/${lesson.conversation.size} · Mô phỏng theo kịch bản",
            color = muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        VoiceSelectionChips(voiceChoice, onVoiceSettings)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            NeonMessage(turn.prompt, if (translation) turn.translation else null, null, false, onPrompt)
            NeonMessage(turn.reply, if (translation) turn.replyTranslation else null, learner, true, onReply)
            if (capturePhase == "idle" && transcript.isNotBlank()) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF132747)).border(1.dp, Color(0xFF365582), RoundedCornerShape(12.dp))
                        .padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearnerAvatar(learner.avatar, Modifier.size(34.dp), square = true)
                    Column(Modifier.weight(1f)) {
                        Text("Lời bạn vừa nói", color = muted, fontSize = 11.sp)
                        Text(transcript, color = white, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                    }
                }
            }
            Text(status, color = muted, fontSize = 12.sp)
            Text("Nhận dạng từ, chưa chấm phát âm chuyên sâu.",
                color = muted, fontSize = 11.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
        Row(
            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onTranslation,
                border = BorderStroke(1.dp, Color(0xFF3D6195)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = white)
            ) { Text(if (translation) "Ẩn dịch" else "Hiện dịch") }
            Box(
                Modifier.size(100.dp).shadow(10.dp, CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF22C7FF), Color(0xFF315BEC))), CircleShape)
                    .border(2.dp, Color(0xFFAAEBFF), CircleShape)
                    .clickable(role = Role.Button, onClick = onSpeak)
                    .semantics { contentDescription = if (capturePhase == "listening") "Kết thúc lượt nói" else "Nói và nhận dạng" },
                Alignment.Center
            ) {
                StudioIcon("mic", Modifier.size(48.dp), Color.White)
            }
            Text("🎤", fontSize = 22.sp, modifier = Modifier.clickable(onClick = onSpeak))
        }
        Text(captureLabel(capturePhase, "Nhấn để nói"),
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            color = muted, fontSize = 13.sp)
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF1174C7), Color(0xFF315DE5))))
                .clickable(role = Role.Button, onClick = onNext).heightIn(min = 49.dp),
            Alignment.Center
        ) { Text(nextLabel, color = white, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
    }
}
