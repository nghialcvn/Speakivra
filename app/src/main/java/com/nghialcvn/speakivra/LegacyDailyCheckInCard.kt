package com.nghialcvn.speakivra

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import java.time.LocalDate
import kotlinx.coroutines.delay

@Composable
internal fun LegacyDailyCheckInCard(learner: Learner, preferences: SharedPreferences, onProfile: () -> Unit) {
    val p = studio
    var today by remember { mutableStateOf(LocalDate.now()) }
    var dates by remember(learner.id) { mutableStateOf(validCheckInDates(preferences.getStringSet(CHECK_IN_DATES, emptySet()).orEmpty())) }
    var error by remember(learner.id) { mutableStateOf(false) }
    val owner = LocalContext.current as? LifecycleOwner
    DisposableEffect(preferences, owner) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == CHECK_IN_DATES) dates = validCheckInDates(preferences.getStringSet(CHECK_IN_DATES, emptySet()).orEmpty())
        }
        val lifecycle = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) today = LocalDate.now() }
        preferences.registerOnSharedPreferenceChangeListener(listener); owner?.lifecycle?.addObserver(lifecycle)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener); owner?.lifecycle?.removeObserver(lifecycle) }
    }
    LaunchedEffect(Unit) { while (true) { today = LocalDate.now(); delay(30000) } }
    val state = DailyCheckIn(dates, today)
    val shape = RoundedCornerShape(if (p == NeonStudio) 22.dp else 18.dp)
    Box(Modifier.fillMaxWidth().shadow(5.dp, shape).clip(shape)
        .background(if (p == NeonStudio) androidx.compose.ui.graphics.Brush.linearGradient(listOf(p.card, p.card)) else brandPanelBrush(p))
        .border(1.dp, p.border, shape).heightIn(min = 142.dp)
        .semantics { contentDescription = "Điểm danh hằng ngày" }) {
        Image(painterResource(R.drawable.brand_logo_clear), null,
            Modifier.align(Alignment.TopEnd).offset(x = if (p == NeonStudio) 24.dp else 12.dp).size(140.dp).alpha(.13f))
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SingleLineInfo("Chào ${learner.name}!", Modifier.fillMaxWidth().padding(start = 5.dp, top = 8.dp, end = if (p == NeonStudio) 78.dp else 102.dp),
                fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(if (p == NeonStudio) 6.dp else 30.dp))
            Row(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(17.dp)).clip(RoundedCornerShape(17.dp))
                .background(p.card.copy(alpha = if (p == NeonStudio) 1f else .7f)).border(1.dp, p.border, RoundedCornerShape(17.dp)).padding(7.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.size(43.dp).background(p.peach, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    if (p == NeonStudio) Text("🔥", fontSize = 26.sp) else BrandFlame(Modifier.size(26.dp, 32.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SingleLineInfo("${state.streak} ngày", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        state.week.forEach { day ->
                            val checked = day.toString() in dates
                            Box(Modifier.size(9.dp).clip(CircleShape)
                                .background(if (checked) p.coral else if (day == today) Color.Transparent else p.border)
                                .then(if (day == today && !checked) Modifier.border(1.5.dp, p.coral, CircleShape) else Modifier)
                                .semantics { contentDescription = "$day: ${if (checked) "Đã điểm danh" else "Chưa điểm danh"}" })
                        }
                    }
                }
                Button(onClick = {
                    val day = LocalDate.now(); today = day
                    val latest = validCheckInDates(preferences.getStringSet(CHECK_IN_DATES, emptySet()).orEmpty())
                    if (day.toString() !in latest) {
                        val updated = DailyCheckIn(latest, day).checkIn()
                        error = !preferences.edit().putStringSet(CHECK_IN_DATES, updated).commit()
                        if (!error) dates = updated
                    }
                }, enabled = !state.checkedToday, shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (p == NeonStudio) p.coral else Color.Transparent,
                        contentColor = if (p == NeonStudio) p.background else Color.White,
                        disabledContainerColor = p.mint, disabledContentColor = p.green),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 10.dp),
                    modifier = Modifier.widthIn(min = 112.dp).heightIn(min = 45.dp)
                        .then(if (p != NeonStudio && !state.checkedToday) Modifier.background(BrandLook.selection, RoundedCornerShape(14.dp)) else Modifier)) {
                    if (state.checkedToday) Text("✓ Đã điểm danh", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    else {
                        Canvas(Modifier.size(12.dp, 16.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * .55f, 0f)
                                cubicTo(size.width * .1f, size.height * .4f, size.width * .65f, size.height * .4f, size.width * .3f, size.height * .63f)
                                lineTo(size.width * .16f, size.height * .43f)
                                cubicTo(-size.width * .2f, size.height, size.width * .9f, size.height * 1.2f, size.width * .9f, size.height * .7f)
                                cubicTo(size.width * .95f, size.height * .43f, size.width * .45f, size.height * .25f, size.width * .55f, 0f)
                                close()
                            }
                            drawPath(path, Color.White, style = Stroke(width = 1.4.dp.toPx()))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Điểm danh", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    }
                }
            }
        }
        // Both drawings share the same horizontal centre: 58dp (46dp for Neon) from the right edge.
        // The large watermark spans the button; only the learner avatar is opaque and in front.
        Box(Modifier.align(Alignment.TopEnd).padding(end = 18.dp, top = 23.dp)
            .size(if (p == NeonStudio) 56.dp else 80.dp).clickable(role = Role.Button, onClick = onProfile)
            .semantics { contentDescription = "Mở hồ sơ ${learner.name}" }) {
            LearnerAvatar(learner.avatar, Modifier.fillMaxSize())
        }
    }
    if (error) Text("Chưa lưu được điểm danh. Hãy thử lại nhé.", color = p.coral, fontSize = 12.sp)
}
