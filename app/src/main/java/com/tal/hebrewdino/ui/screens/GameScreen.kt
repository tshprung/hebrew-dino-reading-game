package com.tal.hebrewdino.ui.screens

import android.os.SystemClock
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.GameAudioEngine
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.Chapter3Station5ReplayColumn
import com.tal.hebrewdino.ui.components.Episode4Stations15HelpColumn
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.InstructionPanelStyle
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationInstructionCopy
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.StationVariant
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.domain.hasVariant
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

internal enum class GamePhase { Intro, Play }

internal enum class DinoVisual { Idle, TryAgain, Jump }

private const val IntroDurationMs = 450L

private const val BetweenQuestionFadeMs = 80

/** Chapters that use the shared six-station journey ([Chapter1StationOrder]); intros, art, and letter pools differ. */
private val SixStationArcChapterRange = 1..5

internal fun isSagaEpisode(chapterId: Int): Boolean = chapterId in SixStationArcChapterRange

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
    /** Replay of an already-completed station: no extra in-game dino motion after correct answers. */
    suppressInGameDinoProgress: Boolean = false,
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
    val episode4HelpSt15 = Episode4Help.isHelpColumnActive(stationUiSpec)
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
    fun registerWrongTapForHintPulse() {
        wrongTapsThisQuestion += 1
        if (wrongTapsThisQuestion >= 2) hintPulseEpoch += 1
    }

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
        GameAudioPreloader.preloadStation1(
            audioEnabled = audioEnabled,
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            chapterId = chapterId,
            letterPoolSpec = letterPoolSpec,
            voice = voice,
            sfx = sfx,
        )
    }

    // Episode 1 station 2: preload instruction + balloon feedback clips for low latency.
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadPopBalloons(
            audioEnabled = audioEnabled,
            usesPopBalloonsSoundPoolPrompt = usesPopBalloonsSoundPoolPrompt,
            letterPoolSpec = letterPoolSpec,
            sfx = sfx,
        )
    }

    // Episode 1–4 station 3: warm instruction (MediaPlayer); preload tap SFX + letter clips for SoundPool.
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadFindGrid(
            audioEnabled = audioEnabled,
            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
            letterPoolSpec = letterPoolSpec,
            voice = voice,
            sfx = sfx,
        )
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

    fun handleImageToWordAttempt(choiceId: String): Boolean {
        if (!consumeTapCooldown()) return false
        cancelFeedbackVoice()
        return when (session.submitImageMatch(choiceId)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                scope.launch {
                    if (audioEnabled) {
                        val clip =
                            AudioClips.imageToWordClipByCatalogId(
                                catalogEntryId = choiceId,
                                chapterId = chapterId,
                                voiceHasAsset = { path -> voice.hasAsset(path) },
                            )
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
                registerWrongTapForHintPulse()
                onWrongFeedback(wrongWordCatalogId = choiceId)
                false
            }
            AnswerResult.Finished -> false
        }
    }

    fun handleImageToWordReplayCorrectChoice() {
        if (!audioEnabled) return
        cancelFeedbackVoice()
        val q = session.currentQuestion as? Question.ImageMatchQuestion ?: return
        val clip =
            AudioClips.imageToWordClipByCatalogId(
                catalogEntryId = q.correctChoiceId,
                chapterId = chapterId,
                voiceHasAsset = { path -> voice.hasAsset(path) },
            )
        if (voice.hasAsset(clip)) {
            feedbackVoiceJob = scope.launch { voice.playBlocking(clip) }
        }
    }

    fun handleImageToWordWordPressed(choiceId: String) {
        if (!audioEnabled) return
        cancelFeedbackVoice()
        feedbackVoiceJob =
            scope.launch {
                val clip =
                    AudioClips.imageToWordClipByCatalogId(
                        catalogEntryId = choiceId,
                        chapterId = chapterId,
                        voiceHasAsset = { path -> voice.hasAsset(path) },
                    )
                voice.playBlocking(clip)
            }
    }

    fun handleImageMatchAttempt(choiceId: String): Boolean {
        if (!consumeTapCooldown()) return false
        cancelFeedbackVoice()
        return when (session.submitImageMatch(choiceId)) {
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
                registerWrongTapForHintPulse()
                if (isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                    onWrongFeedback(wrongWordCatalogId = choiceId)
                } else {
                    onWrongFeedback()
                }
                false
            }
            AnswerResult.Finished -> false
        }
    }

    @Composable
    fun Chapter3Station5ReplayOverlay(modifier: Modifier) {
        if (!((chapterId == 3 || chapterId == 6) && stationId == 5 && !episode4HelpSt15)) return
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
            modifier = modifier,
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
                    GameQuestionHost(
                        phase = phase,
                        stationUiSpec = stationUiSpec,
                        stationId = stationId,
                        chapterId = chapterId,
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
                        episode4HelpSt15 = episode4HelpSt15,
                        episode4HelpActiveHintLetter = episode4Help.activeHintLetter,
                        episode4HelpStation2BalloonHintEpoch = episode4Help.station2BalloonHintEpoch,
                        episode4HelpStation3GridHintEpoch = episode4Help.station3GridHintEpoch,
                        popBalloonsHelpControlsEnabled = popBalloonsHelpControlsEnabled,
                        balloonHelpHintLetter = balloonHelp.hintLetter,
                        showPopBalloonsTargetLetterChip = showPopBalloonsTargetLetterChip,
                        station1PinnedCorrectLetter = station1PinnedCorrectLetter,
                        station2PinnedBalloonLetter = station2PinnedBalloonLetter,
                        station2PinnedBalloonColor = station2PinnedBalloonColor,
                        hintHeaderScale = hintHeaderScale.value,
                        enabled = gameChoicesEnabled,
                        shakeEpoch = shakeEpoch,
                        wrongTapsThisQuestion = wrongTapsThisQuestion,
                        hintPulseEpoch = hintPulseEpoch,
                        correctTapPulseLetter = correctTapPulseLetter,
                        correctTapPulseEpoch = correctTapPulseEpoch,
                        station4WrongFlashLetter = station4WrongFlashLetter,
                        station4WrongFlashEpoch = station4WrongFlashEpoch,
                        station4PinnedCorrectLetter = station4PinnedCorrectLetter,
                        entryPulseEpoch = entryPulseEpoch,
                        entryPulseScale = entryPulseScale.value,
                        optionsShakePx = optionsShake.value,
                        session = session,
                        scope = scope,
                        voice = voice,
                        sfx = sfx,
                        cancelFeedbackVoice = { cancelFeedbackVoice() },
                        getFeedbackVoiceJob = { feedbackVoiceJob },
                        setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
                        consumeTapCooldown = { consumeTapCooldown() },
                        performSideHelpReplay = { performSideHelpReplay() },
                        handleFindGridSagaGridLetterTapped = { tapped, question ->
                            FindGridActions.handleSagaGridLetterTapped(
                                audioEnabled = audioEnabled,
                                tapped = tapped,
                                question = question,
                                scope = scope,
                                sfx = sfx,
                                setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
                                setStation3VoiceStreamId = { id -> station3VoiceStreamId = id },
                            )
                        },
                        handleFindGridCellTapped = { index, question ->
                            FindGridActions.handleCellTapped(
                                consumeTapCooldown = { consumeTapCooldown() },
                                sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                                cancelFeedbackVoice = { cancelFeedbackVoice() },
                                session = session,
                                bumpShakeEpoch = { shakeEpoch += 1 },
                                registerWrongTapForHintPulse = { registerWrongTapForHintPulse() },
                                onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken ->
                                    onWrongFeedback(
                                        wrongPickedLetter = wrongPickedLetter,
                                        wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                    )
                                },
                                index = index,
                                question = question,
                            )
                        },
                        handleFindGridCompleted = {
                            FindGridActions.handleCompleted(
                                consumeTapCooldown = { consumeTapCooldown() },
                                scope = scope,
                                session = session,
                                advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            )
                        },
                        handlePickLetterPick = { picked ->
                            PickLetterActions.handlePick(
                                picked = picked,
                                consumeTapCooldown = { consumeTapCooldown() },
                                cancelFeedbackVoice = { cancelFeedbackVoice() },
                                audioEnabled = audioEnabled,
                                sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                                isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                                isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                                session = session,
                                scope = scope,
                                voice = voice,
                                sfx = sfx,
                                setCorrectTapPulse = { letter ->
                                    correctTapPulseLetter = letter
                                    correctTapPulseEpoch += 1
                                },
                                setStation1PinnedCorrectLetter = { letter -> station1PinnedCorrectLetter = letter },
                                getFeedbackVoiceJob = { feedbackVoiceJob },
                                setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
                                bumpShakeEpoch = { shakeEpoch += 1 },
                                registerWrongTapForHintPulse = { registerWrongTapForHintPulse() },
                                onWrongFeedback = { wrongPickedLetter ->
                                    onWrongFeedback(wrongPickedLetter = wrongPickedLetter)
                                },
                                advanceAfterRound = { isLast, ch3SpellMidWord ->
                                    advanceAfterRound(isLast, ch3SpellMidWord = ch3SpellMidWord)
                                },
                            )
                        },
                        handlePopBalloonsPopSfx = { letter, isCorrect, finalCorrectBalloon, balloonIndex ->
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
                                cancelFeedbackVoice = { cancelFeedbackVoice() },
                                setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
                                getStation2VoiceStreamId = { station2VoiceStreamId },
                                setStation2VoiceStreamId = { id -> station2VoiceStreamId = id },
                                nextStation2CorrectPopVariant = {
                                    val v = station2CorrectPopCount
                                    station2CorrectPopCount += 1
                                    v
                                },
                                station2PopTailPaddingMs = Station2PopTailPaddingMs,
                                station2PopFallbackDurationMs = Station2PopFallbackDurationMs,
                            )
                        },
                        handlePopBalloonsWrongPick = {
                            PopBalloonsActions.handleWrongPick(
                                consumeTapCooldown = { consumeTapCooldown() },
                                sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                                cancelFeedbackVoice = { cancelFeedbackVoice() },
                                onWrongFeedback = { onWrongFeedback() },
                                session = session,
                                bumpShakeEpoch = { shakeEpoch += 1 },
                                registerWrongTapForHintPulse = { registerWrongTapForHintPulse() },
                                chapterId = chapterId,
                                stationId = stationId,
                                scope = scope,
                                optionsShake = optionsShake,
                                setInputLocked = { locked -> inputLocked = locked },
                                setDinoVisual = { v -> dinoVisual = v },
                            )
                        },
                        handlePopBalloonsAllCorrectPopped = { lastLetter, poppedBalloonColor, popAll ->
                            PopBalloonsActions.handleAllCorrectPopped(
                                lastLetter = lastLetter,
                                poppedBalloonColor = poppedBalloonColor,
                                isChapter3PopAllLettersStation = popAll,
                                sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                                chapterId = chapterId,
                                audioEnabled = audioEnabled,
                                cancelFeedbackVoice = { cancelFeedbackVoice() },
                                setStation2PinnedBalloon = { letter, color ->
                                    station2PinnedBalloonLetter = letter
                                    station2PinnedBalloonColor = color
                                },
                                session = session,
                                scope = scope,
                                getFeedbackVoiceJob = { feedbackVoiceJob },
                                advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                            )
                        },
                        handlePictureStartsWithPick = { picked ->
                            PictureStartsWithActions.handlePick(
                                picked = picked,
                                consumeTapCooldown = { consumeTapCooldown() },
                                cancelFeedbackVoice = { cancelFeedbackVoice() },
                                audioEnabled = audioEnabled,
                                chapterId = chapterId,
                                stationId = stationId,
                                sagaEpisode = isSagaEpisode(chapterId),
                                session = session,
                                scope = scope,
                                voice = voice,
                                getFeedbackVoiceJob = { feedbackVoiceJob },
                                setFeedbackVoiceJob = { job -> feedbackVoiceJob = job },
                                setStation4PinnedCorrectLetter = { letter -> station4PinnedCorrectLetter = letter },
                                setCorrectTapPulse = { letter ->
                                    correctTapPulseLetter = letter
                                    correctTapPulseEpoch += 1
                                },
                                advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                                registerWrongTapForHintPulse = { registerWrongTapForHintPulse() },
                                flashStation4WrongLetter = { letter ->
                                    station4WrongFlashLetter = letter
                                    station4WrongFlashEpoch += 1
                                },
                                onWrongFeedback = { wrongPickedLetter, wrongPickedLetterAlreadySpoken ->
                                    onWrongFeedback(
                                        wrongPickedLetter = wrongPickedLetter,
                                        wrongPickedLetterAlreadySpoken = wrongPickedLetterAlreadySpoken,
                                    )
                                },
                            )
                        },
                        handleImageToWordReplayCorrectChoice = { handleImageToWordReplayCorrectChoice() },
                        handleImageToWordWordPressed = { choiceId -> handleImageToWordWordPressed(choiceId) },
                        handleImageToWordAttempt = { choiceId -> handleImageToWordAttempt(choiceId) },
                        handleImageMatchAttempt = { choiceId -> handleImageMatchAttempt(choiceId) },
                        handleFinaleWrongPlacement = {
                            session.wrongTap()
                            shakeEpoch += 1
                        },
                        onWrongFeedback = { onWrongFeedback() },
                        advanceAfterRound = { isLast -> advanceAfterRound(isLast) },
                    )
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
        Chapter3Station5ReplayOverlay(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp, top = 100.dp, bottom = 96.dp)
                    .zIndex(6f),
        )
    }
}

