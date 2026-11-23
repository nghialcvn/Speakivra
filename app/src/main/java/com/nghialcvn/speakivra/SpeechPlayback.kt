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
    private data class Request(val profile: SpeechProfile, val speed: Int, val session: Long)
    private var pending: Request? = null

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
            val chosen = candidates.firstOrNull() ?: tts.voice?.takeIf { voice ->
                voice.locale.language == "en" && voice.locale.country == "US" && !voice.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
            }
            chosen != null && tts.setVoice(chosen) == TextToSpeech.SUCCESS
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
                status("Không phát được giọng dự phòng. Kiểm tra giọng Anh-Mỹ trên thiết bị.")
            }
        }
    }
    fun speak(text: String, speed: Int) {
        if (closed || text.isBlank()) return
        stop()
        val token = session
        hasFocus = runCatching { audio.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED }.getOrDefault(false)
        if (!hasFocus) { status("Chưa thể phát âm thanh lúc này. Hãy thử nghe lại."); return }
        val profile = SpeechProfiles.forText(text)
        val resource = when (profile.clip) {
            "voice_greeting" -> R.raw.voice_greeting
            "voice_thanks" -> R.raw.voice_thanks
            "voice_introduction" -> R.raw.voice_introduction
            "voice_welcome" -> R.raw.voice_welcome
            "voice_ask_name" -> R.raw.voice_ask_name
            "voice_ask_origin" -> R.raw.voice_ask_origin
            "voice_ask_hobbies" -> R.raw.voice_ask_hobbies
            else -> 0
        }
        if (resource == 0) { fallback(Request(profile, speed, token)); return }
        try {
            val created = MediaPlayer.create(app, resource, attributes, AudioManager.AUDIO_SESSION_ID_GENERATE)
                ?: error("Cannot open bundled audio")
            player = created
            created.setOnCompletionListener { completed ->
                if (!closed && token == session && player === completed) {
                    releasePlayer(); releaseFocus(); status("Đã phát xong. Nhấn Nói để luyện tập.", token)
                }
            }
            created.setOnErrorListener { failed, _, _ ->
                if (!closed && token == session && player === failed) {
                    releasePlayer(); fallback(Request(profile, speed, token))
                }
                true
            }
            created.playbackParams = PlaybackParams().allowDefaults().setPitch(1f).setSpeed(SpeechProfiles.playbackRate(speed))
            created.start()
            status("Đang phát giọng hội thoại…", token)
        } catch (_: Exception) {
            releasePlayer(); fallback(Request(profile, speed, token))
        }
    }
    private fun fallback(request: Request) {
        if (closed || request.session != session) return
        if (!initialized) {
            pending = request; status("Đang chuẩn bị giọng đọc dự phòng…", request.session); return
        }
        val tts = engine
        if (!fallbackAvailable || tts == null) {
            releaseFocus(); status("Không phát được âm thanh. Hãy kiểm tra giọng Anh-Mỹ offline trong cài đặt TTS.", request.session); return
        }
        try {
            val prefix = "speech-${request.session}-"
            lastUtterance = prefix + request.profile.parts.lastIndex
            tts.stop()
            request.profile.parts.forEachIndexed { index, part ->
                check(tts.setPitch(part.pitch) == TextToSpeech.SUCCESS)
                check(tts.setSpeechRate(SpeechProfiles.fallbackRate(request.speed, part)) == TextToSpeech.SUCCESS)
                check(tts.speak(part.text, TextToSpeech.QUEUE_ADD, null, prefix + index) == TextToSpeech.SUCCESS)
                if (part.pauseAfterMs > 0 && index < request.profile.parts.lastIndex) {
                    check(tts.playSilentUtterance(part.pauseAfterMs, TextToSpeech.QUEUE_ADD, prefix + "gap-$index") == TextToSpeech.SUCCESS)
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
        session++; pending = null; lastUtterance = null
        releasePlayer(); runCatching { engine?.stop() }; releaseFocus()
        if (notify && wasActive) status("Đã dừng giọng đọc.")
    }
    fun close() {
        closed = true; stop(); runCatching { engine?.shutdown() }; engine = null
    }
}
