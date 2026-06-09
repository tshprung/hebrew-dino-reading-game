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
    private val positionMsByAssetPath: HashMap<String, Int> = hashMapOf()
    private var targetVolume: Float = 0.28f
    private var muted: Boolean = false
    private var voiceDuckDepth: Int = 0

    fun playLoopFromAssets(
        assetPath: String,
        volume: Float = 0.28f,
    ) {
        if (assetPath.isBlank()) return
        val v = volume.coerceIn(0f, 1f)
        targetVolume = v
        if (currentAssetPath == assetPath && player != null) {
            applyVolume()
            if (player?.isPlaying != true) {
                try {
                    player?.start()
                } catch (_: Throwable) {
                }
            }
            return
        }
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
            val initialMuted = muted || voiceDuckDepth > 0
            mp.setVolume(if (initialMuted) 0f else v, if (initialMuted) 0f else v)
            mp.setOnErrorListener { _, _, _ ->
                stop()
                true
            }
            mp.prepare()
            val resumePosMs = positionMsByAssetPath[assetPath] ?: 0
            if (resumePosMs > 0) {
                runCatching { mp.seekTo(resumePosMs) }
            }
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

    fun setMuted(muted: Boolean) {
        this.muted = muted
        applyVolume()
    }

    fun beginVoiceDuck() {
        voiceDuckDepth++
        applyVolume()
    }

    fun endVoiceDuck() {
        voiceDuckDepth = (voiceDuckDepth - 1).coerceAtLeast(0)
        applyVolume()
    }

    private fun applyVolume() {
        val p = player ?: return
        val effectiveMuted = muted || voiceDuckDepth > 0
        val v = if (effectiveMuted) 0f else targetVolume.coerceIn(0f, 1f)
        try {
            p.setVolume(v, v)
        } catch (_: Throwable) {
        }
    }

    fun stop() {
        val p = player
        val path = currentAssetPath
        if (p != null && path != null) {
            val pos =
                runCatching { p.currentPosition }
                    .getOrNull()
                    ?.coerceAtLeast(0)
                    ?: 0
            if (pos > 0) {
                positionMsByAssetPath[path] = pos
            }
        }
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
