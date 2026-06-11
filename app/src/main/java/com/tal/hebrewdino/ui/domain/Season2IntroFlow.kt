package com.tal.hebrewdino.ui.domain

/** SavedStateHandle keys for Season 2 intro / reward navigation signals. */
object Season2NavKeys {
    const val SHOW_SEASON_INTRO = "s2_show_season_intro"
    const val SHOW_CHAPTER_INTRO = "s2_show_chapter_intro"
    const val REQUEST_CHAPTER_CELEBRATION = "s2_request_chapter_celebration"
    /** Monotonic event id — map consumes once to play return caption voice. */
    const val MAP_RETURN_CAPTION_EVENT = "s2_map_return_caption_event"
    /** Completed station count after a fresh station finish (1–5). */
    const val MAP_RETURN_CAPTION_COUNT = "s2_map_return_caption_count"
}

/** Pure logic for Season 2 intro and reward triggers (unit-testable). */
object Season2IntroFlow {
    fun shouldShowSeasonIntro(entryFromSeasons: Boolean): Boolean = entryFromSeasons

    fun shouldShowChapterIntro(
        entryFromChapterSelect: Boolean,
        chapterFullyRevealed: Boolean = false,
    ): Boolean = entryFromChapterSelect && !chapterFullyRevealed

    fun shouldShowChapterIntro(
        chapterId: Int,
        entryFromChapterSelect: Boolean,
        chapterFullyRevealed: Boolean,
    ): Boolean = shouldShowChapterIntro(entryFromChapterSelect, chapterFullyRevealed)

    /**
     * First-time chapter completion via progress update on the puzzle map.
     * Rejects bulk hydration and multi-station jumps.
     */
    fun shouldCelebrateFromStationProgress(
        addedStationCount: Int,
        newStationId: Int,
        previousCompletedCount: Int,
        finalStationId: Int = Season2Chapter1RevealOrder.STATION_COUNT,
    ): Boolean {
        if (addedStationCount != 1) return false
        if (newStationId != finalStationId) return false
        return previousCompletedCount < finalStationId
    }

    /** First-time final station completion (signaled from station screen when map may be disposed). */
    fun shouldCelebrateFromFirstTimeFinalStation(
        stationId: Int,
        chapterWasCompleteBefore: Boolean,
        finalStationId: Int = Season2Chapter1RevealOrder.STATION_COUNT,
    ): Boolean {
        if (stationId != finalStationId) return false
        return !chapterWasCompleteBefore
    }

    /** Replay flow: final station finished again while chapter was already complete. */
    fun shouldCelebrateFromReplayedFinalStation(
        stationId: Int,
        wasStationAlreadyDone: Boolean,
        chapterWasComplete: Boolean,
        finalStationId: Int = Season2Chapter1RevealOrder.STATION_COUNT,
    ): Boolean {
        if (stationId != finalStationId) return false
        return wasStationAlreadyDone && chapterWasComplete
    }

    fun shouldRequestChapterCelebration(
        stationId: Int,
        wasStationAlreadyDone: Boolean,
        chapterWasCompleteBefore: Boolean,
        finalStationId: Int = Season2Chapter1RevealOrder.STATION_COUNT,
    ): Boolean =
        shouldCelebrateFromFirstTimeFinalStation(
            stationId = stationId,
            chapterWasCompleteBefore = chapterWasCompleteBefore,
            finalStationId = finalStationId,
        ) ||
            shouldCelebrateFromReplayedFinalStation(
                stationId = stationId,
                wasStationAlreadyDone = wasStationAlreadyDone,
                chapterWasComplete = chapterWasCompleteBefore,
                finalStationId = finalStationId,
            )
}
