package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal object ImageMatchActions {
    fun handleImageToWordAttempt(
        choiceId: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongWordCatalogId: String?) -> Unit,
    ): Boolean {
        if (!gameViewModel.consumeTapCooldown()) return false
        cancelFeedbackVoice()
        return when (session.submitImageMatch(choiceId)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                scope.launch {
                    if (audioEnabled) {
                        val clip =
                            AudioClips.imageToWordClipByCatalogId(
                                catalogEntryId = choiceId,
                                chapterId = chapterId,
                                voiceHasAsset = { path -> voice.hasAsset(path) },
                            )
                        voice.playBlocking(clip)
                        val praise =
                            mutableListOf(
                                AudioClips.VoPraiseMetzuyan,
                                AudioClips.VoPraiseYofi,
                                AudioClips.VoPraiseHitzlacht,
                                AudioClips.VoNice1,
                                AudioClips.VoGoodJob2,
                                AudioClips.VoGoodJob1,
                            )
                        praise.shuffle()
                        voice.playFirstAvailableBlocking(*praise.toTypedArray())
                    }
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
                true
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                onWrongFeedback(choiceId)
                false
            }
            AnswerResult.Finished -> false
        }
    }

    fun handleImageToWordReplayCorrectChoice(
        audioEnabled: Boolean,
        cancelFeedbackVoice: () -> Unit,
        chapterId: Int,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        setFeedbackVoiceJob: (Job?) -> Unit,
    ) {
        if (!audioEnabled) return
        cancelFeedbackVoice()
        val q = session.currentQuestion as? Question.ImageMatchQuestion ?: return
        val clip =
            AudioClips.imageToWordClipByCatalogId(
                catalogEntryId = q.correctChoiceId,
                chapterId = chapterId,
                voiceHasAsset = { path -> voice.hasAsset(path) },
            )
        if (voice.hasAsset(clip)) {
            setFeedbackVoiceJob(scope.launch { voice.playBlocking(clip) })
        }
    }

    fun handleImageToWordWordPressed(
        choiceId: String,
        audioEnabled: Boolean,
        cancelFeedbackVoice: () -> Unit,
        chapterId: Int,
        scope: CoroutineScope,
        voice: VoicePlayer,
        setFeedbackVoiceJob: (Job?) -> Unit,
    ) {
        if (!audioEnabled) return
        cancelFeedbackVoice()
        setFeedbackVoiceJob(
            scope.launch {
                val clip =
                    AudioClips.imageToWordClipByCatalogId(
                        catalogEntryId = choiceId,
                        chapterId = chapterId,
                        voiceHasAsset = { path -> voice.hasAsset(path) },
                    )
                voice.playBlocking(clip)
            },
        )
    }

    fun handleImageMatchAttempt(
        choiceId: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaEpisode: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongWordCatalogId: String?, generic: Boolean) -> Unit,
    ): Boolean {
        if (!gameViewModel.consumeTapCooldown()) return false
        cancelFeedbackVoice()
        return when (session.submitImageMatch(choiceId)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                scope.launch {
                    if (sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                        if (chapterId != 3 && chapterId != 6) {
                            voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId))
                        }
                    }
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
                true
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                if (sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                    onWrongFeedback(choiceId, false)
                } else {
                    onWrongFeedback(null, true)
                }
                false
            }
            AnswerResult.Finished -> false
        }
    }
}
