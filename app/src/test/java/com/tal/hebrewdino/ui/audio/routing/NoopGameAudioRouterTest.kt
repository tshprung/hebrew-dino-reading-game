package com.tal.hebrewdino.ui.audio.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoopGameAudioRouterTest {
    @Test
    fun noopRouter_returnsEmptyPlan_forAnyEvent() {
        val router: GameAudioRouter = NoopGameAudioRouter
        val plan =
            router.plan(
                AudioEvent.StationReward(
                    chapterId = 1,
                    stationId = 2,
                ),
            )
        assertTrue(plan.steps.isEmpty())
        assertEquals(AudioPlan.Empty, plan)
    }

    @Test(expected = IllegalArgumentException::class)
    fun audioStep_rejectsNegativeDelay() {
        AudioStep(
            lane = AudioLane.Voice,
            source = AudioSource.Asset("audio/vo_try_again_1.wav"),
            blocking = true,
            delayBeforeMs = -1L,
        )
    }
}
