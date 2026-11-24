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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val neonBlue = Color(0xFF35B8FF)
private val neonWhite = Color(0xFFF4F7FF)
private val neonGrey = Color(0xFFB2C3E0)
private val neonPanel = Color(0xFF14213C)
private val neonLine = Color(0xFF34476A)
private val neonBackground = Color(0xFF071126)

@Composable
private fun NeonGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
            color = neonWhite, modifier = Modifier.padding(start = 3.dp))
        content()
    }
}

@Composable
private fun NeonSettingsTile(icon: String, title: String, detail: String,
                             onClick: () -> Unit, highlighted: Boolean = false) {
    val shape = RoundedCornerShape(17.dp)
    Row(Modifier.fillMaxWidth().clip(shape)
        .background(Brush.horizontalGradient(listOf(Color(0xFF172742), Color(0xFF101B35))))
        .border(if (highlighted) 2.dp else 1.dp,
            if (highlighted) neonBlue else neonLine, shape)
        .clickable(role = Role.Button, onClick = onClick)
        .heightIn(min = 66.dp).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
            .background(if (highlighted) Color(0xFF18517F) else Color(0xFF23365A)),
            contentAlignment = Alignment.Center) {
            StudioIcon(icon, Modifier.size(25.dp), neonBlue)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = neonWhite, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(detail, color = neonGrey, fontSize = 12.sp)
        }
        StudioIcon("chevron", Modifier.size(18.dp), neonGrey)
    }
}

@Composable
private fun NeonRadioOption(title: String, description: String, active: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(13.dp)
    Row(Modifier.fillMaxWidth().clip(shape)
        .background(if (active) Color(0xFF15385E) else Color(0xFF101D35))
        .border(if (active) 2.dp else 1.dp, if (active) neonBlue else neonLine, shape)
        .clickable(role = Role.RadioButton, onClick = onClick)
        .semantics { selected = active }
        .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RadioButton(selected = active, onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = neonBlue))
        Column(Modifier.weight(1f)) {
            Text(title, color = neonWhite, fontWeight = FontWeight.Bold)
            Text(description, color = neonGrey, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun NeonProfileScreen(
    speed: Int,
    goal: Int,
    version: String,
    learner: Learner,
    voiceChoice: VoiceChoice,
    explanationLanguage: String,
    repeatCount: Int,
    reminderEnabled: Boolean,
    onBack: () -> Unit,
    onSwitchLearner: () -> Unit,
    onEditLearner: () -> Unit,
    onTheme: (Boolean) -> Unit,
    onNeon: () -> Unit,
    onVoice: () -> Unit,
    onAccent: (String) -> Unit,
    onExplanationLanguage: (String) -> Unit,
    onSpeed: () -> Unit,
    onGoal: () -> Unit,
    onStudyOptions: () -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onSound: () -> Unit,
    onUpdates: () -> Unit,
    onAbout: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(neonBackground)
        .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack) { StudioIcon("back", color = neonWhite) }
            Text("Cài đặt", modifier = Modifier.weight(1f), fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold, color = neonWhite)
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF162949), Color(0xFF111D37))))
            .border(1.dp, neonLine, RoundedCornerShape(18.dp)).padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra",
                Modifier.size(58.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Speakivra", fontSize = 24.sp, color = neonWhite,
                    fontWeight = FontWeight.ExtraBold)
                Text("V$version Beta", color = neonBlue, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
                Text("Học và luyện nói tiếng Anh", color = neonGrey, fontSize = 12.sp)
            }
        }

        NeonGroup("Học tập") {
            NeonSettingsTile("globe", "Ngôn ngữ học",
                if (explanationLanguage == LearningOptions.ENGLISH) "Anh – Anh • Hướng dẫn bằng tiếng Anh"
                else "Anh – Việt • Giải thích bằng tiếng Việt",
                onVoice)
            NeonSettingsTile("speaker", "Giọng đọc & Ngôn ngữ",
                "${voiceChoice.accentLabel} • ${voiceChoice.genderLabel}",
                onVoice, highlighted = true)
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(neonPanel).border(1.dp, neonLine, RoundedCornerShape(16.dp))
                .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tùy chọn ngôn ngữ", color = neonWhite, fontWeight = FontWeight.Bold)
                NeonRadioOption("🇺🇸 Anh – Mỹ (US)", "English (American)",
                    voiceChoice.accent == "US") { onAccent("US") }
                NeonRadioOption("🇬🇧 Anh – Anh (UK)", "English (British)",
                    voiceChoice.accent == "GB") { onAccent("GB") }
                Text("Chế độ giải thích", color = neonWhite, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Anh – Việt" to LearningOptions.VIETNAMESE,
                        "Anh – Anh" to LearningOptions.ENGLISH).forEach { (text, mode) ->
                        val active = explanationLanguage == mode
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (active) Color(0xFF194F82) else Color(0xFF172641))
                            .border(if (active) 2.dp else 1.dp,
                                if (active) neonBlue else neonLine, RoundedCornerShape(12.dp))
                            .clickable(role = Role.RadioButton) { onExplanationLanguage(mode) }
                            .semantics { selected = active }
                            .padding(10.dp), contentAlignment = Alignment.Center) {
                            Text(text, color = neonWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("Chọn giọng nam/nữ và nghe thử trong Giọng đọc & Ngôn ngữ.",
                    color = neonGrey, fontSize = 11.sp)
            }
            NeonSettingsTile("sliders", "Tùy chỉnh bài học",
                "A1 / A2 • ${speedLabel(speed)} • lặp $repeatCount lần", onStudyOptions)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(neonPanel).border(1.dp, neonLine, RoundedCornerShape(16.dp))
                .clickable { onReminderToggle(!reminderEnabled) }
                .heightIn(min = 66.dp).padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioIcon("bell", Modifier.size(29.dp), Color(0xFFEF688A))
                Column(Modifier.weight(1f)) {
                    Text("Thông báo nhắc học", color = neonWhite,
                        fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(if (reminderEnabled) "Đang bật • khoảng 19:00 hằng ngày"
                         else "Đang tắt • chỉ nhắc khi bạn bật",
                        color = neonGrey, fontSize = 12.sp)
                }
                Switch(checked = reminderEnabled, onCheckedChange = onReminderToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF247BEB)))
            }
        }

        NeonGroup("Tùy chỉnh") {
            NeonSettingsTile("sun", "Giao diện", "Neon • Có thể chuyển Sáng / Tối", onNeon)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Triple("Sáng", false, false), Triple("Tối", true, false),
                    Triple("Neon", true, true)).forEach { (label, dark, neon) ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                        .background(if (neon) Color(0xFF164A79) else neonPanel)
                        .border(if (neon) 2.dp else 1.dp,
                            if (neon) neonBlue else neonLine, RoundedCornerShape(13.dp))
                        .clickable(role = Role.RadioButton) { if (neon) onNeon() else onTheme(dark) }
                        .semantics { selected = neon }
                        .heightIn(min = 45.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = neonWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
            NeonSettingsTile("gauge", "Tốc độ nói", speedLabel(speed), onSpeed)
            NeonSettingsTile("volume", "Âm thanh & thông báo",
                "Giọng đọc, nhắc học và quyền Android", onSound)
            NeonSettingsTile("target", "Mục tiêu học tập",
                "$goal phút mỗi ngày", onGoal)
        }

        NeonGroup("Hồ sơ") {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(neonPanel).border(1.dp, neonLine, RoundedCornerShape(16.dp))
                .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LearnerAvatar(learner.avatar, Modifier.size(48.dp))
                Column(Modifier.weight(1f)) {
                    Text(learner.name, color = neonWhite, fontWeight = FontWeight.Bold)
                    Text("Tiến độ lưu riêng trên máy", color = neonGrey, fontSize = 12.sp)
                }
                TextButton(onClick = onEditLearner) { Text("Sửa", color = neonBlue) }
                TextButton(onClick = onSwitchLearner) { Text("Đổi", color = neonBlue) }
            }
        }

        NeonGroup("Ứng dụng") {
            NeonSettingsTile("phone", "Cập nhật ứng dụng",
                "Phiên bản v$version • Kiểm tra và cài đè an toàn", onUpdates)
            NeonSettingsTile("info", "Về Speakivra",
                "Phiên bản, quyền riêng tư và hỗ trợ", onAbout)
        }
        Text("Giọng AI trong demo là thiết kế minh họa. Ứng dụng dùng clip đã duyệt và giọng TTS đã cài, không tự tạo giọng nam/nữ.",
            color = neonGrey, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
    }
}
