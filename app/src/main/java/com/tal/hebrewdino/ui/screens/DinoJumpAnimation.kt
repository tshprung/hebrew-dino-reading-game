package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
internal fun DinoJumpAnimation(
    dinoVisual: DinoVisual,
    jumpFramesCount: Int,
    setJumpFrameIndex: (Int) -> Unit,
    setDinoVisual: (DinoVisual) -> Unit,
) {
    LaunchedEffect(dinoVisual) {
        if (dinoVisual != DinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            setJumpFrameIndex(i % jumpFramesCount)
            delay(85)
        }
        setDinoVisual(DinoVisual.Idle)
    }
}

