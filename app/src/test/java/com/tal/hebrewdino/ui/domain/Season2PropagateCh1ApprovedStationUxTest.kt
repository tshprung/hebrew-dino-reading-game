package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2PropagateCh1ApprovedStationUxTest {
    @Test
    fun s2_chapters_stationKindMapping_and_corePolicies() {
        val expectedKinds =
            mapOf(
                2 to 5 to Season2ChapterStationPlans.StationKind.DragMissingLetter,
                2 to 6 to Season2ChapterStationPlans.StationKind.WordParts,
                4 to 5 to Season2ChapterStationPlans.StationKind.PictureToWord,
                5 to 6 to Season2ChapterStationPlans.StationKind.WordParts,
                6 to 6 to Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            )
        for ((key, kind) in expectedKinds) {
            assertEquals(kind, Season2StationQaPolicy.expectedStationKind(key.first, key.second))
        }
        assertTrue(Season2StationQaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(Season2Chapter1StationOrder.POP_BALLOONS))
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2ChapterIds.Chapter3Stegosaurus,
                season2UxStationId = 2,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldReplayPictureToWordCoachWithInstruction(
                Season2ChapterIds.Chapter4Brachiosaurus,
                season2UxStationId = 5,
            ),
        )
        assertTrue(Season2PostFocusCorrectPolicy.shouldPlayCompanionPraiseOnCorrect(season2HadCoachIntervention = true))
    }

    @Test
    fun s2_noDuplicatePraise_and_s1Isolation() {
        assertTrue(
            Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                Season2ChapterIds.Chapter5Ankylosaurus,
                season2UxStationId = 4,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2ChapterIds.Chapter5Ankylosaurus,
                season2UxStationId = 4,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        assertFalse(Season2StationQaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(null))
        assertFalse(Season2StationQaPolicy.isWhichWordStartsWithStation(1, null))
    }
}
