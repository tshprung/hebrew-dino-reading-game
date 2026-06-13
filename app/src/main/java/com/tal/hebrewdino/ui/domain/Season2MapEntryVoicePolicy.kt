package com.tal.hebrewdino.ui.domain

import androidx.annotation.RawRes
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.audio.Season2StoryAudio

/** When to play the Season 2 chapter-map tile-entry instruction voice. */
object Season2MapEntryVoicePolicy {
    @RawRes
    fun mapEntryInstructionRawRes(
        chapterId: Int,
        chapterFullyRevealed: Boolean,
        nextPlayablePosterTile: Int?,
        entryFromChapterSelect: Boolean,
    ): Int {
        if (
            Season2Ch1QaPolicy.shouldUseCompletedReplayTilesEntryVoice(
                chapterFullyRevealed = chapterFullyRevealed,
                entryFromChapterSelect = entryFromChapterSelect,
            )
        ) {
            return Season2RawAudio.MapEntryReplayTiles
        }
        require(nextPlayablePosterTile != null) {
            "Missing next playable tile for in-progress map entry instruction (chapterFullyRevealed=false)"
        }
        return Season2RawAudio.MapEntryNextTile
    }

    fun shouldOrchestrateMapEntryVoice(
        progressHydrated: Boolean,
        showChapterIntroOverlay: Boolean,
        entryFromChapterSelect: Boolean,
        mapReturnCaptionEvent: Long,
        mapEntryInstructionSpoken: Boolean,
        suppressBecauseStationReturn: Boolean,
    ): Boolean =
        Season2Ch1QaPolicy.shouldOrchestrateMapEntryFromChapterList(
            progressHydrated = progressHydrated,
            showChapterIntroOverlay = showChapterIntroOverlay,
            entryFromChapterSelect = entryFromChapterSelect,
            mapReturnCaptionEvent = mapReturnCaptionEvent,
            mapEntryInstructionSpoken = mapEntryInstructionSpoken,
            suppressBecauseStationReturn = suppressBecauseStationReturn,
        )

    fun shouldPlayPuzzleExplainBeforeEntry(
        chapterId: Int,
        completedStationCount: Int,
        puzzleMapExplainHeard: Boolean,
    ): Boolean =
        Season2Ch1QaPolicy.shouldPlayPuzzleExplainBeforeMapEntry() &&
            Season2StoryAudio.shouldPlayPuzzleMapExplain(
                showChapterIntroOverlay = false,
                completedStationCount = completedStationCount,
                puzzleMapExplainHeard = puzzleMapExplainHeard,
            )

    fun shouldSuppressBecauseStationReturn(mapReturnCaptionEvent: Long): Boolean =
        mapReturnCaptionEvent != 0L
}
