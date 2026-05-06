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
        for (chapterId in 1..6) {
            val last =
                when (chapterId) {
                    1 -> Chapter1Config.STATION_COUNT
                    2 -> Chapter2Config.STATION_COUNT
                    3 -> Chapter3Config.STATION_COUNT
                    4 -> Chapter4Config.STATION_COUNT
                    5 -> Chapter5Config.STATION_COUNT
                    6 -> Chapter6Config.STATION_COUNT
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
                        6 -> StationQuizPlans.chapter6(stationId)
                        else -> error("unexpected")
                    }
                val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
                assertEquals(chapterId, spec.chapterId)
                assertEquals(stationId, spec.stationId)
                assertEquals(plan.mode, spec.quizMode)
                assertEquals(plan.findLetterGridMaxTargetCount, spec.findGridMaxTargetCount)
                assertTrue("templateId must be present", spec.templateId.name.isNotBlank())
                assertTrue("variants must include Standard", spec.variants.contains(StationVariant.Standard))
            }
        }
    }

    @Test
    fun sixStationArcTemplates_match_order_for_chapters_1_2_4_5() {
        fun assertOrder(chapterId: Int) {
            assertEquals(StationTemplateId.PickLetter, StationBehaviorRegistry.getStationUiSpec(chapterId, 1).templateId)
            assertEquals(StationTemplateId.PopBalloons, StationBehaviorRegistry.getStationUiSpec(chapterId, 2).templateId)
            assertEquals(StationTemplateId.FindLetterGrid, StationBehaviorRegistry.getStationUiSpec(chapterId, 3).templateId)
            assertEquals(StationTemplateId.PictureStartsWith, StationBehaviorRegistry.getStationUiSpec(chapterId, 4).templateId)
            assertEquals(StationTemplateId.ImageMatch, StationBehaviorRegistry.getStationUiSpec(chapterId, 5).templateId)
            assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(chapterId, 6).templateId)
        }
        assertOrder(1)
        assertOrder(2)
        assertOrder(4)
        assertOrder(5)
    }

    @Test
    fun chapter3_templates_and_variants_are_explicit() {
        assertEquals(StationTemplateId.PictureStartsWith, StationBehaviorRegistry.getStationUiSpec(3, 1).templateId)
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(3, 2).templateId)
        val s3 = StationBehaviorRegistry.getStationUiSpec(3, 3)
        assertEquals(StationTemplateId.PopBalloons, s3.templateId)
        assertTrue(s3.variants.contains(StationVariant.PopAllLettersInWord))
        val s4 = StationBehaviorRegistry.getStationUiSpec(3, 4)
        assertEquals(StationTemplateId.PickLetter, s4.templateId)
        assertTrue(s4.variants.contains(StationVariant.HighlightedLetterInWord))
        val s5 = StationBehaviorRegistry.getStationUiSpec(3, 5)
        assertEquals(StationTemplateId.PickLetter, s5.templateId)
        assertTrue(s5.variants.contains(StationVariant.Chapter3AudioLetterRecognition))
        val s6 = StationBehaviorRegistry.getStationUiSpec(3, 6)
        assertEquals(StationTemplateId.ImageToWord, s6.templateId)
        assertTrue(s6.variants.contains(StationVariant.Chapter3ImageToWord))
    }

    @Test
    fun chapter6_templates_and_variants_are_explicit() {
        val s1 = StationBehaviorRegistry.getStationUiSpec(6, 1)
        assertEquals(StationTemplateId.PickLetter, s1.templateId)
        assertTrue(s1.variants.contains(StationVariant.ListenFirst))
        assertTrue(s1.variants.contains(StationVariant.Episode4Help))
        assertTrue(s1.helpControlsEnabled)
        assertEquals(StationReplayMode.TargetLetterOnly, s1.replayMode)
        assertEquals(StationHintMode.TemporaryTargetLetter, s1.hintMode)
        assertEquals(2100L, s1.hintDurationMs)
        assertEquals("בחר את האות:", s1.pickLetterInstructionOverride)
        val p1 = StationQuizPlans.chapter6(1)
        assertTrue(p1.listenOnlyTargetPrompt)
        assertEquals(6, p1.pickLetterOptionCount)

        val s2 = StationBehaviorRegistry.getStationUiSpec(6, 2)
        assertEquals(StationTemplateId.PickLetter, s2.templateId)
        assertTrue(s2.variants.contains(StationVariant.HighlightedLetterInWord))
        val p2 = StationQuizPlans.chapter6(2)
        assertTrue((p2.pickLetterOptionCount ?: 99) <= 8)
        val s3 = StationBehaviorRegistry.getStationUiSpec(6, 3)
        assertEquals(StationTemplateId.PopBalloons, s3.templateId)
        assertTrue(s3.variants.contains(StationVariant.PopAllLettersInWord))
        assertEquals(StationInstructionCopy.PopBalloonsPopAllLettersInWord, s3.popBalloonsPopAllLettersBannerInstruction)
        assertEquals(StationTemplateId.PictureStartsWith, StationBehaviorRegistry.getStationUiSpec(6, 4).templateId)
        assertEquals(StationTemplateId.ImageMatch, StationBehaviorRegistry.getStationUiSpec(6, 5).templateId)
        val s6 = StationBehaviorRegistry.getStationUiSpec(6, 6)
        assertEquals(StationTemplateId.MatchLetterToWord, s6.templateId)
        assertTrue(s6.variants.contains(StationVariant.Finale))
    }

    @Test
    fun episode4_helpVariant_present_for_st1_to_5_absent_for_st6() {
        for (s in 1..5) {
            assertTrue(StationBehaviorRegistry.getStationUiSpec(4, s).variants.contains(StationVariant.Episode4Help))
        }
        assertFalse(StationBehaviorRegistry.getStationUiSpec(4, 6).variants.contains(StationVariant.Episode4Help))
    }

    @Test
    fun chapter5_helpVariant_present_for_st1_to_5_absent_for_st6() {
        for (s in 1..5) {
            assertTrue(StationBehaviorRegistry.getStationUiSpec(5, s).variants.contains(StationVariant.Episode4Help))
        }
        assertFalse(StationBehaviorRegistry.getStationUiSpec(5, 6).variants.contains(StationVariant.Episode4Help))
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
    fun chapter5_st1_to_5_helpControlsEnabled_true_st6_false() {
        for (s in 1..5) {
            assertTrue(StationBehaviorRegistry.getStationUiSpec(5, s).helpControlsEnabled)
        }
        assertFalse(StationBehaviorRegistry.getStationUiSpec(5, 6).helpControlsEnabled)
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
    fun chapter3_station5_usesInstructionOverride_and_noInternalListenPanel() {
        val s = StationBehaviorRegistry.getStationUiSpec(3, 5)
        assertFalse(s.pickLetterListenOnlyHebrewPanel)
        assertEquals(StationInstructionCopy.PickLetterListenOnly, s.pickLetterInstructionOverride)
        assertTrue(s.variants.contains(StationVariant.Episode4Help))
        assertTrue(s.helpControlsEnabled)
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
    fun chapter5_station1_listenOnlyPickLetter_noPinnedShortcut_noSeparateListenPanel() {
        val spec = StationBehaviorRegistry.getStationUiSpec(5, TAP_LETTER)
        assertFalse(spec.pickLetterListenOnlyHebrewPanel)
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
    fun chapter5_station2_balloonInstructionOverride_matches_episode4_station2() {
        assertEquals(
            "פוצץ את הבלונים עם האות:",
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

    @Test
    fun chapter3_station3_popAllBanner_and_skipHeaderBlock() {
        val s = StationBehaviorRegistry.getStationUiSpec(3, 3)
        assertTrue(s.popBalloonsSkipInstructionHeaderBlock)
        assertEquals(
            StationInstructionCopy.PopBalloonsPopAllLettersInWord,
            s.popBalloonsPopAllLettersBannerInstruction,
        )
    }

    @Test
    fun chapter3_station4_highlightedPickLetterInstruction_present() {
        assertEquals(
            StationInstructionCopy.PickLetterHighlightedInWord,
            StationBehaviorRegistry.getStationUiSpec(3, PICTURE_PICK_ONE).pickLetterHighlightedInWordInstruction,
        )
    }

    @Test
    fun chapter3_station6_imageToWordInstruction_present() {
        assertEquals(
            StationInstructionCopy.Chapter3ImageToWord,
            StationBehaviorRegistry.getStationUiSpec(3, 6).imageToWordInstructionText,
        )
    }

    @Test
    fun chapter3_station1_pictureStartsWith_sortOptionLetters_inPlan() {
        assertTrue(StationQuizPlans.chapter3(TAP_LETTER).sortPictureStartsWithOptionLetters)
    }

    @Test
    fun chapter5_station2_uses_episode4BalloonInstructionPanel_for_readability_and_inset() {
        val s = StationBehaviorRegistry.getStationUiSpec(5, BALLOON_POP)
        assertTrue(s.useEpisode4BalloonInstructionPanel)
        assertEquals(96f, s.balloonPlayAreaStartInsetDp, 0.001f)
        assertTrue(s.excludeFullScreenBalloonHintOverlay)
    }

    @Test
    fun episode4_finale_matchLetterInstructionText_present() {
        assertEquals(
            StationInstructionCopy.MatchLetterFinale,
            StationBehaviorRegistry.getStationUiSpec(4, FINALE_PICTURE_LETTER_MATCH).matchLetterInstructionText,
        )
    }

    @Test
    fun chapter5_station3_findGrid_listenOnly_whiteRoundedPanel() {
        val spec = StationBehaviorRegistry.getStationUiSpec(5, REVEAL_THEN_CHOOSE)
        assertEquals(InstructionPanelStyle.WhiteRounded, spec.findGridInlineInstructionPanelStyle)
    }
}
