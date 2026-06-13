package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.StationHintMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.StationReplayMode
import com.tal.hebrewdino.ui.domain.StationUiSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal object SideHelpActions {
    fun startReplay(
        audioEnabled: Boolean,
        isPlayPhase: Boolean,
        episode4HelpEnabled: Boolean,
        popBalloonsHelpEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
        stationUiSpec: StationUiSpec,
        session: LevelSession,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        rawVoice: RawVoicePlayer,
        cancelFeedbackVoice: () -> Unit,
        audioRuntime: GameAudioRuntimeState,
        scope: CoroutineScope,
    ) {
        if (!isPlayPhase) return
        if (episode4HelpEnabled) {
            if (!audioEnabled) return
            val q = session.currentQuestion ?: return
            sfx.stopAllStreams()
            GameAudioActions.launchPromptVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) replay@{
                if (plan.highlightedLetterInWordPickLetter && q is Question.PopBalloonsQuestion) {
                    val round = session.highlightedLetterInWordRound() ?: return@replay
                    val wordResId = AudioClips.wordRawResIdByCatalogId(round.catalogId)
                    if (wordResId == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required help replay word audio. chapterId=$chapterId stationId=$stationId context=SideHelpActions.startReplay(highlightedLetterInWordPickLetter) stage=missing raw word mapping catalogId='${round.catalogId}'",
                        )
                        rawVoice.playRawBlocking(0)
                        return@replay
                    }
                    val letterResId = AudioClips.letterNameRawResId(round.correctLetter)
                    if (letterResId == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required help replay letter-name audio. chapterId=$chapterId stationId=$stationId context=SideHelpActions.startReplay(highlightedLetterInWordPickLetter) stage=missing raw letter-name mapping letter='${round.correctLetter}'",
                        )
                        rawVoice.playRawBlocking(0)
                        return@replay
                    }
                    rawVoice.playRawBlocking(wordResId)
                    rawVoice.playRawBlocking(letterResId)
                    return@replay
                }
                if (plan.popAllLettersInWord && q is Question.PopBalloonsQuestion) {
                    val catalogId = session.chapter3PopAllLettersCurrentWord()?.second
                    if (!catalogId.isNullOrBlank()) {
                        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
                        if (wordResId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required help replay word audio. chapterId=$chapterId stationId=$stationId context=SideHelpActions.startReplay(popAllLettersInWord) stage=missing raw word mapping catalogId='$catalogId'",
                            )
                            rawVoice.playRawBlocking(0)
                            return@replay
                        }
                        rawVoice.playRawBlocking(wordResId)
                    }
                    return@replay
                }
                when (stationUiSpec.replayMode) {
                    StationReplayMode.TargetLetterOnly -> {
                        val letter = Episode4Help.targetLetterForHelpHint(q) ?: return@replay
                        val resId = AudioClips.letterNameRawResId(letter)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required help replay letter-name audio. chapterId=$chapterId stationId=$stationId context=SideHelpActions.startReplay(TargetLetterOnly) stage=missing raw letter-name mapping letter='$letter'",
                            )
                            rawVoice.playRawBlocking(0)
                            return@replay
                        }
                        rawVoice.playRawBlocking(resId)
                    }
                    StationReplayMode.TargetWordOnly -> {
                        if (
                            q is Question.WordPartsQuestion ||
                                q is Question.MissingFirstLetterQuestion ||
                                q is Question.RhymingQuestion
                        ) {
                            Season2StationAudio.replayAdvancedInstructionAndWord(
                                q = q,
                                chapterId = chapterId,
                                stationId = stationId,
                                rawVoice = rawVoice,
                            )
                            return@replay
                        }
                        if (
                            q is Question.ImageMatchQuestion &&
                                Season2StationAudio.isPictureToWordStation(chapterId, stationId)
                        ) {
                            Season2StationAudio.speakPictureToWordRoundPrompt(
                                chapterId = chapterId,
                                stationId = stationId,
                                catalogId = q.correctChoiceId,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@replay
                        }
                        val catalogId =
                            when (q) {
                                is Question.PictureStartsWithQuestion -> q.catalogEntryId
                                is Question.ImageMatchQuestion -> q.correctChoiceId
                                else -> null
                            }
                        if (catalogId != null) {
                            val resId = AudioClips.wordRawResIdByCatalogId(catalogId)
                            if (resId == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required help replay word audio. chapterId=$chapterId stationId=$stationId context=SideHelpActions.startReplay(TargetWordOnly) stage=missing raw word mapping catalogId='$catalogId'",
                                )
                                rawVoice.playRawBlocking(0)
                                return@replay
                            }
                            rawVoice.playRawBlocking(resId)
                        }
                    }
                    else ->
                        replayEpisode4Stations15RoundAudio(
                            sfx = sfx,
                            voice = voice,
                            chapterId = chapterId,
                            stationId = stationId,
                            q = q,
                            rawVoice = rawVoice,
                        )
                }
            }
            return
        }
        if (popBalloonsHelpEnabled) {
            if (!audioEnabled) return
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return
            val letter = q.correctAnswer
            GameAudioActions.launchPromptVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) replay@{
                val resId = AudioClips.letterNameRawResId(letter)
                if (resId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required help replay letter-name audio. chapterId=$chapterId stationId=$stationId context=SideHelpActions.startReplay(PopBalloonsHelp) stage=missing raw letter-name mapping letter='$letter'",
                    )
                    rawVoice.playRawBlocking(0)
                    return@replay
                }
                rawVoice.playRawBlocking(resId)
            }
        }
    }

    fun performHint(
        isPlayPhase: Boolean,
        episode4HelpEnabled: Boolean,
        popBalloonsHelpEnabled: Boolean,
        stationId: Int,
        stationUiSpec: StationUiSpec,
        session: LevelSession,
        gameViewModel: GameViewModel,
        scope: CoroutineScope,
    ) {
        if (!isPlayPhase) return
        if (episode4HelpEnabled) {
            val q = session.currentQuestion ?: return
            if (gameViewModel.episode4HelpLocksChoices) return
            if (q is Question.WordPartsQuestion && stationUiSpec.hintMode == StationHintMode.TemporaryFullWord) {
                gameViewModel.episode4HelpLocksChoices = true
                gameViewModel.wordPartsHintRevealWord = q.word
                val duration = stationUiSpec.hintDurationMs ?: Episode4Help.HINT_REVEAL_FALLBACK_MS
                gameViewModel.episode4HelpClearJob?.cancel()
                gameViewModel.episode4HelpClearJob =
                    scope.launch {
                        delay(duration.milliseconds)
                        gameViewModel.wordPartsHintRevealWord = null
                        gameViewModel.episode4HelpLocksChoices = false
                        gameViewModel.episode4HelpClearJob = null
                    }
                return
            }
            val letter = Episode4Help.targetLetterForHelpHint(q)
            gameViewModel.episode4HelpLocksChoices = true
            gameViewModel.episode4HelpActiveHintLetter = letter
            if (stationId == Chapter1StationOrder.BALLOON_POP) {
                gameViewModel.episode4Station2BalloonHintEpoch += 1
            }
            if (stationUiSpec.findGridUseEpisode4HelpHints && q is Question.FindLetterGridQuestion) {
                gameViewModel.episode4Station3GridHintEpoch += 1
            }
            val duration = stationUiSpec.hintDurationMs ?: Episode4Help.HINT_REVEAL_FALLBACK_MS
            gameViewModel.episode4HelpClearJob?.cancel()
            gameViewModel.episode4HelpClearJob =
                scope.launch {
                    delay(duration.milliseconds)
                    gameViewModel.episode4HelpActiveHintLetter = null
                    gameViewModel.episode4HelpLocksChoices = false
                    gameViewModel.episode4HelpClearJob = null
                }
            return
        }
        if (popBalloonsHelpEnabled) {
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return
            if (gameViewModel.balloonHelpLocksChoices) return
            gameViewModel.balloonHelpLocksChoices = true
            gameViewModel.balloonHelpHintLetter = q.correctAnswer
            gameViewModel.balloonHelpClearJob?.cancel()
            gameViewModel.balloonHelpClearJob =
                scope.launch {
                    delay(Episode4Help.HINT_REVEAL_FALLBACK_MS.milliseconds)
                    gameViewModel.balloonHelpHintLetter = null
                    gameViewModel.balloonHelpLocksChoices = false
                    gameViewModel.balloonHelpClearJob = null
                }
        }
    }
}
