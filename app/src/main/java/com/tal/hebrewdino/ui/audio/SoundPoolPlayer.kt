package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

class SoundPoolPlayer(context: Context) {
    companion object {
        /** Kid-game SFX; pool creation is still wrapped in [runCatching] — null pool = silent no-op. */
        const val ENABLED: Boolean = true
    }

    private val appContext = context.applicationContext

    /**
     * When [ENABLED] is false, do not allocate a native SoundPool at all (some devices crash on
     * pool creation even if we never call [play]). When true, creation can still fail — then all
     * calls no-op.
     */
    private val soundPool: SoundPool? =
        if (!ENABLED) {
            null
        } else {
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
        }

    private val loadedSoundIdByPath = ConcurrentHashMap<String, Int>()
    private val readySoundIds = ConcurrentHashMap<Int, Boolean>()
    private val pendingLoads = ConcurrentHashMap<Int, MutableList<(Boolean) -> Unit>>()
    private val durationMsByPath = ConcurrentHashMap<String, Long>()
    private val activeStreamIds = ConcurrentHashMap<Int, Boolean>()

    init {
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            val ok = status == 0
            readySoundIds[sampleId] = ok
            pendingLoads.remove(sampleId)?.forEach { cb -> cb(ok) }
        }
    }

    suspend fun play(assetPath: String, volume: Float = 1f, rate: Float = 1f) {
        if (!ENABLED) return
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
        if (!ENABLED) return null
        val pool = soundPool ?: return null
        val soundId = loadIfNeeded(pool, assetPath) ?: return null
        val streamId = pool.play(soundId, volume, volume, 1, 0, rate.coerceIn(0.8f, 1.25f))
        if (streamId != 0) activeStreamIds[streamId] = true
        return if (streamId != 0) streamId else null
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
        if (!ENABLED) return null
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
        if (!ENABLED) return null
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
        if (!ENABLED) return
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
        if (!ENABLED) return
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

    private suspend fun awaitLoaded(soundId: Int): Int? {
        val ready = readySoundIds[soundId]
        if (ready == true) return soundId
        if (ready == false) return null

        val ok =
            suspendCancellableCoroutine<Boolean> { cont ->
                val list = pendingLoads.getOrPut(soundId) { mutableListOf() }
                list.add { success ->
                    if (cont.isActive) cont.resume(success)
                }
            }
        return if (ok) soundId else null
    }

    suspend fun preload(vararg assetPaths: String) {
        if (!ENABLED) return
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
