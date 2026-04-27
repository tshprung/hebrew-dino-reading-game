package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class Chapter1Station5And6ImageMatchInnerScaleTest {
    @Test
    fun medusa() {
        assertEquals(0.8f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "מ", "מדוזה", 0)), 0f)
        assertEquals(0.8f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_מ_3", "מ", "x", 0)), 0f)
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
    fun bedIsLargerScale() {
        assertEquals(0.72f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_מ_4", "מ", "מיטה", 0)), 0f)
        assertEquals(0.72f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "מ", "מיטה", 0)), 0f)
    }

    @Test
    fun pacifierIsLargerScale() {
        assertEquals(1.5f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_מ_5", "מ", "מוצץ", 0)), 0f)
        assertEquals(1.5f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "מ", "מוצץ", 0)), 0f)
    }

    @Test
    fun chapter2PictureTuning() {
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ש_3", "ש", "שיניים", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ש", "שיניים", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ג_4", "ג", "ג'ירפה", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ג", "ג'ירפה", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ר_5", "ר", "רמזור", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ר", "רמזור", 0)), 0f)
        assertEquals(0.75f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ג_3", "ג", "גדר", 0)), 0f)
        assertEquals(0.75f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ג", "גדר", 0)), 0f)
        assertEquals(1.2f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ש_2", "ש", "שולחן", 0)), 0f)
        assertEquals(1.2f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ש", "שולחן", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("w_ר_2", "ר", "רכב", 0)), 0f)
        assertEquals(1f, Chapter1Station5And6ImageMatchInnerScale.innerScale(LessonChoice("x", "ר", "רכב", 0)), 0f)
    }
}
