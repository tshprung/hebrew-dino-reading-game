package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer

internal object GameAudioActions {
    fun stopStagingSfx(
        sagaUsesPickLetterAudioStaging: Boolean,
        usesPopBalloonsSoundPoolPrompt: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        sfx: SoundPoolPlayer,
        stopAllStreams: Boolean,
        audioRuntime: GameAudioRuntimeState,
    ) {
        if (sagaUsesPickLetterAudioStaging) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(audioRuntime.station1VoiceStreamId)
            audioRuntime.station1VoiceStreamId = 0
        }
        if (usesPopBalloonsSoundPoolPrompt) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(audioRuntime.station2VoiceStreamId)
            audioRuntime.station2VoiceStreamId = 0
        }
        if (sagaUsesFindGridAudioStaging) {
            if (stopAllStreams) sfx.stopAllStreams()
            sfx.stopStream(audioRuntime.station3VoiceStreamId)
            audioRuntime.station3VoiceStreamId = 0
        }
    }

    fun cancelFeedbackVoice(
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        stopStagingSfx: (stopAllStreams: Boolean) -> Unit,
    ) {
        audioRuntime.feedbackVoiceJob?.cancel()
        audioRuntime.feedbackVoiceJob = null
        audioRuntime.promptVoiceJob?.cancel()
        audioRuntime.promptVoiceJob = null
        voice.stopNow()
        stopStagingSfx(true)
    }
}

