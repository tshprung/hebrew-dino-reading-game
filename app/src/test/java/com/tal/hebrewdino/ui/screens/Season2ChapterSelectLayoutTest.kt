package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.domain.Season2ChapterRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2ChapterSelectLayoutTest {
    @Test
    fun chapterSelect_usesThreePlusFourRowsWithFinaleVisible() {
        assertEquals(listOf(1, 2, 3), Season2ChapterSelectTopRowIndices)
        assertEquals(listOf(4, 5, 6, 7), Season2ChapterSelectBottomRowIndices)
        assertEquals(
            Season2ChapterRegistry.CHAPTER_COUNT,
            Season2ChapterSelectTopRowIndices.size + Season2ChapterSelectBottomRowIndices.size,
        )
        assertEquals(7, Season2ChapterSelectBottomRowIndices.last())
    }

    @Test
    fun chapterSelect_source_usesExplicitRowsNotLazyGrid() {
        val source =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterSelectScreen.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/screens/Season2ChapterSelectScreen.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(source.contains("Season2ChapterSelectTopRowIndices"))
        assertTrue(source.contains("Season2ChapterSelectBottomRowIndices"))
        assertTrue(source.contains("isFinaleChapter"))
        assertFalse(source.contains("LazyVerticalGrid"))
    }
}
