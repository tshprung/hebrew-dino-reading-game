package com.tal.hebrewdino.ui.domain

/** Shared Season 2 chapter-flow rules propagated from approved Ch1/Ch2 behavior to Ch3–6. */
object Season2ChapterFlowPolicy {
    /** Chapter reward overlay only on first-time St6 completion (no replay reward). */
    fun shouldRequestFirstTimeChapterReward(
        stationId: Int,
        wasStationAlreadyDone: Boolean,
        chapterWasCompleteBefore: Boolean,
        finalStationId: Int = Season2Chapter1StationOrder.FINALE_STATION,
    ): Boolean =
        stationId == finalStationId &&
            !wasStationAlreadyDone &&
            !chapterWasCompleteBefore

    /** Season 2 finale — summary screen after Ch.7 St6 (first time and replay). */
    fun shouldShowSeasonCompleteSummary(
        chapterIndex: Int,
        stationId: Int,
        finalStationId: Int = Season2Chapter1StationOrder.FINALE_STATION,
    ): Boolean = chapterIndex == 7 && stationId == finalStationId
}
