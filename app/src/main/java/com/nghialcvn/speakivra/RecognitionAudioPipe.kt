package com.nghialcvn.speakivra

import androidx.annotation.RequiresApi
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants

/** A bounded, live PCM connection. No file, network upload or second microphone. */
internal class RecognitionAudioPipe @RequiresApi(30) constructor() : AutoCloseable {
    private val ends = ParcelFileDescriptor.createPipe()
    val source: ParcelFileDescriptor get() = ends[0]
    private val monitor = ParcelFileDescriptor.AutoCloseInputStream(ends[0])
    @Volatile private var closed = false
    private val primerBytes = 4096
    init {
        val flags = Os.fcntlInt(ends[1].fileDescriptor, OsConstants.F_GETFL, 0)
        Os.fcntlInt(ends[1].fileDescriptor, OsConstants.F_SETFL, flags or OsConstants.O_NONBLOCK)
        check(Os.write(ends[1].fileDescriptor, ByteArray(primerBytes), 0, primerBytes) == primerBytes)
    }
    // EXTRA_AUDIO_SOURCE is optional. Check that the provider actually READS the
    // pipe before opening our microphone. Ignoring the extra must never steal
    // the microphone from the working default recognition path.
    fun isConsumed(): Boolean = runCatching {
        monitor.available() < primerBytes
    }.getOrDefault(false)

    fun write(bytes: ByteArray, count: Int): Boolean {
        var offset = 0
        val deadline = SystemClock.elapsedRealtime() + 500
        while (!closed && offset < count) {
            try { offset += Os.write(ends[1].fileDescriptor, bytes, offset, count - offset) }
            catch (error: ErrnoException) {
                if (error.errno != OsConstants.EAGAIN || SystemClock.elapsedRealtime() >= deadline) return false
                SystemClock.sleep(5)
            }
        }
        return offset == count
    }
    fun endInput() { closed = true; runCatching { ends[1].close() } }
    override fun close() { endInput(); runCatching { monitor.close() } }
}
