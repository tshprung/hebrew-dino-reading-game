package com.tal.hebrewdino.ui.audio

import android.content.Context

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
