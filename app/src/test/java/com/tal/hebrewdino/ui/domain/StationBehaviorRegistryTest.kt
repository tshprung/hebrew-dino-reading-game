package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.REVEAL_THEN_CHOOSE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.screens.chapterResetRowIdsForTest
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
        assertEquals(StationTemplateId.FindLetterGrid, StationBehaviorRegistry.getStationUiSpec(6, 1).templateId)
        assertEquals(StationTemplateId.PopBalloons, StationBehaviorRegistry.getStationUiSpec(6, 2).templateId)
        assertEquals(StationTemplateId.PickLetter, StationBehaviorRegistry.getStationUiSpec(6, 3).templateId)
        assertEquals(StationTemplateId.PictureStartsWith, StationBehaviorRegistry.getStationUiSpec(6, 4).templateId)
        assertEquals(StationTemplateId.ImageMatch, StationBehaviorRegistry.getStationUiSpec(6, 5).templateId)
        val s6 = StationBehaviorRegistry.getStationUiSpec(6, 6)
        assertEquals(StationTemplateId.MatchLetterToWord, s6.templateId)
        assertTrue(s6.variants.contains(StationVariant.Finale))
    }

    @Test
    fun chapters_1_2_4_5_are_learning_chapters_target_visible_no_help() {
        for (chapterId in listOf(1, 2, 4, 5)) {
            val last = Chapter1Config.STATION_COUNT
            for (stationId in 1..last) {
                val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
                val plan = when (chapterId) {
                    1 -> StationQuizPlans.chapter1(stationId)
                    2 -> StationQuizPlans.chapter2(stationId)
                    4 -> StationQuizPlans.chapter4(stationId)
                    5 -> StationQuizPlans.chapter5(stationId)
                    else -> error("unexpected")
                }
                
                assertFalse("Chapter $chapterId station $stationId should not be listen-only", plan.listenOnlyTargetPrompt)
                assertFalse("Chapter $chapterId station $stationId should not have ListenFirst variant", spec.variants.contains(StationVariant.ListenFirst))
                assertFalse("Chapter $chapterId station $stationId should not have Episode4Help variant", spec.variants.contains(StationVariant.Episode4Help))
                assertFalse("Chapter $chapterId station $stationId should not have help controls enabled", spec.helpControlsEnabled)
                assertEquals("Chapter $chapterId station $stationId should have None replay mode", StationReplayMode.None, spec.replayMode)
                assertEquals("Chapter $chapterId station $stationId should have None hint mode", StationHintMode.None, spec.hintMode)
                
                // Specific visibility checks
                if (spec.templateId == StationTemplateId.ImageMatch) {
                    assertTrue("Chapter $chapterId station $stationId should show target letter chip", spec.imageMatchShowTargetLetterChip)
                }
                if (spec.templateId == StationTemplateId.MatchLetterToWord) {
                    assertTrue("Chapter $chapterId station $stationId should have readable panel", spec.matchLetterInstructionReadablePanel)
                    assertEquals(StationInstructionCopy.MatchLetterFinale, spec.matchLetterInstructionText)
                }
            }
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
    fun matchLetterToWord_template_is_used_for_all_finale_matching_stations() {
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(1, FINALE_PICTURE_LETTER_MATCH).templateId)
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(2, FINALE_PICTURE_LETTER_MATCH).templateId)
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(4, FINALE_PICTURE_LETTER_MATCH).templateId)
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(5, FINALE_PICTURE_LETTER_MATCH).templateId)
        val ch6 = StationBehaviorRegistry.getStationUiSpec(6, FINALE_PICTURE_LETTER_MATCH)
        assertEquals(StationTemplateId.MatchLetterToWord, ch6.templateId)
        assertTrue(ch6.variants.contains(StationVariant.Finale))
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(3, 2).templateId)
    }

    @Test
    fun matchLetterToWord_instructionPanel_is_enabled_for_all_matching_stations() {
        val ids =
            listOf(
                1 to FINALE_PICTURE_LETTER_MATCH,
                2 to FINALE_PICTURE_LETTER_MATCH,
                4 to FINALE_PICTURE_LETTER_MATCH,
                5 to FINALE_PICTURE_LETTER_MATCH,
                6 to FINALE_PICTURE_LETTER_MATCH,
                3 to 2,
            )
        for ((chapterId, stationId) in ids) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
            assertTrue("Ch$chapterId st$stationId should use readable instruction panel", spec.matchLetterInstructionReadablePanel)
            assertEquals(StationInstructionCopy.MatchLetterFinale, spec.matchLetterInstructionText)
        }
    }

    @Test
    fun chapter6_station2_maxBalloons_is_10() {
        val p2 = StationQuizPlans.chapter6(2)
        assertEquals("Ch6 st2 balloon count should be 10", 10, p2.optionCount)
    }

    @Test
    fun chapter6_station3_pickLetter_plan_is_5_options_and_no_help() {
        val plan = StationQuizPlans.chapter6(3)
        val spec = StationBehaviorRegistry.getStationUiSpec(6, 3)
        assertEquals(StationQuizMode.PickLetter, plan.mode)
        assertFalse(plan.listenOnlyTargetPrompt)
        assertEquals(5, plan.optionCount)
        assertFalse(spec.helpControlsEnabled)
    }

    @Test
    fun chapter3_special_variants_remain_unchanged() {
        // St 3: PopAll
        assertTrue(StationBehaviorRegistry.getStationUiSpec(3, 3).variants.contains(StationVariant.PopAllLettersInWord))
        // St 4: Highlighted
        assertTrue(StationQuizPlans.chapter3(4).highlightedLetterInWordPickLetter)
        // St 5: Audio Recognition
        assertTrue(StationQuizPlans.chapter3(5).chapter3AudioLetterRecognition)
        // St 6: ImageToWord
        assertEquals(StationTemplateId.ImageToWord, StationBehaviorRegistry.getStationUiSpec(3, 6).templateId)
    }

    @Test
    fun collected_eggs_are_chapters_1_3_5_only() {
        assertEquals(0, CollectedEggs.stripCount(beachOutroSeen = false, chapter3Completed = false, chapter5Completed = false))
        assertEquals(1, CollectedEggs.stripCount(beachOutroSeen = true, chapter3Completed = false, chapter5Completed = false))
        assertEquals(2, CollectedEggs.stripCount(beachOutroSeen = true, chapter3Completed = true, chapter5Completed = false))
        assertEquals(3, CollectedEggs.stripCount(beachOutroSeen = true, chapter3Completed = true, chapter5Completed = true))
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
    fun chapter5_station5_imageMatchShowsTargetLetterChip_learning() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(5, PICTURE_PICK_ALL).imageMatchShowTargetLetterChip)
    }

    @Test
    fun imageMatch_station5_is_unified_across_chapters_1_2_4_5_6() {
        val chapters = listOf(1, 2, 4, 5, 6)
        for (chapterId in chapters) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, PICTURE_PICK_ALL)
            assertEquals(StationTemplateId.ImageMatch, spec.templateId)
            assertEquals(
                "בחר את התמונה שמתחילה באות:",
                spec.imageMatchHeaderInstructionOverride,
            )
        }

        for (chapterId in listOf(1, 2, 4, 5)) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, PICTURE_PICK_ALL)
            assertTrue(spec.imageMatchShowTargetLetterChip)
            assertFalse(spec.helpControlsEnabled)
        }

        val ch6 = StationBehaviorRegistry.getStationUiSpec(6, PICTURE_PICK_ALL)
        assertTrue(ch6.imageMatchShowTargetLetterChip)
        assertFalse(ch6.helpControlsEnabled)
        assertEquals(StationReplayMode.None, ch6.replayMode)
        assertEquals(StationHintMode.None, ch6.hintMode)
    }

    @Test
    fun imageMatch_audioPrompt_is_whichWordStartsWithLetter() {
        assertEquals("audio/which_word_starts_with_letter.wav", AudioClips.WhichWordStartsWithLetter)
        assertFalse(AudioClips.WhichWordStartsWithLetter.contains("find_word_starts_with_letter"))
    }

    @Test
    fun chapter5_station1_LearningPickLetter_noPinnedShortcut_noSeparateListenPanel() {
        val spec = StationBehaviorRegistry.getStationUiSpec(5, TAP_LETTER)
        assertFalse(spec.pickLetterListenOnlyHebrewPanel)
        assertEquals(StationInstructionCopy.PickLetterSagaStation1Preamble, spec.pickLetterInstructionOverride)
        assertNull(spec.pickLetterSagaStation1CompactPreamble)
        assertTrue(spec.pickLetterAllowPinnedCorrectShortcut)
    }

    @Test
    fun learning_chapters_station1_pickLetter_shows_chooseLetter_header_and_noHelp() {
        for (chapterId in listOf(1, 2, 4, 5)) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, TAP_LETTER)
            assertEquals(StationTemplateId.PickLetter, spec.templateId)
            assertFalse(spec.helpControlsEnabled)
            assertEquals(StationInstructionCopy.PickLetterSagaStation1Preamble, spec.pickLetterInstructionOverride)
            assertNull(spec.pickLetterSagaStation1CompactPreamble)
        }
    }

    @Test
    fun chapter3_station5_audioRecognition_hasHelpReplayAndHint_and_hidesTarget() {
        val spec = StationBehaviorRegistry.getStationUiSpec(3, 5)
        val plan = StationQuizPlans.chapter3(5)
        assertEquals(StationTemplateId.PickLetter, spec.templateId)
        assertTrue(spec.variants.contains(StationVariant.Chapter3AudioLetterRecognition))
        assertTrue(spec.variants.contains(StationVariant.Episode4Help))
        assertTrue(spec.helpControlsEnabled)
        assertEquals(StationReplayMode.TargetLetterOnly, spec.replayMode)
        assertEquals(StationHintMode.TemporaryTargetLetter, spec.hintMode)
        assertEquals(StationInstructionCopy.PickLetterSagaStation1Preamble, spec.pickLetterInstructionOverride)
        assertTrue(plan.listenOnlyTargetPrompt)
        assertEquals(6, plan.optionCount)
    }

    @Test
    fun learning_chapters_station1_pickLetter_plan_is_5_options_and_no_help() {
        for (chapterId in listOf(1, 2, 4, 5)) {
            val plan =
                when (chapterId) {
                    1 -> StationQuizPlans.chapter1(TAP_LETTER)
                    2 -> StationQuizPlans.chapter2(TAP_LETTER)
                    4 -> StationQuizPlans.chapter4(TAP_LETTER)
                    5 -> StationQuizPlans.chapter5(TAP_LETTER)
                    else -> error("unexpected")
                }
            assertEquals(StationQuizMode.PickLetter, plan.mode)
            assertFalse(plan.listenOnlyTargetPrompt)
            assertEquals(5, plan.optionCount)
        }
    }

    @Test
    fun pickLetter_station1_equivalents_use_chooseLetter_copy() {
        for (chapterId in listOf(1, 2, 4, 5)) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, TAP_LETTER)
            assertEquals(StationInstructionCopy.PickLetterSagaStation1Preamble, spec.pickLetterInstructionOverride)
        }
        assertEquals(
            StationInstructionCopy.PickLetterSagaStation1Preamble,
            StationBehaviorRegistry.getStationUiSpec(3, 5).pickLetterInstructionOverride,
        )
        assertEquals(
            StationInstructionCopy.PickLetterSagaStation1Preamble,
            StationBehaviorRegistry.getStationUiSpec(6, 3).pickLetterInstructionOverride,
        )
    }

    @Test
    fun hebrewLetterOrder_sortsAlphabetically() {
        assertEquals(listOf("א", "ב", "ת"), HebrewLetterOrder.sortForDisplay(listOf("ת", "א", "ב")))
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
    fun episode4_station2_balloonSpec_unified() {
        val s = StationBehaviorRegistry.getStationUiSpec(4, BALLOON_POP)
        assertEquals("פוצץ את הבלונים עם האות:", s.balloonInstructionOverride)
        assertFalse(s.useEpisode4BalloonInstructionPanel)
        assertEquals(0f, s.balloonPlayAreaStartInsetDp, 0.001f)
        assertFalse(s.excludeFullScreenBalloonHintOverlay)
        assertFalse(s.helpControlsEnabled)
    }

    @Test
    fun chapter6_station5_imageMatch_has_instruction_and_shows_target_chip() {
        val spec = StationBehaviorRegistry.getStationUiSpec(6, PICTURE_PICK_ALL)
        val plan = StationQuizPlans.chapter6(PICTURE_PICK_ALL)
        assertEquals(StationTemplateId.ImageMatch, spec.templateId)
        assertFalse(plan.listenOnlyTargetPrompt)
        assertTrue(spec.imageMatchShowTargetLetterChip)
        assertFalse(spec.helpControlsEnabled)
        assertEquals(StationReplayMode.None, spec.replayMode)
        assertEquals(StationHintMode.None, spec.hintMode)
        assertEquals(StationInstructionCopy.ImageMatchFindWordStartingWithLetter, spec.imageMatchHeaderInstructionOverride)
    }

    @Test
    fun chapter6_station4_pictureStartsWith_hasReadablePanel_and_safeInset() {
        val spec = StationBehaviorRegistry.getStationUiSpec(6, PICTURE_PICK_ONE)
        assertEquals(StationTemplateId.PictureStartsWith, spec.templateId)
        assertEquals("באיזו אות מתחילה המילה:", spec.pictureStartsWithInstructionOverride)
        assertTrue(spec.pictureStartsWithReadablePanel)
    }

    @Test
    fun pictureStartsWithOrderedLetters_usesHebrewOrder_when_enabled() {
        assertEquals(
            listOf("א", "ב", "ת"),
            com.tal.hebrewdino.ui.game.pictureStartsWithOrderedLetters(
                sortOptionLetters = true,
                optionLetters = listOf("ת", "א", "ב"),
            ),
        )
    }

    @Test
    fun pictureStartsWith_plans_doNotRequest_moreThan6_options() {
        val ch1 = StationQuizPlans.chapter1(Chapter1StationOrder.PICTURE_PICK_ONE)
        val ch2 = StationQuizPlans.chapter2(Chapter1StationOrder.PICTURE_PICK_ONE)
        val ch3 = StationQuizPlans.chapter3(1)
        val ch4 = StationQuizPlans.chapter4(Chapter1StationOrder.PICTURE_PICK_ONE)
        val ch5 = StationQuizPlans.chapter5(Chapter1StationOrder.PICTURE_PICK_ONE)
        val ch6 = StationQuizPlans.chapter6(4)
        assertTrue((ch1.optionCount ?: 0) <= 6)
        assertTrue((ch2.optionCount ?: 0) <= 6)
        assertTrue((ch3.optionCount ?: 0) <= 6)
        assertTrue((ch4.optionCount ?: 0) <= 6)
        assertTrue((ch5.optionCount ?: 0) <= 6)
        assertTrue((ch6.optionCount ?: 0) <= 6)
    }

    @Test
    fun settings_resetChapters_list_includes_chapter6() {
        assertTrue(6 in chapterResetRowIdsForTest())
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
        assertTrue(StationQuizPlans.chapter3(TAP_LETTER).sortOptionLetters)
    }

    @Test
    fun chapter5_station2_balloonSpec_unified() {
        val s = StationBehaviorRegistry.getStationUiSpec(5, BALLOON_POP)
        assertFalse(s.useEpisode4BalloonInstructionPanel)
        assertEquals(0f, s.balloonPlayAreaStartInsetDp, 0.001f)
        assertFalse(s.excludeFullScreenBalloonHintOverlay)
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
