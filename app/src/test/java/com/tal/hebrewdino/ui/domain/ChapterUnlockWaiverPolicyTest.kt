package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterUnlockWaiverPolicyTest {
    private val emptySnapshot =
        ChapterUnlockWaiverPolicy.Season1Snapshot(
            beachOutroSeen = false,
            chapter1AllStationsComplete = false,
            chapter2Completed = false,
            chapter3Completed = false,
            chapter4Completed = false,
            chapter5Completed = false,
            chapter6Completed = false,
        )

    @Test
    fun waiver_on_chapter1_unlocks_chapter2_without_progress() {
        val flags =
            com.tal.hebrewdino.ui.AppNavHostState.deriveChapterFlags(
                com.tal.hebrewdino.ui.MainUiState(season1ChapterUnlockWaivers = setOf(1)),
            )
        assertEquals(2, flags.unlockedChapter)
        assertTrue(
            ChapterUnlockWaiverPolicy.canOpenChapter(
                chapterId = 2,
                snapshot = emptySnapshot,
                waivers = setOf(1),
            ),
        )
    }

    @Test
    fun waiver_chain_unlocks_later_chapters() {
        val snapshot =
            emptySnapshot.copy(chapter2Completed = true, chapter3Completed = true)
        assertEquals(
            4,
            ChapterUnlockWaiverPolicy.unlockedChapter(snapshot, waivers = setOf(1)),
        )
        assertTrue(ChapterUnlockWaiverPolicy.canOpenChapter(4, snapshot, setOf(1)))
        assertFalse(ChapterUnlockWaiverPolicy.canOpenChapter(5, snapshot, setOf(1)))
    }

    @Test
    fun waiver_on_chapter6_opens_training() {
        assertTrue(
            ChapterUnlockWaiverPolicy.canOpenChapter(
                chapterId = 7,
                snapshot = emptySnapshot,
                waivers = setOf(6),
            ),
        )
    }

    @Test
    fun season2_waiver_unlocks_next_chapter() {
        assertTrue(
            ChapterUnlockWaiverPolicy.isSeason2ChapterUnlocked(
                chapterIndex = 2,
                completedChapters = emptySet(),
                waivers = setOf(1),
            ),
        )
        assertFalse(
            ChapterUnlockWaiverPolicy.isSeason2ChapterUnlocked(
                chapterIndex = 3,
                completedChapters = emptySet(),
                waivers = setOf(1),
            ),
        )
    }

    @Test
    fun serialize_and_parse_waivers() {
        val raw = ChapterUnlockWaiverPolicy.serializeWaivers(setOf(3, 1, 9))
        assertEquals("1,3", raw)
        assertEquals(setOf(1, 3), ChapterUnlockWaiverPolicy.parseWaivers(raw))
    }
}
