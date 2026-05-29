package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationTemplateId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object GameRoundStartActions {
    suspend fun run(
        gameViewModel: GameViewModel,
        audioEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        listenOnlyTargetPrompt: Boolean,
        stationTemplateId: StationTemplateId,
        planPopAllLettersInWord: Boolean,
        sagaUsesPickLetterAudioStaging: Boolean,
        sagaUsesPopBalloonsAudioStaging: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        isChapter3HighlightedLetterInWordStation: Boolean,
        isChapter3AudioLetterRecognitionStation: Boolean,
        session: LevelSession,
        scope: CoroutineScope,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        introDurationMs: Long,
        station1IntroLetterLeadFraction: Float,
        station1IntroToLetterLeadScale: Float,
        station2BalloonIntroLetterLeadFraction: Float,
        station2IntroToLetterLeadScale: Float,
        station2BalloonIntroToLetterGapBoost: Float,
        station2BalloonIntroToLetterExtraPauseMs: Long,
        station4IntroWordLeadFraction: Float,
        station4IntroToWordLeadScale: Float,
        station4IntroToWordGapBoost: Float,
        station4IntroToWordExtraPauseMs: Long,
        cancelFeedbackVoice: () -> Unit,
        audioRuntime: GameAudioRuntimeState,
        chapter1PlayerAddress: PlayerAddress? = null,
        rawVoice: RawVoicePlayer? = null,
    ) {
        gameViewModel.phase = GamePhase.Intro
        gameViewModel.inputLocked = true
        gameViewModel.wrongTapsThisQuestion = 0
        gameViewModel.correctTapPulseLetter = null
        gameViewModel.station2CorrectPopCount = 0
        gameViewModel.station4WrongFlashLetter = null
        gameViewModel.station4WrongFlashEpoch = 0
        gameViewModel.station4PinnedCorrectLetter = null
        gameViewModel.resetEpisode4HelpForNewQuestion()
        gameViewModel.resetBalloonHelpForNewQuestion()
        gameViewModel.station1PinnedCorrectLetter = null
        gameViewModel.station2PinnedBalloonLetter = null
        gameViewModel.station2PinnedBalloonColor = null
        gameViewModel.dinoTalking = false
        cancelFeedbackVoice()
        val q: Question = session.currentQuestion ?: return

        GameAudioActions.launchPromptVoiceNoCancel(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
        ) {
            gameViewModel.dinoTalking = true
            try {
                playIntroPrompt(
                    audioEnabled = true,
                    chapterId = chapterId,
                    stationId = stationId,
                    listenOnlyTargetPrompt = listenOnlyTargetPrompt,
                    stationTemplateId = stationTemplateId,
                    planPopAllLettersInWord = planPopAllLettersInWord,
                    isSagaEpisode = isSagaEpisode(chapterId),
                    sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                    sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                    sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                    isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                    isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                    session = session,
                    q = q,
                    voice = voice,
                    sfx = sfx,
                    station1IntroLetterLeadFraction = station1IntroLetterLeadFraction,
                    station1IntroToLetterLeadScale = station1IntroToLetterLeadScale,
                    station2BalloonIntroLetterLeadFraction = station2BalloonIntroLetterLeadFraction,
                    station2IntroToLetterLeadScale = station2IntroToLetterLeadScale,
                    station2BalloonIntroToLetterGapBoost = station2BalloonIntroToLetterGapBoost,
                    station2BalloonIntroToLetterExtraPauseMs = station2BalloonIntroToLetterExtraPauseMs,
                    station4IntroWordLeadFraction = station4IntroWordLeadFraction,
                    station4IntroToWordLeadScale = station4IntroToWordLeadScale,
                    station4IntroToWordGapBoost = station4IntroToWordGapBoost,
                    station4IntroToWordExtraPauseMs = station4IntroToWordExtraPauseMs,
                    chapter1PlayerAddress = chapter1PlayerAddress,
                    rawVoice = rawVoice,
                )
            } finally {
                gameViewModel.dinoTalking = false
            }
        }

        scope.launch {
            if (audioEnabled) {
                sfx.preload(
                    AudioClips.SfxCorrect,
                    AudioClips.SfxWrong,
                    AudioClips.SfxBalloonPopSoft,
                    AudioClips.SfxBalloonPop,
                    AudioClips.SfxBalloonPopWrongFunny,
                )
            }
        }

        delay(introDurationMs)
        gameViewModel.phase = GamePhase.Play
        gameViewModel.inputLocked = false
        gameViewModel.entryPulseEpoch += 1
        if (sagaUsesFindGridAudioStaging && session.currentIndex == 0) {
            gameViewModel.hintPulseEpoch += 1
        }
    }
}
