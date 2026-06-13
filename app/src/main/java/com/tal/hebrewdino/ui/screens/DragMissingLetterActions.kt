package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season1StationAudio
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
        chapterId: Int,
        stationId: Int,
        rawVoice: RawVoicePlayer?,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
        advanceAfterRound: suspend (Boolean) -> Unit,
    ): Boolean {
        if (gameViewModel.dragMissingLetterCompleting) return false
        if (!gameViewModel.consumeTapCooldown(minIntervalMs = 90L)) return false
        cancelFeedbackVoice()
        when (session.submitDragMissingLetter(letter)) {
            AnswerResult.Correct -> {
                val catalogId =
                    (session.currentQuestion as? Question.DragMissingLetterQuestion)?.catalogEntryId
                gameViewModel.dragMissingLetterCompleting = true
                gameViewModel.inputLocked = true
                scope.launch {
                    if (audioEnabled) {
                        if (
                            rawVoice != null &&
                                Season1StationAudio.isSeason1DragMissingLetterStation(chapterId, stationId)
                        ) {
                            Season1StationAudio.playDragMissingLetterCorrectFeedback(
                                rawVoice = rawVoice,
                                letter = letter,
                                catalogEntryId = catalogId,
                                chapterId = chapterId,
                                stationId = stationId,
                                sfx = sfx,
                            )
                        } else {
                            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
                        }
                    }
                    delay(280.milliseconds)
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
