package com.tal.hebrewdino.ui.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StationVoiceGuideTest {
    @Test
    fun placeholder_size_is_not_real() {
        assertFalse(StationVoiceGuide.isRealRawLength(StationVoiceGuide.PLACEHOLDER_RAW_BYTES))
    }

    @Test
    fun short_clip_is_real() {
        assertTrue(StationVoiceGuide.isRealRawLength(12_000L))
    }

    @Test
    fun oversized_clip_is_not_real() {
        assertFalse(StationVoiceGuide.isRealRawLength(700_000L))
    }
}
