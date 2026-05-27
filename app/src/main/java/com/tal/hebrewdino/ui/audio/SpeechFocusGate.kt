package com.tal.hebrewdino.ui.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * When active, background music must be fully silent (TTS or narrative voice playing).
 */
object SpeechFocusGate {
    private val holdCount = AtomicInteger(0)
    private val _isSpeechActive = MutableStateFlow(false)
    val isSpeechActive: StateFlow<Boolean> = _isSpeechActive.asStateFlow()

    fun begin() {
        if (holdCount.incrementAndGet() == 1) {
            _isSpeechActive.value = true
        }
    }

    fun end() {
        val next = holdCount.updateAndGet { current -> (current - 1).coerceAtLeast(0) }
        if (next == 0) {
            _isSpeechActive.value = false
        }
    }

    fun reset() {
        holdCount.set(0)
        _isSpeechActive.value = false
    }
}
