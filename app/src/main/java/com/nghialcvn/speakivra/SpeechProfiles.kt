package com.nghialcvn.speakivra

internal data class SpeechPart(val text: String, val pitch: Float = 1f, val pace: Float = 1f, val pauseAfterMs: Long = 0)
internal data class SpeechProfile(val text: String, val clip: String?, val parts: List<SpeechPart>)

/** Complete clauses preserve word linking; only sentence boundaries receive extra pauses. */
internal object SpeechProfiles {
    val catalog = listOf(
        SpeechProfile("Hello! How are you?", "voice_greeting", listOf(SpeechPart("Hello!", 1.04f, 1.01f, 140), SpeechPart("How are you?", 1.02f, 0.98f))),
        SpeechProfile("I'm fine, thank you.", "voice_thanks", listOf(SpeechPart("I'm fine, thank you.", 1.01f, 0.98f))),
        SpeechProfile("My name is Alex.", "voice_introduction", listOf(SpeechPart("My name is Alex.", 0.99f, 1f))),
        SpeechProfile("Nice to meet you!", "voice_welcome", listOf(SpeechPart("Nice to meet you!", 1.04f, 1.01f))),
        SpeechProfile("Hello! What's your name?", "voice_ask_name", listOf(SpeechPart("Hello!", 1.04f, 1.01f, 160), SpeechPart("What's your name?", 1.02f, 0.99f))),
        SpeechProfile("Nice to meet you! Where are you from?", "voice_ask_origin", listOf(SpeechPart("Nice to meet you!", 1.04f, 1.01f, 180), SpeechPart("Where are you from?", 1.02f, 0.99f))),
        SpeechProfile("What do you like doing?", "voice_ask_hobbies", listOf(SpeechPart("What do you like doing?", 1.02f, 0.98f)))
    )
    fun forText(text: String): SpeechProfile = catalog.firstOrNull { it.text == text }
        ?: SpeechProfile(text, null, listOf(when {
            text.trimEnd().endsWith("?") -> SpeechPart(text, 1.02f, 0.99f)
            text.trimEnd().endsWith("!") -> SpeechPart(text, 1.04f, 1.01f)
            else -> SpeechPart(text)
        }))
    fun playbackRate(speed: Int): Float = when (speed) { 75 -> 0.75f; 125 -> 1.25f; else -> 1f }
    fun fallbackRate(speed: Int, part: SpeechPart): Float = (playbackRate(speed) / 0.75f * part.pace).coerceIn(0.7f, 2f)
}
