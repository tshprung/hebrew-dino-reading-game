package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Season2AdvancedStationMode
import com.tal.hebrewdino.ui.domain.Season2Copy
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.Season2StationTheme
import com.tal.hebrewdino.ui.domain.Season2StationThemeCopy
import com.tal.hebrewdino.ui.domain.Season2WordPartsPresentationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class Season2RawAudioWiringTest {
    @Test
    fun season2Instructions_useResRawNotAssetsWav() {
        assertEquals(
            R.raw.season2_word_parts_choose_split_instructions,
            Season2StationAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.GuidedWordParts,
            ),
        )
        assertEquals(
            R.raw.season2_word_parts_hidden_split_instructions,
            Season2StationAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            ),
        )
        assertEquals(
            R.raw.season2_missing_first_letter_instructions,
            Season2StationAudio.instructionRawResId(Season2AdvancedStationMode.MissingFirstLetter),
        )
        assertEquals(
            R.raw.season2_rhyming_instructions,
            Season2StationAudio.instructionRawResId(Season2AdvancedStationMode.Rhyming),
        )
        assertEquals(R.raw.instruction_image_to_word, Season2RawAudio.instructionRawResId(Season2AdvancedStationMode.PictureToWord))
    }

    @Test
    fun acceptedInstructionTexts() {
        val theme = Season2StationTheme.StegosaurusPlates
        assertEquals(
            "\u200Fמצאו את חלקי המילה",
            Season2StationThemeCopy.wordPartsInstruction(Season2WordPartsPresentationMode.GuidedWordParts),
        )
        assertEquals(
            "\u200Fמצאו את חלקי המילה ששמעתם",
            Season2StationThemeCopy.wordPartsInstruction(Season2WordPartsPresentationMode.HiddenWordPartsChallenge),
        )
        assertEquals("\u200Fאיזו אות חסרה במילה?", Season2StationThemeCopy.missingFirstLetterInstruction(theme))
        assertEquals("\u200Fאיזו מילה מתחרזת עם?", Season2StationThemeCopy.rhymingInstruction(theme))
    }

    @Test
    fun mapCaptions_useCompanionPraisePools() {
        assertEquals(5, Season2RawAudio.mapPraisePool(com.tal.hebrewdino.ui.data.DinoCharacter.Dino).size)
        assertEquals(5, Season2RawAudio.mapPraisePool(com.tal.hebrewdino.ui.data.DinoCharacter.Dina).size)
        assertEquals(3, Season2RawAudio.focusPool(com.tal.hebrewdino.ui.data.DinoCharacter.Dino).size)
        assertNull(Season2Copy.returnCaptionVoiceRawRes(6))
        assertEquals(R.raw.season2_map_entry_next_tile_01, Season2RawAudio.MapEntryNextTile)
        assertEquals(R.raw.season2_map_entry_replay_tiles_01, Season2RawAudio.MapEntryReplayTiles)
        assertEquals(R.raw.season2_success_01, Season2RawAudio.Success01)
        assertEquals(R.raw.season2_success_02, Season2RawAudio.Success02)
        assertEquals(R.raw.season2_success_03, Season2RawAudio.Success03)
        assertEquals(
            setOf(R.raw.season2_success_01, R.raw.season2_success_02, R.raw.season2_success_03),
            Season2RawAudio.postFocusCorrectPool().toSet(),
        )
        assertEquals(R.raw.season2_replay_tile_instruction, Season2Copy.replayTileInstructionVoiceRawRes())
    }

    @Test
    fun wordPartChunks_mapForCuratedWords() {
        assertEquals(R.raw.wordpart_w_shin_1_p1, Season2RawAudio.wordPartRawResId("w_ש_1", 1))
        assertEquals(R.raw.wordpart_w_shin_1_p2, Season2RawAudio.wordPartRawResId("w_ש_1", 2))
        assertEquals(R.raw.wordpart_w_reish_3_p2, Season2RawAudio.wordPartRawResId("w_ר_3", 2))
        assertNull(Season2RawAudio.wordPartRawResId("w_ז_3", 1))
    }

    @Test
    fun wordPartChunks_doNotUseLetterNameClips() {
        assertNull(Season2RawAudio.wordPartRawResId("w_נ_2", 1))
        assertNotNull(AudioClips.letterNameRawResId("נ"))
    }
}
