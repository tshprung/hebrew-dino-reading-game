package com.tal.hebrewdino.ui.domain

/**
 * Episode 1 stations 5 and 6 (image-match cards): inner illustration scale inside the card.
 * Single implementation so station 6 cannot drift from station 5. Locked by unit tests.
 */
object Chapter1Station5And6ImageMatchInnerScale {
    fun innerScale(choice: LessonChoice): Float {
        val isHouse = choice.word == "בית" || choice.id == "w_ב_1"
        val isMedusa = choice.word == "מדוזה" || choice.id == "w_מ_3"
        val isBed = choice.word == "מיטה" || choice.id == "w_מ_4"
        val isPacifier = choice.word == "מוצץ" || choice.id == "w_מ_5"
        return when {
            isMedusa -> (2f / 3f)
            isHouse -> 1f
            // Episode 1 station 5 feedback: bed reads huge; make it 1/4 of the default (2x) scale.
            isBed -> 0.5f
            // Episode 1 station 5 feedback: pacifier reads huge; make it 1/3 of the default (2x) scale.
            isPacifier -> (2f / 3f)
            else -> 2f
        }
    }
}
