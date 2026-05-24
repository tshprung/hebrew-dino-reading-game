package com.tal.hebrewdino.ui.screens

import android.os.SystemClock
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SagaPictureStartsWithPraiseCandidates =
    arrayOf(
        AudioClips.VoKolHakavod,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
    )

internal object PictureStartsWithActions {
    private const val Chapter4Station4MinCorrectDisplayMs: Long = 650L

    fun handlePick(
        picked: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaEpisode: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        if (audioEnabled &&
            (
                ((chapterId == 3 || chapterId == 6) && stationId == 1) ||
                    (chapterId == 2 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE)
            )
        ) {
            val clip = AudioClips.letterNameClip(picked)
            if (clip != null && voice.hasAsset(clip)) {
                GameAudioActions.launchFeedbackVoiceNoCancel(
                    audioEnabled = true,
                    scope = scope,
                    audioRuntime = audioRuntime,
                ) {
                    voice.playBlocking(clip)
                }
            }
        }
        when (session.submitPictureStartsWith(picked)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                val shouldPinCorrectLetter =
                    (sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) ||
                        (chapterId == 3 && stationId == 1) ||
                        (chapterId == 6 && stationId == 1)
                if (shouldPinCorrectLetter) {
                    gameViewModel.station4PinnedCorrectLetter = picked
                }
                if (sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                    scope.launch {
                        val tapAtMs = SystemClock.uptimeMillis()
                        gameViewModel.correctTapPulseLetter = picked
                        gameViewModel.correctTapPulseEpoch += 1
                        cancelFeedbackVoice()
                        val letterName = AudioClips.letterNameClip(picked)
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                if (letterName != null && voice.hasAsset(letterName)) {
                                    voice.playBlocking(letterName)
                                }
                                GameAudioActions.playPraiseNoImmediateRepeat(voice, audioRuntime, SagaPictureStartsWithPraiseCandidates)
                            }
                        GameAudioActions.joinSilently(job)
                        if ((chapterId == 4 || chapterId == 5) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                            val elapsedMs = SystemClock.uptimeMillis() - tapAtMs
                            val remainingMs = Chapter4Station4MinCorrectDisplayMs - elapsedMs
                            if (remainingMs > 0L) delay(remainingMs)
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast)
                    }
                } else {
                    scope.launch {
                        gameViewModel.correctTapPulseLetter = picked
                        gameViewModel.correctTapPulseEpoch += 1
                        if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
                            GameAudioActions.awaitTrackedVoices(audioRuntime, 10000L)
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast)
                    }
                }
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                if (sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                    gameViewModel.station4WrongFlashLetter = picked
                    gameViewModel.station4WrongFlashEpoch += 1
                    onWrongFeedback(picked, false)
                } else {
                    if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
                        onWrongFeedback(null, true)
                    } else {
                        onWrongFeedback(null, false)
                    }
                }
            }
            AnswerResult.Finished -> {}
        }
    }
}
