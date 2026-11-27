package com.nghialcvn.speakivra

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.time.LocalDate
import kotlinx.coroutines.delay

@Composable
internal fun DailyCheckInCard(learner: Learner, preferences: SharedPreferences, onProfile: () -> Unit) {
    if (studio == NeonStudio) {
        LegacyDailyCheckInCard(learner, preferences, onProfile)
        return
    }
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
    val onCheckIn: () -> Unit = {
        val day = LocalDate.now(); today = day
        val latest = validCheckInDates(preferences.getStringSet(CHECK_IN_DATES, emptySet()).orEmpty())
        if (day.toString() !in latest) {
            val updated = DailyCheckIn(latest, day).checkIn()
            error = !preferences.edit().putStringSet(CHECK_IN_DATES, updated).commit()
            if (!error) dates = updated
        }
    }
    val strip = if (p == LightStudio) Color(0xFFE6E8EF) else Color(0xFF29324A)
    val shape = RoundedCornerShape(18.dp)
    // The figure's virtual ledge is at 91% of its 76dp sprite; hands overlap the strip.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val accessible = maxWidth < 330.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
        val stripHeight = if (accessible) 106.dp else 64.dp
        val cardHeight = stripHeight + 124.dp
        Box(Modifier.fillMaxWidth().height(cardHeight).shadow(4.dp, shape).clip(shape)
            .background(brandPanelBrush(p)).border(1.dp, p.border, shape)
            .semantics { contentDescription = "Điểm danh hằng ngày" }) {
            Image(painterResource(R.drawable.brand_logo_clear), null,
                Modifier.align(Alignment.TopEnd).offset(x = 52.dp, y = (-15).dp).size(220.dp)
                    .alpha(if (p == LightStudio) .28f else .34f).testTag("checkin-watermark"))
            SingleLineInfo("Chào ${learner.name}!", Modifier.fillMaxWidth().padding(start = 16.dp, top = 18.dp, end = 112.dp),
                fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            val button: @Composable () -> Unit = {
                Button(onClick = onCheckIn, enabled = !state.checkedToday,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White,
                        disabledContainerColor = Color(0xFF0BA757), disabledContentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 8.dp),
                    modifier = Modifier.widthIn(min = 112.dp).height(44.dp)
                        .then(if (!state.checkedToday) Modifier.background(BrandLook.action, RoundedCornerShape(14.dp)) else Modifier)) {
                    Text(if (state.checkedToday) "✓ Đã điểm danh" else "Điểm danh", fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold, maxLines = 1)
                }
            }
            val counter: @Composable RowScope.() -> Unit = {
                BrandFlame(Modifier.size(29.dp, 37.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SingleLineInfo("${state.streak} ngày", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        state.week.forEach { day -> CheckInBead(day, day.toString() in dates, day == today) }
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 10.dp, vertical = 10.dp)
                .fillMaxWidth().height(stripHeight).shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)).background(strip).testTag("checkin-gray-strip")
                .border(1.dp, p.border, RoundedCornerShape(16.dp)).padding(8.dp)) {
                if (accessible) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp), content = counter)
                    Box(Modifier.align(Alignment.End)) { button() }
                } else Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) { counter(); button() }
            }
            // Both sprites share the same horizontal centre, 58dp from the right edge.
            Box(Modifier.align(Alignment.TopEnd).offset(y = (114f - 76f * .91f).dp)
                .padding(end = 20.dp).size(76.dp).testTag("checkin-gripping-avatar")
                .clickable(role = Role.Button, onClick = onProfile)
                .semantics { contentDescription = "Mở hồ sơ ${learner.name}" }) {
                GrippingLearnerAvatar(learner.avatar, Modifier.fillMaxSize())
            }
        }
    }
    if (error) Text("Chưa lưu được điểm danh. Hãy thử lại nhé.", color = p.incorrectWord, fontSize = 12.sp)
}

@Composable
private fun CheckInBead(day: LocalDate, checked: Boolean, today: Boolean) {
    val p = studio
    val violet = if (p == LightStudio) Color(0xFF7838EE) else Color(0xFFB494FF)
    val gray = if (p == LightStudio) Color(0xFFBBC0CF) else Color(0xFF68718A)
    Canvas(Modifier.size(10.dp).semantics { contentDescription = "$day: ${if (checked) "Đã điểm danh" else "Chưa điểm danh"}" }) {
        val radius = size.minDimension / 2
        if (today && !checked) {
            drawCircle(violet, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
        } else {
            val colour = if (checked) violet else gray
            drawCircle(Brush.radialGradient(listOf(colour.copy(alpha = .62f), colour),
                center = Offset(size.width * .28f, size.height * .2f), radius = size.width))
        }
        if (today && checked) {
            val path = Path().apply { moveTo(size.width * .23f, size.height * .51f); lineTo(size.width * .43f, size.height * .7f); lineTo(size.width * .77f, size.height * .31f) }
            drawPath(path, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
internal fun GrippingLearnerAvatar(avatar: Int, modifier: Modifier = Modifier) {
    val image = ImageBitmap.imageResource(R.drawable.checkin_grip_avatars)
    val col = image.width / 3; val row = image.height / 2
    val index = avatar.coerceIn(0, 5)
    Canvas(modifier) {
        drawImage(image, srcOffset = IntOffset(index % 3 * col, index / 3 * row), srcSize = IntSize(col, row),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
    }
}
