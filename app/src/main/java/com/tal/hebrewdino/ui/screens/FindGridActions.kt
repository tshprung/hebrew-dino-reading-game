package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

internal object FindGridActions {
    fun handleSagaGridLetterTapped(
        audioEnabled: Boolean,
        tapped: String,
        question: Question.FindLetterGridQuestion,
        scope: CoroutineScope,
        sfx: SoundPoolPlayer,
        cancelFeedbackVoice: () -> Unit,
        audioRuntime: GameAudioRuntimeState,
    ) {
        if (!audioEnabled) return
        cancelFeedbackVoice()
        sfx.stopAllStreams()
        val isCorrect = tapped == question.targetLetter
        GameAudioActions.launchFeedbackVoiceNoCancel(
            audioEnabled = true,
            scope = scope,
            audioRuntime = audioRuntime,
        ) {
            if (isCorrect) {
                sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.62f)
            } else {
                val tappedClip = AudioClips.letterNameClip(tapped)
                val playWrongSfx: suspend () -> Unit = {
                    sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
                }
                val playTappedClip: suspend () -> Boolean = {
                    if (tappedClip == null) {
                        false
                    } else {
                        sfx.playReturningStreamId(tappedClip, volume = 1f)
                        true
                    }
                }
                when (Random.nextInt(100)) {
                    in 0..39 -> {
                        playWrongSfx()
                    }
                    in 40..89 -> {
                        if (!playTappedClip()) playWrongSfx()
                    }
                    else -> {
                        playWrongSfx()
                        playTappedClip()
                    }
                }
            }
        }
    }

    fun handleNonStagedCorrectTap(
        audioEnabled: Boolean,
        scope: CoroutineScope,
        sfx: SoundPoolPlayer,
    ) {
        if (!audioEnabled) return
        scope.launch {
            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
        }
    }

    fun handleCellTapped(
        gameViewModel: GameViewModel,
        sagaUsesFindGridAudioStaging: Boolean,
        cancelFeedbackVoice: () -> Unit,
        session: LevelSession,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
        index: Int,
        question: Question.FindLetterGridQuestion,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        val tappedLetter = question.cells.getOrNull(index)
        if (!sagaUsesFindGridAudioStaging) {
            cancelFeedbackVoice()
        }
        session.wrongTap()
        gameViewModel.shakeEpoch += 1
        HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
        if (!sagaUsesFindGridAudioStaging) {
            onWrongFeedback(tappedLetter, false)
        }
    }

    fun handleCompleted(
        gameViewModel: GameViewModel,
        scope: CoroutineScope,
        session: LevelSession,
        advanceAfterRound: suspend (isLast: Boolean) -> Unit,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        gameViewModel.inputLocked = true
        scope.launch {
            if (session.completeCurrentRound() == AnswerResult.Correct) {
                val isLast = session.currentIndex >= session.totalQuestions - 1
                advanceAfterRound(isLast)
            }
        }
    }
}

