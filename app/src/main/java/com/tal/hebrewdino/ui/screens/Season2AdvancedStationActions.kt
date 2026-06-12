package com.tal.hebrewdino.ui.screens

import android.util.Log
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.Season2WordPartsAudio
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2WordPartsUxPolicy
import com.tal.hebrewdino.ui.domain.Season2EarlyStationQaPolicy
import com.tal.hebrewdino.ui.domain.Season2Station6FeedbackPolicy
import com.tal.hebrewdino.ui.domain.Season2WordPartsCatalog
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal object Season2AdvancedStationActions {
    fun interruptWordPartsVoice(
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        rawVoice: RawVoicePlayer?,
    ) {
        gameViewModel.wordPartsPickJob?.cancel()
        gameViewModel.wordPartsPickJob = null
        cancelFeedbackVoice()
        rawVoice?.stopNow()
    }

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
        picked: Question.WordPartsSplitOption,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        isSeason2QuizChapter: Boolean,
        season2UxStationId: Int?,
        season2AdvancedMode: com.tal.hebrewdino.ui.domain.Season2AdvancedStationMode?,
        consecutiveWrongsInRound: Int,
        chapterId: Int,
        stationId: Int,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: () -> Unit,
        season2HadCoachIntervention: Boolean = false,
    ) {
        if (gameViewModel.wordPartsCompletedEquation != null) return
        if (!gameViewModel.consumeTapCooldown()) return
        interruptWordPartsVoice(gameViewModel, cancelFeedbackVoice, rawVoice)
        val willPlayFocusAfterWrong =
            isSeason2QuizChapter &&
                Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                    consecutiveWrongInRound = consecutiveWrongsInRound + 1,
                    isSeason2Quiz = isSeason2QuizChapter,
                )
        val job =
            scope.launch {
                gameViewModel.inputLocked = true
                try {
                    val q = session.currentQuestion as? Question.WordPartsQuestion
                    val tappedCatalogId =
                        Season2WordPartsCatalog.catalogIdForSplit(picked.firstPart, picked.secondPart)
                    val immediateCorrectFilter =
                        q != null &&
                            Season2WordPartsUxPolicy.filterCorrectSplitImmediatelyBeforeAudio(
                                q.presentationMode,
                            )
                    val outcome =
                        if (immediateCorrectFilter) {
                            val result = session.submitWordParts(picked)
                            if (result == AnswerResult.Correct) {
                                gameViewModel.wordPartsCompletedEquation =
                                    "${q.word} = ${q.firstPart} + ${q.correctPart}"
                            }
                            result
                        } else {
                            null
                        }
                    if (audioEnabled && rawVoice != null) {
                        if (tappedCatalogId == null) {
                            Log.e(
                                "MissingContent",
                                "Missing word-part split catalog mapping. chapterId=$chapterId stationId=$stationId " +
                                    "context=Season2AdvancedStationActions.handleWordPartsPick " +
                                    "split='${picked.firstPart}+${picked.secondPart}'",
                            )
                        } else {
                            Season2WordPartsAudio.playSplitTapSequence(
                                catalogId = tappedCatalogId,
                                rawVoice = rawVoice,
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        }
                    } else if (tappedCatalogId == null) {
                        Log.e(
                            "MissingContent",
                            "Missing word-part split catalog mapping. chapterId=$chapterId stationId=$stationId " +
                                "context=Season2AdvancedStationActions.handleWordPartsPick " +
                                "split='${picked.firstPart}+${picked.secondPart}'",
                        )
                    }

                    when (outcome ?: session.submitWordParts(picked)) {
                        AnswerResult.Correct -> {
                            if (audioEnabled) ChildGameAudioHooks.onCorrect()
                            if (!immediateCorrectFilter) {
                                val correctQ = session.currentQuestion as? Question.WordPartsQuestion
                                if (correctQ != null) {
                                    gameViewModel.wordPartsCompletedEquation =
                                        "${correctQ.word} = ${correctQ.firstPart} + ${correctQ.correctPart}"
                                }
                            }
                            if (
                                audioEnabled &&
                                    rawVoice != null &&
                                    !Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
                                        season2HadCoachIntervention,
                                    )
                            ) {
                                GameAudioActions.playPraiseNoImmediateRepeat(
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    candidates = emptyArray(),
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    context = "Season2AdvancedStationActions.handleWordPartsPick(correct)",
                                    rawVoice = rawVoice,
                                )
                            }
                            delay(
                                if (immediateCorrectFilter) {
                                    Season2WordPartsUxPolicy.CorrectPostPraiseHoldMs
                                } else {
                                    1_400L
                                },
                            )
                            val isLast = session.currentIndex >= session.totalQuestions - 1
                            advanceAfterRound(isLast)
                        }
                        AnswerResult.Wrong -> {
                            if (audioEnabled) ChildGameAudioHooks.onWrong()
                            onWrongFeedback()
                            if (!willPlayFocusAfterWrong) {
                                delay(350.milliseconds)
                                gameViewModel.inputLocked = false
                            }
                        }
                        else -> {
                            gameViewModel.inputLocked = false
                        }
                    }
                } catch (e: CancellationException) {
                    gameViewModel.inputLocked = false
                    throw e
                }
            }
        gameViewModel.wordPartsPickJob = job
        job.invokeOnCompletion {
            if (gameViewModel.wordPartsPickJob === job) {
                gameViewModel.wordPartsPickJob = null
            }
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
