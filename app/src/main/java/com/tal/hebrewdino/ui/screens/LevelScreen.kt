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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.key
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import com.tal.hebrewdino.ui.components.learning.PictureLetterMatchBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    onComplete: (levelId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chapterLevel = levelId.coerceIn(1, Chapter1Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = chapterLevel,
        chapterTitle = "פרק 1 - מצא את הביצה",
        stageLabel = "שלב $chapterLevel",
        plan = StationQuizPlans.chapter1(chapterLevel),
        letterPoolSpec = LetterPoolSpec.Default,
        onBack = onBack,
        onComplete = onComplete,
        modifier = modifier,
    )
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
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        options.forEachIndexed { idx, letter ->
            val faceUp = idx in revealed
            Box(
                modifier =
                    Modifier
                        .width(76.dp)
                        .height(92.dp)
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
                        fontSize = if (up) 40.sp else 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                    )
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
    onPick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        options.forEach { letter ->
            val interaction = remember(letter) { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            val pop = remember { Animatable(1f) }
            LaunchedEffect(correctPulseEpoch, correctPulseLetter) {
                if (correctPulseEpoch <= 0 || correctPulseLetter != letter) return@LaunchedEffect
                pop.snapTo(1f)
                pop.animateTo(0.88f, tween(durationMillis = 70))
                pop.animateTo(1.18f, tween(durationMillis = 110))
                pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
            }
            val pressSquish = if (pressed) 0.94f else 1f
            Button(
                onClick = { onPick(letter) },
                enabled = enabled,
                interactionSource = interaction,
            ) {
                Text(
                    text = letter,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.scale(pressSquish * pop.value),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PopBalloonsOptions(
    options: List<String>,
    correctAnswer: String,
    enabled: Boolean,
    shakePx: Float,
    onPopSfx: suspend (isCorrect: Boolean) -> Unit,
    onPick: (String) -> Unit,
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
    val drift = rememberInfiniteTransition(label = "popBalloonDrift")
    val driftPhase by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(14_000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "popBalloonDriftPhase",
    )

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(440.dp)
                .offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        options.forEachIndexed { idx, letter ->
            if (idx >= alive.size || !alive[idx]) return@forEachIndexed
            val color =
                when (idx % 5) {
                    0 -> Color(0xFFFF6B6B)
                    1 -> Color(0xFFFFD93D)
                    2 -> Color(0xFF6BCB77)
                    3 -> Color(0xFF4D96FF)
                    else -> Color(0xFFB983FF)
                }
            val baseXPx = wPx * (0.06f + (idx % 3) * 0.30f).coerceIn(0.07f, 0.78f)
            val baseYPx = hPx * (0.10f + (idx % 2) * 0.24f)
            val ampXPx = 36f + (idx % 3) * 24f
            val ampYPx = 28f + (idx % 2) * 20f
            val ph = phases.getOrElse(idx) { 0f }
            val t = driftPhase * 2f * PI.toFloat() + ph
            val ox = sin(t.toDouble()).toFloat() * ampXPx
            val oy = cos((t * 0.85f).toDouble()).toFloat() * ampYPx
            val paddingPx = 10f
            val rawXPx = baseXPx + ox
            val rawYPx = baseYPx + oy
            val xPx = rawXPx.coerceIn(paddingPx, (wPx - 100f).coerceAtLeast(paddingPx))
            val yPx = rawYPx.coerceIn(paddingPx, (hPx - 130f).coerceAtLeast(paddingPx))

            key(idx, letter) {
                PopBalloon(
                    letter = letter,
                    color = color,
                    enabled = enabled && !wrongRecoverRunning,
                    shouldPop = letter == correctAnswer,
                    bobPhaseMillis = idx * 220,
                    driftXPx = xPx,
                    driftYPx = yPx,
                    onPop = {
                        if (idx < alive.size) alive[idx] = false
                        scope.launch { onPopSfx(letter == correctAnswer) }
                        onPick(letter)
                    },
                    onPickWrong = { fall ->
                        scope.launch {
                            wrongRecoverRunning = true
                            runWrongBalloonVerticalRecover(fall)
                            onPick(letter)
                            wrongRecoverRunning = false
                        }
                    },
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
    letter: String,
    color: Color,
    enabled: Boolean,
    shouldPop: Boolean,
    bobPhaseMillis: Int,
    driftXPx: Float = 0f,
    driftYPx: Float = 0f,
    onPop: () -> Unit,
    onPickWrong: (Animatable<Float, AnimationVector1D>) -> Unit,
) {
    var popping by remember(letter) { mutableStateOf(false) }
    var visible by remember(letter) { mutableStateOf(true) }
    val scale = remember(letter) { Animatable(1f) }
    val fade = remember(letter) { Animatable(1f) }
    val wrongFall = remember(letter) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val bob =
        rememberInfiniteTransition(label = "balloonBob$letter").animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 2600, easing = LinearEasing, delayMillis = bobPhaseMillis),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "balloonBob",
        )

    LaunchedEffect(popping) {
        if (!popping || !shouldPop) return@LaunchedEffect
        scale.snapTo(1f)
        fade.snapTo(1f)
        scale.animateTo(1.28f, tween(durationMillis = 90))
        fade.animateTo(0f, tween(durationMillis = 240))
        visible = false
        onPop()
    }

    if (!visible) return

    val interaction = remember(letter) { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.offset {
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
                            if (shouldPop) {
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

                drawCircle(color = color, radius = r, center = center)
                // Shine
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                            center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                            radius = r * 0.9f,
                        ),
                    radius = r,
                    center = center,
                )
                // Knot
                drawCircle(color = Color(0xFF0B2B3D).copy(alpha = 0.18f), radius = r * 0.08f, center = Offset(center.x, center.y + r * 0.55f))
            }

            Text(text = letter, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
        }

        // String
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
    dinoScale: androidx.compose.animation.core.Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    showConfetti: MutableState<Boolean>,
): Job = scope.launch {
    showConfetti.value = true
    dinoScale.snapTo(1f)
    dinoScale.animateTo(
        targetValue = 1.18f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 140),
    )
    dinoScale.animateTo(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.45f, stiffness = 600f),
    )
    delay(450)
    showConfetti.value = false
}

private fun playMistakeAnimation(
    scope: CoroutineScope,
    optionsShake: androidx.compose.animation.core.Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
): Job = scope.launch {
    optionsShake.snapTo(0f)
    val amp = 18f
    repeat(5) { i ->
        optionsShake.animateTo(
            targetValue = if (i % 2 == 0) amp else -amp,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 45),
        )
    }
    optionsShake.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 60))
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
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}

