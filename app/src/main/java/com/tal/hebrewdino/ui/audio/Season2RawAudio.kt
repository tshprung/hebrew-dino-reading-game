package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2AdvancedStationMode
import com.tal.hebrewdino.ui.domain.Season2WordPartsPresentationMode

/** Season 2 voice clips in `res/raw` (MP3). */
object Season2RawAudio {
    @RawRes val MapPartRevealed: Int = R.raw.season2_map_part_revealed

    @RawRes val MapAlmostDone: Int = R.raw.season2_map_almost_done

    @RawRes val ReplayTileInstruction: Int = R.raw.season2_replay_tile_instruction

    @RawRes val MapEntryNextTile: Int = R.raw.season2_map_entry_next_tile_01

    @RawRes val MapEntryReplayTiles: Int = R.raw.season2_map_entry_replay_tiles_01

    @RawRes val WordPartsChooseSplitInstructions: Int = R.raw.season2_word_parts_choose_split_instructions

    @RawRes val WordPartsHiddenSplitInstructions: Int = R.raw.season2_word_parts_hidden_split_instructions

    @RawRes val MissingFirstLetterInstructions: Int = R.raw.season2_missing_first_letter_instructions

    @RawRes val RhymingInstructions: Int = R.raw.season2_rhyming_instructions

    @RawRes val Success01: Int = R.raw.season2_success_01

    @RawRes val Success02: Int = R.raw.season2_success_02

    @RawRes val Success03: Int = R.raw.season2_success_03

    private val MapPraiseDinoPool: IntArray =
        intArrayOf(
            R.raw.season2_map_praise_dino_01,
            R.raw.season2_map_praise_dino_02,
            R.raw.season2_map_praise_dino_03,
            R.raw.season2_map_praise_dino_04,
            R.raw.season2_map_praise_dino_05,
        )

    private val MapPraiseDinaPool: IntArray =
        intArrayOf(
            R.raw.season2_map_praise_dina_01,
            R.raw.season2_map_praise_dina_02,
            R.raw.season2_map_praise_dina_03,
            R.raw.season2_map_praise_dina_04,
            R.raw.season2_map_praise_dina_05,
        )

    private val FocusDinoPool: IntArray =
        intArrayOf(
            R.raw.season2_focus_dino_01,
            R.raw.season2_focus_dino_02,
            R.raw.season2_focus_dino_03,
        )

    private val FocusDinaPool: IntArray =
        intArrayOf(
            R.raw.season2_focus_dina_01,
            R.raw.season2_focus_dina_02,
            R.raw.season2_focus_dina_03,
        )

    /** Short neutral praise after two-mistake focus (in-station, not reward screen). */
    private val PostFocusCorrectPool: IntArray =
        intArrayOf(
            Success01,
            Success02,
            Success03,
        )

    fun postFocusCorrectPool(): IntArray = PostFocusCorrectPool

    fun mapPraisePool(companion: DinoCharacter): IntArray =
        when (companion) {
            DinoCharacter.Dina -> MapPraiseDinaPool
            DinoCharacter.Dino -> MapPraiseDinoPool
        }

    fun focusPool(companion: DinoCharacter): IntArray =
        when (companion) {
            DinoCharacter.Dina -> FocusDinaPool
            DinoCharacter.Dino -> FocusDinoPool
        }

    /** Visible caption text aligned to each companion map-praise raw clip. */
    fun mapPraiseCaption(@RawRes rawResId: Int): String? =
        when (rawResId) {
            R.raw.season2_map_praise_dino_01 -> "\u200Fיופי! עוד חלק מהמפה התגלה!"
            R.raw.season2_map_praise_dino_02 -> "\u200Fכל הכבוד! גילינו עוד חלק במפה!"
            R.raw.season2_map_praise_dino_03 -> "\u200Fנהדר! המפה נפתחת עוד קצת!"
            R.raw.season2_map_praise_dino_04 -> "\u200Fאיזה יופי! עוד חלק מהפאזל נחשף!"
            R.raw.season2_map_praise_dino_05 -> "\u200Fוואו! אנחנו מתקרבים לסוף!"
            R.raw.season2_map_praise_dina_01 -> "\u200Fיופי! עוד חלק מהמפה התגלה!"
            R.raw.season2_map_praise_dina_02 -> "\u200Fכל הכבוד! גילינו עוד חלק במפה!"
            R.raw.season2_map_praise_dina_03 -> "\u200Fנהדר! המפה נפתחת עוד קצת!"
            R.raw.season2_map_praise_dina_04 -> "\u200Fאיזה יופי! עוד חלק מהפאזל נחשף!"
            R.raw.season2_map_praise_dina_05 -> "\u200Fוואו! אנחנו מתקרבים לסוף!"
            else -> null
        }

    @RawRes
    fun instructionRawResId(
        mode: Season2AdvancedStationMode,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ): Int =
        when (mode) {
            Season2AdvancedStationMode.PictureToWord -> R.raw.instruction_image_to_word
            Season2AdvancedStationMode.MissingFirstLetter -> MissingFirstLetterInstructions
            Season2AdvancedStationMode.WordParts ->
                when (wordPartsPresentationMode) {
                    Season2WordPartsPresentationMode.HiddenWordPartsChallenge ->
                        WordPartsHiddenSplitInstructions
                    else -> WordPartsChooseSplitInstructions
                }
            Season2AdvancedStationMode.Rhyming -> RhymingInstructions
        }

    @RawRes
    fun wordPartRawResId(
        catalogId: String,
        partIndex: Int,
    ): Int? =
        when (catalogId to partIndex) {
            "w_ש_1" to 1 -> R.raw.wordpart_w_shin_1_p1
            "w_ש_1" to 2 -> R.raw.wordpart_w_shin_1_p2
            "w_ח_3" to 1 -> R.raw.wordpart_w_chet_3_p1
            "w_ח_3" to 2 -> R.raw.wordpart_w_chet_3_p2
            "w_ח_2" to 1 -> R.raw.wordpart_w_chet_2_p1
            "w_ח_2" to 2 -> R.raw.wordpart_w_chet_2_p2
            "w_ג_1" to 1 -> R.raw.wordpart_w_gimel_1_p1
            "w_ג_1" to 2 -> R.raw.wordpart_w_gimel_1_p2
            "w_ג_3" to 1 -> R.raw.wordpart_w_gimel_3_p1
            "w_ג_3" to 2 -> R.raw.wordpart_w_gimel_3_p2
            "w_ר_1" to 1 -> R.raw.wordpart_w_reish_1_p1
            "w_ר_1" to 2 -> R.raw.wordpart_w_reish_1_p2
            "w_פ_2" to 1 -> R.raw.wordpart_w_peh_2_p1
            "w_פ_2" to 2 -> R.raw.wordpart_w_peh_2_p2
            "w_ר_3" to 1 -> R.raw.wordpart_w_reish_3_p1
            "w_ר_3" to 2 -> R.raw.wordpart_w_reish_3_p2
            "w_ס_4" to 1 -> R.raw.wordpart_w_samech_4_p1
            "w_ס_4" to 2 -> R.raw.wordpart_w_samech_4_p2
            "w_ז_3" to 1 -> R.raw.wordpart_w_zayin_3_p1
            "w_ז_3" to 2 -> R.raw.wordpart_w_zayin_3_p2
            "w_ב_2" to 1 -> R.raw.wordpart_w_bet_2_p1
            "w_ב_2" to 2 -> R.raw.wordpart_w_bet_2_p2
            "w_נ_2" to 1 -> R.raw.wordpart_w_nun_2_p1
            "w_נ_2" to 2 -> R.raw.wordpart_w_nun_2_p2
            "w_צ_2" to 1 -> R.raw.wordpart_w_tsadi_2_p1
            "w_צ_2" to 2 -> R.raw.wordpart_w_tsadi_2_p2
            else -> null
        }
}
