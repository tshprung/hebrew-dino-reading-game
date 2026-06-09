package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.audio.AudioClips
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
        assertTrue(Season2StationThemeCopy.pictureToWordInstruction(theme).contains("בחרו"))
        assertTrue(Season2StationThemeCopy.missingFirstLetterInstruction(theme).contains("איזו אות חסרה"))
        assertFalse(Season2StationThemeCopy.missingFirstLetterInstruction(theme).contains("בהתחלה"))
        assertTrue(Season2StationThemeCopy.rhymingInstruction(theme).contains("מתחרזת"))
        assertFalse(Season2StationThemeCopy.rhymingInstruction(theme).contains("מחרוזת"))
    }

    @Test
    fun wordPartsQuestion_doesNotExposeFullWordInPartialDisplay() {
        val spec = Season2WordPartsCatalog.curatedEntries.first { it.catalogId == "w_ש_1" }
        val q =
            Season2AdvancedStationGenerators.wordPartsPickSecondPart(
                rnd = Random(1),
                spec = spec,
                distractorSecondParts = listOf("חל", "פר"),
            )
        assertEquals("ש", q.firstPart)
        assertEquals("מש", q.correctPart)
        assertFalse(q.partOptions.contains(q.word))
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
    fun advancedInstructionAssets_useExpectedPaths() {
        assertEquals(
            AudioClips.Season2WordPartsInstructions,
            Season2StationAudio.instructionAssetPath(Season2AdvancedStationMode.WordParts),
        )
        assertEquals(
            AudioClips.Season2RhymingInstructions,
            Season2StationAudio.instructionAssetPath(Season2AdvancedStationMode.Rhyming),
        )
    }

    @Test
    fun ch6RhymePairs_includeCurtainPairWhenWordInCatalog() {
        val pairs = Season2RhymePairCatalog.pairsForWordIds(Season2ChapterContent.ch6Words)
        assertTrue(pairs.size >= 2)
        assertTrue(pairs.any { it.targetCatalogId == "w_ב_2" && it.rhymeCatalogId == "w_ח_3" })
    }

    @Test
    fun chapterStationPlans_stillValidateAfterCh6WordExpansion() {
        for (chapterIndex in 3..6) {
            val ctx = Season2ChapterStationPlans.contextFor(chapterIndex)!!
            val issues = Season2ChapterStationPlans.validateStation(ctx, 5)
            assertTrue("ch$chapterIndex st5: $issues", issues.isEmpty())
        }
    }
}
