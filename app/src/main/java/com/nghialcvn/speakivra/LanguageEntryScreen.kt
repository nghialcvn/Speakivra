package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class StudyLanguage(val label: String) { ENGLISH("Tiếng Anh"), CHINESE("Tiếng Trung") }

/** Approved entry: two independent learning areas, with the same profile and attendance. */
@Composable
internal fun LanguageEntryScreen(onSelect: (StudyLanguage) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.brand_logo_clear), "Logo Speakivra", Modifier.size(224.dp, 180.dp).testTag("entry-brand-logo"))
            EntryBrandWordmark()
        }
        Spacer(Modifier.height(12.dp))
        Text("Hôm nay bạn muốn học gì?", fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text("Chọn ngôn ngữ để bắt đầu", color = studio.muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        StudyLanguage.entries.forEach { language ->
            StudioCard(Modifier.fillMaxWidth().clickable { onSelect(language) }.testTag(if (language == StudyLanguage.ENGLISH) "language-en" else "language-zh")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val chinese = language == StudyLanguage.CHINESE
                    val badge = if (chinese && studio == LightStudio) Color(0xFFC9F3FC) else studio.peach
                    val accent = if (chinese && studio == LightStudio) Color(0xFF057CAE) else studio.purple
                    Box(Modifier.size(64.dp).background(badge, RoundedCornerShape(18.dp)), Alignment.Center) {
                        Text(if (chinese) "汉" else "Aa", fontSize = 32.sp, color = accent, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        EntryLanguageTitle(language.label)
                        Text(if (language == StudyLanguage.ENGLISH) "English" else "中文", color = studio.muted)
                        Surface(color = badge, shape = RoundedCornerShape(16.dp)) {
                            Text("A1 → C2 • Khách sạn", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = accent, fontSize = 12.sp)
                        }
                    }
                    StudioIcon("chevron", Modifier.size(18.dp), studio.purple)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Mỗi ngôn ngữ, một hành trình riêng.", fontStyle = FontStyle.Italic, color = studio.muted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun StudyLanguageBar(language: StudyLanguage, onSwitch: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Surface(onClick = onSwitch, color = studio.peach, shape = RoundedCornerShape(50)) {
            Text(language.flag() + " ⌄", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSwitch, modifier = Modifier.testTag("switch-study-language")) { Text("Đổi ngôn ngữ") }
    }
}
