package com.tal.hebrewdino.ui.audio

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates pausing all game audio when the app leaves the foreground
 * (home button, app switch, screen off).
 */
object AppForegroundAudio {
    private val _isInForeground = MutableStateFlow(true)
    val isInForeground: StateFlow<Boolean> = _isInForeground.asStateFlow()

    @Volatile
    private var backgroundMusic: BackgroundMusicPlayer? = null

    fun registerBackgroundMusic(player: BackgroundMusicPlayer) {
        backgroundMusic = player
    }

    fun unregisterBackgroundMusic(player: BackgroundMusicPlayer) {
        if (backgroundMusic === player) {
            backgroundMusic = null
        }
    }

    fun onForeground() {
        _isInForeground.value = true
    }

    fun onBackground(context: Context) {
        _isInForeground.value = false
        pauseAll(context)
    }

    fun pauseAll(context: Context) {
        val appContext = context.applicationContext
        SpeechFocusGate.reset()
        backgroundMusic?.stop()
        TextToSpeechManager.get(appContext).stop()
        VoicePlayer.stopAllNow()
        RawVoicePlayer.stopAllNow()
        SoundPoolPlayer.stopAllNow()
    }
}
