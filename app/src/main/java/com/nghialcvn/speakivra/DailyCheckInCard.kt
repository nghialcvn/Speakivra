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
    val strip = if (p == LightStudio) Color(0xFFF8F9FC) else Color(0xFF29324A)
    val shape = RoundedCornerShape(18.dp)
    // Keep the approved three blocks on one row at every supported phone width.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val scale = (maxWidth.value / 330f).coerceIn(.50f, 1.25f)
        val density = androidx.compose.ui.platform.LocalDensity.current
        fun fitText(dp: Float) = with(density) { (dp * scale).dp.toSp() }
        val blockHeight = maxOf(48.dp, (46f * scale).dp)
        val stripHeight = blockHeight + (12f * scale).dp
        val headerHeight = (92f * scale).dp
        val cardHeight = headerHeight + stripHeight + (10f * scale).dp
        val avatarSize = (64f * scale).dp
        val avatarEnd = (18f * scale).dp
        val watermarkWidth = (164f * scale).dp
        val watermarkHeight = watermarkWidth * (518f / 668f)
        val logo = ImageBitmap.imageResource(R.drawable.brand_logo_clear)
        Box(Modifier.fillMaxWidth().height(cardHeight).testTag("checkin-compact-card")
            .shadow(4.dp, shape).clip(shape).background(brandPanelBrush(p)).border(1.dp, p.border, shape)
            .semantics { contentDescription = "Điểm danh hằng ngày" }) {
            // Crop the transparent margins of the existing official logo, as in the picker.
            Canvas(Modifier.align(Alignment.TopEnd)
                .offset(x = (watermarkWidth - avatarSize) / 2 - avatarEnd, y = (3f * scale).dp)
                .size(watermarkWidth, watermarkHeight)
                .alpha(if (p == LightStudio) .27f else .34f).testTag("checkin-watermark")) {
                drawImage(logo, srcOffset = IntOffset(50, 36), srcSize = IntSize(668, 518),
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
            }
            SingleLineInfo("Chào ${learner.name}!", Modifier.fillMaxWidth().padding(
                start = (16f * scale).dp, top = (16f * scale).dp, end = (100f * scale).dp),
                fontSize = fitText(26f), fontWeight = FontWeight.ExtraBold)
            Row(Modifier.align(Alignment.BottomCenter)
                .padding(start = (8f * scale).dp, end = (8f * scale).dp, bottom = (10f * scale).dp)
                .fillMaxWidth().height(stripHeight).testTag("checkin-gray-strip")
                .shadow(2.dp, RoundedCornerShape(15.dp)).clip(RoundedCornerShape(15.dp))
                .background(strip).border(1.dp, p.border, RoundedCornerShape(15.dp))
                .padding((6f * scale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((7f * scale).dp)) {
                Box(Modifier.width((44f * scale).dp).height(blockHeight)
                    .testTag("checkin-flame").background(p.peach, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center) {
                    BrandFlame(Modifier.size((28f * scale).dp, (36f * scale).dp))
                }
                Column(Modifier.weight(1f).height(blockHeight).testTag("checkin-counter")
                    .padding(vertical = (2f * scale).dp),
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Text("${state.streak} ngày", fontSize = fitText(21f), lineHeight = fitText(26f),
                        color = p.ink, fontWeight = FontWeight.ExtraBold, maxLines = 1, softWrap = false)
                    Row(Modifier.testTag("checkin-week"), horizontalArrangement = Arrangement.spacedBy((4f * scale).dp)) {
                        state.week.forEach { day ->
                            CheckInBead(day, day.toString() in dates, day == today, (10f * scale).dp)
                        }
                    }
                }
                Button(onClick = onCheckIn, enabled = !state.checkedToday,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp, disabledElevation = 2.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = .24f)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White,
                        disabledContainerColor = Color(0xFF0BA757), disabledContentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = (5f * scale).dp),
                    modifier = Modifier.size((128f * scale).dp, blockHeight)
                        .testTag("checkin-action")
                        .then(if (!state.checkedToday) Modifier.background(BrandLook.action, RoundedCornerShape(14.dp)) else Modifier)) {
                    Text(if (state.checkedToday) "✓ Đã điểm danh" else "Điểm danh",
                        fontSize = fitText(13f), lineHeight = fitText(16f), fontWeight = FontWeight.ExtraBold,
                        maxLines = 1, softWrap = false)
                }
            }
            // The existing cutout's hands sit at 91% of its height.
            Box(Modifier.align(Alignment.TopEnd)
                .offset(x = -avatarEnd, y = headerHeight - avatarSize * .91f)
                .size(avatarSize).testTag("checkin-gripping-avatar")
                .clickable(role = Role.Button, onClick = onProfile)
                .semantics { contentDescription = "Mở hồ sơ ${learner.name}" }) {
                GrippingLearnerAvatar(learner.avatar, Modifier.fillMaxSize())
            }
        }
    }
    if (error) Text("Chưa lưu được điểm danh. Hãy thử lại nhé.", color = p.incorrectWord, fontSize = 12.sp)
}

@Composable
private fun CheckInBead(day: LocalDate, checked: Boolean, today: Boolean, diameter: Dp) {
    val p = studio
    val violet = if (p == LightStudio) Color(0xFF7838EE) else Color(0xFFB494FF)
    val gray = if (p == LightStudio) Color(0xFFBBC0CF) else Color(0xFF68718A)
    Canvas(Modifier.size(diameter).semantics { contentDescription = "$day: ${if (checked) "Đã điểm danh" else "Chưa điểm danh"}" }) {
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
