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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.components.learning.DinoNestMark
import com.tal.hebrewdino.ui.components.learning.StoryEggStrip
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.JourneyEndMarkerIdle
import com.tal.hebrewdino.ui.domain.JourneyMapLayout
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/** ~1 cm gap under the back button before the egg strip. */
private val JourneySpaceBelowBackBeforeEggs = 38.dp

/** Space between journey header row and title (matches chapters top breathing room). */
private val JourneyHeaderToBodyGap = 8.dp

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
    PinkEgg,
    PurpleEgg,
    Tracks,
    /** Chapter 4: distinct goal after all six stations (smart reinforcement arc). */
    BigEgg,
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

/**
 * Dino progress lives on **segments between station chips** (same fractions as markers), not on the
 * decorative road polyline (which uses a different curve), so walks always end at each station.
 * [f] is in 0..[stationPts.lastIndex]; integer values sit on each station.
 */
private fun xyAlongStationChain(f: Float, stationPts: List<Pair<Float, Float>>): Pair<Float, Float> {
    if (stationPts.isEmpty()) return 0f to 0f
    if (stationPts.size == 1) return stationPts[0]
    val maxF = (stationPts.size - 1).toFloat()
    val ff = f.coerceIn(0f, maxF)
    val i = floor(ff.toDouble()).toInt().coerceAtMost(stationPts.size - 2)
    val t = ff - i
    val (x0, y0) = stationPts[i]
    val (x1, y1) = stationPts[i + 1]
    return (x0 + (x1 - x0) * t) to (y0 + (y1 - y0) * t)
}

@Composable
fun JourneyScreen(
    unlockedLevel: Int,
    completedLevels: Set<Int>,
    onPlayLevel: (Int) -> Unit,
    onBack: () -> Unit,
    /** When true, the end marker (egg/cave) has already been reached in a prior visit; do not auto-walk again. */
    endMarkerReached: Boolean = false,
    /** When true (episode finale), navigate only after Dino reaches the end marker. */
    endWalkThenContinue: Boolean = false,
    /** Called once after the end-walk completes (only when [endWalkThenContinue] is true). */
    onEndWalkComplete: (() -> Unit)? = null,
    onDebugUnlockNext: (() -> Unit)? = null,
    onLettersHelp: (() -> Unit)? = null,
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
    backgroundRes: Int = R.drawable.forest_bg_journey_road,
    /** Eggs already collected in prior chapter finales (shown under the top bar). */
    collectedEggStripCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val resolvedSubtitle = headerSubtitle // when null, hide subtitle (chapter 1 request)
    val nextPlayableSuggested =
        (1..playableLevels).firstOrNull { !completedLevels.contains(it) } ?: (playableLevels + 1)
    val allPlayableComplete = (1..playableLevels).all { level -> completedLevels.contains(level) }
    /** Chapter 1 finale route: block entering stations / quick-play until outro navigation runs. */
    val journeyNavigationLocked = endWalkThenContinue
    // Only add an “end marker walk” when this journey is fully playable (chapter end).
    val canWalkToEndMarker = (playableLevels == totalLevels) && allPlayableComplete
    val baseMaxDinoF = (minOf(totalLevels, stationFractions.size) - 1).coerceAtLeast(0).toFloat()
    val maxDinoF = baseMaxDinoF + if (canWalkToEndMarker) 1f else 0f
    fun idleDinoProgressAlongRoad(): Float =
        if (nextPlayableSuggested <= playableLevels) {
            // On first entry (or if state is lost), start from the previous completed station
            // so the auto-walk has visible motion after a station finishes.
            if (nextPlayableSuggested > 1) {
                (nextPlayableSuggested - 2).toFloat().coerceIn(0f, maxDinoF)
            } else {
                0f
            }
        } else {
            JourneyEndMarkerIdle.idleProgressAfterAllPlayableStationsComplete(
                canWalkToEndMarker = canWalkToEndMarker,
                endMarkerReached = endMarkerReached,
                baseMaxDinoF = baseMaxDinoF,
                maxDinoF = maxDinoF,
            )
        }
    val quickPlayLevel =
        nextPlayableSuggested.coerceAtMost(unlockedLevel).coerceAtMost(playableLevels)
    val completedPlayableCount = completedLevels.count { it in 1..playableLevels }
    val goalSegmentComplete =
        playableLevels < totalLevels &&
            allPlayableComplete

    val scope = rememberCoroutineScope()
    var walking by remember { mutableStateOf(false) }
    var walkFrame by remember { mutableIntStateOf(0) }
    // Persist dino position across navigating into/out of stations.
    var savedProgress by rememberSaveable { mutableStateOf<Float?>(null) }
    val dinoProgress = remember(maxDinoF) {
        val raw = savedProgress
        val idle = idleDinoProgressAlongRoad()
        Animatable((raw ?: idle).coerceIn(0f, maxDinoF))
    }
    // Not saveable: if we come back into this route, we should be able to trigger again.
    var endWalkFired by remember(endWalkThenContinue) { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (savedProgress == null) savedProgress = dinoProgress.value.coerceIn(0f, maxDinoF)
    }
    LaunchedEffect(dinoProgress.value) {
        savedProgress = dinoProgress.value.coerceIn(0f, maxDinoF)
    }

    LaunchedEffect(walking) {
        while (walking) {
            delay(95)
            walkFrame = (walkFrame + 1) % walkFrames.size
        }
    }

    // Auto-walk after a station is completed: stand near the next station (and after station 6, walk to the end marker).
    LaunchedEffect(nextPlayableSuggested, completedLevels, unlockedLevel, playableLevels, maxDinoF, canWalkToEndMarker, baseMaxDinoF, endMarkerReached) {
        if (walking) return@LaunchedEffect
        if (JourneyEndMarkerIdle.suppressRepeatEndMarkerAutoWalk(endMarkerReached, canWalkToEndMarker)) return@LaunchedEffect
        val target = nextPlayableSuggested
        val dest =
            when {
                // Normal station-to-station walk
                target in 2..playableLevels &&
                    target <= unlockedLevel &&
                    completedLevels.contains(target - 1) -> (target - 1).toFloat().coerceIn(0f, maxDinoF)
                // Chapter end: after finishing the last station, walk to the end marker (egg / cave).
                canWalkToEndMarker &&
                    target == (playableLevels + 1) &&
                    completedLevels.contains(playableLevels) -> maxDinoF
                else -> return@LaunchedEffect
            }
        val from = dinoProgress.value
        if (abs(from - dest) < 0.03f) return@LaunchedEffect
        walking = true
        try {
            val dist = abs(from - dest)
            val ms = (320 + dist * 520f).roundToInt().coerceIn(420, 1700)
            dinoProgress.animateTo(dest, tween(ms, easing = LinearEasing))
        } finally {
            walking = false
        }
    }

    // Finale flow: once Dino finishes walking to the end marker, advance to the outro screen.
    LaunchedEffect(endWalkThenContinue, canWalkToEndMarker, maxDinoF) {
        if (!endWalkThenContinue) return@LaunchedEffect
        if (endWalkFired) return@LaunchedEffect
        if (!canWalkToEndMarker) return@LaunchedEffect

        snapshotFlow {
            !walking && abs(dinoProgress.value - maxDinoF) <= 0.12f
        }.filter { it }.first()

        if (endWalkFired) return@LaunchedEffect
        endWalkFired = true
        delay(450)
        onEndWalkComplete?.invoke()
    }

    val roadScroll = rememberScrollState() // API compat; not used for horizontal scroll.
    val journeyNavChipColors = ChapterNavChipStyles.outlinedButtonColors()
    val journeyNavChipTextStyle = ChapterNavChipStyles.labelTextStyle()

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // RTL Row: physical right = חזור + ביצים, center = כותרת + שחק + אותיות, physical left = בדיקה.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedButton(onClick = onBack, enabled = true, colors = journeyNavChipColors) {
                        Text(text = "חזור", style = journeyNavChipTextStyle)
                    }
                    if (collectedEggStripCount > 0) {
                        Spacer(modifier = Modifier.height(JourneySpaceBelowBackBeforeEggs))
                        StoryEggStrip(foundCount = collectedEggStripCount)
                    }
                }
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!resolvedSubtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resolvedSubtitle,
                            modifier = Modifier.fillMaxWidth(),
                            style =
                                if (headerSubtitleCompact) {
                                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                            color = Color(0xFF0B2B3D).copy(alpha = if (headerSubtitleCompact) 0.78f else 1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { if (!walking && !journeyNavigationLocked) onPlayLevel(quickPlayLevel) },
                        modifier = Modifier.wrapContentWidth().height(56.dp),
                        enabled = !walking && !journeyNavigationLocked,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC400).copy(alpha = 0.95f),
                                contentColor = Color(0xFF0B2B3D),
                            ),
                    ) {
                        Text(
                            text = "שחק עכשיו (תחנה $quickPlayLevel)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (onLettersHelp != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onLettersHelp,
                            enabled = !walking && !journeyNavigationLocked,
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.86f),
                                    contentColor = Color(0xFF0B2B3D),
                                ),
                            modifier = Modifier.wrapContentWidth().height(44.dp),
                        ) {
                            Text("אותיות", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                if (onDebugUnlockNext != null) {
                    OutlinedButton(onClick = onDebugUnlockNext, enabled = true, colors = journeyNavChipColors) {
                        Text("בדיקה", style = journeyNavChipTextStyle)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }

            Spacer(modifier = Modifier.height(JourneyHeaderToBodyGap))

            BoxWithConstraints(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            ) {
                val roadH = (maxHeight - 8.dp).coerceIn(156.dp, 292.dp)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    JourneyRoadStrip(
                        totalLevels = totalLevels,
                        playableLevels = playableLevels,
                        unlockedLevel = unlockedLevel,
                        completedLevels = completedLevels,
                        nextSuggested = nextPlayableSuggested,
                        completedPlayableCount = completedPlayableCount,
                        goalSegmentComplete = goalSegmentComplete,
                        walking = walking,
                        navigationLocked = journeyNavigationLocked,
                        dinoProgress = dinoProgress.value,
                        walkDrawable = walkFrames[walkFrame],
                        companionImageRes = companionImageRes,
                        roadScrollState = roadScroll,
                        endMarker = endMarker,
                        roadHeight = roadH,
                        onStationClick = { levelId ->
                            if (!walking && !journeyNavigationLocked) onPlayLevel(levelId)
                        },
                    )
                }
            }
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
    /** When true (e.g. chapter 1 finale walk → outro), do not open any station from the map. */
    navigationLocked: Boolean = false,
    dinoProgress: Float,
    walkDrawable: Int,
    companionImageRes: Int?,
    roadScrollState: ScrollState,
    endMarker: JourneyEndMarker,
    roadHeight: Dp = 300.dp,
    onStationClick: (Int) -> Unit,
) {
    val density = LocalDensity.current

    // Use a physical LTR coordinate space for x-offset math, while the overall app stays RTL.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(roadHeight + 36.dp),
        ) {
            val roadWidth = maxWidth
            // Shift the whole road left (~1cm).
            Box(
                modifier =
                    Modifier
                        .width(roadWidth)
                        .height(roadHeight)
                        .offset(x = (-38).dp),
            ) {
                val roadPts =
                    roadFractions
                val stationPts =
                    stationFractions
            val wPx = with(density) { roadWidth.toPx() }
            val hPx = with(density) { roadHeight.toPx() }
            val edgeMarginPx = with(density) { 38.dp.toPx() } // ~1cm
            val markerHalfPx = with(density) { 40.dp.toPx() }
            val maxCenterPx = (wPx - edgeMarginPx - markerHalfPx).coerceAtLeast(markerHalfPx)

            // Draw a visible zig-zag road under the stations.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val pts = roadPts.map { (fx, fy) -> Offset(fx * size.width, fy * size.height) }
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
                    val (px, py) = xyAlongRoad(t * (roadPts.lastIndex.toFloat()), roadPts)
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
                    val (px, py) = xyAlongRoad(t * roadPts.lastIndex.toFloat(), roadPts)
                    drawCircle(
                        color = Color(0xFFFFF59D).copy(alpha = if (hasFutureTrack) 0.12f else 0.22f),
                        radius = if (hasFutureTrack) 3.5f else 5f,
                        center = Offset(px * size.width, py * size.height),
                    )
                }

                if (hasFutureTrack) {
                    val anchorIdx = (playableLevels - 1).coerceIn(0, stationPts.lastIndex)
                    val (_, fy) = stationPts[anchorIdx]
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

            val (gfx, gfy) = roadPts.last()
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
                            painter = painterResource(id = R.drawable.egg_white),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp).scale(goalPulse),
                            contentScale = ContentScale.Fit,
                        )
                    JourneyEndMarker.HomeCave ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.scale(goalPulse)) {
                                DinoNestMark()
                            }
                        }
                    JourneyEndMarker.PinkEgg ->
                        Image(
                            painter = painterResource(id = R.drawable.egg_pink),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp).scale(goalPulse),
                            contentScale = ContentScale.Fit,
                        )
                    JourneyEndMarker.PurpleEgg ->
                        Image(
                            painter = painterResource(id = R.drawable.egg_purple),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp).scale(goalPulse),
                            contentScale = ContentScale.Fit,
                        )
                    JourneyEndMarker.Tracks ->
                        Image(
                            painter = painterResource(id = R.drawable.finish_marker_tracks),
                            contentDescription = null,
                            modifier = Modifier.size(104.dp).scale(goalPulse),
                            contentScale = ContentScale.Fit,
                        )
                    JourneyEndMarker.BigEgg ->
                        Image(
                            painter = painterResource(id = R.drawable.finish_marker_big_egg),
                            contentDescription = null,
                            modifier = Modifier.size(118.dp).scale(goalPulse),
                            contentScale = ContentScale.Fit,
                        )
                }
            }

            for (levelId in 1..totalLevels) {
                val idx = (levelId - 1).coerceIn(0, stationFractions.lastIndex)
                val (fx, fy) = stationPts[idx]
                val xPxRaw = fx * wPx
                val xPx = if (levelId == 1) xPxRaw.coerceAtMost(maxCenterPx) else xPxRaw
                val yPx = fy * hPx
                val playable = levelId <= playableLevels
                val completed = completedLevels.contains(levelId)
                val interactive =
                    playable &&
                        levelId <= unlockedLevel &&
                        !walking &&
                        !navigationLocked &&
                        (levelId == nextSuggested || completed)
                val lockedWaiting =
                    playable &&
                        levelId <= unlockedLevel &&
                        !completed &&
                        levelId > nextSuggested
                val suggested = interactive && !completed && levelId == nextSuggested
                val isLast = levelId == totalLevels

                JourneyStationMarker(
                    levelId = levelId,
                    playableLevels = playableLevels,
                    interactive = interactive,
                    lockedWaiting = lockedWaiting,
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
                    onClick = { if (interactive) onStationClick(levelId) },
                )
            }

            // Dino follows segments between station chips; after the last station is completed, add one extra
            // “goal” point so he can walk to the end marker (egg / cave) and stand there.
            val allPlayableCompleteLocal = (1..playableLevels).all { level -> completedLevels.contains(level) }
            val canWalkToEndMarkerLocal = (playableLevels == totalLevels) && allPlayableCompleteLocal
            val chainPts =
                if (canWalkToEndMarkerLocal && roadPts.isNotEmpty()) {
                    stationPts.take(totalLevels).plus(roadPts.last())
                } else {
                    stationPts.take(totalLevels)
                }
            val dinoFMax = (chainPts.size - 1).coerceAtLeast(0).toFloat()
            val (dfx, dfy) = xyAlongStationChain(dinoProgress.coerceIn(0f, dinoFMax), chainPts)
            val dinoRes = if (walking) walkDrawable else R.drawable.dino_idle
            // Keep the same screen offset while walking and while idle. Previously walking used the
            // path centerline but idle added an extra X/Y offset, so when the walk finished the dino
            // snapped “back” toward station 1 (road runs toward decreasing x; +X is that direction).
            val dinoX =
                with(density) {
                    // Match station-1 chip clamp even while walking, so idle → walk does not jump right.
                    val dxRaw = dfx * wPx
                    val dx = dxRaw.coerceAtMost(maxCenterPx)
                    // How far to the right of the station chip Dino should wait.
                    val waitOffsetPx = 66.dp.toPx()
                    (dx - 44.dp.toPx() + waitOffsetPx).roundToInt()
                }
            val dinoY =
                with(density) {
                    val dy = dfy * hPx
                    // Slightly lower so feet sit on the road (same offset while walking so no end snap).
                    (dy - 64.dp.toPx() + 26.dp.toPx()).roundToInt()
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
}

@Composable
private fun JourneyStationMarker(
    levelId: Int,
    playableLevels: Int,
    interactive: Boolean,
    lockedWaiting: Boolean,
    completed: Boolean,
    suggested: Boolean,
    isLast: Boolean,
    endMarker: JourneyEndMarker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFutureTrack = levelId > playableLevels
    val gatedOff = !interactive && !completed && !isFutureTrack
    val markerRes =
        when {
            gatedOff || isFutureTrack -> null
            isLast && completed ->
                when (endMarker) {
                    JourneyEndMarker.Egg -> R.drawable.egg_white
                    JourneyEndMarker.HomeCave -> R.drawable.egg_found
                    JourneyEndMarker.PinkEgg -> R.drawable.egg_pink
                    JourneyEndMarker.PurpleEgg -> R.drawable.egg_purple
                    JourneyEndMarker.Tracks -> R.drawable.finish_marker_tracks
                    JourneyEndMarker.BigEgg -> R.drawable.finish_marker_big_egg
                }
            completed -> R.drawable.egg_found
            else -> R.drawable.stop_marker
        }

    val baseColor =
        when {
            isFutureTrack -> Color(0xFF5D6A73).copy(alpha = 0.19f)
            lockedWaiting -> Color(0xFF7E8A93).copy(alpha = 0.38f)
            completed -> Color(0xFF2E7D32).copy(alpha = 0.85f)
            suggested -> Color(0xFF2AA6C9).copy(alpha = 0.95f)
            interactive -> Color(0xFF2AA6C9).copy(alpha = 0.80f)
            else -> Color(0xFF7E8A93).copy(alpha = 0.35f)
        }
    val borderColor =
        when {
            suggested -> Color(0xFFFFC400)
            interactive -> Color.White.copy(alpha = 0.85f)
            else -> Color.Transparent
        }
    val label =
        when {
            isFutureTrack -> ""
            completed -> "✓"
            lockedWaiting -> "🔒"
            !interactive -> "🔒"
            else -> levelId.toString()
        }
    val subtitle =
        when {
            isFutureTrack -> "בהמשך"
            lockedWaiting -> "עוד לא"
            !interactive && !completed -> "נעול"
            completed -> "בוצע"
            else -> "תחנה $levelId"
        }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = baseColor,
            shape = CircleShape,
            shadowElevation = if (interactive) 8.dp else 0.dp,
            modifier =
                Modifier
                    .size(if (suggested) 88.dp else 80.dp)
                    .border(width = if (suggested) 4.dp else 2.dp, color = borderColor, shape = CircleShape)
                    .clickable(enabled = interactive, onClick = onClick),
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
                            .width(if (completed) 104.dp else 88.dp)
                            .background(
                                when {
                                    isFutureTrack -> Color.White.copy(alpha = 0.36f)
                                    lockedWaiting -> Color.White.copy(alpha = 0.52f)
                                    !interactive -> Color.White.copy(alpha = 0.60f)
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
                    run {
                        val base = MaterialTheme.typography.bodySmall
                        when {
                            completed ->
                                base.copy(
                                    fontSize = base.fontSize * 2,
                                    lineHeight = base.lineHeight * 2,
                                    fontWeight = FontWeight.Bold,
                                )
                            isFutureTrack -> base.copy(fontWeight = FontWeight.Medium)
                            else -> base.copy(fontWeight = FontWeight.Bold)
                        }
                    },
                color = Color(0xFF0B2B3D).copy(alpha = if (isFutureTrack) 0.48f else 1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
