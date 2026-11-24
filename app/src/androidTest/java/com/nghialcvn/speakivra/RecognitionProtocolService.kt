package com.nghialcvn.speakivra

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/** Binder protocol fixture, NOT an English recognizer. Exists only in the test APK. */
class RecognitionProtocolService : RecognitionService() {
    companion object {
        @Volatile var ignorePipe = false
        @Volatile var waitForEnd = false
        @Volatile var answer = "Hello there."
        val directStarts = AtomicInteger()
        val liveBytes = AtomicInteger()
    }
    private val handler = Handler(Looper.getMainLooper())
    @Volatile private var active: Callback? = null
    private var input: ParcelFileDescriptor? = null
    override fun onStartListening(intent: Intent, callback: Callback) {
        active = callback
        callback.readyForSpeech(Bundle())
        @Suppress("DEPRECATION")
        val source = intent.getParcelableExtra<ParcelFileDescriptor>(RecognizerIntent.EXTRA_AUDIO_SOURCE)
        if (source != null) {
            input = source
            if (ignorePipe) return
            thread(name = "test-recognition-pipe") {
                try {
                    ParcelFileDescriptor.AutoCloseInputStream(source).use { stream ->
                        val bytes = ByteArray(1024)
                        var received = 0
                        while (active === callback) {
                            val count = stream.read(bytes)
                            if (count < 0) { complete(callback); break }
                            received += count; liveBytes.addAndGet(count)
                            if (received >= 8192) {
                                handler.post { if (active === callback) callback.partialResults(result("Hello")) }
                                // Require live microphone data beyond the probe, not a completed buffer.
                                if (received >= 16000 && !waitForEnd) { complete(callback); break }
                            }
                        }
                    }
                } catch (_: Exception) { /* cancellation closes the pipe */ }
            }
        } else {
            directStarts.incrementAndGet()
            // This tests the real Android service binding and callback path. It
            // deliberately does not claim to recognize emulator microphone silence.
            handler.postDelayed({ if (active === callback) callback.partialResults(result("Hello")) }, 100)
            handler.postDelayed({ complete(callback) }, 500)
        }
    }
    private fun result(text: String) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(text))
    }
    private fun complete(callback: Callback) {
        handler.post {
            if (active === callback) {
                callback.endOfSpeech()
                callback.results(result(answer))
                active = null
            }
        }
    }
    override fun onStopListening(callback: Callback) { complete(callback) }
    override fun onCancel(callback: Callback) { active = null; runCatching { input?.close() }; input = null }
}
