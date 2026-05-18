package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.Job

internal object GameAudioActions {
    fun stopStagingSfx(
        sagaUsesPickLetterAudioStaging: Boolean,
        usesPopBalloonsSoundPoolPrompt: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        sfx: SoundPoolPlayer,
        stopAllStreams: Boolean,
        getStation1VoiceStreamId: () -> Int,
        setStation1VoiceStreamId: (Int) -> Unit,
        getStation2VoiceStreamId: () -> Int,
        setStation2VoiceStreamId: (Int) -> Unit,
        getStation3VoiceStreamId: () -> Int,
        setStation3VoiceStreamId: (Int) -> Unit,
    ) {
        if (sagaUsesPickLetterAudioStaging) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(getStation1VoiceStreamId())
            setStation1VoiceStreamId(0)
        }
        if (usesPopBalloonsSoundPoolPrompt) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(getStation2VoiceStreamId())
            setStation2VoiceStreamId(0)
        }
        if (sagaUsesFindGridAudioStaging) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(getStation3VoiceStreamId())
            setStation3VoiceStreamId(0)
        }
    }

    fun cancelFeedbackVoice(
        voice: VoicePlayer,
        getFeedbackVoiceJob: () -> Job?,
        setFeedbackVoiceJob: (Job?) -> Unit,
        getPromptVoiceJob: () -> Job?,
        setPromptVoiceJob: (Job?) -> Unit,
        stopStagingSfx: (stopAllStreams: Boolean) -> Unit,
    ) {
        getFeedbackVoiceJob()?.cancel()
        setFeedbackVoiceJob(null)
        getPromptVoiceJob()?.cancel()
        setPromptVoiceJob(null)
        voice.stopNow()
        stopStagingSfx(true)
    }
}

