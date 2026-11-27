package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The approved settings layout has ONE component tree shared by Light, Dark and
 * Neon. Appearance changes only tokens, shine and gradients, never the geometry.
 */
@Composable
internal fun UnifiedSettingsScreen(
    dark: Boolean,
    neon: Boolean,
    speed: Int,
    goal: Int,
    version: String,
    learner: Learner,
    choice: VoiceChoice,
    language: String,
    repeats: Int,
    reminder: Boolean,
    onBack: () -> Unit,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
    onTheme: (Boolean) -> Unit,
    onNeon: () -> Unit,
    onVoice: () -> Unit,
    onAccent: (String) -> Unit,
    onGender: (String) -> Unit,
    onLanguage: (String) -> Unit,
    onStudy: () -> Unit,
    onReminder: (Boolean) -> Unit,
    onSpeed: () -> Unit,
    onGoal: () -> Unit,
    onSound: () -> Unit,
    onUpdates: () -> Unit,
    onAbout: () -> Unit,
    onAccount: () -> Unit = {},
    chinese: Boolean = false
) {
    val p = studio
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Cài đặt", color = p.ink, fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold)
            Text("V$version Beta", color = p.coral, fontSize = 11.sp,
                fontWeight = FontWeight.Bold)
        }
        Text("Chạm để mở một nhóm cài đặt. Nhóm trước tự thu lại.",
            color = p.muted, fontSize = 11.sp, lineHeight = 16.sp)
        Column(Modifier.fillMaxWidth().weight(1f)
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            AccordionSection(
                title = "Học tập", summary = if (chinese) "Trung – Việt • Phổ thông Trung Quốc" else (if (language == LearningOptions.ENGLISH)
                    "Anh – Anh" else "Anh – Việt") + " • " + choice.accentLabel,
                icon = "book", neon = neon,
                open = expanded == "study", onToggle = {
                    expanded = if (expanded == "study") null else "study"
                }
            ) {
                SettingsTile("globe", "Ngôn ngữ học",
                    if (chinese) "Trung – Việt • Chữ giản thể • Pinyin" else if (language == LearningOptions.ENGLISH) "Anh – Anh • Hướng dẫn bằng tiếng Anh"
                    else "Anh – Việt • Giải thích bằng tiếng Việt", neon, onVoice)
                SettingsTile("speaker", "Giọng đọc & Ngôn ngữ",
                    if (chinese) "Tiếng Trung phổ thông • Giọng ngoại tuyến trên máy" else "${choice.accentLabel} • ${choice.genderLabel}", neon, onVoice)
                InnerPanel(neon) {
                    Text("Tùy chọn ngôn ngữ", color = p.ink,
                        fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    if (chinese) {
                        Text("Tiếng Trung phổ thông • Trung Quốc (zh-CN)", color = p.muted, fontSize = 13.sp)
                        Text("Chữ giản thể • Pinyin • Nghĩa tiếng Việt", color = p.muted, fontSize = 13.sp)
                    } else {
                    RadioChoice("🇺🇸", "Anh – Mỹ (US)", "English (American)",
                        choice.accent == "US", neon) { onAccent("US") }
                    Text("Chế độ giải thích", color = p.ink,
                        fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip("Anh – Việt",
                            language == LearningOptions.VIETNAMESE, neon,
                            Modifier.weight(1f)) { onLanguage(LearningOptions.VIETNAMESE) }
                        ChoiceChip("Anh – Anh",
                            language == LearningOptions.ENGLISH, neon,
                            Modifier.weight(1f)) { onLanguage(LearningOptions.ENGLISH) }
                    }
                    Text("Giọng đọc (TTS)", color = p.ink,
                        fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderChip("Giọng nữ", choice.gender == "female", neon,
                            Modifier.weight(1f)) { onGender("female") }
                        GenderChip("Giọng nam", choice.gender == "male", neon,
                            Modifier.weight(1f)) { onGender("male") }
                    }
                    }
                    TextButton(onClick = onVoice, modifier = Modifier.fillMaxWidth()) {
                        Text("Nghe thử và chọn giọng có thật trên máy", color = p.coral, fontSize = 12.sp)
                    }
                    Text("Android không đảm bảo nhãn giới tính của TTS. Cần nghe thử và gán đúng giọng.",
                        fontSize = 11.sp, color = p.muted, lineHeight = 16.sp)
                }
                SettingsTile("sliders", "Tùy chỉnh bài học",
                    "A1 / A2 / B1 / B2 / C1 / C2 • Lặp $repeats lần", neon, onStudy)
                InnerPanel(neon) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StudioIcon("bell", Modifier.size(25.dp), p.coral)
                        Column(Modifier.weight(1f)) {
                            Text("Thông báo nhắc học", fontSize = 13.sp,
                                color = p.ink, fontWeight = FontWeight.Bold)
                            Text(if (reminder) "Đang bật • khoảng 20:00" else "Đang tắt",
                                fontSize = 11.sp, color = p.muted)
                        }
                        Switch(checked = reminder, onCheckedChange = onReminder)
                    }
                }
            }

            AccordionSection("Tùy chỉnh", "Giao diện • Tốc độ • Âm thanh",
                "sun", neon, expanded == "custom", onToggle = {
                    expanded = if (expanded == "custom") null else "custom"
                }) {
                InnerPanel(neon) {
                    Text("Giao diện", color = p.ink,
                        fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip("Sáng", !dark && !neon, neon,
                            Modifier.weight(1f)) { onTheme(false) }
                        ChoiceChip("Tối", dark && !neon, neon,
                            Modifier.weight(1f)) { onTheme(true) }
                        ChoiceChip("Neon", neon, neon,
                            Modifier.weight(1f), onNeon)
                    }
                    Text("Một bố cục duy nhất; chỉ thay màu và hiệu ứng.",
                        color = p.muted, fontSize = 11.sp)
                }
                SettingsTile("gauge", "Tốc độ nói", speedLabel(speed), neon, onSpeed)
                SettingsTile("target", "Mục tiêu mỗi ngày",
                    "$goal phút", neon, onGoal)
                SettingsTile("volume", "Âm thanh & thông báo",
                    "Giọng đọc, nhắc học và quyền Android", neon, onSound)
            }

            AccordionSection("Hồ sơ", learner.name, "profile", neon,
                expanded == "profile", onToggle = {
                    expanded = if (expanded == "profile") null else "profile"
                }) {
                InnerPanel(neon) {
                    Row(Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LearnerAvatar(learner.avatar, Modifier.size(46.dp))
                        Column(Modifier.weight(1f)) {
                            Text(learner.name, color = p.ink, fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold)
                            GuestIdentityLabel(learner)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                            Text("Sửa hồ sơ")
                        }
                        OutlinedButton(onClick = onSwitch, modifier = Modifier.weight(1f)) {
                            Text("Đổi người học")
                        }
                    }
                }
                SettingsTile("phone", "Tài khoản & đồng bộ", "Liên kết hồ sơ khách và học tiếp trên thiết bị khác", neon, onAccount)
            }

            AccordionSection("Ứng dụng", "Cập nhật mới • Phiên bản • Thông tin",
                "phone", neon, expanded == "app", onToggle = {
                    expanded = if (expanded == "app") null else "app"
                }) {
                InnerPanel(neon) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.clip(RoundedCornerShape(13.dp))
                            .background(Brush.horizontalGradient(listOf(
                                Color(0xFF087ACB), Color(0xFF3A50E7))))
                            .padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text("✨ Cập nhật mới", color = Color.White,
                                fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Text("V$version", color = p.muted, fontSize = 11.sp)
                    }
                    InstalledReleaseNotes.forVersion(version).forEach { (icon, text) ->
                        UpdateMiniRow(icon, text)
                    }
                }
                SettingsTile("phone", "Kiểm tra cập nhật",
                    "Phiên bản đang dùng: v$version", neon, onUpdates)
                SettingsTile("info", "Về Speakivra",
                    "Phiên bản, quyền riêng tư và hỗ trợ", neon, onAbout)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AccordionSection(
    title: String,
    summary: String,
    icon: String,
    neon: Boolean,
    open: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val p = studio
    val shape = RoundedCornerShape(18.dp)
    val borderColor = if (open) p.coral else p.border
    val modifier = Modifier.fillMaxWidth()
        .then(if (neon) Modifier.neonGlass(18, open)
            else Modifier.clip(shape)
                .background(Brush.linearGradient(listOf(p.card, p.card)))
                .border(1.dp, borderColor, shape))
    Column(modifier) {
        Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onToggle)
            .semantics { stateDescription = if (open) "Đang mở" else "Đang đóng" }
            .padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(Modifier.size(43.dp).clip(RoundedCornerShape(12.dp))
                .background(p.peach), contentAlignment = Alignment.Center) {
                StudioIcon(icon, Modifier.size(24.dp), p.coral)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                    color = p.ink, maxLines = 1)
                Text(summary, color = p.muted, fontSize = 11.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            StudioIcon(if (open) "down" else "chevron",
                Modifier.size(18.dp), if (open) p.coral else p.muted)
        }
        if (open) {
            HorizontalDivider(color = p.border,
                modifier = Modifier.padding(horizontal = 12.dp))
            Column(Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
        }
    }
}

@Composable
private fun InnerPanel(neon: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val p = studio
    val shape = RoundedCornerShape(13.dp)
    Column(Modifier.fillMaxWidth().clip(shape)
        .background(if (neon) NeonLook.glossyPanel
            else Brush.linearGradient(listOf(p.lavender.copy(alpha = .28f),
                p.lavender.copy(alpha = .28f))))
        .border(1.dp, if (neon) NeonLook.azure else p.border, shape).padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun SettingsTile(
    icon: String, title: String, description: String, neon: Boolean,
    onClick: () -> Unit
) {
    val p = studio
    val shape = RoundedCornerShape(12.dp)
    Row(Modifier.fillMaxWidth().clip(shape)
        .background(if (neon) NeonLook.glossyPanel
            else Brush.linearGradient(listOf(p.card, p.card)))
        .border(1.dp, if (neon) NeonLook.blue else p.border, shape)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)).background(p.peach),
            contentAlignment = Alignment.Center) {
            StudioIcon(icon, Modifier.size(21.dp), p.coral)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, color = p.ink,
                fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(description, color = p.muted, fontSize = 11.sp, lineHeight = 16.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        StudioIcon("chevron", Modifier.size(15.dp), p.muted)
    }
}

@Composable
private fun RadioChoice(flag: String, title: String, sub: String,
                        selected: Boolean, neon: Boolean, onClick: () -> Unit) {
    val p = studio
    val shape = RoundedCornerShape(11.dp)
    Row(Modifier.fillMaxWidth().clip(shape)
        .background(if (selected) p.peach else if (neon) Color(0xD2162645) else p.card)
        .border(if (selected) 1.6.dp else 1.dp, if (selected) p.coral else p.border, shape)
        .clickable(role = Role.RadioButton, onClick = onClick)
        .semantics { this.selected = selected }.padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        RadioButton(selected = selected, onClick = null,
            modifier = Modifier.size(23.dp))
        Text(flag, fontSize = 19.sp)
        Column(Modifier.weight(1f)) {
            Text(title, color = p.ink, fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(sub, color = p.muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ChoiceChip(text: String, selected: Boolean, neon: Boolean,
                       modifier: Modifier, onClick: () -> Unit) {
    val p = studio
    val shape = RoundedCornerShape(11.dp)
    Box(modifier.clip(shape)
        .background(if (selected) p.peach else if (neon) Color(0xD8192A4A) else p.card)
        .border(if (selected) 1.6.dp else 1.dp, if (selected) p.coral else p.border, shape)
        .clickable(role = Role.RadioButton, onClick = onClick)
        .semantics { this.selected = selected }
        .heightIn(min = 42.dp).padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center) {
        Text(text, color = if (selected) p.coral else p.ink, fontSize = 12.sp,
            fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun GenderChip(text: String, selected: Boolean, neon: Boolean,
                       modifier: Modifier, onClick: () -> Unit) {
    val p = studio
    val shape = RoundedCornerShape(12.dp)
    Column(modifier.clip(shape)
        .background(if (selected) p.peach else if (neon) Color(0xD8192A4A) else p.card)
        .border(if (selected) 1.6.dp else 1.dp, if (selected) p.coral else p.border, shape)
        .clickable(role = Role.RadioButton, onClick = onClick)
        .semantics { this.selected = selected }
        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        StudioIcon("speaker", Modifier.size(28.dp), if (selected) p.coral else p.muted)
        Text(text, color = p.ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UpdateMiniRow(icon: String, title: String) {
    val p = studio
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(9.dp))
            .background(p.peach), contentAlignment = Alignment.Center) {
            StudioIcon(icon, Modifier.size(20.dp), p.coral)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                color = p.ink)
        }
    }
}
