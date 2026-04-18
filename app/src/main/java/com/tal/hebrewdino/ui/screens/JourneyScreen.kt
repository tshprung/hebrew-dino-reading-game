package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.tal.hebrewdino.ui.components.learning.CaveHomeMark
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.JourneyMapLayout
import androidx.compose.ui.draw.scale
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

private val stationFractions = JourneyMapLayout.stationFractions

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
    onBack: () -> Unit,
    onDebugUnlockNext: (() -> Unit)? = null,
    totalLevels: Int = Chapter1Config.STATION_COUNT,
    /** Stations above this are shown locked until content ships (e.g. chapter 3). */
    playableLevels: Int = totalLevels,
    headerTitle: String = "פרק 1 - מצא את הביצה",
    headerSubtitle: String? = null,
    /** Softer, slightly smaller subtitle so long lines wrap calmly (e.g. chapter 3). */
    headerSubtitleCompact: Boolean = false,
    /** Optional second character drawn beside Dino (e.g. mom in chapter 2). */
    companionImageRes: Int? = null,
    endMarker: JourneyEndMarker = JourneyEndMarker.Egg,
    modifier: Modifier = Modifier,
) {
    val resolvedSubtitle = headerSubtitle ?: "הדרך לביצה — $totalLevels תחנות"
    val nextPlayableSuggested =
        (1..playableLevels).firstOrNull { !completedLevels.contains(it) } ?: (playableLevels + 1)
    fun idleDinoProgressAlongRoad(): Float =
        if (nextPlayableSuggested <= playableLevels) {
            if (nextPlayableSuggested > 1) {
                (nextPlayableSuggested - 2).toFloat().coerceIn(0f, (totalLevels - 1).toFloat().coerceAtLeast(1f))
            } else {
                0f
            }
        } else {
            (roadFractions.lastIndex * 0.88f).coerceAtLeast((playableLevels - 1).coerceAtLeast(1) - 1f)
        }
    val quickPlayLevel =
        nextPlayableSuggested.coerceAtMost(unlockedLevel).coerceAtMost(playableLevels)
    val completedPlayableCount = completedLevels.count { it in 1..playableLevels }
    val goalSegmentComplete =
        playableLevels < totalLevels &&
            (1..playableLevels).all { level -> completedLevels.contains(level) }

    val scope = rememberCoroutineScope()
    var walking by remember { mutableStateOf(false) }
    var walkFrame by remember { mutableIntStateOf(0) }
    val dinoProgress =
        remember(completedLevels, unlockedLevel, playableLevels, totalLevels, nextPlayableSuggested) {
            Animatable(idleDinoProgressAlongRoad())
        }

    LaunchedEffect(walking) {
        while (walking) {
            delay(95)
            walkFrame = (walkFrame + 1) % walkFrames.size
        }
    }

    fun launchWalkThenPlay(targetLevel: Int) {
        if (walking) return
        if (targetLevel > unlockedLevel) return
        if (targetLevel > playableLevels) return
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
                modifier = Modifier.fillMaxWidth(),
                style =
                    if (headerSubtitleCompact) {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                color = Color(0xFF0B2B3D).copy(alpha = if (headerSubtitleCompact) 0.78f else 1f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
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
                playableLevels = playableLevels,
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                nextSuggested = nextPlayableSuggested,
                completedPlayableCount = completedPlayableCount,
                goalSegmentComplete = goalSegmentComplete,
                walking = walking,
                dinoProgress = dinoProgress.value,
                walkDrawable = walkFrames[walkFrame],
                companionImageRes = companionImageRes,
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
    playableLevels: Int,
    unlockedLevel: Int,
    completedLevels: Set<Int>,
    nextSuggested: Int,
    completedPlayableCount: Int,
    /** All playable stations finished while more stations exist on the map (e.g. chapter 3). */
    goalSegmentComplete: Boolean,
    walking: Boolean,
    dinoProgress: Float,
    walkDrawable: Int,
    companionImageRes: Int?,
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
                // Center highlight — slightly clearer as the child finishes playable stations.
                val clarityBoost = (completedPlayableCount.coerceAtMost(playableLevels)) * 0.028f
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = (0.20f + clarityBoost).coerceAtMost(0.32f)),
                    style = Stroke(width = 26f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Light texture (pebbles) to feel “old road”.
                val hasFutureTrack = playableLevels < totalLevels
                val dots = if (hasFutureTrack) 38 else 65
                val pebbleAlpha =
                    (0.14f - completedPlayableCount * 0.008f).coerceIn(0.08f, 0.14f) -
                        if (hasFutureTrack) 0.02f else 0f
                for (i in 0 until dots) {
                    val t = i.toFloat() / (dots - 1).toFloat()
                    val (px, py) = xyAlongRoad(t * (roadFractions.lastIndex.toFloat()), roadFractions)
                    val x = px * size.width + (if (i % 2 == 0) 18f else -14f)
                    val y = py * size.height + (if (i % 3 == 0) 10f else -8f)
                    drawCircle(
                        color = Color(0xFF6B4A2A).copy(alpha = pebbleAlpha.coerceIn(0.06f, 0.12f)),
                        radius = if (i % 4 == 0) 6f else 4f,
                        center = Offset(x, y),
                    )
                }

                // One subtle “sparkle” per completed playable station (cumulative, capped).
                val sparkCap = if (hasFutureTrack) 1 else 6
                val sparkles = completedPlayableCount.coerceIn(0, sparkCap)
                repeat(sparkles) { k ->
                    val t = 0.15f + k * 0.11f
                    val (px, py) = xyAlongRoad(t * roadFractions.lastIndex.toFloat(), roadFractions)
                    drawCircle(
                        color = Color(0xFFFFF59D).copy(alpha = if (hasFutureTrack) 0.12f else 0.22f),
                        radius = if (hasFutureTrack) 3.5f else 5f,
                        center = Offset(px * size.width, py * size.height),
                    )
                }

                if (hasFutureTrack) {
                    val anchorIdx = (playableLevels - 1).coerceIn(0, stationFractions.lastIndex)
                    val (_, fy) = stationFractions[anchorIdx]
                    val startY = (size.height * (fy + 0.05f)).coerceIn(0f, size.height * 0.88f)
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF1B2F3A).copy(alpha = 0.11f),
                                    ),
                                startY = startY,
                                endY = size.height,
                            ),
                        topLeft = Offset(0f, startY),
                        size = Size(size.width, size.height - startY),
                    )
                }
            }

            val (gfx, gfy) = roadFractions.last()
            val goalPulse by rememberInfiniteTransition(label = "goal").animateFloat(
                initialValue = if (goalSegmentComplete) 0.94f else 0.98f,
                targetValue = if (goalSegmentComplete) 1.08f else 1.04f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(if (goalSegmentComplete) 1350 else 1600),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "goalPulse",
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .size(118.dp, 102.dp)
                        .offset {
                            with(density) {
                                IntOffset(
                                    (gfx * wPx - 18.dp.toPx()).roundToInt(),
                                    (gfy * hPx - 88.dp.toPx()).roundToInt(),
                                )
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                when (endMarker) {
                    JourneyEndMarker.Egg ->
                        Image(
                            painter = painterResource(id = R.drawable.finish_marker_egg),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp).scale(goalPulse),
                            contentScale = ContentScale.Fit,
                        )
                    JourneyEndMarker.HomeCave ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.scale(goalPulse)) {
                                CaveHomeMark()
                            }
                        }
                }
            }

            for (levelId in 1..totalLevels) {
                val idx = (levelId - 1).coerceIn(0, stationFractions.lastIndex)
                val (fx, fy) = stationFractions[idx]
                val xPx = fx * wPx
                val yPx = fy * hPx
                val playable = levelId <= playableLevels
                val enabled = playable && levelId <= unlockedLevel && !walking
                val completed = completedLevels.contains(levelId)
                val suggested = enabled && !completed && levelId == nextSuggested
                val isLast = levelId == totalLevels

                JourneyStationMarker(
                    levelId = levelId,
                    playableLevels = playableLevels,
                    enabled = enabled,
                    completed = completed,
                    suggested = suggested,
                    isLast = isLast,
                    endMarker = endMarker,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset {
                                with(density) {
                                    IntOffset(
                                        (xPx - 40.dp.toPx()).roundToInt(),
                                        (yPx - 40.dp.toPx()).roundToInt(),
                                    )
                                }
                            },
                    onClick = { if (enabled) onStationClick(levelId) },
                )
            }

            val (dfx, dfy) = xyAlongRoad(dinoProgress, roadFractions)
            val dinoRes = if (walking) walkDrawable else R.drawable.dino_idle
            val dinoX =
                with(density) {
                    (dfx * wPx - 44.dp.toPx()).roundToInt()
                }
            val dinoY =
                with(density) {
                    (dfy * hPx - 70.dp.toPx()).roundToInt()
                }
            if (companionImageRes != null) {
                Image(
                    painter = painterResource(id = companionImageRes),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .size(80.dp)
                            .offset {
                                with(density) {
                                    IntOffset(
                                        dinoX - 52.dp.roundToPx(),
                                        dinoY + 6.dp.roundToPx(),
                                    )
                                }
                            },
                    contentScale = ContentScale.Fit,
                )
            }
            Image(
                painter = painterResource(id = dinoRes),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .size(88.dp)
                        .offset { IntOffset(dinoX, dinoY) },
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun JourneyStationMarker(
    levelId: Int,
    playableLevels: Int,
    enabled: Boolean,
    completed: Boolean,
    suggested: Boolean,
    isLast: Boolean,
    endMarker: JourneyEndMarker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFutureTrack = levelId > playableLevels
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
            isFutureTrack -> Color(0xFF5D6A73).copy(alpha = 0.19f)
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
            isFutureTrack -> ""
            !enabled -> "🔒"
            completed -> "✓"
            else -> levelId.toString()
        }
    val subtitle =
        when {
            isFutureTrack -> "בהמשך"
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
                } else if (isFutureTrack) {
                    Box(
                        modifier =
                            Modifier
                                .size(5.dp)
                                .background(Color.White.copy(alpha = 0.16f), CircleShape),
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
                            isFutureTrack -> Color.White.copy(alpha = 0.36f)
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
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isFutureTrack) FontWeight.Medium else FontWeight.Bold,
                    ),
                color = Color(0xFF0B2B3D).copy(alpha = if (isFutureTrack) 0.48f else 1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
