package com.nghialcvn.speakivra

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.media.AudioFormat
import kotlin.concurrent.thread
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

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
    private val audio = TemporarySpeechAudio()
    private var source: ParcelFileDescriptor? = null
    private var sink: ParcelFileDescriptor? = null
    private var locale = "en-US"
    val hasRecording get() = audio.available
    fun replay() {
        if (phase == "idle") audio.play { update("idle", null, "Không phát được bản ghi. Hãy nói lại.") }
    }
    val phase get() = session.phase

    fun start(languageTag: String = "en-US") {
        require(languageTag in setOf("en-US", "en-GB"))
        val token = session.begin() ?: return
        audio.clear(); recordingChanged(false)
        locale = languageTag
        update("listening", "", "Đang mở micro…")
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                finish(token, "", "Cần cấp quyền micro trong Cài đặt Android → Speakivra.")
                return
            }
            if (Build.VERSION.SDK_INT >= 33) {
                audio.start()
                timeout = Runnable { stop() }.also { handler.postDelayed(it, 60_000) }
                update("listening", null, "Đang ghi âm… Chạm micro lần nữa để kết thúc.")
                return
            }
            beginRecognition(token, languageTag)
        } catch (_: Exception) {
            finish(token, "", "Không mở được micro. Kiểm tra quyền và thử lại.")
        }
    }
    private fun beginRecognition(token: Long, languageTag: String, bytes: ByteArray? = null) {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                finish(token, "", "Thiết bị chưa có dịch vụ nhận dạng. Bạn vẫn có thể nghe lại bản ghi vừa nói.")
                return
            }
            // Use the configured provider: it chooses available language models/network support.
            val engine = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = engine
            engine.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (session.accepts(token) && phase == "listening")
                        update(phase, null, "Đang nghe… Nói tiếng Anh, chạm micro lần nữa để kết thúc.")
                }
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() { processing(token) }
                override fun onPartialResults(partialResults: Bundle?) {
                    if (session.accepts(token)) update(phase, words(partialResults), "Đang nhận dạng…")
                }
                override fun onResults(results: Bundle?) {
                    val text = words(results).orEmpty()
                    finish(token, text, if (text.isBlank()) "Chưa nhận được lời nói. Chạm micro để thử lại."
                        else "Đã nhận dạng lời nói. Đây chưa phải điểm phát âm.")
                }
                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Cần cấp quyền micro trong Cài đặt Android → Speakivra."
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Chưa nghe rõ. Chạm micro và thử nói lại nhé."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Dịch vụ nhận dạng cần kết nối mạng. Kiểm tra mạng rồi thử lại."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Dịch vụ giọng nói đang bận. Chờ một chút rồi thử lại."
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Dịch vụ chưa sẵn sàng cho $languageTag. Kiểm tra ngôn ngữ nhận dạng trên thiết bị."
                        else -> "Không nhận dạng được lời nói (mã $error). Chạm micro để thử lại."
                    }
                    finish(token, "", message)
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            armTimeout(token, if (bytes == null) 60_000 else 15_000)
            engine.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                if (bytes != null && Build.VERSION.SDK_INT >= 33) {
                    val pipe = ParcelFileDescriptor.createPipe()
                    source = pipe[0]; sink = pipe[1]
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, pipe[0])
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, TemporarySpeechAudio.RATE)
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            })
            if (bytes != null) {
                val output = sink
                thread(name = "speakivra-recognition-input") {
                    runCatching { output?.let { ParcelFileDescriptor.AutoCloseOutputStream(it).use { stream -> stream.write(bytes) } } }
                }
            }
        } catch (_: Exception) {
            finish(token, "", "Không mở được dịch vụ giọng nói. Kiểm tra quyền micro và thử lại.")
        }
    }
    fun stop() {
        if (phase != "listening") return
        val token = session.token
        processing(token)
        if (Build.VERSION.SDK_INT >= 33) {
            val bytes = audio.finish()
            recordingChanged(audio.available)
            if (bytes == null) finish(token, "", "Bản ghi quá ngắn. Chạm micro và nói lại.")
            else beginRecognition(token, locale, bytes)
            return
        }
        try { recognizer?.stopListening() }
        catch (_: Exception) { finish(token, "", "Không kết thúc được lượt nói. Chạm micro để thử lại.") }
    }
    private fun processing(token: Long) {
        if (session.processing(token)) {
            update("processing", null, "Đang xử lý lời nói…")
            armTimeout(token, 15_000)
        }
    }
    private fun words(bundle: Bundle?) = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
    private fun armTimeout(token: Long, delay: Long) {
        timeout?.let(handler::removeCallbacks)
        timeout = Runnable { finish(token, "", "Dịch vụ giọng nói chưa phản hồi. Chạm micro để thử lại.") }
            .also { handler.postDelayed(it, delay) }
    }
    private fun finish(token: Long, words: String?, message: String) {
        if (!session.finish(token)) return
        release()
        update("idle", words, message)
    }
    private fun release() {
        timeout?.let(handler::removeCallbacks); timeout = null
        runCatching { source?.close() }; source = null
        runCatching { sink?.close() }; sink = null
        val engine = recognizer; recognizer = null
        runCatching { engine?.cancel() }
        runCatching { engine?.destroy() }
    }
    fun cancel(discardRecording: Boolean = true) {
        val active = phase != "idle"
        session.cancel()
        release()
        if (discardRecording || active) { audio.clear(); recordingChanged(false) }
        else audio.stopPlayback()
        if (active) update("idle", null, "Đã dừng micro.")
    }
}
