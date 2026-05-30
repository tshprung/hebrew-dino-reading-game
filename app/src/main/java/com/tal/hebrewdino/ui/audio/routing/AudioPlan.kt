package com.tal.hebrewdino.ui.audio.routing

data class AudioStep(
    val lane: AudioLane,
    val source: AudioSource,
    val blocking: Boolean,
    val delayBeforeMs: Long = 0L,
) {
    init {
        require(delayBeforeMs >= 0L)
    }
}

data class AudioPlan(
    val steps: List<AudioStep>,
    val noImmediateRepeatKey: String? = null,
) {
    companion object {
        val Empty: AudioPlan = AudioPlan(steps = emptyList())
    }
}
