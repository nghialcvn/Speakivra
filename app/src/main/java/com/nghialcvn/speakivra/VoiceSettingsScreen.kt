package com.nghialcvn.speakivra

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Approved neon voice-settings extension. Other Speakivra screens keep their prior theme. */
private val voiceBackground = Color(0xFF071126)
private val voiceCard = Color(0xFF14213D)
private val voiceAccent = Color(0xFF33B4FF)
private val voiceText = Color(0xFFF1F4FF)
private val voiceMuted = Color(0xFFB8C7E1)

@Composable
private fun VoicePanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(voiceCard, Color(0xFF0E1932))))
            .border(1.dp, Color(0xFF35476D), RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = voiceText)
        content()
    }
}

@Composable
private fun VoiceOption(label: String, subtitle: String, checked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (checked) Color(0xFF173961) else Color(0xFF18233A))
            .border(if (checked) 2.dp else 1.dp, if (checked) voiceAccent else Color(0xFF35476D), shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 62.dp).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = checked, onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = voiceAccent, unselectedColor = voiceMuted)
        )
        Column(Modifier.weight(1f)) {
            Text(label, color = voiceText, fontWeight = FontWeight.Bold)
            Text(subtitle, color = voiceMuted, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun VoiceSettingsScreen(
    choice: VoiceChoice,
    available: List<InstalledSpeechVoice>,
    speed: Int,
    message: String,
    onBack: () -> Unit,
    onAccent: (String) -> Unit,
    onGender: (String) -> Unit,
    onAssign: (String) -> Unit,
    onPreview: (String) -> Unit,
    onResetDefault: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(voiceBackground).verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Quay lại" }) {
                StudioIcon("back", color = voiceText)
            }
            Column {
                Text("Giọng đọc & Ngôn ngữ", color = voiceText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                Text("Speakivra · Cá nhân hóa giọng đọc", color = voiceMuted, fontSize = 12.sp)
            }
        }
        VoicePanel("Ngôn ngữ phát âm") {
            VoiceOption("🇺🇸 Anh-Mỹ (US)", "English · American", choice.accent == "US") { onAccent("US") }
            VoiceOption("🇬🇧 Anh-Anh (UK)", "English · British", choice.accent == "GB") { onAccent("GB") }
        }
        VoicePanel("Giọng đọc") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("female" to "Giọng nữ", "male" to "Giọng nam").forEach { (key, label) ->
                    val active = choice.gender == key
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(15.dp))
                            .background(if (active) Color(0xFF173961) else Color(0xFF18233A))
                            .border(if (active) 2.dp else 1.dp, if (active) voiceAccent else Color(0xFF35476D), RoundedCornerShape(15.dp))
                            .clickable(role = Role.RadioButton) { onGender(key) }
                            .padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudioIcon("speaker", Modifier.size(29.dp), if (active) voiceAccent else voiceMuted)
                        Text(label, color = voiceText, fontWeight = FontWeight.ExtraBold)
                        RadioButton(selected = active, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = voiceAccent))
                    }
                }
            }
            Text(
                "Android không xác nhận giới tính giọng TTS. Hãy nghe thử và tự gán một giọng phù hợp; app sẽ không tự đoán.",
                color = voiceMuted, fontSize = 12.sp
            )
        }
        VoicePanel("Nghe thử giọng trên máy") {
            if (choice.usesBundledAudio) {
                Text("Đang dùng giọng nữ neural Anh-Mỹ gốc cho câu có âm thanh sẵn.", color = voiceText, fontSize = 13.sp)
                Text("Các câu mới hoặc có tên riêng dùng TTS ngoại tuyến trên điện thoại.", color = voiceMuted, fontSize = 12.sp)
            } else if (choice.voiceId == null) {
                Text("Chưa gán giọng cho ${choice.accentLabel} · ${choice.genderLabel}. Chọn một giọng bên dưới rồi bấm Dùng giọng này.", color = Color(0xFFFFD18A), fontSize = 13.sp)
            } else {
                Text("Giọng đã chọn: ${choice.voiceId}", color = Color(0xFF91F1C8), fontSize = 12.sp)
            }
            if (available.isEmpty()) {
                Text("Chưa tìm thấy giọng ${choice.accentLabel} ngoại tuyến. Kiểm tra hoặc cài dữ liệu giọng trong Cài đặt TTS của Android.", color = voiceMuted, fontSize = 13.sp)
            }
            available.forEachIndexed { index, voice ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                        .background(Color(0xFF1B2B49)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Giọng trên máy ${index + 1}", color = voiceText, fontWeight = FontWeight.Bold)
                    Text(voice.id, color = voiceMuted, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onPreview(voice.id) },
                            border = BorderStroke(1.dp, voiceAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = voiceText)
                        ) { Text("▶ Nghe thử") }
                        Button(
                            onClick = { onAssign(voice.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1878CF))
                        ) { Text(if (voice.id == choice.voiceId) "✓ Đã chọn" else "Dùng giọng này") }
                    }
                }
            }
            if (choice.accent == "US" && choice.gender == "female") {
                TextButton(onClick = onResetDefault) {
                    Text("Khôi phục giọng nữ neural gốc", color = voiceAccent)
                }
            }
            if (message.isNotBlank()) Text(message, color = voiceMuted, fontSize = 12.sp)
        }
        VoicePanel("Tốc độ đọc hiện tại") {
            Text("${speedLabel(speed)} · Giữ nguyên tốc độ đã cài trước đây", color = voiceText)
        }
        Text(
            "Giọng đã chọn được lưu riêng theo người học. Không tải giọng trả phí, không thay đổi bài học hoặc dữ liệu tiến độ.",
            color = voiceMuted, fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))
    }
}
