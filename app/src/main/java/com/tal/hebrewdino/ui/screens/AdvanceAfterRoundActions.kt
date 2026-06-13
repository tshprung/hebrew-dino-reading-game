package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Season1StationAudio
import com.tal.hebrewdino.ui.domain.SixStationArcQaPolicy
import com.tal.hebrewdino.ui.domain.Season2PostFocusCorrectPolicy
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.Season2StationQaPolicy
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.feedback.GameFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private val Episode1PraiseCandidates =
    arrayOf(
        AudioClips.VoKolHakavod,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
    )

private val OtherPraiseCandidates =
    arrayOf(
        AudioClips.VoKolHakavod,
        AudioClips.VoGoodJob1,
    )

internal object AdvanceAfterRoundActions {
    suspend fun run(
        scope: CoroutineScope,
        gameViewModel: GameViewModel,
        audioEnabled: Boolean,
        sagaEpisode: Boolean,
        chapterId: Int,
        stationId: Int,
        season2Chapter1UxStationId: Int? = null,
        isSeason2QuizChapter: Boolean = false,
        suppressAdvanceRoundNarratorPraiseAfterPostFocusCompanion: Boolean = false,
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
        rawVoice: RawVoicePlayer?,
        cancelFeedbackVoice: () -> Unit,
        audioRuntime: GameAudioRuntimeState,
        dinoForward: Animatable<Float, AnimationVector1D>,
        forwardDir: Float,
        dinoScale: Animatable<Float, AnimationVector1D>,
        contentAlpha: Animatable<Float, AnimationVector1D>,
        session: LevelSession,
        onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
        onLevelCompleteHook: () -> Unit,
    ) {
        gameViewModel.inputLocked = true
        if (audioEnabled && !ch3SpellMidWord) onLevelCompleteHook()
        val trainingMatchLetterStation =
            chapterId == TrainingV1Config.CHAPTER_ID &&
                stationId == TrainingV1Config.STATION_MATCH_LETTER_TO_WORD
        val finaleMatchLetterStation =
            sagaEpisode && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
        val stationUiSpec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
        val dragWordToPictureStation =
            stationUiSpec.templateId == StationTemplateId.DragWordToPicture
        val stableDragStation =
            dragWordToPictureStation ||
                (
                    stationUiSpec.templateId == StationTemplateId.DragMissingLetter &&
                        Season1StationAudio.isSeason1DragMissingLetterStation(chapterId, stationId)
                )
        val skipInterRoundFeedback =
            Season2StationQaPolicy.shouldSkipAdvanceRoundInterRoundFeedback(
                gameplayChapterId = chapterId,
                season2UxStationId = season2Chapter1UxStationId,
                isLast = isLast,
            )
        if (audioEnabled) {
            when {
                skipInterRoundFeedback -> Unit
                sagaUsesPickLetterAudioStaging && isLast && chapterId != 3 -> gameFeedback.playCorrect()
                sagaUsesPopBalloonsAudioStaging -> Unit
                sagaUsesFindGridAudioStaging -> Unit
                finaleMatchLetterStation || trainingMatchLetterStation -> Unit
                isLast -> gameFeedback.playSuccessBig()
                else -> gameFeedback.playCorrect()
            }
        }
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord) && !stableDragStation) {
            gameViewModel.dinoVisual = DinoVisual.Jump
        }
        val suppressSagaAdvancePraise =
            Season2StationQaPolicy.shouldSuppressSagaEpisodeAdvancePraise(isSeason2QuizChapter)
        val suppressNarratorPraiseAfterPostFocus =
            Season2PostFocusCorrectPolicy.shouldSuppressAdvanceRoundNarratorPraise(
                suppressAdvanceRoundNarratorPraiseAfterPostFocusCompanion,
            )
        val episode1PraiseEligible =
            audioEnabled &&
                sagaEpisode &&
                !suppressSagaAdvancePraise &&
                !suppressNarratorPraiseAfterPostFocus &&
                stationId in 2..5 &&
                !SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId) &&
                !isChapter3AudioLetterRecognitionStation &&
                !Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                    chapterId,
                    season2Chapter1UxStationId,
                ) &&
                Random.nextFloat() < Episode1PraiseChance
        /** Ch3/Ch6 st5, Ch6 st6 already play praise in action handlers. */
        val inStationPraiseAlreadyPlayed =
            isChapter3AudioLetterRecognitionStation ||
                (chapterId == 6 && stationId == 6) ||
                Season2StationAudio.isPictureToWordStation(chapterId, stationId) ||
                Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                    chapterId,
                    season2Chapter1UxStationId,
                ) ||
                Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                    chapterId,
                    season2Chapter1UxStationId,
                )
        val otherPraiseEligible =
            audioEnabled &&
                !suppressSagaAdvancePraise &&
                !suppressNarratorPraiseAfterPostFocus &&
                !finaleMatchLetterStation &&
                !trainingMatchLetterStation &&
                !(sagaUsesPickLetterAudioStaging) &&
                !(sagaEpisode && SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId)) &&
                !inStationPraiseAlreadyPlayed &&
                !episode1PraiseEligible

        if (episode1PraiseEligible) {
            if (sagaUsesFindGridAudioStaging) {
                GameAudioActions.awaitFeedbackVoice(audioRuntime, 5000L)
            }
            GameAudioActions.launchFeedbackVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) {
                GameAudioActions.playPraiseNoImmediateRepeat(
                    voice = voice,
                    audioRuntime = audioRuntime,
                    candidates = Episode1PraiseCandidates,
                    chapterId = chapterId,
                    stationId = stationId,
                    context = "AdvanceAfterRoundActions.playPraise(episode1)",
                    rawVoice = rawVoice,
                )
            }
        } else if (otherPraiseEligible) {
            if (sagaUsesFindGridAudioStaging) {
                GameAudioActions.awaitFeedbackVoice(audioRuntime, 5000L)
            }
            GameAudioActions.launchFeedbackVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) {
                GameAudioActions.playPraiseNoImmediateRepeat(
                    voice = voice,
                    audioRuntime = audioRuntime,
                    candidates = OtherPraiseCandidates,
                    chapterId = chapterId,
                    stationId = stationId,
                    context = "AdvanceAfterRoundActions.playPraise(other)",
                    rawVoice = rawVoice,
                )
            }
        }
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord) && !stableDragStation) {
            dinoForward.animateTo(dinoForward.value + forwardDir * 12f, spring(dampingRatio = 0.75f, stiffness = 520f))
        }
        val strongerSuccessPulse =
            (sagaUsesPickLetterAudioStaging || sagaUsesFindGridAudioStaging) &&
                !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)
        val station456SuccessPulse =
            (sagaEpisode &&
                (SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId) ||
                    SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(chapterId, stationId) ||
                    stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)) ||
                trainingMatchLetterStation
        if (!(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord) && !stableDragStation) {
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
        if (audioEnabled && chapterId == 6) {
            GameAudioActions.awaitTrackedVoices(audioRuntime, 10000L)
            cancelFeedbackVoice()
        }
        val tightBetweenRounds =
            Season2StationQaPolicy.useTightBetweenRoundTiming(chapterId, season2Chapter1UxStationId)
        delay(
            when {
                ch3SpellMidWord -> 38
                tightBetweenRounds -> 40
                sagaUsesFindGridAudioStaging -> 120
                else -> 170
            }.milliseconds,
        )
        val waitPraiseBeforeFade =
            sagaEpisode &&
                (sagaUsesPopBalloonsAudioStaging ||
                    sagaUsesFindGridAudioStaging ||
                    SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId)) ||
                (isSeason2QuizChapter && sagaUsesPickLetterAudioStaging)
        if (sagaUsesPopBalloonsAudioStaging) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
            gameViewModel.station2PinnedBalloonLetter = null
            gameViewModel.station2PinnedBalloonColor = null
        } else if (
            sagaEpisode &&
                (
                    sagaUsesFindGridAudioStaging ||
                        SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId)
                )
        ) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
        } else if (isSeason2QuizChapter && sagaUsesPickLetterAudioStaging) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
        }
        val betweenFadeMs =
            when {
                stableDragStation -> 0
                tightBetweenRounds -> 40
                else -> BetweenQuestionFadeMs
            }
        if (!stableDragStation) {
            contentAlpha.animateTo(0f, tween(betweenFadeMs))
        }
        if (!waitPraiseBeforeFade && !tightBetweenRounds) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 2500L)
        }
        if (!tightBetweenRounds) {
            delay(5.milliseconds)
        }
        // Stable drag stations skip content fade; hide play UI before advancing so the next
        // question is not composited for a frame while phase is still Play.
        if (stableDragStation) {
            gameViewModel.phase = GamePhase.Intro
        }
        session.nextQuestion()
        if (session.currentQuestion == null && !gameViewModel.completionCallbackFired) {
            gameViewModel.completionCallbackFired = true
            if (audioEnabled) {
                GameAudioActions.finishStationVoiceBeforeReward(
                    audioRuntime = audioRuntime,
                    cancelFeedbackVoice = cancelFeedbackVoice,
                )
            }
            onComplete(stationId, session.correctCount, session.mistakeCount)
        }
        if (!stableDragStation) {
            contentAlpha.animateTo(1f, tween(betweenFadeMs))
        }
    }
}
