package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal object PictureStartsWithActions {
    fun handlePick(
        picked: String,
        gameViewModel: GameViewModel,
        consumeTapCooldown: () -> Boolean,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaEpisode: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        getFeedbackVoiceJob: () -> Job?,
        setFeedbackVoiceJob: (Job?) -> Unit,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
    ) {
        if (!consumeTapCooldown()) return
        cancelFeedbackVoice()
        if (audioEnabled &&
            (
                ((chapterId == 3 || chapterId == 6) && stationId == 1) ||
                    (chapterId == 2 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE)
            )
        ) {
            val clip = AudioClips.letterNameClip(picked)
            if (clip != null && voice.hasAsset(clip)) {
                setFeedbackVoiceJob(scope.launch { voice.playBlocking(clip) })
            }
        }
        when (session.submitPictureStartsWith(picked)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                if (chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                    gameViewModel.station4PinnedCorrectLetter = picked
                }
                if (sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                    scope.launch {
                        gameViewModel.correctTapPulseLetter = picked
                        gameViewModel.correctTapPulseEpoch += 1
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
                        setFeedbackVoiceJob(job)
                        job.join()
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast)
                    }
                } else {
                    scope.launch {
                        gameViewModel.correctTapPulseLetter = picked
                        gameViewModel.correctTapPulseEpoch += 1
                        if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
                            withTimeoutOrNull(1200L) { getFeedbackVoiceJob()?.join() }
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
