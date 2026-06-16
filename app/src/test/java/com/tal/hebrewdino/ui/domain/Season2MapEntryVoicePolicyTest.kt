package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.test.ProjectSource

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2MapEntryVoicePolicyTest {
    @Test
    fun raw_existsAndResolves() {
        assertEquals(R.raw.season2_map_entry_next_tile_01, Season2RawAudio.MapEntryNextTile)
        assertEquals(R.raw.season2_map_entry_replay_tiles_01, Season2RawAudio.MapEntryReplayTiles)
    }

    @Test
    fun inProgress_usesNextTile_whenPlayableTileExists() {
        assertEquals(
            R.raw.season2_map_entry_next_tile_01,
            Season2MapEntryVoicePolicy.mapEntryInstructionRawRes(
                chapterId = 1,
                chapterFullyRevealed = false,
                nextPlayablePosterTile = 1,
                entryFromChapterSelect = true,
            ),
        )
    }

    @Test
    fun completed_usesReplayTiles() {
        assertEquals(
            R.raw.season2_map_entry_replay_tiles_01,
            Season2MapEntryVoicePolicy.mapEntryInstructionRawRes(
                chapterId = 1,
                chapterFullyRevealed = true,
                nextPlayablePosterTile = null,
                entryFromChapterSelect = true,
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun inProgress_failsWithoutPlayableTile() {
        Season2MapEntryVoicePolicy.mapEntryInstructionRawRes(
            chapterId = 1,
            chapterFullyRevealed = false,
            nextPlayablePosterTile = null,
            entryFromChapterSelect = true,
        )
    }

    @Test
    fun orchestrate_whenMapReadyAndNotStationReturn() {
        assertTrue(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = false,
                entryFromChapterSelect = true,
                mapReturnCaptionEvent = 0L,
                mapEntryInstructionSpoken = false,
                suppressBecauseStationReturn = false,
            ),
        )
    }

    @Test
    fun orchestrate_blockedDuringChapterIntro() {
        assertFalse(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = true,
                entryFromChapterSelect = true,
                mapReturnCaptionEvent = 0L,
                mapEntryInstructionSpoken = false,
                suppressBecauseStationReturn = false,
            ),
        )
    }

    @Test
    fun orchestrate_blockedAfterSpoken() {
        assertFalse(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = false,
                entryFromChapterSelect = true,
                mapReturnCaptionEvent = 0L,
                mapEntryInstructionSpoken = true,
                suppressBecauseStationReturn = false,
            ),
        )
    }

    @Test
    fun orchestrate_blockedOnStationReturn() {
        assertTrue(Season2MapEntryVoicePolicy.shouldSuppressBecauseStationReturn(42L))
        assertFalse(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = false,
                entryFromChapterSelect = true,
                mapReturnCaptionEvent = 42L,
                mapEntryInstructionSpoken = false,
                suppressBecauseStationReturn = false,
            ),
        )
        assertFalse(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = false,
                entryFromChapterSelect = true,
                mapReturnCaptionEvent = 0L,
                mapEntryInstructionSpoken = false,
                suppressBecauseStationReturn = true,
            ),
        )
        assertFalse(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = false,
                entryFromChapterSelect = false,
                mapReturnCaptionEvent = 0L,
                mapEntryInstructionSpoken = false,
                suppressBecauseStationReturn = false,
            ),
        )
    }

    @Test
    fun puzzleExplain_thenEntry_onFirstHiddenMap() {
        assertFalse(
            Season2MapEntryVoicePolicy.shouldPlayPuzzleExplainBeforeEntry(
                chapterId = 1,
                completedStationCount = 0,
                puzzleMapExplainHeard = false,
            ),
        )
        assertFalse(
            Season2MapEntryVoicePolicy.shouldPlayPuzzleExplainBeforeEntry(
                chapterId = 2,
                completedStationCount = 0,
                puzzleMapExplainHeard = false,
            ),
        )
        assertFalse(
            Season2MapEntryVoicePolicy.shouldPlayPuzzleExplainBeforeEntry(
                chapterId = 2,
                completedStationCount = 0,
                puzzleMapExplainHeard = true,
            ),
        )
    }

    @Test
    fun screen_usesMapEntryClips_notReplayTileInstruction() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(source.contains("Season2MapEntryVoicePolicy.mapEntryInstructionRawRes"))
        assertTrue(source.contains("Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice"))
        assertFalse(source.contains("replayTileInstructionVoiceRawRes()"))
        assertTrue(source.indexOf("mapEntryInstructionSpoken = true") > source.indexOf("mapEntryInstructionRawRes"))
        assertFalse(source.contains("puzzleExplainStarted"))
        assertFalse(source.indexOf("mapEntryInstructionSpoken = true") < source.indexOf("mapEntryInstructionRawRes"))
    }

    @Test
    fun screen_sequence_chapterIntroThenPuzzleExplainThenMapEntry() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(source.contains("puzzleMapExplainHeardFlow(chapterId).first()"))
        assertTrue(source.contains("LaunchedEffect(chapterId, progressHydrated, showIntro, mapReturnCaptionEvent)"))
        assertTrue(
            source.indexOf("playRawBlocking(Season2StoryAudio.PuzzleMapExplain)") <
                source.indexOf("playRawBlocking(mapEntryRawRes)"),
        )
        assertTrue(source.contains("showChapterIntroOverlay = showIntro"))
    }

    @Test
    fun screen_stationReturn_playsMapPraiseOnly() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(source.contains("playMapReturnVoiceForCompletedCount"))
        assertTrue(source.contains("shouldSuppressBecauseStationReturn"))
        assertFalse(source.contains("replayTileInstructionVoiceRawRes()"))
    }

    @Test
    fun rawFiles_existOnDisk() {
        val rawDir = locateRawDir()
        assertTrue(java.io.File(rawDir, "season2_map_entry_next_tile_01.mp3").exists())
        assertTrue(java.io.File(rawDir, "season2_map_entry_replay_tiles_01.mp3").exists())
    }

    private fun locateRawDir(): java.io.File {
        val candidates =
            listOf(
                java.io.File("app/src/main/res/raw"),
                java.io.File("../app/src/main/res/raw"),
                java.io.File("../../app/src/main/res/raw"),
            )
        return candidates.first { it.isDirectory }
    }
}
