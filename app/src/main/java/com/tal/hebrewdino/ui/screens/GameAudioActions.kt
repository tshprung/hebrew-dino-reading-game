package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.InStationPraiseAudio
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.TrainingV1Config
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
        chapterId: Int? = null,
        stationId: Int? = null,
        context: String,
    ) {
        sfx.stopAllStreams()
        sfx.playRequiredReturningStreamId(
            assetPath = intro,
            volume = 1f,
            context = context,
            chapterId = chapterId,
            stationId = stationId,
        )
        val lead =
            (introMs * leadFraction)
                .toLong()
                .coerceIn(16L, introMs)
        delay(((lead + extraPauseMs) * delayScale).toLong())
        sfx.playRequiredReturningStreamId(
            assetPath = letter,
            volume = 1f,
            context = context,
            chapterId = chapterId,
            stationId = stationId,
        )
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
        delay(STATION_TO_REWARD_AUDIO_HANDOFF_MS)
    }

    suspend fun playPraiseNoImmediateRepeat(
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        candidates: Array<String>,
        @Suppress("UNUSED_PARAMETER") playerAddress: PlayerAddress? = null,
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

    private suspend fun playRequiredBlockingRandomizedNoRepeatWithRawOverride(
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
        assetPaths: Array<String>,
        avoidAssetPath: String?,
        overrideAssetPath: String,
        overrideRawResId: Int,
        chapterId: Int,
        stationId: Int?,
        context: String,
        random: Random = Random.Default,
    ): String? {
        val n = assetPaths.size
        if (n == 0) {
            voice.playRequiredBlocking(
                assetPath = "",
                context = "$context(empty candidates)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return null
        }
        val start = random.nextInt(n)

        for (k in 0 until n) {
            val p = assetPaths[(start + k) % n]
            if (p.isBlank()) continue
            if (avoidAssetPath != null && p == avoidAssetPath) continue
            if (p == overrideAssetPath) {
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required praise raw override. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedRawRes=$overrideRawResId expectedAssetPath='$overrideAssetPath'",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "$context(rawVoice=null for override)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return null
                }
                rawVoice.playRawBlocking(overrideRawResId)
                return p
            }
            voice.playRequiredBlocking(
                assetPath = p,
                context = "$context(praise)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return p
        }

        if (avoidAssetPath != null) {
            for (k in 0 until n) {
                val p = assetPaths[(start + k) % n]
                if (p.isBlank()) continue
                if (p == overrideAssetPath) {
                    if (rawVoice == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required praise raw override. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedRawRes=$overrideRawResId expectedAssetPath='$overrideAssetPath'",
                        )
                        voice.playRequiredBlocking(
                            assetPath = "",
                            context = "$context(rawVoice=null for override)",
                            chapterId = chapterId,
                            stationId = stationId,
                        )
                        return null
                    }
                    rawVoice.playRawBlocking(overrideRawResId)
                    return p
                }
                voice.playRequiredBlocking(
                    assetPath = p,
                    context = "$context(praise)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return p
            }
        }

        voice.playRequiredBlocking(
            assetPath = "",
            context = "$context(no selectable candidates)",
            chapterId = chapterId,
            stationId = stationId,
        )
        return null
    }
}
