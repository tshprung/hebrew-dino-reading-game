package com.tal.hebrewdino.ui.components.learning

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
fun captionFontSizeForWordCard(
    density: Density,
    cardWidth: Dp,
    sizeMultiplier: Float,
): TextUnit =
    with(density) {
        // Automatic sizing: this is only a *target* size.
        // Final fit is handled by AutoFitSingleLineText, so we avoid per-word exception lists.
        val m = sizeMultiplier
        (cardWidth.toPx() * 0.24f * m).coerceIn(
            22f * fontScale,
            44f * fontScale,
        ).sp
    }
