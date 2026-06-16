package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.test.ProjectSource

import com.tal.hebrewdino.ui.screens.chapterResetRowIdsForTest
import com.tal.hebrewdino.ui.screens.chapterUnlockWaiverRowIdsForTest
import com.tal.hebrewdino.ui.screens.season2ChapterResetRowIdsForTest
import com.tal.hebrewdino.ui.screens.season2ChapterUnlockWaiverRowIdsForTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSeasonChapterResetTest {
    @Test
    fun season1_reset_rows_cover_six_chapters() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6), chapterResetRowIdsForTest())
    }

    @Test
    fun season2_reset_rows_cover_seven_chapters() {
        assertEquals((1..Season2ChapterRegistry.CHAPTER_COUNT).toList(), season2ChapterResetRowIdsForTest())
    }

    @Test
    fun waiver_rows_cover_six_chapters() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6), chapterUnlockWaiverRowIdsForTest())
        assertEquals((1..Season2ChapterRegistry.CHAPTER_COUNT).toList(), season2ChapterUnlockWaiverRowIdsForTest())
    }

    @Test
    fun settings_screen_uses_toggle_buttons_and_season2_gate() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/SettingsScreen.kt")
        assertTrue(source.contains("ChapterSelectionToggleGrid"))
        assertTrue(source.contains("season2Enabled"))
        assertTrue(source.contains("onResetSeason2Chapters"))
        assertTrue(source.contains("onSeason1ChapterUnlockWaiversChange"))
        assertTrue(source.contains("if (season2Enabled)"))
    }

    @Test
    fun season2_progress_supports_partial_chapter_reset() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/data/Season2ProgressPrefs.kt")
        assertTrue(source.contains("suspend fun resetChapters(chapterIds: Set<Int>)"))
    }
}
