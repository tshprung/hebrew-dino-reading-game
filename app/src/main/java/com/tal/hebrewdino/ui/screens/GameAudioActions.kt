package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

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

    suspend fun playPraiseNoImmediateRepeat(
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        candidates: Array<String>,
        chapterId: Int? = null,
        rawVoice: RawVoicePlayer? = null,
    ) {
        val played =
            if (chapterId == 1 && rawVoice != null) {
                playFirstAvailableBlockingRandomizedNoRepeatWithRawOverride(
                    voice = voice,
                    rawVoice = rawVoice,
                    assetPaths = candidates,
                    avoidAssetPath = audioRuntime.lastPraiseAssetPath,
                    overrideAssetPath = AudioClips.VoPraiseHitzlacht,
                    overrideRawResId = R.raw.vo_praise_meule,
                )
            } else {
                voice.playFirstAvailableBlockingRandomizedNoRepeat(
                    assetPaths = candidates,
                    avoidAssetPath = audioRuntime.lastPraiseAssetPath,
                )
            }
        if (played != null) {
            audioRuntime.lastPraiseAssetPath = played
        }
    }

    private suspend fun playFirstAvailableBlockingRandomizedNoRepeatWithRawOverride(
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer,
        assetPaths: Array<String>,
        avoidAssetPath: String?,
        overrideAssetPath: String,
        overrideRawResId: Int,
        random: Random = Random.Default,
    ): String? {
        val n = assetPaths.size
        if (n == 0) return null
        val start = random.nextInt(n)

        for (k in 0 until n) {
            val p = assetPaths[(start + k) % n]
            if (p.isBlank()) continue
            if (avoidAssetPath != null && p == avoidAssetPath) continue
            if (!voice.hasAsset(p)) continue
            if (p == overrideAssetPath) {
                rawVoice.playRawBlocking(overrideRawResId)
                return p
            }
            voice.playBlocking(p)
            return p
        }

        if (avoidAssetPath != null) {
            for (k in 0 until n) {
                val p = assetPaths[(start + k) % n]
                if (p.isBlank()) continue
                if (!voice.hasAsset(p)) continue
                if (p == overrideAssetPath) {
                    rawVoice.playRawBlocking(overrideRawResId)
                    return p
                }
                voice.playBlocking(p)
                return p
            }
        }

        return null
    }
}
