package com.nghialcvn.speakivra

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

/** Only the current attempt's PCM lives in RAM. Never writes files or uploads replay audio. */
internal class TemporarySpeechAudio {
    companion object { const val RATE = 16000; const val MAX_BYTES = RATE * 2 * 60 }
    @Volatile private var recording = false
    private var recorder: AudioRecord? = null
    private var worker: Thread? = null
    private var stream: ByteArrayOutputStream? = null
    private var pcm: ByteArray? = null
    private var player: AudioTrack? = null
    val capturedBytes get() = stream?.size() ?: 0
    val isPlaying get() = player?.playState == AudioTrack.PLAYSTATE_PLAYING
    val available get() = pcm?.isNotEmpty() == true

    @SuppressLint("MissingPermission")
    fun start(onEndpoint: (() -> Unit)? = null) {
        clear()
        val minimum = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        check(minimum > 0)
        val input = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minimum * 2, 8192))
        recorder = input
        check(input.state == AudioRecord.STATE_INITIALIZED)
        val output = ByteArrayOutputStream()
        stream = output
        input.startRecording()
        check(input.recordingState == AudioRecord.RECORDSTATE_RECORDING)
        recording = true
        worker = thread(name = "speakivra-current-utterance") {
            val buffer = ByteArray(1024)
            val endpoint = if (onEndpoint != null) SpeechEndpoint() else null
            try {
                while (recording && output.size() < MAX_BYTES) {
                    val count = input.read(buffer, 0, minOf(buffer.size, MAX_BYTES - output.size()))
                    if (count <= 0) { if (recording) onEndpoint?.invoke(); break }
                    output.write(buffer, 0, count)
                    if (endpoint?.accept(buffer, count) != null) {
                        onEndpoint?.invoke()
                        break
                    }
                }
            } finally { buffer.fill(0) }
        }
    }
    fun finish(): ByteArray? {
        recording = false
        runCatching { recorder?.stop() }
        worker?.join(1000)
        val stopped = worker?.isAlive != true
        runCatching { recorder?.release() }; recorder = null
        worker = null
        pcm = if (stopped) stream?.toByteArray()?.takeIf { it.size >= 3200 } else null
        stream?.reset(); stream = null
        return pcm
    }
    fun play(onError: () -> Unit) {
        val bytes = pcm ?: return
        stopPlayback()
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(bytes.size).build()
            player = track
            check(track.write(bytes, 0, bytes.size) == bytes.size)
            track.play()
        } catch (_: Exception) { stopPlayback(); onError() }
    }
    fun stopPlayback() { val active = player; player = null; runCatching { active?.stop() }; runCatching { active?.release() } }
    fun clear() {
        stopPlayback()
        if (recorder != null) finish()
        pcm?.fill(0); pcm = null
        stream?.reset(); stream = null
    }
}
