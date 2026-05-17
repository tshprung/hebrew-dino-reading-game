package com.tal.hebrewdino.ui.audio

import android.content.Context

class GameAudioEngine(
    context: Context,
) {
    val voice: VoicePlayer = VoicePlayer(context = context)
    val sfx: SoundPoolPlayer = SoundPoolPlayer(context = context)

    fun stopVoiceNow() {
        voice.stopNow()
    }

    fun stopAllSfx() {
        sfx.stopAllStreams()
    }

    fun stopSfxStream(streamId: Int) {
        if (streamId != 0) sfx.stopStream(streamId)
    }

    fun release() {
        voice.release()
        sfx.release()
    }
}
