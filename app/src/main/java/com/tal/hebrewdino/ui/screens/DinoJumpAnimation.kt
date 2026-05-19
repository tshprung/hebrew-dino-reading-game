package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
internal fun DinoJumpAnimation(
    gameViewModel: GameViewModel,
    jumpFramesCount: Int,
) {
    LaunchedEffect(gameViewModel.dinoVisual) {
        if (gameViewModel.dinoVisual != DinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            gameViewModel.jumpFrameIndex = (i % jumpFramesCount)
            delay(85)
        }
        gameViewModel.dinoVisual = DinoVisual.Idle
    }
}

