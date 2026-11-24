package com.nghialcvn.speakivra

import android.Manifest
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/** Runs separately from deterministic tests. NO fixture provider or supplied transcript:
 * feeds an existing spoken lesson clip to the emulator's actual configured recognizer.
 * A failure is retained as evidence, never counted as a successful speech test. */
@androidx.test.filters.SdkSuppress(minSdkVersion = 33)
class RealSpeechProviderProbeTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun actualProviderRecognizesSpokenEnglishPcm() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.executeShellCommand("pm grant ${context.packageName} ${Manifest.permission.RECORD_AUDIO}").use {
            java.io.FileInputStream(it.fileDescriptor).readBytes()
        }
        val pcm = decodeLessonClip()
        val pipe = RecognitionAudioPipe()
        val done = CountDownLatch(1)
        val result = AtomicReference("")
        val segments = mutableListOf<String>()
        var engine: SpeechRecognizer? = null
        try {
            ui.runOnUiThread {
                engine = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer ->
                    recognizer.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) = Unit
                        override fun onBeginningOfSpeech() = Unit
                        override fun onRmsChanged(rmsdB: Float) = Unit
                        override fun onBufferReceived(buffer: ByteArray?) = Unit
                        override fun onEndOfSpeech() = Unit
                        override fun onPartialResults(partialResults: Bundle?) = Unit
                        override fun onEvent(eventType: Int, params: Bundle?) = Unit
                        override fun onError(error: Int) { result.set("ERROR_$error"); done.countDown() }
                        override fun onSegmentResults(segmentResults: Bundle) {
                            segmentResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(segments::add)
                        }
                        override fun onEndOfSegmentedSession() {
                            result.set(segments.joinToString(" ")); done.countDown()
                        }
                        override fun onResults(results: Bundle?) {
                            result.set(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty())
                            done.countDown()
                        }
                    })
                    recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, pipe.source)
                        putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, RecognizerIntent.EXTRA_AUDIO_SOURCE)
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, 16000)
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                        putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, android.media.AudioFormat.ENCODING_PCM_16BIT)
                    })
                }
            }
            val writer = thread(name = "real-provider-spoken-clip") {
                val start = android.os.SystemClock.elapsedRealtime()
                while (!pipe.isConsumed() && done.count > 0 && android.os.SystemClock.elapsedRealtime() - start < 7000)
                    android.os.SystemClock.sleep(25)
                if (pipe.isConsumed()) {
                    val bytes = ByteArray(6400) + pcm + ByteArray(64000)
                    var offset = 0
                    while (offset < bytes.size && done.count > 0) {
                        val count = minOf(1024, bytes.size - offset)
                        if (!pipe.write(bytes.copyOfRange(offset, offset + count), count)) break
                        offset += count
                        android.os.SystemClock.sleep(32)
                    }
                }
                pipe.endInput()
            }
            if (!done.await(25, TimeUnit.SECONDS)) result.set("TIMEOUT")
            pipe.close(); writer.join(1000)
            val recognized = result.get()
            val report = "Actual Android recognition provider; input: bundled voice_greeting.ogg (Hello! How are you?); returned: $recognized"
            File(context.getExternalFilesDir(null), "ui-screenshots/real-provider-result.txt").apply { parentFile!!.mkdirs(); writeText(report) }
            println(report)
            assertTrue("Actual provider did not recognize the spoken clip: $recognized",
                !recognized.startsWith("ERROR_") && recognized != "TIMEOUT" && matchWords("Hello! How are you?", recognized).matched.count { it } >= 3)
        } finally {
            pipe.close()
            ui.runOnUiThread { engine?.cancel(); engine?.destroy() }
        }
    }
    private fun decodeLessonClip(): ByteArray {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val extractor = MediaExtractor()
        val descriptor = context.resources.openRawResourceFd(R.raw.voice_greeting)
        extractor.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        descriptor.close()
        extractor.selectTrack(0)
        val format = extractor.getTrackFormat(0)
        format.setInteger(MediaFormat.KEY_PCM_ENCODING, android.media.AudioFormat.ENCODING_PCM_16BIT)
        val codec = MediaCodec.createDecoderByType(requireNotNull(format.getString(MediaFormat.KEY_MIME)))
        codec.configure(format, null, null, 0); codec.start()
        val output = ByteArrayOutputStream()
        var rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        var eos = false
        try {
            val info = MediaCodec.BufferInfo()
            var finished = false
            val deadline = android.os.SystemClock.elapsedRealtime() + 10000
            while (!finished && android.os.SystemClock.elapsedRealtime() < deadline) {
                if (!eos) {
                    val slot = codec.dequeueInputBuffer(10000)
                    if (slot >= 0) {
                        val input = requireNotNull(codec.getInputBuffer(slot))
                        val count = extractor.readSampleData(input, 0)
                        if (count < 0) { codec.queueInputBuffer(slot, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); eos = true }
                        else { codec.queueInputBuffer(slot, 0, count, extractor.sampleTime, 0); extractor.advance() }
                    }
                }
                val slot = codec.dequeueOutputBuffer(info, 10000)
                if (slot == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    rate = codec.outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    channels = codec.outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                } else if (slot >= 0) {
                    val buffer = requireNotNull(codec.getOutputBuffer(slot))
                    buffer.position(info.offset); buffer.limit(info.offset + info.size)
                    val bytes = ByteArray(info.size); buffer.get(bytes); output.write(bytes)
                    finished = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    codec.releaseOutputBuffer(slot, false)
                }
            }
        } finally { codec.stop(); codec.release(); extractor.release() }
        val samples = ByteBuffer.wrap(output.toByteArray()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val frames = samples.remaining() / channels
        check(frames > 0)
        val target = ByteBuffer.allocate((frames.toLong() * 16000 / rate).toInt() * 2).order(ByteOrder.LITTLE_ENDIAN)
        repeat(target.capacity() / 2) { index ->
            val source = (index.toLong() * rate / 16000).toInt().coerceAtMost(frames - 1)
            var sum = 0
            repeat(channels) { channel -> sum += samples.get(source * channels + channel).toInt() }
            target.putShort((sum / channels).toShort())
        }
        return target.array()
    }
}
