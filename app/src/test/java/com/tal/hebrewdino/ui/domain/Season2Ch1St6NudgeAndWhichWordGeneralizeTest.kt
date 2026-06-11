package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1St6NudgeAndWhichWordGeneralizeTest {
    @Test
    fun s2_ch1_st6_instruction_image_shift_right_more_than_words() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
        assertTrue(imageToWord.contains("FinaleDinoReservedWidthDp"))
    }

    @Test
    fun s2_ch1_st6_words_shift_right_0_5cm() {
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("wrapContentWidth(Alignment.CenterHorizontally)"))
        assertFalse(imageToWord.contains("FinaleAbsoluteLeftShiftDp"))
    }

    @Test
    fun s2_ch1_st6_hint_hidden() {
        assertTrue(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
    }

    @Test
    fun which_word_starts_with_layout_applies_to_s1_and_s2() {
        assertTrue(
            Season2Ch1QaPolicy.isWhichWordStartsWithLayoutStation(
                chapterId = 1,
                stationId = Chapter1StationOrder.PICTURE_PICK_ALL,
            ),
        )
        assertTrue(
            Season2Ch1QaPolicy.isWhichWordStartsWithLayoutStation(
                chapterId = Season2ChapterIds.Chapter1Tyrannosaurus,
                stationId = Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("isWhichWordStartsWithLayoutStation"))
        assertTrue(imageMatch.contains("useWhichWordCompactLayout"))
        assertFalse(imageMatch.contains("useCh1WhichWordLayoutPilot"))
    }

    @Test
    fun which_word_starts_with_instruction_single_rtl_compact_bg() {
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionFontScale"))
        assertTrue(imageMatch.contains("maxLines = 1"))
        assertTrue(imageMatch.contains("wrapContentWidth"))
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionBgHorizontalPaddingDp"))
    }

    @Test
    fun which_word_starts_with_cards_and_letter_offsets() {
        assertEquals(4, Season2Ch1QaPolicy.WhichWordStartsWithLayoutPilotCardsDownDp.value.toInt())
        assertEquals(11, Season2Ch1QaPolicy.WhichWordStartsWithInstructionUpDp.value.toInt())
        assertEquals(8, Season2Ch1QaPolicy.WhichWordStartsWithLetterUpDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithLayoutPilotCardsDownDp"))
        assertTrue(imageMatch.contains("WhichWordStartsWithLetterUpDp"))
    }

    @Test
    fun station_content_unchanged() {
        val registry = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/StationBehaviorRegistry.kt")
        assertTrue(registry.contains("PICTURE_PICK_ALL"))
        assertFalse(registry.contains("WhichWordStartsWithLayoutPilotCardsDownDp"))
    }

    @Test
    fun progression_unchanged() {
        val stationScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterStationScreen.kt")
        assertTrue(stationScreen.contains("shouldRequestFirstTimeChapterReward"))
        assertFalse(stationScreen.contains("Ch1FinaleImageToWordAlignedContent"))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
