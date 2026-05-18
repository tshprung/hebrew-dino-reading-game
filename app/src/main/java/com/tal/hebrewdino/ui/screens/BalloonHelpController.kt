package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BalloonHelpController(
    private val scope: CoroutineScope,
) {
    var hintLocksChoices by mutableStateOf(false)
        private set

    var hintLetter by mutableStateOf<String?>(null)
        private set

    fun reset() {
        hintLocksChoices = false
        hintLetter = null
    }

    fun performHint(letter: String, durationMs: Long) {
        if (hintLocksChoices) return
        hintLocksChoices = true
        hintLetter = letter
        scope.launch {
            delay(durationMs)
            reset()
        }
    }
}

@Composable
fun rememberBalloonHelpController(
    stationId: Int,
    scope: CoroutineScope,
): BalloonHelpController = remember(stationId) { BalloonHelpController(scope) }
