package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch2St6WordPartsMicrofixTest {
    @Test
    fun word_down() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2WordPartsUxPolicy.TargetWordDownDp"))
        assertTrue(ui.contains("y = Season2WordPartsUxPolicy.TargetWordDownDp"))
    }

    @Test
    fun image_left() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2WordPartsUxPolicy.ImagePhysicalLeftDp"))
        assertFalse(ui.contains("if (isCh2St6)"))
    }

    @Test
    fun options_left() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2WordPartsUxPolicy.OptionsPhysicalLeftDp"))
    }

    @Test
    fun hint_wrap_content() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("wrapContent = true"))
        assertTrue(ui.contains("wrapContentWidth(Alignment.Start)"))
    }

    @Test
    fun hint_left_away_from_image() {
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2WordPartsUxPolicy.kt")
        assertTrue(policy.contains("HintPhysicalLeftDp = 95.dp"))
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("x = -Season2WordPartsUxPolicy.HintPhysicalLeftDp"))
    }

    @Test
    fun instruction_left_nudge() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("InstructionPhysicalLeftDp"))
    }

    @Test
    fun correct_post_praise_hold_halved() {
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2WordPartsUxPolicy.kt")
        assertTrue(policy.contains("CorrectPostPraiseHoldMs: Long = 700L"))
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("Season2WordPartsUxPolicy.CorrectPostPraiseHoldMs"))
    }

    @Test
    fun correct_filters_immediately_before_audio() {
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("immediateCorrectFilter"))
        assertTrue(actions.contains("wordPartsCompletedEquation ="))
        assertTrue(actions.contains("playSplitTapSequence"))
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2WordPartsUxPolicy.kt")
        assertTrue(policy.contains("filterCorrectSplitImmediatelyBeforeAudio"))
    }

    @Test
    fun reward_only_first_time_st6_completion() {
        assertTrue(
            Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
        assertFalse(
            Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = true,
                chapterWasCompleteBefore = true,
            ),
        )
        val stationScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterStationScreen.kt")
        assertTrue(stationScreen.contains("chapterId in 1..6"))
        assertTrue(stationScreen.contains("Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward"))
    }

    @Test
    fun S1_unchanged() {
        assertEquals(
            StationQuizMode.PickLetter,
            StationQuizPlans.chapter1(Chapter1StationOrder.TAP_LETTER).mode,
        )
    }

    @Test
    fun all_wordparts_modes_use_approved_layout() {
        assertTrue(
            Season2WordPartsUxPolicy.usesApprovedLayout(
                Season2WordPartsPresentationMode.GuidedWordParts,
            ),
        )
        assertTrue(
            Season2WordPartsUxPolicy.usesApprovedLayout(
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            ),
        )
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertFalse(ui.contains("if (isCh2St6)"))
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
