package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter1Config
import androidx.compose.ui.graphics.drawscope.Fill
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

private val walkFrames =
    listOf(
        R.drawable.dino_walk_0,
        R.drawable.dino_walk_1,
        R.drawable.dino_walk_2,
    )

/** Station points along the road (fractions of the road box). */
private val stationFractions: List<Pair<Float, Float>> =
    listOf(
        0.92f to 0.58f,
        0.76f to 0.42f,
        0.60f to 0.60f,
        0.44f to 0.44f,
        0.28f to 0.62f,
        0.16f to 0.46f,
    )

/** More “snake-like” road points for drawing + walking. */
private val roadFractions: List<Pair<Float, Float>> =
    listOf(
        0.96f to 0.62f,
        0.88f to 0.50f,
        0.80f to 0.70f,
        0.70f to 0.44f,
        0.60f to 0.66f,
        0.48f to 0.40f,
        0.38f to 0.68f,
        0.28f to 0.46f,
        0.18f to 0.64f,
        0.10f to 0.48f,
        0.04f to 0.54f, // goal point (egg) sits just after last station
    )

enum class JourneyEndMarker {
    Egg,
    HomeCave,
}

private fun xyAlongRoad(f: Float, points: List<Pair<Float, Float>>): Pair<Float, Float> {
    val last = points.lastIndex
    val maxF = last.toFloat()
    val ff = f.coerceIn(0f, maxF)
    val i = floor(ff.toDouble()).toInt().coerceAtMost(last - 1)
    val t = ff - i
    val (x0, y0) = points[i]
    val (x1, y1) = points[i + 1]
    return (x0 + (x1 - x0) * t) to (y0 + (y1 - y0) * t)
}

@Composable
fun JourneyScreen(
    unlockedLevel: Int,
    completedLevels: Set<Int>,
    onPlayLevel: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
    onDebugUnlockNext: (() -> Unit)? = null,
    totalLevels: Int = Chapter1Config.STATION_COUNT,
    headerTitle: String = "פרק 1 - מצא את הביצה",
    headerSubtitle: String? = null,
    endMarker: JourneyEndMarker = JourneyEndMarker.Egg,
    modifier: Modifier = Modifier,
) {
    val resolvedSubtitle = headerSubtitle ?: "הדרך לביצה — $totalLevels תחנות"
    val nextSuggested =
        (1..totalLevels).firstOrNull { !completedLevels.contains(it) } ?: (totalLevels + 1)
    val quickPlayLevel = nextSuggested.coerceAtMost(unlockedLevel)

    val scope = rememberCoroutineScope()
    var walking by remember { mutableStateOf(false) }
    var walkFrame by remember { mutableIntStateOf(0) }
    val dinoProgress = remember { Animatable(0f) }

    LaunchedEffect(walking) {
        while (walking) {
            delay(95)
            walkFrame = (walkFrame + 1) % walkFrames.size
        }
    }

    LaunchedEffect(nextSuggested, unlockedLevel, completedLevels) {
        if (walking) return@LaunchedEffect
        val target =
            if (nextSuggested <= totalLevels) {
                // Stand exactly on the next uncompleted station.
                (nextSuggested - 1).toFloat().coerceIn(0f, (totalLevels - 1).toFloat())
            } else {
                // All done: stand near the egg at the end of the road.
                roadFractions.lastIndex.toFloat()
            }
        dinoProgress.snapTo(target)
    }

    fun launchWalkThenPlay(targetLevel: Int) {
        if (walking) return
        if (targetLevel > unlockedLevel) return
        scope.launch {
            walking = true
            try {
                val dest = (targetLevel - 1).toFloat().coerceIn(0f, (totalLevels - 1).toFloat())
                val dist = abs(dinoProgress.value - dest)
                val ms = (380 + dist * 420f).roundToInt().coerceIn(450, 2400)
                dinoProgress.animateTo(dest, tween(ms, easing = LinearEasing))
                onPlayLevel(targetLevel)
            } finally {
                walking = false
            }
        }
    }

    val scroll = rememberScrollState()
    val roadScroll = rememberScrollState() // kept for API compatibility; no longer used for horizontal scroll.

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resolvedSubtitle,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onBack,
                    enabled = !walking,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) {
                    Text(text = "חזור")
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    enabled = !walking,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) {
                    Text(text = "הגדרות")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Temporary dev helper: unlock next station for quick testing.
            if (onDebugUnlockNext != null) {
                OutlinedButton(
                    onClick = onDebugUnlockNext,
                    enabled = !walking,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) {
                    Text("בדיקה: פתח תחנה הבאה")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { launchWalkThenPlay(quickPlayLevel) },
                modifier = Modifier.width(280.dp),
                enabled = !walking,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC400).copy(alpha = 0.95f),
                        contentColor = Color(0xFF0B2B3D),
                    ),
            ) {
                Text(
                    text = "שחק עכשיו (תחנה $quickPlayLevel)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            JourneyRoadStrip(
                totalLevels = totalLevels,
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                nextSuggested = nextSuggested,
                walking = walking,
                dinoProgress = dinoProgress.value,
                walkDrawable = walkFrames[walkFrame],
                roadScrollState = roadScroll,
                endMarker = endMarker,
                onStationClick = { levelId -> launchWalkThenPlay(levelId) },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun JourneyRoadStrip(
    totalLevels: Int,
    unlockedLevel: Int,
    completedLevels: Set<Int>,
    nextSuggested: Int,
    walking: Boolean,
    dinoProgress: Float,
    walkDrawable: Int,
    roadScrollState: ScrollState,
    endMarker: JourneyEndMarker,
    onStationClick: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val roadHeight = 300.dp

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(roadHeight + 36.dp)
    ) {
        val roadWidth = maxWidth
        // Shift the whole road slightly left (~0.5cm).
        Box(
            modifier =
                Modifier
                    .width(roadWidth)
                    .height(roadHeight)
                    .offset(x = (-20).dp)
        ) {
            val wPx = with(density) { roadWidth.toPx() }
            val hPx = with(density) { roadHeight.toPx() }

            // Draw a visible zig-zag road under the stations.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val pts = roadFractions.map { (fx, fy) -> Offset(fx * size.width, fy * size.height) }
                if (pts.isNotEmpty()) path.moveTo(pts.first().x, pts.first().y)
                // Smooth “snake” via quadratic segments.
                for (i in 0 until pts.size - 1) {
                    val p0 = pts[i]
                    val p1 = pts[i + 1]
                    val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
                    path.quadraticBezierTo(p0.x, p0.y, mid.x, mid.y)
                }
                // Finish to the last point.
                val last = pts.last()
                path.lineTo(last.x, last.y)

                // Medieval sandy road body + darker edges.
                drawPath(
                    path = path,
                    color = Color(0xFFE2C999).copy(alpha = 0.92f),
                    style = Stroke(width = 54f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                // Road edge
                drawPath(
                    path = path,
                    color = Color(0xFF6B4A2A).copy(alpha = 0.28f),
                    style = Stroke(width = 60f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                // Center highlight
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.20f),
                    style = Stroke(width = 26f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Light texture (pebbles) to feel “old road”.
                val dots = 65
                for (i in 0 until dots) {
                    val t = i.toFloat() / (dots - 1).toFloat()
                    val (px, py) = xyAlongRoad(t * (roadFractions.lastIndex.toFloat()), roadFractions)
                    val x = px * size.width + (if (i % 2 == 0) 18f else -14f)
                    val y = py * size.height + (if (i % 3 == 0) 10f else -8f)
                    drawCircle(
                        color = Color(0xFF6B4A2A).copy(alpha = 0.14f),
                        radius = if (i % 4 == 0) 6.5f else 4.5f,
                        center = Offset(x, y),
                    )
                }
            }

            for (levelId in 1..totalLevels) {
                val idx = (levelId - 1).coerceIn(0, stationFractions.lastIndex)
                val (fx, fy) = stationFractions[idx]
                val xPx = fx * wPx
                val yPx = fy * hPx
                val enabled = levelId <= unlockedLevel && !walking
                val completed = completedLevels.contains(levelId)
                val suggested = enabled && !completed && levelId == nextSuggested
                val isLast = levelId == totalLevels

                JourneyStationMarker(
                    levelId = levelId,
                    enabled = enabled,
                    completed = completed,
                    suggested = suggested,
                    isLast = isLast,
                    endMarker = endMarker,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset {
                                IntOffset(
                                    (xPx - 40.dp.toPx()).roundToInt(),
                                    (yPx - 40.dp.toPx()).roundToInt(),
                                )
                            },
                    onClick = { if (enabled) onStationClick(levelId) },
                )
            }

            val (dfx, dfy) = xyAlongRoad(dinoProgress, roadFractions)
            val dinoRes = if (walking) walkDrawable else R.drawable.dino_idle
            Image(
                painter = painterResource(id = dinoRes),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .size(88.dp)
                        .offset {
                            IntOffset(
                                (dfx * wPx - 44.dp.toPx()).roundToInt(),
                                (dfy * hPx - 70.dp.toPx()).roundToInt(),
                            )
                        },
                contentScale = ContentScale.Fit,
            )

            // Goal at end of the road (egg or cave home).
            val (gfx, gfy) = roadFractions.last()
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .size(118.dp, 102.dp)
                        .offset {
                            IntOffset(
                                (gfx * wPx - 18.dp.toPx()).roundToInt(),
                                (gfy * hPx - 88.dp.toPx()).roundToInt(),
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                when (endMarker) {
                    JourneyEndMarker.Egg ->
                        Image(
                            painter = painterResource(id = R.drawable.finish_marker_egg),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp),
                            contentScale = ContentScale.Fit,
                        )
                    JourneyEndMarker.HomeCave ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width * 0.52f
                            val cy = size.height * 0.62f
                            drawOval(
                                color = Color(0xFF3E2723),
                                topLeft = Offset(cx - size.width * 0.42f, cy - size.height * 0.18f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.84f, size.height * 0.50f),
                                style = Fill,
                            )
                            drawArc(
                                color = Color(0xFF1B120E),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = true,
                                topLeft = Offset(cx - size.width * 0.38f, cy - size.height * 0.55f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.72f),
                                style = Fill,
                            )
                            drawOval(
                                color = Color(0xFF0D0705).copy(alpha = 0.35f),
                                topLeft = Offset(cx - size.width * 0.22f, cy + size.height * 0.02f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.22f),
                                style = Fill,
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun JourneyStationMarker(
    levelId: Int,
    enabled: Boolean,
    completed: Boolean,
    suggested: Boolean,
    isLast: Boolean,
    endMarker: JourneyEndMarker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val markerRes =
        when {
            !enabled -> null
            isLast && completed ->
                when (endMarker) {
                    JourneyEndMarker.Egg -> R.drawable.finish_marker_egg
                    JourneyEndMarker.HomeCave -> R.drawable.egg_found
                }
            completed -> R.drawable.egg_found
            else -> R.drawable.stop_marker
        }

    val baseColor =
        when {
            !enabled -> Color(0xFF7E8A93).copy(alpha = 0.35f)
            completed -> Color(0xFF2E7D32).copy(alpha = 0.85f)
            suggested -> Color(0xFF2AA6C9).copy(alpha = 0.95f)
            else -> Color(0xFF2AA6C9).copy(alpha = 0.80f)
        }
    val borderColor =
        when {
            suggested -> Color(0xFFFFC400)
            enabled -> Color.White.copy(alpha = 0.85f)
            else -> Color.Transparent
        }
    val label =
        when {
            !enabled -> "🔒"
            completed -> "✓"
            else -> levelId.toString()
        }
    val subtitle =
        when {
            !enabled -> "נעול"
            completed -> "בוצע"
            else -> "תחנה $levelId"
        }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = baseColor,
            shape = CircleShape,
            shadowElevation = if (enabled) 8.dp else 0.dp,
            modifier =
                Modifier
                    .size(if (suggested) 88.dp else 80.dp)
                    .border(width = if (suggested) 4.dp else 2.dp, color = borderColor, shape = CircleShape)
                    .clickable(enabled = enabled, onClick = onClick),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (markerRes != null) {
                    Image(
                        painter = painterResource(id = markerRes),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(if (suggested) 64.dp else 56.dp)
                                .offset(y = (-2).dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier =
                Modifier
                    .width(88.dp)
                    .background(
                        when {
                            !enabled -> Color.White.copy(alpha = 0.60f)
                            suggested -> Color(0xFFFFF3C4).copy(alpha = 0.95f)
                            else -> Color.White.copy(alpha = 0.70f)
                        },
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
        }
    }
}
