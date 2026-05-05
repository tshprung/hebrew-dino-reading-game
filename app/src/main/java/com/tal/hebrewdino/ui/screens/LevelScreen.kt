package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.key
import androidx.compose.runtime.withFrameNanos
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.Chapter1LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    onComplete: (levelId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    /** When replaying a station already marked complete, skip the in-game dino “step forward” after each round. */
    suppressInGameDinoProgress: Boolean = false,
    collectedEggStripCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val chapterLevel = levelId.coerceIn(1, Chapter1Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = chapterLevel,
        chapterId = 1,
        chapterTitle = "פרק 1 - מצא את הביצה",
        stageLabel = "שלב $chapterLevel",
        plan = StationQuizPlans.chapter1(chapterLevel),
        letterPoolSpec = Chapter1LetterPoolSpec,
        backgroundRes = R.drawable.forest_bg_level_overlay,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        collectedEggStripCount = collectedEggStripCount,
        modifier = modifier,
    )
}

/**
 * Episode 1 station 2: small “last popped” balloon next to the main target chip (not a second letter chip).
 */
@Composable
internal fun Station2PinnedBalloonMini(
    letter: String,
    balloonColor: Color,
    /** When false (listen-only episodes), the mini keeps color only—no letter glyph. */
    showLetter: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    balloonColor,
                                    Color(0xFF000000).copy(alpha = 0.08f),
                                ),
                            ),
                    )
                    .border(2.dp, Color(0xFF0B2B3D).copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (showLetter) {
                Text(
                    text = letter,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier =
                Modifier
                    .size(width = 8.dp, height = 5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0B2B3D).copy(alpha = 0.22f)),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RevealLetterTiles(
    options: List<String>,
    correctAnswer: String,
    contentKey: Int,
    wrongRevealSignal: Int,
    enabled: Boolean,
    shakePx: Float,
    onRevealPick: (String) -> Unit,
) {
    var revealed by remember(options, correctAnswer, contentKey) { mutableStateOf<Set<Int>>(emptySet()) }
    LaunchedEffect(wrongRevealSignal) {
        if (wrongRevealSignal == 0) return@LaunchedEffect
        delay(1400)
        revealed = emptySet()
    }
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        val n = options.size.coerceAtLeast(1)
        val gap = 10.dp
        val tileW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = maxWidth,
                count = n,
                gap = gap,
                minEach = 56.dp,
                maxEach = 88.dp,
            )
        val tileH = tileW * (92f / 76f)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEachIndexed { idx, letter ->
                val faceUp = idx in revealed
                Box(
                    modifier =
                        Modifier
                            .width(tileW)
                            .height(tileH)
                            .border(
                                width = 2.dp,
                                color = Color(0xFF0B2B3D).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .background(
                                color = if (faceUp) Color.White.copy(alpha = 0.92f) else Color(0xFFE8F4F8),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable(
                                enabled = enabled && !faceUp,
                                interactionSource = remember(idx) { MutableInteractionSource() },
                                indication = null,
                            ) {
                                revealed = revealed + idx
                                onRevealPick(letter)
                            },
                    contentAlignment = Alignment.Center,
                ) {
                AnimatedContent(
                    targetState = faceUp,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(160)) togetherWith fadeOut(animationSpec = tween(110))
                    },
                    label = "revealFace",
                ) { up ->
                    Text(
                        text = if (up) letter else "?",
                        fontSize =
                            if (tileW < 64.dp) {
                                if (up) 30.sp else 26.sp
                            } else {
                                if (up) 40.sp else 34.sp
                            },
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                    )
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LetterOptions(
    options: List<String>,
    enabled: Boolean,
    shakePx: Float,
    correctPulseLetter: String? = null,
    correctPulseEpoch: Int = 0,
    /** Deeper press squish + larger bounce (e.g. Ch3 st5 “מצא את האות שנאמרת”). */
    strongPressFeedback: Boolean = false,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        options.forEach { letter ->
            val interaction = remember(letter) { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            val pop = remember { Animatable(1f) }
            LaunchedEffect(correctPulseEpoch, correctPulseLetter) {
                if (correctPulseEpoch <= 0 || correctPulseLetter != letter) return@LaunchedEffect
                pop.snapTo(1f)
                if (strongPressFeedback) {
                    pop.animateTo(0.82f, tween(durationMillis = 55))
                    pop.animateTo(1.48f, tween(durationMillis = 140))
                    pop.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 420f))
                } else {
                    pop.animateTo(0.88f, tween(durationMillis = 70))
                    pop.animateTo(1.32f, tween(durationMillis = 110))
                    pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
                }
            }
            val pressSquish =
                if (pressed) {
                    if (strongPressFeedback) 0.84f else 0.94f
                } else {
                    1f
                }
            val letterFontSize = if (strongPressFeedback) 46.sp else 42.sp
            Button(
                onClick = { onPick(letter) },
                enabled = enabled,
                interactionSource = interaction,
            ) {
                Text(
                    text = letter,
                    fontSize = letterFontSize,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.scale(pressSquish * pop.value),
                )
            }
        }
    }
}

/**
 * Jacobi-style overlap resolution: accumulate pairwise nudges from the same snapshot, then apply once per
 * iteration. Avoids the “chain reaction” teleporting of in-place Gauss–Seidel when many balloons crowd.
 *
 * When [rawX]/[rawY]/[maxDeltaFromRaw] are set (runtime drift), correction is capped so balloons ease apart
 * over several frames instead of jumping when separation + clamp fires at once.
 */
private fun separateBalloonCentersJacobi(
    x: FloatArray,
    y: FloatArray,
    alive: BooleanArray,
    minDist: Float,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float,
    iterations: Int,
    damping: Float,
    rawX: FloatArray? = null,
    rawY: FloatArray? = null,
    maxDeltaFromRaw: Float? = null,
) {
    val n = x.size
    val accX = FloatArray(n)
    val accY = FloatArray(n)
    repeat(iterations) {
        accX.fill(0f)
        accY.fill(0f)
        for (i in 0 until n) {
            if (!alive[i]) continue
            for (j in i + 1 until n) {
                if (!alive[j]) continue
                var dx = x[i] - x[j]
                var dy = y[i] - y[j]
                var dist = sqrt(dx * dx + dy * dy)
                if (dist < 1e-5f) {
                    dx = if ((i + j) % 2 == 0) 1f else -0.85f
                    dy = if (i % 2 == 0) 0.72f else -0.72f
                    dist = sqrt(dx * dx + dy * dy)
                }
                if (dist >= minDist) continue
                val overlap = (minDist - dist) * damping
                val nx = dx / dist
                val ny = dy / dist
                val half = overlap * 0.5f
                accX[i] += nx * half
                accY[i] += ny * half
                accX[j] -= nx * half
                accY[j] -= ny * half
            }
        }
        for (k in 0 until n) {
            if (!alive[k]) continue
            x[k] = (x[k] + accX[k]).coerceIn(minX, maxX)
            y[k] = (y[k] + accY[k]).coerceIn(minY, maxY)
        }
    }
    val cap = maxDeltaFromRaw
    if (cap != null && rawX != null && rawY != null) {
        for (i in 0 until n) {
            if (!alive[i]) continue
            var dx = x[i] - rawX[i]
            var dy = y[i] - rawY[i]
            val d = hypot(dx, dy)
            if (d > cap) {
                val s = cap / d
                dx *= s
                dy *= s
            }
            x[i] = (rawX[i] + dx).coerceIn(minX, maxX)
            y[i] = (rawY[i] + dy).coerceIn(minY, maxY)
        }
    }
}

/** 2D scattered anchors with min spacing, then relaxed so centers never start overlapping. */
private fun generateBalloonAnchors(
    count: Int,
    r: Random,
    minSep: Float,
    axMin: Float,
    axMax: Float,
    ayMin: Float,
    ayMax: Float,
): List<Pair<Float, Float>> {
    if (count <= 0) return emptyList()
    val minSepSq = minSep * minSep
    val out = ArrayList<Pair<Float, Float>>(count)
    val spanX = (axMax - axMin).coerceAtLeast(1f)
    val spanY = (ayMax - ayMin).coerceAtLeast(1f)
    val cols = ceil(sqrt(count.toDouble())).toInt().coerceAtLeast(1)
    val rows = ceil(count / cols.toFloat()).toInt().coerceAtLeast(1)
    val cellW = spanX / cols
    val cellH = spanY / rows
    for (i in 0 until count) {
        var x = 0f
        var y = 0f
        var ok = false
        repeat(160) {
            x = axMin + r.nextFloat() * spanX
            y = ayMin + r.nextFloat() * spanY
            ok =
                out.none { (px, py) ->
                    val dx = px - x
                    val dy = py - y
                    (dx * dx + dy * dy) < minSepSq
                }
            if (ok) return@repeat
        }
        if (!ok) {
            val row = i / cols
            val col = i % cols
            val jitterX = (r.nextFloat() - 0.5f) * cellW * 0.45f
            val jitterY = (r.nextFloat() - 0.5f) * cellH * 0.45f
            x = (axMin + (col + 0.5f) * cellW + jitterX).coerceIn(axMin, axMax)
            y = (ayMin + (row + 0.5f) * cellH + jitterY).coerceIn(ayMin, ayMax)
        }
        out.add(x.coerceIn(axMin, axMax) to y.coerceIn(ayMin, ayMax))
    }
    val xb = FloatArray(count) { out[it].first }
    val yb = FloatArray(count) { out[it].second }
    val alive = BooleanArray(count) { true }
    separateBalloonCentersJacobi(
        x = xb,
        y = yb,
        alive = alive,
        minDist = minSep,
        minX = axMin,
        maxX = axMax,
        minY = ayMin,
        maxY = ayMax,
        iterations = 26,
        damping = 0.48f,
        rawX = null,
        rawY = null,
        maxDeltaFromRaw = null,
    )
    return List(count) { i -> xb[i] to yb[i] }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PopBalloonsOptions(
    options: List<String>,
    correctAnswer: String,
    /** When set (Episode 3 station 3), any balloon whose letter is in this set counts as correct. */
    correctLetterSet: Set<String>? = null,
    enabled: Boolean,
    shakePx: Float,
    /** Mixes balloon palette between questions (Episode 1 station 2). */
    visualRoundSeed: Int = 0,
    /** Called whenever a balloon is pressed (letter in balloon). */
    onBalloonPressed: ((letter: String) -> Unit)? = null,
    /** [finalCorrectBalloon] is true for the last required correct balloon in this round; [balloonIndex] is option index. */
    onPopSfx: suspend (letter: String, isCorrect: Boolean, finalCorrectBalloon: Boolean, balloonIndex: Int) -> Unit,
    onWrongPick: () -> Unit,
    /** Invoked when every correct-letter balloon is popped; [poppedBalloonColor] is the last balloon’s fill. */
    onAllCorrectPopped: (correctLetter: String, poppedBalloonColor: Color) -> Unit,
    /** Episode 4 station 2 help: increment to softly pulse balloons that match [correctAnswer] (or [correctLetterSet]). */
    episode4CorrectBalloonHintEpoch: Int = 0,
    /**
     * Episode 4 station 2: inset from layout **start** so balloons stay clear of the on-screen help column
     * (RTL: start = physical right where saga help buttons live).
     */
    helpSideInsetDp: Dp = 0.dp,
) {
    val alive =
        remember(options, correctAnswer) {
            mutableStateListOf<Boolean>().apply { repeat(options.size) { add(true) } }
        }
    val scope = rememberCoroutineScope()
    var wrongRecoverRunning by remember(options, correctAnswer) { mutableStateOf(false) }
    val phases =
        remember(options, correctAnswer) {
            options.mapIndexed { idx, _ ->
                Random(idx * 7919L + options.hashCode()).nextFloat() * 1000f
            }
        }
    // Continuous time base (no wrap/reset). The previous repeating 0→1 phase caused visible “resets”
    // because balloons use non-integer frequencies (sin/cos are not continuous at the loop boundary).
    var timeSec by remember(options, correctAnswer) { mutableFloatStateOf(0f) }
    val startNanos = remember(options, correctAnswer) { mutableLongStateOf(0L) }
    LaunchedEffect(options, correctAnswer) {
        startNanos.longValue = 0L
        timeSec = 0f
        while (true) {
            withFrameNanos { now ->
                if (startNanos.longValue == 0L) startNanos.longValue = now
                timeSec = ((now - startNanos.longValue) / 1_000_000_000.0).toFloat()
            }
        }
    }
    val balloonColors =
        remember(options, correctAnswer, visualRoundSeed) {
            val seed =
                options.fold(0L) { acc, s -> acc * 131L + s.hashCode() } * 131L +
                    correctAnswer.hashCode() +
                    visualRoundSeed.toLong() * 999_983L
            val rnd = Random(seed)
            val palette =
                listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFFFFD93D),
                    Color(0xFF6BCB77),
                    Color(0xFF4D96FF),
                    Color(0xFFB983FF),
                )
            val order = palette.shuffled(rnd)
            List(options.size) { order[it % order.size] }
        }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = helpSideInsetDp)
                .height(ScreenFit.popBalloonsAreaHeightDp())
                .offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val balloonWPx = with(density) { 86.dp.toPx() }
        val balloonHPx = with(density) { 86.dp.toPx() + 18.dp.toPx() + 40.sp.toPx() }
        val paddingPx = with(density) { 10.dp.toPx() }
        val minCenterDist = hypot(balloonWPx, balloonHPx) * 0.97f
        val minX = paddingPx
        val minY = paddingPx
        val maxX = (wPx - balloonWPx - paddingPx).coerceAtLeast(minX)
        val maxY = (hPx - balloonHPx - paddingPx).coerceAtLeast(minY)
        fun remainingCorrectCount(): Int =
            if (correctLetterSet != null) {
                options.indices.count { i -> i < alive.size && alive[i] && options[i] in correctLetterSet }
            } else {
                options.indices.count { i -> i < alive.size && alive[i] && options[i] == correctAnswer }
            }

        // Spawn anchors: full-area 2D scatter + relaxation so starts never overlap.
        val anchors =
            remember(
                options,
                correctAnswer,
                wPx.roundToInt(),
                hPx.roundToInt(),
                balloonWPx.roundToInt(),
                balloonHPx.roundToInt(),
            ) {
                val r = Random(correctAnswer.hashCode().toLong() * 31L + options.hashCode().toLong())
                val minSep = hypot(balloonWPx, balloonHPx) * 0.97f
                val pad = paddingPx
                val axMin = pad
                val ayMin = pad
                val axMax = (wPx - balloonWPx - pad).coerceAtLeast(axMin)
                val ayMax = (hPx - balloonHPx - pad).coerceAtLeast(ayMin)
                generateBalloonAnchors(
                    count = options.size,
                    r = r,
                    minSep = minSep,
                    axMin = axMin,
                    axMax = axMax,
                    ayMin = ayMin,
                    ayMax = ayMax,
                )
            }

        val aliveMask = BooleanArray(options.size) { i -> i < alive.size && alive[i] }
        val xBuf = FloatArray(options.size)
        val yBuf = FloatArray(options.size)
        val rawX = FloatArray(options.size)
        val rawY = FloatArray(options.size)
        options.forEachIndexed { idx, _ ->
            if (!aliveMask[idx]) return@forEachIndexed
            val (baseXPx, baseYPx) = anchors.getOrElse(idx) { minX to minY }
            val dir = if (idx % 2 == 0) 1f else -1f
            val ph = phases.getOrElse(idx) { 0f }
            // Every balloon uses its own phase/frequency so they never share one synchronized "line" of motion.
            val f = 0.71f + (idx % 6) * 0.051f + (idx % 3) * 0.019f
            val base = (timeSec / 18f) * 2f * PI.toFloat() // one “lap” ~18s
            val t = base * f + ph * 1.4f + idx * 0.31f
            val slowFreq = 0.17f + (idx % 7) * 0.056f + (idx % 4) * 0.013f
            val tSlow = base * slowFreq + ph * 2.3f + idx * 0.71f
            val usableW = (maxX - minX).coerceAtLeast(1f)
            val usableH = (maxY - minY).coerceAtLeast(1f)
            val ampX = usableW * (0.13f + (idx % 5) * 0.026f)
            val ampY = usableH * (0.11f + (idx % 4) * 0.024f)
            val ox =
                dir *
                    (
                        sin(t.toDouble()).toFloat() * ampX +
                            sin((t * 0.41f + idx * 0.7f).toDouble()).toFloat() * ampX * 0.55f +
                            cos((t * 0.23f + idx * 0.17f).toDouble()).toFloat() * ampX * 0.38f
                    )
            val oy =
                -dir * cos((t * 0.88f + idx * 0.09f).toDouble()).toFloat() * ampY +
                    sin((t * 0.57f + idx * 0.4f).toDouble()).toFloat() * ampY * 0.9f +
                    sin(tSlow.toDouble()).toFloat() * usableH * 0.26f
            val rx = (baseXPx + ox).coerceIn(minX, maxX)
            val ry = (baseYPx + oy).coerceIn(minY, maxY)
            xBuf[idx] = rx
            yBuf[idx] = ry
            rawX[idx] = rx
            rawY[idx] = ry
        }
        separateBalloonCentersJacobi(
            x = xBuf,
            y = yBuf,
            alive = aliveMask,
            minDist = minCenterDist,
            minX = minX,
            maxX = maxX,
            minY = minY,
            maxY = maxY,
            iterations = 5,
            damping = 0.38f,
            rawX = rawX,
            rawY = rawY,
            maxDeltaFromRaw = minCenterDist * 0.36f,
        )

        options.forEachIndexed { idx, letter ->
            if (idx >= alive.size || !alive[idx]) return@forEachIndexed
            val color = balloonColors.getOrElse(idx) { Color(0xFFFF6B6B) }
            val xPx = xBuf[idx]
            val yPx = yBuf[idx]
            val aliveCorrectBeforeTap =
                if (correctLetterSet != null) {
                    options.indices.count { i -> i < alive.size && alive[i] && options[i] in correctLetterSet }
                } else {
                    options.indices.count { i -> i < alive.size && alive[i] && options[i] == correctAnswer }
                }
            val isCorrectLetter = correctLetterSet?.contains(letter) ?: (letter == correctAnswer)
            val isPotentialFinaleCorrect = isCorrectLetter && aliveCorrectBeforeTap == 1
            val hintBoost = remember(idx, options, correctAnswer) { Animatable(1f) }
            LaunchedEffect(episode4CorrectBalloonHintEpoch, idx, letter, options, correctAnswer, correctLetterSet) {
                if (episode4CorrectBalloonHintEpoch <= 0) return@LaunchedEffect
                if (!isCorrectLetter) return@LaunchedEffect
                hintBoost.snapTo(1f)
                hintBoost.animateTo(1.065f, tween(180))
                hintBoost.animateTo(1.03f, tween(520))
                hintBoost.animateTo(1f, tween(380))
            }

            key(idx) {
                PopBalloon(
                    instanceKey = idx,
                    letter = letter,
                    color = color,
                    seed = idx * 7919 + correctAnswer.hashCode(),
                    enabled = enabled && !wrongRecoverRunning,
                    shouldPop = isCorrectLetter,
                    bobPhaseMillis = idx * 220,
                    popJuiceIndex = idx,
                    finaleCorrectPop = isPotentialFinaleCorrect,
                    driftXPx = xPx,
                    driftYPx = yPx,
                    modifier = Modifier.scale(hintBoost.value).zIndex(yPx * 1000f + xPx),
                    onTapCorrect = {
                        // Make the pop feel connected: start SFX/voice immediately on tap (not after animation).
                        // Keep visual pop (onPop) responsible for removing the balloon.
                        val poppedColor = balloonColors.getOrElse(idx) { Color(0xFFFF6B6B) }
                        if (idx < alive.size) alive[idx] = false
                        scope.launch {
                            onPopSfx(letter, true, isPotentialFinaleCorrect, idx)
                        }
                        // If this is the last required correct balloon, pin the header immediately (reward starts now).
                        if (remainingCorrectCount() <= 0) {
                            onAllCorrectPopped(letter, poppedColor)
                        }
                    },
                    onPop = {
                        balloonColors.getOrElse(idx) { Color(0xFFFF6B6B) }
                        if (letter != correctAnswer) {
                            onWrongPick()
                        }
                    },
                    onPickWrong = { fall ->
                        scope.launch {
                            // UX: play a pop/plop even for wrong balloons (kids expect a sound).
                            onPopSfx(letter, false, false, idx)
                            wrongRecoverRunning = true
                            runWrongBalloonVerticalRecover(fall)
                            onWrongPick()
                            wrongRecoverRunning = false
                        }
                    },
                    onPressed = { onBalloonPressed?.invoke(letter) },
                )
            }
        }
    }
}

private suspend fun runWrongBalloonVerticalRecover(
    offset: Animatable<Float, AnimationVector1D>,
) {
    offset.snapTo(0f)
    offset.animateTo(48f, tween(durationMillis = 120))
    offset.animateTo(640f, tween(durationMillis = 340, easing = LinearOutSlowInEasing))
    offset.snapTo(-560f)
    offset.animateTo(0f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
}

@Composable
internal fun PopBalloon(
    instanceKey: Int,
    letter: String,
    color: Color,
    seed: Int,
    enabled: Boolean,
    shouldPop: Boolean,
    bobPhaseMillis: Int,
    /** Episode 1 station 2: small variation in pop animation timing / peak. */
    popJuiceIndex: Int = 0,
    /** Episode 1 station 2: last required correct balloon — bigger, juicier pop. */
    finaleCorrectPop: Boolean = false,
    driftXPx: Float = 0f,
    driftYPx: Float = 0f,
    modifier: Modifier = Modifier,
    /** Called immediately when tapping a correct balloon (before pop animation). */
    onTapCorrect: () -> Unit = {},
    onPop: () -> Unit,
    onPickWrong: (Animatable<Float, AnimationVector1D>) -> Unit,
    onPressed: () -> Unit = {},
) {
    var popping by remember(instanceKey) { mutableStateOf(false) }
    var visible by remember(instanceKey) { mutableStateOf(true) }
    val scale = remember(instanceKey) { Animatable(1f) }
    val fade = remember(instanceKey) { Animatable(1f) }
    val wrongFall = remember(instanceKey) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val bob =
        rememberInfiniteTransition(label = "balloonBob$instanceKey").animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 2600, easing = LinearEasing, delayMillis = bobPhaseMillis),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "balloonBobAmp$instanceKey",
        )

    LaunchedEffect(popping) {
        if (!popping || !shouldPop) return@LaunchedEffect
        scale.snapTo(1f)
        fade.snapTo(1f)
        val ring = popJuiceIndex % 3
        val ms1 =
            when (ring) {
                0 -> 78
                1 -> 68
                else -> 88
            }
        val peak =
            if (finaleCorrectPop) {
                1.76f
            } else {
                when (ring) {
                    0 -> 1.62f
                    1 -> 1.56f
                    else -> 1.68f
                }
            }
        scale.animateTo(peak * 0.88f, tween(durationMillis = ms1))
        scale.animateTo(peak, tween(durationMillis = if (finaleCorrectPop) 118 else 92))
        scale.animateTo(peak * (if (finaleCorrectPop) 0.74f else 0.88f), tween(durationMillis = if (finaleCorrectPop) 92 else 52))
        fade.animateTo(0f, tween(durationMillis = if (finaleCorrectPop) 230 else 200))
        onPop()
        visible = false
    }

    if (!visible) return

    val interaction = remember(instanceKey) { MutableInteractionSource() }
    remember(seed) {
        val r = Random(seed)
        List(10) { r.nextFloat() * 2f * PI.toFloat() }
    }
    val popFrags =
        remember(seed) {
            // More “real” confetti-like balloon pieces: different sizes, angles, speeds and slight spin.
            val r = Random(seed * 31 + 17)
            data class Frag(val ang: Float, val speed: Float, val w: Float, val h: Float, val spin: Float, val hue: Float)
            List(22) {
                val ang = r.nextFloat() * 2f * PI.toFloat()
                val speed = 0.65f + r.nextFloat() * 1.25f
                val w = 3.5f + r.nextFloat() * 6.0f
                val h = 2.2f + r.nextFloat() * 5.4f
                val spin = (r.nextFloat() - 0.5f) * 2.4f
                val hue = r.nextFloat()
                Frag(ang, speed, w, h, spin, hue)
            }
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier.offset {
                IntOffset(
                    driftXPx.roundToInt(),
                    (bob.value + wrongFall.value + driftYPx).roundToInt(),
                )
            },
    ) {
        Box(
            modifier =
                Modifier
                    .size(86.dp)
                    .scale(scale.value)
                    .alpha(fade.value)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled && !popping,
                        onClick = {
                            onPressed()
                            if (shouldPop) {
                                onTapCorrect()
                                popping = true
                            } else {
                                scope.launch {
                                    scale.animateTo(0.92f, tween(durationMillis = 65))
                                    scale.animateTo(1f, tween(durationMillis = 110))
                                    onPickWrong(wrongFall)
                                }
                            }
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Balloon body
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val r = w * 0.48f
                val outline = Color(0xFF0B2B3D).copy(alpha = 0.22f)
                val shineCenter = Offset(center.x - r * 0.28f, center.y - r * 0.30f)

                // Richer fill (closer to "real" balloon): subtle vertical gradient + outline.
                drawCircle(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    color,
                                    Color(0xFF000000).copy(alpha = 0.06f),
                                ),
                            startY = 0f,
                            endY = h,
                        ),
                    radius = r,
                    center = center,
                )
                drawCircle(color = outline, radius = r, center = center, style = Stroke(width = 3.2f))
                // Shine spot (oval-like via two circles)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.62f), Color.Transparent),
                            center = shineCenter,
                            radius = r * 0.75f,
                        ),
                    radius = r * 0.52f,
                    center = shineCenter,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.24f),
                    radius = r * 0.18f,
                    center = Offset(shineCenter.x - r * 0.02f, shineCenter.y - r * 0.05f),
                )
                // Knot
                drawCircle(color = Color(0xFF0B2B3D).copy(alpha = 0.18f), radius = r * 0.08f, center = Offset(center.x, center.y + r * 0.55f))

                if (popping) {
                    val flash = ((scale.value - 1f) / 0.62f).coerceIn(0f, 1f)
                    if (flash > 0.02f) {
                        if (finaleCorrectPop) {
                            drawCircle(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                Color(0xFFFFF59D).copy(alpha = 0.45f * flash),
                                                Color(0xFFFFB74D).copy(alpha = 0.22f * flash),
                                                Color.Transparent,
                                            ),
                                        center = center,
                                        radius = r * (1.35f + flash * 1.15f),
                                    ),
                                radius = r * (1.35f + flash * 1.15f),
                                center = center,
                            )
                        }
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color.White.copy(alpha = 0.55f * flash),
                                            Color(0xFFFFE082).copy(alpha = 0.35f * flash),
                                            Color.Transparent,
                                        ),
                                    center = center,
                                    radius = r * (1.05f + flash * 0.85f),
                                ),
                            radius = r * (1.05f + flash * 0.85f),
                            center = center,
                        )
                        // Balloon fragments (pieces) instead of “rays”.
                        val t = flash
                        val baseLen = r * (0.35f + t * 1.35f)
                        val fadeA = (1f - t).coerceIn(0f, 1f)
                        for (f in popFrags) {
                            val travel = baseLen * f.speed * (0.65f + t * 0.85f)
                            val x = center.x + cos(f.ang) * travel
                            val y = center.y + sin(f.ang) * travel
                            val rot = (t * 2.2f + f.spin) * 180f / PI.toFloat()
                            // drawscope: compose provides translate/rotate helpers in drawscope.
                            translate(left = x, top = y) {
                                rotate(degrees = rot) {
                                // Slightly curved “rubber” shard feel: rounded rect + tiny dot highlight.
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.42f * t * fadeA),
                                    topLeft = Offset(-f.w * 0.5f, -f.h * 0.5f),
                                    size = Size(f.w, f.h),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(f.h * 0.55f, f.h * 0.55f),
                                )
                                drawCircle(
                                    color = Color(0xFFFFE082).copy(alpha = 0.16f * t * fadeA),
                                    radius = (minOf(f.w, f.h) * 0.18f).coerceAtLeast(0.8f),
                                    center = Offset(f.w * 0.18f, -f.h * 0.12f),
                                )
                                }
                            }
                        }
                        // Keep finale a small upgrade: no extra “sparkle dots” (the halo above is enough).
                    }
                }
            }

            Text(text = letter, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
        }

        androidx.compose.foundation.Canvas(modifier = Modifier.width(2.dp).height(18.dp)) {
            drawLine(
                color = Color(0xFF0B2B3D).copy(alpha = 0.30f),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2f,
            )
        }
    }
}

private fun playSuccessAnimation(
    scope: CoroutineScope,
    dinoScale: Animatable<Float, AnimationVector1D>,
    showConfetti: MutableState<Boolean>,
): Job = scope.launch {
    showConfetti.value = true
    dinoScale.snapTo(1f)
    dinoScale.animateTo(
        targetValue = 1.18f,
        animationSpec = tween(durationMillis = 140),
    )
    dinoScale.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 600f),
    )
    delay(450)
    showConfetti.value = false
}

private fun playMistakeAnimation(
    scope: CoroutineScope,
    optionsShake: Animatable<Float, AnimationVector1D>,
): Job = scope.launch {
    optionsShake.snapTo(0f)
    val amp = 18f
    repeat(5) { i ->
        optionsShake.animateTo(
            targetValue = if (i % 2 == 0) amp else -amp,
            animationSpec = tween(durationMillis = 45),
        )
    }
    optionsShake.animateTo(0f, animationSpec = tween(durationMillis = 60))
}

@Composable
internal fun ConfettiOverlay(modifier: Modifier = Modifier) {
    // Very lightweight "confetti": a few translucent circles; enough for MVP.
    Box(modifier = modifier) {
        repeat(14) { idx ->
            val x = (idx * 23) % 100
            val y = (idx * 37) % 100
            val color = when (idx % 5) {
                0 -> Color(0x66FF6B6B)
                1 -> Color(0x66FFD93D)
                2 -> Color(0x666BCB77)
                3 -> Color(0x664D96FF)
                else -> Color(0x66B983FF)
            }
            Spacer(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x.dp * 3, y.dp * 5)
                    .size(18.dp)
                    .background(color, shape = CircleShape),
            )
        }
    }
}

