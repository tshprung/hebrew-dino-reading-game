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
        pool.play(soundId, volume, volume, 1, 0, rate.coerceIn(0.8f, 1.25f))
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
        return if (streamId != 0) streamId else null
    }

    suspend fun playFirstAvailable(vararg assetPaths: String, volume: Float = 1f, rate: Float = 1f) {
        if (!ENABLED) return
        val pool = soundPool ?: return
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val soundId = loadIfNeeded(pool, p) ?: continue
            pool.play(soundId, volume, volume, 1, 0, rate.coerceIn(0.8f, 1.25f))
            return
        }
    }

    fun stopStream(streamId: Int?) {
        if (!ENABLED) return
        val pool = soundPool ?: return
        val id = streamId ?: return
        if (id == 0) return
        pool.stop(id)
    }

    fun release() {
        soundPool?.release()
        loadedSoundIdByPath.clear()
        readySoundIds.clear()
        pendingLoads.clear()
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
                        val header = ByteArray(64)
                        val n = input.read(header)
                        if (n < 44) return@use null
                        // Very small RIFF/WAV parser: find "fmt " and "data" chunks in the first 64 bytes.
                        fun leInt(off: Int): Int =
                            ByteBuffer.wrap(header, off, 4).order(ByteOrder.LITTLE_ENDIAN).int
                        fun leShort(off: Int): Int =
                            ByteBuffer.wrap(header, off, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF

                        // Validate RIFF/WAVE
                        val riff = String(header, 0, 4)
                        val wave = String(header, 8, 4)
                        if (riff != "RIFF" || wave != "WAVE") return@use null

                        // Assume standard layout: fmt at 12, data later. (Our recorded clips follow this.)
                        val fmtId = String(header, 12, 4)
                        if (fmtId != "fmt ") return@use null
                        val fmtSize = leInt(16)
                        val audioFormat = leShort(20)
                        val numChannels = leShort(22)
                        val sampleRate = leInt(24)
                        val bitsPerSample = leShort(34)
                        if (audioFormat != 1) return@use null // PCM only
                        if (sampleRate <= 0 || numChannels <= 0 || bitsPerSample <= 0) return@use null
                        // data chunk header expected at 20 + fmtSize (commonly 36) + 8
                        val dataOff = 20 + fmtSize
                        if (dataOff + 8 > n) return@use null
                        val dataId = String(header, dataOff, 4)
                        if (dataId != "data") return@use null
                        val dataSize = leInt(dataOff + 4).toLong().coerceAtLeast(0L)

                        val bytesPerSample = (bitsPerSample / 8).coerceAtLeast(1)
                        val byteRate = sampleRate.toLong() * numChannels.toLong() * bytesPerSample.toLong()
                        if (byteRate <= 0) return@use null
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
