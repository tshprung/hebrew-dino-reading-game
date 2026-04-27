package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Test

class Chapter1Station4PictureInnerScaleTest {
    @Test
    fun medusa_matchesStation5() {
        assertEquals(0.8f, Chapter1Station4PictureInnerScale.likeStation5("מדוזה", 0), 0f)
        assertEquals(0.8f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_medusa), 0f)
    }

    @Test
    fun house_matchesStation5() {
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("בית", 0), 0f)
    }

    @Test
    fun bed_matchesStation5SmallScale() {
        assertEquals(0.96f, Chapter1Station4PictureInnerScale.likeStation5("מיטה", 0), 0f)
        assertEquals(0.96f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_mitah), 0f)
    }

    @Test
    fun pacifier_matchesStation5() {
        assertEquals(1.5f, Chapter1Station4PictureInnerScale.likeStation5("מוצץ", 0), 0f)
        assertEquals(1.5f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_motzetz), 0f)
    }

    @Test
    fun default_matchesStation5HeartScale() {
        assertEquals(2f, Chapter1Station4PictureInnerScale.likeStation5("לב", 0), 0f)
        assertEquals(2f, Chapter1Station4PictureInnerScale.likeStation5("דלת", 0), 0f)
    }

    @Test
    fun chapter2PictureTuning_matchesStation5() {
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("שיניים", 0), 0f)
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_shinayim), 0f)
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("ג'ירפה", 0), 0f)
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_girafa), 0f)
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("רמזור", 0), 0f)
        assertEquals(1f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_ramzor), 0f)
        assertEquals(0.75f, Chapter1Station4PictureInnerScale.likeStation5("גדר", 0), 0f)
        assertEquals(0.75f, Chapter1Station4PictureInnerScale.likeStation5("x", R.drawable.lesson_pic_gader), 0f)
    }
}
