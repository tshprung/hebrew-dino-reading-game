package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val HighlightedWordDonePraiseCandidates =
    arrayOf(
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
    )

internal object PickLetterActions {
    fun handlePick(
        picked: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaUsesPickLetterAudioStaging: Boolean,
        isChapter3HighlightedLetterInWordStation: Boolean,
        isChapter3AudioLetterRecognitionStation: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        audioRuntime: GameAudioRuntimeState,
        onWrongFeedback: (wrongPickedLetter: String) -> Unit,
        advanceAfterRound: suspend (isLast: Boolean, ch3SpellMidWord: Boolean) -> Unit,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        when (session.submitAnswer(picked)) {
            AnswerResult.Correct -> {
                val isTrainingStation1 =
                    chapterId == TrainingV1Config.CHAPTER_ID &&
                        stationId == TrainingV1Config.STATION_HEAR_LETTER_CHOOSE
                if (audioEnabled && !sagaUsesPickLetterAudioStaging) {
                    ChildGameAudioHooks.onCorrect()
                }
                gameViewModel.correctTapPulseLetter = picked
                gameViewModel.correctTapPulseEpoch += 1
                gameViewModel.inputLocked = true
                gameViewModel.station1PinnedCorrectLetter = picked
                if (audioEnabled && isChapter3HighlightedLetterInWordStation) {
                    val wordDone = session.highlightedLetterInWordCompletesWordAfterCorrectRound()
                    scope.launch {
                        sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.62f)
                        val letterName = AudioClips.letterNameClip(picked)
                        if (letterName != null && voice.hasAsset(letterName)) {
                            voice.playBlocking(letterName)
                        }
                        if (wordDone) {
                            cancelFeedbackVoice()
                            val job =
                                GameAudioActions.launchFeedbackVoiceNoCancel(
                                    audioEnabled = true,
                                    scope = scope,
                                    audioRuntime = audioRuntime,
                                ) {
                                    GameAudioActions.playPraiseNoImmediateRepeat(voice, audioRuntime, HighlightedWordDonePraiseCandidates)
                                }
                            GameAudioActions.joinSilently(job)
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(
                            isLast,
                            !wordDone,
                        )
                    }
                } else if (audioEnabled && sagaUsesPickLetterAudioStaging) {
                    scope.launch {
                        cancelFeedbackVoice()
                        val letterName = AudioClips.letterNameClip(picked)
                        if (letterName == null || !voice.hasAsset(letterName)) {
                            val isLast = session.currentIndex >= session.totalQuestions - 1
                            advanceAfterRound(isLast, false)
                            return@launch
                        }
                        val praise = AudioClips.station1CorrectPraiseTailCandidates()
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                voice.playBlocking(letterName)
                                GameAudioActions.playPraiseNoImmediateRepeat(voice, audioRuntime, praise)
                            }
                        GameAudioActions.joinSilently(job)
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                } else if (audioEnabled && isTrainingStation1) {
                    scope.launch {
                        cancelFeedbackVoice()
                        val letterName = AudioClips.letterNameClip(picked)
                        val praise = AudioClips.station1CorrectPraiseTailCandidates()
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                if (letterName != null && voice.hasAsset(letterName)) {
                                    voice.playBlocking(letterName)
                                }
                                GameAudioActions.playPraiseNoImmediateRepeat(voice, audioRuntime, praise)
                            }
                        GameAudioActions.joinSilently(job)
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                } else {
                    scope.launch {
                        if (audioEnabled) {
                            GameAudioActions.awaitTrackedVoices(audioRuntime, 10000L)
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                }
            }
            AnswerResult.Wrong -> {
                if (audioEnabled &&
                    (!sagaUsesPickLetterAudioStaging || isChapter3HighlightedLetterInWordStation || isChapter3AudioLetterRecognitionStation)
                ) {
                    ChildGameAudioHooks.onWrong()
                }
                gameViewModel.shakeEpoch += 1
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                onWrongFeedback(picked)
            }
            AnswerResult.Finished -> {}
        }
    }
}
