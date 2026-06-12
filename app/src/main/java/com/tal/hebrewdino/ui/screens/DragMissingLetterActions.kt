package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal object DragMissingLetterActions {
    /**
     * @return true when [letter] fills the missing slot; false on wrong letter (try-again only).
     */
    fun handleLetterPlaced(
        letter: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        sfx: SoundPoolPlayer,
        session: LevelSession,
        scope: CoroutineScope,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
        advanceAfterRound: suspend (Boolean) -> Unit,
    ): Boolean {
        if (gameViewModel.dragMissingLetterCompleting) return false
        if (!gameViewModel.consumeTapCooldown(minIntervalMs = 90L)) return false
        cancelFeedbackVoice()
        when (session.submitDragMissingLetter(letter)) {
            AnswerResult.Correct -> {
                if (audioEnabled) {
                    scope.launch {
                        sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
                    }
                }
                gameViewModel.dragMissingLetterCompleting = true
                gameViewModel.inputLocked = true
                scope.launch {
                    delay(400.milliseconds)
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
                return true
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) {
                    scope.launch {
                        sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.55f)
                    }
                }
                scope.launch {
                    onWrongFeedback(letter, false)
                    delay(280.milliseconds)
                }
                return false
            }
            else -> return false
        }
    }
}
