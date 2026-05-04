package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.REVEAL_THEN_CHOOSE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StationBehaviorRegistryTest {

    @Test
    fun everyChapterStationPair_hasSpecAlignedWithQuizPlan() {
        for (chapterId in 1..5) {
            val last =
                when (chapterId) {
                    1 -> Chapter1Config.STATION_COUNT
                    2 -> Chapter2Config.STATION_COUNT
                    3 -> Chapter3Config.STATION_COUNT
                    4 -> Chapter4Config.STATION_COUNT
                    5 -> Chapter5Config.STATION_COUNT
                    else -> error("unexpected")
                }
            for (stationId in 1..last) {
                val plan =
                    when (chapterId) {
                        1 -> StationQuizPlans.chapter1(stationId)
                        2 -> StationQuizPlans.chapter2(stationId)
                        3 -> StationQuizPlans.chapter3(stationId)
                        4 -> StationQuizPlans.chapter4(stationId)
                        5 -> StationQuizPlans.chapter5(stationId)
                        else -> error("unexpected")
                    }
                val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
                assertEquals(chapterId, spec.chapterId)
                assertEquals(stationId, spec.stationId)
                assertEquals(plan.mode, spec.quizMode)
                assertEquals(plan.findLetterGridMaxTargetCount, spec.findGridMaxTargetCount)
            }
        }
    }

    @Test
    fun episode4_st1_to_5_helpControlsEnabled_true_st6_false() {
        for (s in 1..5) {
            assertTrue(StationBehaviorRegistry.getStationUiSpec(4, s).helpControlsEnabled)
        }
        assertFalse(StationBehaviorRegistry.getStationUiSpec(4, 6).helpControlsEnabled)
    }

    @Test
    fun episode4_replayModes() {
        assertEquals(StationReplayMode.TargetLetterOnly, StationBehaviorRegistry.getStationUiSpec(4, 1).replayMode)
        assertEquals(StationReplayMode.TargetLetterOnly, StationBehaviorRegistry.getStationUiSpec(4, 2).replayMode)
        assertEquals(StationReplayMode.TargetLetterOnly, StationBehaviorRegistry.getStationUiSpec(4, 3).replayMode)
        assertEquals(StationReplayMode.TargetWordOnly, StationBehaviorRegistry.getStationUiSpec(4, 4).replayMode)
        assertEquals(StationReplayMode.TargetLetterOnly, StationBehaviorRegistry.getStationUiSpec(4, 5).replayMode)
    }

    @Test
    fun episode4_station3_findGridMaxTargetCount_atMost4() {
        val n =
            StationBehaviorRegistry.getStationUiSpec(4, REVEAL_THEN_CHOOSE).findGridMaxTargetCount
        assertNotNull(n)
        assertTrue(n!! <= 4)
    }

    @Test
    fun episode4_stations456_instructionPanelsEnabled() {
        val s4 = StationBehaviorRegistry.getStationUiSpec(4, PICTURE_PICK_ONE)
        assertTrue(s4.pictureStartsWithReadablePanel)
        val s5 = StationBehaviorRegistry.getStationUiSpec(4, PICTURE_PICK_ALL)
        assertTrue(s5.imageMatchHeaderReadablePanel)
        val s6 = StationBehaviorRegistry.getStationUiSpec(4, FINALE_PICTURE_LETTER_MATCH)
        assertTrue(s6.matchLetterInstructionReadablePanel)
    }

    @Test
    fun chapter5_doesNotInheritEpisode4HelpControls() {
        for (s in 1..Chapter5Config.STATION_COUNT) {
            assertFalse(StationBehaviorRegistry.getStationUiSpec(5, s).helpControlsEnabled)
        }
    }

    @Test
    fun chapter3_station1_pictureReadablePanel_true() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(3, 1).pictureStartsWithReadablePanel)
    }

    @Test
    fun chapter3_station2_matchLetterReadablePanel_true() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(3, 2).matchLetterInstructionReadablePanel)
    }

    @Test
    fun chapter3_station5_listenFirstPickLetterPanel_true() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(3, 5).pickLetterListenOnlyHebrewPanel)
    }

    @Test
    fun chapter3_station6_imageMatchHeaderOverride_notUsedByGameScreen_path() {
        assertNull(StationBehaviorRegistry.getStationUiSpec(3, 6).imageMatchHeaderInstructionOverride)
    }

    @Test
    fun chapter1_station5_imageMatchShowsTargetLetterChip() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(1, PICTURE_PICK_ALL).imageMatchShowTargetLetterChip)
    }

    @Test
    fun chapter5_station5_imageMatchHidesTargetLetterChip() {
        assertFalse(StationBehaviorRegistry.getStationUiSpec(5, PICTURE_PICK_ALL).imageMatchShowTargetLetterChip)
    }

    @Test
    fun chapter5_station1_listenOnlyPickLetterShortcutDisabled() {
        val spec = StationBehaviorRegistry.getStationUiSpec(5, TAP_LETTER)
        assertTrue(spec.pickLetterListenOnlyHebrewPanel)
        assertFalse(spec.pickLetterAllowPinnedCorrectShortcut)
    }

    @Test
    fun chapter1_station2_balloonInstructionOverride_usesNonListenString() {
        assertEquals(
            "פוצץ את הבלונים עם האות:",
            StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP).balloonInstructionOverride,
        )
    }

    @Test
    fun chapter2_station2_balloonInstructionOverride_usesNonListenString() {
        assertEquals(
            "פוצץ את הבלונים עם האות:",
            StationBehaviorRegistry.getStationUiSpec(2, BALLOON_POP).balloonInstructionOverride,
        )
    }

    @Test
    fun chapter5_station2_balloonInstructionOverride_usesListenOnlyString() {
        assertEquals(
            "פוצץ את הבלונים של האות שנשמעה:",
            StationBehaviorRegistry.getStationUiSpec(5, BALLOON_POP).balloonInstructionOverride,
        )
    }

    @Test
    fun episode4_station2_balloonSpec_unchanged() {
        val s = StationBehaviorRegistry.getStationUiSpec(4, BALLOON_POP)
        assertEquals("פוצץ את הבלונים עם האות:", s.balloonInstructionOverride)
        assertTrue(s.useEpisode4BalloonInstructionPanel)
        assertEquals(96f, s.balloonPlayAreaStartInsetDp, 0.001f)
        assertTrue(s.excludeFullScreenBalloonHintOverlay)
        assertTrue(s.helpControlsEnabled)
    }

    @Test
    fun chapter3_station3_balloonInstructionOverride_staysNull() {
        assertNull(StationBehaviorRegistry.getStationUiSpec(3, 3).balloonInstructionOverride)
    }
}
