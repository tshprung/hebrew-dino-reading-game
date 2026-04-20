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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.LessonWordCatalog
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.game.FindLetterGridGame
import com.tal.hebrewdino.ui.game.FinaleGame
import com.tal.hebrewdino.ui.game.ImageMatchGame
import com.tal.hebrewdino.ui.game.MatchLetterToWordGame
import androidx.compose.ui.platform.LocalContext
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.random.Random
import android.os.SystemClock

private enum class GamePhase { Intro, Play }

private enum class DinoVisual { Idle, TryAgain, Jump }

private const val IntroDurationMs = 1_000L
private const val BetweenQuestionFadeMs = 130

@Composable
fun GameScreen(
    stationId: Int,
    chapterId: Int,
    chapterTitle: String,
    stageLabel: String,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    backgroundRes: Int = R.drawable.forest_bg_level_overlay,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // UX: no audio for now (per request).
    val audioEnabled = false

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
    // Removed short-lived particle feedback per UX request.
    var shakeEpoch by remember(stationId) { mutableIntStateOf(0) }
    var dinoTalking by remember(stationId, session.currentIndex) { mutableStateOf(false) }
    val dinoForward = remember(stationId) { Animatable(0f) }
    val dinoSlip = remember(stationId) { Animatable(0f) }
    val dinoTilt = remember(stationId) { Animatable(0f) }
    val successGlow = remember(stationId) { Animatable(0f) }

    // UX: global tap cooldown to prevent fast-tap flow breaks.
    var lastTapMs by remember(stationId) { mutableLongStateOf(0L) }
    fun consumeTapCooldown(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastTapMs < 180L) return false
        lastTapMs = now
        return true
    }
    val jumpFrames =
        remember(stationId) {
            listOf(R.drawable.dino_jump_0, R.drawable.dino_jump_1, R.drawable.dino_jump_2)
        }
    var jumpFrameIndex by remember(stationId) { mutableIntStateOf(0) }
    val forwardDir = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f

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
        if (audioEnabled) sfx.preload(AudioClips.SfxCorrect, AudioClips.SfxWrong, AudioClips.SfxBalloonPop)
        val q = session.currentQuestion ?: return@LaunchedEffect
        launch {
            if (audioEnabled) {
                dinoTalking = true
                try {
                    speakPromptForQuestion(voice, q)
                } finally {
                    dinoTalking = false
                }
            }
        }
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
        if (audioEnabled) ChildGameAudioHooks.onLevelComplete()
        if (audioEnabled) {
            if (isLast) {
                gameFeedback.playSuccessBig()
            } else {
                gameFeedback.playCorrect()
            }
        }
        dinoVisual = DinoVisual.Jump
        if (audioEnabled) voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
        // Progress feel: small forward step after every success.
        dinoForward.animateTo(dinoForward.value + forwardDir * 12f, spring(dampingRatio = 0.75f, stiffness = 520f))
        // Small glow pulse after correct action.
        successGlow.snapTo(0f)
        successGlow.animateTo(1f, tween(110))
        successGlow.animateTo(0f, tween(220))
        playSuccessPulse(scope, dinoScale)
        // UX: short pause before transition.
        delay(400)
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        delay(40)
        session.nextQuestion()
        contentAlpha.animateTo(1f, tween(BetweenQuestionFadeMs))
    }

    fun onWrongFeedback() {
        scope.launch {
            inputLocked = true
            dinoVisual = DinoVisual.TryAgain
            if (chapterId == 2) {
                // Tiny “slip” in the mountains: a playful stumble with no punishment.
                dinoSlip.snapTo(0f)
                dinoTilt.snapTo(0f)
                dinoTilt.animateTo(-7f, tween(90))
                dinoSlip.animateTo(10f, tween(90))
                dinoTilt.animateTo(6f, tween(110))
                dinoSlip.animateTo(-6f, tween(110))
                dinoTilt.animateTo(0f, tween(140))
                dinoSlip.animateTo(0f, tween(140))
            }
            playShake(scope, optionsShake)
            if (audioEnabled) {
                gameFeedback.playWrong()
                ChildGameAudioHooks.onWrong()
                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
            }
            dinoVisual = DinoVisual.Idle
            inputLocked = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (chapterId == 3) {
            // Swamp: a small “friend” becomes more visible as the player progresses.
            val p = (session.currentIndex.toFloat() / session.totalQuestions.coerceAtLeast(1)).coerceIn(0f, 1f)
            Image(
                painter = painterResource(id = R.drawable.dino_idle),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 18.dp, bottom = 96.dp)
                        .size(66.dp)
                        .alpha(0.10f + 0.70f * p),
                contentScale = ContentScale.Fit,
            )
        }
        // Removed “sparkle balls” feedback per UX request.
        // Keep only dino + subtle motion feedback; no short-lived particle overlays.

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
                val targetLetter: String? =
                    when (current) {
                        is Question.FindLetterGridQuestion -> current.targetLetter
                        is Question.PopBalloonsQuestion -> current.correctAnswer
                        is Question.ImageMatchQuestion -> current.targetLetter
                        is Question.FinaleSlotQuestion -> null
                    }
                if (targetLetter != null) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 10.dp)
                                .background(Color.White.copy(alpha = 0.86f), RoundedCornerShape(20.dp))
                                .border(2.dp, Color(0xFF0B2B3D).copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // glow behind letter on success
                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Color(0xFFFFF59D).copy(alpha = 0.30f * successGlow.value),
                                        RoundedCornerShape(20.dp),
                                    ),
                        )
                        Text(
                            text = targetLetter,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
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
                                        if (!consumeTapCooldown()) return@FindLetterGridGame
                                        session.wrongTap()
                                        shakeEpoch += 1
                                        onWrongFeedback()
                                    },
                                    onCompleted = {
                                        if (!consumeTapCooldown()) return@FindLetterGridGame
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
                                    if (plan.mode == com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter) {
                                        LetterOptions(
                                            options = current.options,
                                            enabled = !inputLocked,
                                            shakePx = optionsShake.value,
                                            onPick = { picked ->
                                                if (!consumeTapCooldown()) return@LetterOptions
                                                when (session.submitAnswer(picked)) {
                                                    AnswerResult.Correct -> {
                                                        if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                        scope.launch {
                                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                                            advanceAfterRound(isLast)
                                                        }
                                                    }
                                                    AnswerResult.Wrong -> {
                                                        if (audioEnabled) ChildGameAudioHooks.onWrong()
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
                                                if (!audioEnabled) return@PopBalloonsOptions
                                                scope.launch {
                                                    sfx.playFirstAvailable(
                                                        AudioClips.SfxBalloonPop,
                                                        volume = if (isCorrect) 0.88f else 0.32f,
                                                    )
                                                }
                                            },
                                            onWrongPick = {
                                                if (!consumeTapCooldown()) return@PopBalloonsOptions
                                                // Wrong balloon: feedback only, stay on same question.
                                                session.wrongTap()
                                                shakeEpoch += 1
                                                onWrongFeedback()
                                            },
                                            onAllCorrectPopped = {
                                                if (!consumeTapCooldown()) return@PopBalloonsOptions
                                                // Only advance when ALL correct-letter balloons are popped.
                                                when (session.submitAnswer(current.correctAnswer)) {
                                                    AnswerResult.Correct ->
                                                        scope.launch {
                                                            if (audioEnabled) ChildGameAudioHooks.onCorrect()
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
                                if (stationId == 6) {
                                    val matchChoices =
                                        remember(session.currentIndex, chapterId, letterPoolSpec) {
                                            // Summary station: include multiple letters (max 3 pairs).
                                            val letters = letterPoolSpec.groups.flatten().distinct()
                                            val r = Random(stationId * 7919L + chapterId * 3571L + session.currentIndex * 131L)
                                            val picked = letters.shuffled(r).take(3)
                                            picked.map { l ->
                                                val e = LessonWordCatalog.pickRandom(r, l)
                                                com.tal.hebrewdino.ui.domain.LessonChoice(
                                                    id = e.id,
                                                    letter = e.letter,
                                                    word = e.word,
                                                    tintArgb = e.tintArgb,
                                                    tileDrawable = e.tileRes,
                                                )
                                            }
                                        }
                                    MatchLetterToWordGame(
                                        choices = matchChoices,
                                        enabled = !inputLocked,
                                        onSolved = {
                                            if (!consumeTapCooldown()) return@MatchLetterToWordGame
                                            scope.launch {
                                                when (session.completeCurrentRound()) {
                                                    AnswerResult.Correct -> {
                                                        if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                        val isLast = session.currentIndex >= session.totalQuestions - 1
                                                        advanceAfterRound(isLast)
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    ImageMatchGame(
                                        question = current,
                                        contentKey = session.currentIndex,
                                        enabled = !inputLocked,
                                        shakePx = optionsShake.value,
                                        showWordCaptions = true,
                                        onAttempt = { choiceId ->
                                            if (!consumeTapCooldown()) return@ImageMatchGame false
                                            when (session.submitImageMatch(choiceId)) {
                                                AnswerResult.Correct -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                    scope.launch {
                                                        val isLast = session.currentIndex >= session.totalQuestions - 1
                                                        advanceAfterRound(isLast)
                                                    }
                                                    true
                                                }
                                                AnswerResult.Wrong -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onWrong()
                                                    onWrongFeedback()
                                                    false
                                                }
                                                AnswerResult.Finished -> false
                                            }
                                        },
                                    )
                                }
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
                                                    if (audioEnabled) ChildGameAudioHooks.onCorrect()
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
            val talkFrames =
                if (chapterId == 2) {
                    // “Walk” feel in mountains between rounds.
                    listOf(R.drawable.dino_walk_0, R.drawable.dino_walk_1, R.drawable.dino_walk_2, R.drawable.dino_walk_3)
                } else {
                    listOf(R.drawable.dino_talk_0, R.drawable.dino_talk_1, R.drawable.dino_talk_2, R.drawable.dino_talk_3)
                }
            AnimatedTalkingCharacter(
                idleRes = dinoDrawable,
                talkFrameResIds = talkFrames,
                isTalking = dinoTalking || (chapterId == 2 && inputLocked && dinoVisual == DinoVisual.Jump),
                modifier =
                    Modifier
                        .offset { IntOffset((dinoForward.value + dinoSlip.value).toInt(), 0) }
                        .graphicsLayer { rotationZ = dinoTilt.value }
                        .size(88.dp)
                        .scale(dinoScale.value),
                contentDescription = "דינו",
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
): Job =
    scope.launch {
        dinoScale.snapTo(1f)
        dinoScale.animateTo(1.14f, tween(120))
        dinoScale.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 560f))
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
