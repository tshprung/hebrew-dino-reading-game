package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1QaMicrofixInputLayoutTest {
    @Test
    fun ch1_st1_taps_not_locked_during_feedback() {
        assertTrue(
            Season2Ch1QaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(
                Season2Chapter1StationOrder.POP_BALLOONS,
            ),
        )
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("keepPopBalloonsInputUnlocked"))
        assertTrue(gameScreen.contains("if (!keepPopBalloonsInputUnlocked)"))
    }

    @Test
    fun ch1_st1_new_tap_cancels_previous_feedback() {
        assertTrue(Season2Ch1QaPolicy.shouldCancelPreviousFeedbackOnPopBalloonsTap(season2QuizBalloons = true))
        val pop = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(pop.contains("shouldCancelPreviousFeedbackOnPopBalloonsTap"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("if (isSeason2BalloonStation)"))
        assertTrue(gameScreen.contains("cancelFeedbackVoiceCb()"))
    }

    @Test
    fun ch1_st1_no_duplicate_scoring_completion() {
        val level = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/LevelScreen.kt")
        assertTrue(level.contains("if (idx < alive.size) alive[idx] = false"))
        val pop = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(pop.contains("gameViewModel.inputLocked = true"))
        assertTrue(pop.contains("submitAnswer(lastLetter)"))
    }

    @Test
    fun ch1_st5_layout_cards_down_instruction_single_RTL_line() {
        assertTrue(
            Season2Ch1QaPolicy.isWhichWordStartsWithLayoutStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        assertEquals(4, Season2Ch1QaPolicy.WhichWordStartsWithLayoutPilotCardsDownDp.value.toInt())
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("useWhichWordCompactLayout"))
        assertTrue(imageMatch.contains("WhichWordStartsWithLayoutPilotCardsDownDp"))
        assertTrue(imageMatch.contains("maxLines = 1"))
        val renderer = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchQuestionRenderer.kt")
        assertTrue(renderer.contains("imageMatchHeaderReadablePanel && !usesWhichWordCompactLayout"))
    }

    @Test
    fun ch1_st5_correct_praise_not_clipped_policy() {
        assertTrue(
            Season2Ch1QaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
        assertTrue(actions.contains("shouldOrchestrateWhichWordCorrectPraiseInStation"))
        assertTrue(actions.contains("joinSilently(audioJob)"))
        assertTrue(actions.contains("InStationPraiseAudio.pick"))
    }

    @Test
    fun ch1_st6_core_shift_left_away_from_dino() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
        assertTrue(imageToWord.contains("FinaleDinoReservedWidthDp"))
        assertTrue(imageToWord.contains("FinaleExtraDownDp"))
    }

    @Test
    fun ch1_st6_hint_hidden() {
        assertTrue(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
        val overlay = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameOverlayLayer.kt")
        assertTrue(overlay.contains("showHintButton"))
        val help = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/components/Episode4Stations15HelpColumn.kt")
        assertTrue(help.contains("if (showHintButton)"))
    }

    @Test
    fun s1_unchanged() {
        assertFalse(
            Season2Ch1QaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(null),
        )
        assertFalse(
            Season2Ch1QaPolicy.isWhichWordStartsWithLayoutPilot(null),
        )
        assertFalse(
            Season2Ch1QaPolicy.shouldHideFinaleHintButton(1, Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH),
        )
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
