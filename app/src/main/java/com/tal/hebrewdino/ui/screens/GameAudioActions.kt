package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal object GameAudioActions {
    fun cancelFeedbackVoice(
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        audioRuntime: GameAudioRuntimeState,
    ) {
        audioRuntime.feedbackVoiceJob?.cancel()
        audioRuntime.feedbackVoiceJob = null
        audioRuntime.promptVoiceJob?.cancel()
        audioRuntime.promptVoiceJob = null
        voice.stopNow()
        sfx.stopAllStreams()
    }

    fun launchFeedbackVoice(
        audioEnabled: Boolean,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        cancelFeedbackVoice: () -> Unit,
        cancelBeforeStart: Boolean = true,
        play: suspend () -> Unit,
    ): Job? {
        if (!audioEnabled) return null
        if (cancelBeforeStart) cancelFeedbackVoice()
        val job = scope.launch { play() }
        audioRuntime.feedbackVoiceJob = job
        job.invokeOnCompletion {
            if (audioRuntime.feedbackVoiceJob === job) {
                audioRuntime.feedbackVoiceJob = null
            }
        }
        return job
    }
}

