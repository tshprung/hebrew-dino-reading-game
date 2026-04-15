package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class SoundPoolPlayer(context: Context) {
    private val appContext = context.applicationContext

    private val soundPool: SoundPool =
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .build()

    private val loaded = ConcurrentHashMap<String, Int>()

    suspend fun play(assetPath: String, volume: Float = 1f) {
        val soundId = loadIfNeeded(assetPath) ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    suspend fun playFirstAvailable(vararg assetPaths: String, volume: Float = 1f) {
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val soundId = loadIfNeeded(p) ?: continue
            soundPool.play(soundId, volume, volume, 1, 0, 1f)
            return
        }
    }

    fun release() {
        soundPool.release()
        loaded.clear()
    }

    private suspend fun loadIfNeeded(assetPath: String): Int? {
        loaded[assetPath]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val afd = appContext.assets.openFd(assetPath)
                val id = soundPool.load(afd, 1)
                loaded[assetPath] = id
                id
            } catch (_: IOException) {
                null
            }
        }
    }
}

