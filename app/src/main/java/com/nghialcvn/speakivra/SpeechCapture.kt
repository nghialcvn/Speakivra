package com.nghialcvn.speakivra

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

/** Main-thread, foreground-only speech recognition. Never launches an external activity. */
internal class SpeechCapture(
    private val context: Context,
    private val recordingChanged: (Boolean) -> Unit = {},
    private val update: (phase: String, words: String?, message: String) -> Unit
) {
    private val session = CaptureSession()
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var timeout: Runnable? = null
    private var probe: Runnable? = null
    private val audio = TemporarySpeechAudio()
    private var pipe: RecognitionAudioPipe? = null
    private var capturing = false
    private var generation = 0L
    private var locale = "en-US"
    private var nameHints = arrayListOf<String>()
    private var endpointSilenceMillis = 1400
    private var maximumAttemptMillis = 60000
    val hasRecording get() = audio.available
    val phase get() = session.phase
    fun replay() {
        if (phase == "idle") audio.play { update("idle", null, "Không phát được bản ghi. Hãy nói lại.") }
    }
    fun start(languageTag: String = "en-US", silenceMillis: Int = 1400, properName: String? = null, maximumMillis: Int = 60000) {
        require(languageTag in setOf("en-US", "en-GB"))
        val token = session.begin() ?: return
        audio.clear(); recordingChanged(false)
        maximumAttemptMillis = maximumMillis.coerceIn(60000, MAX_SPEECH_ATTEMPT_MILLIS)
        locale = languageTag
        nameHints = arrayListOf<String>().apply { properName?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) } }
        endpointSilenceMillis = silenceMillis.coerceIn(1400, 4000)
        update("listening", "", "Đang mở micro…")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            finish(token, "", "Cần cấp quyền micro trong Cài đặt Android → Speakivra.")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            finish(token, "", "Thiết bị chưa có dịch vụ nhận dạng. Bật dịch vụ nhận dạng giọng nói trong Cài đặt Android.")
            return
        }
        beginRecognition(token, usePipe = Build.VERSION.SDK_INT >= 33)
    }
    private fun current(token: Long, attempt: Long) = session.accepts(token) && attempt == generation
    private fun beginRecognition(token: Long, usePipe: Boolean, settled: Boolean = false) {
        releaseEngine()
        val attempt = generation
        if (!usePipe && !settled) {
            // cancel/destroy crosses Binder. Allow the previous provider session to
            // release its mic before starting the original direct microphone path.
            handler.postDelayed({
                if (current(token, attempt) && phase == "listening") beginRecognition(token, false, settled = true)
            }, 200)
            return
        }
        var streamingStarted = false
        var providerReady = false
        val segments = mutableListOf<String>()
        try {
            val live = if (usePipe && Build.VERSION.SDK_INT >= 33) RecognitionAudioPipe().also { pipe = it } else null
            val engine = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = engine
            engine.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (!current(token, attempt)) return
                    providerReady = true
                    if (live == null) update(phase, null, "Đang nghe… Nói xong, micro sẽ tự dừng. Chạm để dừng sớm.")
                }
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    if (!current(token, attempt)) return
                    processing(token)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    if (!current(token, attempt)) return
                    val text = words(partialResults)
                    if (!streamingStarted && !text.isNullOrBlank()) {
                        // Only silence has been sent to the pipe so far. These words
                        // came from the provider's own mic; do not interrupt it at the
                        // capability deadline or start a competing recorder.
                        probe?.let(handler::removeCallbacks); probe = null
                    }
                    update(phase, text, "Đang nhận dạng…")
                }
                override fun onSegmentResults(segmentResults: Bundle) {
                    if (!current(token, attempt)) return
                    words(segmentResults)?.takeIf(String::isNotBlank)?.let(segments::add)
                    update(phase, segments.joinToString(" "), "Đang nhận dạng…")
                    if (!streamingStarted && phase == "listening") stop()
                }
                override fun onEndOfSegmentedSession() {
                    if (!current(token, attempt)) return
                    val text = segments.joinToString(" ")
                    finish(token, text, if (text.isBlank()) "Chưa nhận được lời nói. Chạm micro để thử lại."
                        else "Đã nhận dạng lời nói. Đây chưa phải điểm phát âm.")
                }
                override fun onResults(results: Bundle?) {
                    if (!current(token, attempt)) return
                    // A provider that ignores the pipe may already have captured
                    // the learner using its own microphone. Preserve its REAL result;
                    // restarting here would discard the words the learner just spoke.
                    val text = words(results).orEmpty().ifBlank { segments.joinToString(" ") }
                    finish(token, text, if (text.isBlank()) "Chưa nhận được lời nói. Chạm micro để thử lại."
                        else if (!streamingStarted) "Đã nhận dạng. Dịch vụ trên máy không cung cấp âm thanh để nghe lại."
                        else "Đã nhận dạng lời nói. Đây chưa phải điểm phát âm.")
                }
                override fun onError(error: Int) {
                    if (!current(token, attempt)) return
                    if (live != null && !streamingStarted && phase == "listening" && error in setOf(
                            SpeechRecognizer.ERROR_CLIENT, SpeechRecognizer.ERROR_AUDIO)) {
                        beginRecognition(token, usePipe = false)
                        return
                    }
                    val message = when (error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Cần cấp quyền micro trong Cài đặt Android → Speakivra."
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Chưa nghe rõ. Chạm micro và thử nói lại nhé."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Dịch vụ nhận dạng cần kết nối mạng. Kiểm tra mạng rồi thử lại."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Dịch vụ giọng nói đang bận. Chờ một chút rồi thử lại."
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Dịch vụ chưa sẵn sàng cho $locale. Kiểm tra ngôn ngữ nhận dạng trên thiết bị."
                        else -> "Không nhận dạng được lời nói (mã $error). Chạm micro để thử lại."
                    }
                    finish(token, "", message)
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            engine.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                if (live != null && Build.VERSION.SDK_INT >= 33) {
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, live.source)
                    putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, RecognizerIntent.EXTRA_AUDIO_SOURCE)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, TemporarySpeechAudio.RATE)
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                // A recognition hint, never a replacement for the actual recognised words.
                if (Build.VERSION.SDK_INT >= 33 && nameHints.isNotEmpty())
                    putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS, nameHints)
            })
            armTimeout(token, maximumAttemptMillis.toLong())
            if (live != null) {
                val deadline = SystemClock.elapsedRealtime() + 4000
                probe = object : Runnable {
                    override fun run() {
                        if (!current(token, attempt) || phase != "listening") return
                        if (live.isConsumed()) {
                            probe = null
                            try {
                                capturing = true
                                audio.start(onEndpoint = {
                                    handler.post { if (current(token, attempt) && phase == "listening") stop() }
                                }, onPcm = live::write, silenceMillis = endpointSilenceMillis, maximumMillis = maximumAttemptMillis)
                                streamingStarted = true
                                update("listening", null, "Đang nghe… Nói xong, micro sẽ tự dừng. Chạm để dừng sớm.")
                            } catch (_: Exception) {
                                capturing = false
                                audio.clear()
                                beginRecognition(token, usePipe = false)
                            }
                        } else if (SystemClock.elapsedRealtime() >= deadline) {
                            probe = null
                            // An unsupported provider opens its own mic. If it is ready,
                            // keep that SAME session: restarting could lose a long sentence
                            // before its first partial/final result. Never open AudioRecord here.
                            if (providerReady) update("listening", null, "Đang nghe… Nói xong, micro sẽ tự dừng. Chạm để dừng sớm.")
                            else beginRecognition(token, usePipe = false)
                        } else handler.postDelayed(this, 50)
                    }
                }.also { handler.post(it) }
            }
        } catch (_: Exception) {
            if (usePipe) beginRecognition(token, usePipe = false)
            else finish(token, "", "Không mở được dịch vụ giọng nói. Kiểm tra quyền micro và thử lại.")
        }
    }
    private fun stopRecording() {
        if (capturing) {
            capturing = false
            audio.finish()
            recordingChanged(audio.available)
        }
        pipe?.endInput()
    }
    fun stop() {
        if (phase != "listening") return
        val token = session.token
        processing(token)
        try { recognizer?.stopListening() }
        catch (_: Exception) { finish(token, "", "Không kết thúc được lượt nói. Chạm micro để thử lại.") }
    }
    private fun processing(token: Long) {
        if (session.processing(token)) {
            probe?.let(handler::removeCallbacks); probe = null
            stopRecording()
            update("processing", null, "Đang xử lý lời nói…")
            armTimeout(token, 15_000)
        }
    }
    private fun words(bundle: Bundle?) = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
    private fun armTimeout(token: Long, delay: Long) {
        timeout?.let(handler::removeCallbacks)
        timeout = Runnable {
            if (session.accepts(token) && phase == "listening") stop()
            else finish(token, "", "Dịch vụ giọng nói chưa phản hồi. Chạm micro để thử lại.")
        }
            .also { handler.postDelayed(it, delay) }
    }
    private fun finish(token: Long, words: String?, message: String) {
        if (!session.finish(token)) return
        stopRecording()
        releaseEngine()
        update("idle", words, message)
    }
    private fun releaseEngine() {
        generation++ // reject callbacks from a replaced provider, including cancel errors
        timeout?.let(handler::removeCallbacks); timeout = null
        probe?.let(handler::removeCallbacks); probe = null
        pipe?.close(); pipe = null
        val engine = recognizer; recognizer = null
        runCatching { engine?.cancel() }
        runCatching { engine?.destroy() }
    }
    fun cancel(discardRecording: Boolean = true) {
        val active = phase != "idle"
        session.cancel()
        stopRecording()
        releaseEngine()
        if (discardRecording || active) { audio.clear(); recordingChanged(false) }
        else audio.stopPlayback()
        if (active) update("idle", null, "Đã dừng micro.")
    }
}
