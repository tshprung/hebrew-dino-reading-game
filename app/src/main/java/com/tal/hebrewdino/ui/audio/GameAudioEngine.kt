package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

class GameAudioEngine(
    context: Context,
) {
    val voice: VoicePlayer = VoicePlayer(context = context)
    val sfx: SoundPoolPlayer = SoundPoolPlayer(context = context)

    fun release() {
        voice.release()
        sfx.release()
    }
}

class BackgroundMusicPlayer(
    context: Context,
) {
    companion object {
        private const val TAG: String = "BackgroundMusic"
    }

    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null
    private var currentAssetPath: String? = null

    fun playLoopFromAssets(
        assetPath: String,
        volume: Float = 0.28f,
    ) {
        if (assetPath.isBlank()) return
        if (currentAssetPath == assetPath && player?.isPlaying == true) return
        stop()

        try {
            val afd = appContext.assets.openFd(assetPath)
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.isLooping = true
            val v = volume.coerceIn(0f, 1f)
            mp.setVolume(v, v)
            mp.setOnErrorListener { _, _, _ ->
                stop()
                true
            }
            mp.prepare()
            mp.start()
            player = mp
            currentAssetPath = assetPath
        } catch (_: IOException) {
            Log.w(TAG, "Missing/unplayable music asset: $assetPath")
            stop()
        } catch (_: Throwable) {
            stop()
        }
    }

    fun stop() {
        currentAssetPath = null
        try {
            player?.stop()
        } catch (_: Throwable) {
        }
        try {
            player?.release()
        } catch (_: Throwable) {
        }
        player = null
    }

    fun release() {
        stop()
    }
}
