package com.tal.hebrewdino.ui.components.learning

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.min

@Composable
fun AutoFitSingleLineText(
    text: String,
    maxWidth: Dp,
    maxHeight: Dp? = null,
    targetFontSize: TextUnit,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 12.sp,
    textAlign: TextAlign = TextAlign.Center,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val rawMaxWidthPx = with(density) { maxWidth.toPx().roundToInt() }.coerceAtLeast(1)
    // Safety margin: some fonts/glyphs can slightly overhang vs measured bounds (and rounding differs
    // between measure and draw). Shrink-to-fit against a slightly smaller width to prevent spill.
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val safeFraction = if (isRtl) 0.90f else 0.94f
    val maxWidthPx =
        min(
            rawMaxWidthPx - (if (isRtl) 10 else 6),
            (rawMaxWidthPx * safeFraction).roundToInt(),
        ).coerceAtLeast(1)
    val maxHeightPx =
        maxHeight?.let { with(density) { it.toPx().roundToInt() }.coerceAtLeast(1) }
    val fittedSize =
        remember(text, maxWidthPx, maxHeightPx, targetFontSize, style, minFontSize) {
            var size = targetFontSize
            // Tight loop is fine: strings are tiny and count is small.
            while (size > minFontSize) {
                val result =
                    measurer.measure(
                        text = text,
                        style = style.copy(fontSize = size),
                        maxLines = 1,
                        constraints =
                            Constraints(
                                maxWidth = maxWidthPx,
                                maxHeight = maxHeightPx ?: Constraints.Infinity,
                            ),
                    )
                // In RTL/Hebrew, glyph overhang can be clipped even when logical width fits.
                // Require BOTH: measured width fits AND engine reports no visual overflow.
                val fitsWidth = result.size.width <= maxWidthPx && !result.hasVisualOverflow
                val fitsHeight = maxHeightPx == null || result.size.height <= maxHeightPx
                if (fitsWidth && fitsHeight) break
                size = (size.value - 1f).sp
            }
            size
        }

    Text(
        text = text,
        maxLines = 1,
        softWrap = false,
        textAlign = textAlign,
        style = style.copy(fontSize = fittedSize),
        modifier = modifier,
    )
}

