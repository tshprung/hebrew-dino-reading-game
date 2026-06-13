package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.REVEAL_THEN_CHOOSE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingV1SourceParityTest {
    @Test
    fun sourceStation_mapping_matchesFirstOccurrenceInSeason1() {
        assertEquals(1 to TAP_LETTER, TrainingV1SourceStation.sourceChapterAndStation(TrainingV1Config.STATION_HEAR_LETTER_CHOOSE))
        assertEquals(1 to PICTURE_PICK_ALL, TrainingV1SourceStation.sourceChapterAndStation(TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER))
        assertEquals(3 to 6, TrainingV1SourceStation.sourceChapterAndStation(TrainingV1Config.STATION_PICTURE_CHOOSE_WORD))
        assertEquals(1 to REVEAL_THEN_CHOOSE, TrainingV1SourceStation.sourceChapterAndStation(TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID))
        assertEquals(1 to BALLOON_POP, TrainingV1SourceStation.sourceChapterAndStation(TrainingV1Config.STATION_WORD_BALLOONS))
        assertEquals(1 to FINALE_PICTURE_LETTER_MATCH, TrainingV1SourceStation.sourceChapterAndStation(TrainingV1Config.STATION_MATCH_LETTER_TO_WORD))
    }

    @Test
    fun trainingUiSpec_matchesSourceExceptChapterAndStationIds() {
        val stations =
            listOf(
                TrainingV1Config.STATION_HEAR_LETTER_CHOOSE,
                TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
                TrainingV1Config.STATION_PICTURE_CHOOSE_WORD,
                TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID,
                TrainingV1Config.STATION_WORD_BALLOONS,
                TrainingV1Config.STATION_MATCH_LETTER_TO_WORD,
            )
        stations.forEach { trainingStationId ->
            val (sourceChapter, sourceStation) = TrainingV1SourceStation.sourceChapterAndStation(trainingStationId)
            val trainingSpec = StationBehaviorRegistry.getStationUiSpec(TrainingV1Config.CHAPTER_ID, trainingStationId)
            val sourceSpec = StationBehaviorRegistry.getStationUiSpec(sourceChapter, sourceStation)
            assertEquals("template st=$trainingStationId", sourceSpec.templateId, trainingSpec.templateId)
            assertEquals("instruction parity st=$trainingStationId", sourceInstructionKey(sourceSpec), sourceInstructionKey(trainingSpec))
            assertEquals("help st=$trainingStationId", sourceSpec.helpControlsEnabled, trainingSpec.helpControlsEnabled)
            assertEquals("replay st=$trainingStationId", sourceSpec.replayMode, trainingSpec.replayMode)
            assertEquals("hint st=$trainingStationId", sourceSpec.hintMode, trainingSpec.hintMode)
            assertTrue(trainingSpec.showBetweenRoundIntroPulse == false)
        }
    }

    @Test
    fun trainingQuizPlan_matchesSourceShapeWithSingleRound() {
        val stations =
            listOf(
                TrainingV1Config.STATION_HEAR_LETTER_CHOOSE,
                TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
                TrainingV1Config.STATION_PICTURE_CHOOSE_WORD,
                TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID,
                TrainingV1Config.STATION_WORD_BALLOONS,
                TrainingV1Config.STATION_MATCH_LETTER_TO_WORD,
            )
        stations.forEach { trainingStationId ->
            val trainingPlan = StationQuizPlans.trainingV1(trainingStationId)
            val sourcePlan = TrainingV1SourceStation.sourceQuizPlan(trainingStationId)
            assertEquals("mode st=$trainingStationId", sourcePlan.mode, trainingPlan.mode)
            assertEquals(1, trainingPlan.questionCount)
            assertEquals("listenOnly st=$trainingStationId", sourcePlan.listenOnlyTargetPrompt, trainingPlan.listenOnlyTargetPrompt)
            assertEquals("optionCount st=$trainingStationId", sourcePlan.optionCount, trainingPlan.optionCount)
            assertEquals(
                "imageMatchAlwaysThreeChoices st=$trainingStationId",
                sourcePlan.imageMatchAlwaysThreeChoices,
                trainingPlan.imageMatchAlwaysThreeChoices,
            )
            assertEquals(
                "imageMatchChoiceCount st=$trainingStationId",
                sourcePlan.imageMatchChoiceCount,
                trainingPlan.imageMatchChoiceCount,
            )
        }
    }

    @Test
    fun trainingWhichWord_resolvesEarlyArcUxStationId_forSingleInStationPraise() {
        assertEquals(
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            SixStationArcQaPolicy.earlyArcUxStationId(
                TrainingV1Config.CHAPTER_ID,
                TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                gameplayChapterId = TrainingV1Config.CHAPTER_ID,
                season2UxStationId =
                    SixStationArcQaPolicy.earlyArcUxStationId(
                        TrainingV1Config.CHAPTER_ID,
                        TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
                    ),
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                gameplayChapterId = TrainingV1Config.CHAPTER_ID,
                season2UxStationId =
                    SixStationArcQaPolicy.earlyArcUxStationId(
                        TrainingV1Config.CHAPTER_ID,
                        TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
                    ),
            ),
        )
    }

    private fun sourceInstructionKey(spec: StationUiSpec): String =
        listOf(
            spec.pickLetterInstructionOverride,
            spec.imageMatchHeaderInstructionOverride,
            spec.imageToWordInstructionText,
            spec.findGridInlineInstructionOverride,
            spec.balloonInstructionOverride,
            spec.matchLetterInstructionText,
        ).joinToString("|")
}
