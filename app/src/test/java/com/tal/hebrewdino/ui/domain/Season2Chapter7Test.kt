package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Chapter7Test {
    @Test
    fun registry_hasSevenChapters() {
        assertEquals(7, Season2ChapterRegistry.CHAPTER_COUNT)
        assertEquals(7, Season2ChapterRegistry.chapters.size)
    }

    @Test
    fun chapter7_hasExpectedGameplayId() {
        assertEquals(Season2ChapterIds.Chapter7Pteranodon, Season2ChapterIds.chapterGameplayId(7))
        assertEquals(107, Season2ChapterIds.Chapter7Pteranodon)
    }

    @Test
    fun chapter7_isPlayableFinale() {
        val ch7 = Season2ChapterRegistry.chapter(7)!!
        assertEquals("פטרנודון", ch7.dinosaurNameHebrew)
        assertEquals(Season2StationTheme.FlyingSky, ch7.stationTheme)
        assertTrue(ch7.isPlayable)
        assertTrue(ch7.validation.qaReady)
    }

    @Test
    fun chapter7_posterUsesDedicatedPteranodonAsset() {
        val ch7 = Season2ChapterRegistry.chapter(7)!!
        assertNotNull(ch7.posterPuzzleResId)
        assertEquals(R.drawable.season2_ch7_pteranodon_poster, ch7.posterPuzzleResId)
        assertNotEquals(R.drawable.season2_trex_puzzle_full, ch7.posterPuzzleResId)
        assertNotEquals(R.drawable.season2_mosasaurus_puzzle_full, ch7.posterPuzzleResId)
        assertEquals(R.drawable.season2_ch7_pteranodon_poster, Season2ChapterRegistry.posterResId(7))
    }

    @Test
    fun chapter7_unlocksOnlyAfterChapter6Complete() {
        assertFalse(Season2ChapterRegistry.isChapterUnlocked(7, emptySet()))
        assertFalse(Season2ChapterRegistry.isChapterUnlocked(7, setOf(1, 2, 3, 4, 5)))
        assertTrue(Season2ChapterRegistry.isChapterUnlocked(7, setOf(1, 2, 3, 4, 5, 6)))
    }

    @Test
    fun chapter7_hasSixStationsWithFinaleArc() {
        val expected =
            listOf(
                1 to Season2ChapterStationPlans.StationKind.DragWordToPicture,
                2 to Season2ChapterStationPlans.StationKind.DragMissingLetter,
                3 to Season2ChapterStationPlans.StationKind.MemoryMatch,
                4 to Season2ChapterStationPlans.StationKind.Rhyming,
                5 to Season2ChapterStationPlans.StationKind.WordParts,
                6 to Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            )
        for ((station, kind) in expected) {
            assertEquals("st$station", kind, Season2ChapterStationPlans.stationKind(7, station))
        }
    }

    @Test
    fun chapter7_dragWordUsesThreePairsWhenPoolAllows() {
        val ctx = Season2ChapterStationPlans.contextFor(7)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, stationId = 1)
        assertEquals(StationQuizMode.DragWordToPicture, plan.mode)
        assertTrue((plan.dragWordToPicturePairCount ?: 0) >= 2)
        if (ctx.wordCatalogIds.size >= 3) {
            assertEquals(3, plan.dragWordToPicturePairCount)
        }
    }

    @Test
    fun chapter7_usesNoNewLettersOutsideSeason2Union() {
        val union = Season2ChapterContent.season2UnionLetters.toSet()
        val ch7 = Season2ChapterRegistry.chapter(7)!!
        assertTrue(union.containsAll(ch7.letters))
        assertEquals(union.size, ch7.letters.size)
    }

    @Test
    fun chapter7_wordsAreSubsetOfSeason2Union() {
        val union = Season2ChapterContent.season2UnionWordCatalogIds.toSet()
        val ch7 = Season2ChapterRegistry.chapter(7)!!
        assertTrue(union.containsAll(ch7.wordCatalogIds))
        assertEquals(union.size, ch7.wordCatalogIds.size)
    }

    @Test
    fun chapter7_wordsPassAssetValidation() {
        val ch7 = Season2ChapterRegistry.chapter(7)!!
        assertTrue(ch7.validation.missingAssets.isEmpty())
    }

    @Test
    fun chapter7_stationPlansValidate() {
        val ctx = Season2ChapterStationPlans.contextFor(7)!!
        val issues = Season2ChapterStationPlans.validateAllStations(ctx)
        assertTrue("Ch7 validation issues: $issues", issues.isEmpty())
    }

    @Test
    fun chapter7_memoryMatchAtStation3() {
        assertTrue(Season2Chapter1StationOrder.isMemoryMatchStation(7, 3))
        assertFalse(Season2Chapter1StationOrder.isMemoryMatchStation(7, 4))
    }

    @Test
    fun chapter7_wordPartsUsesHiddenCapstone() {
        val ctx = Season2ChapterStationPlans.contextFor(7)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, stationId = 5)
        assertEquals(Season2AdvancedStationMode.WordParts, plan.season2AdvancedMode)
        assertEquals(
            Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            plan.season2WordPartsPresentationMode,
        )
    }

    @Test
    fun progressPrefs_supportsChapter7Range() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/data/Season2ProgressPrefs.kt")
        assertTrue(source.contains("Season2ChapterRegistry.CHAPTER_COUNT"))
        assertTrue(source.contains("chapterRange"))
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

    @Test
    fun completingChapter7_completesSeason2Arc() {
        val allStations = (1..Season2StandardRevealOrder.STATION_COUNT).toSet()
        assertTrue(
            Season2ChapterRegistry.isChapterComplete(
                chapterIndex = 7,
                completedChapters = emptySet(),
                completedStations = allStations,
            ),
        )
    }

    @Test
    fun stationPlanVersion_notBumpedForNewChapterOnly() {
        assertEquals(2, Season2ProgressPrefs.STATION_PLAN_VERSION)
    }

    @Test
    fun season1_chapterCount_unchanged() {
        assertEquals(6, Chapter1Config.STATION_COUNT)
        assertEquals(6, Chapter6Config.STATION_COUNT)
    }
}
