package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch2St6WordPartsMicrofixTest {
    @Test
    fun word_down() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2Ch2St6WordPartsPolicy.TargetWordDownDp"))
        assertTrue(ui.contains("y = Season2Ch2St6WordPartsPolicy.TargetWordDownDp"))
    }

    @Test
    fun image_left() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2Ch2St6WordPartsPolicy.ImagePhysicalLeftDp"))
        assertTrue(ui.contains("if (isCh2St6)"))
    }

    @Test
    fun options_left() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2Ch2St6WordPartsPolicy.OptionsPhysicalLeftDp"))
    }

    @Test
    fun hint_wrap_content() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("wrapContent = isCh2St6"))
        assertTrue(ui.contains("wrapContentWidth(Alignment.Start)"))
    }

    @Test
    fun hint_left_away_from_image() {
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2Ch2St6WordPartsPolicy.kt")
        assertTrue(policy.contains("HintPhysicalLeftDp = 95.dp"))
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("x = -Season2Ch2St6WordPartsPolicy.HintPhysicalLeftDp"))
    }

    @Test
    fun instruction_left_nudge() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("InstructionPhysicalLeftDp"))
    }

    @Test
    fun correct_post_praise_hold_halved() {
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2Ch2St6WordPartsPolicy.kt")
        assertTrue(policy.contains("CorrectPostPraiseHoldMs: Long = 700L"))
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("Season2Ch2St6WordPartsPolicy.CorrectPostPraiseHoldMs"))
    }

    @Test
    fun correct_filters_immediately_before_audio() {
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("immediateCorrectFilter"))
        assertTrue(actions.contains("wordPartsCompletedEquation ="))
        assertTrue(actions.contains("playSplitTapSequence"))
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2Ch2St6WordPartsPolicy.kt")
        assertTrue(policy.contains("filterCorrectSplitImmediatelyBeforeAudio"))
    }

    @Test
    fun reward_only_first_time_st6_completion() {
        assertTrue(
            Season2Ch2St6WordPartsPolicy.shouldRequestFirstTimeChapterReward(
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
        assertFalse(
            Season2Ch2St6WordPartsPolicy.shouldRequestFirstTimeChapterReward(
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = true,
                chapterWasCompleteBefore = true,
            ),
        )
        val stationScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterStationScreen.kt")
        assertTrue(stationScreen.contains("chapterId == 2") || stationScreen.contains("2 ->"))
        assertTrue(stationScreen.contains("Season2Ch2St6WordPartsPolicy.shouldRequestFirstTimeChapterReward"))
    }

    @Test
    fun S1_unchanged() {
        assertEquals(
            StationQuizMode.PickLetter,
            StationQuizPlans.chapter1(Chapter1StationOrder.TAP_LETTER).mode,
        )
    }

    @Test
    fun other_wordparts_unchanged() {
        assertFalse(
            Season2Ch2St6WordPartsPolicy.isCh2St6WordParts(
                Season2WordPartsPresentationMode.GuidedWordParts,
            ),
        )
        assertFalse(
            Season2Ch2St6WordPartsPolicy.isCh2St6WordParts(
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            ),
        )
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("if (isCh2St6)"))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        return candidates.first { it.exists() }.readText()
    }
}
