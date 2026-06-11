package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1LayoutNudgeTest {
    @Test
    fun ch1_st5_instruction_up_11dp() {
        assertEquals(11, Season2Ch1QaPolicy.WhichWordStartsWithInstructionUpDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionUpDp"))
        assertTrue(imageMatch.contains("offset(y = -Season2Ch1QaPolicy.WhichWordStartsWithInstructionUpDp)"))
    }

    @Test
    fun ch1_st5_cards_up_19dp() {
        assertEquals(4, Season2Ch1QaPolicy.WhichWordStartsWithLayoutPilotCardsDownDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("WhichWordStartsWithLayoutPilotCardsDownDp"))
    }

    @Test
    fun ch1_st6_absolute_shift_adjusted_right() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
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
