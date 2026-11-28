package com.nghialcvn.speakivra

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

internal fun savedStudyLanguage(prefs: SharedPreferences): StudyLanguage =
    when (prefs.getString("study_language", null)) {
        "CHINESE", "zh" -> StudyLanguage.CHINESE
        else -> StudyLanguage.ENGLISH
    }
internal fun saveStudyLanguage(prefs: SharedPreferences, language: StudyLanguage) {
    prefs.edit().putString("study_language", language.name).apply()
}
internal fun StudyLanguage.flag() = if (this == StudyLanguage.ENGLISH) "🇺🇸" else "🇨🇳"

@Composable internal fun ProfileSplashScreen() {
    val shine = rememberBrandSweepPhase()
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Image(painterResource(R.drawable.brand_logo_clear), "Logo Speakivra", Modifier.size(240.dp, 196.dp).testTag("splash-brand-logo").brandShine(shine))
        EntryBrandWordmark()
        Spacer(Modifier.height(36.dp))
        Text("Every language, a new horizon.", fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Mỗi ngôn ngữ, một chân trời mới.", color = studio.muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        val transition = rememberInfiniteTransition(label = "Splash dots")
        val phase by transition.animateFloat(0f, 3f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "Loading")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.semantics { contentDescription = "Đang tải" }) {
            listOf(Color(0xFF00D5E5), Color(0xFF70BFFF), Color(0xFFB75CFF)).forEachIndexed { index, color ->
                Box(Modifier.size(8.dp).background(color.copy(alpha = if (phase.toInt() == index) 1f else .4f), RoundedCornerShape(50)))
            }
        }
    }
}
@Composable internal fun StudyLanguageTiles(selected: StudyLanguage, onSelect: (StudyLanguage) -> Unit) {
    var upcoming by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StudyLanguage.entries.forEach { language ->
            Box(Modifier.size(48.dp).border(if (selected == language) 2.dp else 1.dp,
                if (selected == language) studio.purple else studio.border, RoundedCornerShape(12.dp))
                .then(if (selected == language) Modifier.border(2.dp, Brush.linearGradient(listOf(Color(0xFF00D5E5), Color(0xFFB75CFF))), RoundedCornerShape(12.dp)) else Modifier)
                .clickable { onSelect(language) }.testTag(if (language == StudyLanguage.ENGLISH) "language-en" else "language-zh").semantics { contentDescription = language.label }, Alignment.Center) {
                Text(language.flag(), fontSize = 27.sp)
            }
        }
        Box(Modifier.size(48.dp).border(1.dp, studio.border, RoundedCornerShape(12.dp))
            .clickable { upcoming = true }.semantics { contentDescription = "Thêm ngôn ngữ học" }, Alignment.Center) {
            Text("+", fontSize = 30.sp)
        }
    }
    if (upcoming) Dialog(onDismissRequest = { upcoming = false }) {
        Surface(modifier = Modifier.border(1.dp, Brush.linearGradient(listOf(Color(0xFF00D5E5), Color(0xFFB75CFF))), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = studio.card, contentColor = studio.ink) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                StudioIcon("globe", Modifier.size(48.dp), studio.purple)
                Text("Ngôn ngữ mới sắp ra mắt", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, textAlign = TextAlign.Center)
                Text("Các ngôn ngữ mới đang được chuẩn bị. Bạn chờ thêm chút nhé!", color = studio.muted, textAlign = TextAlign.Center)
                CoralButton("Đã hiểu", chevron = false) { upcoming = false }
            }
        }
    }
}
@Composable internal fun StudyLanguageDialog(selected: StudyLanguage, onDismiss: () -> Unit, onSelect: (StudyLanguage) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.border(1.dp, Brush.linearGradient(listOf(Color(0xFF00D5E5), Color(0xFFB75CFF))), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = studio.card, contentColor = studio.ink) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Chọn ngôn ngữ học", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                StudyLanguageTiles(selected, onSelect)
            }
        }
    }
}
@Composable internal fun LearnerAccountMark(learner: Learner) {
    val context = LocalContext.current
    val identity = GuestIdentityStore(context).peek(learner)
    val user = AccountRuntime.get(context).auth.currentUser?.takeIf { it.uid == identity?.ownerUid }
    val resource = accountBrandResource(identity?.ownerUid, user?.uid,
        user?.providerData.orEmpty().map { it.providerId }, user?.email)
    if (resource != null) Image(painterResource(resource), if (identity?.ownerUid == null) "Hồ sơ khách" else "Tài khoản ${user?.email.orEmpty()}", Modifier.size(24.dp))
    else StudioIcon(if (user == null) "cloud-check" else "email", Modifier.size(22.dp), studio.purple)
}

/** A mailbox domain is used only for an authenticated owner, never as proof of Google sign-in. */
internal fun accountBrandResource(ownerUid: String?, signedInUid: String?, providers: List<String>, email: String?): Int? {
    if (ownerUid == null) return R.drawable.brand_logo_clear
    if (ownerUid != signedInUid) return null
    if ("google.com" in providers) return R.drawable.account_google
    if ("microsoft.com" in providers) return R.drawable.account_microsoft
    return when (email?.substringAfterLast('@')?.lowercase()) {
        "gmail.com", "googlemail.com" -> R.drawable.account_gmail
        "outlook.com", "hotmail.com", "live.com", "msn.com" -> R.drawable.account_microsoft
        else -> null
    }
}
