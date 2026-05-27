package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HebrewLetterSpeechTest {
    @Test
    fun phoneme_spoken_for_tzadi_is_vowelized_syllable_not_letter_name() {
        val spoken = phonemeSpokenForTts("צ")
        assertEquals("צֵ", spoken)
        assertNotEquals("צ", spoken)
        assertNotEquals(letterNameSpokenForTts("צ"), spoken)
    }

    @Test
    fun station2_success_speech_uses_phoneme_not_letter_name() {
        val speech = targetSuccessSpeech(ChallengeType.PHONEMIC_ISOLATION, "צ")
        assertEquals("צֵ", speech)
    }

    @Test
    fun letter_name_spoken_for_chet_avoids_cheit_pronunciation() {
        val spoken = letterNameSpokenForTts("ח")
        assertEquals("חֵ", spoken)
        assertTrue(!spoken.contains('י'))
    }

    @Test
    fun dino_name_spoken_uses_hyphenated_syllables() {
        assertEquals("דִּי-נוֹ", dinoNameSpokenForTts())
    }

    @Test
    fun intro_instruction_spoken_uses_part1_egg_story() {
        val spoken = introInstructionSpokenForTts()
        assertTrue(spoken.contains("ביצה"))
        assertTrue(!spoken.contains("משרד"))
    }

    @Test
    fun apply_child_friendly_workarounds_replaces_dino_name() {
        val spoken = applyChildFriendlyTtsWorkarounds("דינו רעב ודינו שוב")
        assertTrue(spoken.contains(dinoNameSpokenForTts()))
        assertTrue(!spoken.contains("דינו"))
    }
}
