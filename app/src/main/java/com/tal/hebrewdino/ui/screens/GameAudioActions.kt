package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.InStationPraiseAudio
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

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

    suspend fun await(
        job: Job?,
        timeoutMs: Long,
    ) {
        withTimeoutOrNull(timeoutMs.milliseconds) { job?.join() }
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

    suspend fun awaitTrackedVoices(
        audioRuntime: GameAudioRuntimeState,
        timeoutMs: Long,
    ) {
        withTimeoutOrNull(timeoutMs.milliseconds) {
            audioRuntime.feedbackVoiceJob?.join()
            audioRuntime.promptVoiceJob?.join()
        }
    }

    /**
     * Lets station feedback/praise finish, stops gameplay voice, then yields briefly so
     * [RawVoicePlayer.release] on [GameScreen] dispose does not cut into reward speech.
     */
    internal const val STATION_TO_REWARD_AUDIO_HANDOFF_MS: Long = 80L

    internal const val STATION_TO_REWARD_VOICE_DRAIN_MS: Long = 15_000L

    suspend fun finishStationVoiceBeforeReward(
        audioRuntime: GameAudioRuntimeState,
        cancelFeedbackVoice: () -> Unit,
        voiceDrainTimeoutMs: Long = STATION_TO_REWARD_VOICE_DRAIN_MS,
    ) {
        awaitTrackedVoices(audioRuntime, voiceDrainTimeoutMs)
        cancelFeedbackVoice()
        delay(STATION_TO_REWARD_AUDIO_HANDOFF_MS.milliseconds)
    }

    suspend fun playPraiseNoImmediateRepeat(
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        candidates: Array<String>,
        chapterId: Int? = null,
        stationId: Int? = null,
        context: String = "GameAudioActions.playPraiseNoImmediateRepeat",
        rawVoice: RawVoicePlayer? = null,
    ) {
        val played =
            if (InStationPraiseAudio.usesRawPraisePool(chapterId)) {
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required praise audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedRawResId!=null",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "$context(rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    null
                } else {
                    val avoid =
                        InStationPraiseAudio.rawResIdFromTrackingKey(audioRuntime.lastPraiseAssetPath)
                    val rawResId = InStationPraiseAudio.pick(avoidRawResId = avoid)
                    rawVoice.playRawBlocking(rawResId)
                    InStationPraiseAudio.trackingKey(rawResId)
                }
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

}
