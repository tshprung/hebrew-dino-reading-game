package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingStoryNarrationTest {
    @Test
    fun training_storyNarrationRawResIds_areDefined() {
        assertTrue(R.raw.training_intro_dino != 0)
        assertTrue(R.raw.training_intro_dina != 0)
        assertTrue(R.raw.training_outro_dino != 0)
        assertTrue(R.raw.training_outro_dina != 0)
    }
}
