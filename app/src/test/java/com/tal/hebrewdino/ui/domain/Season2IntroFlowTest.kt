package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2IntroFlowTest {
    @Test
    fun seasonIntro_onlyWhenEnteredFromSeasons() {
        assertTrue(Season2IntroFlow.shouldShowSeasonIntro(entryFromSeasons = true))
        assertFalse(Season2IntroFlow.shouldShowSeasonIntro(entryFromSeasons = false))
    }

    @Test
    fun chapterIntro_onlyWhenEnteredFromChapterSelect() {
        assertTrue(Season2IntroFlow.shouldShowChapterIntro(entryFromChapterSelect = true))
        assertFalse(Season2IntroFlow.shouldShowChapterIntro(entryFromChapterSelect = false))
    }

    @Test
    fun chapterIntro_notShownWhenReturningFromStation() {
        assertFalse(Season2IntroFlow.shouldShowChapterIntro(entryFromChapterSelect = false))
    }

    @Test
    fun celebrateFromProgress_onlyAfterFinalStationAddedOnce() {
        assertTrue(
            Season2IntroFlow.shouldCelebrateFromStationProgress(
                addedStationCount = 1,
                newStationId = 6,
                previousCompletedCount = 5,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldCelebrateFromStationProgress(
                addedStationCount = 1,
                newStationId = 5,
                previousCompletedCount = 4,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldCelebrateFromStationProgress(
                addedStationCount = 6,
                newStationId = 6,
                previousCompletedCount = 0,
            ),
        )
    }

    @Test
    fun celebrateFromFirstTimeFinalStation_whenChapterNotYetComplete() {
        assertTrue(
            Season2IntroFlow.shouldCelebrateFromFirstTimeFinalStation(
                stationId = 6,
                chapterWasCompleteBefore = false,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldCelebrateFromFirstTimeFinalStation(
                stationId = 5,
                chapterWasCompleteBefore = false,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldCelebrateFromFirstTimeFinalStation(
                stationId = 6,
                chapterWasCompleteBefore = true,
            ),
        )
    }

    @Test
    fun requestCelebration_firstTimeOrReplayFinalOnly() {
        assertTrue(
            Season2IntroFlow.shouldRequestChapterCelebration(
                stationId = 6,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
        assertTrue(
            Season2IntroFlow.shouldRequestChapterCelebration(
                stationId = 6,
                wasStationAlreadyDone = true,
                chapterWasCompleteBefore = true,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldRequestChapterCelebration(
                stationId = 3,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
    }

    @Test
    fun celebrateFromReplay_onlyWhenFinalStationReplayedOnCompleteChapter() {
        assertTrue(
            Season2IntroFlow.shouldCelebrateFromReplayedFinalStation(
                stationId = 6,
                wasStationAlreadyDone = true,
                chapterWasComplete = true,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldCelebrateFromReplayedFinalStation(
                stationId = 5,
                wasStationAlreadyDone = true,
                chapterWasComplete = true,
            ),
        )
        assertFalse(
            Season2IntroFlow.shouldCelebrateFromReplayedFinalStation(
                stationId = 6,
                wasStationAlreadyDone = false,
                chapterWasComplete = false,
            ),
        )
    }
}
