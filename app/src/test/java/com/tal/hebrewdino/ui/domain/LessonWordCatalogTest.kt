package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonWordCatalogTest {
    @Test
    fun catalog_isInternallyConsistent() {
        // Loading [LessonWordCatalog] runs init-time validation (ids, tints, first-letter checks).
        assertTrue(LessonWordCatalog.entries.size >= 40)
    }

    @Test
    fun w_lamed_3_isLimon_withLimonArt() {
        val entry = LessonWordCatalog.entries.single { it.id == "w_ל_3" }
        assertEquals("לימון", entry.word)
        assertEquals("ל", entry.letter)
        assertEquals(R.drawable.lesson_pic_limon, entry.tileRes)
    }
}
