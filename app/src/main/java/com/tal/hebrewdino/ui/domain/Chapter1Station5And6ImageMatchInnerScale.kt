package com.tal.hebrewdino.ui.domain

/**
 * Episode 1 stations 5 and 6 (image-match cards): inner illustration scale inside the card.
 * Single implementation so station 6 cannot drift from station 5. Locked by unit tests.
 */
object Chapter1Station5And6ImageMatchInnerScale {
    fun innerScale(choice: LessonChoice): Float {
        val isHouse = choice.word == "בית" || choice.id == "w_ב_1"
        val isMedusa = choice.word == "מדוזה" || choice.id == "w_מ_3"
        return when {
            isMedusa -> (2f / 3f)
            isHouse -> 1f
            else -> 2f
        }
    }
}
