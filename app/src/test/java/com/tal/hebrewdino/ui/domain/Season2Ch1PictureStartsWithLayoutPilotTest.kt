package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1PictureStartsWithLayoutPilotTest {
    @Test
    fun pilot_station_layout_flagged_only() {
        assertTrue(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
                1,
                Chapter1StationOrder.PICTURE_PICK_ONE,
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
                Season2ChapterIds.Chapter3Stegosaurus,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
    }

    @Test
    fun instruction_between_progress_and_cards() {
        val game = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/PictureStartsWithGame.kt")
        assertTrue(game.contains("useCh1LayoutPilot"))
        assertTrue(game.contains("if (useCh1LayoutPilot)"))
        assertTrue(game.contains("Column(modifier = modifier.fillMaxSize())"))
        val renderer =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithQuestionRenderer.kt")
        assertTrue(renderer.contains("isPictureStartsWithLayoutPilot"))
        assertTrue(renderer.contains("instructionReadablePanel && !layoutPilot"))
    }

    @Test
    fun cards_shifted_down() {
        assertEquals(38, Season2Ch1QaPolicy.PictureStartsWithLayoutPilotCardsDownDp.value.toInt())
        val game = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/PictureStartsWithGame.kt")
        assertTrue(game.contains("PictureStartsWithLayoutPilotCardsDownDp"))
    }

    @Test
    fun s1_unchanged() {
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
                1,
                Chapter1StationOrder.PICTURE_PICK_ONE,
            ),
        )
        val game = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/PictureStartsWithGame.kt")
        assertTrue(game.contains("} else {"))
        assertTrue(game.contains("instructionReadablePanel)"))
    }

    @Test
    fun other_stations_unchanged() {
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
                Season2ChapterIds.Chapter2Triceratops,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
        val game = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/PictureStartsWithGame.kt")
        assertTrue(game.contains("align(Alignment.TopCenter)"))
        assertTrue(game.contains("instructionReadablePanel)"))
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
