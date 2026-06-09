package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2Chapter3StationMappingTest {
    private val ch3GameplayId = Season2ChapterIds.Chapter3Stegosaurus
    private val ctx = Season2ChapterStationPlans.contextFor(3)!!

    @Test
    fun ch3_station1_mapsToPopBalloons_planAndTemplate() {
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 1)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 1)
        assertEquals(StationQuizMode.PopBalloons, plan.mode)
        assertEquals(StationTemplateId.PopBalloons, spec.templateId)
        assertTrue(spec.popBalloonsCompactLandscapePhoneTuning)
        assertTrue(spec.audioStagingPopBalloons)
        assertFalse(spec.audioStagingPickLetter)
    }

    @Test
    fun ch3_station2_mapsToPickLetter_planAndTemplate() {
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 2)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 2)
        assertEquals(StationQuizMode.PickLetter, plan.mode)
        assertEquals(StationTemplateId.PickLetter, spec.templateId)
        assertTrue(spec.audioStagingPickLetter)
        assertFalse(spec.audioStagingPopBalloons)
    }

    @Test
    fun ch3_station5_mapsToGuidedWordParts_modeAndTemplate() {
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 5)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 5)
        assertEquals(Season2AdvancedStationMode.WordParts, plan.season2AdvancedMode)
        assertEquals(Season2WordPartsPresentationMode.GuidedWordParts, plan.season2WordPartsPresentationMode)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
        assertFalse(spec.audioStagingPickLetter)
    }

    @Test
    fun ch3_station6_mapsToHiddenWordParts_notMatchLetterFinale() {
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 6)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3GameplayId, 6)
        assertEquals(Season2AdvancedStationMode.WordParts, plan.season2AdvancedMode)
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, plan.season2WordPartsPresentationMode)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
    }

    @Test
    fun instructionKind_usesTemplate_notLegacyStationNumber_forCh3Arc() {
        val balloonQ =
            Question.PopBalloonsQuestion(
                correctAnswer = "ג",
                options = listOf("ג", "נ", "פ"),
            )
        assertEquals(
            Chapter1AddressAwareAudio.InstructionKind.PopBalloons,
            Chapter1AddressAwareAudio.instructionKindFor(
                stationId = 1,
                stationTemplateId = StationTemplateId.PopBalloons,
                q = balloonQ,
            ),
        )
        val pickQ =
            Question.PopBalloonsQuestion(
                correctAnswer = "נ",
                options = listOf("ג", "נ", "פ"),
            )
        assertEquals(
            Chapter1AddressAwareAudio.InstructionKind.PickLetter,
            Chapter1AddressAwareAudio.instructionKindFor(
                stationId = 2,
                stationTemplateId = StationTemplateId.PickLetter,
                q = pickQ,
            ),
        )
    }

    @Test
    fun wordParts_doesNotRevealFullWordBeforeAnswer_ch3Catalog() {
        val spec = Season2WordPartsCatalog.entriesForWordIds(Season2ChapterContent.ch3Words).first()
        val q =
            Season2AdvancedStationGenerators.wordPartsPickSecondPart(
                rnd = Random(3),
                spec = spec,
                distractorSecondParts = listOf("על", "ור"),
            )
        assertFalse(q.partOptions.contains(q.word))
        assertFalse(q.partOptions.any { it == q.word.drop(1) && it.length > q.correctPart.length })
        assertTrue(q.word.startsWith(q.firstPart))
        assertTrue(q.word.endsWith(q.correctPart))
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
        assertEquals(
            StationTemplateId.PickLetter,
            StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter2Triceratops, 1).templateId,
        )
        assertNull(StationQuizPlans.chapter1(1).season2AdvancedMode)
    }

    @Test
    fun ch4_station6_stillMatchLetterToWord() {
        val spec = StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter4Brachiosaurus, 6)
        assertEquals(StationTemplateId.MatchLetterToWord, spec.templateId)
    }
}
