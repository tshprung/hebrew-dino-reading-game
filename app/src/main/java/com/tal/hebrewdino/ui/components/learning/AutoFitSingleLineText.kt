package com.tal.hebrewdino.ui.components.learning

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun AutoFitSingleLineText(
    text: String,
    maxWidth: Dp,
    targetFontSize: TextUnit,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 12.sp,
    textAlign: TextAlign = TextAlign.Center,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxWidthPx = with(density) { maxWidth.toPx().roundToInt() }.coerceAtLeast(1)
    val fittedSize =
        remember(text, maxWidthPx, targetFontSize, style, minFontSize) {
            var size = targetFontSize
            // Tight loop is fine: strings are tiny and count is small.
            while (size > minFontSize) {
                val result =
                    measurer.measure(
                        text = text,
                        style = style.copy(fontSize = size),
                        maxLines = 1,
                        constraints = Constraints(maxWidth = maxWidthPx),
                    )
                // Prefer deterministic width check; hasVisualOverflow can be conservative across text engines.
                if (result.size.width <= maxWidthPx) break
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

