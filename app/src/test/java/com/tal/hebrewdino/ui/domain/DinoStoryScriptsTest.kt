package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DinoStoryScriptsTest {
    @Test
    fun dino_name_uses_hyphenated_syllables() {
        assertEquals("דִּי-נוֹ", DinoStoryScripts.dinoNameSpoken())
        assertEquals(DinoStoryScripts.dinoNameSpoken(), dinoNameSpokenForTts())
    }

    @Test
    fun part1_intro_contains_egg_narrative_not_offices() {
        val spoken = introInstructionSpokenForTts()
        assertTrue(spoken.contains("ביצה"))
        assertTrue(spoken.contains(DinoStoryScripts.dinoNameSpoken()))
        assertTrue(!spoken.contains("משרד"))
    }

    @Test
    fun part2_and_part3_reference_dino_name() {
        assertTrue(DinoStoryScripts.postHatchIntroSpokenForTts().contains("הנה הוא"))
        assertTrue(DinoStoryScripts.part3FirstAccessorySpokenForTts().contains("כובע"))
    }
}
