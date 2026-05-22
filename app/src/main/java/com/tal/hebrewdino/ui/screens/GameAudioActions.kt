package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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

    private fun setFeedbackVoiceJob(
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

    private fun setPromptVoiceJob(
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
        play: suspend () -> Unit,
    ): Job? {
        cancelFeedbackVoice()
        return launchFeedbackVoiceNoCancel(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
            play = play,
        )
    }

    fun launchFeedbackVoiceNoCancel(
        audioEnabled: Boolean,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        play: suspend () -> Unit,
    ): Job? {
        if (!audioEnabled) return null
        val job = scope.launch { play() }
        setFeedbackVoiceJob(audioRuntime, job)
        return job
    }

    fun launchPromptVoice(
        audioEnabled: Boolean,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        cancelFeedbackVoice: () -> Unit,
        play: suspend () -> Unit,
    ): Job? {
        cancelFeedbackVoice()
        return launchPromptVoiceNoCancel(
            audioEnabled = audioEnabled,
            scope = scope,
            audioRuntime = audioRuntime,
            play = play,
        )
    }

    fun launchPromptVoiceNoCancel(
        audioEnabled: Boolean,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        play: suspend () -> Unit,
    ): Job? {
        if (!audioEnabled) return null
        val job = scope.launch { play() }
        setPromptVoiceJob(audioRuntime, job)
        return job
    }

    suspend fun playSoundPoolIntroWithOverlappedLetter(
        sfx: SoundPoolPlayer,
        intro: String,
        introMs: Long,
        letter: String,
        leadFraction: Float,
        extraPauseMs: Long,
        delayScale: Float,
    ) {
        sfx.stopAllStreams()
        sfx.playReturningStreamId(intro, volume = 1f)
        val lead =
            (introMs * leadFraction)
                .toLong()
                .coerceIn(16L, introMs)
        delay(((lead + extraPauseMs) * delayScale).toLong())
        sfx.playReturningStreamId(letter, volume = 1f)
    }

    suspend fun await(
        job: Job?,
        timeoutMs: Long,
    ) {
        withTimeoutOrNull(timeoutMs) { job?.join() }
    }

    suspend fun joinSilently(job: Job?) {
        runCatching { job?.join() }
    }

    suspend fun awaitFeedbackVoice(
        audioRuntime: GameAudioRuntimeState,
        timeoutMs: Long,
    ) {
        await(audioRuntime.feedbackVoiceJob, timeoutMs)
    }

    suspend fun awaitPromptVoice(
        audioRuntime: GameAudioRuntimeState,
        timeoutMs: Long,
    ) {
        await(audioRuntime.promptVoiceJob, timeoutMs)
    }

    suspend fun awaitTrackedVoices(
        audioRuntime: GameAudioRuntimeState,
        timeoutMs: Long,
    ) {
        withTimeoutOrNull(timeoutMs) {
            audioRuntime.feedbackVoiceJob?.join()
            audioRuntime.promptVoiceJob?.join()
        }
    }
}
