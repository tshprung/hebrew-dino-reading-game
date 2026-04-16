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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    onComplete: (levelId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep the same feel as before: question count scales with level id.
    val questionCount =
        remember(levelId) {
            when (levelId) {
                1, 2 -> 6
                3, 4 -> 8
                5, 6, 7 -> 10
                else -> 12
            }
        }
    val session = remember(levelId) { LevelSession(questionCount = questionCount) }
    var feedback by remember(levelId) { mutableStateOf<String?>(null) }
    fun rtl(text: String): String = "\u200F$text"

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    val characterPrefs = remember { CharacterPrefs(context) }
    val character by characterPrefs.characterFlow.collectAsState(initial = DinoCharacter.Dino)

    val dinoRes = if (character == DinoCharacter.Dina) R.drawable.dino_girl else R.drawable.dino_boy

    // Animations
    val dinoScale = remember(levelId) { androidx.compose.animation.core.Animatable(1f) }
    val optionsShake = remember(levelId) { androidx.compose.animation.core.Animatable(0f) }
    val showConfetti = remember(levelId) { mutableStateOf(false) }
    var inputLocked by remember(levelId) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
            sfx.release()
        }
    }

    val current = session.currentQuestion
    val totalQuestions = session.totalQuestions
    val questionNumber = session.questionNumber

    if (current == null) {
        onComplete(levelId, session.correctCount, session.mistakeCount)
        return
    }

    LaunchedEffect(levelId, session.currentIndex) {
        feedback = null
        // Let UI settle a bit before speaking.
        delay(120)
        val target =
            when (current) {
                is Question.TapChoiceQuestion -> current.correctAnswer
                is Question.PopBalloonsQuestion -> current.correctAnswer
                is Question.DragToEggQuestion -> current.correctAnswer
            }
        val chooseSpecific = AudioClips.chooseLetterClip(target)
        if (chooseSpecific != null) {
            voice.playBlocking(chooseSpecific)
        } else {
            // Fallback: "choose letter" + letter name
            val name = AudioClips.letterNameClip(target)
            voice.playBlocking(AudioClips.VoChooseLetter)
            if (name != null) {
                voice.playBlocking(name)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_level_beach),
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

        // Dino avatar (idle bob + reacts to success)
        Image(
            painter = painterResource(id = dinoRes),
            contentDescription = null,
            modifier = Modifier
                .padding(12.dp)
                .size(96.dp)
                .align(Alignment.TopStart)
                .offset(y = bob.dp)
                .scale(dinoScale.value),
            contentScale = ContentScale.Fit,
        )

        if (showConfetti.value) {
            ConfettiOverlay(
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
        Text(
            text = "שלב $levelId",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
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

        val eggProgress = (questionNumber - 1).coerceAtLeast(0).toFloat() / totalQuestions.toFloat()
        MissionWidget(
            mission = mission,
            progress = eggProgress,
            modifier = Modifier.width(360.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

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
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
            Text(
                text = feedback ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        val onPick: (String) -> Unit = { picked ->
            when (session.submitAnswer(picked)) {
                AnswerResult.Correct -> {
                    // Advance ONLY after "good job" finishes (no overlaps).
                    scope.launch {
                        inputLocked = true
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
                        feedback = "כמעט… בוא ננסה שוב"
                        playMistakeAnimation(scope, optionsShake)
                        sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.7f)
                        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
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

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה למפה") }
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

    Box(
        modifier =
            Modifier
                .size(86.dp)
                .background(color, shape = CircleShape)
                .scale(scale.value)
                .clickable(
                    enabled = enabled && !popping,
                    onClick = {
                        if (shouldPop) {
                            popping = true
                        } else {
                            onPickWithoutPop()
                        }
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = letter, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
    }
}

@Composable
private fun MissionWidget(
    mission: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    // Tiny “game loop” context: egg cracks as you progress through the level.
    val p = progress.coerceIn(0f, 1f)
    val cracks =
        when {
            p < 0.34f -> ""
            p < 0.67f -> " ᐟ"
            else -> " ᐟᐟ"
        }
    Box(
        modifier =
            modifier
                .background(Color.White.copy(alpha = 0.70f), shape = RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "🥚$cracks", fontSize = 28.sp)
            LinearProgressIndicator(
                progress = { p },
                modifier = Modifier.width(180.dp),
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
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset((local.x - half).toInt(), (local.y - half).toInt()) }
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

