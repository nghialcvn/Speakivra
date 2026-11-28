package com.nghialcvn.speakivra

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

internal fun selectMandarinGender(preferences: SharedPreferences, gender: String): String? {
    require(gender in listOf("female", "male"))
    val edit = preferences.edit()
    if (!preferences.contains("zh_voice_gender")) preferences.getString("zh_voice_id", null)?.let { edit.putString("zh_voice_female", it) }
    val voice = preferences.getString("zh_voice_$gender", if (gender == "female" && !preferences.contains("zh_voice_gender")) preferences.getString("zh_voice_id", null) else null)
    edit.putString("zh_voice_gender", gender)
    if (voice == null) edit.remove("zh_voice_id") else edit.putString("zh_voice_id", voice)
    edit.apply()
    return voice
}

@Composable
internal fun ChineseVoiceSettings(preferences: SharedPreferences, onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }
    val playback = remember { SpeechPlayback(context.applicationContext) { status = it } }
    var speed by remember { mutableIntStateOf(preferences.getInt("zh_playback_speed", 75)) }
    var gender by remember { mutableStateOf(preferences.getString("zh_voice_gender", "female") ?: "female") }
    var selected by remember { mutableStateOf(preferences.getString("zh_voice_id", null)) }
    DisposableEffect(playback) { onDispose { playback.close() } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading("Giọng đọc & Ngôn ngữ", "Tiếng Trung phổ thông • Trung Quốc", onBack)
        StudioCard {
            Text("Trung – Việt • Chữ giản thể • Pinyin")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(75, 100, 125).forEach { value -> FilterChip(speed == value, onClick = {
                    playback.stop(); speed = value; preferences.edit().putInt("zh_playback_speed", value).apply()
                }, label = { Text(speedLabel(value)) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("female" to "Giọng nữ", "male" to "Giọng nam").forEach { (value, label) ->
                    FilterChip(gender == value, { playback.stop(); gender = value; selected = selectMandarinGender(preferences, value) }, label = { Text(label) })
                }
            }
            Text("Nghe thử rồi gán giọng ngoại tuyến cho lựa chọn nam/nữ. Android không đảm bảo nhãn nam/nữ của giọng hệ thống.")
            TextButton(onClick = { selected = null; preferences.edit().remove("zh_voice_id").remove("zh_voice_$gender").apply() }) { Text("Tự chọn giọng phổ thông Trung Quốc") }
        }
        val voices = playback.mandarinVoices()
        if (voices.isEmpty()) MandarinVoiceInstall()
        voices.forEachIndexed { n, voice -> StudioCard {
            Text("Giọng ${n + 1}" + if (selected == voice.id) " • Đang chọn" else "")
            Row {
                TextButton(onClick = { playback.speakMandarin("你好，欢迎来到Speakivra。", speed, voice.id) }) { Text("Nghe thử") }
                TextButton(onClick = { playback.stop(); selected = voice.id; preferences.edit().putString("zh_voice_id", voice.id).putString("zh_voice_$gender", voice.id).putString("zh_voice_gender", gender).apply() }) { Text("Chọn giọng này") }
            }
        } }
        Text(status, color = studio.muted)
    }
}

@Composable
internal fun MandarinVoiceInstall() {
    val context = LocalContext.current
    Text("Nếu chưa nghe được mẫu, hãy tải giọng tiếng Trung (Trung Quốc) trong cài đặt giọng nói của máy.")
    TextButton(onClick = {
        val install = android.content.Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        if (runCatching { context.startActivity(install) }.isFailure) {
            runCatching { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS)) }
        }
    }) { Text("Cài dữ liệu giọng đọc") }
}
