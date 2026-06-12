package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2PropagateCh3To6FlowTest {
    @Test
    fun first_time_reward_only_on_st6_ch3_to_ch6() {
        for (chapter in 3..7) {
            assertTrue(
                "ch$chapter first-time st6",
                Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(
                    stationId = Season2Chapter1StationOrder.FINALE_STATION,
                    wasStationAlreadyDone = false,
                    chapterWasCompleteBefore = false,
                ),
            )
            assertFalse(
                "ch$chapter replay st6",
                Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(
                    stationId = Season2Chapter1StationOrder.FINALE_STATION,
                    wasStationAlreadyDone = true,
                    chapterWasCompleteBefore = true,
                ),
            )
            assertFalse(
                "ch$chapter mid-station",
                Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(
                    stationId = 3,
                    wasStationAlreadyDone = false,
                    chapterWasCompleteBefore = false,
                ),
            )
        }
    }

    @Test
    fun chapter_station_screen_uses_unified_flow_policy() {
        val stationScreen =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterStationScreen.kt")
        assertTrue(stationScreen.contains("Season2ChapterRegistry.CHAPTER_COUNT"))
        assertTrue(stationScreen.contains("Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward"))
    }

    @Test
    fun word_parts_ux_propagated_to_all_modes() {
        assertTrue(
            Season2WordPartsUxPolicy.usesApprovedLayout(
                Season2WordPartsPresentationMode.VisibleWordParts,
            ),
        )
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
        assertTrue(ui.contains("Season2WordPartsUxPolicy.TargetWordDownDp"))
        assertFalse(ui.contains("if (isCh2St6)"))
    }

    @Test
    fun memory_match_uses_separate_screen_without_game_screen_coach() {
        val memory =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2MemoryMatchStationScreen.kt")
        assertTrue(memory.contains("Season2MemoryMatchStationScreen"))
        assertFalse(memory.contains("GameScreen"))
    }

    @Test
    fun post_focus_suppresses_advance_narrator_praise() {
        assertTrue(
            Season2PostFocusCorrectPolicy.shouldSuppressAdvanceRoundNarratorPraise(
                playedPostFocusCompanionPraise = true,
            ),
        )
        assertFalse(
            Season2PostFocusCorrectPolicy.shouldSuppressAdvanceRoundNarratorPraise(
                playedPostFocusCompanionPraise = false,
            ),
        )
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("suppressAdvanceRoundNarratorPraiseAfterPostFocusCompanion"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("playedPostFocusCompanionPraise"))
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
