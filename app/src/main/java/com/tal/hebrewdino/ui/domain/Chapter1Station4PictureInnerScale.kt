package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R

/**
 * Episode 1 station 4: inner image scale inside the picture card must match station 5 perception
 * (card size unchanged; only illustration scale). Locked by unit tests — change only with product sign-off.
 */
object Chapter1Station4PictureInnerScale {
    fun likeStation5(word: String, tileDrawable: Int): Float {
        val isMedusa = word == "מדוזה" || tileDrawable == R.drawable.lesson_pic_medusa
        val isHouse = word == "בית"
        return when {
            isMedusa -> (2f / 3f)
            isHouse -> 1f
            else -> 2f
        }
    }
}
