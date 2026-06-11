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
    fun station6Feedback_skipsCoachBubbleOnFirstWrong() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldSkipCoachBubble(
                season2UxStationId = Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD,
                isSeason2Quiz = true,
            ),
        )
        assertFalse(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 1,
                season2UxStationId = 6,
                isSeason2Quiz = true,
            ),
        )
    }

    @Test
    fun station6Feedback_replaysInstructionAfterTwoConsecutiveWrongs() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 2,
                season2UxStationId = 6,
                isSeason2Quiz = true,
            ),
        )
        assertTrue(
            Season2Station6FeedbackPolicy.shouldSkipCoachBubble(
                season2UxStationId = Season2Chapter1StationOrder.PICK_LETTER,
                isSeason2Quiz = true,
            ),
        )
    }

    @Test
    fun pictureToWordFeedback_usesLightPolicyOnNonFinaleStations() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldUseLightWordChoiceFeedback(
                season2UxStationId = 5,
                isSeason2Quiz = true,
                season2AdvancedMode = Season2AdvancedStationMode.PictureToWord,
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
        val spec = StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter1Tyrannosaurus, 1)
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
        assertTrue(Season2StationUx.isWordPartsStation(ch3, 6))
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
    fun ch1Ch2_stationMappings_unchangedAfterPolish() {
        assertEquals(
            StationTemplateId.PopBalloons,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter1Tyrannosaurus, 2).templateId,
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
}
