package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2PedagogicalRampBatchTest {
    private val ch1 = Season2ChapterIds.Chapter1Tyrannosaurus
    private val ch2 = Season2ChapterIds.Chapter2Triceratops
    private val ch3 = Season2ChapterIds.Chapter3Stegosaurus

    @Test
    fun ch1_station6_isPictureToWord() {
        val plan = Season2Chapter1StationOrder.quizPlan(chapterIndex = 1, stationId = 6)
        assertEquals(Season2AdvancedStationMode.PictureToWord, plan.season2AdvancedMode)
        assertEquals(3, plan.imageMatchChoiceCount)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch1, 6)
        assertEquals(StationTemplateId.ImageToWord, spec.templateId)
        assertTrue(spec.helpControlsEnabled)
    }

    @Test
    fun ch2_station6_isVisibleWordParts() {
        val plan = Season2Chapter1StationOrder.quizPlan(chapterIndex = 2, stationId = 6)
        assertEquals(Season2AdvancedStationMode.WordParts, plan.season2AdvancedMode)
        assertEquals(Season2WordPartsPresentationMode.VisibleWordParts, plan.season2WordPartsPresentationMode)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch2, 6)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
    }

    @Test
    fun ch3_station5_isGuidedWordParts() {
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 5)
        assertEquals(Season2AdvancedStationMode.WordParts, plan.season2AdvancedMode)
        assertEquals(Season2WordPartsPresentationMode.GuidedWordParts, plan.season2WordPartsPresentationMode)
    }

    @Test
    fun ch3_station6_isHiddenWordPartsChallenge() {
        assertEquals(
            Season2ChapterStationPlans.StationKind.WordParts,
            Season2StationUx.stationKindForGameplayChapter(ch3, 6),
        )
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 6)
        assertEquals(Season2AdvancedStationMode.WordParts, plan.season2AdvancedMode)
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, plan.season2WordPartsPresentationMode)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3, 6)
        assertEquals(StationTemplateId.WordParts, spec.templateId)
        assertFalse(spec.templateId == StationTemplateId.ImageMatch)
    }

    @Test
    fun visibleWordParts_usesChooseCorrectSplit() {
        val specs =
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2ChapterRegistry.chapter(2)!!.wordCatalogIds,
                Season2WordPartsPresentationMode.VisibleWordParts,
            )
        require(specs.size >= 3)
        val spec = specs.first()
        val q =
            Season2AdvancedStationGenerators.wordPartsChooseCorrectSplit(
                rnd = Random(1),
                spec = spec,
                distractorSpecs = specs.filter { it.catalogId != spec.catalogId }.take(2),
                presentationMode = Season2WordPartsPresentationMode.VisibleWordParts,
            )
        assertEquals(Season2WordPartsPresentationMode.VisibleWordParts, q.presentationMode)
        assertEquals(3, q.splitOptions.size)
    }

    @Test
    fun hiddenWordParts_usesChooseCorrectSplitWithThreeOptions() {
        val specs =
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2ChapterContent.ch3Words,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            )
        assertTrue(specs.all { it.catalogId !in setOf("w_נ_2", "w_צ_2") })
        require(specs.size >= 3)
        val spec = specs.first()
        val q =
            Season2AdvancedStationGenerators.wordPartsChooseCorrectSplit(
                rnd = Random(2),
                spec = spec,
                distractorSpecs = specs.filter { it.catalogId != spec.catalogId }.take(2),
                presentationMode = Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            )
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, q.presentationMode)
        assertEquals(3, q.splitOptions.size)
    }

    @Test
    fun ch2_visibleWordParts_usePreferredSplits() {
        val ids = Season2ChapterRegistry.chapter(2)!!.wordCatalogIds
        val specs =
            Season2WordPartsCatalog.entriesForPresentationMode(
                ids,
                Season2WordPartsPresentationMode.VisibleWordParts,
            )
        assertTrue(specs.any { it.catalogId == "w_ח_3" && it.wordPartLabel() == "חלון" })
        assertTrue(specs.any { it.catalogId == "w_ש_1" })
    }

    @Test
    fun ch4_station6_unchanged_matchLetterFinale() {
        assertEquals(
            Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            Season2StationUx.stationKindForGameplayChapter(Season2ChapterIds.Chapter4Brachiosaurus, 6),
        )
    }

    @Test
    fun season1_chapter1_station6_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        assertNull(plan.season2AdvancedMode)
        assertEquals(StationQuizMode.ImageMatch, plan.mode)
    }

    @Test
    fun roshAndPil_useSyllableLikeSplits() {
        fun splitFor(id: String) =
            Season2WordPartsCatalog.curatedEntries.first { it.catalogId == id }
        assertEquals("רא", splitFor("w_ר_1").firstPart)
        assertEquals("ש", splitFor("w_ר_1").secondPart)
        assertEquals("פי", splitFor("w_פ_2").firstPart)
        assertEquals("ל", splitFor("w_פ_2").secondPart)
        assertTrue(
            Season2StationContentValidator.validateWordPartsEntry(splitFor("w_ר_1")).isEmpty(),
        )
        assertTrue(
            Season2StationContentValidator.validateWordPartsEntry(splitFor("w_פ_2")).isEmpty(),
        )
    }

    @Test
    fun noLegacyDinoAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
    }

    private fun Season2WordPartsEntry.wordPartLabel(): String {
        val entry = LessonWordCatalog.entries.first { it.id == catalogId }
        return entry.word
    }
}
