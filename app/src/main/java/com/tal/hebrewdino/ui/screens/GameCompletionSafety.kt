package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
internal fun GameCompletionSafety(
    stationId: Int,
    sessionCurrentIndex: Int,
    completionCallbackFired: Boolean,
    markCompletionCallbackFired: () -> Unit,
    sessionCorrectCount: Int,
    sessionMistakeCount: Int,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(stationId, sessionCurrentIndex) {
        if (!completionCallbackFired) {
            markCompletionCallbackFired()
            onComplete(stationId, sessionCorrectCount, sessionMistakeCount)
        }
    }
    Box(modifier = modifier.fillMaxSize())
}

