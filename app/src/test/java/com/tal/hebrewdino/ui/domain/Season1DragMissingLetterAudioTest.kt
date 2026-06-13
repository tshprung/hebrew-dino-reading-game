package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season1DragMissingLetterAudioTest {
    @Test
    fun dragMissingLetterInstruction_mapsToSeason1RawAsset() {
        assertEquals(
            R.raw.s1_drag_missing_letter_instruction,
            Season1StationAudio.dragMissingLetterInstructionRawResId(),
        )
    }

    @Test
    fun dragMissingLetterInstruction_appliesOnlyToSeason1Chapter5Station2AndChapter6Station4() {
        assertTrue(Season1StationAudio.isSeason1DragMissingLetterStation(chapterId = 5, stationId = 2))
        assertTrue(Season1StationAudio.isSeason1DragMissingLetterStation(chapterId = 6, stationId = 4))
        assertFalse(Season1StationAudio.isSeason1DragMissingLetterStation(chapterId = 5, stationId = 3))
        assertFalse(Season1StationAudio.isSeason1DragMissingLetterStation(chapterId = 6, stationId = 3))
        assertFalse(Season1StationAudio.isSeason1DragMissingLetterStation(chapterId = 101, stationId = 2))
    }

    @Test
    fun dragMissingLetterBehavior_includesSeason2ParityStations() {
        assertTrue(
            Season1StationAudio.isDragMissingLetterBehaviorStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
        assertTrue(
            Season1StationAudio.isDragMissingLetterBehaviorStation(
                Season2ChapterIds.Chapter2Triceratops,
                5,
            ),
        )
        assertEquals(
            5 to 2,
            Season1StationAudio.resolveDragMissingLetterBehavior(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
        assertFalse(
            Season1StationAudio.isDragMissingLetterBehaviorStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
    }

    @Test
    fun resolveDragMissingLetterBehavior_mapsBothStationsToCanonicalCh5St2() {
        assertEquals(5 to 2, Season1StationAudio.resolveDragMissingLetterBehavior(5, 2))
        assertEquals(5 to 2, Season1StationAudio.resolveDragMissingLetterBehavior(6, 4))
        assertEquals(3 to 4, Season1StationAudio.resolveDragMissingLetterBehavior(3, 4))
    }

    @Test
    fun visibleInstruction_matchesSpokenPrompt() {
        assertEquals(
            "\u200Fגררו את האות החסרה למקום הנכון.",
            DragMissingLetterCopy.Instruction,
        )
    }

    @Test
    fun dragMissingLetterRoundIntro_usesTightInstructionToWordHandoff() {
        assertEquals(30L, Season1StationAudio.DRAG_MISSING_LETTER_INSTRUCTION_TO_WORD_GAP_MS)
        val source =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/domain/Season1StationAudio.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/domain/Season1StationAudio.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(source.contains("fun playDragMissingLetterRoundIntro"))
        assertTrue(source.contains("fun playDragMissingLetterCorrectFeedback"))
        assertTrue(source.contains("playDragMissingLetterLetterName"))
        assertTrue(source.contains("playDragMissingLetterWord"))
        assertTrue(source.contains("SfxCorrect"))

        val introSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/screens/GameIntroPromptPlayer.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/screens/GameIntroPromptPlayer.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(source.contains("fun isDragMissingLetterBehaviorStation"))
        assertTrue(introSource.contains("isDragMissingLetterBehaviorStation"))

        val actionsSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/screens/DragMissingLetterActions.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/screens/DragMissingLetterActions.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(actionsSource.contains("isDragMissingLetterBehaviorStation"))

        val hostSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/screens/GameQuestionHost.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/screens/GameQuestionHost.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(hostSource.contains("onLetterSelected = null"))
    }
}
