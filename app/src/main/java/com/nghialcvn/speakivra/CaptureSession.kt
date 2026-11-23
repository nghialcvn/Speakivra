package com.nghialcvn.speakivra

/** Invalidates callbacks from cancelled sessions and prevents overlapping requests. */
internal class CaptureSession {
    var token = 0L
        private set
    var phase = "idle"
        private set
    fun begin(): Long? {
        if (phase != "idle") return null
        token++
        phase = "listening"
        return token
    }
    fun accepts(value: Long) = value == token && phase != "idle"
    fun processing(value: Long): Boolean {
        if (!accepts(value)) return false
        phase = "processing"
        return true
    }
    fun finish(value: Long): Boolean {
        if (!accepts(value)) return false
        phase = "idle"
        return true
    }
    fun cancel() { token++; phase = "idle" }
}
