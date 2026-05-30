package com.tal.hebrewdino.ui.audio.routing

fun interface GameAudioRouter {
    fun plan(event: AudioEvent): AudioPlan
}

object NoopGameAudioRouter : GameAudioRouter {
    override fun plan(event: AudioEvent): AudioPlan = AudioPlan.Empty
}
