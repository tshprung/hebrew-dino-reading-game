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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.feedback.FeedbackSparkles
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.components.learning.PictureLetterMatchBoard
import com.tal.hebrewdino.ui.components.learning.PicturePickAllBoard
import com.tal.hebrewdino.ui.components.learning.PicturePickOneBoard
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class LqDinoVisual { Idle, TryAgain, Jump }

private const val StationQuestionPauseMs = 560L

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterQuizStationScreen(
    stationId: Int,
    chapterTitle: String,
    stageLabel: String,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
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
    var feedback by remember(stationId) { mutableStateOf<String?>(null) }
    fun rtl(text: String) = "\u200F$text"
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    val gameFeedback = remember(stationId, sfx, view) { GameFeedback(scope, sfx, view) }
    val jumpFrames =
        remember(stationId) {
            listOf(R.drawable.dino_jump_0, R.drawable.dino_jump_1, R.drawable.dino_jump_2)
        }
    var dinoVisual by remember(stationId) { mutableStateOf(LqDinoVisual.Idle) }
    var jumpFrameIndex by remember(stationId) { mutableIntStateOf(0) }
    val dinoScale = remember(stationId) { Animatable(1f) }
    val optionsShake = remember(stationId) { Animatable(0f) }
    val contentAlpha = remember(stationId) { Animatable(1f) }
    val showConfetti = remember(stationId) { mutableStateOf(false) }
    var sparkBurst by remember(stationId) { mutableIntStateOf(0) }
    var correctTapEpoch by remember(stationId) { mutableIntStateOf(0) }
    var lastCorrectLetter by remember(stationId) { mutableStateOf<String?>(null) }
    var pickGlowId by remember(stationId) { mutableStateOf<String?>(null) }
    var pickGlowKey by remember(stationId) { mutableIntStateOf(0) }
    var pickAllGlowIds by remember(stationId) { mutableStateOf<Set<String>>(emptySet()) }
    var pickAllGlowKey by remember(stationId) { mutableIntStateOf(0) }
    var inputLocked by remember(stationId) { mutableStateOf(false) }
    var revealWrongSignal by remember(stationId, session.currentIndex) { mutableIntStateOf(0) }

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
            is Question.PicturePickOneQuestion -> current.targetLetter
            is Question.PicturePickAllQuestion -> current.targetLetter
            is Question.PictureLetterMatchQuestion -> current.pairs.joinToString(" ו־") { it.letter }
            null -> ""
        }

    // Do NOT key off currentQuestion alone: between questions nextQuestion() clears the question
    // briefly; snapshotFlow could see null and call onComplete early (crash / exit mid-station).
    LaunchedEffect(stationId) {
        snapshotFlow { session.currentIndex >= session.totalQuestions }.collect { exhausted ->
            // totalQuestions == 0 would make exhausted true immediately (spurious navigate / crash).
            if (exhausted && session.totalQuestions > 0) {
                onComplete(stationId, session.correctCount, session.mistakeCount)
                return@collect
            }
        }
    }

    if (current == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    var wrongAttemptsThisQuestion by remember(stationId, session.currentIndex) { mutableStateOf(0) }
    var picturePickAllEpoch by remember(stationId) { mutableIntStateOf(0) }

    suspend fun speakPrompt(targetLetter: String) {
        val chooseSpecific = AudioClips.chooseLetterClip(targetLetter)
        if (chooseSpecific != null) {
            voice.playBlocking(chooseSpecific)
        } else {
            voice.playBlocking(AudioClips.VoChooseLetter)
        }
    }

    suspend fun speakPromptForQuestion(q: Question) {
        when (q) {
            is Question.PictureLetterMatchQuestion -> voice.playBlocking(AudioClips.VoChooseLetter)
            is Question.TapChoiceQuestion -> speakPrompt(q.correctAnswer)
            is Question.PopBalloonsQuestion -> speakPrompt(q.correctAnswer)
            is Question.RevealTilesQuestion -> speakPrompt(q.correctAnswer)
            is Question.PicturePickOneQuestion -> speakPrompt(q.targetLetter)
            is Question.PicturePickAllQuestion -> speakPrompt(q.targetLetter)
        }
    }

    LaunchedEffect(dinoVisual) {
        if (dinoVisual != LqDinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            jumpFrameIndex = i % jumpFrames.size
            delay(85)
        }
        dinoVisual = LqDinoVisual.Idle
    }

    LaunchedEffect(stationId, session.currentIndex) {
        val q = session.currentQuestion
        feedback = null
        wrongAttemptsThisQuestion = 0
        picturePickAllEpoch = 0
        pickGlowId = null
        pickAllGlowIds = emptySet()
        sfx.preload(AudioClips.SfxCorrect, AudioClips.SfxWrong, AudioClips.SfxBalloonPop)
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
        val bob by rememberInfiniteTransition(label = "bobLq").animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "bobOffsetLq",
        )
        if (showConfetti.value) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize())
        }
        FeedbackSparkles(
            burstKey = sparkBurst,
            modifier = Modifier.fillMaxSize(),
            seed = stationId,
        )
        val dinoDrawable =
            when (dinoVisual) {
                LqDinoVisual.Idle -> R.drawable.dino_idle
                LqDinoVisual.TryAgain -> R.drawable.dino_try_again
                LqDinoVisual.Jump -> jumpFrames[jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
            }
        val dinoBobY = if (dinoVisual == LqDinoVisual.Idle) bob.dp else 0.dp

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                OutlinedButton(
                    onClick = onBack,
                    colors =
                        androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) { Text("חזור") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stageLabel,
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
                progress = {
                    (questionNumber.toFloat() / totalQuestions.coerceAtLeast(1)).coerceIn(0f, 1f)
                },
                modifier = Modifier.width(320.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextButton(
                    onClick = {
                        if (inputLocked) return@TextButton
                        scope.launch { speakPromptForQuestion(current) }
                    },
                ) { Text("שמע/י שוב") }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text =
                        when (current) {
                            is Question.PictureLetterMatchQuestion ->
                                "חבר כל תמונה למילה שמתחילה באות המתאימה (תמונה ואז אות)"
                            is Question.PicturePickOneQuestion ->
                                "בחרו את התמונה של המילה שמתחילה באות: " + current.targetLetter
                            is Question.PicturePickAllQuestion ->
                                "בחרו את שתי התמונות של מילים שמתחילות באות: " + current.targetLetter
                            is Question.RevealTilesQuestion ->
                                "לחצו על כרטיסייה כדי לחשוף, ואז מצאו את האות: " + current.correctAnswer
                            is Question.TapChoiceQuestion -> "בחר את האות: " + current.correctAnswer
                            is Question.PopBalloonsQuestion -> "בחר את האות: " + current.correctAnswer
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

                val isReveal = current is Question.RevealTilesQuestion
                val onPick: (String) -> Unit = { picked ->
                    when (session.submitAnswer(picked)) {
                        AnswerResult.Correct -> {
                            scope.launch {
                                inputLocked = true
                                val isLast = session.currentIndex >= session.totalQuestions - 1
                                if (isLast) {
                                    gameFeedback.playSuccessBig()
                                } else {
                                    gameFeedback.playCorrect()
                                    sparkBurst += 1
                                }
                                lastCorrectLetter = picked
                                correctTapEpoch += 1
                                if (isReveal) delay(680)
                                dinoVisual = LqDinoVisual.Jump
                                feedback = rtl("כל הכבוד!")
                                playLqSuccess(scope, dinoScale, showConfetti)
                                voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                                contentAlpha.animateTo(0f, tween(durationMillis = 200))
                                delay(StationQuestionPauseMs)
                                session.nextQuestion()
                                contentAlpha.animateTo(1f, tween(durationMillis = 260))
                                inputLocked = false
                            }
                        }
                        AnswerResult.Wrong -> {
                            scope.launch {
                                inputLocked = true
                                dinoVisual = LqDinoVisual.TryAgain
                                wrongAttemptsThisQuestion += 1
                                feedback = "כמעט… בוא ננסה שוב"
                                playLqMistake(scope, optionsShake)
                                gameFeedback.playWrong()
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                if (wrongAttemptsThisQuestion >= 2) {
                                    feedback = rtl("רמז: $correctLetter")
                                }
                                dinoVisual = LqDinoVisual.Idle
                                inputLocked = false
                                if (isReveal) {
                                    delay(1200)
                                    revealWrongSignal++
                                }
                            }
                        }
                        AnswerResult.Finished -> {}
                    }
                }

                when (current) {
                    is Question.TapChoiceQuestion ->
                        LetterOptions(
                            options = current.options,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            correctPulseLetter = lastCorrectLetter,
                            correctPulseEpoch = correctTapEpoch,
                            onPick = onPick,
                        )
                    is Question.PopBalloonsQuestion ->
                        PopBalloonsOptions(
                            options = current.options,
                            correctAnswer = current.correctAnswer,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            onPopSfx = { isCorrect ->
                                if (isCorrect) {
                                    gameFeedback.playCorrect()
                                    sparkBurst += 1
                                    sfx.playFirstAvailable(AudioClips.SfxBalloonPop, volume = 0.88f)
                                } else {
                                    gameFeedback.playWrong()
                                    sfx.playFirstAvailable(AudioClips.SfxBalloonPop, volume = 0.32f)
                                }
                            },
                            onPick = onPick,
                        )
                    is Question.RevealTilesQuestion ->
                        RevealLetterTiles(
                            options = current.options,
                            correctAnswer = current.correctAnswer,
                            contentKey = session.currentIndex,
                            wrongRevealSignal = revealWrongSignal,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            onRevealPick = onPick,
                        )
                    is Question.PicturePickOneQuestion ->
                        PicturePickOneBoard(
                            question = current,
                            contentKey = session.currentIndex,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            successChoiceId = pickGlowId,
                            successKey = pickGlowKey,
                            onPickId = { id ->
                                when (session.submitPicturePickOne(id)) {
                                    AnswerResult.Correct ->
                                        scope.launch {
                                            inputLocked = true
                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                            if (isLast) gameFeedback.playSuccessBig() else {
                                                gameFeedback.playCorrect()
                                                sparkBurst += 1
                                            }
                                            pickGlowId = id
                                            pickGlowKey += 1
                                            dinoVisual = LqDinoVisual.Jump
                                            feedback = rtl("כל הכבוד!")
                                            playLqSuccess(scope, dinoScale, showConfetti)
                                            voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                                            delay(420)
                                            contentAlpha.animateTo(0f, tween(durationMillis = 200))
                                            delay(StationQuestionPauseMs)
                                            session.nextQuestion()
                                            contentAlpha.animateTo(1f, tween(durationMillis = 260))
                                            inputLocked = false
                                        }
                                    AnswerResult.Wrong ->
                                        scope.launch {
                                            inputLocked = true
                                            dinoVisual = LqDinoVisual.TryAgain
                                            wrongAttemptsThisQuestion += 1
                                            feedback = "כמעט… ננסה שוב"
                                            playLqMistake(scope, optionsShake)
                                            gameFeedback.playWrong()
                                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                            if (wrongAttemptsThisQuestion >= 2) {
                                                feedback = rtl("רמז: $correctLetter")
                                            }
                                            dinoVisual = LqDinoVisual.Idle
                                            inputLocked = false
                                        }
                                    AnswerResult.Finished -> {}
                                }
                            },
                        )
                    is Question.PicturePickAllQuestion ->
                        PicturePickAllBoard(
                            question = current,
                            contentKey = session.currentIndex,
                            resetEpoch = picturePickAllEpoch,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            successChoiceIds = pickAllGlowIds,
                            successKey = pickAllGlowKey,
                            onTwoPicked = { picked ->
                                when (session.submitPicturePickAll(picked)) {
                                    AnswerResult.Correct ->
                                        scope.launch {
                                            inputLocked = true
                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                            if (isLast) gameFeedback.playSuccessBig() else {
                                                gameFeedback.playCorrect()
                                                sparkBurst += 1
                                            }
                                            pickAllGlowIds = picked
                                            pickAllGlowKey += 1
                                            dinoVisual = LqDinoVisual.Jump
                                            feedback = rtl("כל הכבוד!")
                                            playLqSuccess(scope, dinoScale, showConfetti)
                                            voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                                            delay(420)
                                            contentAlpha.animateTo(0f, tween(durationMillis = 200))
                                            delay(StationQuestionPauseMs)
                                            session.nextQuestion()
                                            contentAlpha.animateTo(1f, tween(durationMillis = 260))
                                            inputLocked = false
                                        }
                                    AnswerResult.Wrong -> {
                                        picturePickAllEpoch++
                                        scope.launch {
                                            inputLocked = true
                                            dinoVisual = LqDinoVisual.TryAgain
                                            wrongAttemptsThisQuestion += 1
                                            feedback = "עוד ניסיון — בחרו שתי תמונות נכונות"
                                            gameFeedback.playWrong()
                                            delay(650)
                                            dinoVisual = LqDinoVisual.Idle
                                            inputLocked = false
                                        }
                                    }
                                    AnswerResult.Finished -> {}
                                }
                            },
                        )
                    is Question.PictureLetterMatchQuestion ->
                        PictureLetterMatchBoard(
                            question = current,
                            contentKey = session.currentIndex,
                            enabled = !inputLocked,
                            shakePx = optionsShake.value,
                            chapter1FinalePresentation =
                                stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH,
                            onSoftLetterMismatch =
                                if (stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                    {
                                        scope.launch {
                                            wrongAttemptsThisQuestion += 1
                                            feedback = "נסו אות אחרת…"
                                            gameFeedback.playWrong()
                                            delay(420)
                                            feedback = null
                                        }
                                    }
                                } else {
                                    null
                                },
                            onWrongPair = {
                                when (session.submitMatchOutcome(false)) {
                                    AnswerResult.Wrong ->
                                        scope.launch {
                                            inputLocked = true
                                            dinoVisual = LqDinoVisual.TryAgain
                                            wrongAttemptsThisQuestion += 1
                                            feedback = "זוג לא מתאים — ננסה שוב"
                                            playLqMistake(scope, optionsShake)
                                            gameFeedback.playWrong()
                                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                            if (wrongAttemptsThisQuestion >= 2) {
                                                feedback = rtl("רמז: האותות $correctLetter")
                                            }
                                            dinoVisual = LqDinoVisual.Idle
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
                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                            if (isLast) gameFeedback.playSuccessBig() else {
                                                gameFeedback.playCorrect()
                                                sparkBurst += 1
                                            }
                                            dinoVisual = LqDinoVisual.Jump
                                            feedback = rtl("כל הכבוד!")
                                            playLqSuccess(scope, dinoScale, showConfetti)
                                            voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                                            contentAlpha.animateTo(0f, tween(durationMillis = 200))
                                            delay(StationQuestionPauseMs)
                                            session.nextQuestion()
                                            contentAlpha.animateTo(1f, tween(durationMillis = 260))
                                            inputLocked = false
                                        }
                                    else -> {}
                                }
                            },
                        )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    painter = painterResource(id = dinoDrawable),
                    contentDescription = null,
                    modifier =
                        Modifier
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

private fun playLqSuccess(
    scope: CoroutineScope,
    dinoScale: Animatable<Float, AnimationVector1D>,
    showConfetti: MutableState<Boolean>,
): Job =
    scope.launch {
        showConfetti.value = true
        dinoScale.snapTo(1f)
        dinoScale.animateTo(1.18f, tween(140))
        dinoScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 600f))
        delay(450)
        showConfetti.value = false
    }

private fun playLqMistake(scope: CoroutineScope, optionsShake: Animatable<Float, AnimationVector1D>): Job =
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
