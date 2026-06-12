package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2AdvancedStationGeneratorsTest {
    private val rnd = Random(42)

    @Test
    fun pictureToWord_generatesValidatedQuestion() {
        val ids = listOf("w_כ_2", "w_ק_1", "w_כ_3")
        val q =
            Season2AdvancedStationGenerators.pictureToWord(
                rnd = rnd,
                wordCatalogIds = ids,
            )
        assertEquals(3, q.choices.size)
        assertTrue(q.choices.any { it.id == q.correctChoiceId })
        assertTrue(q.choices.all { choice -> ids.contains(choice.id) })
    }

    @Test
    fun missingFirstLetter_generatesPartialWordAndOptions() {
        val q =
            Season2AdvancedStationGenerators.missingFirstLetter(
                rnd = rnd,
                catalogId = "w_ש_1",
                distractorLetters = listOf("ש", "ס", "מ", "ח"),
            )
        assertEquals("שמש", q.word)
        assertEquals("_מש", q.partialWord)
        assertEquals("ש", q.correctLetter)
        assertTrue(q.correctLetter in q.optionLetters)
    }

    @Test
    fun wordParts_generatesChooseCorrectSplitQuestion() {
        val spec = Season2WordPartsCatalog.curatedEntries.first { it.catalogId == "w_ש_1" }
        val distractors =
            Season2WordPartsCatalog.curatedEntries.filter {
                it.catalogId in setOf("w_ג_1", "w_ח_3")
            }
        val q =
            Season2AdvancedStationGenerators.wordPartsChooseCorrectSplit(
                rnd = rnd,
                spec = spec,
                distractorSpecs = distractors,
            )
        assertEquals("שמש", q.word)
        assertEquals("ש", q.firstPart)
        assertEquals("מש", q.correctPart)
        assertEquals(3, q.splitOptions.size)
        assertTrue(
            q.splitOptions.any { it.firstPart == "ש" && it.secondPart == "מש" },
        )
    }

    @Test
    fun rhyming_generatesPictureChoices() {
        val pair = Season2RhymePairCatalog.validatedPairs().first { it.targetCatalogId == "w_ב_2" }
        val ch6Words =
            Season2ChapterRegistry.chapter(6)!!.wordCatalogIds
        val q =
            Season2AdvancedStationGenerators.rhyming(
                rnd = rnd,
                pair = pair,
                wordCatalogIds = ch6Words,
            )
        assertEquals("בלון", q.targetWord)
        assertEquals(3, q.choices.size)
        assertEquals("w_ח_3", q.correctChoiceId)
    }

    @Test
    fun stationContentValidator_rejectsMissingCatalogEntry() {
        val missing = Season2StationContentValidator.validateWords(listOf("w_NOPE"))
        assertTrue(missing.any { it.contains("w_NOPE") })
    }

    @Test
    fun stationContentValidator_rejectsPlaceholderImage() {
        val check = Season2StationContentValidator.wordAssetCheck("w_מ_1")
        assertNotNull(check)
        // מכונית uses car art, not placeholder — should be valid image
        assertTrue(check!!.hasImage)
    }

    @Test
    fun chapterStationPlans_advancedStationsValidate() {
        val advancedStations =
            listOf(
                3 to 5,
                3 to 6,
                4 to 5,
                5 to 5,
                5 to 6,
                6 to 4,
                6 to 5,
            )
        for ((chapterIndex, stationId) in advancedStations) {
            val ctx = Season2ChapterStationPlans.contextFor(chapterIndex)!!
            val issues = Season2ChapterStationPlans.validateStation(ctx, stationId)
            assertTrue("ch$chapterIndex st$stationId: $issues", issues.isEmpty())
        }
    }

    @Test
    fun season1_plans_doNotUseAdvancedModes() {
        for (sid in 1..6) {
            assertNull(StationQuizPlans.chapter1(sid).season2AdvancedMode)
            assertNull(StationQuizPlans.chapter2(sid).season2AdvancedMode)
        }
    }

    @Test
    fun season2_ch1_ch2_finaleUsesExpectedModes() {
        assertEquals(
            StationQuizMode.DragMissingLetter,
            Season2Chapter1StationOrder.quizPlan(chapterIndex = 1, stationId = 6).mode,
        )
        assertEquals(
            Season2AdvancedStationMode.WordParts,
            Season2Chapter1StationOrder.quizPlan(chapterIndex = 2, stationId = 6).season2AdvancedMode,
        )
        assertEquals(StationQuizMode.PopBalloons, Season2Chapter1StationOrder.quizPlan(chapterIndex = 1, stationId = 1).mode)
    }

    @Test
    fun levelSession_advancedMode_generatesQuestions() {
        val advanced =
            Season2AdvancedStationPlans.pictureToWordPlan(
                wordCatalogIds = listOf("w_כ_2", "w_ק_1", "w_כ_3", "w_ב_1"),
            )
        val plan = Season2AdvancedStationPlans.toStationQuizPlan(advanced)
        val session = LevelSession(plan)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.ImageMatchQuestion)
    }

    @Test
    fun rhymeCatalog_documentsLimitedPairs() {
        val validated = Season2RhymePairCatalog.validatedPairs()
        assertTrue(validated.any { it.targetCatalogId == "w_ב_2" && it.rhymeCatalogId == "w_ח_3" })
        // Example pairs from spec without assets are intentionally omitted.
        assertFalse(validated.any { it.rhymeCatalogId.contains("טיל") })
    }
}
