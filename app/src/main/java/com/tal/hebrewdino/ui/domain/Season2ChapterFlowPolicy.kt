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
}
