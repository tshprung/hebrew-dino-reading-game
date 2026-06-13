package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.SixStationArcQaPolicy
import com.tal.hebrewdino.ui.domain.Season2EarlyStationQaPolicy
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SagaPictureStartsWithPraiseCandidates =
    arrayOf(
        AudioClips.VoKolHakavod,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
    )

internal object PictureStartsWithActions {
    fun handlePick(
        picked: String,
        gameViewModel: GameViewModel,
        cancelFeedbackVoice: () -> Unit,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaEpisode: Boolean,
        isSeason2QuizChapter: Boolean = false,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (Boolean) -> Unit,
        onWrongFeedback: (wrongPickedLetter: String?, wrongPickedLetterAlreadySpoken: Boolean) -> Job?,
        season2HadCoachIntervention: Boolean = false,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        cancelFeedbackVoice()
        when (session.submitPictureStartsWith(picked)) {
            AnswerResult.Correct -> {
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                gameViewModel.inputLocked = true
                val useLetterStagingCorrect =
                    (sagaEpisode && SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId)) ||
                        isSeason2QuizChapter
                val shouldPinCorrectLetter =
                    useLetterStagingCorrect ||
                        (chapterId == 3 && stationId == 1) ||
                        (chapterId == 6 && stationId == 1)
                if (shouldPinCorrectLetter) {
                    gameViewModel.station4PinnedCorrectLetter = picked
                }
                if (useLetterStagingCorrect) {
                    scope.launch {
                        gameViewModel.correctTapPulseLetter = picked
                        gameViewModel.correctTapPulseEpoch += 1
                        cancelFeedbackVoice()
                        val applyImmediateLetterNameAudio =
                            chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5 ||
                                isSeason2QuizChapter
                        val job =
                            GameAudioActions.launchFeedbackVoiceNoCancel(
                                audioEnabled = true,
                                scope = scope,
                                audioRuntime = audioRuntime,
                            ) {
                                if (applyImmediateLetterNameAudio) {
                                    val resId = AudioClips.letterNameRawResId(picked)
                                    when {
                                        resId == null -> {
                                            android.util.Log.e(
                                                "MissingContent",
                                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(correct) stage=missing raw letter-name mapping letter='$picked'",
                                            )
                                            rawVoice?.playRawBlocking(0)
                                        }
                                        rawVoice == null -> {
                                            android.util.Log.e(
                                                "MissingContent",
                                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(correct) stage=rawVoice=null expectedRawResId=$resId",
                                            )
                                            voice.playRequiredBlocking(
                                                assetPath = "",
                                                context = "PictureStartsWithActions.handlePick(correct,rawVoice=null)",
                                                chapterId = chapterId,
                                                stationId = stationId,
                                            )
                                        }
                                        else -> rawVoice.playRawBlocking(resId)
                                    }
                                } else {
                                    val letterName = AudioClips.letterNameClip(picked)
                                    if (letterName != null && voice.hasAsset(letterName)) {
                                        voice.playBlocking(letterName)
                                    }
                                }
                                if (
                                    !Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
                                        season2HadCoachIntervention,
                                    )
                                ) {
                                    GameAudioActions.playPraiseNoImmediateRepeat(
                                        voice = voice,
                                        audioRuntime = audioRuntime,
                                        candidates = SagaPictureStartsWithPraiseCandidates,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                        context = "PictureStartsWithActions.handlePick(correct,praise)",
                                        rawVoice = rawVoice,
                                    )
                                }
                            }
                        GameAudioActions.joinSilently(job)
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast)
                    }
                } else {
                    scope.launch {
                        gameViewModel.correctTapPulseLetter = picked
                        gameViewModel.correctTapPulseEpoch += 1
                        if ((chapterId == 3 || chapterId == 6) && stationId == 1) {
                            if (audioEnabled) {
                                GameAudioActions.awaitTrackedVoices(audioRuntime, 10000L)
                            }
                            delay(Chapter3Or6Station1SuccessHoldMs)
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast)
                    }
                }
            }
            AnswerResult.Wrong -> {
                HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                val useLetterStagingWrong =
                    Season2EarlyStationQaPolicy.shouldUseSeason2PictureStartsWithWrongAudio(
                        isSeason2QuizChapter = isSeason2QuizChapter,
                        sagaEpisode = sagaEpisode,
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                if (useLetterStagingWrong) {
                    gameViewModel.station4WrongFlashLetter = picked
                    gameViewModel.station4WrongFlashEpoch += 1
                    val applyImmediateLetterNameAudio =
                        chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5 ||
                            isSeason2QuizChapter
                    if (audioEnabled && applyImmediateLetterNameAudio) {
                        val resId = AudioClips.letterNameRawResId(picked)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(wrong) stage=missing raw letter-name mapping letter='$picked'",
                            )
                            scope.launch {
                                if (rawVoice != null) {
                                    rawVoice.playRawBlocking(0)
                                } else {
                                    voice.playRequiredBlocking(
                                        assetPath = "",
                                        context = "PictureStartsWithActions.handlePick(wrong,missingLetterNameMapping,rawVoice=null)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                }
                            }
                            if (audioEnabled) ChildGameAudioHooks.onWrong()
                            onWrongFeedback(picked, false)
                            return
                        }
                        if (rawVoice == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(wrong) stage=rawVoice=null expectedRawResId=$resId",
                            )
                            scope.launch {
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "PictureStartsWithActions.handlePick(wrong,rawVoice=null)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                            if (audioEnabled) ChildGameAudioHooks.onWrong()
                            onWrongFeedback(picked, false)
                            return
                        }
                        gameViewModel.inputLocked = true
                        scope.launch {
                            try {
                                rawVoice.playRawBlocking(resId)
                                if (audioEnabled) ChildGameAudioHooks.onWrong()
                                val feedbackJob = onWrongFeedback(picked, true)
                                GameAudioActions.joinSilently(feedbackJob)
                            } finally {
                                if (!isSeason2QuizChapter) {
                                    gameViewModel.inputLocked = false
                                }
                            }
                        }
                    } else {
                        val letterName = AudioClips.letterNameClip(picked)
                        if (letterName != null && voice.hasAsset(letterName)) {
                            gameViewModel.inputLocked = true
                            scope.launch {
                                voice.playBlocking(letterName)
                                if (audioEnabled) ChildGameAudioHooks.onWrong()
                                onWrongFeedback(picked, true)
                            }
                        } else {
                            if (audioEnabled) ChildGameAudioHooks.onWrong()
                            onWrongFeedback(picked, false)
                        }
                    }
                } else {
                    if (audioEnabled) ChildGameAudioHooks.onWrong()
                    if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
                        onWrongFeedback(null, true)
                    } else {
                        onWrongFeedback(null, false)
                    }
                }
            }
            AnswerResult.Finished -> {}
        }
    }
}
