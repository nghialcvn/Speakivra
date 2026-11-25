package com.nghialcvn.speakivra

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.util.Locale

internal object IpaText {
    @Volatile private var data: JSONObject? = null
    fun key(text: String) = text.trim().replace('’', '\'').replace('‘', '\'')
        .replace(Regex("\\s+"), " ").lowercase(Locale.US)
    fun lookup(context: Context, text: String, accent: String): String? {
        val source = data ?: synchronized(this) {
            data ?: JSONObject(context.assets.open("ipa.json").bufferedReader().use { it.readText() }).also { data = it }
        }
        val language = source.getJSONObject(if (accent == "GB") "GB" else "US")
        val value = language.optString(key(text), "")
        if (value.isNotBlank()) return "/$value/"
        // Personal names remain in their own spelling outside IPA brackets, never a fabricated English pronunciation.
        val name = Regex("(?i)^I'm (.+?)[.!?]?$").matchEntire(text.trim())?.groupValues?.get(1)
        return if (name != null) "/${language.getString(key("I'm"))}/ $name" else null
    }
}
@Composable
internal fun IpaLine(english: String, accent: String = "US", modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val ipa = remember(english, accent) { IpaText.lookup(context, english, accent) } ?: return
    Text(ipa, modifier = modifier.semantics { contentDescription = "Phiên âm IPA" },
        fontSize = 14.sp, lineHeight = 19.sp, fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic, color = if (studio == NeonStudio) NeonLook.cyan.copy(alpha = .85f) else studio.muted,
        textAlign = TextAlign.Center)
}
