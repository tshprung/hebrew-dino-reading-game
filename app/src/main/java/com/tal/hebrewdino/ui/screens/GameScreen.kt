package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.GameAudioEngine
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.AppAnalytics
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.StationVariant
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.domain.hasVariant
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.Job

internal enum class GamePhase { Intro, Play }

internal enum class DinoVisual { Idle, TryAgain, Jump }

private const val IntroDurationMs = 450L

internal const val BetweenQuestionFadeMs = 80

/** Chapters that use the shared six-station journey ([Chapter1StationOrder]); intros, art, and letter pools differ. */
private val SixStationArcChapterRange = 1..5

internal fun isSagaEpisode(chapterId: Int): Boolean = chapterId in SixStationArcChapterRange

internal class GameAudioRuntimeState {
    var feedbackVoiceJob: Job? = null
    var promptVoiceJob: Job? = null
    var lastPraiseAssetPath: String? = null
}

private object GameAudioPreloader {
    suspend fun preloadStation1(
        audioEnabled: Boolean,
        sagaUsesPickLetterAudioStaging: Boolean,
        chapterId: Int,
        letterPoolSpec: LetterPoolSpec,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && sagaUsesPickLetterAudioStaging)) return
        val poolLetters = letterPoolSpec.groups.flatten().distinct()
        voice.warmUp(
            AudioClips.VoChooseLetter,
            AudioClips.VoBachorEtHaot,
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
        val station1IntroExtras =
            if (chapterId == 4) {
                arrayOf(AudioClips.VoBachorEtHaot)
            } else {
                emptyArray()
            }
        sfx.preload(
            AudioClips.VoChooseLetter,
            *station1IntroExtras,
            *perLetterPaths.toTypedArray(),
            AudioClips.VoTryAgain1,
            AudioClips.VoTryAgain2,
            *AudioClips.station1CorrectPraiseTailCandidates(),
        )
    }

    suspend fun preloadPopBalloons(
        audioEnabled: Boolean,
        usesPopBalloonsSoundPoolPrompt: Boolean,
        letterPoolSpec: LetterPoolSpec,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && usesPopBalloonsSoundPoolPrompt)) return
        val letters = letterPoolSpec.groups.flatten().distinct()
        val paths = ArrayList<String>()
        paths.add(AudioClips.PopBalloonsWithLetter)
        paths.add(AudioClips.PopAllBalloonsWithLetter)
        paths.add(AudioClips.VoTryAgain1)
        paths.add(AudioClips.VoKolHakavod)
        paths.add(AudioClips.VoGoodJob2)
        paths.add(AudioClips.SfxBalloonPopSoft)
        paths.add(AudioClips.SfxBalloonPopWrongFunny)
        paths.add(AudioClips.SfxBalloonPop)
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

    suspend fun preloadFindGrid(
        audioEnabled: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        letterPoolSpec: LetterPoolSpec,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && sagaUsesFindGridAudioStaging)) return
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
}

@Composable
private fun GameAudioLifecycleEffects(
    lifecycleOwner: LifecycleOwner,
    stationId: Int,
    cancelFeedbackVoice: () -> Unit,
    releaseAudio: () -> Unit,
) {
    val cancelFeedbackVoiceLatest by rememberUpdatedState(cancelFeedbackVoice)
    val releaseAudioLatest by rememberUpdatedState(releaseAudio)

    DisposableEffect(lifecycleOwner, stationId) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    cancelFeedbackVoiceLatest()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cancelFeedbackVoiceLatest()
            releaseAudioLatest()
        }
    }
}

@Composable
private fun GameAudioPreloadEffects(
    stationId: Int,
    chapterId: Int,
    letterPoolSpec: LetterPoolSpec,
    audioEnabled: Boolean,
    sagaUsesPickLetterAudioStaging: Boolean,
    usesPopBalloonsSoundPoolPrompt: Boolean,
    sagaUsesFindGridAudioStaging: Boolean,
    voice: VoicePlayer,
    sfx: SoundPoolPlayer,
) {
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadStation1(
            audioEnabled = audioEnabled,
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            chapterId = chapterId,
            letterPoolSpec = letterPoolSpec,
            voice = voice,
            sfx = sfx,
        )
    }

    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadPopBalloons(
            audioEnabled = audioEnabled,
            usesPopBalloonsSoundPoolPrompt = usesPopBalloonsSoundPoolPrompt,
            letterPoolSpec = letterPoolSpec,
            sfx = sfx,
        )
    }

    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadFindGrid(
            audioEnabled = audioEnabled,
            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
            letterPoolSpec = letterPoolSpec,
            voice = voice,
            sfx = sfx,
        )
    }
}

/**
 * Episode 1 stations 2–3: start the letter name this far into the intro clip on SoundPool (overlap).
 * 0.94 ≈ halving the remaining pause vs 0.88 (i.e. moving halfway from 0.88 toward 1.0).
 */
private const val StationIntroLetterLeadFraction = 0.94f
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
internal const val Station1WrongLetterToFollowLeadFraction = 0.7f
/** Adds this fraction of the remaining tail `(1 - baseWrongLead)` to the lead before try-again (station 1 wrong; also station 4 wrong SoundPool path). */
internal const val Station1WrongLetterToTryAgainGapBoost = 0.1f
/** Station 4 wrong: add extra space before try-again (fraction of remaining tail). */
internal const val Station4WrongLetterToTryAgainGapBoost = 0.25f
/**
 * Station 4 wrong: multiplier on the delay before try-again after the wrong letter name (SoundPool overlap).
 * Halves the wait vs [Station1WrongLetterToFollowLeadFraction] alone — tighter gap before "נסה שוב" / similar.
 */
internal const val Station4WrongLetterToFollowLeadScale = 0.5f
/** If pop WAV duration can't be parsed, wait this long so [stopAllStreams] doesn't cut the pop instantly. */
private const val Station2PopFallbackDurationMs = 160L
/** Extra wait after pop SFX before [stopAllStreams] so the tail is not clipped slightly short. */
private const val Station2PopTailPaddingMs = 52L
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

/** Episode 1: chance to play a short praise voice after a correct round. */
internal const val Episode1PraiseChance = 0.62f

/** Episode 4 stations 1–5 help "רמז": show target / lock choices for this long, then hide. */
@Composable
fun GameScreen(
    stationId: Int,
    chapterId: Int,
    chapterTitle: String,
    stageLabel: String,
    stationHeaderMode: StationHeaderMode = StationHeaderMode.None,
    topChromeProgressOverride: Pair<Int, Int>? = null,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    backgroundRes: Int = R.drawable.forest_bg_level_overlay,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    /** Replay of an already-completed station: no extra in-game dino motion after correct answers. */
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // UX: no audio for now (per request).
    val audioEnabled = true

    val gameViewModel: GameViewModel =
        viewModel(
            key = "game-$chapterId-$stationId-${plan.hashCode()}-${letterPoolSpec.hashCode()}",
            factory = remember(plan, letterPoolSpec) { GameViewModel.Factory(plan = plan, letterPoolSpec = letterPoolSpec) },
        )
    val session = gameViewModel.session
    val listenOnly = plan.listenOnlyTargetPrompt
    val stationUiSpec = remember(chapterId, stationId) { StationBehaviorRegistry.getStationUiSpec(chapterId, stationId) }
    val helpColumnEnabled = Episode4Help.isHelpColumnActive(stationUiSpec)
    val sagaUsesPickLetterAudioStaging = stationUiSpec.audioStagingPickLetter
    val sagaUsesPopBalloonsAudioStaging = stationUiSpec.audioStagingPopBalloons
    val usesPopBalloonsSoundPoolPrompt = stationUiSpec.popBalloonsUseSoundPoolPrompt
    val sagaUsesFindGridAudioStaging = stationUiSpec.audioStagingFindGrid
    val isChapter3HighlightedLetterInWordStation =
        stationUiSpec.templateId == StationTemplateId.PickLetter &&
            stationUiSpec.hasVariant(StationVariant.HighlightedLetterInWord) &&
            plan.mode == StationQuizMode.PickLetter &&
            plan.highlightedLetterInWordPickLetter

    val highlightedInWordRound =
        if (isChapter3HighlightedLetterInWordStation) {
            session.highlightedLetterInWordRound()
        } else {
            null
        }

    val isChapter3AudioLetterRecognitionStation =
        stationUiSpec.templateId == StationTemplateId.PickLetter &&
            stationUiSpec.hasVariant(StationVariant.Chapter3AudioLetterRecognition) &&
            plan.mode == StationQuizMode.PickLetter &&
            plan.chapter3AudioLetterRecognition

    val isChapter3PopAllLettersStation =
        stationUiSpec.templateId == StationTemplateId.PopBalloons &&
            stationUiSpec.hasVariant(StationVariant.PopAllLettersInWord) &&
            plan.mode == StationQuizMode.PopBalloons &&
            plan.popAllLettersInWord

    LaunchedEffect(chapterId, stationId) {
        AppAnalytics.logLevelStart(chapterId = chapterId, stationId = stationId)
    }

    if (chapterId == TrainingV1Config.CHAPTER_ID) {
        topChromeProgressOverride?.first
    } else {
        null
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val rewardRepo = remember(context) { CharacterRepository(context.applicationContext) }
    val audio = remember { GameAudioEngine(context = context) }
    val voice = audio.voice
    val sfx = audio.sfx
    val gameFeedback = remember(stationId, sfx, view) { GameFeedback(scope, sfx, view) }

    val audioRuntime = remember(stationId) { GameAudioRuntimeState() }
    val contentAlpha = remember(stationId) { Animatable(1f) }
    val optionsShake = remember(stationId) { Animatable(0f) }
    val dinoScale = remember(stationId) { Animatable(1f) }
    val dinoForward = remember(stationId) { Animatable(0f) }
    val dinoSlip = remember(stationId) { Animatable(0f) }
    val dinoTilt = remember(stationId) { Animatable(0f) }
    val hintHeaderScale = remember(stationId) { Animatable(1f) }
    val entryPulseScale = remember(stationId) { Animatable(1f) }
    /**
     * Episode 1 station 2: after the last correct balloon, show a small balloon (last pop) beside the
     * main target chip until round-end praise finishes — not a second letter chip.
     */
    /** Episode 1 station 2: counts correct pops within the current question (for pop SFX variety). */
    val popBalloonsHelpControlsEnabled = stationUiSpec.popBalloonsHelpControlsEnabled && !helpColumnEnabled
    val showPopBalloonsTargetLetterChip = !listenOnly && !popBalloonsHelpControlsEnabled
    val gameChoicesEnabled =
        !gameViewModel.inputLocked &&
            !gameViewModel.episode4HelpLocksChoices &&
            !(popBalloonsHelpControlsEnabled && gameViewModel.balloonHelpLocksChoices)

    fun cancelFeedbackVoice() {
        GameAudioActions.cancelFeedbackVoice(
            voice = voice,
            sfx = sfx,
            audioRuntime = audioRuntime,
        )
    }

    GameAudioLifecycleEffects(
        lifecycleOwner = lifecycleOwner,
        stationId = stationId,
        cancelFeedbackVoice = { cancelFeedbackVoice() },
        releaseAudio = { audio.release() },
    )

    val performSideHelpReplay: () -> Unit = {
        if (!gameViewModel.inputLocked) {
            SideHelpActions.startReplay(
                audioEnabled = audioEnabled,
                isPlayPhase = gameViewModel.phase == GamePhase.Play && !gameViewModel.inputLocked,
                episode4HelpEnabled = helpColumnEnabled,
                popBalloonsHelpEnabled = popBalloonsHelpControlsEnabled,
                chapterId = chapterId,
                stationId = stationId,
                plan = plan,
                stationUiSpec = stationUiSpec,
                session = session,
                voice = voice,
                sfx = sfx,
                cancelFeedbackVoice = { cancelFeedbackVoice() },
                audioRuntime = audioRuntime,
                scope = scope,
            )
        }
    }

    val performSideHelpHint: () -> Unit = {
        SideHelpActions.performHint(
            isPlayPhase = gameViewModel.phase == GamePhase.Play && !gameViewModel.inputLocked,
            episode4HelpEnabled = helpColumnEnabled,
            popBalloonsHelpEnabled = popBalloonsHelpControlsEnabled,
            stationId = stationId,
            stationUiSpec = stationUiSpec,
            session = session,
            gameViewModel = gameViewModel,
            scope = scope,
        )
    }
    val jumpFrames =
        remember(stationId) {
            listOf(R.drawable.dino_jump_0, R.drawable.dino_jump_1, R.drawable.dino_jump_2)
        }
    val forwardDir = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f

    GameAudioPreloadEffects(
        stationId = stationId,
        chapterId = chapterId,
        letterPoolSpec = letterPoolSpec,
        audioEnabled = audioEnabled,
        sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
        usesPopBalloonsSoundPoolPrompt = usesPopBalloonsSoundPoolPrompt,
        sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
        voice = voice,
        sfx = sfx,
    )

    val current = session.currentQuestion
    if (current == null) {
        GameCompletionSafety(
            stationId = stationId,
            sessionCurrentIndex = session.currentIndex,
            session = session,
            gameViewModel = gameViewModel,
            onComplete = onComplete,
            modifier = modifier,
        )
        return
    }

    LaunchedEffect(stationId, session.currentIndex) {
        GameRoundStartActions.run(
            gameViewModel = gameViewModel,
            audioEnabled = audioEnabled,
            chapterId = chapterId,
            stationId = stationId,
            listenOnlyTargetPrompt = listenOnly,
            stationTemplateId = stationUiSpec.templateId,
            planPopAllLettersInWord = plan.popAllLettersInWord,
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
            isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
            isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
            session = session,
            scope = scope,
            voice = voice,
            sfx = sfx,
            introDurationMs = IntroDurationMs,
            station1IntroLetterLeadFraction = Station1IntroLetterLeadFraction,
            station1IntroToLetterLeadScale = Station1IntroToLetterLeadScale,
            station2BalloonIntroLetterLeadFraction = Station2BalloonIntroLetterLeadFraction,
            station2IntroToLetterLeadScale = Station2IntroToLetterLeadScale,
            station2BalloonIntroToLetterGapBoost = Station2BalloonIntroToLetterGapBoost,
            station2BalloonIntroToLetterExtraPauseMs = Station2BalloonIntroToLetterExtraPauseMs,
            station4IntroWordLeadFraction = Station4IntroWordLeadFraction,
            station4IntroToWordLeadScale = Station4IntroToWordLeadScale,
            station4IntroToWordGapBoost = Station4IntroToWordGapBoost,
            station4IntroToWordExtraPauseMs = Station4IntroToWordExtraPauseMs,
            cancelFeedbackVoice = { cancelFeedbackVoice() },
            audioRuntime = audioRuntime,
        )
    }

    DinoJumpAnimation(
        gameViewModel = gameViewModel,
        jumpFramesCount = jumpFrames.size,
    )

    HintHeaderPulseAnimation(
        hintPulseEpoch = gameViewModel.hintPulseEpoch,
        stationId = stationId,
        hintHeaderScale = hintHeaderScale,
    )
    EntryPulseAnimation(
        entryPulseEpoch = gameViewModel.entryPulseEpoch,
        stationId = stationId,
        entryPulseScale = entryPulseScale,
    )

    suspend fun advanceAfterRound(isLast: Boolean, ch3SpellMidWord: Boolean = false) {
        AdvanceAfterRoundActions.run(
            scope = scope,
            gameViewModel = gameViewModel,
            audioEnabled = audioEnabled,
            sagaEpisode = isSagaEpisode(chapterId),
            chapterId = chapterId,
            stationId = stationId,
            isLast = isLast,
            ch3SpellMidWord = ch3SpellMidWord,
            suppressInGameDinoProgress = suppressInGameDinoProgress,
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
            isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
            isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
            gameFeedback = gameFeedback,
            voice = voice,
            cancelFeedbackVoice = { cancelFeedbackVoice() },
            audioRuntime = audioRuntime,
            dinoForward = dinoForward,
            forwardDir = forwardDir,
            dinoScale = dinoScale,
            contentAlpha = contentAlpha,
            session = session,
            onComplete = onComplete,
            onLevelCompleteHook = { ChildGameAudioHooks.onLevelComplete() },
            grantFoodReward = { delta ->
                rewardRepo.addFood(delta)
                rewardRepo.setPendingRewardFoodDelta(delta)
            },
        )
    }

    fun onWrongFeedback(
        wrongPickedLetter: String? = null,
        wrongWordCatalogId: String? = null,
        wrongPickedLetterAlreadySpoken: Boolean = false,
        wrongWordAlreadySpoken: Boolean = false,
    ) {
        val mistakeType =
            when {
                wrongWordCatalogId != null -> "wrong_word"
                wrongPickedLetter != null -> "wrong_letter"
                else -> "wrong_choice"
            }
        AppAnalytics.logLevelRetry(
            chapterId = chapterId,
            stationId = stationId,
            mistakeType = mistakeType,
        )
        WrongFeedbackActions.trigger(
            scope = scope,
            gameViewModel = gameViewModel,
            audioEnabled = audioEnabled,
            sagaEpisode = isSagaEpisode(chapterId),
            chapterId = chapterId,
            stationId = stationId,
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
            isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
            gameFeedback = gameFeedback,
            voice = voice,
            sfx = sfx,
            cancelFeedbackVoice = { cancelFeedbackVoice() },
            audioRuntime = audioRuntime,
            optionsShake = optionsShake,
            dinoSlip = dinoSlip,
            dinoTilt = dinoTilt,
            onWrongHook = { ChildGameAudioHooks.onWrong() },
            wrongPickedLetter = wrongPickedLetter,
            wrongWordCatalogId = wrongWordCatalogId,
            wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
            wrongWordAlreadySpoken = wrongWordAlreadySpoken,
        )
    }


    Box(modifier = modifier.fillMaxSize()) {
        GameScreenBackgroundLayer(
            chapterId = chapterId,
            backgroundRes = backgroundRes,
        )

        // Station screens: keep the top bar fixed (like Journey). Do not show collected eggs/letters/debug inside stations.
        val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
        val contentTopInset =
            gameScreenContentTopInset(
                isCompactLandscapePhone = isCompactLandscapePhone,
                stationUiSpec = stationUiSpec,
            )
        val (topQuestionNumber, topTotalQuestions) =
            topChromeProgressOverride ?: (session.questionNumber to session.totalQuestions)
        GameScreenTopChrome(
            onBack = onBack,
            questionNumber = topQuestionNumber,
            totalQuestions = topTotalQuestions,
            stationHeaderMode = stationHeaderMode,
            chapterTitle = chapterTitle,
            stageLabel = stageLabel,
            underBackLabel = if (chapterId == TrainingV1Config.CHAPTER_ID) stageLabel else null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .gameScreenStationTopChrome(
                        isCompactLandscapePhone = isCompactLandscapePhone,
                        chapterId = chapterId,
                    )
                    .align(Alignment.TopCenter)
                    .zIndex(4f),
        )

        if (isCompactLandscapePhone) {
            val dinoDrawable =
                when (gameViewModel.dinoVisual) {
                    DinoVisual.Idle -> R.drawable.dino_idle
                    DinoVisual.TryAgain -> R.drawable.dino_try_again
                    DinoVisual.Jump -> jumpFrames[gameViewModel.jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
                }
            val talkFrames =
                listOf(R.drawable.dino_talk_0, R.drawable.dino_talk_1, R.drawable.dino_talk_2, R.drawable.dino_talk_3)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GameScreenDinoLayer(
                        idleRes = dinoDrawable,
                        talkFrameResIds = talkFrames,
                        isTalking =
                            gameViewModel.dinoTalking ||
                                (isSagaEpisode(chapterId) && gameViewModel.inputLocked && gameViewModel.dinoVisual == DinoVisual.Jump),
                        dinoForward = dinoForward,
                        dinoSlip = dinoSlip,
                        dinoTilt = dinoTilt,
                        dinoScale = dinoScale,
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 6.dp, bottom = 8.dp)
                                .scale(0.70f),
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (isCompactLandscapePhone) 6.dp else 8.dp,
                        end = if (isCompactLandscapePhone) 6.dp else 8.dp,
                        top = contentTopInset,
                        bottom = if (isCompactLandscapePhone) 4.dp else 8.dp,
                    )
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
                    val cancelFeedbackVoiceCb: () -> Unit = { cancelFeedbackVoice() }

                    fun handleFindGridSagaGridLetterTapped(
                        tapped: String,
                        question: Question.FindLetterGridQuestion,
                    ) {
                        FindGridActions.handleSagaGridLetterTapped(
                            audioEnabled = audioEnabled,
                            tapped = tapped,
                            question = question,
                            scope = scope,
                            sfx = sfx,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioRuntime = audioRuntime,
                        )
                    }

                    fun handleFindGridCellTapped(
                        index: Int,
                        question: Question.FindLetterGridQuestion,
                    ) {
                        FindGridActions.handleCellTapped(
                            gameViewModel = gameViewModel,
                            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            session = session,
                            onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken ->
                                onWrongFeedback(
                                    wrongPickedLetter = wrongPickedLetter,
                                    wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                )
                            },
                            index = index,
                            question = question,
                        )
                    }

                    fun handleFindGridCompleted() {
                        FindGridActions.handleCompleted(
                            gameViewModel = gameViewModel,
                            scope = scope,
                            session = session,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                        )
                    }

                    fun handlePickLetterPick(picked: String) {
                        PickLetterActions.handlePick(
                            picked = picked,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            stationId = stationId,
                            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                            isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                            isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                            session = session,
                            scope = scope,
                            voice = voice,
                            sfx = sfx,
                            audioRuntime = audioRuntime,
                            onWrongFeedback = { wrongPickedLetter ->
                                onWrongFeedback(wrongPickedLetter = wrongPickedLetter)
                            },
                            advanceAfterRound = { isLast, ch3SpellMidWord ->
                                advanceAfterRound(isLast, ch3SpellMidWord = ch3SpellMidWord)
                            },
                        )
                    }

                    suspend fun handlePopBalloonsPopSfx(
                        letter: String,
                        isCorrect: Boolean,
                        finalCorrectBalloon: Boolean,
                        balloonIndex: Int,
                    ) {
                        PopBalloonsActions.handlePopSfx(
                            audioEnabled = audioEnabled,
                            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                            letter = letter,
                            isCorrect = isCorrect,
                            finalCorrectBalloon = finalCorrectBalloon,
                            balloonIndex = balloonIndex,
                            scope = scope,
                            voice = voice,
                            sfx = sfx,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioRuntime = audioRuntime,
                            nextStation2CorrectPopVariant = {
                                val v = gameViewModel.station2CorrectPopCount
                                gameViewModel.station2CorrectPopCount += 1
                                v
                            },
                            station2PopTailPaddingMs = Station2PopTailPaddingMs,
                            station2PopFallbackDurationMs = Station2PopFallbackDurationMs,
                        )
                    }

                    fun handlePopBalloonsWrongPick() {
                        PopBalloonsActions.handleWrongPick(
                            gameViewModel = gameViewModel,
                            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            onWrongFeedback = { onWrongFeedback() },
                            session = session,
                            chapterId = chapterId,
                            stationId = stationId,
                            scope = scope,
                            optionsShake = optionsShake,
                        )
                    }

                    fun handlePopBalloonsAllCorrectPopped(
                        lastLetter: String,
                        poppedBalloonColor: Color,
                        popAll: Boolean,
                    ) {
                        PopBalloonsActions.handleAllCorrectPopped(
                            lastLetter = lastLetter,
                            poppedBalloonColor = poppedBalloonColor,
                            isChapter3PopAllLettersStation = popAll,
                            gameViewModel = gameViewModel,
                            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                            chapterId = chapterId,
                            stationId = stationId,
                            audioEnabled = audioEnabled,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            session = session,
                            scope = scope,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                        )
                    }

                    fun handlePictureStartsWithPick(picked: String) {
                        PictureStartsWithActions.handlePick(
                            picked = picked,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            stationId = stationId,
                            sagaEpisode = isSagaEpisode(chapterId),
                            session = session,
                            scope = scope,
                            voice = voice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken ->
                                onWrongFeedback(
                                    wrongPickedLetter = wrongPickedLetter,
                                    wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                )
                            },
                        )
                    }

                    fun handleImageToWordReplayCorrectChoice() {
                        ImageMatchActions.handleImageToWordReplayCorrectChoice(
                            audioEnabled = audioEnabled,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            chapterId = chapterId,
                            session = session,
                            scope = scope,
                            voice = voice,
                            audioRuntime = audioRuntime,
                        )
                    }

                    fun handleImageToWordWordPressed(choiceId: String) {
                        ImageMatchActions.handleImageToWordWordPressed(
                            choiceId = choiceId,
                            audioEnabled = audioEnabled,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            chapterId = chapterId,
                            scope = scope,
                            voice = voice,
                            audioRuntime = audioRuntime,
                        )
                    }

                    fun handleImageToWordAttempt(choiceId: String): Boolean {
                        return ImageMatchActions.handleImageToWordAttempt(
                            choiceId = choiceId,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            session = session,
                            scope = scope,
                            voice = voice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongWordCatalogId ->
                                onWrongFeedback(wrongWordCatalogId = wrongWordCatalogId)
                            },
                        )
                    }

                    fun handleImageMatchAttempt(choiceId: String): Boolean {
                        return ImageMatchActions.handleImageMatchAttempt(
                            choiceId = choiceId,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            stationId = stationId,
                            sagaEpisode = isSagaEpisode(chapterId),
                            session = session,
                            scope = scope,
                            voice = voice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongWordCatalogId, generic ->
                                if (generic) {
                                    onWrongFeedback()
                                } else {
                                    onWrongFeedback(wrongWordCatalogId = wrongWordCatalogId)
                                }
                            },
                        )
                    }

                    fun handleFinaleWrongPlacement() {
                        session.wrongTap()
                        gameViewModel.shakeEpoch += 1
                    }

                    GameQuestionHost(
                        ui =
                            GameQuestionHostUi(
                                phase = gameViewModel.phase,
                                stationUiSpec = stationUiSpec,
                                stationId = stationId,
                                chapterId = chapterId,
                                trainingRoundIndex =
                                    if (chapterId == TrainingV1Config.CHAPTER_ID) {
                                        topChromeProgressOverride?.first
                                    } else {
                                        null
                                    },
                                plan = plan,
                                listenOnly = listenOnly,
                                sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                                sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                                sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                                isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                                isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                                isChapter3PopAllLettersStation = isChapter3PopAllLettersStation,
                                highlightedInWordWord = highlightedInWordRound?.word,
                                highlightedInWordSlotIndex = highlightedInWordRound?.slotIndex,
                                audioEnabled = audioEnabled,
                                isCompactLandscapePhone = isCompactLandscapePhone,
                                episode4HelpSt15 = helpColumnEnabled,
                                episode4HelpActiveHintLetter = gameViewModel.episode4HelpActiveHintLetter,
                                episode4HelpStation2BalloonHintEpoch = gameViewModel.episode4Station2BalloonHintEpoch,
                                episode4HelpStation3GridHintEpoch = gameViewModel.episode4Station3GridHintEpoch,
                                popBalloonsHelpControlsEnabled = popBalloonsHelpControlsEnabled,
                                balloonHelpHintLetter = gameViewModel.balloonHelpHintLetter,
                                showPopBalloonsTargetLetterChip = showPopBalloonsTargetLetterChip,
                            ),
                        state =
                            GameQuestionHostState(
                                station1PinnedCorrectLetter = gameViewModel.station1PinnedCorrectLetter,
                                station2PinnedBalloonLetter = gameViewModel.station2PinnedBalloonLetter,
                                station2PinnedBalloonColor = gameViewModel.station2PinnedBalloonColor,
                                hintHeaderScale = hintHeaderScale.value,
                                enabled = gameChoicesEnabled,
                                shakeEpoch = gameViewModel.shakeEpoch,
                                wrongTapsThisQuestion = gameViewModel.wrongTapsThisQuestion,
                                hintPulseEpoch = gameViewModel.hintPulseEpoch,
                                correctTapPulseLetter = gameViewModel.correctTapPulseLetter,
                                correctTapPulseEpoch = gameViewModel.correctTapPulseEpoch,
                                station4WrongFlashLetter = gameViewModel.station4WrongFlashLetter,
                                station4WrongFlashEpoch = gameViewModel.station4WrongFlashEpoch,
                                station4PinnedCorrectLetter = gameViewModel.station4PinnedCorrectLetter,
                                entryPulseEpoch = gameViewModel.entryPulseEpoch,
                                entryPulseScale = entryPulseScale.value,
                                optionsShakePx = optionsShake.value,
                            ),
                        deps =
                            GameQuestionHostDeps(
                                session = session,
                                gameViewModel = gameViewModel,
                                scope = scope,
                                voice = voice,
                                sfx = sfx,
                                cancelFeedbackVoice = cancelFeedbackVoiceCb,
                                audioRuntime = audioRuntime,
                            ),
                        handlers =
                            GameQuestionHostHandlers(
                                performSideHelpReplay = performSideHelpReplay,
                                handleFindGridSagaGridLetterTapped = ::handleFindGridSagaGridLetterTapped,
                                handleFindGridCellTapped = ::handleFindGridCellTapped,
                                handleFindGridCompleted = ::handleFindGridCompleted,
                                handlePickLetterPick = ::handlePickLetterPick,
                                handlePopBalloonsPopSfx = ::handlePopBalloonsPopSfx,
                                handlePopBalloonsWrongPick = ::handlePopBalloonsWrongPick,
                                handlePopBalloonsAllCorrectPopped = ::handlePopBalloonsAllCorrectPopped,
                                handlePictureStartsWithPick = ::handlePictureStartsWithPick,
                                handleImageToWordReplayCorrectChoice = ::handleImageToWordReplayCorrectChoice,
                                handleImageToWordWordPressed = ::handleImageToWordWordPressed,
                                handleImageToWordAttempt = ::handleImageToWordAttempt,
                                handleImageMatchAttempt = ::handleImageMatchAttempt,
                                handleFinaleWrongPlacement = ::handleFinaleWrongPlacement,
                                onWrongFeedback = { wrongPickedLetter, wrongWordCatalogId, wrongPickedLetterAlreadySpoken, wrongWordAlreadySpoken ->
                                    onWrongFeedback(
                                        wrongPickedLetter = wrongPickedLetter,
                                        wrongWordCatalogId = wrongWordCatalogId,
                                        wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                        wrongWordAlreadySpoken = wrongWordAlreadySpoken,
                                    )
                                },
                                advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            ),
                    )
                }
            }
            val dinoDrawable =
                when (gameViewModel.dinoVisual) {
                    DinoVisual.Idle -> R.drawable.dino_idle
                    DinoVisual.TryAgain -> R.drawable.dino_try_again
                    DinoVisual.Jump -> jumpFrames[gameViewModel.jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
                }
            // Same in-round dino mouth animation for all chapters that use this screen (road-specific walks live in Journey).
            val talkFrames =
                listOf(R.drawable.dino_talk_0, R.drawable.dino_talk_1, R.drawable.dino_talk_2, R.drawable.dino_talk_3)
            if (!isCompactLandscapePhone) {
                GameScreenDinoLayer(
                    idleRes = dinoDrawable,
                    talkFrameResIds = talkFrames,
                    isTalking =
                        gameViewModel.dinoTalking ||
                            (isSagaEpisode(chapterId) && gameViewModel.inputLocked && gameViewModel.dinoVisual == DinoVisual.Jump),
                    dinoForward = dinoForward,
                    dinoSlip = dinoSlip,
                    dinoTilt = dinoTilt,
                    dinoScale = dinoScale,
                )
            }
        }
        GameOverlayLayer(
            chapterId = chapterId,
            stationId = stationId,
            episode4HelpSt15 = helpColumnEnabled,
            popBalloonsHelpControlsEnabled = popBalloonsHelpControlsEnabled,
            phase = gameViewModel.phase,
            inputLocked = gameViewModel.inputLocked,
            audioEnabled = audioEnabled,
            stationUiSpec = stationUiSpec,
            episode4HelpLocksChoices = gameViewModel.episode4HelpLocksChoices,
            balloonHelpLocksChoices = gameViewModel.balloonHelpLocksChoices,
            performSideHelpReplay = performSideHelpReplay,
            performSideHelpHint = performSideHelpHint,
            session = session,
            scope = scope,
            voice = voice,
            cancelFeedbackVoice = { cancelFeedbackVoice() },
            audioRuntime = audioRuntime,
        )
    }
}

