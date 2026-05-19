package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
        setFeedbackVoiceJob: (Job?) -> Unit,
        onWrongFeedback: (wrongPickedLetter: String) -> Unit,
        advanceAfterRound: suspend (isLast: Boolean, ch3SpellMidWord: Boolean) -> Unit,
    ) {
        cancelFeedbackVoice()
        if (!gameViewModel.consumeTapCooldown()) return
        when (session.submitAnswer(picked)) {
            AnswerResult.Correct -> {
                if (audioEnabled && !sagaUsesPickLetterAudioStaging) {
                    ChildGameAudioHooks.onCorrect()
                }
                gameViewModel.correctTapPulseLetter = picked
                gameViewModel.correctTapPulseEpoch += 1
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
                                scope.launch {
                                    voice.playFirstAvailableBlocking(*praise.toTypedArray())
                                }
                            setFeedbackVoiceJob(job)
                            withTimeoutOrNull(2800L) { job.join() }
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
                            scope.launch {
                                voice.playBlocking(letterName)
                                voice.playFirstAvailableBlocking(*praise.toTypedArray())
                            }
                        setFeedbackVoiceJob(job)
                        job.join()
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
