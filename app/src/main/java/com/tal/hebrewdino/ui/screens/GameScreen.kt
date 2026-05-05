package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.components.Episode4Stations15HelpColumn
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.components.station.FindLetterGridStationContent
import com.tal.hebrewdino.ui.game.FinaleGame
import com.tal.hebrewdino.ui.components.station.Chapter3SagaPopBalloonsWordBanner
import com.tal.hebrewdino.ui.components.station.Chapter3Station6ImageToWordStationContent
import com.tal.hebrewdino.ui.components.station.MatchLetterToWordStationContent
import com.tal.hebrewdino.ui.components.station.PickLetterStationContent
import com.tal.hebrewdino.ui.components.station.PopBalloonsInstructionHeaderBlock
import com.tal.hebrewdino.ui.components.station.PopBalloonsStationContent
import com.tal.hebrewdino.ui.components.station.PictureStartsWithStationContent
import com.tal.hebrewdino.ui.components.station.SagaImageMatchGameStationContent
import androidx.compose.ui.platform.LocalContext
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

/** ~1 cm vertical gap between the back button and the collected-egg strip. */
private val SpaceBelowBackBeforeEggs = 38.dp

/** Chapters that use the shared six-station journey ([Chapter1StationOrder]); intros, art, and letter pools differ. */
private val SixStationArcChapterRange = 1..5

private fun isSagaEpisode(chapterId: Int): Boolean = chapterId in SixStationArcChapterRange

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
private const val Episode1PraiseChance = 0.62f

/** Episode 4 stations 1–5 help "רמז": show target / lock choices for this long, then hide. */
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
    val listenOnly = plan.listenOnlyTargetPrompt
    val stationUiSpec = remember(chapterId, stationId) { StationBehaviorRegistry.getStationUiSpec(chapterId, stationId) }
    val episode4HelpSt15 = Episode4Help.isHelpColumnActive(chapterId, stationUiSpec)
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
    val episode4Help = rememberEpisode4HelpController(stationId = stationId, scope = scope)
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
    var station4PinnedCorrectLetter by remember(stationId, session.currentIndex) { mutableStateOf<String?>(null) }
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
    val gameChoicesEnabled = !inputLocked && !episode4Help.hintLocksChoices

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

    fun performEpisode4HelpReplay() {
        if (!audioEnabled || !episode4HelpSt15 || phase != GamePhase.Play) return
        val q = session.currentQuestion ?: return
        cancelFeedbackVoice()
        sfx.stopAllStreams()
        if (sagaUsesPickLetterAudioStaging) {
            sfx.stopStream(station1VoiceStreamId)
            station1VoiceStreamId = 0
        }
        if (sagaUsesPopBalloonsAudioStaging) {
            sfx.stopStream(station2VoiceStreamId)
            station2VoiceStreamId = 0
        }
        if (sagaUsesFindGridAudioStaging) {
            sfx.stopStream(station3VoiceStreamId)
            station3VoiceStreamId = 0
        }
        feedbackVoiceJob =
            scope.launch {
                replayEpisode4Stations15RoundAudio(sfx = sfx, voice = voice, stationId = stationId, q = q)
            }
    }

    fun performEpisode4HelpHint() {
        val q = session.currentQuestion ?: return
        val letter = Episode4Help.targetLetterForHelpHint(q)
        episode4Help.performHint(
            isHelpEnabled = episode4HelpSt15,
            isPlayPhase = phase == GamePhase.Play,
            letter = letter,
            stationId = stationId,
            hintDurationMs = stationUiSpec.hintDurationMs,
        )
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
        station4PinnedCorrectLetter = null
        episode4Help.resetForNewQuestion()
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
                                    // UX: say the word first, then the letter (matches on-screen context).
                                    add(AudioClips.wordClipByCatalogId(round.catalogId))
                                    AudioClips.letterNameClip(round.correctLetter)?.let { add(it) }
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
                                val intro =
                                    if ((chapterId == 4 || chapterId == 5) && stationId == Chapter1StationOrder.TAP_LETTER) {
                                        if (voice.hasAsset(AudioClips.VoBachorEtHaot)) {
                                            AudioClips.VoBachorEtHaot
                                        } else {
                                            AudioClips.VoChooseLetter
                                        }
                                    } else {
                                        AudioClips.VoChooseLetter
                                    }
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
                            playSagaFindGridIntroSoundPool(sfx, voice, q, chapterId, StationIntroLetterLeadFraction)
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
                                val (_, catalogId) =
                                    session.chapter3PopAllLettersCurrentWord()
                                        ?: error("Missing Chapter 3 balloons word for index ${session.currentIndex}")
                                val wordPath = AudioClips.wordClipByCatalogId(catalogId)
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
                            speakPromptForQuestion(
                                voice,
                                sfx,
                                stationId = stationId,
                                chapterId = chapterId,
                                listenOnlyTargetPrompt = listenOnly,
                                q = q,
                            )
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
                // Episode 3 station 5 already plays praise on correct pick; avoid double praise between rounds.
                !isChapter3AudioLetterRecognitionStation &&
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
            playShake(
                scope,
                optionsShake,
                baseShakeAmplitudePx = if (isSagaEpisode(chapterId)) 20f else 18f,
                strength = if (strongerWrongShake) 1.25f else 1f,
            )
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
                        val feedbackDelayMs =
                            when {
                                // Episode 4 feedback: station 1 and station 4 should feel near-instant on tap.
                                chapterId == 4 && stationId == Chapter1StationOrder.TAP_LETTER -> 12L
                                chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE -> 12L
                                // Episode 5 feedback: station 4 wrong response should feel near-instant.
                                chapterId == 5 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE -> 12L
                                else -> 110L
                            }
                        delay(feedbackDelayMs)
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
        GameScreenBackgroundLayer(
            chapterId = chapterId,
            backgroundRes = backgroundRes,
        )

        // Station screens: keep the top bar fixed (like Journey). Do not show collected eggs/letters/debug inside stations.
        val contentTopInset = 40.dp
        GameScreenTopChrome(
            onBack = onBack,
            questionNumber = session.questionNumber,
            totalQuestions = session.totalQuestions,
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
        )

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
                            is Question.FindLetterGridQuestion -> {
                                val isSagaRevealStation =
                                    isSagaEpisode(chapterId) &&
                                        stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE
                                FindLetterGridStationContent(
                                    question = current,
                                    modifier = Modifier.fillMaxSize(),
                                    chapterId = chapterId,
                                    listenOnly = listenOnly,
                                    isSagaRevealStation = isSagaRevealStation,
                                    sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                                    stationUiSpec = stationUiSpec,
                                    chapter3ContextWordHint =
                                        if (chapterId == 3 && sagaUsesFindGridAudioStaging) {
                                            Chapter3EpisodeContent.gridHintWord(session.currentIndex)
                                        } else {
                                            null
                                        },
                                    floatingTargetLetterHint =
                                        if (episode4HelpSt15 &&
                                            stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE
                                        ) {
                                            episode4Help.activeHintLetter
                                        } else {
                                            null
                                        },
                                    episode4TargetCellsHintEpoch =
                                        if (episode4HelpSt15 &&
                                            stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE
                                        ) {
                                            episode4Help.station3GridHintEpoch
                                        } else {
                                            0
                                        },
                                    hintPulseEpoch = hintPulseEpoch,
                                    enabled = gameChoicesEnabled,
                                    contentKey = session.currentIndex,
                                    entryPulseScale = entryPulseScale.value,
                                    optionsShakePx = optionsShake.value,
                                    onSagaGridLetterTapped =
                                        if (sagaUsesFindGridAudioStaging) {
                                            sagaLetterTap@{ tapped ->
                                                if (!audioEnabled) return@sagaLetterTap
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
                                    onCellTapped = gridCellTap@{ index ->
                                        if (!consumeTapCooldown()) return@gridCellTap
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
                                    onCompleted = gridComplete@{
                                        if (!consumeTapCooldown()) return@gridComplete
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
                                )
                            }
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
                                        Chapter3SagaPopBalloonsWordBanner(
                                            popAllLettersWord =
                                                session.chapter3PopAllLettersCurrentWord()?.first.orEmpty(),
                                        )
                                    }
                                    if (plan.mode != StationQuizMode.PickLetter) {
                                        PopBalloonsInstructionHeaderBlock(
                                            chapterId = chapterId,
                                            stationId = stationId,
                                            listenOnly = listenOnly,
                                            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                                            isSagaEpisode = isSagaEpisode(chapterId),
                                            balloonInstructionOverride = stationUiSpec.balloonInstructionOverride,
                                            useEpisode4BalloonInstructionPanel = stationUiSpec.useEpisode4BalloonInstructionPanel,
                                            episode4HelpSt15 = episode4HelpSt15,
                                            episode4HelpActiveHintLetter = episode4Help.activeHintLetter,
                                            hintHeaderScale = hintHeaderScale.value,
                                            correctAnswer = current.correctAnswer,
                                            station2PinnedBalloonLetter = station2PinnedBalloonLetter,
                                            station2PinnedBalloonColor = station2PinnedBalloonColor,
                                        )
                                    }
                                    if (plan.mode == StationQuizMode.PickLetter) {
                                        val pickLetterOptions =
                                            if (sagaUsesPickLetterAudioStaging &&
                                                station1PinnedCorrectLetter != null &&
                                                !isChapter3HighlightedLetterInWordStation &&
                                                !isChapter3AudioLetterRecognitionStation &&
                                                stationUiSpec.pickLetterAllowPinnedCorrectShortcut
                                            ) {
                                                listOf(station1PinnedCorrectLetter!!)
                                            } else {
                                                if (isChapter3HighlightedLetterInWordStation) {
                                                    current.options.sorted()
                                                } else {
                                                    current.options
                                                }
                                            }
                                        val letterOptionsExtraTopPaddingDp =
                                            when {
                                                isChapter3AudioLetterRecognitionStation -> 6.dp
                                                isChapter3HighlightedLetterInWordStation -> SixStationArcHalfCmNudge
                                                else -> 0.dp
                                            }
                                        val pickLetterBoxTopPaddingDp =
                                            if (sagaUsesPickLetterAudioStaging) SixStationArcHalfCmNudge else 0.dp
                                        val pickLetterDisplayOptions =
                                            if (isChapter3AudioLetterRecognitionStation && correctTapPulseLetter != null) {
                                                listOf(correctTapPulseLetter!!)
                                            } else {
                                                pickLetterOptions
                                            }
                                        PickLetterStationContent(
                                            question = current,
                                            sessionRoundIndex = session.currentIndex,
                                            isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                                            isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                                            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                                            station1PinnedCorrectLetter = station1PinnedCorrectLetter,
                                            pickLetterInstructionOverride = stationUiSpec.pickLetterInstructionOverride,
                                            pickLetterListenOnlyHebrewPanel = stationUiSpec.pickLetterListenOnlyHebrewPanel,
                                            pickLetterAllowPinnedCorrectShortcut = stationUiSpec.pickLetterAllowPinnedCorrectShortcut,
                                            isSagaEpisode = isSagaEpisode(chapterId),
                                            chapterId = chapterId,
                                            stationId = stationId,
                                            boxTopPaddingDp = pickLetterBoxTopPaddingDp,
                                            letterOptionsExtraTopPaddingDp = letterOptionsExtraTopPaddingDp,
                                            enabled = gameChoicesEnabled,
                                            shakePx = optionsShake.value,
                                            correctPulseLetter =
                                                correctTapPulseLetter
                                                    ?: current.correctAnswer.takeIf { wrongTapsThisQuestion >= 2 },
                                            correctPulseEpoch = hintPulseEpoch + correctTapPulseEpoch,
                                            letterOptions = pickLetterDisplayOptions,
                                            strongLetterButtonFeedback = isChapter3AudioLetterRecognitionStation,
                                            onRepeatLetterClick = repeatLetter@{
                                                if (!audioEnabled) return@repeatLetter
                                                cancelFeedbackVoice()
                                                val letter = current.correctAnswer
                                                val clip = AudioClips.letterNameClip(letter) ?: return@repeatLetter
                                                feedbackVoiceJob =
                                                    scope.launch {
                                                        voice.playBlocking(clip)
                                                    }
                                            },
                                            onPick = pickLetterPick@{ picked ->
                                                // UX: any tap should immediately cancel currently playing voice,
                                                // even if we ignore the tap due to cooldown.
                                                cancelFeedbackVoice()
                                                if (!consumeTapCooldown()) return@pickLetterPick
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
                                                                val letterName = AudioClips.letterNameClip(picked)
                                                                if (letterName != null && voice.hasAsset(letterName)) {
                                                                    voice.playBlocking(letterName)
                                                                }
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
                                                        if (audioEnabled && isChapter3AudioLetterRecognitionStation) {
                                                            scope.launch {
                                                                cancelFeedbackVoice()
                                                                val clip = AudioClips.letterNameClip(picked) ?: return@launch
                                                                voice.playBlocking(clip)
                                                                val tryAgain =
                                                                    mutableListOf(
                                                                        AudioClips.VoTryAgain2,
                                                                        AudioClips.VoTryAgain1,
                                                                    )
                                                                tryAgain.shuffle()
                                                                voice.playFirstAvailableBlocking(*tryAgain.toTypedArray())
                                                            }
                                                        }
                                                        shakeEpoch += 1
                                                        wrongTapsThisQuestion += 1
                                                        if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                        onWrongFeedback(wrongPickedLetter = picked)
                                                    }
                                                    AnswerResult.Finished -> {}
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                                        )
                                    } else {
                                        PopBalloonsStationContent(
                                            question = current,
                                            correctLetterSet =
                                                if (isChapter3PopAllLettersStation) {
                                                    val w = session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                                                    w.toCharArray().map { it.toString() }.toSet()
                                                } else {
                                                    null
                                                },
                                            enabled = gameChoicesEnabled,
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
                                            onPopSfx = popBalloonSfx@{ letter, isCorrect, finalCorrectBalloon, balloonIndex ->
                                                if (!audioEnabled) return@popBalloonSfx
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
                                                    return@popBalloonSfx
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
                                            onWrongPick = popWrong@{
                                                if (!consumeTapCooldown()) return@popWrong
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
                                                            baseShakeAmplitudePx =
                                                                if (isSagaEpisode(chapterId)) 20f else 18f,
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
                                                // Episode 1 station 2: show the last popped balloon beside the header chip.
                                                // Episodes 4–5 feedback: do not pin the last balloon in the header.
                                                if (ch1St2 && chapterId != 4 && chapterId != 5) {
                                                    station2PinnedBalloonLetter = lastLetter
                                                    station2PinnedBalloonColor = poppedBalloonColor
                                                } else if (!ch1St2) {
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
                                            episode4CorrectBalloonHintEpoch =
                                                if (episode4HelpSt15 && stationId == Chapter1StationOrder.BALLOON_POP) {
                                                    episode4Help.station2BalloonHintEpoch
                                                } else {
                                                    0
                                                },
                                            helpSideInsetDp = stationUiSpec.balloonPlayAreaStartInsetDp.dp,
                                        )
                                    }
                                }
                            is Question.PictureStartsWithQuestion -> {
                                val pictureInstructionText =
                                    when {
                                        stationUiSpec.pictureStartsWithInstructionOverride != null ->
                                            stationUiSpec.pictureStartsWithInstructionOverride
                                        listenOnly && isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE ->
                                            "באיזו אות המילה מתחילה?"
                                        else ->
                                            "באיזו אות מתחילה המילה?"
                                    }
                                val pictureShowWordCaption =
                                    !(
                                        listenOnly &&
                                            isSagaEpisode(chapterId) &&
                                            stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
                                            stationUiSpec.hidePictureWordCaptionWhenListenOnlySaga
                                    )
                                val pictureVerticalNudge =
                                    if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                        SixStationArcHalfCmNudge
                                    } else {
                                        0.dp
                                    }
                                PictureStartsWithStationContent(
                                    question = current,
                                    instructionText = pictureInstructionText,
                                    instructionReadablePanel = stationUiSpec.pictureStartsWithReadablePanel,
                                    showWordCaption = pictureShowWordCaption,
                                    onPictureTapReplayWord =
                                        if (episode4HelpSt15 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            { performEpisode4HelpReplay() }
                                        } else {
                                            null
                                        },
                                    temporaryStartingLetterHint =
                                        if (episode4HelpSt15 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            episode4Help.activeHintLetter
                                        } else {
                                            null
                                        },
                                    pinnedCorrectLetter =
                                        if (chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            station4PinnedCorrectLetter
                                        } else {
                                            null
                                        },
                                    enabled = gameChoicesEnabled,
                                    shakePx = optionsShake.value,
                                    entryPulseEpoch = entryPulseEpoch,
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
                                    pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    hintCorrectLetter = current.correctLetter.takeIf { wrongTapsThisQuestion >= 2 },
                                    hintPulseEpoch = hintPulseEpoch,
                                    correctPulseLetter = correctTapPulseLetter,
                                    correctPulseEpoch = correctTapPulseEpoch,
                                    wrongFlashLetter = station4WrongFlashLetter,
                                    wrongFlashEpoch = station4WrongFlashEpoch,
                                    entryPulseScale = entryPulseScale.value,
                                    verticalNudgeDp = pictureVerticalNudge,
                                    onPickLetter = picturePick@{ picked ->
                                        if (!consumeTapCooldown()) return@picturePick
                                        cancelFeedbackVoice()
                                        if (audioEnabled && chapterId == 3 && stationId == 1) {
                                            val clip = AudioClips.letterNameClip(picked)
                                            if (clip != null && voice.hasAsset(clip)) {
                                                feedbackVoiceJob = scope.launch { voice.playBlocking(clip) }
                                            }
                                        }
                                        when (session.submitPictureStartsWith(picked)) {
                                            AnswerResult.Correct -> {
                                                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                if (chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                                    station4PinnedCorrectLetter = picked
                                                }
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
                                                        if (audioEnabled && chapterId == 3 && stationId == 1) {
                                                            withTimeoutOrNull(1200L) { feedbackVoiceJob?.join() }
                                                        }
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
                                                    if (audioEnabled && chapterId == 3 && stationId == 1) {
                                                        // Let the tapped letter name play; wrong feedback is still immediate via visuals/SFX.
                                                        onWrongFeedback(wrongPickedLetterAlreadySpoken = true)
                                                    } else {
                                                        onWrongFeedback()
                                                    }
                                                }
                                            }
                                            AnswerResult.Finished -> {}
                                        }
                                    },
                                )
                            }
                            is Question.ImageMatchQuestion ->
                                if (chapterId == 3 && stationId == 6) {
                                    Chapter3Station6ImageToWordStationContent(
                                        question = current,
                                        contentKey = session.currentIndex,
                                        enabled = gameChoicesEnabled,
                                        entryPulseScale = entryPulseScale.value,
                                        optionsShakePx = optionsShake.value,
                                        onWordPressed = ch3ImgWord@{ choiceId ->
                                            if (!audioEnabled) return@ch3ImgWord
                                            cancelFeedbackVoice()
                                            feedbackVoiceJob =
                                                scope.launch {
                                                    val ch3Clip = "audio/ch3_word_${choiceId}.wav"
                                                    val clip =
                                                        if (voice.hasAsset(ch3Clip)) ch3Clip else AudioClips.wordClipByCatalogId(choiceId)
                                                    voice.playBlocking(clip)
                                                }
                                        },
                                        onAttempt = ch3ImgAttempt@{ choiceId ->
                                            if (!consumeTapCooldown()) return@ch3ImgAttempt false
                                            cancelFeedbackVoice()
                                            when (session.submitImageMatch(choiceId)) {
                                                AnswerResult.Correct -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                                    scope.launch {
                                                        if (audioEnabled) {
                                                            val ch3Clip = "audio/ch3_word_${choiceId}.wav"
                                                            val clip =
                                                                if (voice.hasAsset(ch3Clip)) ch3Clip else AudioClips.wordClipByCatalogId(choiceId)
                                                            voice.playBlocking(clip)
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
                                                        val isLast = session.currentIndex >= session.totalQuestions - 1
                                                        advanceAfterRound(isLast)
                                                    }
                                                    true
                                                }
                                                AnswerResult.Wrong -> {
                                                    if (audioEnabled) ChildGameAudioHooks.onWrong()
                                                    wrongTapsThisQuestion += 1
                                                    if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                    // Station 6: play error SFX AND say which word was pressed.
                                                    onWrongFeedback(wrongWordCatalogId = choiceId)
                                                    false
                                                }
                                                AnswerResult.Finished -> false
                                            }
                                        },
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
                                    val matchLetterVerticalNudge =
                                        if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                                            SixStationArcHalfCmNudge
                                        } else {
                                            0.dp
                                        }
                                    MatchLetterToWordStationContent(
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
                                        enabled = gameChoicesEnabled,
                                        compactWideSpread =
                                            isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH,
                                        onWordPressed = { choiceId ->
                                            speakNow {
                                                val ch3Clip = "audio/ch3_word_${choiceId}.wav"
                                                val clip =
                                                    if (voice.hasAsset(ch3Clip)) ch3Clip else AudioClips.wordClipByCatalogId(choiceId)
                                                voice.playBlocking(clip)
                                            }
                                        },
                                        onLetterPressed = matchLetterTap@{ letter ->
                                            val clip = AudioClips.letterNameClip(letter) ?: return@matchLetterTap
                                            speakNow { voice.playBlocking(clip) }
                                        },
                                        onCorrectMatch = matchCorrect@{ _ ->
                                            if (!audioEnabled) return@matchCorrect
                                            // Short reward, no waiting.
                                            scope.launch { sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f) }
                                        },
                                        onWrongMatch = { pickedLetter, pickedChoiceId ->
                                            // Letter/word are spoken on press; keep wrong feedback minimal (no extra voice stacking here).
                                        },
                                        onMatchAttempt = matchAttempt@{ correct ->
                                            if (!audioEnabled) return@matchAttempt
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
                                        instructionReadablePanelOverride =
                                            (chapterId == 3 && stationId == 2) ||
                                                stationUiSpec.matchLetterInstructionReadablePanel,
                                        instructions = "ליחצו על אות והמילה שמתחילה באותה האות",
                                        onSolved = matchSolved@{
                                            if (!consumeTapCooldown()) return@matchSolved
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
                                        entryPulseScale = entryPulseScale.value,
                                        verticalNudgeDp = matchLetterVerticalNudge,
                                    )
                                } else {
                                    val imageMatchVerticalNudge =
                                        if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                            SixStationArcHalfCmNudge
                                        } else {
                                            0.dp
                                        }
                                    SagaImageMatchGameStationContent(
                                        question = current,
                                        headerInstructionText =
                                            stationUiSpec.imageMatchHeaderInstructionOverride
                                                ?: when {
                                                    isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL ->
                                                        if (listenOnly) {
                                                            "מצאו את המילה לפי האות שנשמעה:"
                                                        } else {
                                                            "מצא את המילה המתחילה באות:"
                                                        }
                                                    else ->
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
                                        showTargetLetterChip = stationUiSpec.imageMatchShowTargetLetterChip,
                                        listenOnlyTemporaryHintLetter =
                                            if (episode4HelpSt15 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                episode4Help.activeHintLetter
                                            } else {
                                                null
                                            },
                                        headerTopPaddingDp =
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) 10 else 0,
                                        readableInstructionHeaderPanel = stationUiSpec.imageMatchHeaderReadablePanel,
                                        targetLetterChipOffsetYDp =
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) 0 else -10,
                                        contentKey = session.currentIndex,
                                        enabled = gameChoicesEnabled,
                                        shakePx = optionsShake.value,
                                        entryPulseEpoch = entryPulseEpoch,
                                        hintCorrectChoiceId = current.correctChoiceId.takeIf { wrongTapsThisQuestion >= 2 },
                                        hintPulseEpoch = hintPulseEpoch,
                                        showWordCaptions = !(chapterId == 3 && stationId == 6),
                                        captionSizeMultiplier =
                                            plan.imageMatchCaptionSizeMultiplier *
                                                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                    1.2f
                                                } else {
                                                    1f
                                                },
                                        pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                        innerPictureScaleForChoice = { choice ->
                                            if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                                                Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                            } else {
                                                1f
                                            }
                                        },
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        onAttempt = imageMatchAttempt@{ choiceId ->
                                            if (!consumeTapCooldown()) return@imageMatchAttempt false
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
                                        entryPulseScale = entryPulseScale.value,
                                        verticalNudgeDp = imageMatchVerticalNudge,
                                    )
                                }
                            is Question.FinaleSlotQuestion ->
                                FinaleGame(
                                    question = current,
                                    contentKey = session.currentIndex,
                                    enabled = gameChoicesEnabled,
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
                    if (phase == GamePhase.Play &&
                        episode4HelpSt15 &&
                        (stationId == Chapter1StationOrder.TAP_LETTER || stationId == Chapter1StationOrder.BALLOON_POP) &&
                        episode4Help.activeHintLetter != null &&
                        current is Question.PopBalloonsQuestion &&
                        !stationUiSpec.excludeFullScreenBalloonHintOverlay
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().zIndex(3f),
                            contentAlignment = Alignment.Center,
                        ) {
                            TargetLetterHeaderChip(letter = episode4Help.activeHintLetter!!)
                        }
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
            GameScreenDinoLayer(
                idleRes = dinoDrawable,
                talkFrameResIds = talkFrames,
                isTalking = dinoTalking || (isSagaEpisode(chapterId) && inputLocked && dinoVisual == DinoVisual.Jump),
                dinoForward = dinoForward,
                dinoSlip = dinoSlip,
                dinoTilt = dinoTilt,
                dinoScale = dinoScale,
            )
        }
        if (episode4HelpSt15) {
            Episode4Stations15HelpColumn(
                // Replay must stay available while choices are locked for feedback; only letter/grid taps use inputLocked.
                replayEnabled = phase == GamePhase.Play,
                hintEnabled = phase == GamePhase.Play && !episode4Help.hintLocksChoices,
                onReplay = { performEpisode4HelpReplay() },
                onHint = { performEpisode4HelpHint() },
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp, top = 100.dp, bottom = 96.dp)
                        .zIndex(6f),
            )
        }
    }
}
