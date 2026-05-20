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

    fun setFeedbackVoiceJob(
        audioRuntime: GameAudioRuntimeState,
        job: Job?,
    ) {
        audioRuntime.feedbackVoiceJob = job
        job?.invokeOnCompletion {
            if (audioRuntime.feedbackVoiceJob === job) {
                audioRuntime.feedbackVoiceJob = null
            }
        }
    }

    fun setPromptVoiceJob(
        audioRuntime: GameAudioRuntimeState,
        job: Job?,
    ) {
        audioRuntime.promptVoiceJob = job
        job?.invokeOnCompletion {
            if (audioRuntime.promptVoiceJob === job) {
                audioRuntime.promptVoiceJob = null
            }
        }
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
        setFeedbackVoiceJob(audioRuntime, job)
        return job
    }

    fun launchFeedbackVoiceAfterCancel(
        audioEnabled: Boolean,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        cancelFeedbackVoice: () -> Unit,
        play: suspend () -> Unit,
    ): Job? {
        cancelFeedbackVoice()
        return launchFeedbackVoice(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
            cancelFeedbackVoice = cancelFeedbackVoice,
            cancelBeforeStart = false,
            play = play,
        )
    }
}

