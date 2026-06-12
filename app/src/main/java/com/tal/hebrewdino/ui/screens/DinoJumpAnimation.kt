package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun DinoJumpAnimation(
    gameViewModel: GameViewModel,
    jumpFramesCount: Int,
) {
    LaunchedEffect(gameViewModel.dinoVisual) {
        if (gameViewModel.dinoVisual != DinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            gameViewModel.jumpFrameIndex = (i % jumpFramesCount)
            delay(85.milliseconds)
        }
        gameViewModel.dinoVisual = DinoVisual.Idle
    }
}

