package com.tal.hebrewdino.ui.audio

import android.content.Context

/** Stops all gameplay speech/SFX so a new tap never overlaps prior audio. */
object InteractionAudio {
    fun stopAllNow(context: Context) {
        AppForegroundAudio.pauseAll(context)
    }

    /** Stops TTS and narrative voice only — leaves background music playing. */
    fun stopSpeechOnly(context: Context) {
        val appContext = context.applicationContext
        TextToSpeechManager.get(appContext).cancelActiveSpeech()
        VoicePlayer.stopAllNow()
        RawVoicePlayer.stopAllNow()
    }
}
