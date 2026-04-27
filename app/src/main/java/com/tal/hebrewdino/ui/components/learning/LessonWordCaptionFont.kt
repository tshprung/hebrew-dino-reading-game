package com.tal.hebrewdino.ui.components.learning

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
): TextUnit =
    with(density) {
        val codePoints = word.codePointCount(0, word.length)
        val wordTight =
            when {
                // Feedback: shrink hippo by ~15%, surprise by ~10%.
                word == "היפופוטם" -> 0.66f
                word == "הפתעה" -> 0.77f
                word == "גלידה" -> 0.86f
                word == "שולחן" -> 0.82f
                // Chapter 1 station 4 feedback: "אבטיח" should be ~10% smaller.
                word == "אבטיח" -> 0.90f
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
