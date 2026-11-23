package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class CaptureSessionTest {
    @Test fun waitsForFinalResultBeforeAnotherStart() {
        val state = CaptureSession()
        val token = state.begin()!!
        assertNull(state.begin())
        assertTrue(state.processing(token))
        assertNull(state.begin())
        assertTrue(state.finish(token))
        assertNotNull(state.begin())
    }
    @Test fun navigationDiscardsOldPartialResultsAndErrors() {
        val state = CaptureSession()
        val old = state.begin()!!
        state.cancel()
        val current = state.begin()!!
        assertFalse(state.accepts(old))
        assertFalse(state.finish(old))
        assertFalse(state.processing(old))
        assertTrue(state.accepts(current))
        assertEquals("listening", state.phase)
    }
    @Test fun duplicateFinalCallbacksAreIgnored() {
        val state = CaptureSession()
        val token = state.begin()!!
        assertTrue(state.finish(token))
        assertFalse(state.finish(token))
        assertFalse(state.accepts(token))
        assertEquals("idle", state.phase)
    }
}
