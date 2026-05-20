package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.feedback.GameFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

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
                withTimeoutOrNull(5000L) { audioRuntime.feedbackVoiceJob?.join() }
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
            GameAudioActions.launchFeedbackVoice(
                audioEnabled = true,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
                cancelBeforeStart = false,
            ) {
                voice.playFirstAvailableBlocking(*arr)
            }
        } else if (otherPraiseEligible) {
            if (sagaUsesFindGridAudioStaging) {
                withTimeoutOrNull(5000L) { audioRuntime.feedbackVoiceJob?.join() }
            }
            cancelFeedbackVoice()
            GameAudioActions.launchFeedbackVoice(
                audioEnabled = true,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
                cancelBeforeStart = false,
            ) {
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
            withTimeoutOrNull(8000) { audioRuntime.feedbackVoiceJob?.join() }
            gameViewModel.station2PinnedBalloonLetter = null
            gameViewModel.station2PinnedBalloonColor = null
        } else if (sagaEpisode && (sagaUsesFindGridAudioStaging || stationId == Chapter1StationOrder.PICTURE_PICK_ONE)) {
            withTimeoutOrNull(8000) { audioRuntime.feedbackVoiceJob?.join() }
        }
        contentAlpha.animateTo(0f, tween(BetweenQuestionFadeMs))
        if (!waitPraiseBeforeFade) {
            withTimeoutOrNull(2500) { audioRuntime.feedbackVoiceJob?.join() }
        }
        delay(5)
        session.nextQuestion()
        if (session.currentQuestion == null && !gameViewModel.completionCallbackFired) {
            gameViewModel.completionCallbackFired = true
            onComplete(stationId, session.correctCount, session.mistakeCount)
        }
        contentAlpha.animateTo(1f, tween(BetweenQuestionFadeMs))
    }
}
