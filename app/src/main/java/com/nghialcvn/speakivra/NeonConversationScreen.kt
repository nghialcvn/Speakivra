package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
private val neon = Color(0xFF36B8FF)
private val white = Color(0xFFF3F6FF)
private val muted = Color(0xFFB2C1DE)
private val bubble = Color(0xFF1D315B)

@Composable
private fun AiAvatar() {
    val p = studio
    Box(Modifier.size(47.dp), contentAlignment = Alignment.Center) {
        LearnerAvatar(1, Modifier.size(40.dp))
        Text("AI", color = Color.White, fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.BottomEnd)
                .background(p.coral, RoundedCornerShape(7.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp))
    }
}

@Composable
private fun NeonMessage(
    text: String, translation: String?, learner: Learner?,
    isReply: Boolean, onPlay: () -> Unit, showPlay: Boolean = true,
    neonTheme: Boolean = true
) {
    val p = studio
    val foreground = p.ink
    val replyInk = Color.White
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = if (isReply) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom) {
        if (!isReply) {
            AiAvatar()
            Spacer(Modifier.width(7.dp))
        }
        Column(Modifier.fillMaxWidth(if (isReply) 0.79f else 0.81f),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(
                    if (isReply && neonTheme) listOf(Color(0xFF0871D8), Color(0xFF214FC2))
                    else if (isReply) listOf(p.coral, p.coralLight)
                    else if (neonTheme) listOf(bubble, Color(0xFF172643))
                    else listOf(p.card, p.card)))
                .border(1.dp,
                    if (isReply) p.coral else p.border,
                    RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(text, Modifier.weight(1f), fontSize = 17.sp,
                    lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (isReply) replyInk else foreground)
                if (showPlay) IconButton(onClick = onPlay, modifier = Modifier.size(35.dp)
                    .semantics { contentDescription =
                        if (isReply) "Nghe câu trả lời" else "Nghe câu hỏi" }) {
                    StudioIcon("speaker", Modifier.size(20.dp),
                        if (isReply) Color.White else p.coral)
                }
            }
            if (!translation.isNullOrBlank()) {
                Text(translation, color = p.muted, fontSize = 12.sp,
                    fontStyle = FontStyle.Italic, lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 10.dp))
            }
        }
        if (isReply && learner != null) {
            Spacer(Modifier.width(7.dp))
            LearnerAvatar(learner.avatar, Modifier.size(39.dp))
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
    onVoiceSettings: () -> Unit,
    explanationLanguage: String = LearningOptions.VIETNAMESE,
    onTypedText: (String) -> Unit = {},
    neonTheme: Boolean = true
) {
    val p = studio
    val white = p.ink
    val muted = p.muted
    val neon = p.coral
    val current = lesson.conversation[turnIndex]
    val englishOnly = explanationLanguage == LearningOptions.ENGLISH
    var showKeyboard by rememberSaveable { mutableStateOf(false) }
    var typedText by rememberSaveable(turnIndex) { mutableStateOf("") }
    // Full-bleed cyan/violet wave lives on the app root, behind the conversation.
    Column(Modifier.fillMaxSize().background(Color.Transparent)
        .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.semantics { contentDescription = "Quay lại" }) {
                StudioIcon("back", color = white)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Image(painterResource(R.drawable.speakivra_icon),
                        "Logo Speakivra", Modifier.size(30.dp))
                    Text("Speakivra", color = white, fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold)
                }
                Text("Mô phỏng theo kịch bản", color = muted, fontSize = 11.sp)
            }
            StudioIcon("book", Modifier.size(24.dp), Color(0xFFFFD779))
            Spacer(Modifier.width(10.dp))
        }
        Text("Bài ${Lessons.all.indexOfFirst { it.id == lesson.id } + 1}: ${lessonLabel(lesson)}",
            color = white, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).height(9.dp).clip(CircleShape)
                .background(p.border.copy(alpha = .5f))) {
                Box(Modifier.fillMaxWidth((turnIndex + 1f) / lesson.conversation.size)
                    .fillMaxHeight().background(Brush.horizontalGradient(
                        listOf(p.coral, p.coralLight)), CircleShape))
            }
            Text("${turnIndex + 1}/${lesson.conversation.size}",
                color = white, fontSize = 12.sp)
        }
        VoiceSelectionChips(voiceChoice, onVoiceSettings)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onTranslation) {
                Text(if (translation) "Ẩn hướng dẫn" else "Hiện hướng dẫn",
                    color = neon, fontSize = 12.sp)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)) {
            if (turnIndex > 0) {
                val previous = lesson.conversation[turnIndex - 1]
                Text("Lượt trước", color = muted, fontSize = 12.sp)
                NeonMessage(previous.reply, null, learner, true, onPlay = {},
                    showPlay = false, neonTheme = neonTheme)
            }
            NeonMessage(current.prompt,
                if (!translation) null else if (englishOnly)
                    EnglishStudy.explanation(current.prompt) else current.translation,
                null, false, onPrompt, neonTheme = neonTheme)
            NeonMessage(current.reply,
                if (!translation) null else if (englishOnly)
                    EnglishStudy.pronunciationTip(current.reply)
                else current.replyTranslation,
                learner, true, onReply, neonTheme = neonTheme)
            if (capturePhase == "idle" && transcript.isNotBlank()) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                    .background(p.card)
                    .border(1.dp, p.border, RoundedCornerShape(13.dp))
                    .padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LearnerAvatar(learner.avatar, Modifier.size(36.dp), square = true)
                    Column(Modifier.weight(1f)) {
                        Text("Câu trả lời của bạn", color = muted, fontSize = 11.sp)
                        Text(transcript, color = white, fontSize = 14.sp,
                            fontStyle = FontStyle.Italic)
                    }
                }
            }
            if (showKeyboard) {
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { if (it.length <= 500) typedText = it },
                    label = { Text("Nhập câu trả lời bằng tiếng Anh") },
                    singleLine = false, minLines = 1, maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = white, unfocusedTextColor = white,
                        focusedLabelColor = neon, unfocusedLabelColor = muted,
                        focusedBorderColor = neon)
                )
                OutlinedButton(
                    onClick = { onTypedText(typedText.trim()); showKeyboard = false },
                    enabled = typedText.isNotBlank()) {
                    Text("Gửi câu trả lời đã nhập")
                }
            }
            Text(status, color = muted, fontSize = 12.sp)
            Text(if (englishOnly) "Scripted dialogue • Not phonetic scoring."
                 else "Hội thoại theo kịch bản • Không chấm phát âm chuyên sâu.",
                color = muted, fontSize = 11.sp)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly) {
            IconButton(
                onClick = { showKeyboard = !showKeyboard },
                modifier = Modifier.size(56.dp).background(
                    Color(0xFF23345A), CircleShape)
                    .semantics { contentDescription = "Nhập câu trả lời" }
            ) { StudioIcon("keyboard", Modifier.size(28.dp), white) }
            Box(Modifier.size(104.dp)
                .shadow(13.dp, CircleShape)
                .background(Brush.linearGradient(
                    if (neonTheme) listOf(Color(0xFF27D0FF), Color(0xFF3058EE))
                    else listOf(p.coralLight, p.coral)), CircleShape)
                .border(2.dp, if (neonTheme) Color(0xFFB3EEFF) else p.coralLight, CircleShape)
                .clickable(role = Role.Button, onClick = onSpeak)
                .semantics { contentDescription =
                    if (capturePhase == "listening") "Kết thúc lượt nói" else "Nói và nhận dạng" },
                contentAlignment = Alignment.Center) {
                StudioIcon("mic", Modifier.size(56.dp), white)
            }
            IconButton(onClick = onNext,
                modifier = Modifier.size(56.dp)
                    .background(p.card, CircleShape)
                    .semantics { contentDescription = "Tiếp tục hội thoại" }) {
                StudioIcon("next", Modifier.size(29.dp), white)
            }
        }
        Text(captureLabel(capturePhase, "Nhấn và nói"),
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            color = muted, fontSize = 13.sp)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp))
            .background(Brush.horizontalGradient(
                if (neonTheme) listOf(Color(0xFF187ECF), Color(0xFF315BE8))
                else listOf(p.coral, p.coralLight)))
            .clickable(role = Role.Button, onClick = onNext)
            .heightIn(min = 44.dp), contentAlignment = Alignment.Center) {
            Text(nextLabel, color = white, fontWeight = FontWeight.ExtraBold)
        }
    }
}
