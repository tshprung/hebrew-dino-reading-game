package com.tal.hebrewdino.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavHostStateTest {
    @Test
    fun unlockedChapter_afterChapter1Complete_is2() {
        val flags =
            AppNavHostState.deriveChapterFlags(
                MainUiState(
                    beachOutroSeen = true,
                ),
            )
        assertEquals(2, flags.unlockedChapter)
    }

    @Test
    fun unlockedChapter_afterChapter2Complete_is3() {
        val flags =
            AppNavHostState.deriveChapterFlags(
                MainUiState(
                    chapter2Completed = true,
                    beachOutroSeen = true,
                ),
            )
        assertEquals(3, flags.unlockedChapter)
    }

    @Test
    fun unlockedChapter_afterChapter3Complete_is4() {
        val flags =
            AppNavHostState.deriveChapterFlags(
                MainUiState(
                    chapter3Completed = true,
                    beachOutroSeen = true,
                ),
            )
        assertEquals(4, flags.unlockedChapter)
        assertFalse(flags.chapter4ComingSoon)
    }

    @Test
    fun unlockedChapter_freshGame_is1() {
        val flags = AppNavHostState.deriveChapterFlags(MainUiState())
        assertEquals(1, flags.unlockedChapter)
        assertTrue(flags.chapter4ComingSoon)
        assertTrue(flags.chapter5ComingSoon)
        assertTrue(flags.chapter6ComingSoon)
    }

    @Test
    fun unlockedChapter_chainUnlocksUntil6() {
        assertEquals(
            4,
            AppNavHostState.deriveChapterFlags(
                MainUiState(chapter3Completed = true, beachOutroSeen = true),
            ).unlockedChapter,
        )
        assertEquals(
            5,
            AppNavHostState.deriveChapterFlags(
                MainUiState(chapter4Completed = true, chapter3Completed = true, beachOutroSeen = true),
            ).unlockedChapter,
        )
        assertEquals(
            6,
            AppNavHostState.deriveChapterFlags(
                MainUiState(
                    chapter5Completed = true,
                    chapter4Completed = true,
                    chapter3Completed = true,
                    beachOutroSeen = true,
                ),
            ).unlockedChapter,
        )
    }

    @Test
    fun chapter1ProgressForStrip_trueWhenAllStationsComplete() {
        val flags =
            AppNavHostState.deriveChapterFlags(
                MainUiState(
                    completedLevels = setOf(1, 2, 3, 4, 5, 6),
                ),
            )
        assertTrue(flags.chapter1ProgressForStrip)
        assertTrue(flags.chapter1AllStationsComplete)
    }

    @Test
    fun maxSelectableChapterId_capsAt3UntilChapter3Done() {
        val flags =
            AppNavHostState.deriveChapterFlags(
                MainUiState(
                    chapter2Completed = true,
                    beachOutroSeen = true,
                ),
            )
        assertEquals(3, flags.maxSelectableChapterId)
    }
}
