package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationReplayMode
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                    val wordPath = AudioClips.wordClipByCatalogId(round.catalogId)
                    val letterClip = AudioClips.letterNameClip(round.correctLetter)
                    val parts =
                        buildList {
                            if (voice.hasAsset(wordPath)) add(wordPath)
                            if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
                        }
                    if (parts.isNotEmpty()) {
                        voice.playSequenceBlocking(parts)
                    } else {
                        if (letterClip != null) speakLetterPrompt(voice, round.correctLetter)
                    }
                    return@replay
                }
                if (plan.popAllLettersInWord && q is Question.PopBalloonsQuestion) {
                    val catalogId = session.chapter3PopAllLettersCurrentWord()?.second
                    if (!catalogId.isNullOrBlank()) {
                        val wordPath = AudioClips.wordClipByCatalogId(catalogId)
                        if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                    }
                    return@replay
                }
                when (stationUiSpec.replayMode) {
                    StationReplayMode.TargetLetterOnly -> {
                        val letter = Episode4Help.targetLetterForHelpHint(q) ?: return@replay
                        if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
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
                            return@replay
                        }
                        val letterClip = AudioClips.letterNameClip(letter)
                        if (letterClip != null) {
                            val id =
                                sfx.playRequiredReturningStreamId(
                                    assetPath = letterClip,
                                    volume = 1f,
                                    context = "SideHelpActions.startReplay(TargetLetterOnly)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            if (id != null) return@replay
                            if (voice.hasAsset(letterClip)) {
                                voice.playBlocking(letterClip)
                                return@replay
                            }
                        }
                        if (chapterId == TrainingV1Config.CHAPTER_ID) return@replay
                        speakLetterPrompt(
                            voice = voice,
                            letter = letter,
                            chapterId = chapterId,
                            stationId = stationId,
                            context = "SideHelpActions.startReplay(TargetLetterOnly,fallback)",
                        )
                    }
                    StationReplayMode.TargetWordOnly -> {
                        if (q is Question.PictureStartsWithQuestion) {
                            val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                            if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
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
                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
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
                    return@replay
                }
                val letterClip = AudioClips.letterNameClip(letter)
                if (letterClip != null && voice.hasAsset(letterClip)) {
                    voice.playBlocking(letterClip)
                    return@replay
                }
                speakLetterPrompt(
                    voice = voice,
                    letter = letter,
                    chapterId = chapterId,
                    stationId = stationId,
                    context = "SideHelpActions.startReplay(PopBalloonsHelp,fallback)",
                )
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
            val letter = Episode4Help.targetLetterForHelpHint(q)
            if (gameViewModel.episode4HelpLocksChoices) return
            gameViewModel.episode4HelpLocksChoices = true
            gameViewModel.episode4HelpActiveHintLetter = letter
            if (stationId == Chapter1StationOrder.BALLOON_POP) {
                gameViewModel.episode4Station2BalloonHintEpoch += 1
            }
            if (stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE) {
                gameViewModel.episode4Station3GridHintEpoch += 1
            }
            val duration = stationUiSpec.hintDurationMs ?: Episode4Help.HINT_REVEAL_FALLBACK_MS
            gameViewModel.episode4HelpClearJob?.cancel()
            gameViewModel.episode4HelpClearJob =
                scope.launch {
                    delay(duration)
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
                    delay(Episode4Help.HINT_REVEAL_FALLBACK_MS)
                    gameViewModel.balloonHelpHintLetter = null
                    gameViewModel.balloonHelpLocksChoices = false
                    gameViewModel.balloonHelpClearJob = null
                }
        }
    }
}
