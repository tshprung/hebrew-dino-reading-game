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

    suspend fun playEggCrack() {
        player.playFirstAvailable(
            AudioClips.SfxEggCrack,
            AudioClips.SfxFanfare,
            AudioClips.SfxCorrect,
        )
    }

    suspend fun playChew() {
        player.playFirstAvailable(
            AudioClips.SfxChew,
            AudioClips.SfxBalloonPopSoft,
            AudioClips.SfxCorrect,
        )
    }

    suspend fun playBabyChirp() {
        player.playFirstAvailable(
            AudioClips.SfxBabyChirp,
            AudioClips.SfxCorrect,
        )
    }

    suspend fun playHungryWhimper() {
        player.playFirstAvailable(
            AudioClips.SfxHungryWhimper,
            AudioClips.SfxWrong,
        )
    }

    fun release() {
        player.release()
    }
}
