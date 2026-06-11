package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1LayoutNudge2Test {
    @Test
    fun ch1_st5_only_letter_moved_up() {
        assertEquals(8, Season2Ch1QaPolicy.WhichWordStartsWithLetterUpDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithLetterUpDp"))
        assertTrue(imageMatch.contains("offset(y = -Season2Ch1QaPolicy.WhichWordStartsWithLetterUpDp)"))
    }

    @Test
    fun ch1_st5_instruction_and_cards_unchanged() {
        assertEquals(11, Season2Ch1QaPolicy.WhichWordStartsWithInstructionUpDp.value.toInt())
        assertEquals(4, Season2Ch1QaPolicy.WhichWordStartsWithLayoutPilotCardsDownDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionUpDp"))
        assertTrue(imageMatch.contains("WhichWordStartsWithLayoutPilotCardsDownDp"))
    }

    @Test
    fun ch1_st6_shift_adjusted_right_again() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
    }

    @Test
    fun ch1_st6_hint_hidden() {
        assertTrue(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
    }

    @Test
    fun s1_unchanged() {
        assertFalse(Season2Ch1QaPolicy.isWhichWordStartsWithLayoutPilot(null))
        assertEquals(38, Season2Ch1QaPolicy.PictureStartsWithLayoutPilotCardsDownDp.value.toInt())
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
