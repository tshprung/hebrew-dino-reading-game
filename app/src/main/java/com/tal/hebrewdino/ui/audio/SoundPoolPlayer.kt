package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

class SoundPoolPlayer(context: Context) {
    companion object {
        /** Temporarily disable all SFX playback (we’ll re-enable later). */
        const val ENABLED: Boolean = false
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

    init {
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            val ok = status == 0
            readySoundIds[sampleId] = ok
            pendingLoads.remove(sampleId)?.forEach { cb -> cb(ok) }
        }
    }

    suspend fun play(assetPath: String, volume: Float = 1f) {
        if (!ENABLED) return
        val pool = soundPool ?: return
        val soundId = loadIfNeeded(pool, assetPath) ?: return
        pool.play(soundId, volume, volume, 1, 0, 1f)
    }

    suspend fun playFirstAvailable(vararg assetPaths: String, volume: Float = 1f) {
        if (!ENABLED) return
        val pool = soundPool ?: return
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val soundId = loadIfNeeded(pool, p) ?: continue
            pool.play(soundId, volume, volume, 1, 0, 1f)
            return
        }
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
}
