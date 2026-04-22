package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Test

class Chapter1Station4PictureInnerScaleTest {
    @Test
    fun medusa_matchesStation5() {
        assertEquals(2f / 3f, Chapter1Station4PictureInnerScale.likeStation5("מדוזה", 0), 0f)
        assertEquals(2f / 3f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_medusa), 0f)
    }

    @Test
    fun house_matchesStation5() {
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("בית", 0), 0f)
    }

    @Test
    fun bed_matchesStation5SmallScale() {
        assertEquals(2f / 3f, Chapter1Station4PictureInnerScale.likeStation5("מיטה", 0), 0f)
        assertEquals(2f / 3f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_mitah), 0f)
    }

    @Test
    fun default_matchesStation5HeartScale() {
        assertEquals(2f, Chapter1Station4PictureInnerScale.likeStation5("לב", 0), 0f)
        assertEquals(2f, Chapter1Station4PictureInnerScale.likeStation5("דלת", 0), 0f)
    }
}
