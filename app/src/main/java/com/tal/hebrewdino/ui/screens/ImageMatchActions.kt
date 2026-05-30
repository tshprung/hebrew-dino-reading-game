package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val ImageToWordPraiseCandidates =
    arrayOf(
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
    )

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
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongWordCatalogId: String?) -> Unit,
    ): Boolean {
        if (!gameViewModel.consumeTapCooldown()) return false
        cancelFeedbackVoice()
        return when (session.submitImageMatch(choiceId)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                val audioJob =
                    GameAudioActions.launchFeedbackVoiceNoCancel(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                    ) {
                        val clip =
                            AudioClips.imageToWordClipByCatalogId(
                                catalogEntryId = choiceId,
                                chapterId = chapterId,
                                voiceHasAsset = { path -> voice.hasAsset(path) },
                            )
                        voice.playBlocking(clip)
                        GameAudioActions.playPraiseNoImmediateRepeat(
                            voice = voice,
                            audioRuntime = audioRuntime,
                            candidates = ImageToWordPraiseCandidates,
                            chapterId = chapterId,
                            rawVoice = rawVoice,
                        )
                    }
                scope.launch {
                    GameAudioActions.joinSilently(audioJob)
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
        audioRuntime: GameAudioRuntimeState,
    ) {
        if (!audioEnabled) return
        val q = session.currentQuestion as? Question.ImageMatchQuestion ?: return
        val clip =
            AudioClips.imageToWordClipByCatalogId(
                catalogEntryId = q.correctChoiceId,
                chapterId = chapterId,
                voiceHasAsset = { path -> voice.hasAsset(path) },
            )
        if (voice.hasAsset(clip)) {
            GameAudioActions.launchPromptVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) {
                voice.playBlocking(clip)
            }
        }
    }

    fun handleImageToWordWordPressed(
        choiceId: String,
        audioEnabled: Boolean,
        cancelFeedbackVoice: () -> Unit,
        chapterId: Int,
        scope: CoroutineScope,
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
    ) {
        if (!audioEnabled) return
        GameAudioActions.launchPromptVoice(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
            cancelFeedbackVoice = cancelFeedbackVoice,
        ) {
            val clip =
                AudioClips.imageToWordClipByCatalogId(
                    catalogEntryId = choiceId,
                    chapterId = chapterId,
                    voiceHasAsset = { path -> voice.hasAsset(path) },
                )
            voice.playBlocking(clip)
        }
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
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongWordCatalogId: String?, generic: Boolean) -> Unit,
    ): Boolean {
        if (!gameViewModel.consumeTapCooldown()) return false
        cancelFeedbackVoice()
        return when (session.submitImageMatch(choiceId)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                val audioJob =
                    GameAudioActions.launchFeedbackVoiceNoCancel(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                    ) {
                        when {
                            chapterId == TrainingV1Config.CHAPTER_ID &&
                                stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER -> {
                                val wordPath = AudioClips.wordClipByCatalogId(choiceId)
                                if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                                GameAudioActions.playPraiseNoImmediateRepeat(
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    candidates = ImageToWordPraiseCandidates,
                                    chapterId = chapterId,
                                    rawVoice = rawVoice,
                                )
                            }
                            sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL -> {
                                if (chapterId != 3 && chapterId != 6) {
                                    voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId))
                                }
                            }
                            else -> Unit
                        }
                    }
                scope.launch {
                    GameAudioActions.joinSilently(audioJob)
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    advanceAfterRound(isLast)
                }
                true
            }
            AnswerResult.Wrong -> {
                if (audioEnabled) ChildGameAudioHooks.onWrong()
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                when {
                    chapterId == TrainingV1Config.CHAPTER_ID &&
                        stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER ->
                        onWrongFeedback(choiceId, false)

                    sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL ->
                        onWrongFeedback(choiceId, false)

                    else ->
                        onWrongFeedback(null, true)
                }
                false
            }
            AnswerResult.Finished -> false
        }
    }
}
