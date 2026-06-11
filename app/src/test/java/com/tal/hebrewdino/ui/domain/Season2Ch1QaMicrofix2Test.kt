package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1QaMicrofix2Test {
    @Test
    fun ch1_st5_instruction_bg_present_compact() {
        assertEquals(0.72f, Season2Ch1QaPolicy.WhichWordStartsWithInstructionBgAlpha, 0.001f)
        val imageMatch = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageMatchGame.kt")
        assertTrue(imageMatch.contains("wrapContentWidth"))
        assertTrue(imageMatch.contains("WhichWordStartsWithInstructionBgAlpha"))
        assertTrue(imageMatch.contains("maxLines = 1"))
    }

    @Test
    fun ch1_st5_cards_moved_up_from_pilot() {
        assertEquals(4, Season2Ch1QaPolicy.WhichWordStartsWithLayoutPilotCardsDownDp.value.toInt())
    }

    @Test
    fun ch1_st5_correct_single_praise() {
        assertTrue(
            Season2Ch1QaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("shouldOrchestrateWhichWordCorrectPraiseInStation"))
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
        assertTrue(actions.contains("joinSilently(audioJob)"))
    }

    @Test
    fun ch1_st6_shift_away_from_dino_increased() {
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
        val imageToWord = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/ImageToWordGame.kt")
        assertTrue(imageToWord.contains("Ch1FinaleImageToWordAlignedContent"))
    }

    @Test
    fun ch1_reward_only_on_first_chapter_completion() {
        assertTrue(
            Season2Ch1QaPolicy.shouldRequestFirstTimeChapterReward(
                registryChapterId = 1,
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.shouldRequestFirstTimeChapterReward(
                registryChapterId = 1,
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = true,
                chapterWasCompleteBefore = true,
            ),
        )
    }

    @Test
    fun ch1_replay_st6_no_reward() {
        assertFalse(
            Season2Ch1QaPolicy.shouldRequestFirstTimeChapterReward(
                registryChapterId = 1,
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = true,
                chapterWasCompleteBefore = true,
            ),
        )
        val station = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterStationScreen.kt")
        assertTrue(station.contains("shouldRequestFirstTimeChapterReward"))
    }

    @Test
    fun ch1_map_entry_single_instruction_voice() {
        assertFalse(Season2Ch1QaPolicy.shouldPlayPuzzleExplainBeforeMapEntry(registryChapterId = 1))
        assertTrue(Season2Ch1QaPolicy.shouldPlayPuzzleExplainBeforeMapEntry(registryChapterId = 2))
        assertFalse(
            Season2MapEntryVoicePolicy.shouldPlayPuzzleExplainBeforeEntry(
                chapterId = 1,
                completedStationCount = 0,
                puzzleMapExplainHeard = false,
            ),
        )
        val map = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(map.contains("playRawBlocking(mapEntryRawRes)"))
    }

    @Test
    fun s1_unchanged() {
        assertTrue(Season2Ch1QaPolicy.shouldPlayPuzzleExplainBeforeMapEntry(registryChapterId = 2))
        assertFalse(
            Season2Ch1QaPolicy.shouldRequestFirstTimeChapterReward(
                registryChapterId = 2,
                stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
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
