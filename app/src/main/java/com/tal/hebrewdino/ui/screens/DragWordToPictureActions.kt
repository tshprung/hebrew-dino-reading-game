package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal object DragWordToPictureActions {
    /**
     * @return true when [wordCatalogId] locks onto [pictureCatalogId]; false on wrong target (try-again only).
     */
    fun handleDropAttempt(
        wordCatalogId: String,
        pictureCatalogId: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        sfx: SoundPoolPlayer,
        session: LevelSession,
        scope: CoroutineScope,
        onWrongFeedback: () -> Job?,
    ): Boolean {
        if (gameViewModel.dragWordRoundCompleting) return false
        if (!gameViewModel.consumeTapCooldown(minIntervalMs = 90L)) return false
        cancelFeedbackVoice()
        if (!session.validateDragWordToPicturePlacement(wordCatalogId, pictureCatalogId)) {
            scope.launch {
                if (audioEnabled) {
                    sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.55f)
                }
                val feedbackJob = onWrongFeedback()
                GameAudioActions.joinSilently(feedbackJob)
                delay(280.milliseconds)
            }
            return false
        }
        if (audioEnabled) {
            scope.launch {
                sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
            }
        }
        return true
    }

    fun handleRoundComplete(
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        advanceAfterRound: suspend (Boolean) -> Unit,
    ) {
        if (gameViewModel.dragWordRoundCompleting) return
        gameViewModel.dragWordRoundCompleting = true
        gameViewModel.inputLocked = true
        cancelFeedbackVoice()
        when (session.completeDragWordToPictureRound()) {
            AnswerResult.Correct -> {
                scope.launch {
                    delay(350.milliseconds)
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
            }
            else -> {
                gameViewModel.dragWordRoundCompleting = false
                gameViewModel.inputLocked = false
            }
        }
    }
}
