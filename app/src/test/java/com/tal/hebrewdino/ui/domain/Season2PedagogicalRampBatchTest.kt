package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2PedagogicalRampBatchTest {
    private val ch1 = Season2ChapterIds.Chapter1Tyrannosaurus
    private val ch2 = Season2ChapterIds.Chapter2Triceratops
    private val ch3 = Season2ChapterIds.Chapter3Stegosaurus

    @Test
    fun s2_finaleAndWordParts_stationPlans_byChapter() {
        val ch1St6 = Season2Chapter1StationOrder.quizPlan(chapterIndex = 1, stationId = 6)
        assertEquals(StationQuizMode.DragMissingLetter, ch1St6.mode)
        assertEquals(StationTemplateId.DragMissingLetter, StationBehaviorRegistry.getStationUiSpec(ch1, 6).templateId)

        val ch2St6 = Season2Chapter1StationOrder.quizPlan(chapterIndex = 2, stationId = 6)
        assertEquals(Season2AdvancedStationMode.WordParts, ch2St6.season2AdvancedMode)
        assertEquals(Season2WordPartsPresentationMode.VisibleWordParts, ch2St6.season2WordPartsPresentationMode)

        val ch3Ctx = Season2ChapterStationPlans.contextFor(3)!!
        assertEquals(Season2WordPartsPresentationMode.GuidedWordParts, Season2ChapterStationPlans.quizPlan(ch3Ctx, 5).season2WordPartsPresentationMode)
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, Season2ChapterStationPlans.quizPlan(ch3Ctx, 6).season2WordPartsPresentationMode)
        assertEquals(
            Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            Season2StationUx.stationKindForGameplayChapter(Season2ChapterIds.Chapter4Brachiosaurus, 6),
        )
        assertNull(StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH).season2AdvancedMode)
    }

    @Test
    fun wordParts_generators_threeOptions_forVisibleAndHidden() {
        fun assertThreeOptions(mode: Season2WordPartsPresentationMode, catalogIds: List<String>) {
            val specs = Season2WordPartsCatalog.entriesForPresentationMode(catalogIds, mode)
            require(specs.size >= 3)
            val spec = specs.first()
            val q =
                Season2AdvancedStationGenerators.wordPartsChooseCorrectSplit(
                    rnd = Random(mode.ordinal),
                    spec = spec,
                    distractorSpecs = specs.filter { it.catalogId != spec.catalogId }.take(2),
                    presentationMode = mode,
                )
            assertEquals(mode, q.presentationMode)
            assertEquals(3, q.splitOptions.size)
        }
        assertThreeOptions(
            Season2WordPartsPresentationMode.VisibleWordParts,
            Season2ChapterRegistry.chapter(2)!!.wordCatalogIds,
        )
        assertThreeOptions(
            Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words),
        )
    }

    @Test
    fun wordParts_curatedSplits_roshAndPil() {
        fun splitFor(id: String) = Season2WordPartsCatalog.curatedEntries.first { it.catalogId == id }
        assertEquals("רא", splitFor("w_ר_1").firstPart)
        assertEquals("ש", splitFor("w_ר_1").secondPart)
        assertEquals("פי", splitFor("w_פ_2").firstPart)
        assertEquals("ל", splitFor("w_פ_2").secondPart)
        assertTrue(Season2StationContentValidator.validateWordPartsEntry(splitFor("w_ר_1")).isEmpty())
        assertFalse(
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2ChapterRegistry.chapter(2)!!.wordCatalogIds,
                Season2WordPartsPresentationMode.VisibleWordParts,
            ).none { it.catalogId == "w_פ_2" },
        )
    }
}
