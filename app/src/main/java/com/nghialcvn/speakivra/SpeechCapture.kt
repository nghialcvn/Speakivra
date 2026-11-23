package com.nghialcvn.speakivra

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Main-thread, foreground-only speech recognition. Never launches an external activity. */
internal class SpeechCapture(
    private val context: Context,
    private val update: (phase: String, words: String?, message: String) -> Unit
) {
    private val session = CaptureSession()
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var timeout: Runnable? = null
    val phase get() = session.phase

    fun start() {
        val token = session.begin() ?: return
        update("listening", "", "Đang mở micro…")
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                finish(token, "", "Cần cấp quyền micro trong Cài đặt Android → Speakivra.")
                return
            }
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                finish(token, null, "Thiết bị chưa có dịch vụ nhận dạng giọng nói. Kiểm tra cài đặt giọng nói Android.")
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
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Dịch vụ chưa sẵn sàng cho tiếng Anh Mỹ. Kiểm tra ngôn ngữ nhận dạng trên thiết bị."
                        else -> "Không nhận dạng được lời nói (mã $error). Chạm micro để thử lại."
                    }
                    finish(token, "", message)
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            armTimeout(token, 60_000)
            engine.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            })
        } catch (_: Exception) {
            finish(token, "", "Không mở được dịch vụ giọng nói. Kiểm tra quyền micro và thử lại.")
        }
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
        val engine = recognizer; recognizer = null
        runCatching { engine?.cancel() }
        runCatching { engine?.destroy() }
    }
    fun cancel() {
        val active = phase != "idle"
        session.cancel()
        release()
        if (active) update("idle", null, "Đã dừng micro.")
    }
}
