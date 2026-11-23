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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role

private val neonBlue = Color(0xFF36B8FF)
private val neonWhite = Color(0xFFF3F6FF)
private val neonGrey = Color(0xFFB2C1DA)
private val neonPanel = Color(0xFF12203A)

@Composable
private fun NeonSettingsTile(icon: String, title: String, detail: String, onClick: () -> Unit, selected: Boolean = false) {
    val shape = RoundedCornerShape(17.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF152541), Color(0xFF111B35))))
            .border(if (selected) 2.dp else 1.dp, if (selected) neonBlue else Color(0xFF344665), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 69.dp).padding(horizontal = 15.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                .background(if (selected) Color(0xFF195080) else Color(0xFF24375A)),
            contentAlignment = Alignment.Center
        ) { StudioIcon(icon, Modifier.size(24.dp), neonBlue) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = neonWhite, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(detail, color = neonGrey, fontSize = 12.sp)
        }
        StudioIcon("chevron", Modifier.size(18.dp), neonGrey)
    }
}

@Composable
internal fun NeonProfileScreen(
    speed: Int,
    goal: Int,
    version: String,
    learner: Learner,
    voiceChoice: VoiceChoice,
    onSwitchLearner: () -> Unit,
    onEditLearner: () -> Unit,
    onTheme: (Boolean) -> Unit,
    onNeon: () -> Unit,
    onVoice: () -> Unit,
    onSpeed: () -> Unit,
    onGoal: () -> Unit,
    onUpdates: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF071126))
            .verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Text("Cài đặt", modifier = Modifier.fillMaxWidth(), fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold, color = neonWhite)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF142747), Color(0xFF101B37))))
                .border(1.dp, Color(0xFF344665), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra", Modifier.size(55.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Speakivra", color = neonWhite, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("V$version Beta · Học tiếng Anh", color = neonGrey, fontSize = 12.sp)
            }
        }
        NeonSettingsTile("speaker", "Giọng đọc & Ngôn ngữ",
            "${voiceChoice.accentLabel} · ${voiceChoice.genderLabel}", onVoice, selected = true)
        NeonSettingsTile("gauge", "Tốc độ đọc", speedLabel(speed), onSpeed)
        NeonSettingsTile("target", "Mục tiêu học tập", "$goal phút mỗi ngày", onGoal)
        Text("Giao diện", color = neonWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(
                Triple("Sáng", false, false),
                Triple("Tối", true, false),
                Triple("Neon", true, true)
            ).forEach { (label, dark, neon) ->
                val active = neon
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                        .background(if (active) Color(0xFF164A79) else neonPanel)
                        .border(if (active) 2.dp else 1.dp, if (active) neonBlue else Color(0xFF344665), RoundedCornerShape(13.dp))
                        .clickable(role = Role.RadioButton) { if (neon) onNeon() else onTheme(dark) }
                        .heightIn(min = 46.dp).padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) { Text(label, color = neonWhite, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium) }
            }
        }
        Text("Người học", color = neonWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(neonPanel)
                .border(1.dp, Color(0xFF344665), RoundedCornerShape(16.dp)).padding(11.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LearnerAvatar(learner.avatar, Modifier.size(53.dp))
            Column(Modifier.weight(1f)) {
                Text(learner.name, color = neonWhite, fontWeight = FontWeight.ExtraBold)
                Text("Tiến độ được lưu riêng", color = neonGrey, fontSize = 12.sp)
            }
            TextButton(onClick = onEditLearner) { Text("Sửa", color = neonBlue) }
            TextButton(onClick = onSwitchLearner) { Text("Đổi", color = neonBlue) }
        }
        NeonSettingsTile("phone", "Cập nhật ứng dụng", "Kiểm tra phiên bản mới và cài đặt", onUpdates)
        Text("Không đổi dữ liệu học, hồ sơ hoặc khóa ký khi thay giao diện.",
            color = neonGrey, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
    }
}
