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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tal.hebrewdino.ui.StationMatchPlaceholders
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.components.learning.PictureLetterMatchBoard
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

private data class Chapter1StationSpec(
    val mode: StationQuizMode,
    val questionCount: Int,
    val initialGroupIndex: Int,
)

private fun chapter1StationSpec(stationId: Int): Chapter1StationSpec =
    when (stationId) {
        1 -> Chapter1StationSpec(StationQuizMode.TapChoice, questionCount = 3, initialGroupIndex = 0)
        2 -> Chapter1StationSpec(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
        3 -> Chapter1StationSpec(StationQuizMode.RevealTiles, questionCount = 5, initialGroupIndex = 1)
        4 -> Chapter1StationSpec(StationQuizMode.TapChoice, questionCount = 5, initialGroupIndex = 1)
        5 -> Chapter1StationSpec(StationQuizMode.PopBalloons, questionCount = 6, initialGroupIndex = 2)
        else -> Chapter1StationSpec(StationQuizMode.PictureLetterMatch, questionCount = 3, initialGroupIndex = 3)
    }

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    onComplete: (levelId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chapterLevel = levelId.coerceIn(1, Chapter1Config.STATION_COUNT)
    val stationSpec = remember(chapterLevel) { chapter1StationSpec(chapterLevel) }
    val questionCount = stationSpec.questionCount
    val initialGroupIndex = stationSpec.initialGroupIndex
    val quizMode = stationSpec.mode
    val session =
        remember(chapterLevel, quizMode) {
            LevelSession(
                questionCount = questionCount,
                initialGroupIndex = initialGroupIndex,
                quizMode = quizMode,
                matchPlaceholders = StationMatchPlaceholders.forest,
            )
        }
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
            is Question.RevealTilesQuestion -> current.correctAnswer
            is Question.PictureLetterMatchQuestion ->
                current.pairs.joinToString(" ו־") { it.letter }
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

    suspend fun speakPromptForQuestion(q: Question) {
        when (q) {
            is Question.PictureLetterMatchQuestion -> {
                voice.playBlocking(AudioClips.VoChooseLetter)
            }
            is Question.TapChoiceQuestion -> speakPrompt(q.correctAnswer)
            is Question.PopBalloonsQuestion -> speakPrompt(q.correctAnswer)
            is Question.RevealTilesQuestion -> speakPrompt(q.correctAnswer)
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
        val q = session.currentQuestion
        feedback = null
        wrongAttemptsThisQuestion = 0
        // Preload SFX so the first play isn't silent.
        sfx.preload(AudioClips.SfxCorrect, AudioClips.SfxWrong, AudioClips.SfxBalloonPop)
        // Let UI settle a bit before speaking.
        delay(120)
        if (q != null) speakPromptForQuestion(q)
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
                TextButton(
                    onClick = {
                        if (inputLocked) return@TextButton
                        val q = current
                        scope.launch { speakPromptForQuestion(q) }
                    },
                ) {
                    Text("שמע/י שוב")
                }
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text =
                        when (current) {
                            is Question.PictureLetterMatchQuestion ->
                                "חבר כל תמונה לאות המתאימה (הקישו תמונה ואז אות)"
                            is Question.RevealTilesQuestion ->
                                "לחצו על כרטיסייה כדי לחשוף, ואז מצאו את האות: " + current.correctAnswer
                            is Question.TapChoiceQuestion ->
                                "בחר את האות: " + current.correctAnswer
                            is Question.PopBalloonsQuestion ->
                                "בחר את האות: " + current.correctAnswer
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
                    is Question.RevealTilesQuestion -> {
                        RevealLetterTiles(
                            options = current.options,
                            correctAnswer = current.correctAnswer,
                            contentKey = session.currentIndex,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            onRevealPick = onPick,
                        )
                    }
                    is Question.PictureLetterMatchQuestion -> {
                        PictureLetterMatchBoard(
                            question = current,
                            contentKey = session.currentIndex,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            onWrongPair = {
                                when (session.submitMatchOutcome(false)) {
                                    AnswerResult.Wrong ->
                                        scope.launch {
                                            inputLocked = true
                                            dinoVisual = LevelDinoVisual.TryAgain
                                            wrongAttemptsThisQuestion += 1
                                            feedback = "זוג לא מתאים — ננסה שוב"
                                            playMistakeAnimation(scope, optionsShake)
                                            sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.7f)
                                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                            if (wrongAttemptsThisQuestion >= 2) {
                                                feedback = rtl("רמז: האותות $correctLetter")
                                            }
                                            dinoVisual = LevelDinoVisual.Idle
                                            inputLocked = false
                                        }
                                    else -> {}
                                }
                            },
                            onRoundComplete = {
                                when (session.submitMatchOutcome(true)) {
                                    AnswerResult.Correct ->
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
                                    else -> {}
                                }
                            },
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
private fun RevealLetterTiles(
    options: List<String>,
    correctAnswer: String,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    onRevealPick: (String) -> Unit,
) {
    var revealed by remember(options, correctAnswer, contentKey) { mutableStateOf<Set<Int>>(emptySet()) }
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
                        .clickable(enabled = enabled && !faceUp) {
                            revealed = revealed + idx
                            onRevealPick(letter)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (faceUp) letter else "?",
                    fontSize = if (faceUp) 40.sp else 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                )
            }
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
    var wrongRecoverRunning by remember(options, correctAnswer) { mutableStateOf(false) }

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
                enabled = enabled && !wrongRecoverRunning,
                shouldPop = letter == correctAnswer,
                bobPhaseMillis = idx * 220,
                onPop = {
                    // Only pop the correct balloon (keeps retry possible).
                    alive[letter] = false
                    scope.launch { onPopSfx() }
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
private fun PopBalloon(
    letter: String,
    color: Color,
    enabled: Boolean,
    shouldPop: Boolean,
    bobPhaseMillis: Int,
    onPop: () -> Unit,
    onPickWrong: (Animatable<Float, AnimationVector1D>) -> Unit,
) {
    var popping by remember(letter) { mutableStateOf(false) }
    var visible by remember(letter) { mutableStateOf(true) }
    val scale = remember(letter) { Animatable(1f) }
    val fade = remember(letter) { Animatable(1f) }
    val wrongFall = remember(letter) { Animatable(0f) }
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
        if (!popping) return@LaunchedEffect
        scale.snapTo(1f)
        fade.snapTo(1f)
        scale.animateTo(
            targetValue = 1.12f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        )
        fade.animateTo(
            targetValue = 0f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        )
        visible = false
        onPop()
    }

    if (!visible) return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.offset {
                IntOffset(0, (bob.value + wrongFall.value).roundToInt())
            },
    ) {
        Box(
            modifier =
                Modifier
                    .size(86.dp)
                    .scale(scale.value)
                    .alpha(fade.value)
                    .clickable(
                        enabled = enabled && !popping,
                        onClick = {
                            if (shouldPop) {
                                popping = true
                            } else {
                                onPickWrong(wrongFall)
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

