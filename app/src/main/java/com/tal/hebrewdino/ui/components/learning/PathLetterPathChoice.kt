package com.tal.hebrewdino.ui.components.learning

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

internal fun cubicBezierLetterPath(
    t: Float,
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t
    return Offset(
        uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x,
        uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y,
    )
}

internal data class PathLetterLanePoints(
    val p0: Offset,
    val p1: Offset,
    val p2: Offset,
    val p3: Offset,
)

internal fun pathLetterLanePoints(
    w: Float,
    h: Float,
    pathCount: Int,
    laneIdx: Int,
): PathLetterLanePoints {
    val lanes = pathCount.coerceIn(2, 3)
    val y =
        when (lanes) {
            2 -> if (laneIdx == 0) h * 0.35f else h * 0.68f
            else -> listOf(h * 0.26f, h * 0.50f, h * 0.76f)[laneIdx]
        }
    val startX = w * 0.92f
    val endX = w * 0.14f
    val c1 = Offset(w * 0.70f, y + (if (laneIdx % 2 == 0) -h * 0.10f else h * 0.10f))
    val c2 = Offset(w * 0.40f, y + (if (laneIdx % 2 == 0) h * 0.08f else -h * 0.08f))
    return PathLetterLanePoints(Offset(startX, y), c1, c2, Offset(endX, y))
}

/**
 * Path mini-game with letter markers + walk-before-resolve (Chapter 2 and Chapter 3).
 */
@Composable
fun PathLetterPathChoice(
    options: List<String>,
    targetLetter: String,
    pathCount: Int,
    locked: Boolean,
    shakePx: Float,
    detourT: Float = 0f,
    shortcutLaneIndex: Int? = null,
    pathHazeAlpha: Float = 0f,
    enabled: Boolean,
    /** Return false to ignore the tap (e.g. shared tap cooldown) before the walk animation starts. */
    allowInteraction: () -> Boolean = { true },
    onPickResolved: (picked: String, correct: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var walking by remember { mutableStateOf(false) }
    val walkProgress = remember { Animatable(0f) }
    var walkerLane by remember { mutableIntStateOf(-1) }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(260.dp)
                .offset(x = shakePx.dp)
                .background(Color.White.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                .padding(14.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val startX = w * 0.92f
            val endX = w * 0.14f
            val lanes = pathCount.coerceIn(2, 3)
            val ys =
                when (lanes) {
                    2 -> listOf(h * 0.35f, h * 0.68f)
                    else -> listOf(h * 0.26f, h * 0.50f, h * 0.76f)
                }

            ys.forEachIndexed { idx, y ->
                val letterHere = options.getOrNull(idx).orEmpty()
                val isTargetLane = letterHere == targetLetter
                val laneVis = if (isTargetLane) 1f else 0.88f
                val p = Path()
                p.moveTo(startX, y)
                val c1 = Offset(w * 0.70f, y + (if (idx % 2 == 0) -h * 0.10f else h * 0.10f))
                val c2 = Offset(w * 0.40f, y + (if (idx % 2 == 0) h * 0.08f else -h * 0.08f))
                p.cubicTo(c1.x, c1.y, c2.x, c2.y, endX, y)

                drawPath(
                    path = p,
                    color = Color(0xFFE2C999).copy(alpha = 0.92f * laneVis),
                    style = Stroke(width = 44f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = p,
                    color = Color(0xFF6B4A2A).copy(alpha = 0.22f * laneVis),
                    style = Stroke(width = 50f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = p,
                    color = Color.White.copy(alpha = 0.18f * laneVis),
                    style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                if (!isTargetLane) {
                    repeat(5) { k ->
                        val tt = 0.2f + k * 0.15f
                        val pt =
                            cubicBezierLetterPath(tt, Offset(startX, y), c1, c2, Offset(endX, y))
                        drawCircle(
                            color = Color(0xFF6B4A2A).copy(alpha = 0.06f),
                            radius = 3f,
                            center = pt,
                        )
                    }
                }
            }

            if (detourT > 0.01f) {
                val y0 = h * 0.22f
                val p = Path()
                p.moveTo(w * 0.70f, y0)
                p.cubicTo(w * 0.60f, y0 + h * 0.10f, w * 0.46f, y0 + h * 0.20f, w * 0.34f, y0 + h * 0.24f)
                drawPath(
                    path = p,
                    color = Color(0xFF2AA6C9).copy(alpha = 0.12f * detourT),
                    style = Stroke(width = (24f * detourT).coerceAtLeast(1f), cap = StrokeCap.Round),
                )
            }

            if (pathHazeAlpha > 0.01f) {
                drawRect(color = Color(0xFFECEFF1).copy(alpha = 0.45f * pathHazeAlpha.coerceIn(0f, 1f)))
            }

            if (walking && walkerLane >= 0) {
                val g = pathLetterLanePoints(w, h, pathCount, walkerLane)
                val pt = cubicBezierLetterPath(walkProgress.value, g.p0, g.p1, g.p2, g.p3)
                drawCircle(
                    color = Color(0xFFFF8A65).copy(alpha = 0.92f),
                    radius = 10f,
                    center = pt,
                )
            }
        }

        val laneYs =
            if (pathCount == 2) listOf(0.28f, 0.62f) else listOf(0.20f, 0.46f, 0.74f)

        options.take(pathCount).forEachIndexed { idx, letter ->
            val yFrac = laneYs[idx]
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-14).dp, y = (yFrac * 240f).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (shortcutLaneIndex != null && shortcutLaneIndex == idx) {
                    Text(
                        text = "קיצור",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF1565C0),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Box(
                    modifier =
                        Modifier
                            .size(78.dp)
                            .background(
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color.White.copy(alpha = 0.95f),
                                            Color(0xFFFFF3C4).copy(alpha = 0.92f),
                                        ),
                                ),
                                shape = CircleShape,
                            )
                            .clickable(enabled = enabled && !locked && !walking) {
                                if (!allowInteraction()) return@clickable
                                scope.launch {
                                    walking = true
                                    walkerLane = idx
                                    walkProgress.snapTo(0f)
                                    val correct = letter == targetLetter
                                    val dur =
                                        if (correct) {
                                            LearningUxTiming.CorrectPathWalkMs
                                        } else {
                                            LearningUxTiming.WrongPathWalkMs
                                        }
                                    walkProgress.animateTo(1f, tween(dur, easing = LinearEasing))
                                    onPickResolved(letter, correct)
                                    walkProgress.snapTo(0f)
                                    walkerLane = -1
                                    walking = false
                                }
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                    )
                }
            }
        }
    }
}
