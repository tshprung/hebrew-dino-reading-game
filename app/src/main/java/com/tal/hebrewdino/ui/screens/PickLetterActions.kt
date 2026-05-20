package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal object PickLetterActions {
    fun handlePick(
        picked: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
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
                if (audioEnabled && !sagaUsesPickLetterAudioStaging) {
                    ChildGameAudioHooks.onCorrect()
                }
                gameViewModel.correctTapPulseLetter = picked
                gameViewModel.correctTapPulseEpoch += 1
                gameViewModel.inputLocked = true
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
                            val job =
                                GameAudioActions.launchFeedbackVoiceNoCancel(
                                    audioEnabled = true,
                                    scope = scope,
                                    audioRuntime = audioRuntime,
                                ) {
                                    voice.playFirstAvailableBlocking(*praise.toTypedArray())
                                }
                            GameAudioActions.await(job, 2800L)
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
                        gameViewModel.station1PinnedCorrectLetter = picked
                        val praise =
                            AudioClips.station1CorrectPraiseTailCandidates()
                                .toMutableList()
                        praise.shuffle()
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                voice.playBlocking(letterName)
                                voice.playFirstAvailableBlocking(*praise.toTypedArray())
                            }
                        job?.join()
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                } else {
                    scope.launch {
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
