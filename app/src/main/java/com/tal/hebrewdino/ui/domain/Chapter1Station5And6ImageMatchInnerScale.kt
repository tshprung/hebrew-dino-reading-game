package com.tal.hebrewdino.ui.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardCaptionAreaHeight
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardCaptionSpacerHeight
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.layout.ScreenFit

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
        val isWindow = choice.word == "חלון" || choice.id == "w_ח_3" || choice.tileDrawable == R.drawable.lesson_pic_chalon
        val isButterfly = choice.word == "פרפר" || choice.id == "w_פ_4" || choice.tileDrawable == R.drawable.lesson_pic_parpar
        val isTrainEngine = choice.word == "קטר" || choice.id == "w_ק_3"
        val isMonkey = choice.word == "קוף" || choice.id == "w_ק_1"
        val isCube = choice.word == "קוביה" || choice.id == "w_ק_2" || choice.tileDrawable == R.drawable.lesson_pic_kubia
        val isToast = choice.word == "טוסט" || choice.id == "w_ט_1" || choice.tileDrawable == R.drawable.lesson_pic_tost
        val isPlate = choice.word == "צלחת" || choice.id == "w_צ_4"
        val isAnt = choice.word == "נמלה" || choice.id == "w_נ_1"
        val isFrog = choice.word == "צפרדע" || choice.id == "w_צ_3"
        val isHippo = choice.word == "היפופוטם" || choice.id == "w_ה_3"
        val isMountain = choice.word == "הר" || choice.id == "w_ה_1"
        val isRasterLessonPic = RasterLessonPicDrawables.isPngTile(choice.tileDrawable)
        return when {
            // PNG tiles are full-canvas art; default 2f inner scale is for sparse vectors.
            isRasterLessonPic -> 1.05f
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
            // Episode 5 feedback: cube illustration ~50% smaller vs default (2f).
            isCube -> 1f
            // Feedback: toast illustration ~50% smaller vs default (2f).
            isToast -> 1f
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

data class Station456CardSize(
    val width: Dp,
    val height: Dp,
)

object Chapter1Station4To6LessonChoiceCardSpec {
    fun station5And6CardSize(
        maxWidth: Dp,
        maxHeight: Dp,
        choiceCount: Int,
        pictureSizeMultiplier: Float,
        showWordCaption: Boolean,
    ): Station456CardSize {
        val columnGap = 10.dp
        val sidePanelW =
            if (maxWidth < 480.dp) {
                170.dp
            } else {
                200.dp
            }
        val cardsAreaW = (maxWidth - sidePanelW - columnGap).coerceAtLeast(240.dp)
        val cardGap = 8.dp
        var cardW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = cardsAreaW,
                count = choiceCount.coerceAtLeast(1),
                gap = cardGap,
                minEach = 68.dp,
                maxEach = 150.dp,
            )
        cardW =
            (cardW * (pictureSizeMultiplier * 0.90f)).coerceAtMost(
                (cardsAreaW - cardGap * (choiceCount - 1).coerceAtLeast(0)) / choiceCount.coerceAtLeast(1),
            )
        val cardsAreaVerticalPadding = 6.dp
        val perCardExtraHeight =
            24.dp +
                if (showWordCaption) {
                    LessonChoiceCardCaptionSpacerHeight + LessonChoiceCardCaptionAreaHeight
                } else {
                    0.dp
                }
        val maxPictureHeight =
            (maxHeight - cardsAreaVerticalPadding * 2 - perCardExtraHeight)
                .coerceAtLeast(60.dp)
        val maxCardWByHeight = maxPictureHeight / LessonChoiceCardPictureAspect
        val cardShrink = 0.80f
        val width = (minOf(cardW, maxCardWByHeight) * cardShrink).coerceAtLeast(60.dp)
        return Station456CardSize(width = width, height = width * LessonChoiceCardPictureAspect)
    }

    @Composable
    fun Card(
        choice: LessonChoice,
        enabled: Boolean,
        cardWidth: Dp,
        cardHeight: Dp,
        captionFontSize: TextUnit,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        scale: Float = 1f,
        showWordCaption: Boolean = true,
        innerPictureScale: Float = 1f,
        isCorrectPick: Boolean = false,
        isSelected: Boolean = false,
        wrongFlashAlpha: Float = 0f,
    ) {
        LessonChoiceCard(
            choice = choice,
            enabled = enabled,
            scale = scale,
            showWordCaption = showWordCaption,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            captionFontSize = captionFontSize,
            innerPictureScale = innerPictureScale,
            innerPictureScaleY = innerPictureScale,
            innerPictureTransformOrigin = TransformOrigin(0.5f, 0.5f),
            innerPictureTranslateY = 0.dp,
            pictureContentAlignment = Alignment.Center,
            isCorrectPick = isCorrectPick,
            isSelected = isSelected,
            wrongFlashAlpha = wrongFlashAlpha,
            onClick = onClick,
            modifier = modifier,
        )
    }
}
