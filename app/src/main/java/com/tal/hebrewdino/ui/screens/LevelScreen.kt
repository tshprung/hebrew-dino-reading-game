package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class LevelDinoVisual {
    Idle,
    TryAgain,
    Jump,
}

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    onComplete: (levelId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chapterLevel = levelId.coerceIn(1, Chapter1Config.STATION_COUNT)
    // Chapter 1 pacing: short early levels, longer later (six stations).
    val questionCount =
        remember(chapterLevel) {
            when (chapterLevel) {
                1 -> 3
                2 -> 4
                3 -> 5
                4 -> 5
                5 -> 6
                else -> 7
            }
        }
    val initialGroupIndex =
        remember(chapterLevel) {
            when (chapterLevel) {
                1, 2 -> 0
                3, 4 -> 1
                5 -> 2
                else -> 3
            }
        }
    val session = remember(chapterLevel) { LevelSession(questionCount = questionCount, initialGroupIndex = initialGroupIndex) }
    var feedback by remember(chapterLevel) { mutableStateOf<String?>(null) }
    fun rtl(text: String): String = "\u200F$text"

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    val jumpFrames =
        remember(chapterLevel) {
            listOf(R.drawable.dino_jump_0, R.drawable.dino_jump_1, R.drawable.dino_jump_2)
        }
    var dinoVisual by remember(chapterLevel) { mutableStateOf(LevelDinoVisual.Idle) }
    var jumpFrameIndex by remember(chapterLevel) { mutableIntStateOf(0) }

    // Animations
    val dinoScale = remember(chapterLevel) { androidx.compose.animation.core.Animatable(1f) }
    val optionsShake = remember(chapterLevel) { androidx.compose.animation.core.Animatable(0f) }
    val showConfetti = remember(chapterLevel) { mutableStateOf(false) }
    var inputLocked by remember(chapterLevel) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
            sfx.release()
        }
    }

    val current = session.currentQuestion
    val totalQuestions = session.totalQuestions
    val questionNumber = session.questionNumber
    val correctLetter =
        when (current) {
            is Question.TapChoiceQuestion -> current.correctAnswer
            is Question.PopBalloonsQuestion -> current.correctAnswer
            is Question.DragToEggQuestion -> current.correctAnswer
            null -> ""
        }

    if (current == null) {
        onComplete(chapterLevel, session.correctCount, session.mistakeCount)
        return
    }

    var wrongAttemptsThisQuestion by remember(chapterLevel, session.currentIndex) { mutableStateOf(0) }

    suspend fun speakPrompt(targetLetter: String) {
        // Chapter letters are taught at the chapter intro screen, so in-level we only say “choose X”.
        val chooseSpecific = AudioClips.chooseLetterClip(targetLetter)
        if (chooseSpecific != null) {
            voice.playBlocking(chooseSpecific)
        } else {
            voice.playBlocking(AudioClips.VoChooseLetter)
        }
    }

    LaunchedEffect(dinoVisual) {
        if (dinoVisual != LevelDinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            jumpFrameIndex = i % jumpFrames.size
            delay(85)
        }
        dinoVisual = LevelDinoVisual.Idle
    }

    LaunchedEffect(chapterLevel, session.currentIndex) {
        val promptLetter = correctLetter
        feedback = null
        wrongAttemptsThisQuestion = 0
        // Preload SFX so the first play isn't silent.
        sfx.preload(AudioClips.SfxCorrect, AudioClips.SfxWrong, AudioClips.SfxBalloonPop)
        // Let UI settle a bit before speaking.
        delay(120)
        speakPrompt(promptLetter)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_level_overlay),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        val bob by rememberInfiniteTransition(label = "bob").animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bobOffset",
        )

        if (showConfetti.value) {
            ConfettiOverlay(
                modifier = Modifier.fillMaxSize(),
            )
        }

        val dinoDrawable =
            when (dinoVisual) {
                LevelDinoVisual.Idle -> R.drawable.dino_idle
                LevelDinoVisual.TryAgain -> R.drawable.dino_try_again
                LevelDinoVisual.Jump -> jumpFrames[jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
            }
        val dinoBobY =
            when (dinoVisual) {
                LevelDinoVisual.Idle -> bob.dp
                else -> 0.dp
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            OutlinedButton(
                onClick = onBack,
                colors =
                    androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.86f),
                        contentColor = Color(0xFF0B2B3D),
                    ),
            ) { Text("חזור") }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "שלב $chapterLevel",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "שאלה $questionNumber מתוך $totalQuestions",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { questionNumber.toFloat() / totalQuestions.toFloat() },
            modifier = Modifier.width(320.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        val mission =
            when (current) {
                is Question.TapChoiceQuestion -> "משימה: תבחר/י את האות הנכונה"
                is Question.PopBalloonsQuestion -> "משימה: תפוצץ/י בלון עם האות הנכונה"
                is Question.DragToEggQuestion -> "משימה: תגרור/י את האות אל הביצה"
            }

        // Make progress feel chunky (kids notice each step).
        val rawProgress = (questionNumber - 1).coerceAtLeast(0).toFloat() / totalQuestions.toFloat()
        val steps = 5
        val eggProgress = (kotlin.math.floor(rawProgress * steps) / steps).coerceIn(0f, 1f)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
                MissionWidget(
                    mission = mission,
                    progress = eggProgress,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        if (inputLocked) return@TextButton
                        val promptLetter = correctLetter
                        scope.launch { speakPrompt(promptLetter) }
                    },
                ) {
                    Text("שמע/י שוב")
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mission,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text =
                        "בחר את האות: " +
                            when (current) {
                                is Question.TapChoiceQuestion -> current.correctAnswer
                                is Question.PopBalloonsQuestion -> current.correctAnswer
                                is Question.DragToEggQuestion -> current.correctAnswer
                            },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF0B2B3D),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = feedback ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF0B2B3D),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                val onPick: (String) -> Unit = { picked ->
            when (session.submitAnswer(picked)) {
                AnswerResult.Correct -> {
                    // Advance ONLY after "good job" finishes (no overlaps).
                    scope.launch {
                        inputLocked = true
                        dinoVisual = LevelDinoVisual.Jump
                        feedback = rtl("כל הכבוד!")
                        playSuccessAnimation(scope, dinoScale, showConfetti)
                        sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.7f)
                        voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                        session.nextQuestion()
                        inputLocked = false
                    }
                }
                AnswerResult.Wrong -> {
                    scope.launch {
                        inputLocked = true
                        dinoVisual = LevelDinoVisual.TryAgain
                        wrongAttemptsThisQuestion += 1
                        feedback = "כמעט… בוא ננסה שוב"
                        playMistakeAnimation(scope, optionsShake)
                        sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.7f)
                        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                        if (wrongAttemptsThisQuestion >= 2) {
                            feedback = rtl("רמז: $correctLetter")
                        }
                        dinoVisual = LevelDinoVisual.Idle
                        inputLocked = false
                    }
                }
                AnswerResult.Finished -> {}
            }
        }

        when (current) {
            is Question.TapChoiceQuestion -> {
                LetterOptions(
                    options = current.options,
                    enabled = !inputLocked,
                    shakePx = optionsShake.value,
                    onPick = onPick,
                )
            }
            is Question.PopBalloonsQuestion -> {
                PopBalloonsOptions(
                    options = current.options,
                    correctAnswer = current.correctAnswer,
                    enabled = !inputLocked,
                    shakePx = optionsShake.value,
                    onPopSfx = { sfx.playFirstAvailable(AudioClips.SfxBalloonPop, volume = 0.8f) },
                    onPick = onPick,
                )
            }
            is Question.DragToEggQuestion -> {
                DragToEggOptions(
                    options = current.options,
                    enabled = !inputLocked,
                    shakePx = optionsShake.value,
                    onDrop = onPick,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Dino sits near the options area (bottom-ish), so it’s easy to notice.
        Image(
            painter = painterResource(id = dinoDrawable),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .offset(y = dinoBobY)
                .scale(dinoScale.value),
            contentScale = ContentScale.Fit,
        )
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה לדרך") }
    }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterOptions(
    options: List<String>,
    enabled: Boolean,
    shakePx: Float,
    onPick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        options.forEach { letter ->
            Button(onClick = { onPick(letter) }, enabled = enabled) {
                Text(text = letter, fontSize = 42.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopBalloonsOptions(
    options: List<String>,
    correctAnswer: String,
    enabled: Boolean,
    shakePx: Float,
    onPopSfx: suspend () -> Unit,
    onPick: (String) -> Unit,
) {
    // Reuses the reward-screen “balloon pop” feel but shows letters inside.
    val alive = remember(options, correctAnswer) { options.associateWith { true }.toMutableMap() }
    val scope = rememberCoroutineScope()

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        options.forEachIndexed { idx, letter ->
            val isAlive = alive[letter] == true
            if (!isAlive) return@forEachIndexed

            val color =
                when (idx % 5) {
                    0 -> Color(0xFFFF6B6B)
                    1 -> Color(0xFFFFD93D)
                    2 -> Color(0xFF6BCB77)
                    3 -> Color(0xFF4D96FF)
                    else -> Color(0xFFB983FF)
                }
            PopBalloon(
                letter = letter,
                color = color,
                enabled = enabled,
                shouldPop = letter == correctAnswer,
                onPop = {
                    // Only pop the correct balloon (keeps retry possible).
                    alive[letter] = false
                    scope.launch { onPopSfx() }
                    onPick(letter)
                },
                onPickWithoutPop = { onPick(letter) },
            )
        }
    }
}

@Composable
private fun PopBalloon(
    letter: String,
    color: Color,
    enabled: Boolean,
    shouldPop: Boolean,
    onPop: () -> Unit,
    onPickWithoutPop: () -> Unit,
) {
    var popping by remember(letter) { mutableStateOf(false) }
    var visible by remember(letter) { mutableStateOf(true) }
    val scale = remember(letter) { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(popping) {
        if (!popping) return@LaunchedEffect
        scale.snapTo(1f)
        scale.animateTo(
            targetValue = 1.22f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 110),
        )
        scale.animateTo(
            targetValue = 0.15f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 90),
        )
        visible = false
        onPop()
    }

    if (!visible) return

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(86.dp)
                    .scale(scale.value)
                    .clickable(
                        enabled = enabled && !popping,
                        onClick = {
                            if (shouldPop) popping = true else onPickWithoutPop()
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

@Composable
private fun MissionWidget(
    mission: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val p = progress.coerceIn(0f, 1f)
    val crackStage =
        when {
            p < 0.25f -> 0
            p < 0.50f -> 1
            p < 0.75f -> 2
            else -> 3
        }

    Box(
        modifier =
            modifier
                .background(Color.White.copy(alpha = 0.72f), shape = RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EggGraphic(stage = crackStage, modifier = Modifier.size(42.dp))
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = Color(0xFFFFC400),
                    trackColor = Color(0xFF0B2B3D).copy(alpha = 0.12f),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mission,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun EggGraphic(stage: Int, modifier: Modifier = Modifier) {
    // Simple drawn egg with crack stages (no asset needed).
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val eggRadiusX = w * 0.36f
        val eggRadiusY = h * 0.44f

        // Soft glow as we progress.
        if (stage >= 2) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(Color(0x66FFC400), Color.Transparent),
                        center = center,
                        radius = w * 0.7f,
                    ),
                radius = w * 0.45f,
                center = center,
            )
        }

        // Egg body gradient
        drawOval(
            brush =
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFBF2), Color(0xFFF1E3C6)),
                ),
            topLeft = Offset(center.x - eggRadiusX, center.y - eggRadiusY),
            size = androidx.compose.ui.geometry.Size(eggRadiusX * 2f, eggRadiusY * 2f),
        )
        drawOval(
            color = Color(0xFF8B6B3E).copy(alpha = 0.35f),
            topLeft = Offset(center.x - eggRadiusX, center.y - eggRadiusY),
            size = androidx.compose.ui.geometry.Size(eggRadiusX * 2f, eggRadiusY * 2f),
            style = Stroke(width = w * 0.06f),
        )

        if (stage >= 1) {
            // A simple crack line
            val y0 = center.y - eggRadiusY * 0.15f
            val y1 = center.y + eggRadiusY * 0.25f
            val x = center.x
            val pts =
                listOf(
                    Offset(x - w * 0.06f, y0),
                    Offset(x + w * 0.02f, y0 + h * 0.08f),
                    Offset(x - w * 0.03f, y0 + h * 0.16f),
                    Offset(x + w * 0.05f, y0 + h * 0.24f),
                    Offset(x - w * 0.01f, y1),
                )
            for (i in 0 until pts.size - 1) {
                drawLine(
                    color = Color(0xFF6B4A2A),
                    start = pts[i],
                    end = pts[i + 1],
                    strokeWidth = w * 0.05f,
                )
            }
        }
        if (stage >= 3) {
            // A second crack
            drawLine(
                color = Color(0xFF6B4A2A),
                start = Offset(center.x - w * 0.10f, center.y + h * 0.02f),
                end = Offset(center.x + w * 0.12f, center.y + h * 0.18f),
                strokeWidth = w * 0.04f,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DragToEggOptions(
    options: List<String>,
    enabled: Boolean,
    shakePx: Float,
    onDrop: (String) -> Unit,
) {
    var containerRect by remember { mutableStateOf<Rect?>(null) }
    var eggRect by remember { mutableStateOf<Rect?>(null) }
    var draggingLetter by remember { mutableStateOf<String?>(null) }
    var dragPosInRoot by remember { mutableStateOf<Offset?>(null) }
    val snapAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()

    val hoveringEgg = eggRect != null && dragPosInRoot != null && eggRect!!.contains(dragPosInRoot!!)

    Box(
        modifier =
            Modifier.onGloballyPositioned { containerRect = it.boundsInRoot() },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Drop target (egg)
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .onGloballyPositioned { coords ->
                            eggRect = coords.boundsInRoot()
                        }
                        .background(
                            if (hoveringEgg) Color(0xFFFFF3C4).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.75f),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🥚", fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Draggables
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
            ) {
                options.forEach { letter ->
                    var coords: LayoutCoordinates? by remember(letter) { mutableStateOf(null) }
                    val isDraggingThis = draggingLetter == letter

                    Box(
                        modifier =
                            Modifier
                                .size(84.dp)
                                .background(
                                    Color(0xFFF3F7FF).copy(alpha = if (isDraggingThis) 0.35f else 0.95f),
                                    shape = CircleShape,
                                )
                                .onGloballyPositioned { coords = it }
                                .pointerInput(enabled, letter) {
                                    if (!enabled) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingLetter = letter
                                            dragPosInRoot = coords?.localToRoot(Offset(42f, 42f))
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val c = coords
                                            if (c != null) {
                                                dragPosInRoot = c.localToRoot(change.position)
                                            }
                                        },
                                        onDragEnd = {
                                            val rect = eggRect
                                            val pos = dragPosInRoot
                                            if (rect != null && pos != null && rect.contains(pos)) {
                                                scope.launch {
                                                    snapAnim.snapTo(0f)
                                                    snapAnim.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(140))
                                                }
                                                onDrop(letter)
                                            }
                                            draggingLetter = null
                                            dragPosInRoot = null
                                        },
                                        onDragCancel = {
                                            draggingLetter = null
                                            dragPosInRoot = null
                                        },
                                    )
                                }
                                .clickable(enabled = enabled) {
                                    // Fallback: allow tap to attempt (useful for accessibility / if drag is hard).
                                    onDrop(letter)
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = letter, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
                    }
                }
            }
        }

        // Floating dragged letter (visual feedback)
        val cRect = containerRect
        val posRoot = dragPosInRoot
        val dragging = draggingLetter
        if (cRect != null && posRoot != null && dragging != null) {
            val local = posRoot - cRect.topLeft
            val half = 42
            val egg = eggRect
            val snapT = snapAnim.value
            val snapTargetLocal =
                if (egg != null) {
                    (egg.center - cRect.topLeft) - Offset(half.toFloat(), half.toFloat())
                } else {
                    Offset((local.x - half), (local.y - half))
                }
            val start = Offset((local.x - half), (local.y - half))
            val blended = Offset(
                x = start.x + (snapTargetLocal.x - start.x) * snapT,
                y = start.y + (snapTargetLocal.y - start.y) * snapT,
            )
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(blended.x.toInt(), blended.y.toInt()) }
                        .size(84.dp)
                        .background(Color.White.copy(alpha = 0.92f), shape = CircleShape)
                        .border(2.dp, Color(0xFFFFC400), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = dragging, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
            }
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
private fun ConfettiOverlay(modifier: Modifier = Modifier) {
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

