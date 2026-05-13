package com.tal.hebrewdino.ui.components.learning

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Station 4/5/6 word cards: caption size is derived from card width; long words need an extra tighten
 * so Hebrew captions stay on one line inside narrow tiles.
 */
fun captionFontSizeForWordCard(
    density: Density,
    cardWidth: Dp,
    word: String,
    sizeMultiplier: Float,
    /** When set, allows chapter/station-specific caption tweaks (e.g. Ch2 station 5). */
    chapterId: Int? = null,
    stationId: Int? = null,
): TextUnit =
    with(density) {
        val codePoints = word.codePointCount(0, word.length)
        val wordTight =
            when {
                // Episode 4 feedback: window caption ~20% smaller.
                word == "חלון" -> 0.80f
                // Episode 4 feedback: bag caption ~20% smaller.
                word == "תיק" -> 0.80f
                // Episode 5 feedback: monkey caption ~20% smaller (illustration enlarged).
                word == "קוף" -> 0.80f
                // Feedback: drum caption −15%.
                word == "תוף" -> 0.85f
                // Feedback: train caption −15%.
                word == "קטר" -> 0.85f
                // Chapter 2 station 5: "שעון" caption −15%.
                chapterId == 2 &&
                    stationId == Chapter1StationOrder.PICTURE_PICK_ALL &&
                    word == "שעון" -> 0.85f
                // Feedback: shrink hippo by ~15%, surprise by ~10%.
                word == "היפופוטם" -> 0.528f
                word == "הפתעה" -> 0.616f
                word == "גלידה" -> 0.86f
                word == "שולחן" -> 0.82f
                word == "חולצה" -> 0.72f
                word == "פרפר" -> 0.90f
                word == "טיגריס" -> 0.72f
                word == "צלחת" -> 0.81f
                word == "נמלה" -> 0.90f
                word == "טוסט" -> 0.95f
                word == "דחליל" -> 0.76f
                word == "צפרדע" -> 0.665f
                word == "פילפל" -> 0.855f
                word == "תפוח" -> 0.90f
                // Feedback: balloon should be ~10% smaller everywhere.
                word == "בלון" -> 0.90f
                // Episode 1 station 5 feedback: bed caption −10% (keep readable, avoid any clipping).
                word == "מיטה" -> 0.90f
                // Episode 1 station 5 feedback: pan caption.
                word == "מחבת" -> 0.81f
                // Feedback: "למידה" caption.
                word == "למידה" -> 0.81f
                // Feedback: "מכונית" caption −10%.
                word == "מכונית" -> 0.81f
                // Feedback: "אבטיח" caption.
                word == "אבטיח" -> 0.72f
                word == "ארנב" -> 0.80f
                word == "מוצץ" -> 0.80f
                word == "ברווז" -> 0.90f
                codePoints >= 9 -> 0.82f
                codePoints >= 7 -> 0.88f
                codePoints >= 6 -> 0.80f
                codePoints >= 5 -> 0.86f
                else -> 1f
            }
        val m = sizeMultiplier * wordTight
        (cardWidth.toPx() * 0.22f * m).coerceIn(
            22f * fontScale * m,
            40f * fontScale * m,
        ).sp
    }
