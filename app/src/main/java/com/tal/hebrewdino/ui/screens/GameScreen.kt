package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.feedback.FeedbackSparkles
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.game.FindLetterGridGame
import com.tal.hebrewdino.ui.game.FinaleGame
import com.tal.hebrewdino.ui.game.ImageMatchGame
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

private enum class GamePhase { Intro, Play }

private enum class DinoVisual { Idle, TryAgain, Jump }

private const val IntroDurationMs = 1_000L
private const val BetweenQuestionFadeMs = 130

@Composable
fun GameScreen(
    stationId: Int,
    chapterTitle: String,
    stageLabel: String,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val session =
        remember(stationId, plan) {
            LevelSession(
                questionCount = plan.questionCount,
                initialGroupIndex = plan.initialGroupIndex,
                quizMode = plan.mode,
                letterPoolSpec = letterPoolSpec,
            )
        }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    val gameFeedback = remember(stationId, sfx, view) { GameFeedback(scope, sfx, view) }

    var phase by remember(stationId) { mutableStateOf(GamePhase.Intro) }
    var inputLocked by remember(stationId) { mutableStateOf(true) }
    val contentAlpha = remember(stationId) { Animatable(1f) }
    val optionsShake = remember(stationId) { Animatable(0f) }
    var dinoVisual by remember(stationId) { mutableStateOf(DinoVisual.Idle) }
    val dinoScale = remember(stationId) { Animatable(1f) }
    var sparkBurst by remember(stationId) { mutableIntStateOf(0) }
    var shakeEpoch by remember(stationId) { mutableIntStateOf(0) }
    val jumpFrames =
        remember(stationId) {
            listOf(R.drawable.dino_jump_0, R.drawable.dino_jump_1, R.drawable.dino_jump_2)
        }
    var jumpFrameIndex by remember(stationId) { mutableIntStateOf(0) }
    val showConfetti = remember(stationId) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
            sfx.release()
        }
    }

    LaunchedEffect(stationId) {
        snapshotFlow { session.currentIndex >= session.totalQuestions }.collect { exhausted ->
            if (exhausted && session.totalQuestions > 0) {
                onComplete(stationId, session.correctCount, session.mistakeCount)
            }
        }
    }

    val current = session.currentQuestion
    if (current == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    LaunchedEffect(stationId, session.currentIndex) {
        phase = GamePhase.Intro
        inputLocked = true
        sfx.preload(AudioClips.SfxCorrect, AudioClips.SfxWrong, AudioClips.SfxBalloonPop)
        val q = session.currentQuestion ?: return@LaunchedEffect
        launch { speakPromptForQuestion(voice, q) }
        delay(IntroDurationMs)
        phase = GamePhase.Play
        inputLocked = false
    }

    LaunchedEffect(dinoVisual) {
        if (dinoVisual != DinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            jumpFrameIndex = i % jumpFrames.size
            delay(85)
        }
        dinoVisual = DinoVisual.Idle
    }

    suspend fun advanceAfterRound(isLast: Boolean) {
        inputLocked = true
        ChildGameAudioHooks.onLevelComplete()
        if (isLast) {
            gameFeedback.playSuccessBig()
        } else {
            gameFeedback.playCorrect()
            sparkBurst += 1
        }
        dinoVisual = DinoVisual.Jump
        voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
        playSuccessPulse(scope, dinoScale, showConfetti)
        delay(220)
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        delay(40)
        session.nextQuestion()
        contentAlpha.animateTo(1f, tween(BetweenQuestionFadeMs))
    }

    fun onWrongFeedback() {
        scope.launch {
            inputLocked = true
            dinoVisual = DinoVisual.TryAgain
            playShake(scope, optionsShake)
            gameFeedback.playWrong()
            ChildGameAudioHooks.onWrong()
            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
            dinoVisual = DinoVisual.Idle
            inputLocked = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_level_overlay),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (showConfetti.value) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize())
        }
        FeedbackSparkles(
            burstKey = sparkBurst,
            modifier = Modifier.fillMaxSize(),
            seed = stationId,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(4f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(0.dp)) {
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(22.dp))
                            .border(2.dp, Color(0xFF0B2B3D).copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("חזור", fontSize = 16.sp, color = Color(0xFF0B2B3D), fontWeight = FontWeight.Black)
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "שאלה ${session.questionNumber} מתוך ${session.totalQuestions}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0B2B3D),
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        (session.questionNumber.toFloat() / session.totalQuestions.coerceAtLeast(1))
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = Color(0xFF2E7D32),
                    trackColor = Color(0xFF0B2B3D).copy(alpha = 0.12f),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onLettersHelp != null) {
                    OutlinedButton(
                        onClick = onLettersHelp,
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) { Text("אותיות", style = MaterialTheme.typography.labelLarge) }
                }
                if (onDebugStationAdvance != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onDebugStationAdvance,
                        modifier = Modifier.height(40.dp),
                    ) { Text("בדיקה", style = MaterialTheme.typography.labelLarge) }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 52.dp, bottom = 8.dp)
                    .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                    contentAlignment = Alignment.Center,
                ) {
                    if (phase == GamePhase.Intro) {
                        IntroPulse(stationId = stationId, question = current, modifier = Modifier.fillMaxWidth())
                    } else {
                        when (current) {
                            is Question.FindLetterGridQuestion ->
                                FindLetterGridGame(
                                    question = current,
                                    onCellTapped = { _ ->
                                        session.wrongTap()
                                        shakeEpoch += 1
                                        onWrongFeedback()
                                    },
                                    onCompleted = {
                                        scope.launch {
                                            when (session.completeCurrentRound()) {
                                                AnswerResult.Correct -> {
                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                    advanceAfterRound(isLast)
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    enabled = !inputLocked,
                                    contentKey = session.currentIndex,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .offset { IntOffset(optionsShake.value.toInt(), 0) },
                                )
                            is Question.PopBalloonsQuestion ->
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Keep the target letter visible throughout.
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(top = 2.dp, bottom = 10.dp)
                                                .background(Color.White.copy(alpha = 0.86f), RoundedCornerShape(18.dp))
                                                .border(
                                                    2.dp,
                                                    Color(0xFF0B2B3D).copy(alpha = 0.18f),
                                                    RoundedCornerShape(18.dp),
                                                )
                                                .padding(horizontal = 18.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = current.correctAnswer,
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0B2B3D),
                                            textAlign = TextAlign.Center,
                                        )
                                    }

                                    if (plan.mode == com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter) {
                                        LetterOptions(
                                            options = current.options,
                                            enabled = !inputLocked,
                                            shakePx = optionsShake.value,
                                            onPick = { picked ->
                                                when (session.submitAnswer(picked)) {
                                                    AnswerResult.Correct -> {
                                                        ChildGameAudioHooks.onCorrect()
                                                        scope.launch {
                                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                                            advanceAfterRound(isLast)
                                                        }
                                                    }
                                                    AnswerResult.Wrong -> {
                                                        ChildGameAudioHooks.onWrong()
                                                        onWrongFeedback()
                                                    }
                                                    AnswerResult.Finished -> {}
                                                }
                                            },
                                        )
                                    } else {
                                        PopBalloonsOptions(
                                            options = current.options,
                                            correctAnswer = current.correctAnswer,
                                            enabled = !inputLocked,
                                            shakePx = optionsShake.value,
                                            onPopSfx = { isCorrect ->
                                                // SFX only; no suspend call needed.
                                                scope.launch {
                                                    sfx.playFirstAvailable(
                                                        AudioClips.SfxBalloonPop,
                                                        volume = if (isCorrect) 0.88f else 0.32f,
                                                    )
                                                }
                                            },
                                            onWrongPick = {
                                                // Wrong balloon: feedback only, stay on same question.
                                                session.wrongTap()
                                                shakeEpoch += 1
                                                onWrongFeedback()
                                            },
                                            onAllCorrectPopped = {
                                                // Only advance when ALL correct-letter balloons are popped.
                                                when (session.submitAnswer(current.correctAnswer)) {
                                                    AnswerResult.Correct ->
                                                        scope.launch {
                                                            ChildGameAudioHooks.onCorrect()
                                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                                            advanceAfterRound(isLast)
                                                        }
                                                    else -> {}
                                                }
                                            },
                                        )
                                    }
                                }
                            is Question.ImageMatchQuestion ->
                                ImageMatchGame(
                                    question = current,
                                    contentKey = session.currentIndex,
                                    enabled = !inputLocked,
                                    shakePx = optionsShake.value,
                                    showWordCaptions = true,
                                    onAttempt = { choiceId ->
                                        when (session.submitImageMatch(choiceId)) {
                                            AnswerResult.Correct -> {
                                                ChildGameAudioHooks.onCorrect()
                                                scope.launch {
                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                    advanceAfterRound(isLast)
                                                }
                                                true
                                            }
                                            AnswerResult.Wrong -> {
                                                ChildGameAudioHooks.onWrong()
                                                onWrongFeedback()
                                                false
                                            }
                                            AnswerResult.Finished -> false
                                        }
                                    },
                                )
                            is Question.FinaleSlotQuestion ->
                                FinaleGame(
                                    question = current,
                                    contentKey = session.currentIndex,
                                    enabled = !inputLocked,
                                    shakeEpoch = shakeEpoch,
                                    onWrongPlacement = {
                                        session.wrongTap()
                                        shakeEpoch += 1
                                        onWrongFeedback()
                                    },
                                    onSolved = { words ->
                                        scope.launch {
                                            when (session.submitFinaleWords(words)) {
                                                AnswerResult.Correct -> {
                                                    ChildGameAudioHooks.onCorrect()
                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                    advanceAfterRound(isLast)
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .offset { IntOffset(optionsShake.value.toInt(), 0) },
                                )
                        }
                    }
                }
            }
            val dinoDrawable =
                when (dinoVisual) {
                    DinoVisual.Idle -> R.drawable.dino_idle
                    DinoVisual.TryAgain -> R.drawable.dino_try_again
                    DinoVisual.Jump -> jumpFrames[jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
                }
            Image(
                painter = painterResource(id = dinoDrawable),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(88.dp)
                        .scale(dinoScale.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun IntroPulse(
    stationId: Int,
    question: Question,
    modifier: Modifier = Modifier,
) {
    val pulse =
        rememberInfiniteTransition(label = "introPulse").animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
            label = "introPulseScale",
        )
    val label =
        when (question) {
            is Question.FindLetterGridQuestion -> question.targetLetter
            is Question.PopBalloonsQuestion -> question.correctAnswer
            is Question.ImageMatchQuestion ->
                if (stationId in 4..6) question.targetLetter else question.targetWord
            is Question.FinaleSlotQuestion -> "★"
        }
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val narrow = maxWidth < 380.dp
        val medium = maxWidth < 520.dp
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = label,
                fontSize =
                    when (question) {
                        is Question.ImageMatchQuestion ->
                            when {
                                stationId in 4..6 && narrow -> 52.sp
                                stationId in 4..6 -> 64.sp
                                narrow -> 44.sp
                                medium -> 48.sp
                                else -> 56.sp
                            }
                        is Question.FinaleSlotQuestion ->
                            if (narrow) 54.sp else 72.sp
                        is Question.FindLetterGridQuestion ->
                            if (narrow) 68.sp else 87.sp
                        else ->
                            if (narrow) 72.sp else 96.sp
                    },
                fontWeight = FontWeight.Black,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(pulse.value),
                maxLines = 2,
            )
            if (question is Question.FinaleSlotQuestion) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "בנו את המילים",
                    style =
                        if (narrow) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private suspend fun speakLetterPrompt(
    voice: VoicePlayer,
    letter: String,
) {
    val chooseSpecific = AudioClips.chooseLetterClip(letter)
    if (chooseSpecific != null) {
        voice.playBlocking(chooseSpecific)
    } else {
        voice.playBlocking(AudioClips.VoChooseLetter)
    }
}

private suspend fun speakPromptForQuestion(
    voice: VoicePlayer,
    q: Question,
) {
    when (q) {
        is Question.PopBalloonsQuestion -> speakLetterPrompt(voice, q.correctAnswer)
        is Question.FindLetterGridQuestion -> speakLetterPrompt(voice, q.targetLetter)
        is Question.ImageMatchQuestion -> speakLetterPrompt(voice, q.targetLetter)
        is Question.FinaleSlotQuestion -> voice.playBlocking(AudioClips.VoChooseLetter)
    }
}

private fun playSuccessPulse(
    scope: CoroutineScope,
    dinoScale: Animatable<Float, AnimationVector1D>,
    showConfetti: MutableState<Boolean>,
): Job =
    scope.launch {
        showConfetti.value = true
        dinoScale.snapTo(1f)
        dinoScale.animateTo(1.14f, tween(120))
        dinoScale.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 560f))
        delay(380)
        showConfetti.value = false
    }

private fun playShake(
    scope: CoroutineScope,
    optionsShake: Animatable<Float, AnimationVector1D>,
): Job =
    scope.launch {
        optionsShake.snapTo(0f)
        val amp = 18f
        repeat(5) { i ->
            optionsShake.animateTo(
                if (i % 2 == 0) amp else -amp,
                tween(45),
            )
        }
        optionsShake.animateTo(0f, tween(60))
    }
