package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.audio.BackgroundMusicPlayer
import com.tal.hebrewdino.ui.audio.Season2PostFocusCorrectAudio
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2EarlyStationQaPolicy
import com.tal.hebrewdino.ui.domain.Season2WarmupStationQaPolicy
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
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
        onWrongFeedback: (wrongPickedLetter: String, wrongPickedLetterAlreadySpoken: Boolean) -> Unit,
        advanceAfterRound: suspend (isLast: Boolean, ch3SpellMidWord: Boolean) -> Unit,
        season2HadCoachIntervention: Boolean = false,
        companionCharacter: DinoCharacter? = null,
        backgroundMusic: BackgroundMusicPlayer? = null,
        postFocusAvoidPraiseRawResId: Int = 0,
        onPostFocusPraisePlayed: (Int) -> Unit = {},
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        val applyImmediateLetterNameAudio =
            Season2WarmupStationQaPolicy.usesRawLetterNameStationFeedback(chapterId)
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
                        val resId = AudioClips.letterNameRawResId(picked)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,highlightedLetterInWord) stage=missing raw letter-name mapping letter='$picked'",
                            )
                            if (rawVoice != null) {
                                rawVoice.playRawBlocking(0)
                            } else {
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "PickLetterActions.handlePick(correct,highlightedLetterInWord,missingLetterNameMapping,rawVoice=null)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                        } else if (rawVoice == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,highlightedLetterInWord) stage=rawVoice=null expectedRawResId=$resId",
                            )
                            voice.playRequiredBlocking(
                                assetPath = "",
                                context = "PickLetterActions.handlePick(correct,highlightedLetterInWord,rawVoice=null)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        } else {
                            rawVoice.playRawBlocking(resId)
                        }
                        if (wordDone) {
                            cancelFeedbackVoice()
                            val job =
                                GameAudioActions.launchFeedbackVoiceNoCancel(
                                    audioEnabled = true,
                                    scope = scope,
                                    audioRuntime = audioRuntime,
                                ) {
                                    GameAudioActions.playPraiseNoImmediateRepeat(
                                        voice = voice,
                                        audioRuntime = audioRuntime,
                                        candidates = HighlightedWordDonePraiseCandidates,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        context = "PickLetterActions.handlePick(correct,highlightedWordDonePraise)",
                                        rawVoice = rawVoice,
                                    )
                                }
                            GameAudioActions.joinSilently(job)
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(
                            isLast,
                            !wordDone,
                        )
                    }
                } else if (audioEnabled && isChapter3AudioLetterRecognitionStation) {
                    scope.launch {
                        cancelFeedbackVoice()
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                val resId = AudioClips.letterNameRawResId(picked)
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,ch3AudioRecognition) stage=missing raw letter-name mapping letter='$picked'",
                                    )
                                    rawVoice?.playRawBlocking(0)
                                } else if (rawVoice == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,ch3AudioRecognition) stage=rawVoice=null expectedRawResId=$resId",
                                    )
                                    voice.playRequiredBlocking(
                                        assetPath = "",
                                        context = "PickLetterActions.handlePick(correct,ch3AudioRecognition,rawVoice=null)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else {
                                    rawVoice.playRawBlocking(resId)
                                }
                                GameAudioActions.playPraiseNoImmediateRepeat(
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    candidates = HighlightedWordDonePraiseCandidates,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    context = "PickLetterActions.handlePick(correct,ch3AudioRecognitionPraise)",
                                    rawVoice = rawVoice,
                                )
                            }
                        GameAudioActions.joinSilently(job)
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                } else if (audioEnabled && sagaUsesPickLetterAudioStaging) {
                    scope.launch {
                        cancelFeedbackVoice()
                        if (applyImmediateLetterNameAudio) {
                            val resId = AudioClips.letterNameRawResId(picked)
                            if (resId == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,sagaStaging) stage=missing raw letter-name mapping letter='$picked'",
                                )
                                rawVoice?.playRawBlocking(0)
                                val isLast = session.currentIndex >= session.totalQuestions - 1
                                advanceAfterRound(isLast, false)
                                return@launch
                            }
                            if (rawVoice == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,sagaStaging) stage=rawVoice=null expectedRawResId=$resId",
                                )
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "PickLetterActions.handlePick(correct,sagaStaging,rawVoice=null)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                                val isLast = session.currentIndex >= session.totalQuestions - 1
                                advanceAfterRound(isLast, false)
                                return@launch
                            }
                            val skipNarratorPraise =
                                Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
                                    season2HadCoachIntervention,
                                )
                            val praise = AudioClips.station1CorrectPraiseTailCandidates()
                            val job =
                                GameAudioActions.launchFeedbackVoiceNoCancel(
                                    audioEnabled = true,
                                    scope = scope,
                                    audioRuntime = audioRuntime,
                                ) {
                                    rawVoice.playRawBlocking(resId)
                                    if (skipNarratorPraise && companionCharacter != null) {
                                        val praiseRes =
                                            Season2PostFocusCorrectAudio.playBlocking(
                                                companion = companionCharacter,
                                                rawVoice = rawVoice,
                                                backgroundMusic = backgroundMusic,
                                                avoidRawResId = postFocusAvoidPraiseRawResId,
                                            )
                                        onPostFocusPraisePlayed(praiseRes)
                                    } else if (!skipNarratorPraise) {
                                        GameAudioActions.playPraiseNoImmediateRepeat(
                                            voice = voice,
                                            audioRuntime = audioRuntime,
                                            candidates = praise,
                                            chapterId = chapterId,
                                            stationId = stationId,
                                            context = "PickLetterActions.handlePick(correct,sagaStagingPraise)",
                                            rawVoice = rawVoice,
                                        )
                                    }
                                }
                            GameAudioActions.joinSilently(job)
                            val isLast = session.currentIndex >= session.totalQuestions - 1
                            advanceAfterRound(isLast, false)
                            return@launch
                        }
                        val letterName = AudioClips.letterNameClip(picked)
                        if (letterName == null || !voice.hasAsset(letterName)) {
                            val isLast = session.currentIndex >= session.totalQuestions - 1
                            advanceAfterRound(isLast, false)
                            return@launch
                        }
                        val skipNarratorPraise =
                            Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
                                season2HadCoachIntervention,
                            )
                        val praise = AudioClips.station1CorrectPraiseTailCandidates()
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                voice.playBlocking(letterName)
                                if (!skipNarratorPraise) {
                                    GameAudioActions.playPraiseNoImmediateRepeat(
                                        voice = voice,
                                        audioRuntime = audioRuntime,
                                        candidates = praise,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        context = "PickLetterActions.handlePick(correct,sagaStagingPraise)",
                                        rawVoice = rawVoice,
                                    )
                                }
                            }
                        GameAudioActions.joinSilently(job)
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                } else if (audioEnabled && isTrainingStation1) {
                    scope.launch {
                        cancelFeedbackVoice()
                        val praise = AudioClips.station1CorrectPraiseTailCandidates()
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                val resId = AudioClips.letterNameRawResId(picked)
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,training) stage=missing raw letter-name mapping letter='$picked'",
                                    )
                                    if (rawVoice != null) {
                                        rawVoice.playRawBlocking(0)
                                    } else {
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "PickLetterActions.handlePick(correct,training,missingLetterNameMapping,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                    }
                                } else if (rawVoice == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct,training) stage=rawVoice=null expectedRawResId=$resId",
                                    )
                                    voice.playRequiredBlocking(
                                        assetPath = "",
                                        context = "PickLetterActions.handlePick(correct,training,rawVoice=null)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else {
                                    rawVoice.playRawBlocking(resId)
                                }
                                GameAudioActions.playPraiseNoImmediateRepeat(
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    candidates = praise,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    context = "PickLetterActions.handlePick(correct,trainingPraise)",
                                    rawVoice = rawVoice,
                                )
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
                        if (audioEnabled && applyImmediateLetterNameAudio) {
                            val resId = AudioClips.letterNameRawResId(picked)
                            when {
                                resId == null -> {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct) stage=missing raw letter-name mapping letter='$picked'",
                                    )
                                    rawVoice?.playRawBlocking(0)
                                }
                                rawVoice == null -> {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(correct) stage=rawVoice=null expectedRawResId=$resId",
                                    )
                                    voice.playRequiredBlocking(
                                        assetPath = "",
                                        context = "PickLetterActions.handlePick(correct,rawVoice=null)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                }
                                else -> rawVoice.playRawBlocking(resId)
                            }
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast, false)
                    }
                }
            }
            AnswerResult.Wrong -> {
                gameViewModel.shakeEpoch += 1
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                val shouldRunWrongHook =
                    audioEnabled &&
                        (!sagaUsesPickLetterAudioStaging ||
                            isChapter3HighlightedLetterInWordStation ||
                            isChapter3AudioLetterRecognitionStation)
                if (audioEnabled && applyImmediateLetterNameAudio) {
                    val resId = AudioClips.letterNameRawResId(picked)
                    if (resId == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(wrong) stage=missing raw letter-name mapping letter='$picked'",
                        )
                        scope.launch {
                            if (rawVoice != null) {
                                rawVoice.playRawBlocking(0)
                            } else {
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "PickLetterActions.handlePick(wrong,missingLetterNameMapping,rawVoice=null)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                        }
                        if (shouldRunWrongHook) ChildGameAudioHooks.onWrong()
                        onWrongFeedback(picked, false)
                        return
                    }
                    if (rawVoice == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PickLetterActions.handlePick(wrong) stage=rawVoice=null expectedRawResId=$resId",
                        )
                        scope.launch {
                            voice.playRequiredBlocking(
                                assetPath = "",
                                context = "PickLetterActions.handlePick(wrong,rawVoice=null)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        }
                        if (shouldRunWrongHook) ChildGameAudioHooks.onWrong()
                        onWrongFeedback(picked, false)
                        return
                    }
                    gameViewModel.inputLocked = true
                    scope.launch {
                        try {
                            rawVoice.playRawBlocking(resId)
                            if (shouldRunWrongHook) ChildGameAudioHooks.onWrong()
                            onWrongFeedback(picked, true)
                        } finally {
                            if (!sagaUsesPickLetterAudioStaging) {
                                gameViewModel.inputLocked = false
                            }
                        }
                    }
                } else {
                    if (shouldRunWrongHook) ChildGameAudioHooks.onWrong()
                    onWrongFeedback(picked, false)
                }
            }
            AnswerResult.Finished -> {}
        }
    }
}
