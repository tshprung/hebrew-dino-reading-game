package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2QaPolishBatchTest {
    private val ch3 = Season2ChapterIds.Chapter3Stegosaurus
    private val ch4 = Season2ChapterIds.Chapter4Brachiosaurus

    @Test
    fun station6Feedback_skipsFocusReplayOnFirstWrong() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldUseCompanionFocusFeedback(companionCoachEnabled = true),
        )
        assertFalse(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 1,
                companionCoachEnabled = true,
            ),
        )
    }

    @Test
    fun station6Feedback_replaysInstructionAfterTwoConsecutiveWrongs() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 2,
                companionCoachEnabled = true,
            ),
        )
        assertFalse(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 2,
                companionCoachEnabled = false,
            ),
        )
    }

    @Test
    fun rewardLayout_posterDominatesCompanionAndText() {
        assertTrue(Season2RewardLayout.POSTER_MAX_HEIGHT_COMPACT_DP >= 220)
        assertTrue(
            Season2RewardLayout.COMPANION_SIZE_COMPACT_DP <
                Season2RewardLayout.POSTER_MAX_HEIGHT_COMPACT_DP,
        )
        assertTrue(Season2RewardLayout.HEADLINE_SP_COMPACT in 26..34)
        assertTrue(Season2RewardLayout.SUBLINE_SP_COMPACT in 15..22)
    }

    @Test
    fun completedMapBackEmphasis_onlyWhenChapterComplete() {
        assertEquals(6, Season2Chapter1RevealOrder.STATION_COUNT)
        assertTrue(
            Season2Copy.isChapterComplete(
                chapterIndex = 1,
                completedChapters = emptySet(),
                completedStations = (1..6).toSet(),
            ),
        )
    }

    @Test
    fun season2PickLetter_hasNoInitialTargetLetterIntroPulse() {
        val spec = StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter1Tyrannosaurus, 2)
        assertEquals(StationTemplateId.PickLetter, spec.templateId)
        assertFalse(spec.showBetweenRoundIntroPulse)
    }

    @Test
    fun ch3WordParts_hintEnabled_fullWordNotDefaultVisible() {
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3, 5)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
        assertTrue(spec.helpControlsEnabled)
        assertEquals(StationHintMode.TemporaryFullWord, spec.hintMode)
    }

    @Test
    fun ch3Station6_hiddenWordPartsHelpAndNoIntroPulse() {
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3, 6)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
        assertFalse(spec.showBetweenRoundIntroPulse)
        assertTrue(spec.helpControlsEnabled)
        assertTrue(Season2StationUx.stationKindForGameplayChapter(ch3, 6) == Season2ChapterStationPlans.StationKind.WordParts)
    }

    @Test
    fun ch4Station6_matchLetterParityWithCh1St6() {
        val ch4 = Season2ChapterIds.Chapter4Brachiosaurus
        val ch1Spec = StationBehaviorRegistry.getStationUiSpec(1, Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        val ch4Spec = StationBehaviorRegistry.getStationUiSpec(ch4, 6)
        assertTrue(Season2StationUx.usesChapter1StyleMatchLetterBehavior(ch4, 6))
        assertEquals(ch1Spec.templateId, ch4Spec.templateId)
        assertEquals(ch1Spec.matchLetterInstructionText, ch4Spec.matchLetterInstructionText)
        assertEquals(ch1Spec.matchLetterCompactWideSpread, ch4Spec.matchLetterCompactWideSpread)
        assertEquals(ch1Spec.matchLetterInstructionReadablePanel, ch4Spec.matchLetterInstructionReadablePanel)
        val ch1Plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        val ch4Plan = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(4)!!, 6)
        assertEquals(ch1Plan.questionCount, ch4Plan.questionCount)
        assertEquals(ch1Plan.imageMatchCaptionSizeMultiplier, ch4Plan.imageMatchCaptionSizeMultiplier)
        val matchGame = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/MatchLetterToWordGame.kt")
        assertTrue(matchGame.contains("Season2StationUx.usesChapter1StyleMatchLetterBehavior"))
        val questionHost = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameQuestionHost.kt")
        assertTrue(questionHost.contains("usesChapter1StyleMatchLetterBehavior"))
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("Season2StationUx.isMatchLetterFinale(chapterId, stationId)"))
    }

    @Test
    fun ch4Station6_matchLetterCompactWideSpread() {
        val spec = StationBehaviorRegistry.getStationUiSpec(ch4, 6)
        assertEquals(StationTemplateId.MatchLetterToWord, spec.templateId)
        assertTrue(spec.matchLetterCompactWideSpread)
        assertFalse(spec.showBetweenRoundIntroPulse)
        assertTrue(Season2StationUx.isMatchLetterFinale(ch4, 6))
    }

    @Test
    fun ch1Ch2_stationMappings_matchDragWiring() {
        assertEquals(
            StationTemplateId.PopBalloons,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter1Tyrannosaurus, 1).templateId,
        )
        assertEquals(
            StationTemplateId.PickLetter,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter2Triceratops, 1).templateId,
        )
    }

    @Test
    fun noLegacyDinoDrawableReferencesInCompanionAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
        assertEquals(R.drawable.companion_dino_talk_1, dino.talkFrameResIds.first())
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        return candidates.first { it.exists() }.readText()
    }
}
