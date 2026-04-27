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
        val isTeeth = choice.word == "שיניים" || choice.id == "w_ש_3"
        val isGiraffe = choice.word == "ג'ירפה" || choice.id == "w_ג_4"
        val isTrafficLight = choice.word == "רמזור" || choice.id == "w_ר_5"
        val isFence = choice.word == "גדר" || choice.id == "w_ג_3"
        val isTable = choice.word == "שולחן" || choice.id == "w_ש_2"
        val isCar = choice.word == "רכב" || choice.id == "w_ר_2"
        val isCurtain = choice.word == "וילון" || choice.id == "w_ו_3"
        val isWaffle = choice.word == "וופל" || choice.id == "w_ו_2"
        val isLeg = choice.word == "רגל" || choice.id == "w_ר_3"
        val isRose = choice.word == "ורד" || choice.id == "w_ו_1"
        val isHippo = choice.word == "היפופוטם" || choice.id == "w_ה_3"
        val isMountain = choice.word == "הר" || choice.id == "w_ה_1"
        return when {
            // Tweaked: +20% for readability.
            isMedusa -> 0.8f
            isHouse -> 1f
            // Chapter 2 art tuning: default inner scale is 2f; halve for traffic light + fence, quarter for giraffe + teeth.
            isTrafficLight -> 1f
            // Feedback: fence bigger.
            isFence -> 0.75f
            // Feedback: enlarge teeth (2x vs earlier).
            isTeeth -> 1f
            // Feedback: giraffe 2x.
            isGiraffe -> 1f
            isTable || isCar -> 1f
            isCurtain -> 1f
            // Feedback: shrink waffle 2x (relative to default 2f).
            isWaffle -> 1f
            isLeg || isRose -> 1f
            // Feedback: hippo reads small; bump by ~20% relative to default 2f.
            isHippo -> 2.4f
            // Feedback: mountain +15%.
            isMountain -> 2.3f
            // Episode 1 station 5 feedback: bed reads huge; make it 1/4 of the default (2x) scale.
            // Tweaked: slightly larger (≈ +20%) to read better.
            // Tweaked again: +20%.
            isBed -> 0.72f
            // Episode 1 station 5 feedback: pacifier reads huge; make it 1/3 of the default (2x) scale.
            isPacifier -> (2f / 3f)
            else -> 2f
        }
    }
}
