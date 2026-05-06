package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R

/**
 * Six-station arc (chapters 1–4) stations **4, 5, and 6**: inner illustration scale inside [LessonChoiceCard].
 * Station 4 picture rounds and station 5/6 image cards must stay identical — one implementation, unit-tested.
 */
object Chapter1Station5And6ImageMatchInnerScale {
    /** Station 4 single picture card: same scale rule as station 5/6 ([innerScale]). */
    fun innerScalePictureStartsWith(
        catalogEntryId: String,
        letter: String,
        word: String,
        tintArgb: Int,
        tileDrawable: Int,
    ): Float =
        innerScale(
            LessonChoice(
                id = catalogEntryId,
                letter = letter,
                word = word,
                tintArgb = tintArgb,
                tileDrawable = tileDrawable,
            ),
        )

    fun innerScale(choice: LessonChoice): Float {
        val isHouse = choice.word == "בית" || choice.id == "w_ב_1"
        val isMedusa =
            choice.word == "מדוזה" || choice.id == "w_מ_3" || choice.tileDrawable == R.drawable.lesson_pic_medusa
        val isBed = choice.word == "מיטה" || choice.id == "w_מ_4" || choice.tileDrawable == R.drawable.lesson_pic_mitah
        val isPacifier =
            choice.word == "מוצץ" || choice.id == "w_מ_5" || choice.tileDrawable == R.drawable.lesson_pic_motzetz
        val isTeeth =
            choice.word == "שיניים" || choice.id == "w_ש_3" || choice.tileDrawable == R.drawable.lesson_pic_shinayim
        val isGiraffe =
            choice.word == "ג'ירפה" || choice.id == "w_ג_4" || choice.tileDrawable == R.drawable.lesson_pic_girafa
        val isTrafficLight =
            choice.word == "רמזור" || choice.id == "w_ר_5" || choice.tileDrawable == R.drawable.lesson_pic_ramzor
        val isFence = choice.word == "גדר" || choice.id == "w_ג_3" || choice.tileDrawable == R.drawable.lesson_pic_gader
        val isTable =
            choice.word == "שולחן" || choice.id == "w_ש_2" || choice.tileDrawable == R.drawable.lesson_pic_shulchan
        /** Reserved for a dedicated vector car asset (catalog uses emoji placeholder with [isCarPlaceholderSynonym]). */
        val isVectorCarTile = choice.tileDrawable == R.drawable.lesson_pic_car
        /** אוטו / מכונית / רכב: same placeholder drawing + inner scale as פרק 1 car words. */
        val isCarPlaceholderSynonym =
            choice.word == "מכונית" ||
                choice.id == "w_מ_1" ||
                choice.word == "אוטו" ||
                choice.id == "w_א_4" ||
                choice.word == "רכב" ||
                choice.id == "w_ר_2"
        val isCurtain =
            choice.word == "וילון" || choice.id == "w_ו_3" || choice.tileDrawable == R.drawable.lesson_pic_vilon
        val isWaffle = choice.word == "וופל" || choice.id == "w_ו_2" || choice.tileDrawable == R.drawable.lesson_pic_wafel
        val isLeg = choice.word == "רגל" || choice.id == "w_ר_3" || choice.tileDrawable == R.drawable.lesson_pic_regel
        val isRose = choice.word == "ורד" || choice.id == "w_ו_1" || choice.tileDrawable == R.drawable.lesson_pic_vered
        val isHoney = choice.word == "דבש" || choice.id == "w_ד_4" || choice.tileDrawable == R.drawable.lesson_pic_dvash
        val isWindow = choice.word == "חלון" || choice.id == "w_ח_3" || choice.tileDrawable == R.drawable.lesson_pic_halon
        val isButterfly = choice.word == "פרפר" || choice.id == "w_פ_4" || choice.tileDrawable == R.drawable.lesson_pic_parpar
        val isTrainEngine = choice.word == "קטר" || choice.id == "w_ק_3"
        val isMonkey = choice.word == "קוף" || choice.id == "w_ק_1"
        val isPlate = choice.word == "צלחת" || choice.id == "w_צ_4"
        val isAnt = choice.word == "נמלה" || choice.id == "w_נ_1"
        val isFrog = choice.word == "צפרדע" || choice.id == "w_צ_3"
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
            isTable -> 1.2f
            isVectorCarTile -> 1.15f
            isCurtain -> 1f
            // Feedback: shrink waffle 2x (relative to default 2f).
            isWaffle -> 1f
            // Honey jar vector: similar to waffle — keep inner art readable inside the card frame.
            isHoney -> 1.08f
            // Episode 4 feedback: window picture ~20% smaller.
            isWindow -> 1.6f
            // Episode 5 feedback: butterfly should read much larger.
            isButterfly -> 2.0f
            // Episode 5 feedback: locomotive + dish + ant + frog — emoji art at default card inner scale.
            isTrainEngine || isPlate || isAnt || isFrog -> 2f
            // Episode 5 feedback: monkey illustration should be ~25% smaller vs prior (caption sized separately).
            isMonkey -> 3f
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
            // +50% tuning, then −10% on the drawing.
            isPacifier -> 1.35f
            // מכונית / אוטו / רכב: default card inner scale is 2f; +15% on the drawing.
            isCarPlaceholderSynonym -> 2.3f
            else -> 2f
        }
    }
}
