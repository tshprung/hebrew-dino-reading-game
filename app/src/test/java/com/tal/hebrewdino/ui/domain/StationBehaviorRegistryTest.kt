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
    private fun expectedMatchLetterInstructionText(
        chapterId: Int,
        stationId: Int,
    ): String {
        return if ((chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
            stationId == FINALE_PICTURE_LETTER_MATCH
        ) {
            "התאימו כל אות למילה שמתחילה בה"
        } else if ((chapterId == 3 || chapterId == 6) && stationId == 2) {
            "התאימו כל אות למילה שמתחילה בה"
        } else {
            StationInstructionCopy.MatchLetterFinale
        }
    }

    private fun StationUiSpec.layoutComparable(): StationUiSpec =
        copy(
            chapterId = 0,
            stationId = 0,
            audioStagingPickLetter = false,
            audioStagingPopBalloons = false,
            audioStagingFindGrid = false,
            popBalloonsUseSoundPoolPrompt = false,
            riskNotes = "",
        )

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
    fun learningArc_templateOrder_byChapter() {
        val expected =
            mapOf(
                1 to
                    listOf(
                        StationTemplateId.PickLetter,
                        StationTemplateId.PopBalloons,
                        StationTemplateId.FindLetterGrid,
                        StationTemplateId.PictureStartsWith,
                        StationTemplateId.ImageMatch,
                        StationTemplateId.MatchLetterToWord,
                    ),
                2 to
                    listOf(
                        StationTemplateId.PickLetter,
                        StationTemplateId.FindLetterGrid,
                        StationTemplateId.PictureStartsWith,
                        StationTemplateId.ImageMatch,
                        StationTemplateId.PopBalloons,
                        StationTemplateId.MatchLetterToWord,
                    ),
                4 to
                    listOf(
                        StationTemplateId.PickLetter,
                        StationTemplateId.PopBalloons,
                        StationTemplateId.FindLetterGrid,
                        StationTemplateId.PictureStartsWith,
                        StationTemplateId.ImageMatch,
                        StationTemplateId.MatchLetterToWord,
                    ),
                5 to
                    listOf(
                        StationTemplateId.PickLetter,
                        StationTemplateId.DragMissingLetter,
                        StationTemplateId.FindLetterGrid,
                        StationTemplateId.PictureStartsWith,
                        StationTemplateId.ImageMatch,
                        StationTemplateId.MatchLetterToWord,
                    ),
            )
        for ((chapterId, templates) in expected) {
            templates.forEachIndexed { index, template ->
                assertEquals(template, StationBehaviorRegistry.getStationUiSpec(chapterId, index + 1).templateId)
            }
        }
    }

    @Test
    fun chapter3_templates_and_variants_are_explicit() {
        assertEquals(StationTemplateId.PictureStartsWith, StationBehaviorRegistry.getStationUiSpec(3, 1).templateId)
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(3, 2).templateId)
        val s3 = StationBehaviorRegistry.getStationUiSpec(3, 3)
        assertEquals(StationTemplateId.DragWordToPicture, s3.templateId)
        val s4 = StationBehaviorRegistry.getStationUiSpec(3, 4)
        assertEquals(StationTemplateId.PickLetter, s4.templateId)
        assertTrue(s4.variants.contains(StationVariant.HighlightedLetterInWord))
        assertEquals(StationHintMode.TemporaryTargetLetter, s4.hintMode)
        assertEquals(StationInstructionCopy.PickLetterHighlightedInWord, s4.pickLetterHighlightedInWordInstruction)
        val s5 = StationBehaviorRegistry.getStationUiSpec(3, 5)
        assertEquals(StationTemplateId.PickLetter, s5.templateId)
        assertTrue(s5.variants.contains(StationVariant.Chapter3AudioLetterRecognition))
        val s6 = StationBehaviorRegistry.getStationUiSpec(3, 6)
        assertEquals(StationTemplateId.ImageToWord, s6.templateId)
        assertTrue(s6.variants.contains(StationVariant.Chapter3ImageToWord))
        assertEquals(StationInstructionCopy.Chapter3ImageToWord, s6.imageToWordInstructionText)
        assertNull(s6.imageMatchHeaderInstructionOverride)
        assertTrue(StationQuizPlans.chapter3(4).highlightedLetterInWordPickLetter)
        assertTrue(StationQuizPlans.chapter3(5).chapter3AudioLetterRecognition)
        assertTrue(StationQuizPlans.chapter3(TAP_LETTER).sortOptionLetters)
        assertTrue(StationBehaviorRegistry.getStationUiSpec(3, 1).pictureStartsWithReadablePanel)
        assertTrue(StationBehaviorRegistry.getStationUiSpec(3, 2).matchLetterInstructionReadablePanel)
    }

    @Test
    fun chapter6_templates_and_variants_are_explicit() {
        assertEquals(StationTemplateId.PictureStartsWith, StationBehaviorRegistry.getStationUiSpec(6, 1).templateId)
        assertEquals(StationTemplateId.MatchLetterToWord, StationBehaviorRegistry.getStationUiSpec(6, 2).templateId)
        val s3 = StationBehaviorRegistry.getStationUiSpec(6, 3)
        assertEquals(StationTemplateId.DragWordToPicture, s3.templateId)
        val s4 = StationBehaviorRegistry.getStationUiSpec(6, 4)
        assertEquals(StationTemplateId.DragMissingLetter, s4.templateId)
        val s5 = StationBehaviorRegistry.getStationUiSpec(6, 5)
        assertEquals(StationTemplateId.PickLetter, s5.templateId)
        assertTrue(s5.variants.contains(StationVariant.Chapter3AudioLetterRecognition))
        assertTrue(s5.variants.contains(StationVariant.HelpColumn))
        assertTrue(s5.helpControlsEnabled)
        assertEquals(StationReplayMode.TargetLetterOnly, s5.replayMode)
        assertEquals(StationHintMode.TemporaryTargetLetter, s5.hintMode)
        val s6 = StationBehaviorRegistry.getStationUiSpec(6, 6)
        assertEquals(StationTemplateId.ImageToWord, s6.templateId)
        assertTrue(s6.variants.contains(StationVariant.Chapter3ImageToWord))
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
                assertFalse("Chapter $chapterId station $stationId should not have HelpColumn variant", spec.variants.contains(StationVariant.HelpColumn))
                assertFalse("Chapter $chapterId station $stationId should not have help controls enabled", spec.helpControlsEnabled)
                if (spec.templateId == StationTemplateId.DragWordToPicture ||
                    spec.templateId == StationTemplateId.DragMissingLetter
                ) {
                    assertEquals(StationReplayMode.TargetWordOnly, spec.replayMode)
                    assertEquals(StationHintMode.None, spec.hintMode)
                } else {
                    assertEquals("Chapter $chapterId station $stationId should have None replay mode", StationReplayMode.None, spec.replayMode)
                    assertEquals("Chapter $chapterId station $stationId should have None hint mode", StationHintMode.None, spec.hintMode)
                }
                
                // Specific visibility checks
                if (spec.templateId == StationTemplateId.ImageMatch) {
                    assertTrue("Chapter $chapterId station $stationId should show target letter chip", spec.imageMatchShowTargetLetterChip)
                }
                if (spec.templateId == StationTemplateId.MatchLetterToWord) {
                    assertTrue("Chapter $chapterId station $stationId should have readable panel", spec.matchLetterInstructionReadablePanel)
                    assertEquals(expectedMatchLetterInstructionText(chapterId, stationId), spec.matchLetterInstructionText)
                }
            }
        }
    }

    @Test
    fun matchLetterToWord_instructionPanel_is_enabled_for_all_matching_stations() {
        val ids =
            listOf(
                1 to FINALE_PICTURE_LETTER_MATCH,
                2 to FINALE_PICTURE_LETTER_MATCH,
                4 to FINALE_PICTURE_LETTER_MATCH,
                5 to FINALE_PICTURE_LETTER_MATCH,
                3 to 2,
                6 to 2,
            )
        for ((chapterId, stationId) in ids) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
            assertEquals(StationTemplateId.MatchLetterToWord, spec.templateId)
            assertTrue("Ch$chapterId st$stationId should use readable instruction panel", spec.matchLetterInstructionReadablePanel)
            assertEquals(expectedMatchLetterInstructionText(chapterId, stationId), spec.matchLetterInstructionText)
        }
    }

    @Test
    fun chapter6_station3_dragWordToPicture_matchesChapter3() {
        val p3 = StationQuizPlans.chapter6(3)
        assertEquals(StationQuizMode.DragWordToPicture, p3.mode)
        assertEquals(5, p3.questionCount)
        assertEquals(3, p3.dragWordToPicturePairCount)
        val ch3Spec = StationBehaviorRegistry.getStationUiSpec(3, 3)
        val ch6Spec = StationBehaviorRegistry.getStationUiSpec(6, 3)
        assertEquals(ch3Spec.layoutComparable(), ch6Spec.layoutComparable())
    }

    @Test
    fun chapter6_station4_dragMissingLetter_firstLetterOnly() {
        val plan = StationQuizPlans.chapter6(4)
        val spec = StationBehaviorRegistry.getStationUiSpec(6, 4)
        assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
        assertEquals(0, plan.dragMissingLetterIndex)
        assertEquals(StationTemplateId.DragMissingLetter, spec.templateId)
    }

    @Test
    fun chapter6_station_layout_matches_chapter3_except_station4() {
        // Ch3 st3 has dedicated drag-word layout polish; Ch3 st4 differs from Ch6 st4 by design.
        for (stationId in listOf(1, 2, 3, 5, 6)) {
            val ch3 = StationBehaviorRegistry.getStationUiSpec(3, stationId).layoutComparable()
            val ch6 = StationBehaviorRegistry.getStationUiSpec(6, stationId).layoutComparable()
            assertEquals("Station $stationId layout must match between ch3 and ch6", ch3, ch6)
        }
    }

    @Test
    fun collected_eggs_are_chapters_1_3_5_only() {
        assertEquals(0, CollectedEggs.stripCount(beachOutroSeen = false, chapter3Completed = false, chapter5Completed = false))
        assertEquals(1, CollectedEggs.stripCount(beachOutroSeen = true, chapter3Completed = false, chapter5Completed = false))
        assertEquals(2, CollectedEggs.stripCount(beachOutroSeen = true, chapter3Completed = true, chapter5Completed = false))
        assertEquals(3, CollectedEggs.stripCount(beachOutroSeen = true, chapter3Completed = true, chapter5Completed = true))
    }

    @Test
    fun imageMatch_learning_midArc_is_unified_across_chapters_1_2_4_5() {
        val stations = listOf(1 to PICTURE_PICK_ALL, 2 to 4, 4 to PICTURE_PICK_ALL, 5 to 5)
        for ((chapterId, stationId) in stations) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
            assertEquals(StationTemplateId.ImageMatch, spec.templateId)
            assertEquals(
                "בחר את התמונה שמתחילה באות:",
                spec.imageMatchHeaderInstructionOverride,
            )
            assertTrue(spec.imageMatchShowTargetLetterChip)
            assertFalse(spec.helpControlsEnabled)
        }
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
    fun chapter3_station5_audioRecognition_hasHelpReplayAndHint_and_hidesTarget() {
        val spec = StationBehaviorRegistry.getStationUiSpec(3, 5)
        val plan = StationQuizPlans.chapter3(5)
        assertEquals(StationTemplateId.PickLetter, spec.templateId)
        assertTrue(spec.variants.contains(StationVariant.Chapter3AudioLetterRecognition))
        assertTrue(spec.variants.contains(StationVariant.HelpColumn))
        assertTrue(spec.helpControlsEnabled)
        assertEquals(StationReplayMode.TargetLetterOnly, spec.replayMode)
        assertEquals(StationHintMode.TemporaryTargetLetter, spec.hintMode)
        assertEquals(StationInstructionCopy.PickLetterSagaStation1Preamble, spec.pickLetterInstructionOverride)
        assertTrue(plan.listenOnlyTargetPrompt)
        assertEquals(6, plan.optionCount)
    }

    @Test
    fun pickLetter_station1_equivalents_use_chooseLetter_copy_and_fiveOptions() {
        for (chapterId in listOf(1, 2, 4, 5)) {
            val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, TAP_LETTER)
            val plan =
                when (chapterId) {
                    1 -> StationQuizPlans.chapter1(TAP_LETTER)
                    2 -> StationQuizPlans.chapter2(TAP_LETTER)
                    4 -> StationQuizPlans.chapter4(TAP_LETTER)
                    5 -> StationQuizPlans.chapter5(TAP_LETTER)
                    else -> error("unexpected")
                }
            assertEquals(StationTemplateId.PickLetter, spec.templateId)
            assertFalse(spec.helpControlsEnabled)
            assertEquals(StationInstructionCopy.PickLetterSagaStation1Preamble, spec.pickLetterInstructionOverride)
            assertNull(spec.pickLetterSagaStation1CompactPreamble)
            assertEquals(5, plan.optionCount)
            assertFalse(plan.listenOnlyTargetPrompt)
        }
        for (chapterId in listOf(3, 6)) {
            assertEquals(
                StationInstructionCopy.PickLetterSagaStation1Preamble,
                StationBehaviorRegistry.getStationUiSpec(chapterId, 5).pickLetterInstructionOverride,
            )
        }
    }

    @Test
    fun hebrewLetterOrder_sortsAlphabetically() {
        assertEquals(listOf("א", "ב", "ת"), HebrewLetterOrder.sortForDisplay(listOf("ת", "א", "ב")))
    }

    @Test
    fun balloonInstructionOverride_learningChapters() {
        val expected = "פוצץ את הבלונים עם האות:"
        assertEquals(expected, StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP).balloonInstructionOverride)
        assertEquals(expected, StationBehaviorRegistry.getStationUiSpec(2, 5).balloonInstructionOverride)
        val ep4 = StationBehaviorRegistry.getStationUiSpec(4, BALLOON_POP)
        assertEquals(expected, ep4.balloonInstructionOverride)
        assertFalse(ep4.helpControlsEnabled)
    }

    @Test
    fun chapter6_station6_is_imageToWord_template() {
        val spec = StationBehaviorRegistry.getStationUiSpec(6, 6)
        val plan = StationQuizPlans.chapter6(6)
        assertEquals(StationTemplateId.ImageToWord, spec.templateId)
        assertEquals(StationQuizMode.ImageMatch, plan.mode)
        assertTrue(spec.variants.contains(StationVariant.Chapter3ImageToWord))
        assertEquals(StationInstructionCopy.Chapter3ImageToWord, spec.imageToWordInstructionText)
    }

    @Test
    fun chapter6_station4_pictureStartsWith_hasReadablePanel_and_safeInset() {
        val spec = StationBehaviorRegistry.getStationUiSpec(6, 1)
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
        val ch6 = StationQuizPlans.chapter6(1)
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
    fun chapter5_station2_usesDragMissingLetterTemplate() {
        assertEquals(StationTemplateId.DragMissingLetter, StationBehaviorRegistry.getStationUiSpec(5, 2).templateId)
    }

    @Test
    fun chapter5_station2_dragMissingLetter_usesSideBySideLayout() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(5, 2).dragMissingLetterSideBySideLayout)
    }

    @Test
    fun chapter6_station4_dragMissingLetter_matchesChapter5Station2Layout() {
        val ch5 = StationBehaviorRegistry.getStationUiSpec(5, 2)
        val ch6 = StationBehaviorRegistry.getStationUiSpec(6, 4)
        assertEquals(StationTemplateId.DragMissingLetter, ch6.templateId)
        assertTrue(ch6.dragMissingLetterSideBySideLayout)
        assertEquals(ch5.layoutComparable(), ch6.layoutComparable())
        assertEquals(StationQuizPlans.chapter5(2).questionCount, StationQuizPlans.chapter6(4).questionCount)
        assertEquals(
            StationQuizPlans.chapter5(2).dragMissingLetterIndex,
            StationQuizPlans.chapter6(4).dragMissingLetterIndex,
        )
    }

    @Test
    fun episode4_finale_matchLetterInstructionText_present() {
        assertEquals(
            "התאימו כל אות למילה שמתחילה בה",
            StationBehaviorRegistry.getStationUiSpec(4, FINALE_PICTURE_LETTER_MATCH).matchLetterInstructionText,
        )
    }

    @Test
    fun chapter5_station3_findGrid_whiteRoundedPanel() {
        val spec = StationBehaviorRegistry.getStationUiSpec(5, 3)
        assertEquals(InstructionPanelStyle.WhiteRounded, spec.findGridInlineInstructionPanelStyle)
    }

    @Test
    fun sagaAudioStagingFlags_areTrue_onlyFor_sixStationArc_chapters() {
        val c1s1 = StationBehaviorRegistry.getStationUiSpec(1, TAP_LETTER)
        assertTrue(c1s1.audioStagingPickLetter)
        assertFalse(c1s1.audioStagingPopBalloons)
        assertFalse(c1s1.audioStagingFindGrid)

        val c1s2 = StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP)
        assertFalse(c1s2.audioStagingPickLetter)
        assertTrue(c1s2.audioStagingPopBalloons)
        assertFalse(c1s2.audioStagingFindGrid)

        val c1s3 = StationBehaviorRegistry.getStationUiSpec(1, REVEAL_THEN_CHOOSE)
        assertFalse(c1s3.audioStagingPickLetter)
        assertFalse(c1s3.audioStagingPopBalloons)
        assertTrue(c1s3.audioStagingFindGrid)

        val c3s4 = StationBehaviorRegistry.getStationUiSpec(3, 4)
        assertTrue(c3s4.audioStagingPickLetter)
        assertFalse(c3s4.audioStagingPopBalloons)
        assertFalse(c3s4.audioStagingFindGrid)
    }

    @Test
    fun popBalloons_soundPoolAndHelpControls() {
        assertTrue(StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP).popBalloonsUseSoundPoolPrompt)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP).popBalloonsHelpControlsEnabled)
        val training =
            StationBehaviorRegistry.getStationUiSpec(
                TrainingV1Config.CHAPTER_ID,
                TrainingV1Config.STATION_WORD_BALLOONS,
            )
        val ch1Balloons = StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP)
        assertEquals(ch1Balloons.popBalloonsUseSoundPoolPrompt, training.popBalloonsUseSoundPoolPrompt)
        assertEquals(ch1Balloons.popBalloonsHelpControlsEnabled, training.popBalloonsHelpControlsEnabled)
        assertEquals(ch1Balloons.balloonInstructionOverride, training.balloonInstructionOverride)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(3, 3).popBalloonsUseSoundPoolPrompt)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(3, 3).popBalloonsHelpControlsEnabled)
    }

    @Test
    fun showBetweenRoundIntroPulse_is_false_for_known_excluded_stations() {
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, TAP_LETTER).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, BALLOON_POP).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, REVEAL_THEN_CHOOSE).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, PICTURE_PICK_ONE).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, PICTURE_PICK_ALL).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(1, FINALE_PICTURE_LETTER_MATCH).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(3, 1).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(3, 2).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(3, 3).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(3, 6).showBetweenRoundIntroPulse)
        assertFalse(StationBehaviorRegistry.getStationUiSpec(TrainingV1Config.CHAPTER_ID, 1).showBetweenRoundIntroPulse)
    }
}
