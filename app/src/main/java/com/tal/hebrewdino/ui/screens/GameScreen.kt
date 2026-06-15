package com.tal.hebrewdino.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.InStationPraiseAudio
import com.tal.hebrewdino.ui.audio.GameAudioEngine
import com.tal.hebrewdino.ui.audio.LocalBackgroundMusic
import com.tal.hebrewdino.ui.audio.withVoiceDuck
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Season2Chapter1LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Season2Chapter2LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Season2Ch1QaPolicy
import com.tal.hebrewdino.ui.domain.Season2GuessingCoach
import com.tal.hebrewdino.ui.domain.Season2GuessingDetector
import com.tal.hebrewdino.ui.domain.Season2GuessingHintCopy
import com.tal.hebrewdino.ui.companion.playAddressAwareTryAgainBlocking
import com.tal.hebrewdino.ui.domain.Season2AdvancedStationMode
import com.tal.hebrewdino.ui.domain.Season2EarlyStationQaPolicy
import com.tal.hebrewdino.ui.domain.Season2Station6FeedbackPolicy
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.companion.CompanionVisualPolicy
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal enum class GamePhase { Intro, Play }

internal enum class DinoVisual { Idle, TryAgain, Jump }

private const val IntroDurationMs = 450L

internal const val BetweenQuestionFadeMs = 80

/** Ch3/Ch6 station 1: keep pinned letter + picture visible before round transition (~0.5 s). */
internal const val Chapter3Or6Station1SuccessHoldMs = 480L

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
        val usesRawLetterNames =
            chapterId == 1 ||
                chapterId == 2 ||
                chapterId == 3 ||
                chapterId == 4 ||
                chapterId == 5 ||
                chapterId == 6 ||
                chapterId == TrainingV1Config.CHAPTER_ID
        val poolLetters = letterPoolSpec.groups.flatten().distinct()
        if (chapterId == 4) {
            voice.warmUp(AudioClips.VoBachorEtHaot)
        }
        val perLetterPaths =
            poolLetters.flatMap { letter ->
                listOfNotNull(
                    AudioClips.station1WrongCombined(letter),
                    if (usesRawLetterNames) null else AudioClips.letterNameClip(letter),
                )
            }
        val station1IntroExtras =
            if (chapterId == 4) {
                arrayOf(AudioClips.VoBachorEtHaot)
            } else {
                emptyArray()
            }
        sfx.preload(
            *station1IntroExtras,
            *perLetterPaths.toTypedArray(),
        )
    }

    suspend fun preloadPopBalloons(
        audioEnabled: Boolean,
        usesPopBalloonsSoundPoolPrompt: Boolean,
        chapterId: Int,
        letterPoolSpec: LetterPoolSpec,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && usesPopBalloonsSoundPoolPrompt)) return
        val usesRawLetterNames =
            chapterId == 1 ||
                chapterId == 2 ||
                chapterId == 3 ||
                chapterId == 4 ||
                chapterId == 5 ||
                chapterId == 6 ||
                chapterId == TrainingV1Config.CHAPTER_ID
        val letters = letterPoolSpec.groups.flatten().distinct()
        val paths = ArrayList<String>()
        paths.add(AudioClips.PopAllBalloonsWithLetter)
        paths.add(AudioClips.SfxBalloonPopSoft)
        paths.add(AudioClips.SfxBalloonPopWrongFunny)
        paths.add(AudioClips.SfxBalloonPop)
        paths.add(AudioClips.SfxStation2PopSoft1)
        paths.add(AudioClips.SfxStation2PopSoft2)
        paths.add(AudioClips.SfxStation2PopPlop)
        paths.add(AudioClips.SfxStation2PopFinale)
        for (letter in letters) {
            if (!usesRawLetterNames) {
                AudioClips.letterNameClip(letter)?.let(paths::add)
            }
            AudioClips.wrongSentenceClip(letter)?.let(paths::add)
            AudioClips.station1WrongCombined(letter)?.let(paths::add)
        }
        sfx.preload(*paths.distinct().toTypedArray())
    }

    suspend fun preloadFindGrid(
        audioEnabled: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && sagaUsesFindGridAudioStaging)) return
        val paths = ArrayList<String>()
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
            chapterId = chapterId,
            letterPoolSpec = letterPoolSpec,
            sfx = sfx,
        )
    }

    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadFindGrid(
            audioEnabled = audioEnabled,
            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
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
    chapter1CompanionCharacter: DinoCharacter? = null,
    chapter1PlayerAddress: PlayerAddress? = null,
    /** Season 2 Chapter 1 UX station index (1..6); enables anti-guessing coach when set. */
    season2Chapter1StationId: Int? = null,
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
    val isSeason2QuizChapter =
        season2Chapter1StationId != null &&
            (
                letterPoolSpec === Season2Chapter1LetterPoolSpec ||
                    letterPoolSpec === Season2Chapter2LetterPoolSpec ||
                    letterPoolSpec is com.tal.hebrewdino.ui.domain.Season2ChapterLetterPool
            )
    val companionCoachEnabled =
        com.tal.hebrewdino.ui.domain.CompanionCoachPolicy.isEnabled(
            chapterId = chapterId,
            companion = chapter1CompanionCharacter,
            playerAddress = chapter1PlayerAddress,
        )
    val qaUxStationId =
        com.tal.hebrewdino.ui.domain.SixStationArcQaPolicy.resolveUxStationIdForQa(
            chapterId = chapterId,
            stationId = stationId,
            season2UxStationId = season2Chapter1StationId,
        )
    val coachUxStationId =
        qaUxStationId
            ?: com.tal.hebrewdino.ui.domain.CompanionCoachPolicy.uxStationId(
                season2UxStationId = season2Chapter1StationId,
                stationId = stationId,
            )
    val season2GuessingDetector =
        remember(companionCoachEnabled) {
            if (companionCoachEnabled) {
                Season2GuessingDetector()
            } else {
                null
            }
        }
    var season2HintText by remember(stationId) { mutableStateOf<String?>(null) }
    var season2HadCoachIntervention by remember(stationId) { mutableStateOf(false) }
    val isSeason2BalloonStation =
        companionCoachEnabled && plan.mode == StationQuizMode.PopBalloons
    var season2SkipBalloonStagingAwait by remember(stationId) { mutableStateOf(false) }
    var season2Station6ConsecutiveWrongs by remember(stationId) { mutableIntStateOf(0) }
    var lastSeason2FocusRawResId by remember(stationId) { mutableIntStateOf(0) }
    var lastSeason2BalloonPraiseRawResId by remember(stationId) { mutableIntStateOf(0) }
    var lastSeason2PostFocusPraiseRawResId by remember(stationId) { mutableIntStateOf(0) }
    val recordCompanionPraisePlayed: (Int) -> Unit = { resId ->
        lastSeason2PostFocusPraiseRawResId = resId
        season2HadCoachIntervention = false
    }
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

    if (chapterId == TrainingV1Config.CHAPTER_ID) {
        topChromeProgressOverride?.first
    } else {
        null
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val audio = remember { GameAudioEngine(context = context) }
    val voice = audio.voice
    val sfx = audio.sfx
    val rawVoice = remember { RawVoicePlayer(context = context) }
    val backgroundMusic = LocalBackgroundMusic.current
    val gameFeedback = remember(stationId, sfx, view) { GameFeedback(scope, sfx, view) }
    val devToolsEnabled = DevTools.enabled(context)

    val expectsSelectedCompanion = CompanionVisualPolicy.expectsSelectedCompanion(chapterId)
    if (expectsSelectedCompanion && chapter1CompanionCharacter == null) {
        val msg =
            "Missing selected companion for station gameplay. chapterId=$chapterId stationId=$stationId context=GameScreen chapter1CompanionCharacter=null"
        Log.e(
            "MissingContent",
            msg,
        )
        if (devToolsEnabled) throw IllegalStateException(msg)
    }

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
        rawVoice.stopNow()
    }

    fun debugSkipToReward() {
        if (!devToolsEnabled || gameViewModel.completionCallbackFired) return
        gameViewModel.completionCallbackFired = true
        gameViewModel.inputLocked = true
        scope.launch {
            if (audioEnabled) {
                GameAudioActions.finishStationVoiceBeforeReward(
                    audioRuntime = audioRuntime,
                    cancelFeedbackVoice = { cancelFeedbackVoice() },
                )
            } else {
                voice.stopNow()
                cancelFeedbackVoice()
            }
            val correct = session.totalQuestions.coerceAtLeast(1)
            onComplete(stationId, correct, session.mistakeCount)
        }
    }

    GameAudioLifecycleEffects(
        lifecycleOwner = lifecycleOwner,
        stationId = stationId,
        cancelFeedbackVoice = { cancelFeedbackVoice() },
        releaseAudio = {
            audio.release()
            rawVoice.release()
        },
    )

    val performSideHelpReplay: () -> Unit = {
        val wordPartsReplay =
            plan.season2AdvancedMode == Season2AdvancedStationMode.WordParts &&
                session.currentQuestion is Question.WordPartsQuestion
        if (!gameViewModel.inputLocked || wordPartsReplay) {
            if (wordPartsReplay) {
                Season2AdvancedStationActions.interruptWordPartsVoice(
                    gameViewModel = gameViewModel,
                    cancelFeedbackVoice = { cancelFeedbackVoice() },
                    rawVoice = rawVoice,
                )
            }
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
                rawVoice = rawVoice,
                cancelFeedbackVoice = { cancelFeedbackVoice() },
                audioRuntime = audioRuntime,
                scope = scope,
            )
        }
    }

    val performSideHelpHint: () -> Unit = {
        if (plan.season2AdvancedMode == Season2AdvancedStationMode.PictureToWord) {
            performSideHelpReplay()
        } else {
            if (
                plan.season2AdvancedMode == Season2AdvancedStationMode.WordParts &&
                    session.currentQuestion is Question.WordPartsQuestion
            ) {
                Season2AdvancedStationActions.interruptWordPartsVoice(
                    gameViewModel = gameViewModel,
                    cancelFeedbackVoice = { cancelFeedbackVoice() },
                    rawVoice = rawVoice,
                )
            }
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
    }
    val useSagaSelectedCompanionDino =
        expectsSelectedCompanion &&
            chapter1CompanionCharacter != null
    val chapter1CompanionAssets =
        remember(chapter1CompanionCharacter) {
            chapter1CompanionCharacter?.let { CompanionVisualPolicy.assetsFor(it) }
        }
    val companionTalkFrames = chapter1CompanionAssets?.talkFrameResIds.orEmpty()
    val jumpFrames = companionTalkFrames
    val forwardDir =
        if (isSeason2QuizChapter) {
            1f
        } else if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
            -1f
        } else {
            1f
        }

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
            prepareForRewardNavigation =
                if (audioEnabled) {
                    {
                        GameAudioActions.finishStationVoiceBeforeReward(
                            audioRuntime = audioRuntime,
                            cancelFeedbackVoice = { cancelFeedbackVoice() },
                        )
                    }
                } else {
                    null
                },
            modifier = modifier,
        )
        return
    }

    LaunchedEffect(stationId, session.currentIndex, companionCoachEnabled) {
        if (companionCoachEnabled) {
            season2GuessingDetector?.onNewRound()
            season2HadCoachIntervention = false
        }
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
            chapter1PlayerAddress = chapter1PlayerAddress,
            rawVoice = rawVoice,
            backgroundMusic = backgroundMusic,
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
        season2Station6ConsecutiveWrongs = 0
        var playedPostFocusCompanionPraise = false
        if (
            com.tal.hebrewdino.ui.domain.Season2PostFocusCorrectPolicy.shouldPlayCompanionPraiseOnCorrect(
                season2HadCoachIntervention = season2HadCoachIntervention,
            )
        ) {
            playedPostFocusCompanionPraise = true
            val companion = chapter1CompanionCharacter
            if (audioEnabled && companion != null) {
                lastSeason2PostFocusPraiseRawResId =
                    com.tal.hebrewdino.ui.audio.Season2PostFocusCorrectAudio.playBlocking(
                        companion = companion,
                        rawVoice = rawVoice,
                        backgroundMusic = backgroundMusic,
                        avoidRawResId = lastSeason2PostFocusPraiseRawResId,
                    )
            }
            season2HadCoachIntervention = false
        }
        season2GuessingDetector?.onCorrect()
        AdvanceAfterRoundActions.run(
            scope = scope,
            gameViewModel = gameViewModel,
            audioEnabled = audioEnabled,
            sagaEpisode = isSagaEpisode(chapterId),
            chapterId = chapterId,
            stationId = stationId,
            season2Chapter1UxStationId = qaUxStationId,
            isSeason2QuizChapter = isSeason2QuizChapter,
            suppressAdvanceRoundNarratorPraiseAfterPostFocusCompanion = playedPostFocusCompanionPraise,
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
            rawVoice = rawVoice,
            cancelFeedbackVoice = { cancelFeedbackVoice() },
            audioRuntime = audioRuntime,
            dinoForward = dinoForward,
            forwardDir = forwardDir,
            dinoScale = dinoScale,
            contentAlpha = contentAlpha,
            session = session,
            onComplete = onComplete,
            onLevelCompleteHook = { ChildGameAudioHooks.onLevelComplete() },
        )
    }

    fun onWrongFeedback(
        wrongPickedLetter: String? = null,
        wrongWordCatalogId: String? = null,
        wrongPickedLetterAlreadySpoken: Boolean = false,
        wrongWordAlreadySpoken: Boolean = false,
        skipTryAgainAudio: Boolean = false,
    ): Job? {
        val detector = season2GuessingDetector
        val keepPopBalloonsInputUnlocked =
            isSeason2BalloonStation &&
                Season2Ch1QaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(coachUxStationId)
        if (detector != null && companionCoachEnabled) {
            season2Station6ConsecutiveWrongs++
            if (
                Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                    consecutiveWrongInRound = season2Station6ConsecutiveWrongs,
                    companionCoachEnabled = companionCoachEnabled,
                )
            ) {
                season2Station6ConsecutiveWrongs = 0
                val companion = chapter1CompanionCharacter ?: DinoCharacter.Dino
                return scope.launch {
                    try {
                        cancelFeedbackVoice()
                        if (!keepPopBalloonsInputUnlocked) {
                            gameViewModel.inputLocked = true
                        }
                        if (audioEnabled) {
                            val focusRes =
                                com.tal.hebrewdino.ui.audio.Season2CompanionFeedbackAudio.pickFocusLine(
                                    companion = companion,
                                    avoidRawResId = lastSeason2FocusRawResId,
                                )
                            lastSeason2FocusRawResId = focusRes
                            if (backgroundMusic != null) {
                                backgroundMusic.withVoiceDuck {
                                    rawVoice.playRawBlocking(focusRes)
                                }
                            } else {
                                rawVoice.playRawBlocking(focusRes)
                            }
                        }
                        if (chapter1PlayerAddress != null) {
                            Season2GuessingCoach.replayTargetAudio(
                                uxStationId = coachUxStationId,
                                session = session,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                gameplayChapterId = chapterId,
                                chapterId = chapterId,
                                stationUiSpec = stationUiSpec,
                            )
                        }
                        season2HadCoachIntervention = true
                    } finally {
                        gameViewModel.dinoVisual = DinoVisual.Idle
                        gameViewModel.inputLocked = false
                    }
                }
            } else {
                return scope.launch {
                    try {
                        if (!keepPopBalloonsInputUnlocked) {
                            gameViewModel.inputLocked = true
                        }
                        if (
                            !skipTryAgainAudio &&
                                audioEnabled &&
                                chapter1PlayerAddress != null
                        ) {
                            playAddressAwareTryAgainBlocking(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                                context = "GameScreen.onWrongFeedback(coachTryAgain)",
                            )
                        } else if (audioEnabled && !skipTryAgainAudio) {
                            ChildGameAudioHooks.onWrong()
                        }
                        optionsShake.animateTo(7f, tween(70))
                        optionsShake.animateTo(0f, tween(140))
                    } finally {
                        gameViewModel.dinoVisual = DinoVisual.Idle
                        gameViewModel.inputLocked = false
                    }
                }
            }
        }
        return WrongFeedbackActions.trigger(
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
            chapter1PlayerAddress = chapter1PlayerAddress,
            rawVoice = rawVoice,
            skipTryAgainAudio = skipTryAgainAudio,
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

        val showSeason2CoachHint =
            companionCoachEnabled &&
                season2HintText != null &&
                chapter1CompanionCharacter != null
        val season2CompanionLayoutScale = if (companionCoachEnabled) 0.80f else 0.70f

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
                            chapterId = chapterId,
                            stationId = stationId,
                            tapped = tapped,
                            question = question,
                            scope = scope,
                            sfx = sfx,
                            rawVoice = rawVoice,
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
                            speakLetterNameOnGridTap = audioEnabled,
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
                        val willPlayCoachFocusAfterWrong =
                            companionCoachEnabled &&
                                season2Station6ConsecutiveWrongs + 1 >=
                                    Season2Station6FeedbackPolicy.CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
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
                            rawVoice = rawVoice,
                            audioRuntime = audioRuntime,
                            onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken, skipTryAgainAudio ->
                                onWrongFeedback(
                                    wrongPickedLetter = wrongPickedLetter,
                                    wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                    skipTryAgainAudio = skipTryAgainAudio,
                                )
                            },
                            advanceAfterRound = { isLast, ch3SpellMidWord ->
                                advanceAfterRound(isLast, ch3SpellMidWord = ch3SpellMidWord)
                            },
                            chapter1PlayerAddress = chapter1PlayerAddress,
                            willPlayCoachFocusAfterWrong = willPlayCoachFocusAfterWrong,
                            season2HadCoachIntervention = season2HadCoachIntervention,
                            companionCharacter = chapter1CompanionCharacter,
                            backgroundMusic = backgroundMusic,
                            postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                            onPostFocusPraisePlayed = { resId ->
                                lastSeason2PostFocusPraiseRawResId = resId
                                season2HadCoachIntervention = false
                            },
                        )
                    }

                    suspend fun handlePopBalloonsPopSfx(
                        letter: String,
                        isCorrect: Boolean,
                        finalCorrectBalloon: Boolean,
                        balloonIndex: Int,
                    ) {
                        val afterCoachIntervention =
                            isCorrect &&
                                isSeason2BalloonStation &&
                                season2HadCoachIntervention
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
                            chapterId = chapterId,
                            stationId = stationId,
                            chapter1PlayerAddress = chapter1PlayerAddress,
                            rawVoice = rawVoice,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioRuntime = audioRuntime,
                            nextStation2CorrectPopVariant = {
                                val v = gameViewModel.station2CorrectPopCount
                                gameViewModel.station2CorrectPopCount += 1
                                v
                            },
                            station2PopTailPaddingMs = Station2PopTailPaddingMs,
                            station2PopFallbackDurationMs = Station2PopFallbackDurationMs,
                            season2QuizBalloons = isSeason2BalloonStation,
                            afterCoachIntervention = afterCoachIntervention,
                            season2HadCoachIntervention = season2HadCoachIntervention,
                            companionCharacter = chapter1CompanionCharacter,
                            postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                            lastPraiseRawResId = lastSeason2BalloonPraiseRawResId,
                            backgroundMusic = backgroundMusic,
                            onPostFocusPraisePlayed = { resId ->
                                lastSeason2PostFocusPraiseRawResId = resId
                                season2HadCoachIntervention = false
                                season2SkipBalloonStagingAwait = true
                            },
                            onPraisePlayed = { resId ->
                                lastSeason2BalloonPraiseRawResId = resId
                            },
                        )
                    }

                    fun handlePopBalloonsWrongPick() {
                        if (companionCoachEnabled) {
                            if (!gameViewModel.consumeTapCooldown()) return
                            session.wrongTap()
                            gameViewModel.shakeEpoch += 1
                            scope.launch {
                                if (audioEnabled && isSeason2BalloonStation) {
                                    GameAudioActions.awaitFeedbackVoice(audioRuntime, 5000L)
                                }
                                val feedbackJob =
                                    onWrongFeedback(skipTryAgainAudio = isSeason2BalloonStation)
                                GameAudioActions.joinSilently(feedbackJob)
                            }
                            return
                        }
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
                        val skipStagingAwait = season2SkipBalloonStagingAwait
                        season2SkipBalloonStagingAwait = false
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
                            skipStagingVoiceAwait = skipStagingAwait,
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
                            isSeason2QuizChapter = isSeason2QuizChapter,
                            session = session,
                            scope = scope,
                            voice = voice,
                            rawVoice = rawVoice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken ->
                                onWrongFeedback(
                                    wrongPickedLetter = wrongPickedLetter,
                                    wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                )
                            },
                            season2HadCoachIntervention = season2HadCoachIntervention,
                            companionCharacter = chapter1CompanionCharacter,
                            backgroundMusic = backgroundMusic,
                            postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                            onCompanionPraisePlayed = recordCompanionPraisePlayed,
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
                            rawVoice = rawVoice,
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
                            rawVoice = rawVoice,
                            audioRuntime = audioRuntime,
                        )
                    }

                    fun handleImageToWordAttempt(choiceId: String): Boolean {
                        val willPlayCoachFocusAfterWrong =
                            companionCoachEnabled &&
                                season2Station6ConsecutiveWrongs + 1 >=
                                    Season2Station6FeedbackPolicy.CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
                        return ImageMatchActions.handleImageToWordAttempt(
                            choiceId = choiceId,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            stationId = stationId,
                            session = session,
                            scope = scope,
                            voice = voice,
                            rawVoice = rawVoice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongWordCatalogId, wrongWordAlreadySpoken, skipTryAgainAudio ->
                                onWrongFeedback(
                                    wrongWordCatalogId = wrongWordCatalogId,
                                    wrongWordAlreadySpoken = wrongWordAlreadySpoken,
                                    skipTryAgainAudio = skipTryAgainAudio,
                                )
                            },
                            season2HadCoachIntervention = season2HadCoachIntervention,
                            season2Chapter1UxStationId = season2Chapter1StationId,
                            chapter1PlayerAddress = chapter1PlayerAddress,
                            willPlayCoachFocusAfterWrong = willPlayCoachFocusAfterWrong,
                            companionCharacter = chapter1CompanionCharacter,
                            backgroundMusic = backgroundMusic,
                            postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                            onCompanionPraisePlayed = recordCompanionPraisePlayed,
                        )
                    }

                    fun handleAdvancedReplayWord() {
                        val q = session.currentQuestion ?: return
                        if (!audioEnabled) return
                        if (
                            q is Question.ImageMatchQuestion &&
                                Season2StationAudio.isPictureToWordStation(chapterId, stationId)
                        ) {
                            scope.launch {
                                Season2StationAudio.replayPictureToWordTargetWordOnly(
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    catalogId = q.correctChoiceId,
                                    rawVoice = rawVoice,
                                    voice = voice,
                                )
                            }
                            return
                        }
                        if (q is Question.WordPartsQuestion) {
                            Season2AdvancedStationActions.interruptWordPartsVoice(
                                gameViewModel = gameViewModel,
                                cancelFeedbackVoice = cancelFeedbackVoiceCb,
                                rawVoice = rawVoice,
                            )
                            scope.launch {
                                GameAudioActions.launchPromptVoice(
                                    audioEnabled = audioEnabled,
                                    scope = scope,
                                    audioRuntime = audioRuntime,
                                    cancelFeedbackVoice = cancelFeedbackVoiceCb,
                                ) {
                                    Season2StationAudio.replayAdvancedInstructionAndWord(
                                        q = q,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        rawVoice = rawVoice,
                                    )
                                }
                            }
                            return
                        }
                        val catalogId = Season2AdvancedStationActions.catalogIdForReplay(q) ?: return
                        cancelFeedbackVoice()
                        scope.launch {
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                Season2StationAudio.playWordByCatalogId(
                                    catalogId = catalogId,
                                    rawVoice = rawVoice,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    context = "GameScreen.handleAdvancedReplayWord",
                                )
                            }
                        }
                    }

                    fun handleWordPartsPictureTap() {
                        val q = session.currentQuestion as? Question.WordPartsQuestion ?: return
                        if (!audioEnabled) return
                        Season2AdvancedStationActions.interruptWordPartsVoice(
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            rawVoice = rawVoice,
                        )
                        scope.launch {
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                com.tal.hebrewdino.ui.audio.Season2WordPartsAudio.playPictureTapSequence(
                                    catalogId = q.catalogEntryId,
                                    rawVoice = rawVoice,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                        }
                    }

                    fun handleWordPartsHintRevealAudio() {
                        val q = session.currentQuestion as? Question.WordPartsQuestion ?: return
                        if (!audioEnabled) return
                        Season2AdvancedStationActions.interruptWordPartsVoice(
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            rawVoice = rawVoice,
                        )
                        scope.launch {
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                com.tal.hebrewdino.ui.audio.Season2WordPartsAudio.playHintRevealSequence(
                                    catalogId = q.catalogEntryId,
                                    rawVoice = rawVoice,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                        }
                    }

                    fun handleMissingFirstLetterPick(picked: String) {
                        Season2AdvancedStationActions.handleMissingFirstLetterPick(
                            picked = picked,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            stationId = stationId,
                            session = session,
                            scope = scope,
                            rawVoice = rawVoice,
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

                    fun handleWordPartsPick(picked: Question.WordPartsSplitOption) {
                        Season2AdvancedStationActions.handleWordPartsPick(
                            picked = picked,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            companionCoachEnabled = companionCoachEnabled,
                            season2UxStationId = season2Chapter1StationId,
                            season2AdvancedMode = plan.season2AdvancedMode,
                            consecutiveWrongsInRound = season2Station6ConsecutiveWrongs,
                            chapterId = chapterId,
                            stationId = stationId,
                            session = session,
                            scope = scope,
                            voice = voice,
                            rawVoice = rawVoice,
                            chapter1PlayerAddress = chapter1PlayerAddress,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { _ ->
                                onWrongFeedback(skipTryAgainAudio = true)
                            },
                            season2HadCoachIntervention = season2HadCoachIntervention,
                            companionCharacter = chapter1CompanionCharacter,
                            backgroundMusic = backgroundMusic,
                            postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                            onCompanionPraisePlayed = recordCompanionPraisePlayed,
                        )
                    }

                    fun handleRhymingPick(choiceId: String) {
                        Season2AdvancedStationActions.handleRhymingPick(
                            choiceId = choiceId,
                            gameViewModel = gameViewModel,
                            cancelFeedbackVoice = cancelFeedbackVoiceCb,
                            audioEnabled = audioEnabled,
                            session = session,
                            scope = scope,
                            chapterId = chapterId,
                            stationId = stationId,
                            rawVoice = rawVoice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongWordCatalogId ->
                                onWrongFeedback(
                                    wrongWordCatalogId = wrongWordCatalogId,
                                    wrongWordAlreadySpoken = true,
                                )
                            },
                        )
                    }

                    fun handleDragWordToPictureDrop(
                        wordCatalogId: String,
                        pictureCatalogId: String,
                    ): Boolean {
                        val accepted =
                            DragWordToPictureActions.handleDropAttempt(
                                wordCatalogId = wordCatalogId,
                                pictureCatalogId = pictureCatalogId,
                                gameViewModel = gameViewModel,
                                cancelFeedbackVoice = cancelFeedbackVoiceCb,
                                audioEnabled = audioEnabled,
                                sfx = sfx,
                                session = session,
                                scope = scope,
                                chapterId = chapterId,
                                stationId = stationId,
                                rawVoice = rawVoice,
                                audioRuntime = audioRuntime,
                                onWrongFeedback = { onWrongFeedback() },
                            )
                        if (!accepted) {
                            gameViewModel.shakeEpoch += 1
                        }
                        return accepted
                    }

                    fun handleDragWordToPictureRoundComplete() {
                        DragWordToPictureActions.handleRoundComplete(
                            gameViewModel = gameViewModel,
                            audioEnabled = audioEnabled,
                            session = session,
                            scope = scope,
                            chapterId = chapterId,
                            stationId = stationId,
                            rawVoice = rawVoice,
                            sfx = sfx,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                        )
                    }

                    fun handleDragWordToPicturePictureTap(catalogEntryId: String) {
                        if (!audioEnabled) return
                        if (
                            !com.tal.hebrewdino.ui.domain.Season1StationAudio
                                .isDragWordToPictureBehaviorStation(chapterId, stationId)
                        ) {
                            Season2AdvancedStationActions.replayWordByCatalogId(
                                catalogId = catalogEntryId,
                                chapterId = chapterId,
                                stationId = stationId,
                                rawVoice = rawVoice,
                                scope = scope,
                                audioRuntime = audioRuntime,
                                audioEnabled = audioEnabled,
                            )
                            return
                        }
                        cancelFeedbackVoice()
                        scope.launch {
                            com.tal.hebrewdino.ui.domain.Season1StationAudio.playDragWordToPictureWord(
                                rawVoice = rawVoice,
                                catalogEntryId = catalogEntryId,
                                chapterId = chapterId,
                                stationId = stationId,
                                context = "GameScreen.handleDragWordToPicturePictureTap",
                            )
                        }
                    }

                    fun handleDragMissingLetterPlace(letter: String): Boolean {
                        val accepted =
                            DragMissingLetterActions.handleLetterPlaced(
                                letter = letter,
                                gameViewModel = gameViewModel,
                                cancelFeedbackVoice = cancelFeedbackVoiceCb,
                                audioEnabled = audioEnabled,
                                sfx = sfx,
                                session = session,
                                scope = scope,
                                chapterId = chapterId,
                                stationId = stationId,
                                rawVoice = rawVoice,
                                onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken ->
                                    onWrongFeedback(
                                        wrongPickedLetter = wrongPickedLetter,
                                        wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                    )
                                },
                                advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            )
                        if (!accepted) {
                            gameViewModel.shakeEpoch += 1
                        }
                        return accepted
                    }

                    fun handleDragMissingLetterPictureTap() {
                        val q = session.currentQuestion as? Question.DragMissingLetterQuestion ?: return
                        if (!audioEnabled) return
                        scope.launch {
                            if (
                                com.tal.hebrewdino.ui.domain.Season1StationAudio
                                    .isDragMissingLetterBehaviorStation(chapterId, stationId)
                            ) {
                                com.tal.hebrewdino.ui.domain.Season1StationAudio.playDragMissingLetterWord(
                                    rawVoice = rawVoice,
                                    catalogEntryId = q.catalogEntryId,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    context = "GameScreen.handleDragMissingLetterPictureTap",
                                )
                            } else {
                                Season2AdvancedStationActions.replayWordByCatalogId(
                                    catalogId = q.catalogEntryId,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    rawVoice = rawVoice,
                                    scope = scope,
                                    audioRuntime = audioRuntime,
                                    audioEnabled = audioEnabled,
                                )
                            }
                        }
                    }

                    fun handleImageMatchAttempt(choiceId: String): Boolean {
                        val willPlayCoachFocusAfterWrong =
                            companionCoachEnabled &&
                                season2Station6ConsecutiveWrongs + 1 >=
                                    Season2Station6FeedbackPolicy.CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
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
                            rawVoice = rawVoice,
                            audioRuntime = audioRuntime,
                            advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            onWrongFeedback = { wrongWordCatalogId, wrongWordAlreadySpoken, skipTryAgainAudio ->
                                if (wrongWordCatalogId == null && !wrongWordAlreadySpoken) {
                                    onWrongFeedback()
                                } else {
                                    onWrongFeedback(
                                        wrongWordCatalogId = wrongWordCatalogId,
                                        wrongWordAlreadySpoken = wrongWordAlreadySpoken,
                                        skipTryAgainAudio = skipTryAgainAudio,
                                    )
                                }
                            },
                            season2HadCoachIntervention = season2HadCoachIntervention,
                            season2Chapter1UxStationId = qaUxStationId,
                            chapter1PlayerAddress = chapter1PlayerAddress,
                            willPlayCoachFocusAfterWrong = willPlayCoachFocusAfterWrong,
                            companionCharacter = chapter1CompanionCharacter,
                            backgroundMusic = backgroundMusic,
                            postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                            onCompanionPraisePlayed = recordCompanionPraisePlayed,
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
                                chapter1PlayerAddress = chapter1PlayerAddress,
                                season2Chapter1UxStationId = qaUxStationId,
                                season2HadCoachIntervention = season2HadCoachIntervention,
                                companionCharacter = chapter1CompanionCharacter,
                                backgroundMusic = backgroundMusic,
                                postFocusAvoidPraiseRawResId = lastSeason2PostFocusPraiseRawResId,
                                onCompanionPraisePlayed = recordCompanionPraisePlayed,
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
                                wordPartsCompletedEquation = gameViewModel.wordPartsCompletedEquation,
                                wordPartsHintRevealWord = gameViewModel.wordPartsHintRevealWord,
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
                                rawVoice = rawVoice,
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
                                handleMissingFirstLetterPick = ::handleMissingFirstLetterPick,
                                handleWordPartsPick = ::handleWordPartsPick,
                                handleRhymingPick = ::handleRhymingPick,
                                handleDragWordToPictureDrop = ::handleDragWordToPictureDrop,
                                handleDragWordToPictureRoundComplete = ::handleDragWordToPictureRoundComplete,
                                handleDragWordToPicturePictureTap = ::handleDragWordToPicturePictureTap,
                                handleDragMissingLetterPlace = ::handleDragMissingLetterPlace,
                                handleDragMissingLetterPictureTap = ::handleDragMissingLetterPictureTap,
                                handleAdvancedReplayWord = ::handleAdvancedReplayWord,
                                handleWordPartsPictureTap = ::handleWordPartsPictureTap,
                                handleWordPartsHintRevealAudio = ::handleWordPartsHintRevealAudio,
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
            if (
                !isCompactLandscapePhone &&
                    !companionCoachEnabled &&
                    useSagaSelectedCompanionDino &&
                    chapter1CompanionAssets != null &&
                    jumpFrames.isNotEmpty()
            ) {
                val assets = chapter1CompanionAssets
                val dinoDrawable =
                    when (gameViewModel.dinoVisual) {
                        DinoVisual.Idle -> assets.poseIdle
                        DinoVisual.TryAgain -> assets.poseEncourage
                        DinoVisual.Jump ->
                            jumpFrames[gameViewModel.jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
                    }
                GameScreenDinoLayer(
                    idleRes = dinoDrawable,
                    talkFrameResIds = companionTalkFrames,
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
            rawVoice = rawVoice,
            cancelFeedbackVoice = { cancelFeedbackVoice() },
            audioRuntime = audioRuntime,
            chapter1PlayerAddress = chapter1PlayerAddress,
            showHintButton =
                !Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                    gameplayChapterId = chapterId,
                    season2UxStationId = season2Chapter1StationId,
                ),
        )

        if ((isCompactLandscapePhone || companionCoachEnabled) &&
            useSagaSelectedCompanionDino &&
            chapter1CompanionAssets != null &&
            jumpFrames.isNotEmpty()
        ) {
            val assets = chapter1CompanionAssets
            val s2DinoDrawable =
                when (gameViewModel.dinoVisual) {
                    DinoVisual.Idle -> assets.poseIdle
                    DinoVisual.TryAgain -> assets.poseEncourage
                    DinoVisual.Jump ->
                        jumpFrames[gameViewModel.jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
                }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .zIndex(20f),
                ) {
                    GameScreenDinoLayer(
                        idleRes = s2DinoDrawable,
                        talkFrameResIds = companionTalkFrames,
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
                                .scale(season2CompanionLayoutScale),
                    )
                    if (showSeason2CoachHint) {
                        Season2CompanionSpeechHint(
                            text = season2HintText!!,
                            companionCharacter = chapter1CompanionCharacter,
                            isTalking = true,
                            showCompanionPortrait = false,
                            companionSizeDp = 70.dp,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 76.dp, bottom = 10.dp)
                                    .zIndex(21f),
                        )
                    }
                }
            }
        }

        StationDebugSkipButton(
            onSkip = { debugSkipToReward() },
            enabled = !gameViewModel.completionCallbackFired,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp)
                    .zIndex(6f),
        )
    }
}

