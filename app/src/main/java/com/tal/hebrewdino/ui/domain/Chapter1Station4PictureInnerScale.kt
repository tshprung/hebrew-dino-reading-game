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
        val isPacifier = word == "מוצץ" || tileDrawable == R.drawable.lesson_pic_motzetz
        val isTeeth = word == "שיניים" || tileDrawable == R.drawable.lesson_pic_shinayim
        val isGiraffe = word == "ג'ירפה" || tileDrawable == R.drawable.lesson_pic_girafa
        val isTrafficLight = word == "רמזור" || tileDrawable == R.drawable.lesson_pic_ramzor
        val isFence = word == "גדר" || tileDrawable == R.drawable.lesson_pic_gader
        val isTable = word == "שולחן" || tileDrawable == R.drawable.lesson_pic_shulchan
        val isCar = word == "רכב" || tileDrawable == R.drawable.lesson_pic_car
        val isCurtain = word == "וילון" || tileDrawable == R.drawable.lesson_pic_vilon
        val isLeg = word == "רגל" || tileDrawable == R.drawable.lesson_pic_regel
        val isRose = word == "ורד" || tileDrawable == R.drawable.lesson_pic_vered
        val isHippo = word == "היפופוטם"
        val isWaffle = word == "וופל" || tileDrawable == R.drawable.lesson_pic_wafel
        val isMountain = word == "הר"
        return when {
            // Tweaked: +20% for readability.
            isMedusa -> 0.8f
            isHouse -> 1f
            // UX: "מיטה" was too large; shrink to match station 5 perception (1/3 of previous 2f).
            // Tweaked: slightly larger (≈ +20%) to read better.
            // Tweaked again: +20%.
            isBed -> 0.96f
            // Same drawable as station 5 (`lesson_pic_motzetz`); match [Chapter1Station5And6ImageMatchInnerScale] pacifier scale.
            // New request: pacifier +50% everywhere.
            isPacifier -> 1.5f
            isTrafficLight -> 1f
            isFence -> 0.75f
            isTeeth -> 1f
            isGiraffe -> 1f
            isTable -> 1.2f
            isCar -> 1f
            isCurtain -> 1f
            isWaffle -> 1f
            isLeg || isRose -> 1f
            isHippo -> 2.4f
            isMountain -> 2.3f
            else -> 2f
        }
    }
}
