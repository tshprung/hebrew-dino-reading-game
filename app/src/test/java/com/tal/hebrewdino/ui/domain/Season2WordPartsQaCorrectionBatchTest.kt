package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2WordPartsQaCorrectionBatchTest {
    private val ch1 = Season2ChapterIds.Chapter1Tyrannosaurus
    private val ch2 = Season2ChapterIds.Chapter2Triceratops
    private val ch3 = Season2ChapterIds.Chapter3Stegosaurus

    @Test
    fun ch1_station6_pictureToWordInstruction_matchesProvenPattern() {
        val text = Season2StationThemeCopy.pictureToWordInstruction(Season2StationTheme.Standard)
        assertEquals("\u200Fאיזו מילה מתאימה לתמונה?", text)
        val spec = StationBehaviorRegistry.getStationUiSpec(ch1, 6)
        assertEquals(text, spec.imageToWordInstructionText)
        assertEquals(StationReplayMode.TargetWordOnly, spec.replayMode)
    }

    @Test
    fun ch2_station6_usesChooseCorrectSplitMode() {
        val plan = Season2Chapter1StationOrder.quizPlan(chapterIndex = 2, stationId = 6)
        assertEquals(Season2WordPartsPresentationMode.VisibleWordParts, plan.season2WordPartsPresentationMode)
        assertEquals(
            "\u200Fמצאו את חלקי המילה",
            Season2StationThemeCopy.wordPartsInstruction(
                Season2WordPartsPresentationMode.VisibleWordParts,
            ),
        )
    }

    @Test
    fun ch3_station5_guidedChooseCorrectSplit() {
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 5)
        assertEquals(Season2WordPartsPresentationMode.GuidedWordParts, plan.season2WordPartsPresentationMode)
        val ch3Scope = Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words)
        val q = generateWordParts(ch3Scope, Season2WordPartsPresentationMode.GuidedWordParts)
        assertEquals(3, q.splitOptions.size)
        assertTrue(q.splitOptions.any { it.firstPart == q.firstPart && it.secondPart == q.correctPart })
    }

    @Test
    fun ch3_station6_hiddenChooseCorrectSplit() {
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 6)
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, plan.season2WordPartsPresentationMode)
        val ch3Scope = Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words)
        val q = generateWordParts(ch3Scope, Season2WordPartsPresentationMode.HiddenWordPartsChallenge)
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, q.presentationMode)
        assertEquals(3, q.splitOptions.size)
    }

    @Test
    fun splitOptions_areFullSplits_notSingleParts() {
        val q = generateWordParts(Season2ChapterRegistry.chapter(2)!!.wordCatalogIds, Season2WordPartsPresentationMode.VisibleWordParts)
        q.splitOptions.forEach { option ->
            assertTrue(option.firstPart.isNotEmpty())
            assertTrue(option.secondPart.isNotEmpty())
            assertNotEquals(q.word, option.firstPart)
            assertNotEquals(q.word, option.secondPart)
        }
    }

    @Test
    fun splitOptions_preserveCorrectFirstSecondOrder() {
        val spec = Season2WordPartsCatalog.curatedEntries.first { it.catalogId == "w_ר_1" }
        val q =
            Season2AdvancedStationGenerators.wordPartsChooseCorrectSplit(
                rnd = Random(3),
                spec = spec,
                distractorSpecs =
                    Season2WordPartsCatalog.curatedEntries.filter {
                        it.catalogId in setOf("w_פ_2", "w_ג_1")
                    },
            )
        val correct = q.splitOptions.first { it.firstPart == "רא" && it.secondPart == "ש" }
        assertEquals("רא", correct.firstPart)
        assertEquals("ש", correct.secondPart)
        assertEquals("ראש", correct.firstPart + correct.secondPart)
    }

    @Test
    fun ch3_letterPool_includesReviewLetterShin() {
        assertTrue(Season2ChapterContent.ch3Letters.contains("ש"))
        assertEquals(5, Season2ChapterContent.ch3Letters.distinct().size)
        assertTrue(Season2ChapterContent.ch3Words.contains("w_ש_1"))
        assertTrue(Season2ChapterContent.ch3Words.contains("w_ש_4"))
    }

    @Test
    fun season2_popBalloons_noIntroPulse() {
        val spec = StationBehaviorRegistry.getStationUiSpec(ch3, 1)
        assertEquals(StationTemplateId.PopBalloons, spec.templateId)
        assertFalse(spec.showBetweenRoundIntroPulse)
        val ch2Spec = StationBehaviorRegistry.getStationUiSpec(ch2, 1)
        assertFalse(ch2Spec.showBetweenRoundIntroPulse)
    }

    @Test
    fun wordPartsInstructionAssets_useModeSpecificPaths() {
        assertEquals(
            Season2RawAudio.WordPartsChooseSplitInstructions,
            Season2StationAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.GuidedWordParts,
            ),
        )
        assertEquals(
            Season2RawAudio.WordPartsHiddenSplitInstructions,
            Season2StationAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            ),
        )
    }

    @Test
    fun season1_chapter1_station6_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        assertNull(plan.season2AdvancedMode)
    }

    @Test
    fun noLegacyDinoAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
    }

    private fun generateWordParts(
        wordIds: List<String>,
        mode: Season2WordPartsPresentationMode,
    ): Question.WordPartsQuestion {
        val specs = Season2WordPartsCatalog.entriesForPresentationMode(wordIds, mode)
        require(specs.size >= 3)
        return Season2AdvancedStationGenerators.generateForMode(
            rnd = Random(7),
            mode = Season2AdvancedStationMode.WordParts,
            wordCatalogIds = wordIds,
            roundIndex = 0,
            excludeCorrectIds = emptySet(),
            distractorLetters = emptyList(),
            wordPartsPresentationMode = mode,
        ) as Question.WordPartsQuestion
    }
}
