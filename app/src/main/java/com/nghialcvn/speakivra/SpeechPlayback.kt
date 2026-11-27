package com.nghialcvn.speakivra

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

/** Bundled neural clips first; installed US-English TTS is an offline fallback. */
internal class SpeechPlayback(context: Context, private val onStatus: (String) -> Unit) {
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener({ change ->
            if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop(true)
        }, handler).build()
    private var hasFocus = false
    private var player: MediaPlayer? = null
    private var engine: TextToSpeech? = null
    private var initialized = false
    private var fallbackAvailable = false
    private var closed = false
    private var session = 0L
    private var lastUtterance: String? = null
    private data class Request(val profile: SpeechProfile, val speed: Int, val session: Long, val choice: VoiceChoice, val repeats: Int = 1, val language: String = "en-US")
    private var pending: Request? = null
    private var defaultVoice: Voice? = null
    private var remainingAudioRepeats = 0

    /** User confirms gender by listening; Android TTS does not report voice gender. */
    fun installedVoices(accent: String): List<InstalledSpeechVoice> {
        if (!initialized || closed) return emptyList()
        return engine?.voices.orEmpty()
            .filter { it.locale.language == "en" && it.locale.country == accent &&
                !it.isNetworkConnectionRequired &&
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
            .sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.latency }.thenBy { it.name })
            .map { InstalledSpeechVoice(it.name, accent, it.quality) }
    }

    fun mandarinVoices(): List<InstalledSpeechVoice> = engine?.voices.orEmpty()
        .filter { it.locale.language == "zh" && it.locale.country == "CN" &&
            !it.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
        .sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.latency }.thenBy { it.name })
        .map { InstalledSpeechVoice(it.name, "CN", it.quality) }

    fun speakMandarin(text: String, speed: Int, voiceId: String? = null) {
        if (closed || text.isBlank()) return
        stop()
        hasFocus = runCatching { audio.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED }.getOrDefault(false)
        if (!hasFocus) { status("Chưa thể phát âm thanh lúc này. Hãy thử nghe lại."); return }
        fallback(Request(SpeechProfile(text, null, listOf(SpeechPart(text, 1f, 1f, 0))),
            speed, session, VoiceChoice(voiceId = voiceId), language = "zh-CN"))
    }


    init {
        engine = TextToSpeech(app) { result -> handler.post { initialize(result) } }
    }
    private fun status(message: String, token: Long = session) {
        handler.post { if (!closed && session == token) onStatus(message) }
    }
    private fun initialize(result: Int) {
        if (closed) return
        initialized = true
        val tts = engine ?: return
        if (result != TextToSpeech.SUCCESS) {
            val waiting = pending; pending = null
            if (waiting != null && waiting.session == session) fallback(waiting)
            return
        }
        fallbackAvailable = runCatching {
            if (tts.setLanguage(Locale.US) < TextToSpeech.LANG_AVAILABLE) return@runCatching false
            val candidates = tts.voices.orEmpty().filter { voice ->
                voice.locale.language == "en" && voice.locale.country == "US" && !voice.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
            }.sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.latency }.thenBy { it.name })
            defaultVoice = candidates.firstOrNull() ?: tts.voice?.takeIf { voice ->
                voice.locale.language == "en" && voice.locale.country == "US" && !voice.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
            }
            defaultVoice != null && tts.setVoice(defaultVoice) == TextToSpeech.SUCCESS
        }.getOrDefault(false)
        tts.setAudioAttributes(attributes)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                handler.post {
                    if (!closed && utteranceId?.startsWith("speech-$session-") == true) status("Đang phát giọng dự phòng…")
                }
            }
            override fun onDone(utteranceId: String?) {
                handler.post {
                    if (!closed && utteranceId != null && utteranceId == lastUtterance) {
                        lastUtterance = null; releaseFocus(); status("Đã phát xong. Nhấn Nói để luyện tập.")
                    }
                }
            }
            @Deprecated("Required by the Android TTS listener")
            override fun onError(utteranceId: String?) = ttsError(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = ttsError(utteranceId)
        })
        val waiting = pending
        pending = null
        if (waiting != null && waiting.session == session) fallback(waiting)
    }
    private fun ttsError(id: String?) {
        handler.post {
            if (!closed && id?.startsWith("speech-$session-") == true) {
                engine?.stop(); lastUtterance = null; releaseFocus()
                status("Không phát được giọng đã chọn. Kiểm tra dữ liệu TTS ngoại tuyến.")
            }
        }
    }
    fun speak(text: String, speed: Int, choice: VoiceChoice = VoiceChoice(), repeatCount: Int = 1) {
        if (closed || text.isBlank()) return
        stop()
        val token = session
        hasFocus = runCatching { audio.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED }.getOrDefault(false)
        if (!hasFocus) { status("Chưa thể phát âm thanh lúc này. Hãy thử nghe lại."); return }
        val profile = SpeechProfiles.forText(text)
        val count = repeatCount.coerceIn(1, 3)
        remainingAudioRepeats = count - 1
        if (!choice.usesBundledAudio) { fallback(Request(profile, speed, token, choice, count)); return }
        val resource = when (profile.clip) {
            "voice_greeting" -> R.raw.voice_greeting
            "voice_thanks" -> R.raw.voice_thanks
            "voice_introduction" -> R.raw.voice_introduction
            "voice_welcome" -> R.raw.voice_welcome
            "voice_ask_name" -> R.raw.voice_ask_name
            "voice_ask_origin" -> R.raw.voice_ask_origin
            "voice_ask_hobbies" -> R.raw.voice_ask_hobbies
            "voice_greetings_8" -> R.raw.voice_greetings_8
            "voice_greetings_9" -> R.raw.voice_greetings_9
            "voice_greetings_10" -> R.raw.voice_greetings_10
            "voice_greetings_11" -> R.raw.voice_greetings_11
            "voice_greetings_12" -> R.raw.voice_greetings_12
            "voice_family_13" -> R.raw.voice_family_13
            "voice_family_14" -> R.raw.voice_family_14
            "voice_family_15" -> R.raw.voice_family_15
            "voice_family_16" -> R.raw.voice_family_16
            "voice_family_17" -> R.raw.voice_family_17
            "voice_family_18" -> R.raw.voice_family_18
            "voice_family_19" -> R.raw.voice_family_19
            "voice_family_20" -> R.raw.voice_family_20
            "voice_family_21" -> R.raw.voice_family_21
            "voice_family_22" -> R.raw.voice_family_22
            "voice_family_23" -> R.raw.voice_family_23
            "voice_routine_24" -> R.raw.voice_routine_24
            "voice_routine_25" -> R.raw.voice_routine_25
            "voice_routine_26" -> R.raw.voice_routine_26
            "voice_routine_27" -> R.raw.voice_routine_27
            "voice_routine_28" -> R.raw.voice_routine_28
            "voice_routine_29" -> R.raw.voice_routine_29
            "voice_routine_30" -> R.raw.voice_routine_30
            "voice_routine_31" -> R.raw.voice_routine_31
            "voice_routine_32" -> R.raw.voice_routine_32
            "voice_routine_33" -> R.raw.voice_routine_33
            "voice_routine_34" -> R.raw.voice_routine_34
            "voice_food_35" -> R.raw.voice_food_35
            "voice_food_36" -> R.raw.voice_food_36
            "voice_food_37" -> R.raw.voice_food_37
            "voice_food_38" -> R.raw.voice_food_38
            "voice_food_39" -> R.raw.voice_food_39
            "voice_food_40" -> R.raw.voice_food_40
            "voice_food_41" -> R.raw.voice_food_41
            "voice_food_42" -> R.raw.voice_food_42
            "voice_food_43" -> R.raw.voice_food_43
            "voice_food_44" -> R.raw.voice_food_44
            "voice_food_45" -> R.raw.voice_food_45
            "voice_shopping_46" -> R.raw.voice_shopping_46
            "voice_shopping_47" -> R.raw.voice_shopping_47
            "voice_shopping_48" -> R.raw.voice_shopping_48
            "voice_shopping_49" -> R.raw.voice_shopping_49
            "voice_shopping_50" -> R.raw.voice_shopping_50
            "voice_shopping_51" -> R.raw.voice_shopping_51
            "voice_shopping_52" -> R.raw.voice_shopping_52
            "voice_shopping_53" -> R.raw.voice_shopping_53
            "voice_shopping_54" -> R.raw.voice_shopping_54
            "voice_shopping_55" -> R.raw.voice_shopping_55
            "voice_shopping_56" -> R.raw.voice_shopping_56
            "voice_directions_57" -> R.raw.voice_directions_57
            "voice_directions_58" -> R.raw.voice_directions_58
            "voice_directions_59" -> R.raw.voice_directions_59
            "voice_directions_60" -> R.raw.voice_directions_60
            "voice_directions_61" -> R.raw.voice_directions_61
            "voice_directions_62" -> R.raw.voice_directions_62
            "voice_directions_63" -> R.raw.voice_directions_63
            "voice_directions_64" -> R.raw.voice_directions_64
            "voice_directions_65" -> R.raw.voice_directions_65
            "voice_directions_66" -> R.raw.voice_directions_66
            "voice_directions_67" -> R.raw.voice_directions_67
            "voice_hobbies_68" -> R.raw.voice_hobbies_68
            "voice_hobbies_69" -> R.raw.voice_hobbies_69
            "voice_hobbies_70" -> R.raw.voice_hobbies_70
            "voice_hobbies_71" -> R.raw.voice_hobbies_71
            "voice_hobbies_72" -> R.raw.voice_hobbies_72
            "voice_hobbies_73" -> R.raw.voice_hobbies_73
            "voice_hobbies_74" -> R.raw.voice_hobbies_74
            "voice_hobbies_75" -> R.raw.voice_hobbies_75
            "voice_hobbies_76" -> R.raw.voice_hobbies_76
            "voice_hobbies_77" -> R.raw.voice_hobbies_77
            "voice_time_78" -> R.raw.voice_time_78
            "voice_time_79" -> R.raw.voice_time_79
            "voice_time_80" -> R.raw.voice_time_80
            "voice_time_81" -> R.raw.voice_time_81
            "voice_time_82" -> R.raw.voice_time_82
            "voice_time_83" -> R.raw.voice_time_83
            "voice_time_84" -> R.raw.voice_time_84
            "voice_time_85" -> R.raw.voice_time_85
            "voice_time_86" -> R.raw.voice_time_86
            "voice_time_87" -> R.raw.voice_time_87
            "voice_time_88" -> R.raw.voice_time_88
            "voice_travel_89" -> R.raw.voice_travel_89
            "voice_travel_90" -> R.raw.voice_travel_90
            "voice_travel_91" -> R.raw.voice_travel_91
            "voice_travel_92" -> R.raw.voice_travel_92
            "voice_travel_93" -> R.raw.voice_travel_93
            "voice_travel_94" -> R.raw.voice_travel_94
            "voice_travel_95" -> R.raw.voice_travel_95
            "voice_travel_96" -> R.raw.voice_travel_96
            "voice_travel_97" -> R.raw.voice_travel_97
            "voice_travel_98" -> R.raw.voice_travel_98
            "voice_travel_99" -> R.raw.voice_travel_99
            "voice_conversation_100" -> R.raw.voice_conversation_100
            "voice_conversation_101" -> R.raw.voice_conversation_101
            "voice_conversation_102" -> R.raw.voice_conversation_102
            "voice_conversation_103" -> R.raw.voice_conversation_103
            "voice_conversation_104" -> R.raw.voice_conversation_104
            "voice_conversation_105" -> R.raw.voice_conversation_105
            "voice_conversation_106" -> R.raw.voice_conversation_106
            "voice_conversation_107" -> R.raw.voice_conversation_107
            "voice_conversation_108" -> R.raw.voice_conversation_108
            "voice_conversation_109" -> R.raw.voice_conversation_109
            "voice_conversation_110" -> R.raw.voice_conversation_110
            else -> 0
        }
        if (resource == 0) { fallback(Request(profile, speed, token, choice, count)); return }
        try {
            val created = MediaPlayer.create(app, resource, attributes, AudioManager.AUDIO_SESSION_ID_GENERATE)
                ?: error("Cannot open bundled audio")
            player = created
            created.setOnCompletionListener { completed ->
                if (!closed && token == session && player === completed) {
                    if (remainingAudioRepeats > 0) {
                        remainingAudioRepeats--
                        runCatching { completed.seekTo(0); completed.start() }.onFailure {
                            releasePlayer(); releaseFocus()
                            status("Không thể phát lại mẫu giọng.", token)
                        }
                    } else {
                        releasePlayer(); releaseFocus(); status("Đã phát xong. Nhấn Nói để luyện tập.", token)
                    }
                }
            }
            created.setOnErrorListener { failed, _, _ ->
                if (!closed && token == session && player === failed) {
                    releasePlayer(); fallback(Request(profile, speed, token, choice, count))
                }
                true
            }
            created.playbackParams = PlaybackParams().allowDefaults().setPitch(1f).setSpeed(SpeechProfiles.playbackRate(speed))
            created.start()
            status("Đang phát giọng hội thoại…", token)
        } catch (_: Exception) {
            releasePlayer(); fallback(Request(profile, speed, token, choice))
        }
    }
    private fun fallback(request: Request) {
        if (closed || request.session != session) return
        if (!initialized) {
            pending = request; status("Đang chuẩn bị giọng đọc dự phòng…", request.session); return
        }
        val tts = engine
        if (tts == null) {
            releaseFocus()
            status("Không có dịch vụ giọng đọc trên thiết bị.", request.session)
            return
        }
        val chinese = request.language == "zh-CN"
        val selected = if (chinese) {
            val voices = mandarinVoices()
            val chosen = if (request.choice.voiceId == null) voices.firstOrNull() else voices.firstOrNull { it.id == request.choice.voiceId }
            tts.voices.orEmpty().firstOrNull { it.name == chosen?.id }
        } else if (request.choice.usesBundledAudio) defaultVoice
            else tts.voices.orEmpty().firstOrNull { voice ->
                voice.name == request.choice.voiceId &&
                    voice.locale.language == "en" && voice.locale.country == request.choice.accent &&
                    !voice.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
            }
        if (selected == null || (!chinese && request.choice.usesBundledAudio && !fallbackAvailable)) {
            releaseFocus()
            status(if (chinese) "Chưa có giọng tiếng Trung ngoại tuyến phù hợp. Hãy cài dữ liệu giọng Trung Quốc trong cài đặt Chuyển văn bản thành giọng nói của Android, rồi mở lại Speakivra." else "Chưa chọn giọng ngoại tuyến phù hợp. Vào Giọng đọc & Ngôn ngữ để nghe thử.", request.session)
            return
        }
        try {
            val prefix = "speech-${request.session}-"
            lastUtterance = prefix + "repeat-" + (request.repeats - 1) + "-" + request.profile.parts.lastIndex
            tts.stop()
            check(tts.setVoice(selected) == TextToSpeech.SUCCESS)
            repeat(request.repeats) { round ->
                request.profile.parts.forEachIndexed { index, part ->
                    check(tts.setPitch(part.pitch) == TextToSpeech.SUCCESS)
                    check(tts.setSpeechRate(if (chinese) request.speed / 100f else SpeechProfiles.fallbackRate(request.speed, part)) == TextToSpeech.SUCCESS)
                    check(tts.speak(if (chinese) part.text else personalNameSpeech(part.text), TextToSpeech.QUEUE_ADD, null, prefix + "repeat-$round-$index") == TextToSpeech.SUCCESS)
                    if (part.pauseAfterMs > 0 && index < request.profile.parts.lastIndex) {
                        check(tts.playSilentUtterance(part.pauseAfterMs, TextToSpeech.QUEUE_ADD, prefix + "gap-$round-$index") == TextToSpeech.SUCCESS)
                    }
                }
                if (round < request.repeats - 1) {
                    check(tts.playSilentUtterance(350L, TextToSpeech.QUEUE_ADD, prefix + "between-$round") == TextToSpeech.SUCCESS)
                }
            }
        } catch (_: Exception) {
            tts.stop(); lastUtterance = null; releaseFocus()
            status("Không phát được giọng dự phòng. Hãy thử lại.", request.session)
        }
    }
    private fun releasePlayer() {
        val active = player; player = null
        active?.setOnCompletionListener(null); active?.setOnErrorListener(null)
        runCatching { active?.release() }
    }
    private fun releaseFocus() {
        if (hasFocus) { hasFocus = false; runCatching { audio.abandonAudioFocusRequest(focusRequest) } }
    }
    fun stop(notify: Boolean = false) {
        val wasActive = player != null || pending != null || lastUtterance != null
        session++; pending = null; lastUtterance = null; remainingAudioRepeats = 0
        releasePlayer(); runCatching { engine?.stop() }; releaseFocus()
        if (notify && wasActive) status("Đã dừng giọng đọc.")
    }
    fun close() {
        closed = true; stop(); runCatching { engine?.shutdown() }; engine = null
    }
}

/** Hint the name's language without changing the displayed name or English voice selection. */
internal fun personalNameSpeech(text: String): CharSequence {
    val name = Regex("(?i)^My name is (.+?)[.!?]?$" ).matchEntire(text.trim())?.groups?.get(1) ?: return text
    if (name.value.none { it.code > 127 }) return text
    return android.text.SpannableString(text).apply {
        setSpan(android.text.style.LocaleSpan(java.util.Locale.forLanguageTag("vi-VN")),
            name.range.first, name.range.last + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
