package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

object StationInstructionReplay {
    const val IDLE_REPEAT_MS: Long = 8_000L
    const val IDLE_POLL_MS: Long = 500L
}

/**
 * Replays station voice instructions after [StationInstructionReplay.IDLE_REPEAT_MS] without user input.
 * Returns a callback to call on taps / answers so the idle timer resets.
 */
@Composable
fun rememberInstructionIdleReplay(
    enabled: Boolean,
    resetKey: Any?,
    onReplay: () -> Unit,
    idleMs: Long = StationInstructionReplay.IDLE_REPEAT_MS,
): () -> Unit {
    var lastActivityAtMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(resetKey) {
        lastActivityAtMs = System.currentTimeMillis()
    }

    LaunchedEffect(enabled, resetKey) {
        if (!enabled) return@LaunchedEffect
        while (true) {
            delay(StationInstructionReplay.IDLE_POLL_MS)
            if (!enabled) continue
            if (System.currentTimeMillis() - lastActivityAtMs >= idleMs) {
                onReplay()
                lastActivityAtMs = System.currentTimeMillis()
            }
        }
    }

    return remember {
        { lastActivityAtMs = System.currentTimeMillis() }
    }
}
