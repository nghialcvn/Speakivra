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

@Composable
internal fun StudyLanguageBar(language: StudyLanguage, onSwitch: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Surface(onClick = onSwitch, modifier = Modifier.testTag("study-language-flag"), color = studio.peach, shape = RoundedCornerShape(50)) {
            Text(language.flag() + " ⌄", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSwitch, modifier = Modifier.testTag("switch-study-language")) { Text("Đổi ngôn ngữ") }
    }
}
