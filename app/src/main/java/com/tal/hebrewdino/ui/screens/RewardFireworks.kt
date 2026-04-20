package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val BurstColors =
    listOf(
        Color(0xFFFFE082),
        Color(0xFFFF8A65),
        Color(0xFF81D4FA),
        Color(0xFFA5D6A7),
        Color(0xFFE1BEE7),
        Color(0xFFFFF59D),
    )

/**
 * Lightweight celebratory fireworks (no assets) — rockets rise then radial bursts.
 */
@Composable
fun RewardFireworksLayer(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "fw").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(3200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "fwPhase",
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val launches = listOf(0.12f, 0.32f, 0.52f, 0.72f, 0.88f, 0.42f, 0.62f)
        for ((i, lx) in launches.withIndex()) {
            val cycle = (t + i * 0.09f) % 1f
            val cx = w * lx
            val baseY = h * 0.92f
            val peakY = h * (0.18f + (i % 4) * 0.06f)
            val riseEnd = 0.38f
            val rise = (cycle / riseEnd).coerceIn(0f, 1f)
            val inv = 1f - rise
            val easeRise = 1f - inv * inv
            val cy = baseY + (peakY - baseY) * easeRise
            if (cycle < riseEnd * 0.98f) {
                drawCircle(color = Color(0xFFFFECB3), radius = 5f, center = Offset(cx, cy))
                drawLine(
                    color = Color(0xFFFFA000).copy(alpha = 0.55f),
                    start = Offset(cx, cy + 6f),
                    end = Offset(cx, cy + 28f),
                    strokeWidth = 3f,
                )
            } else {
                val burstT = ((cycle - riseEnd) / (1f - riseEnd)).coerceIn(0f, 1f)
                val rays = 14
                for (r in 0 until rays) {
                    val ang = r * 2f * PI.toFloat() / rays + i * 0.31f
                    val dist = burstT * w * 0.14f
                    val px = cx + cos(ang) * dist
                    val py = cy + sin(ang) * dist * 0.88f
                    val fade = (1f - burstT * 0.92f).coerceIn(0f, 1f)
                    val c = BurstColors[(i + r) % BurstColors.size].copy(alpha = fade)
                    drawCircle(color = c, radius = (5.5f * (1f - burstT * 0.55f)).coerceAtLeast(1.5f), center = Offset(px, py))
                }
                if (burstT > 0.12f) {
                    drawCircle(
                        color = Color.White.copy(alpha = (0.35f * (1f - burstT)).coerceIn(0f, 1f)),
                        radius = 18f * burstT,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f),
                    )
                }
            }
        }
    }
}
