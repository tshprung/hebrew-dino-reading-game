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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
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
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Chapter3EpisodeContent
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.components.learning.StoryEggStrip
import com.tal.hebrewdino.ui.components.learning.storyEggStripVerticalHeight
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.game.FindLetterGridGame
import com.tal.hebrewdino.ui.game.FinaleGame
import com.tal.hebrewdino.ui.game.ImageToWordGame
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

/** Chapter 1 station 3 / Episode 3 station 1: find-grid intro (letter name on SoundPool overlap). */
private suspend fun playSagaFindGridIntroSoundPool(
    sfx: SoundPoolPlayer,
    voice: VoicePlayer,
    q: Question.FindLetterGridQuestion,
) {
    sfx.stopAllStreams()
    val combined = AudioClips.chooseLetterClip(q.targetLetter)
    val letter = AudioClips.letterNameClip(q.targetLetter)
    val findIntro = AudioClips.VoFindLetter
    val chooseIntro = AudioClips.VoChooseLetter
    val findMs = sfx.durationMs(findIntro) ?: 0L
    val chooseMs = sfx.durationMs(chooseIntro) ?: 0L
    val introPair: Pair<String, Long>? =
        when {
            findMs > 0L -> findIntro to findMs
            chooseMs > 0L -> chooseIntro to chooseMs
            else -> null
        }
    if (letter != null && introPair != null) {
        val (intro, introMs) = introPair
        sfx.playReturningStreamId(intro, volume = 1f)
        val lead =
            (introMs * StationIntroLetterLeadFraction * Station3IntroToLetterLeadStretch)
                .toLong()
                .coerceIn(16L, introMs)
        delay(lead)
        sfx.playReturningStreamId(letter, volume = 1f)
    } else {
        val bundledPath =
            sfx.playFirstAvailableReturningPath(
                *(listOfNotNull(combined, letter).toTypedArray()),
                volume = 1f,
            )
        if (bundledPath != null) {
            val parsed = sfx.durationMs(bundledPath) ?: 0L
            val waitMs =
                if (parsed > 0L) {
                    parsed.coerceAtLeast(80L)
                } else {
                    Station3InstructionFallbackDurationMs
                }
            delay(waitMs.coerceAtMost(6000L))
        } else {
            when {
                voice.hasAsset(findIntro) ->
                    voice.playSequenceBlocking(findIntro, letter ?: "")
                voice.hasAsset(chooseIntro) ->
                    voice.playSequenceBlocking(chooseIntro, letter ?: "")
                else -> speakLetterPrompt(voice, q.targetLetter)
            }
        }
    }
}

/** Episode 3 station 2: sentence prompt + the current word (no letter-name voice). */
// Kept for backwards compatibility; Episode 3 station 2 prompt is now handled inline.
private suspend fun playChapter3FindAllLettersInWordPrompt(
    voice: VoicePlayer,
    round: Chapter3EpisodeContent.SpellRound,
) {
    // Legacy helper kept; Episode 3 flow now uses dedicated station clips.
    val wordPath = AudioClips.wordClipByCatalogId(round.catalogId)
    if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
}

private const val BetweenQuestionFadeMs = 80

/** ~½ cm: shared layout nudges for the six-station arc (chapters 1–4); letters/art/intros differ per chapter. */
private val SixStationArcHalfCmNudge = 19.dp

/** ~1 cm vertical gap between the back button and the collected-egg strip. */
private val SpaceBelowBackBeforeEggs = 38.dp

/** Chapters that use the shared six-station journey ([Chapter1StationOrder]); intros, art, and letter pools differ. */
private val SixStationArcChapterRange = 1..4

private fun isSagaEpisode(chapterId: Int): Boolean = chapterId in SixStationArcChapterRange

/**
 * Episode 1 stations 2–3: start the letter name this far into the intro clip on SoundPool (overlap).
 * 0.94 ≈ halving the remaining pause vs 0.88 (i.e. moving halfway from 0.88 toward 1.0).
 */
private const val StationIntroLetterLeadFraction = 0.94f
/** Station 3 find-letter intro: stretch the intro→letter delay by this factor (e.g. 1.10 = 10% more space before the letter). */
private const val Station3IntroToLetterLeadStretch = 1.10f
/**
 * Station 2 balloon intro ("פוצץ את הבלונים…"): **25% shorter** remaining gap vs [StationIntroLetterLeadFraction]
 * before overlapping the target letter name.
 */
private const val Station2BalloonIntroLetterLeadFraction =
    1f - (1f - StationIntroLetterLeadFraction) * 0.75f
/**
 * Scales the overlap lead before the letter after [PopBalloonsWithLetter] (smaller = letter starts earlier).
 */
private const val Station2IntroToLetterLeadScale = 0.5f
/** Adds this fraction of the remaining tail `(1 - baseLead)` before the letter, where `baseLead` = [Station2BalloonIntroLetterLeadFraction] × [Station2IntroToLetterLeadScale]. */
private const val Station2BalloonIntroToLetterGapBoost = 0.2f
/** Fixed extra pause after that lead so the letter name does not ride on `pop_balloons_with_letter` (ms). */
private const val Station2BalloonIntroToLetterExtraPauseMs = 1000L
/**
 * Station 1 only: same overlap model, but the perceived gap before the letter is **20% shorter** than
 * [StationIntroLetterLeadFraction] would leave (i.e. start the letter earlier into `vo_choose_letter`).
 */
private const val Station1IntroLetterLeadFraction =
    1f - (1f - StationIntroLetterLeadFraction) * 0.8f
/**
 * Multiplier on [Station1IntroLetterLeadFraction] before starting the letter on SoundPool.
 * Higher = more space between "בחר את האות" and the letter.
 */
private const val Station1IntroToLetterLeadScale = 1.1f
/**
 * Station 1 wrong pick: start the follow-up line (e.g. try-again / "כמעט") this far into the letter-name clip.
 * 0.7 ⇒ ~30% less wait than playing the full letter clip before the next line (overlap into the tail).
 */
private const val Station1WrongLetterToFollowLeadFraction = 0.7f
/** Adds this fraction of the remaining tail `(1 - baseWrongLead)` to the lead before try-again (station 1 wrong; also station 4 wrong SoundPool path). */
private const val Station1WrongLetterToTryAgainGapBoost = 0.1f
/** Station 4 wrong: add extra space before try-again (fraction of remaining tail). */
private const val Station4WrongLetterToTryAgainGapBoost = 0.25f
/**
 * Station 4 wrong: multiplier on the delay before try-again after the wrong letter name (SoundPool overlap).
 * Halves the wait vs [Station1WrongLetterToFollowLeadFraction] alone — tighter gap before "נסה שוב" / similar.
 */
private const val Station4WrongLetterToFollowLeadScale = 0.5f
/** If pop WAV duration can't be parsed, wait this long so [stopAllStreams] doesn't cut the pop instantly. */
private const val Station2PopFallbackDurationMs = 160L
/** Extra wait after pop SFX before [stopAllStreams] so the tail is not clipped slightly short. */
private const val Station2PopTailPaddingMs = 52L
/** Station 3 intro on SoundPool: if [SoundPoolPlayer.durationMs] is 0, wait this long so the line is audible. */
private const val Station3InstructionFallbackDurationMs = 1300L
/**
 * Episode 1 station 4: start the spoken word this far into `which_letter_does_word_start` (overlap).
 * 0.775 = **25% shorter** remaining gap vs 0.70 (i.e. `1 - (1 - 0.70) * 0.75`).
 */
private const val Station4IntroWordLeadFraction = 0.775f
/**
 * Halves the SoundPool wait before the spoken word after [WhichLetterDoesWordStart] (shorter gap before the word).
 */
private const val Station4IntroToWordLeadScale = 0.5f
/** Adds this fraction of `(1 - baseLead)` before the word, where `baseLead` = [Station4IntroWordLeadFraction] × [Station4IntroToWordLeadScale] (reduces overlap with `which_letter_does_word_start`). */
private const val Station4IntroToWordGapBoost = 0.2f
/** Fixed extra pause after that lead before the spoken word clip (ms). */
private const val Station4IntroToWordExtraPauseMs = 950L
/**
 * Episode 1 station 5: start the letter name this far into `find_word_starts_with_letter` (overlap).
 * Same **25% shorter tail** target as [Station4IntroWordLeadFraction] (was sequential full intro before letter).
 */
private const val Station5WhichWordIntroLetterLeadFraction = 0.775f
/**
 * Halves the SoundPool wait before the letter after [WhichWordStartsWithLetter] (shorter gap before letter name).
 */
private const val Station5WhichWordIntroToLetterLeadScale = 0.5f
/** Adds this fraction of `(1 - baseLead)` before the letter, where `baseLead` = [Station5WhichWordIntroLetterLeadFraction] × [Station5WhichWordIntroToLetterLeadScale]. */
private const val Station5WhichWordIntroToLetterGapBoost = 0.50f
/** Fixed extra pause after that lead before the letter name clip (ms). */
private const val Station5WhichWordIntroToLetterExtraPauseMs = 500L

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
    /** Eggs already collected in prior chapter finales (shown upright under the status bar). */
    collectedEggStripCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    // UX: no audio for now (per request).
    val audioEnabled = true

    // Must include letterPoolSpec + chapterId: station 1 plan is identical across chapters 1–4 (same mode/count),
    // but the letter pool differs — reusing a cached LevelSession would throw in generators / desync UI.
    val session =
        remember(stationId, letterPoolSpec, chapterId, plan) {
            LevelSession(plan = plan, letterPoolSpec = letterPoolSpec)
        }
    val sagaUsesPickLetterAudioStaging = isSagaEpisode(chapterId) && plan.mode == StationQuizMode.PickLetter
    val sagaUsesPopBalloonsAudioStaging = isSagaEpisode(chapterId) && plan.mode == StationQuizMode.PopBalloons
    val sagaUsesFindGridAudioStaging = isSagaEpisode(chapterId) && plan.mode == StationQuizMode.FindLetterGrid
    val isChapter3HighlightedLetterInWordStation =
        chapterId == 3 &&
            stationId == 4 &&
            plan.mode == StationQuizMode.PickLetter &&
            plan.chapter3HighlightedLetterInWordPickLetter

    val isChapter3AudioLetterRecognitionStation =
        chapterId == 3 &&
            stationId == 5 &&
            plan.mode == StationQuizMode.PickLetter &&
            plan.chapter3AudioLetterRecognition

    val isChapter3PopAllLettersStation =
        chapterId == 3 &&
            stationId == 3 &&
            plan.mode == StationQuizMode.PopBalloons &&
            plan.chapter3PopAllLettersInWord
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
    var station4WrongFlashEpoch by remember(stationId, session.currentIndex) { mutableIntStateOf(0) }
    var station4WrongFlashLetter by remember(stationId, session.currentIndex) { mutableStateOf<String?>(null) }
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
        if (sagaUsesPickLetterAudioStaging) {
            // Station 1 uses SoundPool for letter + praise tail; stop all streams so no tail leaks into the next round.
            sfx.stopAllStreams()
            sfx.stopStream(station1VoiceStreamId)
            station1VoiceStreamId = 0
        }
        if (sagaUsesPopBalloonsAudioStaging) {
            sfx.stopAllStreams()
            sfx.stopStream(station2VoiceStreamId)
            station2VoiceStreamId = 0
        }
        if (sagaUsesFindGridAudioStaging) {
            sfx.stopAllStreams()
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
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        if (!(audioEnabled && sagaUsesPickLetterAudioStaging)) return@LaunchedEffect
        val poolLetters = letterPoolSpec.groups.flatten().distinct()
        voice.warmUp(
            AudioClips.VoChooseLetter,
            AudioClips.VoFindLetter,
            AudioClips.VoKolHakavod,
        )
        val perLetterPaths =
            poolLetters.flatMap { letter ->
                listOfNotNull(
                    AudioClips.chooseLetterClip(letter),
                    AudioClips.station1WrongCombined(letter),
                    AudioClips.letterNameClip(letter),
                )
            }
        sfx.preload(
            AudioClips.VoChooseLetter,
            *perLetterPaths.toTypedArray(),
            AudioClips.VoTryAgain1,
            AudioClips.VoTryAgain2,
            *AudioClips.station1CorrectPraiseTailCandidates(),
        )
    }

    // Episode 1 station 2: preload instruction + balloon feedback clips for low latency.
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        if (!(audioEnabled && sagaUsesPopBalloonsAudioStaging)) return@LaunchedEffect
        val letters = letterPoolSpec.groups.flatten().distinct()
        val paths = ArrayList<String>()
        paths.add(AudioClips.PopBalloonsWithLetter)
        paths.add(AudioClips.VoTryAgain1)
        paths.add(AudioClips.VoKolHakavod)
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

    // Episode 1–4 station 3: warm instruction (MediaPlayer); preload tap SFX + letter clips for SoundPool.
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        if (!(audioEnabled && sagaUsesFindGridAudioStaging)) return@LaunchedEffect
        val letters = letterPoolSpec.groups.flatten().distinct()
        voice.warmUp(AudioClips.VoFindLetter, AudioClips.VoChooseLetter, AudioClips.VoKolHakavod)
        for (l in letters) {
            AudioClips.letterNameClip(l)?.let { voice.warmUp(it) }
        }
        val paths = ArrayList<String>()
        paths.add(AudioClips.VoFindLetter)
        paths.add(AudioClips.VoChooseLetter)
        for (l in letters) {
            AudioClips.chooseLetterClip(l)?.let(paths::add)
            AudioClips.letterNameClip(l)?.let(paths::add)
        }
        paths.add(AudioClips.VoTryAgain1)
        paths.add(AudioClips.VoNice1)
        paths.add(AudioClips.VoKolHakavod)
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
        station4WrongFlashLetter = null
        station1PinnedCorrectLetter = null
        station2PinnedBalloonLetter = null
        station2PinnedBalloonColor = null
        // Cancel any in-flight feedback/instructions from the previous question.
        cancelFeedbackVoice()
        val q = session.currentQuestion ?: return@LaunchedEffect

        // CRITICAL: start instruction voice as early as possible (especially Station 1).
        // Use application scope — NOT a child of this LaunchedEffect — or the job is cancelled when this effect
        // finishes after IntroDurationMs, cutting instructions mid-sentence (station 1 sounded "silent").
        promptVoiceJob =
            scope.launch {
            if (audioEnabled) {
                dinoTalking = true
                try {
                        // No artificial delay before instruction voice.
                        // Station 1: use SoundPool for ultra-low-latency voice.
                        if (isChapter3HighlightedLetterInWordStation && q is Question.PopBalloonsQuestion) {
                            // Per-round instruction (letter + word).
                            val round = Chapter3EpisodeContent.pickSpellRound(session.currentIndex)
                            sfx.stopAllStreams()
                            val intro = AudioClips.Ch3St4FindHighlightedLetterInWordInstruction
                            val parts =
                                buildList {
                                    if (voice.hasAsset(intro)) add(intro)
                                    AudioClips.letterNameClip(round.correctLetter)?.let { add(it) }
                                    add(AudioClips.wordClipByCatalogId(round.catalogId))
                                }.filter { voice.hasAsset(it) }
                            if (parts.isNotEmpty()) {
                                voice.playSequenceBlocking(*parts.toTypedArray())
                            }
                        } else if (isChapter3AudioLetterRecognitionStation && q is Question.PopBalloonsQuestion) {
                            sfx.stopAllStreams()
                            val instruction = AudioClips.Ch3St5AudioLetterRecognitionInstruction
                            val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                            val parts =
                                buildList {
                                    if (voice.hasAsset(instruction)) add(instruction)
                                    if (letterClip != null) add(letterClip)
                                }.filter { voice.hasAsset(it) }
                            if (parts.isNotEmpty()) voice.playSequenceBlocking(*parts.toTypedArray())
                        } else if (sagaUsesPickLetterAudioStaging) {
                            val target =
                                when (q) {
                                    is Question.PopBalloonsQuestion -> q.correctAnswer
                                    is Question.FindLetterGridQuestion -> q.targetLetter
                                    is Question.PictureStartsWithQuestion -> q.correctLetter
                                    is Question.ImageMatchQuestion -> q.targetLetter
                                    is Question.FinaleSlotQuestion -> null
                                }
                            if (target != null) {
                                val letterClip = AudioClips.letterNameClip(target)
                                val intro = AudioClips.VoChooseLetter
                                val introMs = sfx.durationMs(intro) ?: 0L
                                if (introMs > 0L && letterClip != null) {
                                    sfx.stopAllStreams()
                                    sfx.playReturningStreamId(intro, volume = 1f)
                                    val lead =
                                        (introMs * Station1IntroLetterLeadFraction * Station1IntroToLetterLeadScale)
                                            .toLong()
                                            .coerceIn(16L, introMs)
                                    delay(lead)
                                    sfx.playReturningStreamId(letterClip, volume = 1f)
                                } else {
                                    voice.playSequenceBlocking(
                                        intro,
                                        letterClip ?: "",
                                    )
                                }
                            }
                        } else if (sagaUsesFindGridAudioStaging && q is Question.FindLetterGridQuestion) {
                            playSagaFindGridIntroSoundPool(sfx, voice, q)
                        } else if (chapterId == 3 && stationId == 1 && q is Question.PictureStartsWithQuestion) {
                            sfx.stopAllStreams()
                            // Episode 3 station 1: reuse Episode 1 station 4 instruction voice.
                            val clip = AudioClips.WhichLetterDoesWordStart
                            val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                            if (voice.hasAsset(clip)) voice.playBlocking(clip)
                            if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                        } else if (chapterId == 3 && stationId == 2 && q is Question.ImageMatchQuestion) {
                            // Episode 3 station 2: reuse Episode 1 station 6 instruction voice.
                            sfx.stopAllStreams()
                            voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
                        } else if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE && q is Question.PictureStartsWithQuestion) {
                            // Station 4: reduce gap between intro and word by skipping intro trailing silence.
                            val intro = AudioClips.WhichLetterDoesWordStart
                            val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                            val introMs = sfx.durationMs(intro) ?: 0L
                            if (introMs > 0 && voice.hasAsset(wordPath)) {
                                sfx.stopAllStreams()
                                sfx.playReturningStreamId(intro, volume = 1f)
                                val baseIntroWordLeadFrac =
                                    Station4IntroWordLeadFraction * Station4IntroToWordLeadScale
                                val introWordLeadFrac =
                                    baseIntroWordLeadFrac +
                                        Station4IntroToWordGapBoost * (1f - baseIntroWordLeadFrac)
                                val lead =
                                    (introMs * introWordLeadFrac)
                                        .toLong()
                                        .coerceIn(16L, introMs)
                                delay(lead + Station4IntroToWordExtraPauseMs)
                                sfx.stopAllStreams()
                                voice.playBlocking(wordPath)
                            } else {
                                voice.playSequenceBlocking(intro, wordPath)
                            }
                        } else if (sagaUsesPopBalloonsAudioStaging && q is Question.PopBalloonsQuestion) {
                            if (chapterId == 3) {
                                sfx.stopAllStreams()
                                val clip = AudioClips.Ch3St3PopAllLettersInWordInstruction
                                val r = Chapter3EpisodeContent.popAllLettersRound(session.currentIndex)
                                val wordPath = AudioClips.wordClipByCatalogId(r.catalogId)
                                if (voice.hasAsset(clip)) voice.playBlocking(clip)
                                if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                            } else {
                                // Station 2: we want minimal gap between intro and letter (skip intro trailing silence).
                                // Prefer SoundPool overlap timing when duration is available; otherwise fall back to strict sequence.
                                val intro = AudioClips.PopBalloonsWithLetter
                                val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                                val introMs = sfx.durationMs(intro) ?: 0L
                                if (introMs > 0 && letterClip != null) {
                                    // Let intro keep playing on one stream; start letter before the very end.
                                    sfx.stopAllStreams()
                                    sfx.playReturningStreamId(intro, volume = 1f)
                                    val baseIntroLeadFrac =
                                        Station2BalloonIntroLetterLeadFraction * Station2IntroToLetterLeadScale
                                    val introLeadFrac =
                                        baseIntroLeadFrac +
                                            Station2BalloonIntroToLetterGapBoost * (1f - baseIntroLeadFrac)
                                    val lead =
                                        (introMs * introLeadFrac)
                                            .toLong()
                                            .coerceIn(16L, introMs)
                                    delay(lead + Station2BalloonIntroToLetterExtraPauseMs)
                                    sfx.playReturningStreamId(letterClip, volume = 1f)
                                } else {
                                    // Fallback: strict sequential.
                                    voice.playSequenceBlocking(
                                        intro,
                                        letterClip ?: "",
                                    )
                                }
                            }
                        } else {
                            speakPromptForQuestion(voice, sfx, stationId = stationId, chapterId = chapterId, q = q)
                        }
                } finally {
                    dinoTalking = false
                }
            }
        }

        // Preload SFX after prompt kickoff (never block instruction start).
        scope.launch {
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
        val introPauseMs = IntroDurationMs
        delay(introPauseMs)
        phase = GamePhase.Play
        inputLocked = false
        entryPulseEpoch += 1
        if (sagaUsesFindGridAudioStaging && session.currentIndex == 0) {
            // Entry guidance: subtle pulse once at the station start (not every round).
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

    suspend fun advanceAfterRound(isLast: Boolean, ch3SpellMidWord: Boolean = false) {
        inputLocked = true
        if (audioEnabled && !ch3SpellMidWord) ChildGameAudioHooks.onLevelComplete()
        // CRITICAL UX: do not block visuals/transitions on voice playback.
        // Episode 1 station 2: keep end-of-round clean (no extra "success big" stack; last balloon already feels special).
        if (audioEnabled) {
            when {
                sagaUsesPickLetterAudioStaging && isLast && chapterId != 3 -> gameFeedback.playCorrect()
                sagaUsesPopBalloonsAudioStaging -> Unit
                // Station 3 grid already plays per-tap SFX; avoid a second “pip” at round transition.
                sagaUsesFindGridAudioStaging -> Unit
                // Station 6: per-match already plays success cues; avoid stacking another SFX at round transition.
                isSagaEpisode(chapterId) && stationId == 6 -> Unit
                isLast -> gameFeedback.playSuccessBig()
                else -> gameFeedback.playCorrect()
            }
        }
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)) {
            dinoVisual = DinoVisual.Jump
        }
        // Episode 1: small praise pool (sometimes silent to keep pace). Station 1 stays unchanged/hardened.
        // Station 6 (episode 1): "kol hakavod" is played per correct match; avoid double-speaking here.
        val episode1PraiseEligible =
            audioEnabled &&
                isSagaEpisode(chapterId) &&
                stationId in 2..5 &&
                // Station 4: letter + praise is played on correct tap before advancing.
                stationId != Chapter1StationOrder.PICTURE_PICK_ONE &&
                Random.nextFloat() < Episode1PraiseChance
        val otherPraiseEligible =
            audioEnabled &&
                !(isSagaEpisode(chapterId) && stationId == 6) &&
                !(sagaUsesPickLetterAudioStaging) &&
                !(isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) &&
                !episode1PraiseEligible

        if (episode1PraiseEligible) {
            // Station 3: last correct tap starts target letter on SoundPool; don't cancel until it finishes
            // or "כל הכבוד" overwrites the stream.
            if (sagaUsesFindGridAudioStaging) {
                withTimeoutOrNull(5000L) { feedbackVoiceJob?.join() }
            }
            cancelFeedbackVoice()
            val candidates =
                mutableListOf(
                    AudioClips.VoKolHakavod,
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
                if (sagaUsesFindGridAudioStaging) {
                    // Use MediaPlayer so we don't rely on durationMs() (WAV header parsing can be off after editing).
                    scope.launch { voice.playFirstAvailableBlocking(*arr) }
                } else {
                    scope.launch { voice.playFirstAvailableBlocking(*arr) }
                }
        } else if (otherPraiseEligible) {
            if (sagaUsesFindGridAudioStaging) {
                withTimeoutOrNull(5000L) { feedbackVoiceJob?.join() }
            }
            cancelFeedbackVoice()
            feedbackVoiceJob =
                scope.launch {
                    val pool = mutableListOf(AudioClips.VoKolHakavod, AudioClips.VoGoodJob1)
                    pool.shuffle()
                    voice.playFirstAvailableBlocking(*pool.toTypedArray())
                }
        }
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)) {
            dinoForward.animateTo(dinoForward.value + forwardDir * 12f, spring(dampingRatio = 0.75f, stiffness = 520f))
        }
        val strongerSuccessPulse =
            (sagaUsesPickLetterAudioStaging || sagaUsesFindGridAudioStaging) &&
                !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)
        val station456SuccessPulse =
            (isSagaEpisode(chapterId) &&
                (stationId == Chapter1StationOrder.PICTURE_PICK_ONE ||
                    stationId == Chapter1StationOrder.PICTURE_PICK_ALL ||
                    stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH))
        if (!(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)) {
            playSuccessPulse(
                scope,
                dinoScale,
                peakScale =
                    when {
                        strongerSuccessPulse -> 1.28f
                        station456SuccessPulse -> 1.24f
                        else -> 1.14f
                    },
            )
        }
        // UX: short pause before transition.
        delay(
            when {
                ch3SpellMidWord -> 38
                sagaUsesFindGridAudioStaging -> 120
                else -> 170
            },
        )
        // Episode 1 station 2: praise + pinned balloon stay until voice ends.
        // Episode 1 station 3: praise (SoundPool) must finish before fade — otherwise the screen goes blank mid-sentence.
        val waitPraiseBeforeFade =
            isSagaEpisode(chapterId) &&
                (sagaUsesPopBalloonsAudioStaging ||
                    sagaUsesFindGridAudioStaging ||
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE)
        if (sagaUsesPopBalloonsAudioStaging) {
            withTimeoutOrNull(8000) { feedbackVoiceJob?.join() }
            station2PinnedBalloonLetter = null
            station2PinnedBalloonColor = null
        } else if (isSagaEpisode(chapterId) && (sagaUsesFindGridAudioStaging || stationId == Chapter1StationOrder.PICTURE_PICK_ONE)) {
            withTimeoutOrNull(8000) { feedbackVoiceJob?.join() }
        }
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        if (!waitPraiseBeforeFade) {
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
            if (isSagaEpisode(chapterId)) {
                // Tiny playful stumble on wrong tap (same across the six-station arc chapters).
                dinoSlip.snapTo(0f)
                dinoTilt.snapTo(0f)
                dinoTilt.animateTo(-7f, tween(90))
                dinoSlip.animateTo(10f, tween(90))
                dinoTilt.animateTo(6f, tween(110))
                dinoSlip.animateTo(-6f, tween(110))
                dinoTilt.animateTo(0f, tween(140))
                dinoSlip.animateTo(0f, tween(140))
            }
            val strongerWrongShake =
                (isSagaEpisode(chapterId) && (stationId == Chapter1StationOrder.PICTURE_PICK_ONE || stationId == Chapter1StationOrder.PICTURE_PICK_ALL))
            playShake(scope, optionsShake, chapterId = chapterId, strength = if (strongerWrongShake) 1.25f else 1f)
            if (audioEnabled) {
                // Station 1: no SFX; voice only. Episode 3 station 3: light SFX on wrong (no letter voice loop).
                val allowWrongSfx =
                    (!(sagaUsesPickLetterAudioStaging) || isChapter3HighlightedLetterInWordStation || isChapter3AudioLetterRecognitionStation) &&
                        !(isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL)
                if (allowWrongSfx) {
                    gameFeedback.playWrong()
                    ChildGameAudioHooks.onWrong()
                }
                // Station 1: wrong tap — keep it varied to reduce fatigue.
                if (sagaUsesPickLetterAudioStaging && wrongPickedLetter != null && chapterId != 3) {
                    cancelFeedbackVoice()
                    feedbackVoiceJob =
                        scope.launch {
                            val letterClip = AudioClips.letterNameClip(wrongPickedLetter)
                            val letterMs = letterClip?.let { sfx.durationMs(it) } ?: 0L
                            // 0: try-again only, 1: letter only, 2+: letter + try-again (overlapped)
                            val variant = Random.nextInt(100)
                            if (variant < 20) {
                                sfx.playFirstAvailable(
                                    AudioClips.VoTryAgain2,
                                    AudioClips.VoTryAgain1,
                                    volume = 1f,
                                )
                                return@launch
                            }
                            if (variant < 55) {
                                if (letterClip != null && voice.hasAsset(letterClip)) voice.playBlocking(letterClip)
                                return@launch
                            }

                            if (letterClip != null && letterMs > 0L) {
                                sfx.stopAllStreams()
                                sfx.playReturningStreamId(letterClip, volume = 1f)
                                val followLeadFrac =
                                    Station1WrongLetterToFollowLeadFraction +
                                        Station1WrongLetterToTryAgainGapBoost *
                                        (1f - Station1WrongLetterToFollowLeadFraction)
                                val lead =
                                    (letterMs * followLeadFrac)
                                        .toLong()
                                        .coerceIn(16L, letterMs)
                                delay(lead)
                                sfx.playFirstAvailable(
                                    AudioClips.VoTryAgain2,
                                    AudioClips.VoTryAgain1,
                                    volume = 1f,
                                )
                            } else {
                                if (letterClip != null && voice.hasAsset(letterClip)) {
                                    voice.playBlocking(letterClip)
                                }
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                            }
                        }
                    dinoVisual = DinoVisual.Idle
                    inputLocked = false
                    return@launch
                }
                if (isSagaEpisode(chapterId) &&
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
                    wrongPickedLetter != null
                ) {
                    cancelFeedbackVoice()
                    feedbackVoiceJob =
                        scope.launch {
                            val lc = AudioClips.letterNameClip(wrongPickedLetter)
                            val letterMs = lc?.let { sfx.durationMs(it) } ?: 0L
                            if (lc != null && letterMs > 0L) {
                                sfx.stopAllStreams()
                                sfx.playReturningStreamId(lc, volume = 1f)
                                val baseWrongFrac =
                                    Station1WrongLetterToFollowLeadFraction *
                                        Station4WrongLetterToFollowLeadScale
                                val followLeadFrac =
                                    baseWrongFrac +
                                        Station4WrongLetterToTryAgainGapBoost * (1f - baseWrongFrac)
                                val lead =
                                    (letterMs * followLeadFrac)
                                        .toLong()
                                        .coerceIn(16L, letterMs)
                                delay(lead)
                                sfx.playFirstAvailable(
                                    AudioClips.VoTryAgain2,
                                    AudioClips.VoTryAgain1,
                                    volume = 1f,
                                )
                            } else {
                                if (lc != null && voice.hasAsset(lc)) {
                                    voice.playBlocking(lc)
                                }
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
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
                            // One try-again line only ([playSequenceBlocking] would play every clip, so both try WAVs).
                            voice.playSequenceBlocking(
                                AudioClips.ThisIsPrefix,
                                AudioClips.wordClipByCatalogId(wrongWordCatalogId),
                            )
                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
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
                            // Fallback: "זה" + letter, then one try-again clip (not both).
                            voice.playSequenceBlocking(
                                AudioClips.ThisIsPrefix,
                                letterName ?: "",
                            )
                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
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
        if (chapterId == 2) {
            // Chapter 2 PNG reads a bit flat on-device; a very light warm veil keeps the scene readable
            // while restoring some "sun" without fighting the authored art.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFF3E0).copy(alpha = 0.10f),
                                Color(0xFFFFFDE7).copy(alpha = 0.06f),
                                Color(0xFFE1F5FE).copy(alpha = 0.05f),
                            ),
                        ),
                    ),
            )
        }
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

        // Station screens: keep the top bar fixed (like Journey). Do not show collected eggs/letters/debug inside stations.
        val contentTopInset = 40.dp
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    // Give a consistent ~1cm breathing room below the status bar so the progress line
                    // and the "חזור" button are never clipped.
                    .padding(start = 8.dp, end = 8.dp, top = 38.dp, bottom = 0.dp)
                    .offset(y = if (isSagaEpisode(chapterId)) -SixStationArcHalfCmNudge else 0.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(4f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Keep back on the physical right; put the progress line immediately to its left.
                OutlinedButton(
                    onClick = onBack,
                    colors = ChapterNavChipStyles.outlinedButtonColors(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text("חזור", style = ChapterNavChipStyles.labelTextStyle())
                }
                LinearProgressIndicator(
                    progress = {
                        (session.questionNumber.toFloat() / session.totalQuestions.coerceAtLeast(1))
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.weight(1f).height(9.dp),
                    color = Color(0xFF2E7D32),
                    trackColor = Color(0xFF0B2B3D).copy(alpha = 0.12f),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = contentTopInset, bottom = 8.dp)
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
                            !(sagaUsesPickLetterAudioStaging) &&
                            // Episode 1/2 station 2 (balloons): no blinking letter intro.
                            !(isSagaEpisode(chapterId) && stationId == 2 && current is Question.PopBalloonsQuestion) &&
                            // Station 4: don't blink the word; keep it readable/stable.
                            !(isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) &&
                            // Station 5: no pulsing letter intro (go straight to choices).
                            !(isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) &&
                            // Station 3: no pulsing letter intro before each round.
                            !(sagaUsesFindGridAudioStaging) &&
                            // Episode 3 stations 1-3: no blinking intro (word/letter).
                            !(chapterId == 3 && (stationId == 1 || stationId == 2 || stationId == 3))
                        ) {
                            IntroPulse(stationId = stationId, question = current, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        when (current) {
                            is Question.FindLetterGridQuestion ->
                                FindLetterGridGame(
                                    question = current,
                                    contextWordHint =
                                        if (chapterId == 3 && sagaUsesFindGridAudioStaging) {
                                            Chapter3EpisodeContent.gridHintWord(session.currentIndex)
                                        } else {
                                            null
                                        },
                                    suppressHeaderTargetLetter = chapterId == 3 && sagaUsesFindGridAudioStaging,
                                    inlineInstructionText =
                                        if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE) {
                                            "מצא את האות:"
                                        } else {
                                            null
                                        },
                                    cellSideScale =
                                        if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE) {
                                            0.9f
                                        } else {
                                            1f
                                        },
                                    contentNudgeDownFraction =
                                        if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE) {
                                            0.05f
                                        } else {
                                            0f
                                        },
                                    // Station 3 (six-station arc): SoundPool voice per tap for low-latency feedback.
                                    onLetterTapped =
                                        if (sagaUsesFindGridAudioStaging) {
                                            { tapped ->
                                                if (!audioEnabled) return@FindLetterGridGame
                                                // Cut any in-flight round intro / previous letter stream (ids are not tracked for intro).
                                                sfx.stopAllStreams()
                                                station3VoiceStreamId = 0
                                                val isCorrect = tapped == current.targetLetter
                                                feedbackVoiceJob =
                                                    scope.launch {
                                                        if (isCorrect) {
                                                            // Correct: positive SFX only.
                                                            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.62f)
                                                        } else {
                                                            // Wrong: keep it lighter and varied (avoid stacking voice + SFX every time).
                                                            val tappedClip = AudioClips.letterNameClip(tapped)
                                                            when (Random.nextInt(100)) {
                                                                in 0..39 -> {
                                                                    // SFX only
                                                                    sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                                                                }
                                                                in 40..89 -> {
                                                                    // Letter only
                                                                    if (tappedClip != null) {
                                                                        station3VoiceStreamId =
                                                                            sfx.playReturningStreamId(tappedClip, volume = 1f) ?: 0
                                                                    } else {
                                                                        sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                                                                    }
                                                                }
                                                                else -> {
                                                                    // Rare: both
                                                                    sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                                                                    if (tappedClip != null) {
                                                                        station3VoiceStreamId =
                                                                            sfx.playReturningStreamId(tappedClip, volume = 1f) ?: 0
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                            }
                                        } else {
                                            null
                                        },
                                    hintPulseEpoch = hintPulseEpoch,
                                    hintHeaderPeakScale = if (sagaUsesFindGridAudioStaging) 1.30f else 1.12f,
                                    // Episode 1 station 3: bigger letters inside same boxes.
                                    gridLetterSizeMultiplier = if (sagaUsesFindGridAudioStaging) 1.5f else 1f,
                                    correctCellPeakScale = if (sagaUsesFindGridAudioStaging) 1.30f else 1.12f,
                                    onCellTapped = { index ->
                                        if (!consumeTapCooldown()) return@FindLetterGridGame
                                        // Station 3: wrong-tap voice is scheduled in onLetterTapped; cancel here would cut it off.
                                        if (!(sagaUsesFindGridAudioStaging)) {
                                            cancelFeedbackVoice()
                                        }
                                        session.wrongTap()
                                        shakeEpoch += 1
                                        wrongTapsThisQuestion += 1
                                        if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                        val tappedLetter = current.cells.getOrNull(index)
                                        if (!(sagaUsesFindGridAudioStaging)) {
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
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .scale(entryPulseScale.value)
                                            .then(
                                                if (sagaUsesPopBalloonsAudioStaging) {
                                                    Modifier.padding(top = SixStationArcHalfCmNudge)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    if (chapterId == 3 && sagaUsesPopBalloonsAudioStaging) {
                                        val bw =
                                            when (session.currentIndex.coerceIn(0, 2)) {
                                                0 -> "גמל"
                                                1 -> "דבש"
                                                else -> "שמש"
                                            }
                                        val emoji = com.tal.hebrewdino.ui.domain.LessonWordIllustrations.emojiForWord(bw)
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Text(
                                                text = "פוצץ את כל הבלונים עם אותיות המופיעות במילה:",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0B2B3D),
                                                textAlign = TextAlign.Center,
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .background(
                                                                color = Color(0xFFFFF59D).copy(alpha = 0.95f),
                                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                                            )
                                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = bw,
                                                        fontSize = 41.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF0B2B3D),
                                                        textAlign = TextAlign.Center,
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(text = emoji, fontSize = 36.sp)
                                            }
                                        }
                                    }
                                    if (plan.mode != com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter) {
                                        if (chapterId == 3 && sagaUsesPopBalloonsAudioStaging) {
                                            // Episode 3 station 3: no target letter chip.
                                        } else if (sagaUsesPopBalloonsAudioStaging && station2PinnedBalloonLetter != null) {
                                            Text(
                                                text = "פוצץ את הבלונים עם האות:",
                                                fontSize = 30.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0B2B3D),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                                            )
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
                                            if (isSagaEpisode(chapterId) && stationId == 2) {
                                                Text(
                                                    text = "פוצץ את הבלונים עם האות:",
                                                    fontSize = 30.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0B2B3D),
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                                                )
                                            }
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
                                                    .weight(1f, fill = true)
                                                    .then(
                                                        if (sagaUsesPickLetterAudioStaging) {
                                                            Modifier.padding(top = SixStationArcHalfCmNudge)
                                                        } else {
                                                            Modifier
                                                        },
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                if (isChapter3HighlightedLetterInWordStation) {
                                                    val spell = Chapter3EpisodeContent.pickSpellRound(session.currentIndex)
                                                    val emoji =
                                                        com.tal.hebrewdino.ui.domain.LessonWordIllustrations.emojiForWord(spell.word)
                                                    Text(
                                                        text = "מצא את האות המודגשת במילה:",
                                                        fontSize = 32.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0B2B3D),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 6.dp),
                                                    )
                                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.Center,
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Text(text = emoji, fontSize = 42.sp)
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .background(
                                                                            color = Color(0xFFFFF59D).copy(alpha = 0.95f),
                                                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                                                                        )
                                                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                                            ) {
                                                                Chapter3SpellWordRow(
                                                                    word = spell.word,
                                                                    highlightIndex = spell.slotIndex,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                if (isChapter3AudioLetterRecognitionStation) {
                                                    Text(
                                                        text = "מצא את האות שנאמרת",
                                                        fontSize = 26.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF0B2B3D),
                                                        textAlign = TextAlign.Center,
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (!audioEnabled) return@OutlinedButton
                                                            cancelFeedbackVoice()
                                                            val letter = current.correctAnswer
                                                            val clip = AudioClips.letterNameClip(letter) ?: return@OutlinedButton
                                                            feedbackVoiceJob =
                                                                scope.launch {
                                                                    voice.playBlocking(clip)
                                                                }
                                                        },
                                                        colors = ChapterNavChipStyles.outlinedButtonColors(),
                                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                                    ) {
                                                        Text("חזור על האות", style = ChapterNavChipStyles.labelTextStyle())
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                } else if (isSagaEpisode(chapterId) && stationId == 1) {
                                                    Text(
                                                        text = "בחר את האות:",
                                                        fontSize = 22.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF0B2B3D),
                                                        textAlign = TextAlign.Center,
                                                    )
                                                    TargetLetterHeaderChip(
                                                        letter = current.correctAnswer,
                                                        modifier = Modifier.padding(top = 10.dp),
                                                    )
                                                    Spacer(modifier = Modifier.height(18.dp))
                                                } else if (!isChapter3HighlightedLetterInWordStation) {
                                                    TargetLetterHeaderChip(
                                                        letter = current.correctAnswer,
                                                        modifier = Modifier.padding(top = 4.dp),
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                }
                                        LetterOptions(
                                        options =
                                                    if (sagaUsesPickLetterAudioStaging &&
                                                        station1PinnedCorrectLetter != null &&
                                                        !isChapter3HighlightedLetterInWordStation &&
                                                        !isChapter3AudioLetterRecognitionStation
                                                    ) {
                                                        listOf(station1PinnedCorrectLetter!!)
                                                    } else {
                                                        if (isChapter3HighlightedLetterInWordStation) {
                                                            current.options.sorted()
                                                        } else {
                                                            current.options
                                                        }
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
                                                    val result = session.submitAnswer(picked)
                                                    when (result) {
                                                        AnswerResult.Correct -> {
                                                            // Station 1: no SFX before speaking the letter name.
                                                            if (audioEnabled && !(sagaUsesPickLetterAudioStaging)) {
                                                                ChildGameAudioHooks.onCorrect()
                                                            }
                                                            correctTapPulseLetter = picked
                                                            correctTapPulseEpoch += 1
                                                            if (audioEnabled && isChapter3HighlightedLetterInWordStation) {
                                                                val idx = session.currentIndex
                                                                val wordDone =
                                                                    Chapter3EpisodeContent.spellCompletesWordAfterCorrectRound(idx)
                                                                scope.launch {
                                                                    sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.62f)
                                                                    if (wordDone) {
                                                                        cancelFeedbackVoice()
                                                                        val praise =
                                                                            mutableListOf(
                                                                                AudioClips.VoPraiseMetzuyan,
                                                                                AudioClips.VoPraiseYofi,
                                                                                AudioClips.VoPraiseHitzlacht,
                                                                                AudioClips.VoNice1,
                                                                                AudioClips.VoGoodJob2,
                                                                                AudioClips.VoGoodJob1,
                                                                            )
                                                                        praise.shuffle()
                                                                        val job =
                                                                            scope.launch {
                                                                                voice.playFirstAvailableBlocking(*praise.toTypedArray())
                                                                            }
                                                                        feedbackVoiceJob = job
                                                                        withTimeoutOrNull(2800L) { job.join() }
                                                                    }
                                                                    val isLast = session.currentIndex >= session.totalQuestions - 1
                                                                    advanceAfterRound(
                                                                        isLast,
                                                                        ch3SpellMidWord = !wordDone,
                                                                    )
                                                                }
                                                            } else if (audioEnabled && sagaUsesPickLetterAudioStaging) {
                                                                // Station 1: letter name then a random praise tail (SoundPool), then advance.
                                                                scope.launch {
                                                                    cancelFeedbackVoice()
                                                                    val letterName = AudioClips.letterNameClip(picked)
                                                                    if (letterName == null || !voice.hasAsset(letterName)) {
                                                                        val isLast =
                                                                            session.currentIndex >= session.totalQuestions - 1
                                                                        advanceAfterRound(isLast)
                                                                        return@launch
                                                                    }
                                                                    station1PinnedCorrectLetter = picked
                                                                    val praise =
                                                                        AudioClips.station1CorrectPraiseTailCandidates()
                                                                            .toMutableList()
                                                                    praise.shuffle()
                                                                    val job =
                                                                        scope.launch {
                                                                            voice.playBlocking(letterName)
                                                                            voice.playFirstAvailableBlocking(*praise.toTypedArray())
                                                                        }
                                                                    feedbackVoiceJob = job
                                                                    job.join()
                                                                    val isLast =
                                                                        session.currentIndex >= session.totalQuestions - 1
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
                                                            if (audioEnabled &&
                                                                (!(sagaUsesPickLetterAudioStaging) || isChapter3HighlightedLetterInWordStation || isChapter3AudioLetterRecognitionStation)
                                                            ) {
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
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f, fill = true)
                                                        .then(
                                                            if (isChapter3AudioLetterRecognitionStation) {
                                                                Modifier.padding(top = 6.dp)
                                                            } else if (isChapter3HighlightedLetterInWordStation) {
                                                                Modifier.padding(top = SixStationArcHalfCmNudge)
                                                            } else {
                                                                Modifier
                                                            },
                                                        ),
                                            )
                                            }
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
                                                correctLetterSet =
                                                    if (isChapter3PopAllLettersStation) {
                                                        val w =
                                                            when (session.currentIndex.coerceIn(0, 2)) {
                                                                0 -> "גמל"
                                                                1 -> "דבש"
                                                                else -> "שמש"
                                                            }
                                                        w.toCharArray().map { it.toString() }.toSet()
                                                    } else {
                                                        null
                                                    },
                                                enabled = !inputLocked,
                                                shakePx = optionsShake.value,
                                                visualRoundSeed =
                                                    if (sagaUsesPopBalloonsAudioStaging) {
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
                                                    if (sagaUsesPopBalloonsAudioStaging) {
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
                                                                    val popPlayed =
                                                                        sfx.playFirstAvailableReturningPathAndStreamId(
                                                                            *pops,
                                                                            volume = if (finalCorrectBalloon) 0.72f else 0.64f,
                                                                            rate = rate,
                                                                        )
                                                                    val popPath = popPlayed?.first
                                                                    val popMs = popPath?.let { sfx.durationMs(it) } ?: 0L
                                                                    val r = rate.coerceIn(0.8f, 1.25f)
                                                                    val popWaitMs =
                                                                        when {
                                                                            popPath == null -> 0L
                                                                            popMs > 0 ->
                                                                                (popMs / r).toLong().coerceAtLeast(16L) +
                                                                                    Station2PopTailPaddingMs
                                                                            else ->
                                                                                Station2PopFallbackDurationMs +
                                                                                    Station2PopTailPaddingMs
                                                                        }.coerceAtMost(5000L)
                                                                    if (popWaitMs > 0) delay(popWaitMs)
                                                                    sfx.stopAllStreams()
                                                                    // Prefer pop OR voice (not always both). Always speak the last balloon.
                                                                    val speakLetter = finalCorrectBalloon || Random.nextFloat() < 0.35f
                                                                    if (speakLetter) {
                                                                        val letterClip = AudioClips.letterNameClip(letter)
                                                                        if (letterClip != null) {
                                                                            station2VoiceStreamId =
                                                                                sfx.playReturningStreamId(letterClip, volume = 1f) ?: 0
                                                                            val d = sfx.durationMs(letterClip) ?: 0L
                                                                            if (d > 0) delay(d)
                                                                        }
                                                                    }
                                                                } else {
                                                                    val wrongPops =
                                                                        AudioClips.station2WrongPopPlaylist(balloonIndex)
                                                                    val wrongPopPlayed =
                                                                        sfx.playFirstAvailableReturningPathAndStreamId(
                                                                            *wrongPops,
                                                                            volume = 0.56f,
                                                                            rate = 1f,
                                                                        )
                                                                    val wrongPopPath = wrongPopPlayed?.first
                                                                    val wrongPopMs = wrongPopPath?.let { sfx.durationMs(it) } ?: 0L
                                                                    val wrongWaitMs =
                                                                        when {
                                                                            wrongPopPath == null -> 0L
                                                                            wrongPopMs > 0 ->
                                                                                wrongPopMs.coerceAtLeast(16L) +
                                                                                    Station2PopTailPaddingMs
                                                                            else ->
                                                                                Station2PopFallbackDurationMs +
                                                                                    Station2PopTailPaddingMs
                                                                        }.coerceAtMost(5000L)
                                                                    if (wrongWaitMs > 0) delay(wrongWaitMs)
                                                                    sfx.stopAllStreams()
                                                                    // Wrong balloon: say the tapped letter, then one try-again clip (not both WAVs in one sequence).
                                                                    val letterClip = AudioClips.letterNameClip(letter)
                                                                    if (letterClip != null && voice.hasAsset(letterClip)) {
                                                                        voice.playBlocking(letterClip)
                                                                    }
                                                                    voice.playFirstAvailableBlocking(
                                                                        AudioClips.VoTryAgain2,
                                                                        AudioClips.VoTryAgain1,
                                                                    )
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
                                                    if (!(sagaUsesPopBalloonsAudioStaging)) {
                                                        cancelFeedbackVoice()
                                                    }
                                                    // Wrong balloon: feedback only, stay on same question.
                                                    session.wrongTap()
                                                    shakeEpoch += 1
                                                    wrongTapsThisQuestion += 1
                                                    if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                    if (sagaUsesPopBalloonsAudioStaging) {
                                                        scope.launch {
                                                            inputLocked = true
                                                            dinoVisual = DinoVisual.TryAgain
                                                            val strongerWrongShake =
                                                                (isSagaEpisode(chapterId) && (stationId == Chapter1StationOrder.PICTURE_PICK_ONE || stationId == Chapter1StationOrder.PICTURE_PICK_ALL))
                                                            playShake(
                                                                scope,
                                                                optionsShake,
                                                                chapterId = chapterId,
                                                                strength = if (strongerWrongShake) 1.25f else 1f,
                                                            )
                                                            dinoVisual = DinoVisual.Idle
                                                            inputLocked = false
                                                        }
                                                    } else {
                                                        onWrongFeedback()
                                                    }
                                                },
                                                onAllCorrectPopped = { lastLetter, poppedBalloonColor ->
                                                    val ch1St2 = sagaUsesPopBalloonsAudioStaging
                                                    if (ch1St2) {
                                                        station2PinnedBalloonLetter = lastLetter
                                                        station2PinnedBalloonColor = poppedBalloonColor
                                                    } else {
                                                        cancelFeedbackVoice()
                                                    }
                                                    // Only advance when ALL correct-letter balloons are popped.
                                                    if (isChapter3PopAllLettersStation) {
                                                        scope.launch {
                                                            session.completeCurrentRound()
                                                            if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                                            advanceAfterRound(isLast)
                                                        }
                                                    } else {
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
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            is Question.PictureStartsWithQuestion ->
                                PictureStartsWithGame(
                                    question = current,
                                    instructionText =
                                        "באיזו אות מתחילה המילה?",
                                    enabled = !inputLocked,
                                    shakePx = optionsShake.value,
                                    entryPulseEpoch = entryPulseEpoch,
                                    // Same caption sizing formula as [ImageMatchGame] on station 5 (plan × 1.2f in saga arc).
                                    promptWordSizeMultiplier =
                                        if ((isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) ||
                                            (chapterId == 3 && stationId == 1)
                                        ) {
                                            plan.imageMatchCaptionSizeMultiplier * 1.2f
                                        } else {
                                            1f
                                        },
                                    innerPictureScale =
                                        if ((isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) ||
                                            (chapterId == 3 && stationId == 1)
                                        ) {
                                            Chapter1Station5And6ImageMatchInnerScale.innerScalePictureStartsWith(
                                                catalogEntryId = current.catalogEntryId,
                                                letter = current.correctLetter,
                                                word = current.word,
                                                tintArgb = current.tintArgb,
                                                tileDrawable = current.tileDrawable,
                                            )
                                        } else {
                                            1f
                                        },
                                    // Same outer card width formula as [ImageMatchGame] (stations 5/6).
                                    pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    hintCorrectLetter = current.correctLetter.takeIf { wrongTapsThisQuestion >= 2 },
                                    hintPulseEpoch = hintPulseEpoch,
                                    correctPulseLetter = correctTapPulseLetter,
                                    correctPulseEpoch = correctTapPulseEpoch,
                                    wrongFlashLetter = station4WrongFlashLetter,
                                    wrongFlashEpoch = station4WrongFlashEpoch,
                                    onPickLetter = { picked ->
                                        if (!consumeTapCooldown()) return@PictureStartsWithGame
                                            cancelFeedbackVoice()
                                        when (session.submitPictureStartsWith(picked)) {
                                            AnswerResult.Correct -> {
                                                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                                    scope.launch {
                                                        correctTapPulseLetter = picked
                                                        correctTapPulseEpoch += 1
                                                        cancelFeedbackVoice()
                                                        val letterName = AudioClips.letterNameClip(picked)
                                                        val praise =
                                                            mutableListOf(
                                                                AudioClips.VoKolHakavod,
                                                                AudioClips.VoNice1,
                                                                AudioClips.VoGoodJob2,
                                                                AudioClips.VoGoodJob1,
                                                                AudioClips.VoPraiseMetzuyan,
                                                                AudioClips.VoPraiseYofi,
                                                                AudioClips.VoPraiseHitzlacht,
                                                            )
                                                        praise.shuffle()
                                                        val job =
                                                            scope.launch {
                                                                if (letterName != null && voice.hasAsset(letterName)) {
                                                                    voice.playBlocking(letterName)
                                                                }
                                                                voice.playFirstAvailableBlocking(*praise.toTypedArray())
                                                            }
                                                        feedbackVoiceJob = job
                                                        job.join()
                                                        val isLast =
                                                            session.currentIndex >= session.totalQuestions - 1
                                                        advanceAfterRound(isLast)
                                                    }
                                                } else {
                                                    scope.launch {
                                                        correctTapPulseLetter = picked
                                                        correctTapPulseEpoch += 1
                                                        val isLast =
                                                            session.currentIndex >= session.totalQuestions - 1
                                                        advanceAfterRound(isLast)
                                                    }
                                                }
                                            }
                                            AnswerResult.Wrong -> {
                                                if (audioEnabled) ChildGameAudioHooks.onWrong()
                                                wrongTapsThisQuestion += 1
                                                if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                                    station4WrongFlashLetter = picked
                                                    station4WrongFlashEpoch += 1
                                                    onWrongFeedback(wrongPickedLetter = picked)
                                                } else {
                                                    onWrongFeedback()
                                                }
                                            }
                                            AnswerResult.Finished -> {}
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .scale(entryPulseScale.value)
                                            .then(
                                                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                                    Modifier.offset(y = SixStationArcHalfCmNudge)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                )
                            is Question.ImageMatchQuestion ->
                                if (chapterId == 3 && stationId == 6) {
                                    ImageToWordGame(
                                        question = current,
                                        contentKey = session.currentIndex,
                                        enabled = !inputLocked,
                                        instructionText = "איזו מילה מתאימה לתמונה של:",
                                        onAttempt = { choiceId ->
                                            if (!consumeTapCooldown()) return@ImageToWordGame false
                                            cancelFeedbackVoice()
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
                                                    wrongTapsThisQuestion += 1
                                                    if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                    onWrongFeedback()
                                                    false
                                                }
                                                AnswerResult.Finished -> false
                                            }
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .scale(entryPulseScale.value)
                                                .offset { IntOffset(optionsShake.value.toInt(), 0) },
                                    )
                                } else if (
                                    (chapterId == 3 && stationId == 2) ||
                                        (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
                                ) {
                                    // Same three picture cards as this round's ImageMatch question (station 5 generator/shape).
                                    val matchChoices = current.choices
                                    fun speakNow(play: suspend () -> Unit) {
                                        if (!audioEnabled) return
                                        // Station 6 request: stop the current voice immediately, then speak the newly pressed item.
                                        cancelFeedbackVoice()
                                        feedbackVoiceJob = scope.launch { play() }
                                    }
                                    MatchLetterToWordGame(
                                        choices = matchChoices,
                                        choicePairLimit =
                                            if (chapterId == 3 && stationId == 2) {
                                                3
                                            } else if (chapterId == 3 && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                                4
                                            } else {
                                                3
                                            },
                                        contentKey = session.currentIndex,
                                        enabled = !inputLocked,
                                        compactWideSpread =
                                            isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH,
                                        onWordPressed = { choiceId ->
                                            speakNow { voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId)) }
                                        },
                                        onLetterPressed = { letter ->
                                            val clip = AudioClips.letterNameClip(letter) ?: return@MatchLetterToWordGame
                                            speakNow { voice.playBlocking(clip) }
                                        },
                                        onCorrectMatch = {
                                            if (!audioEnabled) return@MatchLetterToWordGame
                                            // Short reward, no waiting.
                                            scope.launch { sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f) }
                                        },
                                        onWrongMatch = { pickedLetter, pickedChoiceId ->
                                            // Letter/word are spoken on press; keep wrong feedback minimal (no extra voice stacking here).
                                        },
                                        onMatchAttempt = { correct ->
                                            if (!audioEnabled) return@MatchLetterToWordGame
                                            // Wrong speech handled in onWrongMatch (letter name + try again).
                                        },
                                        innerPictureScaleForChoice = { choice ->
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                                Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                            } else if (chapterId == 3 && stationId == 2) {
                                                // Match Episode 1/2 station 5 picture sizing.
                                                Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                            } else {
                                                when {
                                                    choice.word == "מדוזה" || choice.id == "w_מ_3" || choice.tileDrawable == R.drawable.lesson_pic_medusa -> 0.5f
                                                    else -> 1f
                                                }
                                            }
                                        },
                                        captionSizeMultiplier =
                                            plan.imageMatchCaptionSizeMultiplier *
                                                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                                    1.2f
                                                } else if (chapterId == 3 && stationId == 2) {
                                                    1.2f
                                                } else {
                                                    1f
                                                },
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        instructions = "ליחצו על אות והמילה שמתחילה באותה האות",
                                        onSolved = {
                                            if (!consumeTapCooldown()) return@MatchLetterToWordGame
                                            scope.launch {
                                                // Station 6 request: clear/advance only after the last pressed letter/word finishes.
                                                withTimeoutOrNull(4500L) { feedbackVoiceJob?.join() }
                                                // Add a clear, single positive praise between rounds (voice, not stacked).
                                                if (audioEnabled) {
                                                    cancelFeedbackVoice()
                                                    val job =
                                                        scope.launch {
                                                            val praise =
                                                                mutableListOf(
                                                                    AudioClips.VoPraiseMetzuyan,
                                                                    AudioClips.VoPraiseYofi,
                                                                    AudioClips.VoPraiseHitzlacht,
                                                                    AudioClips.VoNice1,
                                                                    AudioClips.VoGoodJob2,
                                                                    AudioClips.VoGoodJob1,
                                                                )
                                                            praise.shuffle()
                                                            voice.playFirstAvailableBlocking(*praise.toTypedArray())
                                                        }
                                                    feedbackVoiceJob = job
                                                    withTimeoutOrNull(3000L) { job.join() }
                                                }
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
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .scale(entryPulseScale.value)
                                                .then(
                                                    if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                                        Modifier.offset(y = SixStationArcHalfCmNudge)
                                                    } else {
                                                        Modifier
                                                    },
                                                ),
                                    )
                                } else {
                                    ImageMatchGame(
                                        question = current,
                                        headerInstructionText =
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                "מצא את המילה המתחילה באות:"
                                            } else if (chapterId == 3 && stationId == 6) {
                                                "איזו תמונה מראה את המילה?"
                                            } else {
                                                null
                                            },
                                        headerInstructionFontScale =
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                1.35f * 1.5f
                                            } else {
                                                1.35f
                                            },
                                        headerPromptWord =
                                            if (chapterId == 3 && stationId == 6) {
                                                current.targetWord
                                            } else {
                                                null
                                            },
                                        showTargetLetterChip =
                                            !(chapterId == 3 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL),
                                        headerTopPaddingDp =
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) 10 else 0,
                                        targetLetterChipOffsetYDp =
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) 0 else -10,
                                        contentKey = session.currentIndex,
                                        enabled = !inputLocked,
                                        shakePx = optionsShake.value,
                                        entryPulseEpoch = entryPulseEpoch,
                                        hintCorrectChoiceId = current.correctChoiceId.takeIf { wrongTapsThisQuestion >= 2 },
                                        hintPulseEpoch = hintPulseEpoch,
                                        showWordCaptions = !(chapterId == 3 && stationId == 6),
                                        // Episode 1 station 5: caption text +20%.
                                        captionSizeMultiplier =
                                            plan.imageMatchCaptionSizeMultiplier *
                                                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                    1.2f
                                                } else {
                                                    1f
                                                },
                                        pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                        innerPictureScaleForChoice = { choice ->
                                            // Station 5 request: all pictures should look same-size as the heart.
                                            // Use Crop in the card and keep per-choice scaling at 1x.
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                            } else {
                                                1f
                                            }
                                        },
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        onAttempt = { choiceId ->
                                            if (!consumeTapCooldown()) return@ImageMatchGame false
                                            cancelFeedbackVoice()
                                            when (session.submitImageMatch(choiceId)) {
                                                AnswerResult.Correct -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                    scope.launch {
                                                        if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                            // Episode 1 station 5: say the tapped WORD, then the existing "good job" flow will run.
                                                            // Episode 3 station 5: do NOT speak the word (word should appear only in the top prompt).
                                                            if (chapterId != 3) {
                                                                voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId))
                                                            }
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
                                                    if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                        onWrongFeedback(wrongWordCatalogId = choiceId)
                                                    } else {
                                                        onWrongFeedback()
                                                    }
                                                    false
                                                }
                                                AnswerResult.Finished -> false
                                            }
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .scale(entryPulseScale.value)
                                                .then(
                                                    if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                        Modifier.offset(y = SixStationArcHalfCmNudge)
                                                    } else {
                                                        Modifier
                                                    },
                                                ),
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
            // Same in-round dino mouth animation for all chapters that use this screen (road-specific walks live in Journey).
            val talkFrames =
                listOf(R.drawable.dino_talk_0, R.drawable.dino_talk_1, R.drawable.dino_talk_2, R.drawable.dino_talk_3)
            AnimatedTalkingCharacter(
                idleRes = dinoDrawable,
                talkFrameResIds = talkFrames,
                isTalking = dinoTalking || (isSagaEpisode(chapterId) && inputLocked && dinoVisual == DinoVisual.Jump),
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
private fun Chapter3SpellWordRow(
    word: String,
    highlightIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        word.forEachIndexed { i, ch ->
            Text(
                text = ch.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = if (i == highlightIndex) Color(0xFFE65100) else Color(0xFF0B2B3D),
                modifier = Modifier.padding(horizontal = 3.dp),
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
                    stationId == 6 -> "ליחצו על אות והמילה שמתחילה באותה האות"
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
    /**
     * Episode 1 station 1: pick randomly between "מצא את האות" and "בחר את האות" when both files exist;
     * other stations use only "בחר את האות" when present.
     */
    station1IntroVariant: Boolean = false,
) {
    // Prefer one combined clip when the asset exists (e.g. full "בחר את האות …" per letter).
    val combined = AudioClips.chooseLetterClip(letter)
    if (combined != null && voice.hasAsset(combined)) {
        voice.playBlocking(combined)
        return
    }

    val letterName = AudioClips.letterNameClip(letter)
    val parts =
        buildList {
            if (station1IntroVariant) {
                val findOk = voice.hasAsset(AudioClips.VoFindLetter)
                val chooseOk = voice.hasAsset(AudioClips.VoChooseLetter)
                when {
                    findOk && chooseOk ->
                        add(
                            if (Random.nextBoolean()) {
                                AudioClips.VoFindLetter
                            } else {
                                AudioClips.VoChooseLetter
                            },
                        )
                    findOk -> add(AudioClips.VoFindLetter)
                    chooseOk -> add(AudioClips.VoChooseLetter)
                }
            } else {
                if (voice.hasAsset(AudioClips.VoChooseLetter)) add(AudioClips.VoChooseLetter)
            }
            if (letterName != null && voice.hasAsset(letterName)) add(letterName)
        }
    when (parts.size) {
        0 -> return
        1 -> voice.playBlocking(parts[0])
        else -> voice.playSequenceBlocking(*parts.toTypedArray())
    }
}

    private suspend fun speakPromptForQuestion(
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
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
                // Episode 1 station 4: instruction + spoken word (e.g. "ברווז").
                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                    val intro = AudioClips.WhichLetterDoesWordStart
                    val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                    // Exact timing/overlap handled in the prompt startup path (has access to SoundPool).
                    // Keep this path as a safe fallback.
                    if (voice.hasAsset(intro) && voice.hasAsset(wordPath)) voice.playSequenceBlocking(intro, wordPath)
                    else speakLetterPrompt(voice, q.correctLetter)
                } else {
                    speakLetterPrompt(voice, q.correctLetter)
                }
            }
            is Question.ImageMatchQuestion -> {
                if (chapterId == 3 && stationId == 6) {
                    val clip = AudioClips.ImageToWordInstructions
                    if (voice.hasAsset(clip)) {
                        voice.playBlocking(clip)
                    } else {
                        // Fallback to legacy station 6 instruction.
                        voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
                    }
                    return
                }
                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                    // Episode 1 station 5: "איזו מילה מתחילה באות" + letter name (SoundPool overlap when duration parses).
                    val intro = AudioClips.WhichWordStartsWithLetter
                    val letterName = AudioClips.letterNameClip(q.targetLetter)
                    val introMs = sfx.durationMs(intro) ?: 0L
                    if (introMs > 0L && letterName != null) {
                        sfx.stopAllStreams()
                        sfx.playReturningStreamId(intro, volume = 1f)
                        val baseWhichWordLeadFrac =
                            Station5WhichWordIntroLetterLeadFraction *
                                Station5WhichWordIntroToLetterLeadScale
                        val whichWordLeadFrac =
                            baseWhichWordLeadFrac +
                                Station5WhichWordIntroToLetterGapBoost * (1f - baseWhichWordLeadFrac)
                        val lead =
                            (introMs * whichWordLeadFrac)
                                .toLong()
                                .coerceIn(16L, introMs)
                        delay(lead + Station5WhichWordIntroToLetterExtraPauseMs)
                        sfx.playReturningStreamId(letterName, volume = 1f)
                    } else {
                        voice.playBlocking(intro)
                        if (letterName != null) voice.playBlocking(letterName)
                    }
                } else if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                    // Station 6: "ליחצו על אות והמילה שמתחילה באותה האות".
                    // Episode 3 uses a dedicated recording; fall back to the shared file.
                    if (chapterId == 3 && voice.hasAsset(AudioClips.Ch3MatchLetterToWordInstructions)) {
                        voice.playBlocking(AudioClips.Ch3MatchLetterToWordInstructions)
                    } else {
                        voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
                    }
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
    peakScale: Float = 1.14f,
): Job =
    scope.launch {
        dinoScale.snapTo(1f)
        dinoScale.animateTo(peakScale, tween(120))
        dinoScale.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 560f))
    }

private fun playShake(
    scope: CoroutineScope,
    optionsShake: Animatable<Float, AnimationVector1D>,
    chapterId: Int,
    strength: Float = 1f,
): Job =
    scope.launch {
        optionsShake.snapTo(0f)
        val amp = (if (isSagaEpisode(chapterId)) 20f else 18f) * strength.coerceIn(0.8f, 1.6f)
        repeat(5) { i ->
            optionsShake.animateTo(
                if (i % 2 == 0) amp else -amp,
                tween(45),
            )
        }
        optionsShake.animateTo(0f, tween(60))
    }
