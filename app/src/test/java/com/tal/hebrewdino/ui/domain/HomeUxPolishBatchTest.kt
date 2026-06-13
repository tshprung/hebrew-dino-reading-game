package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.ParentInfoPrefs
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.datastore.preferences.core.PreferenceDataStoreFactory

class HomeUxPolishBatchTest {
    @Test
    fun parentInfoPolicy_showsOncePerVersion() {
        assertTrue(ParentInfoPolicy.shouldAutoShow(lastSeenVersionCode = null, currentVersionCode = 11))
        assertTrue(ParentInfoPolicy.shouldAutoShow(lastSeenVersionCode = 10, currentVersionCode = 11))
        assertFalse(ParentInfoPolicy.shouldAutoShow(lastSeenVersionCode = 11, currentVersionCode = 11))
    }

    @Test
    fun parentInfoPrefs_persistsLastSeenVersionCode() = runBlocking {
        val tmpDir = Files.createTempDirectory("parent-info-prefs").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { File(tmpDir, "prefs.preferences_pb") },
            )
        val prefs = ParentInfoPrefs(dataStore)

        assertEquals(null, prefs.lastSeenVersionCodeFlow.first())
        prefs.markSeenForVersion(11)
        assertEquals(11, prefs.lastSeenVersionCodeFlow.first())

        scope.cancel()
    }

    @Test
    fun parentInfoCopy_omitsUnsupportedSkipStagesWording() {
        val body = ParentInfoCopy.dialogBodyLines().joinToString(" ")
        assertTrue(body.contains("אזור הורים"))
        assertTrue(body.contains("לאפס התקדמות"))
        assertTrue(body.contains("דינו ודינה"))
        assertFalse(body.contains("לדלג"))
        assertFalse(body.contains("לפתוח"))
    }

    @Test
    fun openingScreen_hasManualParentInfoButton() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/OpeningScreen.kt")
        assertTrue(source.contains("onOpenParentInfo"))
        assertTrue(source.contains("opening_parent_info"))
        assertTrue(source.contains("ParentInfoCopy.InfoButtonLabel"))
    }

    @Test
    fun seasonsScreen_season2CardUsesOverviewHero() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/SeasonsScreen.kt")
        assertTrue(source.contains("SeasonHeroKind.Season2Overview"))
        assertTrue(source.contains("R.drawable.season2_overview_hero"))
        assertFalse(source.contains("Season2FrostedPosterPreview"))
    }

    @Test
    fun seasonsScreen_release_includes_season1_and_season2_only() {
        val seasons = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/SeasonsScreen.kt")
        assertTrue(seasons.contains("seasonId = 2"))
        assertTrue(seasons.contains("SeasonAvailabilityPolicy.isSeason2Enabled()"))
        assertTrue(seasons.contains("if (BuildConfig.DEBUG)"))
        val policy = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/SeasonAvailabilityPolicy.kt")
        assertTrue(policy.contains("2 -> BuildConfig.DEBUG"))
        val nav = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/AppNavSystemGraph.kt")
        assertTrue(nav.contains("onOpenSeason2 = {"))
        assertTrue(nav.contains("NavRoutes.Season2ChapterSelect"))
        assertTrue(nav.contains("if (!SeasonAvailabilityPolicy.isSeason2Enabled())"))
    }

    @Test
    fun season2IntroText_matchesStoryAudioScript() {
        val lines = Season2Copy.seasonIntroStoryLines()
        assertEquals(4, lines.size)
        assertTrue(lines[0].contains("מצאנו מפה עתיקה"))
        assertTrue(lines[1].contains("בכל חלק של המפה"))
        assertTrue(lines[2].contains("בכל פעם שתפתרו משימה"))
        assertTrue(lines[3].contains("בואו נגלה מי מסתתר שם"))
    }

    @Test
    fun ch1IntroText_matchesChapterIntroAudioScript() {
        val lines = Season2Copy.ch1MapIntroStoryLines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("שיניים חדות"))
        assertTrue(lines[1].contains("בואו נגלה מי זה"))
    }

    @Test
    fun ch1Registry_usesCh1IntroLines() {
        val chapter = Season2ChapterRegistry.chapter(1)
        assertNotNull(chapter)
        val lines = chapter!!.mapIntroStoryLines(com.tal.hebrewdino.ui.data.PlayerAddress.Boy)
        assertEquals(Season2Copy.ch1MapIntroStoryLines(), lines)
    }

    @Test
    fun season2OverviewHeroDrawable_exists() {
        assertEquals(R.drawable.season2_overview_hero, R.drawable.season2_overview_hero)
    }

    @Test
    fun noSeason1GameplayChangesInBatch() {
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertFalse(gameScreen.contains("ParentInfo"))
        assertFalse(gameScreen.contains("season2_overview_hero"))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                File(relativePath),
                File("../$relativePath"),
                File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
