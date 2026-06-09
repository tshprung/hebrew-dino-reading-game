package com.tal.hebrewdino.ui.screens

import android.util.Log
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object Season2AdvancedStationActions {
    fun handleMissingFirstLetterPick(
        picked: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        session: LevelSession,
        scope: CoroutineScope,
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        when (session.submitMissingFirstLetter(picked)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                scope.launch {
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                onWrongFeedback(picked, false)
            }
            else -> {}
        }
    }

    fun handleWordPartsPick(
        picked: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: () -> Unit,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        when (session.submitWordParts(picked)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                val q = session.currentQuestion as? Question.WordPartsQuestion
                if (q != null) {
                    gameViewModel.wordPartsCompletedEquation =
                        "${q.firstPart} + ${q.correctPart} = ${q.word}"
                }
                scope.launch {
                    delay(1_400)
                    gameViewModel.wordPartsCompletedEquation = null
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                onWrongFeedback()
            }
            else -> {}
        }
    }

    fun handleRhymingPick(
        choiceId: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongWordCatalogId: String?) -> Unit,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        when (session.submitRhyming(choiceId)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                scope.launch {
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                onWrongFeedback(choiceId)
            }
            else -> {}
        }
    }

    fun replayWordByCatalogId(
        catalogId: String,
        chapterId: Int,
        stationId: Int,
        rawVoice: RawVoicePlayer?,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        audioEnabled: Boolean,
    ) {
        if (!audioEnabled) return
        val resId = AudioClips.wordRawResIdByCatalogId(catalogId)
        if (resId == null) {
            Log.e(
                "MissingContent",
                "Missing required word audio. chapterId=$chapterId stationId=$stationId context=Season2AdvancedStationActions.replayWord catalogId='$catalogId'",
            )
            return
        }
        if (rawVoice == null) {
            Log.e(
                "MissingContent",
                "Missing rawVoice player. chapterId=$chapterId stationId=$stationId context=Season2AdvancedStationActions.replayWord catalogId='$catalogId'",
            )
            return
        }
        scope.launch {
            GameAudioActions.launchFeedbackVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = audioRuntime,
            ) {
                rawVoice.playRawBlocking(resId)
            }
        }
    }

    fun catalogIdForReplay(question: Question): String? =
        when (question) {
            is Question.MissingFirstLetterQuestion -> question.catalogEntryId
            is Question.WordPartsQuestion -> question.catalogEntryId
            is Question.RhymingQuestion -> question.targetCatalogEntryId
            is Question.ImageMatchQuestion -> question.correctChoiceId
            else -> null
        }
}
