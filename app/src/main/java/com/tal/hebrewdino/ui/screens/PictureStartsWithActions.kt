package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.BackgroundMusicPlayer
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.DinoCharacter
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
        companionCharacter: DinoCharacter? = null,
        backgroundMusic: BackgroundMusicPlayer? = null,
        postFocusAvoidPraiseRawResId: Int = 0,
        onCompanionPraisePlayed: (Int) -> Unit = {},
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
                        isSeason2QuizChapter ||
                        ((chapterId == 3 || chapterId == 6) && stationId == 1)
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
                                PostCoachCorrectPraiseActions.playInStationOrNarratorPraise(
                                    hadCoachIntervention = season2HadCoachIntervention,
                                    companion = companionCharacter,
                                    rawVoice = rawVoice,
                                    backgroundMusic = backgroundMusic,
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    narratorCandidates = SagaPictureStartsWithPraiseCandidates,
                                    avoidCompanionRawResId = postFocusAvoidPraiseRawResId,
                                    context = "PictureStartsWithActions.handlePick(correct,praise)",
                                    onCompanionPraisePlayed = onCompanionPraisePlayed,
                                )
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
                            gameViewModel.inputLocked = true
                            if (audioEnabled) {
                                val letterJob =
                                    GameAudioActions.launchFeedbackVoiceNoCancel(
                                        audioEnabled = true,
                                        scope = scope,
                                        audioRuntime = audioRuntime,
                                    ) {
                                        val resId = AudioClips.letterNameRawResId(picked)
                                        when {
                                            resId == null -> {
                                                android.util.Log.e(
                                                    "MissingContent",
                                                    "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(correct,ch3Or6St1) stage=missing raw letter-name mapping letter='$picked'",
                                                )
                                                rawVoice?.playRawBlocking(0)
                                            }
                                            rawVoice == null -> {
                                                android.util.Log.e(
                                                    "MissingContent",
                                                    "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(correct,ch3Or6St1) stage=rawVoice=null expectedRawResId=$resId",
                                                )
                                                voice.playRequiredBlocking(
                                                    assetPath = "",
                                                    context = "PictureStartsWithActions.handlePick(correct,ch3Or6St1,rawVoice=null)",
                                                    chapterId = chapterId,
                                                    stationId = stationId,
                                                )
                                            }
                                            else -> rawVoice.playRawBlocking(resId)
                                        }
                                    }
                                GameAudioActions.joinSilently(letterJob)
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
                        isSeason2QuizChapter ||
                        ((chapterId == 3 || chapterId == 6) && stationId == 1)
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
                    if (audioEnabled && (chapterId == 3 || chapterId == 6) && stationId == 1) {
                        val resId = AudioClips.letterNameRawResId(picked)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(wrong,ch3Or6St1) stage=missing raw letter-name mapping letter='$picked'",
                            )
                            if (audioEnabled) ChildGameAudioHooks.onWrong()
                            onWrongFeedback(picked, false)
                            return
                        }
                        if (rawVoice == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required letter-name audio. chapterId=$chapterId stationId=$stationId context=PictureStartsWithActions.handlePick(wrong,ch3Or6St1) stage=rawVoice=null expectedRawResId=$resId",
                            )
                            if (audioEnabled) ChildGameAudioHooks.onWrong()
                            onWrongFeedback(picked, false)
                            return
                        }
                        gameViewModel.inputLocked = true
                        scope.launch {
                            try {
                                rawVoice.playRawBlocking(resId)
                                if (audioEnabled) ChildGameAudioHooks.onWrong()
                                onWrongFeedback(picked, true)
                            } finally {
                                gameViewModel.inputLocked = false
                            }
                        }
                    } else {
                        if (audioEnabled) ChildGameAudioHooks.onWrong()
                        onWrongFeedback(null, false)
                    }
                }
            }
            AnswerResult.Finished -> {}
        }
    }
}
