package com.tal.hebrewdino.ui.audio

import android.content.Context

class SfxManager(context: Context) {
    private val player: SoundPoolPlayer = SoundPoolPlayer(context.applicationContext)

    suspend fun playCorrect() {
        player.playFirstAvailable(*arrayOf(AudioClips.SfxCorrect))
    }

    suspend fun playWrong() {
        player.playFirstAvailable(*arrayOf(AudioClips.SfxWrong))
    }

    suspend fun playFanfare() {
        player.playFirstAvailableReturningPath(
            AudioClips.SfxFanfare,
            AudioClips.SfxCorrect,
        )
    }

    suspend fun playEggTap() {
        player.playFirstAvailable(*arrayOf(AudioClips.SfxBalloonPopSoft, AudioClips.SfxBalloonPop))
    }

    fun release() {
        player.release()
    }
}
