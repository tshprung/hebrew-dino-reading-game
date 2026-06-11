package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1St6AlignAndWhichWordGeneralizeTest {
    @Test
    fun s2_ch1_st6_image_centered_to_instruction() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
        assertTrue(imageToWord.contains("horizontalAlignment = Alignment.CenterHorizontally"))
        assertTrue(imageToWord.contains("wrapContentWidth(Alignment.CenterHorizontally)"))
        assertFalse(imageToWord.contains("FinaleInstructionImageAbsoluteOffsetXDp"))
    }

    @Test
    fun s2_ch1_st6_middle_word_centered_to_image() {
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("FinaleDinoReservedWidthDp"))
        assertTrue(imageToWord.contains("ImageToWordOptionsRow"))
        assertTrue(imageToWord.contains("Modifier.wrapContentWidth(Alignment.CenterHorizontally)"))
        assertTrue(imageToWord.contains("middle option x-center"))
    }

    @Test
    fun s2_ch1_st6_no_hint() {
        assertTrue(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        val ch1Block =
            imageToWord
                .substringAfter("private fun Ch1FinaleImageToWordAlignedContent")
                .substringBefore("private fun ImageToWordOptionsRow")
        assertFalse(ch1Block.contains("hintLetter"))
        assertFalse(ch1Block.contains("FilledTonalButton"))
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
        assertTrue(imageMatch.contains("useWhichWordCompactLayout"))
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionFontScale"))
    }

    @Test
    fun which_word_starts_with_behavior_content_unchanged() {
        val registry = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/StationBehaviorRegistry.kt")
        assertTrue(registry.contains("PICTURE_PICK_ALL"))
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
        assertTrue(actions.contains("shouldOrchestrateWhichWordCorrectPraiseInStation"))
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
