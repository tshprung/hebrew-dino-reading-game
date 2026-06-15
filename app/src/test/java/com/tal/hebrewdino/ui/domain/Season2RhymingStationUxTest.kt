package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2RhymingStationUxTest {
    @Test
    fun rhymingCoachFeedback_replaysFullInstructionAndTargetWord() {
        val coachSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2GuessingCoach.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/domain/Season2GuessingCoach.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(coachSource.contains("StationKind.Rhyming"))
        assertTrue(coachSource.contains("replayAdvancedInstructionAndWord"))
    }

    @Test
    fun rhymingPick_playsChoiceThenTargetThenChoiceOnCorrect() {
        val actionsSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(actionsSource.contains("handleRhymingPick(choice)"))
        assertTrue(actionsSource.contains("handleRhymingPick(target)"))
        assertTrue(actionsSource.contains("handleRhymingPick(choice-repeat)"))
        assertTrue(actionsSource.contains("playWordByCatalogId"))
    }

    @Test
    fun rhymingLayout_usesUniformCardsTightRowsAndPhysicalGaps() {
        val layoutSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/game/Season2RhymingGame.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/game/Season2RhymingGame.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(layoutSource.contains("BottomRowChoiceGap"))
        assertTrue(layoutSource.contains("* 0.7f"))
        assertTrue(layoutSource.contains("captionAreaHeight"))
        assertTrue(layoutSource.contains("captionWrapContent = false"))
        assertFalse(layoutSource.contains("captionWrapContent = true"))
        assertFalse(layoutSource.contains("ChoiceGapMultiplier"))
        assertFalse(layoutSource.contains("InstructionDownOffset"))
        assertFalse(layoutSource.contains("choiceToTargetWidthRatio"))
        assertTrue(layoutSource.contains("Alignment.TopCenter"))
    }

    @Test
    fun rhymingDisplayWords_usePlainCatalogText() {
        val displaySource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2RhymingDisplayWords.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/domain/Season2RhymingDisplayWords.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(displaySource.contains("plainWord"))
        assertFalse(displaySource.contains("בָּלוֹן"))
    }
}
