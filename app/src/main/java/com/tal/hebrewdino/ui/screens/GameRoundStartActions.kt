package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationTemplateId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object GameRoundStartActions {
    suspend fun run(
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
        setPhase: (GamePhase) -> Unit,
        setInputLocked: (Boolean) -> Unit,
        setWrongTapsThisQuestion: (Int) -> Unit,
        setCorrectTapPulseLetter: (String?) -> Unit,
        clearStation4WrongFlashLetter: () -> Unit,
        clearStation4PinnedCorrectLetter: () -> Unit,
        resetEpisode4HelpForNewQuestion: () -> Unit,
        resetBalloonHelpForNewQuestion: () -> Unit,
        clearStation1PinnedCorrectLetter: () -> Unit,
        clearPinnedBalloon: () -> Unit,
        cancelFeedbackVoice: () -> Unit,
        setPromptVoiceJob: (Job?) -> Unit,
        setDinoTalking: (Boolean) -> Unit,
        bumpEntryPulseEpoch: () -> Unit,
        bumpHintPulseEpoch: () -> Unit,
    ) {
        setPhase(GamePhase.Intro)
        setInputLocked(true)
        setWrongTapsThisQuestion(0)
        setCorrectTapPulseLetter(null)
        clearStation4WrongFlashLetter()
        clearStation4PinnedCorrectLetter()
        resetEpisode4HelpForNewQuestion()
        resetBalloonHelpForNewQuestion()
        clearStation1PinnedCorrectLetter()
        clearPinnedBalloon()
        cancelFeedbackVoice()
        val q: Question = session.currentQuestion ?: return

        setPromptVoiceJob(
            scope.launch {
                if (audioEnabled) {
                    setDinoTalking(true)
                    try {
                        playIntroPrompt(
                            audioEnabled = audioEnabled,
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
                        )
                    } finally {
                        setDinoTalking(false)
                    }
                }
            },
        )

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
        setPhase(GamePhase.Play)
        setInputLocked(false)
        bumpEntryPulseEpoch()
        if (sagaUsesFindGridAudioStaging && session.currentIndex == 0) {
            bumpHintPulseEpoch()
        }
    }
}
