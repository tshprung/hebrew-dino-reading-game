package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2PropagateCh1ApprovedStationUxTest {
    @Test
    fun audit_station_type_mapping_ch2_6() {
        assertEquals(Season2ChapterStationPlans.StationKind.WhichWordStartsWith, Season2StationQaPolicy.expectedStationKind(2, 5))
        assertEquals(Season2ChapterStationPlans.StationKind.WordParts, Season2StationQaPolicy.expectedStationKind(2, 6))
        assertEquals(Season2ChapterStationPlans.StationKind.PictureToWord, Season2StationQaPolicy.expectedStationKind(4, 5))
        assertEquals(Season2ChapterStationPlans.StationKind.WhichWordStartsWith, Season2StationQaPolicy.expectedStationKind(5, 6))
        assertEquals(Season2ChapterStationPlans.StationKind.PictureToWord, Season2StationQaPolicy.expectedStationKind(6, 6))
    }

    @Test
    fun pop_balloons_policy_all_s2() {
        assertTrue(Season2StationQaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(Season2Chapter1StationOrder.POP_BALLOONS))
        val pop = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(pop.contains("shouldCancelPreviousFeedbackOnPopBalloonsTap"))
        assertFalse(Season2Ch1QaPolicy.shouldCancelPreviousFeedbackOnPopBalloonsTap(season2QuizBalloons = true))
    }

    @Test
    fun pick_letter_policy_all_s2() {
        val pick = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt")
        assertTrue(pick.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
        assertTrue(Season2PostFocusCorrectPolicy.shouldPlayCompanionPraiseOnCorrect(isSeason2QuizChapter = true, season2HadCoachIntervention = true))
    }

    @Test
    fun picture_starts_with_policy_all_s2() {
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2ChapterIds.Chapter3Stegosaurus,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
        assertTrue(Season2EarlyStationQaPolicy.shouldReplayWordForPictureStartsWithCoach(Season2Chapter1StationOrder.PICTURE_STARTS_WITH))
        val picture = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithActions.kt")
        assertTrue(picture.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
    }

    @Test
    fun memory_match_policy_all_s2() {
        val memory = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2MemoryMatchStationScreen.kt")
        assertTrue(memory.contains("season2_memory_match_bg"))
    }

    @Test
    fun picture_to_word_policy_all_s2() {
        assertTrue(
            Season2StationQaPolicy.shouldReplayPictureToWordCoachWithInstruction(
                Season2ChapterIds.Chapter4Brachiosaurus,
                season2UxStationId = 5,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipPictureToWordAssetPraiseOnLastRound(
                Season2ChapterIds.Chapter6Mosasaurus,
                season2UxStationId = 6,
                isLast = true,
            ),
        )
        assertFalse(
            Season2StationQaPolicy.shouldSkipPictureToWordAssetPraiseOnLastRound(
                Season2ChapterIds.Chapter2Triceratops,
                season2UxStationId = 6,
                isLast = true,
            ),
        )
    }

    @Test
    fun word_parts_policy_all_s2() {
        val advanced = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(advanced.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
        assertTrue(advanced.contains("playSplitTapSequence"))
    }

    @Test
    fun post_focus_correct_success_pool_all_relevant_s2() {
        val coach = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2GuessingCoach.kt")
        assertTrue(coach.contains("Season2StationQaPolicy.shouldReplayPictureToWordCoachWithInstruction"))
        val postFocus = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2PostFocusCorrectPolicy.kt")
        assertTrue(postFocus.contains("shouldPlayCompanionPraiseOnCorrect"))
    }

    @Test
    fun no_duplicate_praise_representative_stations() {
        assertTrue(
            Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(
                Season2ChapterIds.Chapter5Ankylosaurus,
                season2UxStationId = 6,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2ChapterIds.Chapter5Ankylosaurus,
                season2UxStationId = 6,
            ),
        )
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation"))
    }

    @Test
    fun no_duplicate_try_again_representative_stations() {
        assertTrue(Season2Ch1QaPolicy.shouldPlayTryAgainInPopBalloonsSfx(season2QuizBalloons = true))
        val picture = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithActions.kt")
        assertTrue(picture.contains("joinSilently(feedbackJob)"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("skipTryAgainAudio = isSeason2BalloonStation"))
    }

    @Test
    fun ch1_unchanged() {
        assertTrue(
            Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldOrchestrateWhichWordCorrectPraiseInStation(Season2ChapterIds.Chapter1Tyrannosaurus, 
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
    }

    @Test
    fun s1_behavior_unchanged() {
        assertFalse(Season2StationQaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(null))
        assertFalse(Season2StationQaPolicy.isWhichWordStartsWithStation(1, null))
    }

    @Test
    fun progression_content_unchanged() {
        val plans = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2ChapterStationPlans.kt")
        assertTrue(plans.contains("StationKind.PopBalloons"))
        assertFalse(plans.contains("Season2StationQaPolicy"))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
