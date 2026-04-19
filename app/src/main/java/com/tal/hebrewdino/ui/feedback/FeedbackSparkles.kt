package com.tal.hebrewdino.ui.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight sparkle burst (Compose-only). [burstKey] increment triggers a new burst.
 */
@Composable
fun FeedbackSparkles(
    burstKey: Int,
    modifier: Modifier = Modifier,
    seed: Int = 0,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        alpha.snapTo(0.9f)
        alpha.animateTo(0f, tween(durationMillis = 420))
    }
    val rnd = remember(seed) { Random(seed) }
    Canvas(modifier = modifier.fillMaxSize()) {
        if (alpha.value <= 0.01f) return@Canvas
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val n = 10
        repeat(n) { i ->
            val ang = (i / n.toFloat()) * 6.2831855f + rnd.nextFloat() * 0.4f
            val dist = size.minDimension * (0.22f + rnd.nextFloat() * 0.28f)
            val ox = cos(ang) * dist
            val oy = sin(ang) * dist
            val r = 3f + rnd.nextFloat() * 4f
            drawCircle(
                color =
                    when (i % 4) {
                        0 -> Color(0xFFFFE082)
                        1 -> Color(0xFFFFFFFF)
                        2 -> Color(0xFF81D4FA)
                        else -> Color(0xFFA5D6A7)
                    }.copy(alpha = alpha.value * 0.85f),
                radius = r,
                center = Offset(cx + ox * 0.35f, cy + oy * 0.35f),
            )
        }
    }
}
