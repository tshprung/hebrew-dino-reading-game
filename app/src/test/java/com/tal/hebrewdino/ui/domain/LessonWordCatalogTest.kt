package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class LessonWordCatalogTest {
    @Test
    fun catalog_isInternallyConsistent() {
        // Loading [LessonWordCatalog] runs init-time validation (ids, tints, first-letter checks).
        assertTrue(LessonWordCatalog.entries.size >= 40)
    }
}
