package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.audio.GameAudioEngine
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Chapter3EpisodeContent
import com.tal.hebrewdino.ui.domain.InstructionPanelStyle
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationInstructionCopy
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationReplayMode
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.StationVariant
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.domain.hasVariant
import com.tal.hebrewdino.ui.screens.StationHeaderMode
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.components.Episode4Stations15HelpColumn
import com.tal.hebrewdino.ui.components.Chapter3Station5ReplayColumn
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.layout.ScreenFit
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

/** ~1 cm vertical gap between the back button and the collected-egg strip. */
private val SpaceBelowBackBeforeEggs = 38.dp

/** Chapters that use the shared six-station journey ([Chapter1StationOrder]); intros, art, and letter pools differ. */
private val SixStationArcChapterRange = 1..5

private fun isSagaEpisode(chapterId: Int): Boolean = chapterId in SixStationArcChapterRange

private data class SideHelpControls(
    val replayEnabled: Boolean,
    val hintEnabled: Boolean,
    val onReplay: () -> Unit,
    val onHint: () -> Unit,
)

private class TapCooldown(private val minIntervalMs: Long = 130L) {
    private var lastTapMs: Long = 0L

    fun consume(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastTapMs < minIntervalMs) return false
        lastTapMs = now
        return true
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

private object WrongFeedbackActions {
    fun trigger(
        scope: CoroutineScope,
        audioEnabled: Boolean,
        sagaEpisode: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaUsesPickLetterAudioStaging: Boolean,
        isChapter3HighlightedLetterInWordStation: Boolean,
        isChapter3AudioLetterRecognitionStation: Boolean,
        gameFeedback: GameFeedback,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        cancelFeedbackVoice: () -> Unit,
        setFeedbackVoiceJob: (Job?) -> Unit,
        optionsShake: Animatable<Float, AnimationVector1D>,
        dinoSlip: Animatable<Float, AnimationVector1D>,
        dinoTilt: Animatable<Float, AnimationVector1D>,
        setDinoVisual: (DinoVisual) -> Unit,
        setInputLocked: (Boolean) -> Unit,
        onWrongHook: () -> Unit,
        wrongPickedLetter: String? = null,
        wrongWordCatalogId: String? = null,
        wrongPickedLetterAlreadySpoken: Boolean = false,
        wrongWordAlreadySpoken: Boolean = false,
    ) {
        scope.launch {
            setInputLocked(true)
            setDinoVisual(DinoVisual.TryAgain)
            if (sagaEpisode) {
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
                (sagaEpisode && (stationId == Chapter1StationOrder.PICTURE_PICK_ONE || stationId == Chapter1StationOrder.PICTURE_PICK_ALL))
            playShake(
                scope,
                optionsShake,
                baseShakeAmplitudePx = if (sagaEpisode) 20f else 18f,
                strength = if (strongerWrongShake) 1.25f else 1f,
            )
            if (audioEnabled) {
                val allowWrongSfx =
                    (!(sagaUsesPickLetterAudioStaging) || isChapter3HighlightedLetterInWordStation || isChapter3AudioLetterRecognitionStation) &&
                        !(sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL)
                if (allowWrongSfx) {
                    gameFeedback.playWrong()
                    onWrongHook()
                }
                if (sagaUsesPickLetterAudioStaging && wrongPickedLetter != null && chapterId != 3) {
                    cancelFeedbackVoice()
                    setFeedbackVoiceJob(
                        scope.launch {
                            val letterClip = AudioClips.letterNameClip(wrongPickedLetter)
                            val letterMs = letterClip?.let { sfx.durationMs(it) } ?: 0L
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
                        },
                    )
                    setDinoVisual(DinoVisual.Idle)
                    setInputLocked(false)
                    return@launch
                }
                if (sagaEpisode &&
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
                    wrongPickedLetter != null
                ) {
                    cancelFeedbackVoice()
                    setFeedbackVoiceJob(
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
                        },
                    )
                    setDinoVisual(DinoVisual.Idle)
                    setInputLocked(false)
                    return@launch
                }
                cancelFeedbackVoice()
                setFeedbackVoiceJob(
                    scope.launch {
                        val feedbackDelayMs =
                            when {
                                chapterId == 4 && stationId == Chapter1StationOrder.TAP_LETTER -> 0L
                                chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE -> 0L
                                chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL -> 0L
                                (chapterId == 3 || chapterId == 6) && stationId == 4 -> 0L
                                chapterId == 5 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE -> 0L
                                else -> 110L
                            }
                        delay(feedbackDelayMs)
                        if (wrongWordCatalogId != null && !wrongWordAlreadySpoken) {
                            val wordPath = AudioClips.wordClipByCatalogId(wrongWordCatalogId)
                            if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                            return@launch
                        }

                        if (wrongPickedLetter != null) {
                            if (chapterId == 5 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                val lc = AudioClips.letterNameClip(wrongPickedLetter)
                                if (lc != null && voice.hasAsset(lc)) {
                                    voice.playBlocking(lc)
                                }
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                return@launch
                            }
                            if (!wrongPickedLetterAlreadySpoken) {
                                val letterName = AudioClips.letterNameClip(wrongPickedLetter)
                                if (letterName != null && voice.hasAsset(letterName)) {
                                    voice.playBlocking(letterName)
                                }
                            }
                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                            return@launch
                        }

                        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                    },
                )
            }
            setDinoVisual(DinoVisual.Idle)
            setInputLocked(false)
        }
    }
}

private object AdvanceAfterRoundActions {
    suspend fun run(
        scope: CoroutineScope,
        audioEnabled: Boolean,
        sagaEpisode: Boolean,
        chapterId: Int,
        stationId: Int,
        isLast: Boolean,
        ch3SpellMidWord: Boolean,
        suppressInGameDinoProgress: Boolean,
        sagaUsesPickLetterAudioStaging: Boolean,
        sagaUsesPopBalloonsAudioStaging: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        isChapter3HighlightedLetterInWordStation: Boolean,
        isChapter3AudioLetterRecognitionStation: Boolean,
        gameFeedback: GameFeedback,
        voice: VoicePlayer,
        cancelFeedbackVoice: () -> Unit,
        getFeedbackVoiceJob: () -> Job?,
        setFeedbackVoiceJob: (Job?) -> Unit,
        setInputLocked: (Boolean) -> Unit,
        setDinoVisual: (DinoVisual) -> Unit,
        dinoForward: Animatable<Float, AnimationVector1D>,
        forwardDir: Float,
        dinoScale: Animatable<Float, AnimationVector1D>,
        contentAlpha: Animatable<Float, AnimationVector1D>,
        clearPinnedBalloon: () -> Unit,
        session: LevelSession,
        getCompletionCallbackFired: () -> Boolean,
        markCompletionCallbackFired: () -> Unit,
        onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
        onLevelCompleteHook: () -> Unit,
    ) {
        setInputLocked(true)
        if (audioEnabled && !ch3SpellMidWord) onLevelCompleteHook()
        if (audioEnabled) {
            when {
                sagaUsesPickLetterAudioStaging && isLast && chapterId != 3 -> gameFeedback.playCorrect()
                sagaUsesPopBalloonsAudioStaging -> Unit
                sagaUsesFindGridAudioStaging -> Unit
                sagaEpisode && stationId == 6 -> Unit
                isLast -> gameFeedback.playSuccessBig()
                else -> gameFeedback.playCorrect()
            }
        }
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)) {
            setDinoVisual(DinoVisual.Jump)
        }
        val episode1PraiseEligible =
            audioEnabled &&
                sagaEpisode &&
                stationId in 2..5 &&
                stationId != Chapter1StationOrder.PICTURE_PICK_ONE &&
                !isChapter3AudioLetterRecognitionStation &&
                Random.nextFloat() < Episode1PraiseChance
        val otherPraiseEligible =
            audioEnabled &&
                !(sagaEpisode && stationId == 6) &&
                !(sagaUsesPickLetterAudioStaging) &&
                !(sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) &&
                !episode1PraiseEligible

        if (episode1PraiseEligible) {
            if (sagaUsesFindGridAudioStaging) {
                withTimeoutOrNull(5000L) { getFeedbackVoiceJob()?.join() }
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
            candidates.shuffle()
            val arr = candidates.toTypedArray()
            setFeedbackVoiceJob(scope.launch { voice.playFirstAvailableBlocking(*arr) })
        } else if (otherPraiseEligible) {
            if (sagaUsesFindGridAudioStaging) {
                withTimeoutOrNull(5000L) { getFeedbackVoiceJob()?.join() }
            }
            cancelFeedbackVoice()
            setFeedbackVoiceJob(
                scope.launch {
                    val pool = mutableListOf(AudioClips.VoKolHakavod, AudioClips.VoGoodJob1)
                    pool.shuffle()
                    voice.playFirstAvailableBlocking(*pool.toTypedArray())
                },
            )
        }
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)) {
            dinoForward.animateTo(dinoForward.value + forwardDir * 12f, spring(dampingRatio = 0.75f, stiffness = 520f))
        }
        val strongerSuccessPulse =
            (sagaUsesPickLetterAudioStaging || sagaUsesFindGridAudioStaging) &&
                !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)
        val station456SuccessPulse =
            (sagaEpisode &&
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
        delay(
            when {
                ch3SpellMidWord -> 38
                sagaUsesFindGridAudioStaging -> 120
                else -> 170
            },
        )
        val waitPraiseBeforeFade =
            sagaEpisode &&
                (sagaUsesPopBalloonsAudioStaging ||
                    sagaUsesFindGridAudioStaging ||
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE)
        if (sagaUsesPopBalloonsAudioStaging) {
            withTimeoutOrNull(8000) { getFeedbackVoiceJob()?.join() }
            clearPinnedBalloon()
        } else if (sagaEpisode && (sagaUsesFindGridAudioStaging || stationId == Chapter1StationOrder.PICTURE_PICK_ONE)) {
            withTimeoutOrNull(8000) { getFeedbackVoiceJob()?.join() }
        }
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        if (!waitPraiseBeforeFade) {
            withTimeoutOrNull(2500) { getFeedbackVoiceJob()?.join() }
        }
        delay(5)
        session.nextQuestion()
        if (session.currentQuestion == null && !getCompletionCallbackFired()) {
            markCompletionCallbackFired()
            onComplete(stationId, session.correctCount, session.mistakeCount)
        }
        contentAlpha.animateTo(1f, tween(BetweenQuestionFadeMs))
    }
}

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

    val trainingRound: Int? =
        if (chapterId == TrainingV1Config.CHAPTER_ID) {
            topChromeProgressOverride?.first
        } else {
            null
        }
    val scope = rememberCoroutineScope()
    val episode4Help = rememberEpisode4HelpController(stationId = stationId, scope = scope)
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val audio = remember { GameAudioEngine(context = context) }
    val voice = audio.voice
    val sfx = audio.sfx
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
    val popBalloonsHelpControlsEnabled = stationUiSpec.popBalloonsHelpControlsEnabled && !episode4HelpSt15
    val showPopBalloonsTargetLetterChip = !listenOnly && !popBalloonsHelpControlsEnabled
    val balloonHelp = rememberBalloonHelpController(stationId = stationId, scope = scope)
    val gameChoicesEnabled =
        !inputLocked &&
            !episode4Help.hintLocksChoices &&
            !(popBalloonsHelpControlsEnabled && balloonHelp.hintLocksChoices)

    fun stopStagingSfx(stopAllStreams: Boolean) {
        if (sagaUsesPickLetterAudioStaging) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(station1VoiceStreamId)
            station1VoiceStreamId = 0
        }
        if (usesPopBalloonsSoundPoolPrompt) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(station2VoiceStreamId)
            station2VoiceStreamId = 0
        }
        if (sagaUsesFindGridAudioStaging) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(station3VoiceStreamId)
            station3VoiceStreamId = 0
        }
    }

    fun cancelFeedbackVoice() {
        feedbackVoiceJob?.cancel()
        feedbackVoiceJob = null
        promptVoiceJob?.cancel()
        promptVoiceJob = null
        voice.stopNow()
        stopStagingSfx(stopAllStreams = true)
    }

    DisposableEffect(lifecycleOwner, stationId) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    cancelFeedbackVoice()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cancelFeedbackVoice()
            audio.release()
        }
    }

    // UX: global tap cooldown to prevent fast-tap flow breaks.
    val tapCooldown = remember(stationId) { TapCooldown() }
    fun consumeTapCooldown(): Boolean = tapCooldown.consume()

    fun performSideHelpReplay() {
        feedbackVoiceJob =
            SideHelpActions.startReplay(
                audioEnabled = audioEnabled,
                isPlayPhase = phase == GamePhase.Play,
                episode4HelpEnabled = episode4HelpSt15,
                popBalloonsHelpEnabled = popBalloonsHelpControlsEnabled,
                chapterId = chapterId,
                stationId = stationId,
                plan = plan,
                stationUiSpec = stationUiSpec,
                session = session,
                voice = voice,
                sfx = sfx,
                cancelFeedbackVoice = { cancelFeedbackVoice() },
                stopStagingSfx = { stopAll -> stopStagingSfx(stopAllStreams = stopAll) },
                scope = scope,
            )
    }

    fun performSideHelpHint() {
        SideHelpActions.performHint(
            isPlayPhase = phase == GamePhase.Play,
            episode4HelpEnabled = episode4HelpSt15,
            popBalloonsHelpEnabled = popBalloonsHelpControlsEnabled,
            stationId = stationId,
            stationUiSpec = stationUiSpec,
            session = session,
            episode4Help = episode4Help,
            balloonHelp = balloonHelp,
        )
    }
    val jumpFrames =
        remember(stationId) {
            listOf(R.drawable.dino_jump_0, R.drawable.dino_jump_1, R.drawable.dino_jump_2)
        }
    var jumpFrameIndex by remember(stationId) { mutableIntStateOf(0) }
    val forwardDir = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f

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
        if (!(audioEnabled && usesPopBalloonsSoundPoolPrompt)) return@LaunchedEffect
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
        balloonHelp.reset()
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
                    playIntroPrompt(
                        audioEnabled = audioEnabled,
                        chapterId = chapterId,
                        stationId = stationId,
                        listenOnlyTargetPrompt = listenOnly,
                        stationTemplateId = stationUiSpec.templateId,
                        planPopAllLettersInWord = plan.popAllLettersInWord,
                        isSagaEpisode = isSagaEpisode(chapterId),
                        sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                        sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                        sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                        isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                        isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                        session = session,
                        q = q,
                        voice = voice,
                        sfx = sfx,
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
                    )
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
        AdvanceAfterRoundActions.run(
            scope = scope,
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
            getFeedbackVoiceJob = { feedbackVoiceJob },
            setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
            setInputLocked = { locked -> inputLocked = locked },
            setDinoVisual = { v -> dinoVisual = v },
            dinoForward = dinoForward,
            forwardDir = forwardDir,
            dinoScale = dinoScale,
            contentAlpha = contentAlpha,
            clearPinnedBalloon = {
                station2PinnedBalloonLetter = null
                station2PinnedBalloonColor = null
            },
            session = session,
            getCompletionCallbackFired = { completionCallbackFired },
            markCompletionCallbackFired = { completionCallbackFired = true },
            onComplete = onComplete,
            onLevelCompleteHook = { ChildGameAudioHooks.onLevelComplete() },
        )
    }

    fun onWrongFeedback(
        wrongPickedLetter: String? = null,
        wrongWordCatalogId: String? = null,
        wrongPickedLetterAlreadySpoken: Boolean = false,
        wrongWordAlreadySpoken: Boolean = false,
    ) {
        WrongFeedbackActions.trigger(
            scope = scope,
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
            setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
            optionsShake = optionsShake,
            dinoSlip = dinoSlip,
            dinoTilt = dinoTilt,
            setDinoVisual = { v -> dinoVisual = v },
            setInputLocked = { locked -> inputLocked = locked },
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
        val contentTopInsetBase = (stationUiSpec.contentTopInsetDp?.dp ?: 40.dp)
        val contentTopInset =
            if (isCompactLandscapePhone) {
                contentTopInsetBase.coerceAtMost(28.dp)
            } else {
                contentTopInsetBase
            }
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
                    .statusBarsPadding()
                    // Give a consistent ~1cm breathing room below the status bar so the progress line
                    // and the "חזור" button are never clipped.
                    .padding(
                        start = if (isCompactLandscapePhone) 6.dp else 8.dp,
                        end = if (isCompactLandscapePhone) 6.dp else 8.dp,
                        top = if (isCompactLandscapePhone) 12.dp else 38.dp,
                        bottom = 0.dp,
                    )
                    .offset(y = if (isSagaEpisode(chapterId)) -SixStationArcHalfCmNudge else 0.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(4f),
        )

        if (isCompactLandscapePhone) {
            val dinoDrawable =
                when (dinoVisual) {
                    DinoVisual.Idle -> R.drawable.dino_idle
                    DinoVisual.TryAgain -> R.drawable.dino_try_again
                    DinoVisual.Jump -> jumpFrames[jumpFrameIndex.coerceIn(0, jumpFrames.lastIndex)]
                }
            val talkFrames =
                listOf(R.drawable.dino_talk_0, R.drawable.dino_talk_1, R.drawable.dino_talk_2, R.drawable.dino_talk_3)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GameScreenDinoLayer(
                        idleRes = dinoDrawable,
                        talkFrameResIds = talkFrames,
                        isTalking = dinoTalking || (isSagaEpisode(chapterId) && inputLocked && dinoVisual == DinoVisual.Jump),
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
                    if (phase == GamePhase.Intro) {
                        if (stationUiSpec.showBetweenRoundIntroPulse) {
                            IntroPulse(stationId = stationId, question = current, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        when (current) {
                            is Question.FindLetterGridQuestion -> {
                                FindLetterGridQuestionRenderer(
                                    current = current,
                                    listenOnly = listenOnly,
                                    isSagaRevealStation = stationUiSpec.findGridSagaRevealStation,
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
                                            (stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE ||
                                                (chapterId == TrainingV1Config.CHAPTER_ID &&
                                                    stationId == TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID))
                                        ) {
                                            episode4Help.activeHintLetter
                                        } else {
                                            null
                                        },
                                    episode4TargetCellsHintEpoch =
                                        if (episode4HelpSt15 &&
                                            (stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE ||
                                                (chapterId == TrainingV1Config.CHAPTER_ID &&
                                                    stationId == TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID))
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
                                                sfx.stopAllStreams()
                                                station3VoiceStreamId = 0
                                                val isCorrect = tapped == current.targetLetter
                                                feedbackVoiceJob =
                                                    scope.launch {
                                                        if (isCorrect) {
                                                            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.62f)
                                                        } else {
                                                            val tappedClip = AudioClips.letterNameClip(tapped)
                                                            when (Random.nextInt(100)) {
                                                                in 0..39 -> {
                                                                    sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                                                                }
                                                                in 40..89 -> {
                                                                    if (tappedClip != null) {
                                                                        station3VoiceStreamId =
                                                                            sfx.playReturningStreamId(tappedClip, volume = 1f) ?: 0
                                                                    } else {
                                                                        sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                                                                    }
                                                                }
                                                                else -> {
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
                                    onCorrectTap =
                                        if (audioEnabled && !sagaUsesFindGridAudioStaging) {
                                            {
                                                scope.launch {
                                                    sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
                                                }
                                            }
                                        } else {
                                            null
                                        },
                                    onCellTapped = gridCellTap@{ index ->
                                        if (!consumeTapCooldown()) return@gridCellTap
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
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            is Question.PopBalloonsQuestion -> {
                                if (plan.mode == StationQuizMode.PickLetter) {
                                    PickLetterQuestionRenderer(
                                        current = current,
                                        stationUiSpec = stationUiSpec,
                                        listenOnly = listenOnly,
                                        isSagaEpisode = isSagaEpisode(chapterId),
                                        sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        highlightedInWordWord = highlightedInWordRound?.word,
                                        highlightedInWordSlotIndex = highlightedInWordRound?.slotIndex,
                                        isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                                        isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                                        station1PinnedCorrectLetter = station1PinnedCorrectLetter,
                                        entryPulseScale = entryPulseScale.value,
                                        enabled = gameChoicesEnabled,
                                        shakePx = optionsShake.value,
                                        wrongTapsThisQuestion = wrongTapsThisQuestion,
                                        hintPulseEpoch = hintPulseEpoch,
                                        correctTapPulseLetter = correctTapPulseLetter,
                                        correctTapPulseEpoch = correctTapPulseEpoch,
                                        temporaryHintLetter = episode4Help.activeHintLetter,
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
                                                        val wordDone =
                                                            session.highlightedLetterInWordCompletesWordAfterCorrectRound()
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
                                                    shakeEpoch += 1
                                                    wrongTapsThisQuestion += 1
                                                    if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
                                                    onWrongFeedback(wrongPickedLetter = picked)
                                                }
                                                AnswerResult.Finished -> {}
                                            }
                                        },
                                    )
                                } else {
                                        val correctLetterSet =
                                            if (isChapter3PopAllLettersStation) {
                                                val w = session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                                                w.toCharArray().map { it.toString() }.toSet()
                                            } else {
                                                null
                                            }
                                        val visualRoundSeed =
                                            if (sagaUsesPopBalloonsAudioStaging) {
                                                session.currentIndex
                                            } else {
                                                0
                                            }
                                        val maxVisibleBalloonCount =
                                            if (isCompactLandscapePhone && stationUiSpec.popBalloonsCompactLandscapePhoneTuning) 8 else null
                                        val onPopSfx: suspend (String, Boolean, Boolean, Int) -> Unit =
                                            popBalloonSfx@{ letter, isCorrect, finalCorrectBalloon, balloonIndex ->
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
                                            }
                                        val onWrongPick =
                                            popWrong@{
                                                if (!consumeTapCooldown()) return@popWrong
                                                if (!(sagaUsesPopBalloonsAudioStaging)) {
                                                    cancelFeedbackVoice()
                                                }
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
                                            }
                                        val onAllCorrectPopped: (String, Color) -> Unit = { lastLetter, poppedBalloonColor ->
                                            val ch1St2 = sagaUsesPopBalloonsAudioStaging
                                            if (ch1St2 && chapterId != 4 && chapterId != 5) {
                                                station2PinnedBalloonLetter = lastLetter
                                                station2PinnedBalloonColor = poppedBalloonColor
                                            } else if (!ch1St2) {
                                                cancelFeedbackVoice()
                                            }
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
                                                            if (audioEnabled && !ch1St2) {
                                                                ChildGameAudioHooks.onCorrect()
                                                            }
                                                            val isLast = session.currentIndex >= session.totalQuestions - 1
                                                            advanceAfterRound(isLast)
                                                        }
                                                    else -> {}
                                                }
                                            }
                                        }
                                        val episode4CorrectBalloonHintEpoch =
                                            if (episode4HelpSt15 && stationId == Chapter1StationOrder.BALLOON_POP) {
                                                episode4Help.station2BalloonHintEpoch
                                            } else {
                                                0
                                            }
                                        val helpSideInsetDp = stationUiSpec.balloonPlayAreaStartInsetDp.dp
                                        val popAllWordForBanner = session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                                        PopBalloonsQuestionRenderer(
                                            current = current,
                                            planMode = plan.mode,
                                            planPopAllLettersInWord = plan.popAllLettersInWord,
                                            popAllLettersWordForBanner = popAllWordForBanner,
                                            popAllLettersBannerInstruction =
                                                stationUiSpec.popBalloonsPopAllLettersBannerInstruction
                                                    ?: StationInstructionCopy.PopBalloonsPopAllLettersInWord,
                                            stationUiSpec = stationUiSpec,
                                            isCompactLandscapePhone = isCompactLandscapePhone,
                                            listenOnly = listenOnly,
                                            sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                                            showPopBalloonsTargetLetterChip = showPopBalloonsTargetLetterChip,
                                            episode4HelpSt15 = episode4HelpSt15,
                                            episode4HelpActiveHintLetter = episode4Help.activeHintLetter,
                                            hintHeaderScale = hintHeaderScale.value,
                                            station2PinnedBalloonLetter = station2PinnedBalloonLetter,
                                            station2PinnedBalloonColor = station2PinnedBalloonColor,
                                            correctLetterSet = correctLetterSet,
                                            enabled = gameChoicesEnabled,
                                            shakePx = optionsShake.value,
                                            entryPulseScale = entryPulseScale.value,
                                            visualRoundSeed = visualRoundSeed,
                                            maxVisibleBalloonCount = maxVisibleBalloonCount,
                                            episode4CorrectBalloonHintEpoch = episode4CorrectBalloonHintEpoch,
                                            helpSideInsetDp = helpSideInsetDp,
                                            contentTopPaddingDp =
                                                if (sagaUsesPopBalloonsAudioStaging) SixStationArcHalfCmNudge else 0.dp,
                                            onBalloonPressed = { _ -> },
                                            onPopSfx = onPopSfx,
                                            onWrongPick = onWrongPick,
                                            onAllCorrectPopped = onAllCorrectPopped,
                                        )
                                    }
                            }
                            is Question.PictureStartsWithQuestion -> {
                                val pictureInstructionText =
                                    when {
                                        stationUiSpec.pictureStartsWithInstructionOverride != null ->
                                            stationUiSpec.pictureStartsWithInstructionOverride
                                        listenOnly && isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE ->
                                            stationUiSpec.pictureStartsWithListenOnlySagaInstruction
                                                ?: StationInstructionCopy.PictureStartsWithListenFirstSaga
                                        else ->
                                            StationInstructionCopy.PictureStartsWithDefault
                                    }
                                val pictureInstructionReadablePanel =
                                    stationUiSpec.pictureStartsWithReadablePanel ||
                                        stationUiSpec.pictureStartsWithInstructionPanelStyle == InstructionPanelStyle.WhiteRounded
                                val pictureShowWordCaption =
                                    !(listenOnly && stationUiSpec.hidePictureWordCaptionWhenListenOnlySaga)
                                PictureStartsWithQuestionRenderer(
                                    current = current,
                                    stationUiSpec = stationUiSpec,
                                    isCompactLandscapePhone = isCompactLandscapePhone,
                                    instructionText = pictureInstructionText,
                                    instructionReadablePanel = pictureInstructionReadablePanel,
                                    showWordCaption = pictureShowWordCaption,
                                    onPictureTapReplayWord =
                                        if (episode4HelpSt15 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            { performSideHelpReplay() }
                                        } else if (audioEnabled) {
                                            {
                                                cancelFeedbackVoice()
                                                val wordPath = AudioClips.wordClipByCatalogId(current.catalogEntryId)
                                                if (voice.hasAsset(wordPath)) {
                                                    feedbackVoiceJob = scope.launch { voice.playBlocking(wordPath) }
                                                }
                                            }
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
                                    entryPulseEpoch =
                                        if (chapterId == 6 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                            0
                                        } else {
                                            entryPulseEpoch
                                        },
                                    promptWordSizeMultiplier = plan.imageMatchCaptionSizeMultiplier * 1.2f,
                                    innerPictureScale =
                                        Chapter1Station5And6ImageMatchInnerScale.innerScalePictureStartsWith(
                                            catalogEntryId = current.catalogEntryId,
                                            letter = current.correctLetter,
                                            word = current.word,
                                            tintArgb = current.tintArgb,
                                            tileDrawable = current.tileDrawable,
                                        ),
                                    pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                    sortOptionLetters = plan.sortOptionLetters,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    hintCorrectLetter = current.correctLetter.takeIf { wrongTapsThisQuestion >= 2 },
                                    hintPulseEpoch = hintPulseEpoch,
                                    correctPulseLetter = correctTapPulseLetter,
                                    correctPulseEpoch = correctTapPulseEpoch,
                                    wrongFlashLetter = station4WrongFlashLetter,
                                    wrongFlashEpoch = station4WrongFlashEpoch,
                                    entryPulseScale = 1f,
                                    onPickLetter = picturePick@{ picked ->
                                        if (!consumeTapCooldown()) return@picturePick
                                        cancelFeedbackVoice()
                                        if (audioEnabled && (
                                                ((chapterId == 3 || chapterId == 6) && stationId == 1) ||
                                                    (chapterId == 2 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE)
                                            )
                                        ) {
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
                                                        if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
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
                                                if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
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
                                if (stationUiSpec.templateId == StationTemplateId.ImageToWord) {
                                    ImageToWordQuestionRenderer(
                                        current = current,
                                        contentKey = session.currentIndex,
                                        enabled = gameChoicesEnabled,
                                        entryPulseScale = entryPulseScale.value,
                                        optionsShakePx = optionsShake.value,
                                        instructionText =
                                            stationUiSpec.imageToWordInstructionText
                                                ?: StationInstructionCopy.Chapter3ImageToWord,
                                        onPictureTapReplayWord =
                                            if (audioEnabled) {
                                                {
                                                    cancelFeedbackVoice()
                                                    val id = current.correctChoiceId
                                                    val clip =
                                                        if (chapterId == 3 || chapterId == 6) {
                                                            val ch3Clip = "audio/ch3_word_${id}.wav"
                                                            if (voice.hasAsset(ch3Clip)) ch3Clip else AudioClips.wordClipByCatalogId(id)
                                                        } else {
                                                            AudioClips.wordClipByCatalogId(id)
                                                        }
                                                    if (voice.hasAsset(clip)) {
                                                        feedbackVoiceJob = scope.launch { voice.playBlocking(clip) }
                                                    }
                                                }
                                            } else {
                                                null
                                            },
                                        onWordPressed = ch3ImgWord@{ choiceId ->
                                            if (!audioEnabled) return@ch3ImgWord
                                            cancelFeedbackVoice()
                                            feedbackVoiceJob =
                                                scope.launch {
                                                    val clip =
                                                        if (chapterId == 3 || chapterId == 6) {
                                                            val ch3Clip = "audio/ch3_word_${choiceId}.wav"
                                                            if (voice.hasAsset(ch3Clip)) ch3Clip else AudioClips.wordClipByCatalogId(choiceId)
                                                        } else {
                                                            AudioClips.wordClipByCatalogId(choiceId)
                                                        }
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
                                                            val clip =
                                                                if (chapterId == 3 || chapterId == 6) {
                                                                    val ch3Clip = "audio/ch3_word_${choiceId}.wav"
                                                                    if (voice.hasAsset(ch3Clip)) ch3Clip else AudioClips.wordClipByCatalogId(choiceId)
                                                                } else {
                                                                    AudioClips.wordClipByCatalogId(choiceId)
                                                                }
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
                                    stationUiSpec.templateId == StationTemplateId.MatchLetterToWord
                                ) {
                                    // Same three picture cards as this round's ImageMatch question (station 5 generator/shape).
                                    val matchChoices = current.choices
                                    fun speakNow(play: suspend () -> Unit) {
                                        if (!audioEnabled) return
                                        // Station 6 request: stop the current voice immediately, then speak the newly pressed item.
                                        cancelFeedbackVoice()
                                        feedbackVoiceJob = scope.launch { play() }
                                    }
                                    var lastPraiseClip by remember(chapterId, stationId) { mutableStateOf<String?>(null) }
                                    MatchLetterToWordQuestionRenderer(
                                        choices = matchChoices,
                                        stationUiSpec = stationUiSpec,
                                        isCompactLandscapePhone = isCompactLandscapePhone,
                                        choicePairLimit = 3,
                                        contentKey = session.currentIndex,
                                        enabled = gameChoicesEnabled,
                                        entryPulseScale = entryPulseScale.value,
                                        letterTileSizeMultiplier = if (chapterId == TrainingV1Config.CHAPTER_ID) 1.10f else 1f,
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
                                            Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                                        },
                                        captionSizeMultiplier = plan.imageMatchCaptionSizeMultiplier,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        instructionReadablePanel = stationUiSpec.matchLetterInstructionReadablePanel,
                                        instructions =
                                            stationUiSpec.matchLetterInstructionText
                                                ?: StationInstructionCopy.MatchLetterFinale,
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
                                                            val candidates =
                                                                listOf(
                                                                    AudioClips.VoPraiseMetzuyan,
                                                                    AudioClips.VoPraiseYofi,
                                                                    AudioClips.VoPraiseHitzlacht,
                                                                    AudioClips.VoNice1,
                                                                    AudioClips.VoGoodJob2,
                                                                    AudioClips.VoGoodJob1,
                                                                ).filter { it != lastPraiseClip }
                                                            val pickFrom = if (candidates.isNotEmpty()) candidates else listOfNotNull(lastPraiseClip)
                                                            val picked = pickFrom.shuffled().firstOrNull()
                                                            if (picked != null) {
                                                                lastPraiseClip = picked
                                                                voice.playBlocking(picked)
                                                            }
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
                                    )
                                } else {
                                    val listenOnlyTemporaryHintLetter =
                                        if (episode4HelpSt15 &&
                                            stationUiSpec.templateId == StationTemplateId.ImageMatch &&
                                            stationUiSpec.hintMode == com.tal.hebrewdino.ui.domain.StationHintMode.TemporaryTargetLetter
                                        ) {
                                            episode4Help.activeHintLetter
                                        } else {
                                            null
                                        }
                                    ImageMatchQuestionRenderer(
                                        current = current,
                                        stationUiSpec = stationUiSpec,
                                        isCompactLandscapePhone = isCompactLandscapePhone,
                                        headerInstructionFontScale =
                                            (if (chapterId == TrainingV1Config.CHAPTER_ID) 1.35f else 1.35f * 2f),
                                        listenOnlyTemporaryHintLetter = listenOnlyTemporaryHintLetter,
                                        contentKey = session.currentIndex,
                                        enabled = gameChoicesEnabled,
                                        shakePx = optionsShake.value,
                                        entryPulseEpoch = entryPulseEpoch,
                                        hintCorrectChoiceId = current.correctChoiceId.takeIf { wrongTapsThisQuestion >= 2 },
                                        hintPulseEpoch = hintPulseEpoch,
                                        captionSizeMultiplier = plan.imageMatchCaptionSizeMultiplier,
                                        pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                                        innerPictureScaleForChoice = { choice ->
                                            Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
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
                                                            if (chapterId != 3 && chapterId != 6) {
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
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            is Question.FinaleSlotQuestion ->
                                FinaleSlotQuestionRenderer(
                                    current = current,
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
                    val fullScreenBalloonHintLetter =
                        when {
                            episode4HelpSt15 -> episode4Help.activeHintLetter
                            popBalloonsHelpControlsEnabled -> balloonHelp.hintLetter
                            else -> null
                        }
                    if (phase == GamePhase.Play &&
                        fullScreenBalloonHintLetter != null &&
                        current is Question.PopBalloonsQuestion &&
                        stationUiSpec.templateId == StationTemplateId.PopBalloons &&
                        !stationUiSpec.excludeFullScreenBalloonHintOverlay
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().zIndex(3f),
                            contentAlignment = Alignment.Center,
                        ) {
                            TargetLetterHeaderChip(letter = fullScreenBalloonHintLetter!!)
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
            if (!isCompactLandscapePhone) {
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
        }
        val sideHelpControls: SideHelpControls? =
            when {
                episode4HelpSt15 ->
                    SideHelpControls(
                        replayEnabled = phase == GamePhase.Play,
                        hintEnabled =
                            phase == GamePhase.Play &&
                                !episode4Help.hintLocksChoices &&
                                stationUiSpec.hintMode != com.tal.hebrewdino.ui.domain.StationHintMode.None,
                        onReplay = { performSideHelpReplay() },
                        onHint = { performSideHelpHint() },
                    )
                popBalloonsHelpControlsEnabled ->
                    SideHelpControls(
                        replayEnabled = phase == GamePhase.Play,
                        hintEnabled = phase == GamePhase.Play && !balloonHelp.hintLocksChoices,
                        onReplay = { performSideHelpReplay() },
                        onHint = { performSideHelpHint() },
                    )
                else -> null
            }
        if (sideHelpControls != null) {
            Episode4Stations15HelpColumn(
                replayEnabled = sideHelpControls.replayEnabled,
                hintEnabled = sideHelpControls.hintEnabled,
                onReplay = sideHelpControls.onReplay,
                onHint = sideHelpControls.onHint,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp, top = 100.dp, bottom = 96.dp)
                        .zIndex(6f),
            )
        }
        if ((chapterId == 3 || chapterId == 6) && stationId == 5 && !episode4HelpSt15) {
            Chapter3Station5ReplayColumn(
                replayEnabled = phase == GamePhase.Play,
                onReplayLetter = {
                    if (!audioEnabled || phase != GamePhase.Play) return@Chapter3Station5ReplayColumn
                    val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
                    cancelFeedbackVoice()
                    val letterClip = AudioClips.letterNameClip(q.correctAnswer) ?: return@Chapter3Station5ReplayColumn
                    if (voice.hasAsset(letterClip)) {
                        feedbackVoiceJob = scope.launch { voice.playBlocking(letterClip) }
                    }
                },
                onReplayFull = {
                    if (!audioEnabled || phase != GamePhase.Play) return@Chapter3Station5ReplayColumn
                    val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
                    cancelFeedbackVoice()
                    feedbackVoiceJob =
                        scope.launch {
                            val instruction = AudioClips.VoChooseLetter
                            val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                            val parts =
                                buildList {
                                    if (voice.hasAsset(instruction)) add(instruction)
                                    if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
                                }
                            if (parts.isNotEmpty()) voice.playSequenceBlocking(*parts.toTypedArray())
                        }
                },
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp, top = 100.dp, bottom = 96.dp)
                        .zIndex(6f),
            )
        }
    }
}

