package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ch2 reordered slots must mirror Ch1 equivalent station behavior (words/pictures/letters differ only). */
class Season1Chapter2ParityTest {
    private data class SlotParity(
        val ch1Station: Int,
        val ch2Station: Int,
        val ch1PlanStation: Int,
        val ch2PlanStation: Int,
    )

    @Test
    fun chapter2_reorderedSlots_matchChapter1_equivalentStations() {
        val slots =
            listOf(
                SlotParity(
                    ch1Station = Chapter1StationOrder.REVEAL_THEN_CHOOSE,
                    ch2Station = 2,
                    ch1PlanStation = 3,
                    ch2PlanStation = 2,
                ),
                SlotParity(
                    ch1Station = Chapter1StationOrder.PICTURE_PICK_ONE,
                    ch2Station = 3,
                    ch1PlanStation = 4,
                    ch2PlanStation = 3,
                ),
                SlotParity(
                    ch1Station = Chapter1StationOrder.PICTURE_PICK_ALL,
                    ch2Station = 4,
                    ch1PlanStation = 5,
                    ch2PlanStation = 4,
                ),
            )
        for ((ch1St, ch2St, ch1PlanSt, ch2PlanSt) in slots) {
            assertEquals(StationQuizPlans.chapter1(ch1PlanSt), StationQuizPlans.chapter2(ch2PlanSt))
            val ch1Spec = StationBehaviorRegistry.getStationUiSpec(1, ch1St)
            val ch2Spec = StationBehaviorRegistry.getStationUiSpec(2, ch2St)
            assertEquals(ch1Spec.templateId, ch2Spec.templateId)
            when (ch1St) {
                Chapter1StationOrder.REVEAL_THEN_CHOOSE -> {
                    assertTrue(ch2Spec.findGridSagaRevealStation)
                    assertTrue(ch2Spec.findGridUseEpisode4HelpHints)
                    assertTrue(ch2Spec.audioStagingFindGrid)
                }
                Chapter1StationOrder.PICTURE_PICK_ONE -> {
                    assertTrue(SixStationArcQaPolicy.isSagaPictureStartsWithStation(1, ch1St))
                    assertTrue(SixStationArcQaPolicy.isSagaPictureStartsWithStation(2, ch2St))
                    assertTrue(ch2Spec.pictureStartsWithSagaStation)
                    assertEquals(19f, ch2Spec.pictureStartsWithVerticalNudgeDp, 0f)
                }
                Chapter1StationOrder.PICTURE_PICK_ALL -> {
                    assertTrue(SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(1, ch1St))
                    assertTrue(SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(2, ch2St))
                    assertFalse(SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(2, 6))
                    assertTrue(ch2Spec.imageMatchSagaWhichWordStation)
                    assertEquals(19f, ch2Spec.imageMatchVerticalNudgeDp, 0f)
                    assertTrue(ch2Spec.imageMatchSuppressEntryPulseEpoch)
                }
            }
        }
    }

    @Test
    fun sixStationArc_whichWordUxTiming_andUxIdResolution() {
        for ((chapterId, stationId) in listOf(1 to Chapter1StationOrder.PICTURE_PICK_ALL, 2 to 4)) {
            val uxId =
                SixStationArcQaPolicy.resolveUxStationIdForQa(
                    chapterId = chapterId,
                    stationId = stationId,
                    season2UxStationId = null,
                )
            assertEquals(Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH, uxId)
            assertTrue(Season2StationQaPolicy.useTightBetweenRoundTiming(chapterId, uxId))
            assertTrue(
                Season2StationQaPolicy.shouldSkipAdvanceRoundInterRoundFeedback(
                    gameplayChapterId = chapterId,
                    season2UxStationId = uxId,
                    isLast = false,
                ),
            )
            assertTrue(Season2Ch1QaPolicy.isWhichWordStartsWithLayoutStation(chapterId, stationId))
            assertTrue(
                Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                    gameplayChapterId = chapterId,
                    season2UxStationId = uxId,
                ),
            )
        }
        assertNull(
            SixStationArcQaPolicy.earlyArcUxStationId(
                chapterId = 1,
                stationId = Chapter1StationOrder.TAP_LETTER,
            ),
        )
        assertFalse(SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(3, 5))
        assertEquals(
            99,
            SixStationArcQaPolicy.resolveUxStationIdForQa(
                chapterId = 1,
                stationId = Chapter1StationOrder.PICTURE_PICK_ALL,
                season2UxStationId = 99,
            ),
        )
        assertEquals(
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            SixStationArcQaPolicy.earlyArcUxStationId(
                chapterId = 2,
                stationId = 4,
            ),
        )
    }

    @Test
    fun sixStationArc_popBalloonsUxId_and_coachBalloonPolicies() {
        assertTrue(
            SixStationArcQaPolicy.isSagaPopBalloonsStation(
                chapterId = 1,
                stationId = Chapter1StationOrder.BALLOON_POP,
            ),
        )
        assertTrue(
            SixStationArcQaPolicy.isSagaPopBalloonsStation(
                chapterId = 2,
                stationId = 5,
            ),
        )
        assertEquals(
            Season2Chapter1StationOrder.POP_BALLOONS,
            SixStationArcQaPolicy.earlyArcUxStationId(
                chapterId = 1,
                stationId = Chapter1StationOrder.BALLOON_POP,
            ),
        )
        assertEquals(
            Season2Chapter1StationOrder.POP_BALLOONS,
            SixStationArcQaPolicy.earlyArcUxStationId(
                chapterId = 2,
                stationId = 5,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(
                Season2Chapter1StationOrder.POP_BALLOONS,
            ),
        )
        assertTrue(Season2Ch1QaPolicy.shouldPlayTryAgainInPopBalloonsSfx())
    }
}
