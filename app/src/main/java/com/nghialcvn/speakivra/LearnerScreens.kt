package com.nghialcvn.speakivra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

internal val LocalLearnerArtwork = staticCompositionLocalOf<ImageBitmap?> { null }

/** Equal 512px atlas cells, with safe margins around the full face and hair. */
@Composable
internal fun LearnerAvatar(avatar: Int, modifier: Modifier = Modifier, square: Boolean = false) {
    val artwork = LocalLearnerArtwork.current ?: ImageBitmap.imageResource(R.drawable.learner_avatars)
    val value = avatar.coerceIn(0, 5)
    val cell = artwork.width / 3
    val row = artwork.height / 2
    val side = minOf(cell, row)
    Canvas(modifier.clip(if (square) RoundedCornerShape(4.dp) else CircleShape)) {
        drawImage(artwork, srcOffset = IntOffset((value % 3) * cell + (cell - side) / 2, (value / 3) * row + (row - side) / 2), srcSize = IntSize(side, side),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()))
    }
}

@Composable
internal fun LearnerPicker(store: LearnerStore, learners: List<Learner>, initial: String?, onAdd: () -> Unit, onStart: (Learner) -> Unit) {
    val p = studio
    if (learners.isEmpty()) {
        // First install: no default named person, and no empty progress profile.
        Column(Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.weight(1f))
            Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra",
                Modifier.size(85.dp))
            Text("Ai học hôm nay?", fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold)
            Text("Lần đầu sử dụng Speakivra, hãy tạo hồ sơ để bắt đầu.",
                color = p.muted, fontSize = 15.sp, lineHeight = 22.sp)
            StudioCard {
                Text("Chưa có hồ sơ người học", fontSize = 17.sp,
                    fontWeight = FontWeight.Bold)
                Text("Tên và ảnh đại diện do bạn tự chọn. Tiến độ lưu riêng trên máy.",
                    color = p.muted, fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f))
            CoralButton("+ Tạo hồ sơ mới", onClick = onAdd)
        }
        return
    }
    var selected by rememberSaveable { mutableStateOf(initial ?: learners.first().id) }
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(painterResource(R.drawable.speakivra_icon), "Logo Speakivra", Modifier.align(Alignment.CenterHorizontally).size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text("Ai học hôm nay?", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Chọn hồ sơ để tiếp tục", color = p.muted, modifier = Modifier.align(Alignment.CenterHorizontally))
            learners.forEach { learner ->
                val chosen = learner.id == selected
                val (done, total) = store.learned(learner)
                Row(Modifier.fillMaxWidth().then(if (p == NeonStudio) Modifier.neonGlass(18, chosen)
                    else Modifier.clip(RoundedCornerShape(18.dp))
                        .background(if (chosen) p.peach.copy(alpha = .35f) else p.card)
                        .border(if (chosen) 1.5.dp else 1.dp, if (chosen) p.coral else p.border, RoundedCornerShape(18.dp)))
                    .clickable(role = Role.RadioButton) { selected = learner.id }.semantics { this.selected = chosen }
                    .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LearnerAvatar(learner.avatar, Modifier.size(64.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(learner.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        StudioProgress(done.toFloat() / total)
                        Text("Đã học $done/$total câu • ${done * 100 / total}%", fontSize = 12.sp, color = p.muted)
                        Text("Tiếp tục bài học ›", color = p.coral, fontSize = 14.sp)
                    }
                    if (chosen) StudioIcon("check", Modifier.size(24.dp), p.coral)
                }
            }
            OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp), shape = RoundedCornerShape(18.dp)) {
                Text("+", fontSize = 30.sp, color = p.coral); Spacer(Modifier.width(12.dp)); Text("Thêm người học", color = p.ink, fontSize = 17.sp)
            }
        }
        Text("Không cần mật khẩu • Tiến độ lưu riêng trên máy", color = p.muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        CoralButton("Bắt đầu học", onClick = { learners.firstOrNull { it.id == selected }?.let(onStart) })
    }
}

@Composable
internal fun CreateLearnerScreen(onBack: () -> Unit, initial: Learner? = null, onSave: (String, Int) -> String?) {
    val p = studio
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var avatar by rememberSaveable(initial?.id) { mutableIntStateOf(initial?.avatar ?: 1) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().imePadding().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            PageHeading(if (initial == null) "Tạo hồ sơ" else "Chỉnh sửa hồ sơ", onBack = onBack)
            Text("Tên người học", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            OutlinedTextField(value = name, onValueChange = { if (it.length <= 30 && it.none { c -> c.isISOControl() }) name = it; error = null },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); keyboard?.hide() }),
                singleLine = true, label = { Text("Tên người học") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Text("Chọn avatar", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            LearnerAvatar(avatar, Modifier.align(Alignment.CenterHorizontally).size(130.dp).border(2.dp, p.coral, CircleShape).padding(6.dp))
            repeat(2) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    repeat(3) { col ->
                        val value = row * 3 + col
                        Box(Modifier.size(88.dp).clip(CircleShape).border(if (value == avatar) 2.dp else 1.dp,
                            if (value == avatar) p.coral else p.border, CircleShape).clickable(role = Role.RadioButton) { avatar = value }
                            .semantics { contentDescription = "Avatar ${value + 1}"; selected = value == avatar }.padding(6.dp)) {
                            LearnerAvatar(value, Modifier.fillMaxSize())
                            if (value == avatar) StudioIcon("check", Modifier.size(21.dp).align(Alignment.TopEnd), p.coral)
                        }
                    }
                }
            }
            Text("Mỗi hồ sơ có tiến độ riêng", color = p.muted, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        }
        CoralButton(if (initial == null) "Lưu và bắt đầu" else "Lưu thay đổi", enabled = name.isNotBlank(), onClick = { focus.clearFocus(); keyboard?.hide(); error = onSave(name, avatar) })
    }
}
