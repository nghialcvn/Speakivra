package com.nghialcvn.speakivra

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.util.Locale

internal object IpaText {
    @Volatile private var data: JSONObject? = null
    @Volatile private var expandedData: JSONObject? = null
    @Volatile private var hotelData: JSONObject? = null
    fun key(text: String) = text.trim().replace('’', '\'').replace('‘', '\'')
        .replace(Regex("\\s+"), " ").lowercase(Locale.US)
    fun lookup(context: Context, text: String, accent: String): String? {
        val source = data ?: synchronized(this) {
            data ?: JSONObject(context.assets.open("ipa.json").bufferedReader().use { it.readText() }).also { data = it }
        }
        val language = source.getJSONObject(if (accent == "GB") "GB" else "US")
        val value = language.optString(key(text), "")
        if (value.isNotBlank()) return "/$value/"
        val hotel = hotelData ?: synchronized(this) {
            hotelData ?: JSONObject(context.assets.open("hotel-ipa.json").bufferedReader().use { it.readText() }).also { hotelData = it }
        }
        val hotelValue = hotel.getJSONObject(if (accent == "GB") "GB" else "US").optString(key(text), "")
        if (hotelValue.isNotBlank()) return "/$hotelValue/"
        val expanded = expandedData ?: synchronized(this) {
            expandedData ?: JSONObject(context.assets.open("expanded-ipa.json").bufferedReader().use { it.readText() }).also { expandedData = it }
        }
        val expandedValue = expanded.getJSONObject(if (accent == "GB") "GB" else "US").optString(key(text), "")
        if (expandedValue.isNotBlank()) return "/$expandedValue/"
        // Personal names remain in their own spelling outside IPA brackets, never a fabricated English pronunciation.
        val name = Regex("(?i)^(?:I'm|My name is) (.+?)[.!?]?$").matchEntire(text.trim())?.groupValues?.get(1)
        val prefix = if (text.startsWith("My name is", ignoreCase = true)) "My name is" else "I'm"
        val prefixIpa = language.optString(key(prefix)).ifBlank { if (prefix == "My name is") "maɪ neɪm ɪz" else "aɪm" }
        return if (name != null) "/$prefixIpa/ $name" else null
    }
}
// A packaged italic face keeps IPA independent of Samsung's configurable system serif.
internal val ApprovedIpaFont = FontFamily(Font(R.font.ipa_serif_italic,
    weight = FontWeight.Normal, style = FontStyle.Italic))

@Composable
internal fun IpaLine(english: String, accent: String = "US", modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val ipa = remember(english, accent) { IpaText.lookup(context, english, accent) } ?: return
    Text(ipa, modifier = modifier.semantics { contentDescription = "Phiên âm IPA" },
        fontSize = 14.sp, lineHeight = 19.sp, fontFamily = ApprovedIpaFont, fontWeight = FontWeight.Normal, letterSpacing = 0.sp,
        style = TextStyle(fontSynthesis = FontSynthesis.None),
        fontStyle = FontStyle.Italic, color = if (studio == NeonStudio) NeonLook.cyan.copy(alpha = .85f) else studio.muted,
        textAlign = TextAlign.Center)
}
