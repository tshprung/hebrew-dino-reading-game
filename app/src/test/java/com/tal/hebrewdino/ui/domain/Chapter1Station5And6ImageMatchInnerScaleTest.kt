package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class Chapter1Station5And6ImageMatchInnerScaleTest {
    @Test
    fun medusa() {
        assertEquals(2f / 3f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "מ", "מדוזה", 0)), 0f)
        assertEquals(2f / 3f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_מ_3", "מ", "x", 0)), 0f)
    }

    @Test
    fun house() {
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ב_1", "ב", "בית", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ב", "בית", 0)), 0f)
    }

    @Test
    fun defaultMatchesHeartScale() {
        assertEquals(2f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("id", "ל", "לב", 0)), 0f)
    }

    @Test
    fun bedIsQuarterScale() {
        assertEquals(0.5f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_מ_4", "מ", "מיטה", 0)), 0f)
        assertEquals(0.5f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "מ", "מיטה", 0)), 0f)
    }

    @Test
    fun pacifierIsThirdScale() {
        assertEquals(2f / 3f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_מ_5", "מ", "מוצץ", 0)), 0f)
        assertEquals(2f / 3f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "מ", "מוצץ", 0)), 0f)
    }
}
