package com.tal.hebrewdino.ui.screens

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.AppAnalytics
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
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
            gameViewModel.dinoVisual = DinoVisual.Jump
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
        if (audioEnabled && chapterId == 6) {
            GameAudioActions.awaitTrackedVoices(audioRuntime, 10000L)
            cancelFeedbackVoice()
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
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
            gameViewModel.station2PinnedBalloonLetter = null
            gameViewModel.station2PinnedBalloonColor = null
        } else if (sagaEpisode && (sagaUsesFindGridAudioStaging || stationId == Chapter1StationOrder.PICTURE_PICK_ONE)) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 8000L)
        }
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        if (!waitPraiseBeforeFade) {
            GameAudioActions.awaitFeedbackVoice(audioRuntime, 2500L)
        }
        delay(5)
        session.nextQuestion()
        if (session.currentQuestion == null && !gameViewModel.completionCallbackFired) {
            gameViewModel.completionCallbackFired = true
            val timeTakenSeconds =
                ((SystemClock.elapsedRealtime() - gameViewModel.stationStartMs) / 1000L)
                    .coerceAtLeast(0L)
            AppAnalytics.logLevelComplete(
                chapterId = chapterId,
                stationId = stationId,
                timeTakenSeconds = timeTakenSeconds,
            )
            if (audioEnabled) {
                GameAudioActions.finishStationVoiceBeforeReward(
                    audioRuntime = audioRuntime,
                    cancelFeedbackVoice = cancelFeedbackVoice,
                )
            }
            onComplete(stationId, session.correctCount, session.mistakeCount)
        }
        contentAlpha.animateTo(1f, tween(BetweenQuestionFadeMs))
    }
}
