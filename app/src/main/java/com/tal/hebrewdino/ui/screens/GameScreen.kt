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
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.Chapter1Station4PictureInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
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
import com.tal.hebrewdino.ui.game.PictureStartsWithGame
import androidx.compose.ui.platform.LocalContext
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import android.os.SystemClock

private enum class GamePhase { Intro, Play }

private enum class DinoVisual { Idle, TryAgain, Jump }

private const val IntroDurationMs = 450L
private const val BetweenQuestionFadeMs = 80

/** Episode 1 station 2: start target letter voice this far through the intro clip (overlap = shorter gap). */
private const val Station2IntroLetterLeadFraction = 0.72f

/** Episode 1: chance to play a short praise voice after a correct round. */
private const val Episode1PraiseChance = 0.62f

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
    /** Replay of an already-completed station: no extra in-game dino motion after correct answers. */
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // UX: no audio for now (per request).
    val audioEnabled = true

    val session =
        remember(stationId, plan) {
            LevelSession(plan = plan, letterPoolSpec = letterPoolSpec)
        }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    val gameFeedback = remember(stationId, sfx, view) { GameFeedback(scope, sfx, view) }
    var completionCallbackFired by remember(stationId) { mutableStateOf(false) }

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
    var wrongTapsThisQuestion by remember(stationId) { mutableIntStateOf(0) }
    var hintPulseEpoch by remember(stationId) { mutableIntStateOf(0) }
    val hintHeaderScale = remember(stationId) { Animatable(1f) }
    var entryPulseEpoch by remember(stationId) { mutableIntStateOf(0) }
    val entryPulseScale = remember(stationId) { Animatable(1f) }
    var correctTapPulseEpoch by remember(stationId) { mutableIntStateOf(0) }
    var correctTapPulseLetter by remember(stationId) { mutableStateOf<String?>(null) }
    var feedbackVoiceJob by remember(stationId) { mutableStateOf<Job?>(null) }
    var promptVoiceJob by remember(stationId) { mutableStateOf<Job?>(null) }
    var station1VoiceStreamId by remember(stationId) { mutableIntStateOf(0) }
    /** Episode 1 station 2: Hebrew feedback played via SoundPool (same pattern as station 1). */
    var station2VoiceStreamId by remember(stationId) { mutableIntStateOf(0) }
    /** Episode 1 station 3: SoundPool voice for ultra-low-latency letter feedback. */
    var station3VoiceStreamId by remember(stationId) { mutableIntStateOf(0) }
    /**
     * Episode 1 station 2: after the last correct balloon, show a small balloon (last pop) beside the
     * main target chip until round-end praise finishes — not a second letter chip.
     */
    var station2PinnedBalloonLetter by remember(stationId) { mutableStateOf<String?>(null) }
    var station2PinnedBalloonColor by remember(stationId) { mutableStateOf<Color?>(null) }
    /** Episode 1 station 2: counts correct pops within the current question (for pop SFX variety). */
    var station2CorrectPopCount by remember(stationId, session.currentIndex) { mutableIntStateOf(0) }
    var station1PinnedCorrectLetter by remember(stationId) { mutableStateOf<String?>(null) }

    fun cancelFeedbackVoice() {
        feedbackVoiceJob?.cancel()
        feedbackVoiceJob = null
        promptVoiceJob?.cancel()
        promptVoiceJob = null
        voice.stopNow()
        if (chapterId == 1 && stationId == 1) {
            sfx.stopStream(station1VoiceStreamId)
            station1VoiceStreamId = 0
        }
        if (chapterId == 1 && stationId == 2) {
            sfx.stopStream(station2VoiceStreamId)
            station2VoiceStreamId = 0
        }
        if (chapterId == 1 && stationId == 3) {
            sfx.stopStream(station3VoiceStreamId)
            station3VoiceStreamId = 0
        }
    }

    // UX: global tap cooldown to prevent fast-tap flow breaks.
    var lastTapMs by remember(stationId) { mutableLongStateOf(0L) }
    fun consumeTapCooldown(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastTapMs < 130L) return false
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

    // Station 1: preload ALL voice clips as early as possible (screen entry),
    // so instruction playback has near-zero latency when the first question appears.
    LaunchedEffect(stationId, chapterId) {
        if (!(audioEnabled && chapterId == 1 && stationId == 1)) return@LaunchedEffect
        sfx.preload(
            // Instruction clips (choose_<letter>)
            AudioClips.chooseLetterClip("א") ?: "",
            AudioClips.chooseLetterClip("ב") ?: "",
            AudioClips.chooseLetterClip("ג") ?: "",
            AudioClips.chooseLetterClip("ד") ?: "",
            AudioClips.chooseLetterClip("ה") ?: "",
            AudioClips.chooseLetterClip("ל") ?: "",
            AudioClips.chooseLetterClip("מ") ?: "",
            // Combined feedback clips (st1_wrong/st1_correct)
            AudioClips.station1WrongCombined("א") ?: "",
            AudioClips.station1WrongCombined("ב") ?: "",
            AudioClips.station1WrongCombined("ג") ?: "",
            AudioClips.station1WrongCombined("ד") ?: "",
            AudioClips.station1WrongCombined("ה") ?: "",
            AudioClips.station1WrongCombined("ל") ?: "",
            AudioClips.station1WrongCombined("מ") ?: "",
            AudioClips.station1CorrectCombined("א") ?: "",
            AudioClips.station1CorrectCombined("ב") ?: "",
            AudioClips.station1CorrectCombined("ג") ?: "",
            AudioClips.station1CorrectCombined("ד") ?: "",
            AudioClips.station1CorrectCombined("ה") ?: "",
            AudioClips.station1CorrectCombined("ל") ?: "",
            AudioClips.station1CorrectCombined("מ") ?: "",
        )
    }

    // Episode 1 station 2: preload instruction + balloon feedback clips for low latency.
    LaunchedEffect(stationId, chapterId) {
        if (!(audioEnabled && chapterId == 1 && stationId == 2)) return@LaunchedEffect
        val letters = listOf("א", "ב", "ג", "ד", "ה", "ל", "מ")
        val paths = ArrayList<String>()
        paths.add(AudioClips.PopBalloonsWithLetter)
        paths.add(AudioClips.VoTryAgain1)
        paths.add(AudioClips.VoGoodJob2)
        paths.add(AudioClips.SfxBalloonPopSoft)
        paths.add(AudioClips.SfxBalloonPopWrongFunny)
        paths.add(AudioClips.SfxBalloonPop)
        // Preferred natural Station 2 pops (may be missing until provided).
        paths.add(AudioClips.SfxStation2PopSoft1)
        paths.add(AudioClips.SfxStation2PopSoft2)
        paths.add(AudioClips.SfxStation2PopPlop)
        paths.add(AudioClips.SfxStation2PopFinale)
        for (letter in letters) {
            AudioClips.letterNameClip(letter)?.let(paths::add)
            AudioClips.wrongSentenceClip(letter)?.let(paths::add)
            AudioClips.station1WrongCombined(letter)?.let(paths::add)
        }
        sfx.preload(*paths.distinct().toTypedArray())
    }

    // Episode 1 station 3: preload instruction + letter names + short feedback for SoundPool voice.
    LaunchedEffect(stationId, chapterId) {
        if (!(audioEnabled && chapterId == 1 && stationId == 3)) return@LaunchedEffect
        val letters = Chapter1Config.letters
        val paths = ArrayList<String>()
        for (l in letters) {
            AudioClips.chooseLetterClip(l)?.let(paths::add)
            AudioClips.letterNameClip(l)?.let(paths::add)
        }
        paths.add(AudioClips.VoTryAgain1)
        paths.add(AudioClips.VoNice1)
        paths.add(AudioClips.SfxWrong)
        paths.add(AudioClips.SfxCorrect)
        sfx.preload(*paths.distinct().toTypedArray())
    }

    LaunchedEffect(stationId) {
        snapshotFlow { session.currentIndex >= session.totalQuestions }.collect { exhausted ->
            if (exhausted && session.totalQuestions > 0) {
                if (!completionCallbackFired) {
                    completionCallbackFired = true
                    onComplete(stationId, session.correctCount, session.mistakeCount)
                }
            }
        }
    }

    val current = session.currentQuestion
    if (current == null) {
        // Safety: if we reached the end and for some reason the snapshotFlow completion path didn't navigate yet,
        // fire completion once so we never get "blank screen stuck".
        LaunchedEffect(stationId, session.currentIndex) {
            if (!completionCallbackFired) {
                completionCallbackFired = true
                onComplete(stationId, session.correctCount, session.mistakeCount)
            }
        }
        Box(modifier = modifier.fillMaxSize())
        return
    }

    LaunchedEffect(stationId, session.currentIndex) {
        phase = GamePhase.Intro
        inputLocked = true
        wrongTapsThisQuestion = 0
        correctTapPulseLetter = null
        station1PinnedCorrectLetter = null
        station2PinnedBalloonLetter = null
        station2PinnedBalloonColor = null
        // Cancel any in-flight feedback/instructions from the previous question.
        cancelFeedbackVoice()
        val q = session.currentQuestion ?: return@LaunchedEffect

        // CRITICAL: start instruction voice as early as possible (especially Station 1).
        promptVoiceJob =
            launch {
            if (audioEnabled) {
                // Episode 1 station 6: do not introduce a target letter (this station is a picture↔word match).
                val skipLetterPrompt = (stationId == 6 && q is Question.ImageMatchQuestion)
                if (!skipLetterPrompt) {
                    dinoTalking = true
                    try {
                        // No artificial delay before instruction voice.
                        // Station 1: use SoundPool for ultra-low-latency voice.
                        if (chapterId == 1 && stationId == 1) {
                            val target =
                                when (q) {
                                    is Question.PopBalloonsQuestion -> q.correctAnswer
                                    is Question.FindLetterGridQuestion -> q.targetLetter
                                    is Question.PictureStartsWithQuestion -> q.correctLetter
                                    is Question.ImageMatchQuestion -> q.targetLetter
                                    is Question.FinaleSlotQuestion -> null
                                }
                            if (target != null) {
                                val clip = AudioClips.chooseLetterClip(target)
                                if (clip != null) {
                                    sfx.stopStream(station1VoiceStreamId)
                                    station1VoiceStreamId = sfx.playReturningStreamId(clip, volume = 1f) ?: 0
                                }
                            }
                        } else if (chapterId == 1 && stationId == 3 && q is Question.FindLetterGridQuestion) {
                            // Station 3: SoundPool instruction to eliminate MediaPlayer latency.
                            val target = q.targetLetter
                            val clip = AudioClips.chooseLetterClip(target) ?: AudioClips.letterNameClip(target)
                            if (clip != null) {
                                sfx.stopStream(station3VoiceStreamId)
                                station3VoiceStreamId = sfx.playReturningStreamId(clip, volume = 1f) ?: 0
                            }
                        } else if (chapterId == 1 && stationId == 2 && q is Question.PopBalloonsQuestion) {
                            // SoundPool: start target letter while intro is still ending (minimal gap).
                            sfx.stopStream(station2VoiceStreamId)
                            station2VoiceStreamId = 0
                            val intro = AudioClips.PopBalloonsWithLetter
                            station2VoiceStreamId = sfx.playReturningStreamId(intro, volume = 1f) ?: 0
                            val introMs = sfx.durationMs(intro) ?: 0L
                            if (introMs > 0) {
                                val lead =
                                    (introMs * Station2IntroLetterLeadFraction)
                                        .toLong()
                                        .coerceIn(16L, introMs)
                                delay(lead)
                            }
                            val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                            if (letterClip != null) {
                                station2VoiceStreamId = sfx.playReturningStreamId(letterClip, volume = 1f) ?: 0
                            }
                        } else {
                            speakPromptForQuestion(voice, stationId = stationId, chapterId = chapterId, q = q)
                        }
                    } finally {
                        dinoTalking = false
                    }
                }
            }
        }

        // Preload SFX after prompt kickoff (never block instruction start).
        launch {
            if (audioEnabled) {
                sfx.preload(
                    AudioClips.SfxCorrect,
                    AudioClips.SfxWrong,
                    AudioClips.SfxBalloonPopSoft,
                    AudioClips.SfxBalloonPop,
                    AudioClips.SfxBalloonPopWrongFunny,
                )
            }
        }
        delay(IntroDurationMs)
        phase = GamePhase.Play
        inputLocked = false
        entryPulseEpoch += 1
        if (chapterId == 1 && stationId == 3) {
            // Entry guidance: a subtle pulse to draw attention to the target letter.
            hintPulseEpoch += 1
        }
    }

    LaunchedEffect(dinoVisual) {
        if (dinoVisual != DinoVisual.Jump) return@LaunchedEffect
        repeat(9) { i ->
            jumpFrameIndex = i % jumpFrames.size
            delay(85)
        }
        dinoVisual = DinoVisual.Idle
    }

    LaunchedEffect(hintPulseEpoch, stationId) {
        if (hintPulseEpoch <= 0) return@LaunchedEffect
        hintHeaderScale.snapTo(1f)
        hintHeaderScale.animateTo(1.10f, tween(120))
        hintHeaderScale.animateTo(1f, spring(dampingRatio = 0.56f, stiffness = 420f))
    }

    LaunchedEffect(entryPulseEpoch, stationId) {
        if (entryPulseEpoch <= 0) return@LaunchedEffect
        entryPulseScale.snapTo(1f)
        entryPulseScale.animateTo(1.04f, tween(130))
        entryPulseScale.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 420f))
    }

    suspend fun advanceAfterRound(isLast: Boolean) {
        inputLocked = true
        if (audioEnabled) ChildGameAudioHooks.onLevelComplete()
        // CRITICAL UX: do not block visuals/transitions on voice playback.
        // Episode 1 station 2: keep end-of-round clean (no extra "success big" stack; last balloon already feels special).
        if (audioEnabled) {
            when {
                chapterId == 1 && stationId == 1 && isLast -> gameFeedback.playCorrect()
                chapterId == 1 && stationId == 2 -> Unit
                // Station 3 grid already plays per-tap SFX; avoid a second “pip” at round transition.
                chapterId == 1 && stationId == 3 -> Unit
                isLast -> gameFeedback.playSuccessBig()
                else -> gameFeedback.playCorrect()
            }
        }
        if (!suppressInGameDinoProgress) {
            dinoVisual = DinoVisual.Jump
        }
        // Episode 1: small praise pool (sometimes silent to keep pace). Station 1 stays unchanged/hardened.
        // Station 6 (episode 1): "kol hakavod" is played per correct match; avoid double-speaking here.
        val episode1PraiseEligible =
            audioEnabled &&
                chapterId == 1 &&
                stationId in 2..5 &&
                Random.nextFloat() < Episode1PraiseChance
        val otherPraiseEligible =
            audioEnabled &&
                !(chapterId == 1 && stationId == 6) &&
                !(chapterId == 1 && stationId == 1) &&
                !episode1PraiseEligible

        if (episode1PraiseEligible) {
            cancelFeedbackVoice()
            val candidates =
                mutableListOf(
                    AudioClips.VoNice1,
                    AudioClips.VoGoodJob2,
                    AudioClips.VoGoodJob1,
                    AudioClips.VoPraiseMetzuyan,
                    AudioClips.VoPraiseYofi,
                    AudioClips.VoPraiseHitzlacht,
                )
            // Shuffle to randomize while still using "first available" semantics.
            candidates.shuffle()
            val arr = candidates.toTypedArray()
            feedbackVoiceJob =
                if (stationId == 3) {
                    scope.launch {
                        sfx.stopStream(station3VoiceStreamId)
                        station3VoiceStreamId = 0
                        // Try in randomized order until one loads.
                        for (p in arr) {
                            val id = sfx.playReturningStreamId(p, volume = 1f)
                            if (id != null && id != 0) {
                                station3VoiceStreamId = id
                                val d = sfx.durationMs(p) ?: 0L
                                if (d > 0) delay(d)
                                break
                            }
                        }
                    }
                } else {
                    scope.launch { voice.playFirstAvailableBlocking(*arr) }
                }
        } else if (otherPraiseEligible) {
            cancelFeedbackVoice()
            feedbackVoiceJob = scope.launch { voice.playBlocking(AudioClips.VoGoodJob1) }
        }
        if (!suppressInGameDinoProgress) {
            dinoForward.animateTo(dinoForward.value + forwardDir * 12f, spring(dampingRatio = 0.75f, stiffness = 520f))
        }
        playSuccessPulse(scope, dinoScale)
        // UX: short pause before transition.
        delay(if (chapterId == 1 && stationId == 3) 120 else 170)
        // Episode 1 station 2: "כל הכבוד" + main chip + pinned mini balloon stay visible until praise ends — fade only after.
        val station2WaitPraiseBeforeFade = chapterId == 1 && stationId == 2
        if (station2WaitPraiseBeforeFade) {
            withTimeoutOrNull(8000) { feedbackVoiceJob?.join() }
            station2PinnedBalloonLetter = null
            station2PinnedBalloonColor = null
        }
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        if (!station2WaitPraiseBeforeFade) {
            // Don't advance to next question until praise voice is finished (unless the user taps again on the next screen).
            // Safety: never get stuck on a blank screen if a voice job hangs.
            withTimeoutOrNull(2500) { feedbackVoiceJob?.join() }
        }
        delay(5)
        session.nextQuestion()
        contentAlpha.animateTo(1f, tween(BetweenQuestionFadeMs))
    }

    fun onWrongFeedback(
        wrongPickedLetter: String? = null,
        wrongWordCatalogId: String? = null,
        wrongPickedLetterAlreadySpoken: Boolean = false,
        wrongWordAlreadySpoken: Boolean = false,
    ) {
        scope.launch {
            inputLocked = true
            dinoVisual = DinoVisual.TryAgain
            if (chapterId == 2 || chapterId == 4) {
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
                // Station 1: no SFX; voice only.
                if (!(chapterId == 1 && stationId == 1)) {
                    gameFeedback.playWrong()
                    ChildGameAudioHooks.onWrong()
                }
                // Station 1: wrong tap should be just "LETTER NAME" + "try again", as fast as possible.
                if (chapterId == 1 && stationId == 1 && wrongPickedLetter != null) {
                    cancelFeedbackVoice()
                    val combined = AudioClips.station1WrongCombined(wrongPickedLetter)
                    val letterClip = AudioClips.letterNameClip(wrongPickedLetter)
                    feedbackVoiceJob =
                        scope.launch {
                            if (combined != null) {
                                station1VoiceStreamId = sfx.playReturningStreamId(combined, volume = 1f) ?: 0
                                return@launch
                            }
                            if (letterClip != null) {
                                station1VoiceStreamId = sfx.playReturningStreamId(letterClip, volume = 1f) ?: 0
                            }
                        }
                    dinoVisual = DinoVisual.Idle
                    inputLocked = false
                    return@launch
                }
                cancelFeedbackVoice()
                feedbackVoiceJob =
                    scope.launch {
                        delay(110)
                        if (wrongWordCatalogId != null && !wrongWordAlreadySpoken) {
                            voice.playSequenceBlocking(
                                AudioClips.ThisIsPrefix,
                                AudioClips.wordClipByCatalogId(wrongWordCatalogId),
                                AudioClips.VoTryAgain2,
                                AudioClips.VoTryAgain1,
                            )
                            return@launch
                        }

                        if (wrongPickedLetter != null) {
                            // Prefer a single combined clip: "זה <letter>, נסה שוב" (sounds most connected).
                            val combined = AudioClips.wrongSentenceClip(wrongPickedLetter)
                            if (combined != null && !wrongPickedLetterAlreadySpoken) {
                                voice.playSequenceBlocking(combined)
                                return@launch
                            }

                            val letterName =
                                if (!wrongPickedLetterAlreadySpoken) AudioClips.letterNameClip(wrongPickedLetter) else null
                            // Fallback: atomic sequence "זה" + (letter) + "נסה שוב"
                            voice.playSequenceBlocking(
                                AudioClips.ThisIsPrefix,
                                letterName ?: "",
                                AudioClips.VoTryAgain2,
                                AudioClips.VoTryAgain1,
                            )
                            return@launch
                        }

                        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                    }
            }
            dinoVisual = DinoVisual.Idle
            // Allow immediate retry; new taps cancel the previous feedback voice.
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
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                    contentAlignment = Alignment.Center,
                ) {
                    if (phase == GamePhase.Intro) {
                        // Station 6: don't show the mid-screen intro pulse between rounds.
                        if (!(stationId == 6 && current is Question.ImageMatchQuestion) &&
                            // Station 1: don't show blinking letter between rounds.
                            !(chapterId == 1 && stationId == 1)
                        ) {
                            IntroPulse(stationId = stationId, question = current, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        when (current) {
                            is Question.FindLetterGridQuestion ->
                                FindLetterGridGame(
                                    question = current,
                                    // Station 3 (episode 1): SoundPool voice per tap for low-latency feedback.
                                    onLetterTapped =
                                        if (chapterId == 1 && stationId == 3) {
                                            { tapped ->
                                                if (!audioEnabled) return@FindLetterGridGame
                                                // Do not block visuals; cancel current voice immediately and start new.
                                                sfx.stopStream(station3VoiceStreamId)
                                                station3VoiceStreamId = 0
                                                val isCorrect = tapped == current.targetLetter
                                                val letterClip = AudioClips.letterNameClip(tapped)
                                                if (letterClip != null) {
                                                    feedbackVoiceJob =
                                                        scope.launch {
                                                            station3VoiceStreamId =
                                                                sfx.playReturningStreamId(letterClip, volume = 1f) ?: 0
                                                            val d = sfx.durationMs(letterClip) ?: 0L
                                                            if (d > 0) delay(d)
                                                            if (!isCorrect) {
                                                                station3VoiceStreamId =
                                                                    sfx.playReturningStreamId(AudioClips.VoTryAgain1, volume = 1f) ?: 0
                                                            }
                                                        }
                                                }
                                            }
                                        } else {
                                            null
                                        },
                                    hintPulseEpoch = hintPulseEpoch,
                                    hintHeaderPeakScale = if (chapterId == 1 && stationId == 3) 1.16f else 1.12f,
                                    // Episode 1 station 3: bigger letters inside same boxes.
                                    gridLetterSizeMultiplier = if (stationId == 3) 1.5f else 1f,
                                    correctCellPeakScale = if (chapterId == 1 && stationId == 3) 1.16f else 1.12f,
                                    onCellTapped = { index ->
                                        if (!consumeTapCooldown()) return@FindLetterGridGame
                                        cancelFeedbackVoice()
                                        session.wrongTap()
                                        shakeEpoch += 1
                                        wrongTapsThisQuestion += 1
                                        if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                        val tappedLetter = current.cells.getOrNull(index)
                                        if (!(chapterId == 1 && stationId == 3)) {
                                            onWrongFeedback(
                                                wrongPickedLetter = tappedLetter,
                                                wrongPickedLetterAlreadySpoken = false,
                                            )
                                        }
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
                                            .scale(entryPulseScale.value)
                                            .offset { IntOffset(optionsShake.value.toInt(), 0) },
                                )
                            is Question.PopBalloonsQuestion ->
                                Column(
                                    modifier = Modifier.fillMaxSize().scale(entryPulseScale.value),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    if (plan.mode != com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter) {
                                        if (chapterId == 1 && stationId == 2 && station2PinnedBalloonLetter != null) {
                                            Row(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                TargetLetterHeaderChip(
                                                    letter = current.correctAnswer,
                                                    modifier = Modifier.scale(hintHeaderScale.value),
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Station2PinnedBalloonMini(
                                                    letter = station2PinnedBalloonLetter!!,
                                                    balloonColor = station2PinnedBalloonColor ?: Color(0xFF6BCB77),
                                                    modifier = Modifier.scale(hintHeaderScale.value),
                                                )
                                            }
                                        } else {
                                            TargetLetterHeaderChip(
                                                letter = current.correctAnswer,
                                                modifier =
                                                    Modifier
                                                        .padding(top = 4.dp)
                                                        .scale(hintHeaderScale.value),
                                            )
                                        }
                                    }
                                    if (plan.mode == com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f, fill = true),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            TargetLetterHeaderChip(
                                                letter = current.correctAnswer,
                                                modifier =
                                                    Modifier
                                                        .align(Alignment.TopCenter)
                                                        .padding(top = 4.dp),
                                            )
                                            LetterOptions(
                                                options =
                                                    if (chapterId == 1 && stationId == 1 && station1PinnedCorrectLetter != null) {
                                                        listOf(station1PinnedCorrectLetter!!)
                                                    } else {
                                                        current.options
                                                    },
                                                enabled = !inputLocked,
                                                shakePx = optionsShake.value,
                                                correctPulseLetter =
                                                    correctTapPulseLetter
                                                        ?: current.correctAnswer.takeIf { wrongTapsThisQuestion >= 2 },
                                                correctPulseEpoch = hintPulseEpoch + correctTapPulseEpoch,
                                                onPick = { picked ->
                                                    // UX: any tap should immediately cancel currently playing voice,
                                                    // even if we ignore the tap due to cooldown.
                                                    cancelFeedbackVoice()
                                                    if (!consumeTapCooldown()) return@LetterOptions
                                                    // CRITICAL UX: visuals/SFX are immediate; voice is scheduled later.
                                                    when (session.submitAnswer(picked)) {
                                                        AnswerResult.Correct -> {
                                                            // Station 1: no SFX before speaking the letter name.
                                                            if (audioEnabled && !(chapterId == 1 && stationId == 1)) {
                                                                ChildGameAudioHooks.onCorrect()
                                                            }
                                                            correctTapPulseLetter = picked
                                                            correctTapPulseEpoch += 1
                                                            // Station 1: play the combined positive clip and ONLY THEN advance.
                                                            if (audioEnabled && chapterId == 1 && stationId == 1) {
                                                                scope.launch {
                                                                    cancelFeedbackVoice()
                                                                    station1PinnedCorrectLetter = picked
                                                                    val combined = AudioClips.station1CorrectCombined(picked) ?: return@launch
                                                                    val ms = sfx.durationMs(combined) ?: 0L
                                                                    station1VoiceStreamId = sfx.playReturningStreamId(combined, volume = 1f) ?: 0
                                                                    if (ms > 0) delay(ms)
                                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                                    advanceAfterRound(isLast)
                                                                }
                                                            } else {
                                                                scope.launch {
                                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                                    advanceAfterRound(isLast)
                                                                }
                                                            }
                                                        }
                                                        AnswerResult.Wrong -> {
                                                            // Station 1: no SFX before speaking the letter name.
                                                            if (audioEnabled && !(chapterId == 1 && stationId == 1)) {
                                                                ChildGameAudioHooks.onWrong()
                                                            }
                                                            shakeEpoch += 1
                                                            wrongTapsThisQuestion += 1
                                                            if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                            onWrongFeedback(wrongPickedLetter = picked)
                                                        }
                                                        AnswerResult.Finished -> {}
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            PopBalloonsOptions(
                                                options = current.options,
                                                correctAnswer = current.correctAnswer,
                                                enabled = !inputLocked,
                                                shakePx = optionsShake.value,
                                                visualRoundSeed =
                                                    if (chapterId == 1 && stationId == 2) {
                                                        session.currentIndex
                                                    } else {
                                                        0
                                                    },
                                                onBalloonPressed = { _ ->
                                                    // Voice is triggered after pop SFX (see onPopSfx) so it feels connected.
                                                },
                                                onPopSfx = { letter, isCorrect, finalCorrectBalloon, balloonIndex ->
                                                    if (!audioEnabled) return@PopBalloonsOptions
                                                    cancelFeedbackVoice()
                                                    if (chapterId == 1 && stationId == 2) {
                                                        feedbackVoiceJob =
                                                            scope.launch {
                                                                sfx.stopStream(station2VoiceStreamId)
                                                                station2VoiceStreamId = 0
                                                                if (isCorrect) {
                                                                    val variant = station2CorrectPopCount++
                                                                    val rate =
                                                                        if (variant % 2 == 0) {
                                                                            0.98f
                                                                        } else {
                                                                            1.02f
                                                                        }
                                                                    val pops =
                                                                        AudioClips.station2CorrectPopPlaylist(
                                                                            variant,
                                                                            finalCorrectBalloon,
                                                                        )
                                                                    sfx.playFirstAvailable(
                                                                        *pops,
                                                                        volume = if (finalCorrectBalloon) 0.72f else 0.64f,
                                                                        rate = rate,
                                                                    )
                                                                    val letterClip = AudioClips.letterNameClip(letter)
                                                                    if (letterClip != null) {
                                                                        station2VoiceStreamId =
                                                                            sfx.playReturningStreamId(letterClip, volume = 1f) ?: 0
                                                                        val d = sfx.durationMs(letterClip) ?: 0L
                                                                        if (d > 0) delay(d)
                                                                    }
                                                                } else {
                                                                    val wrongPops =
                                                                        AudioClips.station2WrongPopPlaylist(balloonIndex)
                                                                    sfx.playFirstAvailable(
                                                                        *wrongPops,
                                                                        volume = 0.56f,
                                                                        rate = 1f,
                                                                    )
                                                                    val letterClip = AudioClips.letterNameClip(letter)
                                                                    if (letterClip != null) {
                                                                        station2VoiceStreamId =
                                                                            sfx.playReturningStreamId(letterClip, volume = 1f) ?: 0
                                                                        val d = sfx.durationMs(letterClip) ?: 0L
                                                                        if (d > 0) delay(d)
                                                                    }
                                                                    station2VoiceStreamId =
                                                                        sfx.playReturningStreamId(AudioClips.VoTryAgain1, volume = 1f) ?: 0
                                                                    val t = sfx.durationMs(AudioClips.VoTryAgain1) ?: 0L
                                                                    if (t > 0) delay(t)
                                                                }
                                                            }
                                                        return@PopBalloonsOptions
                                                    }
                                                    scope.launch {
                                                        sfx.playFirstAvailable(
                                                            if (isCorrect) AudioClips.SfxBalloonPopSoft else AudioClips.SfxBalloonPopWrongFunny,
                                                            AudioClips.SfxBalloonPopSoft,
                                                            AudioClips.SfxBalloonPop,
                                                            volume = if (isCorrect) 0.88f else 0.32f,
                                                        )
                                                    }
                                                    val clip = AudioClips.letterNameClip(letter)
                                                    if (clip != null) {
                                                        feedbackVoiceJob =
                                                            scope.launch {
                                                                delay(90)
                                                                voice.playSequenceBlocking(clip)
                                                            }
                                                    }
                                                },
                                                onWrongPick = {
                                                    if (!consumeTapCooldown()) return@PopBalloonsOptions
                                                    if (!(chapterId == 1 && stationId == 2)) {
                                                        cancelFeedbackVoice()
                                                    }
                                                    // Wrong balloon: feedback only, stay on same question.
                                                    session.wrongTap()
                                                    shakeEpoch += 1
                                                    wrongTapsThisQuestion += 1
                                                    if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                    if (chapterId == 1 && stationId == 2) {
                                                        scope.launch {
                                                            inputLocked = true
                                                            dinoVisual = DinoVisual.TryAgain
                                                            playShake(scope, optionsShake)
                                                            dinoVisual = DinoVisual.Idle
                                                            inputLocked = false
                                                        }
                                                    } else {
                                                        onWrongFeedback()
                                                    }
                                                },
                                                onAllCorrectPopped = { lastLetter, poppedBalloonColor ->
                                                    val ch1St2 = chapterId == 1 && stationId == 2
                                                    if (ch1St2) {
                                                        station2PinnedBalloonLetter = lastLetter
                                                        station2PinnedBalloonColor = poppedBalloonColor
                                                    } else {
                                                        cancelFeedbackVoice()
                                                    }
                                                    // Only advance when ALL correct-letter balloons are popped.
                                                    when (session.submitAnswer(lastLetter)) {
                                                        AnswerResult.Correct ->
                                                            scope.launch {
                                                                if (ch1St2) {
                                                                    withTimeoutOrNull(4000) { feedbackVoiceJob?.join() }
                                                                    cancelFeedbackVoice()
                                                                }
                                                                // Station 2: round-end uses playSuccessBig in advanceAfterRound; skip extra hook SFX.
                                                                if (audioEnabled && !ch1St2) {
                                                                    ChildGameAudioHooks.onCorrect()
                                                                }
                                                                val isLast = session.currentIndex >= session.totalQuestions - 1
                                                                advanceAfterRound(isLast)
                                                            }
                                                        else -> {}
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            is Question.PictureStartsWithQuestion ->
                                PictureStartsWithGame(
                                    question = current,
                                    enabled = !inputLocked,
                                    shakePx = optionsShake.value,
                                    pictureImageHeight =
                                        if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            // Station 4: keep the frame compact so the word + letters remain visible.
                                            160.dp
                                        } else {
                                            140.dp
                                        },
                                    // Station 4 request: word + image doubled, frame (box) slightly smaller.
                                    promptWordSizeMultiplier =
                                        if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            2f
                                        } else {
                                            1f
                                        },
                                    // Station 4 screenshots: the outer card should be wider (more rectangle).
                                    pictureFrameMaxWidthFraction =
                                        if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            // Station 4: frame (box) ~20% smaller.
                                            0.25f
                                        } else {
                                            null
                                        },
                                    pictureFrameMinWidth =
                                        if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            // Station 4: frame (box) ~20% smaller.
                                            112.dp
                                        } else {
                                            200.dp
                                        },
                                    // Normalize “perceived” picture size: some assets (medusa/house) read too large,
                                    // while emoji/placeholder art reads too small.
                                    pictureInnerScale = { word, tileDrawable ->
                                        if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            Chapter1Station4PictureInnerScale.likeStation5(word, tileDrawable)
                                        } else {
                                            1f
                                        }
                                    },
                                    hintCorrectLetter = current.correctLetter.takeIf { wrongTapsThisQuestion >= 2 },
                                    hintPulseEpoch = hintPulseEpoch,
                                    correctPulseLetter = correctTapPulseLetter,
                                    correctPulseEpoch = correctTapPulseEpoch,
                                    onPickLetter = { picked ->
                                        if (!consumeTapCooldown()) return@PictureStartsWithGame
                                            cancelFeedbackVoice()
                                        when (session.submitPictureStartsWith(picked)) {
                                            AnswerResult.Correct -> {
                                                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                scope.launch {
                                                    correctTapPulseLetter = picked
                                                    correctTapPulseEpoch += 1
                                                    if (chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                                        val letterName = AudioClips.letterNameClip(picked)
                                                        if (letterName != null) voice.playBlocking(letterName)
                                                    }
                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                    advanceAfterRound(isLast)
                                                }
                                            }
                                            AnswerResult.Wrong -> {
                                                if (audioEnabled) ChildGameAudioHooks.onWrong()
                                                wrongTapsThisQuestion += 1
                                                if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                if (chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                                    onWrongFeedback(wrongPickedLetter = picked)
                                                } else {
                                                    onWrongFeedback()
                                                }
                                            }
                                            AnswerResult.Finished -> {}
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize().scale(entryPulseScale.value),
                                )
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
                                        contentKey = session.currentIndex,
                                        enabled = !inputLocked,
                                        compactWideSpread =
                                            chapterId in 1..4 && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH,
                                        onWordPressed = { choiceId ->
                                            if (!audioEnabled) return@MatchLetterToWordGame
                                            scope.launch {
                                                voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId))
                                            }
                                        },
                                        onLetterPressed = { letter ->
                                            if (!audioEnabled) return@MatchLetterToWordGame
                                            val clip = AudioClips.letterNameClip(letter) ?: return@MatchLetterToWordGame
                                            scope.launch { voice.playBlocking(clip) }
                                        },
                                        onMatchAttempt = { correct ->
                                            if (!audioEnabled) return@MatchLetterToWordGame
                                            scope.launch {
                                                if (correct) {
                                                    voice.playFirstAvailableBlocking(
                                                        AudioClips.VoNice1,
                                                        AudioClips.VoGoodJob1,
                                                        AudioClips.VoGoodJob2,
                                                    )
                                                } else {
                                                    voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                                }
                                            }
                                        },
                                        innerPictureScaleForChoice = { choice ->
                                            if (chapterId in 1..4 && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                                Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                            } else {
                                                when {
                                                    choice.word == "מדוזה" || choice.id == "w_מ_3" || choice.tileDrawable == R.drawable.lesson_pic_medusa -> 0.5f
                                                    else -> 1f
                                                }
                                            }
                                        },
                                        instructions = "חברו בין  אות למילה המתאימה",
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
                                        modifier = Modifier.fillMaxSize().scale(entryPulseScale.value),
                                    )
                                } else {
                                    ImageMatchGame(
                                        question = current,
                                        contentKey = session.currentIndex,
                                        enabled = !inputLocked,
                                        shakePx = optionsShake.value,
                                        hintCorrectChoiceId = current.correctChoiceId.takeIf { wrongTapsThisQuestion >= 2 },
                                        hintPulseEpoch = hintPulseEpoch,
                                        showWordCaptions = true,
                                        // Episode 1 station 5: caption text +20%.
                                        captionSizeMultiplier =
                                            plan.imageMatchCaptionSizeMultiplier *
                                                if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                    1.2f
                                                } else {
                                                    1f
                                                },
                                        pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                        innerPictureScaleForChoice = { choice ->
                                            // Station 5 request: all pictures should look same-size as the heart.
                                            // Use Crop in the card and keep per-choice scaling at 1x.
                                            if (chapterId in 1..4 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                            } else {
                                                1f
                                            }
                                        },
                                        onAttempt = { choiceId ->
                                            if (!consumeTapCooldown()) return@ImageMatchGame false
                                            cancelFeedbackVoice()
                                            when (session.submitImageMatch(choiceId)) {
                                                AnswerResult.Correct -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                    scope.launch {
                                                        if (chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                            // Station 5 request: say the tapped WORD, then the existing "good job" flow will run.
                                                            voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId))
                                                        }
                                                        val isLast = session.currentIndex >= session.totalQuestions - 1
                                                        advanceAfterRound(isLast)
                                                    }
                                                    true
                                                }
                                                AnswerResult.Wrong -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onWrong()
                                                    wrongTapsThisQuestion += 1
                                                    if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                    if (chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                        onWrongFeedback(wrongWordCatalogId = choiceId)
                                                    } else {
                                                        onWrongFeedback()
                                                    }
                                                    false
                                                }
                                                AnswerResult.Finished -> false
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize().scale(entryPulseScale.value),
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
                if (chapterId in 2..4) {
                    // “Walk” feel on chapter roads between rounds.
                    listOf(R.drawable.dino_walk_0, R.drawable.dino_walk_1, R.drawable.dino_walk_2, R.drawable.dino_walk_3)
                } else {
                    listOf(R.drawable.dino_talk_0, R.drawable.dino_talk_1, R.drawable.dino_talk_2, R.drawable.dino_talk_3)
                }
            AnimatedTalkingCharacter(
                idleRes = dinoDrawable,
                talkFrameResIds = talkFrames,
                isTalking = dinoTalking || (chapterId in 2..4 && inputLocked && dinoVisual == DinoVisual.Jump),
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
            is Question.PictureStartsWithQuestion -> question.word
            is Question.ImageMatchQuestion ->
                when {
                    stationId == 6 -> "חברו מילה לתמונה"
                    stationId in 4..6 -> question.targetLetter
                    else -> question.targetWord
                }
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
                        is Question.PictureStartsWithQuestion ->
                            if (narrow) 40.sp else 48.sp
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
    // Prefer per-letter recorded prompt (e.g. "בחר את האות אלף") when available.
    val chooseSpecific = AudioClips.chooseLetterClip(letter)
    if (chooseSpecific != null) {
        voice.playBlocking(chooseSpecific)
        return
    }

    // Otherwise use the generic "בחר את האות" + letter name (requires both clips recorded).
    voice.playBlocking(AudioClips.VoChooseLetter)
    val letterName = AudioClips.letterNameClip(letter) ?: return
    voice.playSequenceBlocking(letterName)
}

    private suspend fun speakPromptForQuestion(
        voice: VoicePlayer,
        stationId: Int,
        chapterId: Int,
        q: Question,
    ) {
    when (q) {
        is Question.PopBalloonsQuestion -> {
            // Episode 1 station 2: instruction is started from GameScreen LaunchedEffect (SoundPool).
            speakLetterPrompt(voice, q.correctAnswer)
        }
        is Question.FindLetterGridQuestion -> speakLetterPrompt(voice, q.targetLetter)
            is Question.PictureStartsWithQuestion -> {
                // Episode 1 station 4: say the WORD shown first (if recorded), then fall back to the usual letter prompt.
                if (chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                    voice.playBlocking(AudioClips.wordClipByCatalogId(q.catalogEntryId))
                } else {
                    speakLetterPrompt(voice, q.correctLetter)
                }
            }
            is Question.ImageMatchQuestion -> {
                // Episode 1 station 5: "איזו מילה מתחילה באות" + letter name.
                if (chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                    voice.playBlocking(AudioClips.WhichWordStartsWithLetter)
                    val letterName = AudioClips.letterNameClip(q.targetLetter)
                    if (letterName != null) voice.playBlocking(letterName)
                } else if (chapterId == 1 && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                    // Episode 1 station 6: instructions.
                    voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
                } else {
                    speakLetterPrompt(voice, q.targetLetter)
                }
            }
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
