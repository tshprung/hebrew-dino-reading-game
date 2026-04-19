package com.tal.hebrewdino.ui.components.learning

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationQuizMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class MatchDinoVisual { Idle, TryAgain, Jump }

private const val MatchStationPauseMs = 560L

private fun playBoardShake(scope: CoroutineScope, shake: Animatable<Float, AnimationVector1D>): Job =
    scope.launch {
        shake.snapTo(0f)
        val amp = 18f
        repeat(5) { i ->
            shake.animateTo(if (i % 2 == 0) amp else -amp, tween(durationMillis = 45))
        }
        shake.animateTo(0f, tween(durationMillis = 60))
    }

/**
 * Full-screen picture–letter matching finale (several rounds), shared by chapters 2–3.
 */
@Composable
fun PictureLetterMatchStation(
    stationId: Int,
    chapterTitle: String,
    questionCount: Int,
    initialGroupIndex: Int,
    letterPoolSpec: LetterPoolSpec,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val session =
        remember(stationId, questionCount, initialGroupIndex, letterPoolSpec) {
            LevelSession(
                questionCount = questionCount,
                initialGroupIndex = initialGroupIndex,
                quizMode = StationQuizMode.PictureLetterMatch,
                letterPoolSpec = letterPoolSpec,
            )
        }
    var feedback by remember(stationId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    val gameFeedback = remember(stationId, sfx, view) { GameFeedback(scope, sfx, view) }
    val boardShake = remember(stationId) { Animatable(0f) }
    val contentAlpha = remember(stationId) { Animatable(1f) }
    var dinoVisual by remember(stationId) { mutableStateOf(MatchDinoVisual.Idle) }
    var inputLocked by remember(stationId) { mutableStateOf(false) }
    var wrongAttemptsThisQuestion by remember(stationId, session.currentIndex) { mutableStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
            sfx.release()
        }
    }

    val current = session.currentQuestion
    val totalQuestions = session.totalQuestions
    val questionNumber = session.questionNumber

    val correctLettersHint =
        when (current) {
            is Question.PictureLetterMatchQuestion ->
                current.pairs.joinToString(" ו־") { it.letter }
            else -> ""
        }

    LaunchedEffect(stationId) {
        snapshotFlow { session.currentIndex >= session.totalQuestions }.collect { exhausted ->
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

    suspend fun speakPromptForQuestion(q: Question) {
        when (q) {
            is Question.PictureLetterMatchQuestion -> voice.playBlocking(AudioClips.VoChooseLetter)
            is Question.TapChoiceQuestion -> {
                val c = AudioClips.chooseLetterClip(q.correctAnswer)
                if (c != null) voice.playBlocking(c) else voice.playBlocking(AudioClips.VoChooseLetter)
            }
            is Question.PopBalloonsQuestion -> {
                val c = AudioClips.chooseLetterClip(q.correctAnswer)
                if (c != null) voice.playBlocking(c) else voice.playBlocking(AudioClips.VoChooseLetter)
            }
            is Question.RevealTilesQuestion -> {
                val c = AudioClips.chooseLetterClip(q.correctAnswer)
                if (c != null) voice.playBlocking(c) else voice.playBlocking(AudioClips.VoChooseLetter)
            }
            is Question.PicturePickOneQuestion -> {
                val c = AudioClips.chooseLetterClip(q.targetLetter)
                if (c != null) voice.playBlocking(c) else voice.playBlocking(AudioClips.VoChooseLetter)
            }
            is Question.PicturePickAllQuestion -> {
                val c = AudioClips.chooseLetterClip(q.targetLetter)
                if (c != null) voice.playBlocking(c) else voice.playBlocking(AudioClips.VoChooseLetter)
            }
        }
    }

    LaunchedEffect(stationId, session.currentIndex) {
        val q = session.currentQuestion
        feedback = null
        wrongAttemptsThisQuestion = 0
        sfx.preload(AudioClips.SfxCorrect, AudioClips.SfxWrong, AudioClips.SfxBalloonPop)
        delay(120)
        if (q != null) speakPromptForQuestion(q)
    }

    val dinoDrawable =
        when (dinoVisual) {
            MatchDinoVisual.Idle -> R.drawable.dino_idle
            MatchDinoVisual.TryAgain -> R.drawable.dino_try_again
            MatchDinoVisual.Jump -> R.drawable.dino_jump_1
        }

    fun rtl(text: String) = "\u200F$text"

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_level_overlay),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Text(
                text = "תחנה $stationId — חיבור תמונה לאות",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "סיבוב $questionNumber מתוך $totalQuestions",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0B2B3D),
            )
            LinearProgressIndicator(
                progress = {
                    (questionNumber.toFloat() / totalQuestions.coerceAtLeast(1)).coerceIn(0f, 1f)
                },
                modifier = Modifier.width(300.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = {
                    if (inputLocked) return@TextButton
                    val q = current
                    scope.launch { speakPromptForQuestion(q) }
                },
            ) { Text("שמע/י שוב") }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "חבר כל תמונה למילה שמתחילה באות המתאימה (הקישו תמונה ואז אות)",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feedback ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (current) {
                is Question.PictureLetterMatchQuestion -> {
                    PictureLetterMatchBoard(
                        question = current,
                        contentKey = session.currentIndex,
                        enabled = !inputLocked,
                        shakePx = boardShake.value,
                        onWrongPair = {
                            when (session.submitMatchOutcome(false)) {
                                AnswerResult.Wrong ->
                                    scope.launch {
                                        inputLocked = true
                                        dinoVisual = MatchDinoVisual.TryAgain
                                        wrongAttemptsThisQuestion += 1
                                        feedback = "זוג לא מתאים — ננסה שוב"
                                        playBoardShake(scope, boardShake)
                                        gameFeedback.playWrong()
                                        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                        if (wrongAttemptsThisQuestion >= 2) {
                                            feedback = rtl("רמז: האותות $correctLettersHint")
                                        }
                                        dinoVisual = MatchDinoVisual.Idle
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
                                        if (isLast) gameFeedback.playSuccessBig() else gameFeedback.playCorrect()
                                        dinoVisual = MatchDinoVisual.Jump
                                        feedback = rtl("כל הכבוד!")
                                        voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                                        contentAlpha.animateTo(0f, tween(durationMillis = 200))
                                        delay(MatchStationPauseMs)
                                        session.nextQuestion()
                                        contentAlpha.animateTo(1f, tween(durationMillis = 260))
                                        dinoVisual = MatchDinoVisual.Idle
                                        inputLocked = false
                                    }
                                else -> {}
                            }
                        },
                    )
                }
                else -> {
                    /* LevelSession is in PictureLetterMatch mode; should not happen */
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = dinoDrawable),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onBack) { Text("חזרה לדרך") }
        }
    }
}
