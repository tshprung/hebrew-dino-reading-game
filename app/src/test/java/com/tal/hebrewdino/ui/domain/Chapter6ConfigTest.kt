package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class Chapter6ConfigTest {
    @Test
    fun chapter6_letters_are_subset_of_chapters1to5() {
        val taught =
            (Chapter1Config.letters + Chapter2Config.letters + Chapter3Config.letters + Chapter4Config.letters + Chapter5Config.letters)
                .toSet()
        assertTrue(Chapter6Config.letters.isNotEmpty())
        assertTrue(Chapter6Config.letters.all { it in taught })
    }
}

