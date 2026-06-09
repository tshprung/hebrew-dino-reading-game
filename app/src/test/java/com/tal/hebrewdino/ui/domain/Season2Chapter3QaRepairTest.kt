package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2Chapter3QaRepairTest {
    private val ch3GameplayId = Season2ChapterIds.Chapter3Stegosaurus

    @Test
    fun ch3_station2_pickLetterOptions_areUnique() {
        val gen = PopBalloonsGenerator()
        val letters = Season2ChapterContent.ch3Letters
        repeat(40) { seed ->
            val q =
                gen.generatePickLetterOptions(
                    rnd = Random(seed),
                    group = letters,
                    correctAnswer = letters[seed % letters.size],
                    optionCount = 5,
                )
            assertEquals(q.options.size, q.options.distinct().size)
            assertTrue(q.correctAnswer in q.options)
            assertTrue(q.options.all { it in letters })
        }
    }

    @Test
    fun ch3_station3_noIntroPulse_andHelpEnabled() {
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 3)
        assertEquals(StationTemplateId.PictureStartsWith, spec.templateId)
        assertFalse(spec.showBetweenRoundIntroPulse)
        assertTrue(spec.helpControlsEnabled)
        assertTrue(Season2StationUx.isWarmupPictureStartsWith(ch3GameplayId, 3))
    }

    @Test
    fun ch3_station5_wordPartsTemplateWired() {
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 5)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
        assertEquals(
            Season2AdvancedStationMode.WordParts,
            Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(3)!!, 5).season2AdvancedMode,
        )
    }

    @Test
    fun ch3_station6_isHiddenWordPartsFinale() {
        assertEquals(
            Season2ChapterStationPlans.StationKind.WordParts,
            Season2StationUx.stationKindForGameplayChapter(ch3GameplayId, 6),
        )
        assertEquals(
            Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            Season2StationUx.stationKindForGameplayChapter(Season2ChapterIds.Chapter4Brachiosaurus, 6),
        )
        assertEquals(StationTemplateId.WordParts, StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 6).templateId)
        val plan = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(3)!!, 6)
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, plan.season2WordPartsPresentationMode)
    }

    @Test
    fun ch3_gameplayChapter_expectsSelectedCompanion() {
        assertTrue(com.tal.hebrewdino.ui.companion.CompanionVisualPolicy.expectsSelectedCompanion(ch3GameplayId))
    }

    @Test
    fun ch4_station6_stillMatchLetterToWord_unchanged() {
        assertEquals(
            Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            Season2StationUx.stationKindForGameplayChapter(Season2ChapterIds.Chapter4Brachiosaurus, 6),
        )
        val spec = StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter4Brachiosaurus, 6)
        assertEquals(StationTemplateId.MatchLetterToWord, spec.templateId)
    }

    @Test
    fun ch1_ch2_stationMappings_unchanged() {
        assertEquals(
            StationTemplateId.PopBalloons,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter1Tyrannosaurus, 2).templateId,
        )
        assertEquals(
            StationTemplateId.PickLetter,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter1Tyrannosaurus, 1).templateId,
        )
        assertEquals(
            StationTemplateId.PopBalloons,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter2Triceratops, 2).templateId,
        )
        assertFalse(
            Season2StationUx.isWarmupPictureStartsWith(Season2ChapterIds.Chapter1Tyrannosaurus, 3),
        )
    }
}
