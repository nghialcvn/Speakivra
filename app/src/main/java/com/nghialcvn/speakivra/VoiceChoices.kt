package com.nghialcvn.speakivra

import android.content.SharedPreferences

/** Speaking accent, not translation language. A male/female label is user-confirmed,
 * because Android's Voice API has no reliable gender field.
 */
internal data class VoiceChoice(
    val accent: String = "US",
    val gender: String = "female",
    val voiceId: String? = null
) {
    val languageTag: String get() = "en-US"
    val accentLabel: String get() = "Anh-Mỹ (US)"
    val genderLabel: String get() = if (gender == "male") "Giọng nam" else "Giọng nữ"
    val usesBundledAudio: Boolean get() = accent == "US" && gender == "female" && voiceId == null
}

internal data class InstalledSpeechVoice(val id: String, val accent: String, val quality: Int)

/** Preferences use the existing per-learner preference file; do not change progress keys. */
internal object VoiceChoices {
    private const val ACCENT = "speech_accent"
    private const val GENDER = "speech_gender"
    private fun safeAccent(@Suppress("UNUSED_PARAMETER") value: String?): String = "US"
    private fun safeGender(value: String?): String = if (value == "male") "male" else "female"
    private fun voiceKey(accent: String, gender: String) = "speech_voice_${accent}_${gender}"

    fun read(prefs: SharedPreferences): VoiceChoice {
        val accent = safeAccent(prefs.getString(ACCENT, "US"))
        val gender = safeGender(prefs.getString(GENDER, "female"))
        val name = prefs.getString(voiceKey(accent, gender), null)
            ?.takeIf { it.isNotBlank() && it.length <= 256 && it.none(Char::isISOControl) }
        return VoiceChoice(accent, gender, name)
    }

    /** Change only active selection, without erasing any previously assigned system voice. */
    fun select(prefs: SharedPreferences, accent: String, gender: String): VoiceChoice {
        require(accent == "US" && gender in listOf("male", "female"))
        check(prefs.edit().putString(ACCENT, accent).putString(GENDER, gender).commit())
        return read(prefs)
    }

    /** The learner listens and explicitly assigns an installed voice to this label. */
    fun assign(prefs: SharedPreferences, accent: String, gender: String, voiceId: String): VoiceChoice {
        require(accent == "US" && gender in listOf("male", "female"))
        require(voiceId.isNotBlank() && voiceId.length <= 256 && voiceId.none(Char::isISOControl))
        check(prefs.edit().putString(voiceKey(accent, gender), voiceId)
            .putString(ACCENT, accent).putString(GENDER, gender).commit())
        return read(prefs)
    }

    /** The original US female neural clips stay the default and can be restored. */
    fun resetBundled(prefs: SharedPreferences): VoiceChoice {
        check(prefs.edit().remove(voiceKey("US", "female"))
            .putString(ACCENT, "US").putString(GENDER, "female").commit())
        return read(prefs)
    }
}
