package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.domain.LevelSession

@Composable
internal fun GameCompletionSafety(
    stationId: Int,
    sessionCurrentIndex: Int,
    session: LevelSession,
    gameViewModel: GameViewModel,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(stationId, sessionCurrentIndex) {
        if (!gameViewModel.completionCallbackFired) {
            gameViewModel.completionCallbackFired = true
            onComplete(stationId, session.correctCount, session.mistakeCount)
        }
    }
    Box(modifier = modifier.fillMaxSize())
}

