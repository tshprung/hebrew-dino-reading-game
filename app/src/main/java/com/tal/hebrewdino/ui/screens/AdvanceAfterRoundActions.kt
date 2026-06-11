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
import com.tal.hebrewdino.ui.domain.Season2Ch1QaPolicy
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.feedback.GameFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
        val skipInterRoundFeedback =
            Season2Ch1QaPolicy.shouldSkipAdvanceRoundInterRoundFeedback(
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
        if (!suppressInGameDinoProgress && !(isChapter3HighlightedLetterInWordStation && ch3SpellMidWord)) {
            gameViewModel.dinoVisual = DinoVisual.Jump
        }
        val episode1PraiseEligible =
            audioEnabled &&
                sagaEpisode &&
                stationId in 2..5 &&
                stationId != Chapter1StationOrder.PICTURE_PICK_ONE &&
                !isChapter3AudioLetterRecognitionStation &&
                Random.nextFloat() < Episode1PraiseChance
        /** Ch3/Ch6 st5, Ch6 st6, and Training st1/2/3 already play praise in action handlers. */
        val trainingInStationPraiseAlreadyPlayed =
            chapterId == TrainingV1Config.CHAPTER_ID &&
                (stationId == TrainingV1Config.STATION_HEAR_LETTER_CHOOSE ||
                    stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER ||
                    stationId == TrainingV1Config.STATION_PICTURE_CHOOSE_WORD)
        val inStationPraiseAlreadyPlayed =
            isChapter3AudioLetterRecognitionStation ||
                (chapterId == 6 && stationId == 6) ||
                trainingInStationPraiseAlreadyPlayed ||
                Season2StationAudio.isPictureToWordStation(chapterId, stationId) ||
                Season2Ch1QaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(season2Chapter1UxStationId)
        val otherPraiseEligible =
            audioEnabled &&
                !finaleMatchLetterStation &&
                !trainingMatchLetterStation &&
                !(sagaUsesPickLetterAudioStaging) &&
                !(sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) &&
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
                    stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)) ||
                trainingMatchLetterStation
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
        if (audioEnabled && chapterId == 6) {
            GameAudioActions.awaitTrackedVoices(audioRuntime, 10000L)
            cancelFeedbackVoice()
        }
        val tightBetweenRounds =
            Season2Ch1QaPolicy.useTightBetweenRoundTiming(season2Chapter1UxStationId)
        delay(
            when {
                ch3SpellMidWord -> 38
                tightBetweenRounds -> 40
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
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
            gameViewModel.station2PinnedBalloonLetter = null
            gameViewModel.station2PinnedBalloonColor = null
        } else if (sagaEpisode && (sagaUsesFindGridAudioStaging || stationId == Chapter1StationOrder.PICTURE_PICK_ONE)) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
        }
        val betweenFadeMs = if (tightBetweenRounds) 40 else BetweenQuestionFadeMs
        contentAlpha.animateTo(0f, tween(betweenFadeMs))
        if (!waitPraiseBeforeFade && !tightBetweenRounds) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 2500L)
        }
        if (!tightBetweenRounds) {
            delay(5)
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
        contentAlpha.animateTo(1f, tween(betweenFadeMs))
    }
}
