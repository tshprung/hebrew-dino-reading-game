package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

internal object FindGridActions {
    fun handleSagaGridLetterTapped(
        audioEnabled: Boolean,
        tapped: String,
        question: Question.FindLetterGridQuestion,
        scope: CoroutineScope,
        sfx: SoundPoolPlayer,
        setFeedbackVoiceJob: (Job?) -> Unit,
        setStation3VoiceStreamId: (Int) -> Unit,
    ) {
        if (!audioEnabled) return
        sfx.stopAllStreams()
        setStation3VoiceStreamId(0)
        val isCorrect = tapped == question.targetLetter
        setFeedbackVoiceJob(
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
                                setStation3VoiceStreamId(
                                    sfx.playReturningStreamId(tappedClip, volume = 1f) ?: 0,
                                )
                            } else {
                                sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                            }
                        }
                        else -> {
                            sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                            if (tappedClip != null) {
                                setStation3VoiceStreamId(
                                    sfx.playReturningStreamId(tappedClip, volume = 1f) ?: 0,
                                )
                            }
                        }
                    }
                }
            },
        )
    }

    fun handleCellTapped(
        consumeTapCooldown: () -> Boolean,
        gameViewModel: GameViewModel,
        sagaUsesFindGridAudioStaging: Boolean,
        cancelFeedbackVoice: () -> Unit,
        session: LevelSession,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
        index: Int,
        question: Question.FindLetterGridQuestion,
    ) {
        if (!consumeTapCooldown()) return
        if (!sagaUsesFindGridAudioStaging) {
            cancelFeedbackVoice()
        }
        session.wrongTap()
        gameViewModel.shakeEpoch += 1
        HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
        val tappedLetter = question.cells.getOrNull(index)
        if (!sagaUsesFindGridAudioStaging) {
            onWrongFeedback(tappedLetter, false)
        }
    }

    fun handleCompleted(
        consumeTapCooldown: () -> Boolean,
        scope: CoroutineScope,
        session: LevelSession,
        advanceAfterRound: suspend (isLast: Boolean) -> Unit,
    ) {
        if (!consumeTapCooldown()) return
        scope.launch {
            when (session.completeCurrentRound()) {
                AnswerResult.Correct -> {
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
                else -> {}
            }
        }
    }
}

