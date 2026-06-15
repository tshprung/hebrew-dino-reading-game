package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2StationAudioTest {
    @Test
    fun pictureToWordStation_detectsS2GameplayIds() {
        assertTrue(Season2StationAudio.isPictureToWordStation(Season2ChapterIds.Chapter4Brachiosaurus, 5))
        assertTrue(Season2StationAudio.isPictureToWordStation(Season2ChapterIds.Chapter6Mosasaurus, 6))
        assertFalse(Season2StationAudio.isPictureToWordStation(Season2ChapterIds.Chapter3Stegosaurus, 5))
    }

    @Test
    fun usesImageToWordRawWordClips_includesSeason2PictureToWord() {
        assertTrue(Season2StationAudio.usesImageToWordRawWordClips(Season2ChapterIds.Chapter4Brachiosaurus))
        assertTrue(Season2StationAudio.usesImageToWordRawWordClips(Season2ChapterIds.Chapter6Mosasaurus))
    }

    @Test
    fun stationThemeCopy_usesShortKidFriendlyInstructions() {
        val theme = Season2StationTheme.StegosaurusPlates
        assertTrue(Season2StationThemeCopy.pictureToWordInstruction(theme).contains("איזו מילה"))
        assertTrue(Season2StationThemeCopy.missingFirstLetterInstruction(theme).contains("איזו אות חסרה במילה"))
        assertFalse(Season2StationThemeCopy.missingFirstLetterInstruction(theme).contains("בהתחלה"))
        assertTrue(Season2StationThemeCopy.rhymingInstruction(theme).contains("מתחרזת עם"))
        assertFalse(Season2StationThemeCopy.rhymingInstruction(theme).contains("מחרוזת"))
        assertTrue(
            Season2StationThemeCopy.wordPartsInstruction(Season2WordPartsPresentationMode.GuidedWordParts)
                .contains("מצאו את חלקי המילה"),
        )
    }

    @Test
    fun wordPartsQuestion_splitOptionsAreFullSplits() {
        val spec = Season2WordPartsCatalog.curatedEntries.first { it.catalogId == "w_ש_1" }
        val q =
            Season2AdvancedStationGenerators.wordPartsChooseCorrectSplit(
                rnd = Random(1),
                spec = spec,
                distractorSpecs =
                    Season2WordPartsCatalog.curatedEntries.filter {
                        it.catalogId in setOf("w_ג_1", "w_ח_3")
                    },
            )
        assertEquals("ש", q.firstPart)
        assertEquals("מש", q.correctPart)
        assertFalse(q.splitOptions.any { it.secondPart == q.word })
    }

    @Test
    fun missingFirstLetter_usesUnderscorePartialOnly() {
        val q =
            Season2AdvancedStationGenerators.missingFirstLetter(
                rnd = Random(2),
                catalogId = "w_ש_1",
                distractorLetters = listOf("ש", "ס", "מ"),
            )
        assertTrue(q.partialWord.startsWith("_"))
        assertEquals("_מש", q.partialWord)
        assertFalse(q.partialWord == q.word)
    }

    @Test
    fun advancedInstructionAssets_useResRawIds() {
        assertEquals(
            Season2RawAudio.WordPartsChooseSplitInstructions,
            Season2StationAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.GuidedWordParts,
            ),
        )
        assertEquals(
            R.raw.season2_rhyming_instructions,
            Season2StationAudio.instructionRawResId(Season2AdvancedStationMode.Rhyming),
        )
    }

    @Test
    fun ch6RhymePairs_includeKofTofPairWhenWordInCatalog() {
        val scope =
            Season2RhymePairCatalog.wordCatalogIdsForRhymingStation(6, Season2ChapterContent.ch6Words)
        val pairs = Season2RhymePairCatalog.pairsForStation(6, 4)!!
        assertTrue(pairs.any { it.targetCatalogId == "w_ק_1" && it.rhymeCatalogId == "w_ת_4" })
        assertTrue("w_ת_4" in scope)
    }

    @Test
    fun chapterStationPlans_stillValidateAfterRhymeExpansion() {
        val rhymeStations = listOf(5 to 5, 6 to 4, 7 to 4)
        rhymeStations.forEach { (chapterIndex, stationId) ->
            val ctx = Season2ChapterStationPlans.contextFor(chapterIndex)!!
            val issues = Season2ChapterStationPlans.validateStation(ctx, stationId)
            assertTrue("ch$chapterIndex st$stationId: $issues", issues.isEmpty())
        }
    }
}
