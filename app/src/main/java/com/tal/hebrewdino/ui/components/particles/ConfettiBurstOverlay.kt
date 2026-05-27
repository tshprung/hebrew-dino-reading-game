package com.tal.hebrewdino.ui.components.particles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val BURST_DURATION_MS = 2_600L
private const val GRAVITY_PX_PER_S2 = 1_420f
private const val MAX_PARTICLES = 72

private enum class ConfettiShape {
    Square,
    Circle,
    Star,
}

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotationDeg: Float,
    var spinDegPerSec: Float,
    var sizePx: Float,
    var color: Color,
    var shape: ConfettiShape,
    var alpha: Float,
) {
    fun step(deltaSec: Float, width: Float, height: Float) {
        vy += GRAVITY_PX_PER_S2 * deltaSec
        x += vx * deltaSec
        y += vy * deltaSec
        rotationDeg += spinDegPerSec * deltaSec
        val life = (y / height).coerceIn(0f, 1.2f)
        alpha = (1f - life * 0.85f).coerceIn(0f, 1f)
        if (x < -40f || x > width + 40f || y > height + 60f) {
            alpha = 0f
        }
    }

    fun draw(scope: DrawScope) {
        if (alpha <= 0.01f) return
        val c = color.copy(alpha = alpha)
        scope.rotate(rotationDeg, pivot = Offset(x, y)) {
            when (shape) {
                ConfettiShape.Square ->
                    scope.drawRect(
                        color = c,
                        topLeft = Offset(x - sizePx / 2f, y - sizePx / 2f),
                        size = Size(sizePx, sizePx),
                    )
                ConfettiShape.Circle ->
                    scope.drawCircle(color = c, radius = sizePx / 2f, center = Offset(x, y))
                ConfettiShape.Star -> drawStar(scope, x, y, sizePx, c)
            }
        }
    }
}

private fun drawStar(
    scope: DrawScope,
    cx: Float,
    cy: Float,
    size: Float,
    color: Color,
) {
    val path = Path()
    val outer = size * 0.55f
    val inner = size * 0.22f
    for (i in 0 until 10) {
        val angle = Math.PI / 2 + i * Math.PI / 5
        val r = if (i % 2 == 0) outer else inner
        val px = cx + (cos(angle) * r).toFloat()
        val py = cy - (sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    scope.drawPath(path, color)
}

private fun spawnBurst(
    width: Float,
    height: Float,
    rng: Random,
): List<ConfettiParticle> {
    val palette =
        listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFFE66D),
            Color(0xFF4ECDC4),
            Color(0xFF95E1D3),
            Color(0xFFFF9F1C),
            Color(0xFF9B5DE5),
            Color(0xFFF15BB5),
            Color(0xFF00BBF9),
        )
    return List(MAX_PARTICLES) {
        val shape = ConfettiShape.entries[rng.nextInt(ConfettiShape.entries.size)]
        ConfettiParticle(
            x = rng.nextFloat() * width,
            y = -rng.nextFloat() * height * 0.15f,
            vx = (rng.nextFloat() - 0.5f) * 280f,
            vy = 120f + rng.nextFloat() * 320f,
            rotationDeg = rng.nextFloat() * 360f,
            spinDegPerSec = (rng.nextFloat() - 0.5f) * 420f,
            sizePx = 8f + rng.nextFloat() * 14f,
            color = palette[rng.nextInt(palette.size)],
            shape = shape,
            alpha = 1f,
        )
    }
}

/** Full-screen confetti burst; increment [trigger] to replay. */
@Composable
fun ConfettiBurstOverlay(
    trigger: Int,
    modifier: Modifier = Modifier,
    durationMs: Long = BURST_DURATION_MS,
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(trigger, widthPx, heightPx) {
            if (trigger <= 0 || widthPx <= 1f || heightPx <= 1f) return@LaunchedEffect
            val rng = Random(trigger * 31_337)
            particles = spawnBurst(widthPx, heightPx, rng)
            var lastFrameMs = withFrameMillis { it }
            val endMs = lastFrameMs + durationMs
            while (lastFrameMs < endMs) {
                val frameMs = withFrameMillis { it }
                val deltaSec = ((frameMs - lastFrameMs).coerceAtLeast(1L)).toFloat() / 1000f
                lastFrameMs = frameMs
                val alive =
                    particles
                        .onEach { it.step(deltaSec, widthPx, heightPx) }
                        .filter { it.alpha > 0.02f }
                particles = alive
                if (alive.isEmpty()) break
            }
            particles = emptyList()
        }

        if (particles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { it.draw(this) }
            }
        }
    }
}

fun shouldShowConfettiForCue(cue: com.tal.hebrewdino.ui.domain.economy.ParticleCue?): Boolean =
    when (cue) {
        com.tal.hebrewdino.ui.domain.economy.ParticleCue.STATION_APPLE_BURST,
        com.tal.hebrewdino.ui.domain.economy.ParticleCue.LEVEL_COMPLETE_SPARKLE,
        com.tal.hebrewdino.ui.domain.economy.ParticleCue.CONFETTI_BURST,
        -> true
        null -> true
    }
