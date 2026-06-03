package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InStationPraiseAudioTest {
    @Test
    fun pool_hasTenClips_allDefined() {
        val pool = InStationPraiseAudio.pool()
        assertEquals(10, pool.size)
        pool.forEach { assertTrue(it != 0) }
    }

    @Test
    fun pool_doesNotIncludeLegacyMeuleOrFeedbackGreat() {
        val pool = InStationPraiseAudio.pool().toSet()
        assertFalse(pool.contains(R.raw.feedback_great_boy))
        assertFalse(pool.contains(R.raw.feedback_great_girl))
    }

    @Test
    fun pick_returnsValidRawId_fromPool() {
        val picked = InStationPraiseAudio.pick(avoidRawResId = null, random = Random(0))
        assertTrue(picked != 0)
        assertTrue(InStationPraiseAudio.pool().contains(picked))
    }

    @Test
    fun pick_noImmediateRepeat_avoidsPreviousWhenPossible() {
        val first = InStationPraiseAudio.pick(avoidRawResId = null, random = Random(1))
        val second = InStationPraiseAudio.pick(avoidRawResId = first, random = Random(1))
        assertNotEquals(first, second)
    }

    @Test
    fun trackingKey_roundTripsRawResId() {
        val rawResId = R.raw.praise_short_03
        val key = InStationPraiseAudio.trackingKey(rawResId)
        assertEquals(rawResId, InStationPraiseAudio.rawResIdFromTrackingKey(key))
    }

    @Test
    fun usesRawPraisePool_coversChaptersOneThroughSixAndTraining() {
        assertTrue(InStationPraiseAudio.usesRawPraisePool(1))
        assertTrue(InStationPraiseAudio.usesRawPraisePool(6))
        assertTrue(InStationPraiseAudio.usesRawPraisePool(TrainingV1Config.CHAPTER_ID))
        assertFalse(InStationPraiseAudio.usesRawPraisePool(99))
    }

    @Test
    fun pool_isNotSingleClip() {
        assertTrue(InStationPraiseAudio.pool().size >= 10)
    }
}
