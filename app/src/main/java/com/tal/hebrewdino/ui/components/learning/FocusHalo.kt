package com.tal.hebrewdino.ui.components.learning

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Slow, calm focus ring behind a letter — used when audio is off (e.g. letters intro playback).
 * Only one active halo per screen in normal use.
 */
@Composable
fun FocusHalo(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!active) return
    val pulse by rememberInfiniteTransition(label = "halo").animateFloat(
        initialValue = 0.74f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1900), repeatMode = RepeatMode.Reverse),
        label = "haloPulse",
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val r = size.minDimension * 0.52f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            color = Color(0xFF2E7D32).copy(alpha = 0.085f * pulse),
            radius = r,
            center = c,
        )
        drawCircle(
            color = Color(0xFF81C784).copy(alpha = 0.055f * pulse),
            radius = r * 0.78f,
            center = c,
        )
    }
}

/** Letter content centered with optional halo behind (intro / focus). */
@Composable
fun LetterTileWithHalo(
    haloActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.padding(4.dp), contentAlignment = Alignment.Center) {
        if (haloActive) {
            FocusHalo(active = true, modifier = Modifier.fillMaxSize())
        }
        content()
    }
}
