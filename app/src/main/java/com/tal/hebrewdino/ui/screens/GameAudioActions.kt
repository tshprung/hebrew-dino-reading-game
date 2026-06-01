package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
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

    suspend fun playPraiseNoImmediateRepeat(
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        candidates: Array<String>,
        playerAddress: PlayerAddress? = null,
        chapterId: Int? = null,
        stationId: Int? = null,
        context: String = "GameAudioActions.playPraiseNoImmediateRepeat",
        rawVoice: RawVoicePlayer? = null,
    ) {
        val requiredChapter =
            chapterId == 1 ||
                chapterId == 2 ||
                chapterId == 3 ||
                chapterId == 4 ||
                chapterId == 5 ||
                chapterId == 6 ||
                chapterId == TrainingV1Config.CHAPTER_ID
        val played =
            if (requiredChapter) {
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
                    val addressSpecific =
                        playerAddress?.let { Chapter1AddressAwareAudio.greatRawRes(it) }
                    val options =
                        buildList(2) {
                            if (addressSpecific != null) {
                                add("raw:feedback_great" to addressSpecific)
                            }
                            add("raw:vo_praise_meule" to R.raw.vo_praise_meule)
                        }.toTypedArray()
                    val avoid = audioRuntime.lastPraiseAssetPath
                    val start = Random.nextInt(options.size)
                    val pick =
                        (0 until options.size)
                            .asSequence()
                            .map { options[(start + it) % options.size] }
                            .firstOrNull { (k, _) -> avoid == null || k != avoid }
                            ?: options.first()
                    rawVoice.playRawBlocking(pick.second)
                    pick.first
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
