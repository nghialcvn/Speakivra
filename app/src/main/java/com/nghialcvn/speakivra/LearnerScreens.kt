package com.nghialcvn.speakivra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/** Draw the six exact approved faces from the demo artwork, rather than redesigning them. */
@Composable
internal fun LearnerAvatar(avatar: Int, modifier: Modifier = Modifier) {
    val artwork = ImageBitmap.imageResource(R.drawable.approved_profiles_demo)
    val x = listOf(480, 585, 690)[avatar.coerceIn(0, 5) % 3]
    val y = if (avatar < 3) 575 else 700
    Canvas(modifier.clip(CircleShape)) {
        drawImage(artwork, srcOffset = IntOffset(x - 43, y - 43), srcSize = IntSize(86, 86),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()))
    }
}

@Composable
internal fun LearnerPicker(store: LearnerStore, learners: List<Learner>, initial: String?, onAdd: () -> Unit, onStart: (Learner) -> Unit) {
    val p = studio
    var selected by rememberSaveable { mutableStateOf(initial ?: learners.first().id) }
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Speakivra", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(8.dp))
            Text("Ai học hôm nay?", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Chọn hồ sơ để tiếp tục", color = p.muted, modifier = Modifier.align(Alignment.CenterHorizontally))
            learners.forEach { learner ->
                val chosen = learner.id == selected
                val (done, total) = store.learned(learner)
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (chosen) p.peach.copy(alpha = .35f) else p.card)
                    .border(if (chosen) 1.5.dp else 1.dp, if (chosen) p.coral else p.border, RoundedCornerShape(18.dp))
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
internal fun CreateLearnerScreen(onBack: () -> Unit, onSave: (String, Int) -> String?) {
    val p = studio
    var name by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableIntStateOf(1) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().imePadding().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            PageHeading("Tạo hồ sơ", onBack = onBack)
            Text("Tên người học", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            OutlinedTextField(value = name, onValueChange = { if (it.length <= 30 && it.none { c -> c.isISOControl() }) name = it; error = null },
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
        CoralButton("Lưu và bắt đầu", enabled = name.isNotBlank(), onClick = { error = onSave(name, avatar) })
    }
}
