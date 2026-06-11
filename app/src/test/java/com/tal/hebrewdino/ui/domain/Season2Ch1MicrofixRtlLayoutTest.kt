package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1MicrofixRtlLayoutTest {
    @Test
    fun ch1_st5_instruction_font_smaller() {
        assertEquals(0.7f, Season2Ch1QaPolicy.WhichWordStartsWithInstructionFontScale, 0.001f)
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionFontScale"))
    }

    @Test
    fun ch1_st5_instruction_bg_tight_wrap() {
        assertEquals(7, Season2Ch1QaPolicy.WhichWordStartsWithInstructionBgHorizontalPaddingDp.value.toInt())
        assertEquals(3, Season2Ch1QaPolicy.WhichWordStartsWithInstructionBgVerticalPaddingDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("wrapContentWidth"))
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionBgHorizontalPaddingDp"))
        assertTrue(imageMatch.contains("maxLines = 1"))
    }

    @Test
    fun ch1_st6_layout_direction_RTL() {
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
        assertTrue(imageToWord.contains("LocalLayoutDirection provides LayoutDirection.Rtl"))
        assertTrue(imageToWord.contains("horizontalAlignment = Alignment.CenterHorizontally"))
    }

    @Test
    fun ch1_st6_uses_absolute_left_shift_not_start_end_padding() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
        assertTrue(imageToWord.contains("FinaleDinoReservedWidthDp"))
        assertFalse(imageToWord.contains("FinaleShiftAwayFromDinoDp"))
        assertFalse(imageToWord.contains("padding(end =\n                            when {\n                                isSeason2ImageToWord && chapterId == Season2ChapterIds.Chapter1Tyrannosaurus"))
    }

    @Test
    fun ch1_st6_hint_still_hidden() {
        assertTrue(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
        val overlay = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameOverlayLayer.kt")
        assertTrue(overlay.contains("showHintButton"))
    }

    @Test
    fun s1_unchanged() {
        assertFalse(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                1,
                Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH,
            ),
        )
        assertFalse(Season2Ch1QaPolicy.isWhichWordStartsWithLayoutPilot(null))
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
