package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal object FindGridActions {
    fun handleSagaGridLetterTapped(
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        tapped: String,
        question: Question.FindLetterGridQuestion,
        scope: CoroutineScope,
        sfx: SoundPoolPlayer,
        rawVoice: RawVoicePlayer,
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
            val resId = AudioClips.letterNameRawResId(tapped)
            if (resId == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=FindGridActions.handleSagaGridLetterTapped(letterNameFirst) stage=missing raw letter-name mapping letter='$tapped'",
                )
                rawVoice.playRawBlocking(0)
                return@launchFeedbackVoiceNoCancel
            }
            rawVoice.playRawBlocking(resId)
            if (isCorrect) {
                sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.62f)
            } else {
                sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.58f)
            }
        }
    }

    fun handleCellTapped(
        gameViewModel: GameViewModel,
        sagaUsesFindGridAudioStaging: Boolean,
        speakLetterNameOnGridTap: Boolean,
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
        if (!sagaUsesFindGridAudioStaging && !speakLetterNameOnGridTap) {
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

