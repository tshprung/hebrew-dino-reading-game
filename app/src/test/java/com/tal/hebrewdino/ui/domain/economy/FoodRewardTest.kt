package com.tal.hebrewdino.ui.domain.economy

import org.junit.Assert.assertEquals
import org.junit.Test

class FoodRewardTest {
    @Test
    fun fanfare_uses_feminine_three_for_bananas() {
        val banana = FoodRewardKind("בננה", "בננות", "🍌")
        assertEquals("הצלחתם! קיבלתם שלוש בננות", fanfareTextForFood(3, banana))
    }
}
