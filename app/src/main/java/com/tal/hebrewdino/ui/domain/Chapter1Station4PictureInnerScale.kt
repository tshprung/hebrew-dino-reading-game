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
        val isBed = word == "מיטה" || tileDrawable == R.drawable.lesson_pic_mitah
        return when {
            isMedusa -> (2f / 3f)
            isHouse -> 1f
            // UX: "מיטה" was too large; shrink to match station 5 perception (1/3 of previous 2f).
            isBed -> (2f / 3f)
            else -> 2f
        }
    }
}
