package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.InStationPraiseAudio
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2Ch1QaPolicy
import com.tal.hebrewdino.ui.domain.Season2EarlyStationQaPolicy
import com.tal.hebrewdino.ui.domain.Season2StationQaPolicy
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.SixStationArcQaPolicy
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private fun usesImageToWordRawWordClips(chapterId: Int): Boolean =
    Season2StationAudio.usesImageToWordRawWordClips(chapterId)

private val ImageToWordPraiseCandidates =
    arrayOf(
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
    )

private suspend fun playSagaWhichWordInStationPraise(
    season2HadCoachIntervention: Boolean,
    rawVoice: RawVoicePlayer?,
    voice: VoicePlayer,
    chapterId: Int,
    stationId: Int,
) {
    if (
        Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
            season2HadCoachIntervention,
        )
    ) {
        return
    }
    val praiseRes = InStationPraiseAudio.pick(avoidRawResId = null)
    if (rawVoice == null) {
        voice.playRequiredBlocking(
            assetPath = "",
            context = "ImageMatchActions.playSagaWhichWordInStationPraise(rawVoice=null)",
            chapterId = chapterId,
            stationId = stationId,
        )
    } else {
        rawVoice.playRawBlocking(praiseRes)
    }
}

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
        onWrongFeedback: (wrongWordCatalogId: String?, wrongWordAlreadySpoken: Boolean) -> Job?,
        season2HadCoachIntervention: Boolean = false,
        season2Chapter1UxStationId: Int? = null,
    ): Boolean {
        if (!gameViewModel.consumeTapCooldown()) return false
        val result = session.submitImageMatch(choiceId)
        scope.launch {
            playImageToWordTappedOptionAudio(
                choiceId = choiceId,
                audioEnabled = audioEnabled,
                chapterId = chapterId,
                voice = voice,
                rawVoice = rawVoice,
            )
            when (result) {
                AnswerResult.Correct -> {
                    cancelFeedbackVoice()
                    if (audioEnabled) ChildGameAudioHooks.onCorrect()
                    gameViewModel.inputLocked = true
                    val isLast = session.currentIndex >= session.totalQuestions - 1
                    val audioJob =
                        GameAudioActions.launchFeedbackVoiceNoCancel(
                            audioEnabled = audioEnabled,
                            scope = scope,
                            audioRuntime = audioRuntime,
                        ) {
                            if (
                                !Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
                                    season2HadCoachIntervention,
                                ) &&
                                !Season2StationQaPolicy.shouldSkipPictureToWordAssetPraiseOnLastRound(
                                    gameplayChapterId = chapterId,
                                    season2UxStationId = season2Chapter1UxStationId,
                                    isLast = isLast,
                                )
                            ) {
                                GameAudioActions.playPraiseNoImmediateRepeat(
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    candidates = ImageToWordPraiseCandidates,
                                    chapterId = chapterId,
                                    rawVoice = rawVoice,
                                )
                            }
                        }
                    GameAudioActions.joinSilently(audioJob)
                    advanceAfterRound(isLast)
                }
                AnswerResult.Wrong -> {
                    if (audioEnabled) ChildGameAudioHooks.onWrong()
                    HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                    val feedbackJob = onWrongFeedback(choiceId, true)
                    GameAudioActions.joinSilently(feedbackJob)
                }
                else -> Unit
            }
        }
        return when (result) {
            AnswerResult.Correct -> true
            AnswerResult.Wrong -> false
            AnswerResult.Finished -> false
        }
    }

    private suspend fun playImageToWordTappedOptionAudio(
        choiceId: String,
        audioEnabled: Boolean,
        chapterId: Int,
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
    ) {
        if (!audioEnabled) return
        if (usesImageToWordRawWordClips(chapterId)) {
            val wordResId = AudioClips.wordRawResIdByCatalogId(choiceId)
            if (wordResId == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required word audio. chapterId=$chapterId stationId=null " +
                        "context=ImageMatchActions.playImageToWordTappedOptionAudio stage=missing raw word mapping " +
                        "catalogId='$choiceId'",
                )
                if (rawVoice != null) {
                    rawVoice.playRawBlocking(0)
                } else {
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "ImageMatchActions.playImageToWordTappedOptionAudio(missingWordMapping,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = null,
                    )
                }
                return
            }
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required word audio. chapterId=$chapterId stationId=null " +
                        "context=ImageMatchActions.playImageToWordTappedOptionAudio stage=rawVoice=null " +
                        "expectedRawResId=$wordResId",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "ImageMatchActions.playImageToWordTappedOptionAudio(rawVoice=null)",
                    chapterId = chapterId,
                    stationId = null,
                )
                return
            }
            rawVoice.playRawBlocking(wordResId)
            return
        }
        val clip =
            AudioClips.imageToWordClipByCatalogId(
                catalogEntryId = choiceId,
                chapterId = chapterId,
                voiceHasAsset = { path -> voice.hasAsset(path) },
            )
        voice.playBlocking(clip)
    }

    fun handleImageToWordReplayCorrectChoice(
        audioEnabled: Boolean,
        cancelFeedbackVoice: () -> Unit,
        chapterId: Int,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
    ) {
        if (!audioEnabled) return
        val q = session.currentQuestion as? Question.ImageMatchQuestion ?: return
        GameAudioActions.launchPromptVoice(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
            cancelFeedbackVoice = cancelFeedbackVoice,
        ) {
            if (usesImageToWordRawWordClips(chapterId)) {
                val wordResId = AudioClips.wordRawResIdByCatalogId(q.correctChoiceId)
                if (wordResId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required word audio. chapterId=$chapterId stationId=null context=ImageMatchActions.handleImageToWordReplayCorrectChoice stage=missing raw word mapping catalogId='${q.correctChoiceId}'",
                    )
                    if (rawVoice != null) {
                        rawVoice.playRawBlocking(0)
                    } else {
                        voice.playRequiredBlocking(
                            assetPath = "",
                            context = "ImageMatchActions.handleImageToWordReplayCorrectChoice(missingWordMapping,rawVoice=null)",
                            chapterId = chapterId,
                            stationId = null,
                        )
                    }
                    return@launchPromptVoice
                }
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required word audio. chapterId=$chapterId stationId=null context=ImageMatchActions.handleImageToWordReplayCorrectChoice stage=rawVoice=null expectedRawResId=$wordResId",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "ImageMatchActions.handleImageToWordReplayCorrectChoice(rawVoice=null)",
                        chapterId = chapterId,
                        stationId = null,
                    )
                    return@launchPromptVoice
                }
                rawVoice.playRawBlocking(wordResId)
                return@launchPromptVoice
            }
            val clip =
                AudioClips.imageToWordClipByCatalogId(
                    catalogEntryId = q.correctChoiceId,
                    chapterId = chapterId,
                    voiceHasAsset = { path -> voice.hasAsset(path) },
                )
            if (voice.hasAsset(clip)) {
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
        rawVoice: RawVoicePlayer?,
        audioRuntime: GameAudioRuntimeState,
    ) {
        if (!audioEnabled) return
        GameAudioActions.launchPromptVoice(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
            cancelFeedbackVoice = cancelFeedbackVoice,
        ) {
            if (usesImageToWordRawWordClips(chapterId)) {
                val wordResId = AudioClips.wordRawResIdByCatalogId(choiceId)
                if (wordResId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required word audio. chapterId=$chapterId stationId=null context=ImageMatchActions.handleImageToWordWordPressed stage=missing raw word mapping catalogId='$choiceId'",
                    )
                    if (rawVoice != null) {
                        rawVoice.playRawBlocking(0)
                    } else {
                        voice.playRequiredBlocking(
                            assetPath = "",
                            context = "ImageMatchActions.handleImageToWordWordPressed(missingWordMapping,rawVoice=null)",
                            chapterId = chapterId,
                            stationId = null,
                        )
                    }
                    return@launchPromptVoice
                }
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required word audio. chapterId=$chapterId stationId=null context=ImageMatchActions.handleImageToWordWordPressed stage=rawVoice=null expectedRawResId=$wordResId",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "ImageMatchActions.handleImageToWordWordPressed(rawVoice=null)",
                        chapterId = chapterId,
                        stationId = null,
                    )
                    return@launchPromptVoice
                }
                rawVoice.playRawBlocking(wordResId)
                return@launchPromptVoice
            }
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
        onWrongFeedback: (wrongWordCatalogId: String?, wrongWordAlreadySpoken: Boolean) -> Job?,
        season2HadCoachIntervention: Boolean = false,
        season2Chapter1UxStationId: Int? = null,
    ): Boolean {
        if (!gameViewModel.consumeTapCooldown()) return false
        val resolvedUxStationId =
            season2Chapter1UxStationId
                ?: SixStationArcQaPolicy.earlyArcUxStationId(chapterId, stationId)
        if (
            Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                gameplayChapterId = chapterId,
                season2UxStationId = resolvedUxStationId,
            )
        ) {
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
                            playImageToWordTappedOptionAudio(
                                choiceId = choiceId,
                                audioEnabled = audioEnabled,
                                chapterId = chapterId,
                                voice = voice,
                                rawVoice = rawVoice,
                            )
                            playSagaWhichWordInStationPraise(
                                season2HadCoachIntervention = season2HadCoachIntervention,
                                rawVoice = rawVoice,
                                voice = voice,
                                chapterId = chapterId,
                                stationId = stationId,
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
                    scope.launch {
                        playImageToWordTappedOptionAudio(
                            choiceId = choiceId,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            voice = voice,
                            rawVoice = rawVoice,
                        )
                        if (audioEnabled) ChildGameAudioHooks.onWrong()
                        HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                        val feedbackJob = onWrongFeedback(choiceId, true)
                        GameAudioActions.joinSilently(feedbackJob)
                    }
                    false
                }
                AnswerResult.Finished -> false
            }
        }
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
                                val wordResId = AudioClips.wordRawResIdByCatalogId(choiceId)
                                if (wordResId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required word audio. chapterId=$chapterId stationId=$stationId context=ImageMatchActions.handleImageMatchAttempt(correct,training) stage=missing raw word mapping catalogId='$choiceId'",
                                    )
                                    if (rawVoice != null) {
                                        rawVoice.playRawBlocking(0)
                                    } else {
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "ImageMatchActions.handleImageMatchAttempt(correct,training,missingWordMapping,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                    }
                                    return@launchFeedbackVoiceNoCancel
                                }
                                if (rawVoice == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required word audio. chapterId=$chapterId stationId=$stationId context=ImageMatchActions.handleImageMatchAttempt(correct,training) stage=rawVoice=null expectedRawResId=$wordResId",
                                    )
                                    voice.playRequiredBlocking(
                                        assetPath = "",
                                        context = "ImageMatchActions.handleImageMatchAttempt(correct,training,rawVoice=null)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                    return@launchFeedbackVoiceNoCancel
                                }
                                rawVoice.playRawBlocking(wordResId)
                                GameAudioActions.playPraiseNoImmediateRepeat(
                                    voice = voice,
                                    audioRuntime = audioRuntime,
                                    candidates = ImageToWordPraiseCandidates,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    context = "ImageMatchActions.handleImageMatchPick(correct,trainingPraise)",
                                    rawVoice = rawVoice,
                                )
                            }
                            sagaEpisode &&
                                SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(
                                    chapterId,
                                    stationId,
                                ) -> {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    val wordResId = AudioClips.wordRawResIdByCatalogId(choiceId)
                                    if (wordResId == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required word audio. chapterId=$chapterId stationId=$stationId context=ImageMatchActions.handleImageMatchAttempt(correct,sagaStation5) stage=missing raw word mapping catalogId='$choiceId'",
                                        )
                                        if (rawVoice != null) {
                                            rawVoice.playRawBlocking(0)
                                        } else {
                                            voice.playRequiredBlocking(
                                                assetPath = "",
                                                context = "ImageMatchActions.handleImageMatchAttempt(correct,sagaStation5,missingWordMapping,rawVoice=null)",
                                                chapterId = chapterId,
                                                stationId = stationId,
                                            )
                                        }
                                        return@launchFeedbackVoiceNoCancel
                                    }
                                    if (rawVoice == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required word audio. chapterId=$chapterId stationId=$stationId context=ImageMatchActions.handleImageMatchAttempt(correct,sagaStation5) stage=rawVoice=null expectedRawResId=$wordResId",
                                        )
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "ImageMatchActions.handleImageMatchAttempt(correct,sagaStation5,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                        return@launchFeedbackVoiceNoCancel
                                    }
                                    rawVoice.playRawBlocking(wordResId)
                                    playSagaWhichWordInStationPraise(
                                        season2HadCoachIntervention = season2HadCoachIntervention,
                                        rawVoice = rawVoice,
                                        voice = voice,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else if (chapterId != 3 && chapterId != 6) {
                                    voice.playBlocking(AudioClips.wordClipByCatalogId(choiceId))
                                    playSagaWhichWordInStationPraise(
                                        season2HadCoachIntervention = season2HadCoachIntervention,
                                        rawVoice = rawVoice,
                                        voice = voice,
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
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
                val playTappedWordFirst =
                    sagaEpisode &&
                        SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(chapterId, stationId)
                if (playTappedWordFirst) {
                    scope.launch {
                        playImageToWordTappedOptionAudio(
                            choiceId = choiceId,
                            audioEnabled = audioEnabled,
                            chapterId = chapterId,
                            voice = voice,
                            rawVoice = rawVoice,
                        )
                        if (audioEnabled) ChildGameAudioHooks.onWrong()
                        HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                        val feedbackJob = onWrongFeedback(choiceId, true)
                        GameAudioActions.joinSilently(feedbackJob)
                    }
                } else {
                    if (audioEnabled) ChildGameAudioHooks.onWrong()
                    HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
                    when {
                        chapterId == TrainingV1Config.CHAPTER_ID &&
                            stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER ->
                            onWrongFeedback(choiceId, false)

                        else ->
                            onWrongFeedback(null, false)
                    }
                }
                false
            }
            AnswerResult.Finished -> false
        }
    }
}
