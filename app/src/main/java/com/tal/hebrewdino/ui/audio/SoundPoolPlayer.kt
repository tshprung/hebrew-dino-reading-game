package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

class SoundPoolPlayer(context: Context) {
    companion object {
        private const val TAG: String = "SoundPoolPlayer"
        private const val MISSING_TAG: String = "MissingContent"
        private const val FLAG_DEBUGGABLE: Int = 0x2

        private val Registry: MutableSet<SoundPoolPlayer> =
            Collections.newSetFromMap(WeakHashMap())

        fun stopAllNow() {
            val snapshot = synchronized(Registry) { Registry.toList() }
            for (p in snapshot) {
                p.stopAllStreams()
            }
        }
    }

    private val appContext = context.applicationContext
    private val isDebuggable: Boolean =
        (appContext.applicationInfo.flags and FLAG_DEBUGGABLE) != 0

    private val soundPool: SoundPool? =
        runCatching {
            SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .build()
        }.getOrNull()

    private val loadedSoundIdByPath = ConcurrentHashMap<String, Int>()
    private val readySoundIds = ConcurrentHashMap<Int, Boolean>()
    private val pendingLoads = ConcurrentHashMap<Int, MutableList<(Boolean) -> Unit>>()
    private val durationMsByPath = ConcurrentHashMap<String, Long>()
    private val activeStreamIds = ConcurrentHashMap<Int, Boolean>()

    init {
        synchronized(Registry) { Registry.add(this) }
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            val ok = status == 0
            readySoundIds[sampleId] = ok
            pendingLoads.remove(sampleId)?.forEach { cb -> cb(ok) }
        }
    }

    suspend fun play(assetPath: String, volume: Float = 1f, rate: Float = 1f) {
        val pool = soundPool ?: return
        val soundId = loadIfNeeded(pool, assetPath) ?: return
        val streamId = pool.play(soundId, volume, volume, 1, 0, rate.coerceIn(0.8f, 1.25f))
        if (streamId != 0) activeStreamIds[streamId] = true
    }

    /**
     * Plays and returns the active stream id so callers can stop it immediately.
     * Returns null if the asset couldn't be loaded/played.
     */
    suspend fun playReturningStreamId(assetPath: String, volume: Float = 1f, rate: Float = 1f): Int? {
        val pool = soundPool ?: return null
        val soundId = loadIfNeeded(pool, assetPath) ?: return null
        val streamId = pool.play(soundId, volume, volume, 1, 0, rate.coerceIn(0.8f, 1.25f))
        if (streamId != 0) activeStreamIds[streamId] = true
        return if (streamId != 0) streamId else null
    }

    suspend fun playRequiredReturningStreamId(
        assetPath: String,
        volume: Float = 1f,
        rate: Float = 1f,
        context: String,
        chapterId: Int? = null,
        stationId: Int? = null,
    ): Int? {
        if (assetPath.isBlank()) {
            reportRequiredFailure(
                assetPath = assetPath,
                stage = "assetPath blank",
                context = context,
                chapterId = chapterId,
                stationId = stationId,
                cause = null,
            )
            return null
        }
        val pool =
            soundPool
                ?: run {
                    reportRequiredFailure(
                        assetPath = assetPath,
                        stage = "SoundPool unavailable",
                        context = context,
                        chapterId = chapterId,
                        stationId = stationId,
                        cause = null,
                    )
                    return null
                }
        val soundId = loadIfNeededRequired(pool, assetPath, context, chapterId, stationId) ?: return null
        val streamId =
            runCatching { pool.play(soundId, volume, volume, 1, 0, rate.coerceIn(0.8f, 1.25f)) }
                .getOrElse { t ->
                    reportRequiredFailure(
                        assetPath = assetPath,
                        stage = "SoundPool.play threw",
                        context = context,
                        chapterId = chapterId,
                        stationId = stationId,
                        cause = t,
                    )
                    return null
                }
        if (streamId == 0) {
            reportRequiredFailure(
                assetPath = assetPath,
                stage = "SoundPool.play returned streamId=0",
                context = context,
                chapterId = chapterId,
                stationId = stationId,
                cause = null,
            )
            return null
        }
        activeStreamIds[streamId] = true
        return streamId
    }

    suspend fun durationMsRequiredOrNull(
        assetPath: String,
        context: String,
        chapterId: Int? = null,
        stationId: Int? = null,
    ): Long? {
        val ms =
            runCatching { durationMs(assetPath) }
                .getOrElse { t ->
                    reportRequiredFailure(
                        assetPath = assetPath,
                        stage = "durationMs threw",
                        context = context,
                        chapterId = chapterId,
                        stationId = stationId,
                        cause = t,
                    )
                    return null
                }
        if (ms == null) {
            reportRequiredFailure(
                assetPath = assetPath,
                stage = "durationMs unavailable (missing/unparseable WAV)",
                context = context,
                chapterId = chapterId,
                stationId = stationId,
                cause = null,
            )
        }
        return ms
    }

    suspend fun playFirstAvailable(vararg assetPaths: String, volume: Float = 1f, rate: Float = 1f) {
        playFirstAvailableReturningPath(*assetPaths, volume = volume, rate = rate)
    }

    /**
     * Like [playFirstAvailable], but returns the asset path that was actually started (first loadable), or null.
     */
    suspend fun playFirstAvailableReturningPath(
        vararg assetPaths: String,
        volume: Float = 1f,
        rate: Float = 1f,
    ): String? {
        val pool = soundPool ?: return null
        val r = rate.coerceIn(0.8f, 1.25f)
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val soundId = loadIfNeeded(pool, p) ?: continue
            val streamId = pool.play(soundId, volume, volume, 1, 0, r)
            if (streamId != 0) activeStreamIds[streamId] = true
            return p
        }
        return null
    }

    /**
     * Like [playFirstAvailableReturningPath], but also returns the stream id so callers can stop/sequence reliably.
     */
    suspend fun playFirstAvailableReturningPathAndStreamId(
        vararg assetPaths: String,
        volume: Float = 1f,
        rate: Float = 1f,
    ): Pair<String, Int>? {
        val pool = soundPool ?: return null
        val r = rate.coerceIn(0.8f, 1.25f)
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val soundId = loadIfNeeded(pool, p) ?: continue
            val streamId = pool.play(soundId, volume, volume, 1, 0, r)
            if (streamId != 0) {
                activeStreamIds[streamId] = true
                return p to streamId
            }
        }
        return null
    }

    fun stopStream(streamId: Int?) {
        val pool = soundPool ?: return
        val id = streamId ?: return
        if (id == 0) return
        pool.stop(id)
        activeStreamIds.remove(id)
    }

    /**
     * Best-effort: stop every stream we've started since init. Useful when sequencing voice/SFX and we must
     * guarantee no overlap even if durations are slightly off.
     */
    fun stopAllStreams() {
        val pool = soundPool ?: return
        val ids = activeStreamIds.keys.toList()
        for (id in ids) {
            if (id == 0) continue
            runCatching { pool.stop(id) }
        }
        activeStreamIds.clear()
    }

    fun release() {
        stopAllStreams()
        soundPool?.release()
        loadedSoundIdByPath.clear()
        readySoundIds.clear()
        pendingLoads.clear()
        activeStreamIds.clear()
        synchronized(Registry) { Registry.remove(this) }
    }

    private suspend fun loadIfNeeded(pool: SoundPool, assetPath: String): Int? {
        loadedSoundIdByPath[assetPath]?.let { existingId ->
            val ready = readySoundIds[existingId]
            return if (ready == true) existingId else awaitLoaded(existingId)
        }

        val id =
            withContext(Dispatchers.IO) {
                try {
                    val afd = appContext.assets.openFd(assetPath)
                    val newId = pool.load(afd, 1)
                    loadedSoundIdByPath[assetPath] = newId
                    newId
                } catch (_: IOException) {
                    null
                }
            } ?: return null

        return awaitLoaded(id)
    }

    private suspend fun loadIfNeededRequired(
        pool: SoundPool,
        assetPath: String,
        context: String,
        chapterId: Int?,
        stationId: Int?,
    ): Int? {
        loadedSoundIdByPath[assetPath]?.let { existingId ->
            val ready = readySoundIds[existingId]
            val resolved = if (ready == true) existingId else awaitLoaded(existingId)
            if (resolved == null) {
                reportRequiredFailure(
                    assetPath = assetPath,
                    stage = "awaitLoaded failed (previous load not ready or failed)",
                    context = context,
                    chapterId = chapterId,
                    stationId = stationId,
                    cause = null,
                )
            }
            return resolved
        }

        val idResult =
            withContext(Dispatchers.IO) {
                runCatching {
                    val afd = appContext.assets.openFd(assetPath)
                    val newId = pool.load(afd, 1)
                    loadedSoundIdByPath[assetPath] = newId
                    newId
                }
            }
        val id = idResult.getOrNull()
        if (id == null) {
            reportRequiredFailure(
                assetPath = assetPath,
                stage = "assets.openFd/load failed",
                context = context,
                chapterId = chapterId,
                stationId = stationId,
                cause = idResult.exceptionOrNull(),
            )
            return null
        }
        val loaded = awaitLoaded(id)
        if (loaded == null) {
            reportRequiredFailure(
                assetPath = assetPath,
                stage = "awaitLoaded failed (load complete status not OK)",
                context = context,
                chapterId = chapterId,
                stationId = stationId,
                cause = null,
            )
        }
        return loaded
    }

    private fun reportRequiredFailure(
        assetPath: String,
        stage: String,
        context: String,
        chapterId: Int?,
        stationId: Int?,
        cause: Throwable?,
    ) {
        val msg =
            "Required SoundPool asset failed. assetPath='$assetPath' chapterId=$chapterId stationId=$stationId context=$context stage=$stage"
        if (cause != null) {
            Log.e(MISSING_TAG, msg, cause)
        } else {
            Log.e(MISSING_TAG, msg)
        }
        if (isDebuggable) {
            if (cause != null) throw IllegalStateException(msg, cause)
            throw IllegalStateException(msg)
        }
    }

    private suspend fun awaitLoaded(soundId: Int): Int? {
        val ready = readySoundIds[soundId]
        if (ready == true) return soundId
        if (ready == false) return null

        val ok =
            suspendCancellableCoroutine { cont ->
                val list = pendingLoads.getOrPut(soundId) { mutableListOf() }
                list.add { success ->
                    if (cont.isActive) cont.resume(success)
                }
            }
        return if (ok) soundId else null
    }

    suspend fun preload(vararg assetPaths: String) {
        val pool = soundPool ?: return
        for (p in assetPaths) {
            if (p.isBlank()) continue
            loadIfNeeded(pool, p)
        }
    }

    /**
     * Best-effort duration for PCM WAV assets, based on header parse.
     * Cached per asset path.
     */
    suspend fun durationMs(assetPath: String): Long? {
        if (assetPath.isBlank()) return null
        durationMsByPath[assetPath]?.let { return it }
        val ms =
            withContext(Dispatchers.IO) {
                try {
                    appContext.assets.open(assetPath).use { input ->
                        val bytes = input.readBytes()
                        if (bytes.size < 44) return@use null
                        fun leInt(off: Int): Int =
                            ByteBuffer.wrap(bytes, off, 4).order(ByteOrder.LITTLE_ENDIAN).int
                        fun leShort(off: Int): Int =
                            ByteBuffer.wrap(bytes, off, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF

                        if (String(bytes, 0, 4, StandardCharsets.US_ASCII) != "RIFF") return@use null
                        if (String(bytes, 8, 4, StandardCharsets.US_ASCII) != "WAVE") return@use null

                        var pos = 12
                        var sampleRate = 0
                        var numChannels = 0
                        var bitsPerSample = 0
                        var dataSize: Long = -1L

                        while (pos + 8 <= bytes.size) {
                            val chunkId = String(bytes, pos, 4, StandardCharsets.US_ASCII)
                            val chunkSize = leInt(pos + 4)
                            if (chunkSize < 0) return@use null
                            val payloadStart = pos + 8
                            val payloadEnd = payloadStart + chunkSize
                            if (payloadEnd > bytes.size) return@use null

                            when (chunkId) {
                                "fmt " -> {
                                    if (chunkSize < 16) return@use null
                                    val audioFormat = leShort(payloadStart)
                                    if (audioFormat != 1) return@use null // PCM only
                                    numChannels = leShort(payloadStart + 2)
                                    sampleRate = leInt(payloadStart + 4)
                                    bitsPerSample = leShort(payloadStart + 14)
                                }
                                "data" -> {
                                    dataSize = chunkSize.toLong() and 0xFFFF_FFFFL
                                }
                            }

                            pos = payloadEnd + (chunkSize and 1)
                        }

                        if (sampleRate <= 0 || numChannels <= 0 || bitsPerSample <= 0 || dataSize < 0L) {
                            return@use null
                        }
                        val bytesPerSample = (bitsPerSample / 8).coerceAtLeast(1)
                        val byteRate = sampleRate.toLong() * numChannels.toLong() * bytesPerSample.toLong()
                        if (byteRate <= 0L) return@use null
                        ((dataSize * 1000L) / byteRate).coerceAtMost(15_000L)
                    }
                } catch (_: Throwable) {
                    null
                }
            }
        if (ms != null) durationMsByPath[assetPath] = ms
        return ms
    }
}
