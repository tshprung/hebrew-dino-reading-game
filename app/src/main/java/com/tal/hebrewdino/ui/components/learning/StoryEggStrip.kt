package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R

private val EggImageSize = 44.dp
private val EggStackSpacing = 8.dp
private val EggStripPaddingH = 12.dp
private val EggStripPaddingV = 8.dp

/** Total height of [StoryEggStrip] for a given [foundCount] (vertical stack). */
fun storyEggStripVerticalHeight(foundCount: Int): Dp {
    if (foundCount <= 0) return 0.dp
    val n = foundCount.coerceIn(1, 3)
    return EggStripPaddingV * 2 + EggImageSize * n + EggStackSpacing * (n - 1).coerceAtLeast(0)
}

/**
 * Shows up to three collected story eggs (white → pink → cream), upright, stacked top-to-bottom.
 */
@Composable
fun StoryEggStrip(
    foundCount: Int,
    modifier: Modifier = Modifier,
) {
    if (foundCount <= 0) return
    val n = foundCount.coerceIn(1, 3)
    Column(
        modifier =
            modifier
                .width(EggImageSize + EggStripPaddingH * 2)
                .padding(horizontal = EggStripPaddingH, vertical = EggStripPaddingV),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EggStackSpacing, Alignment.Top),
    ) {
        repeat(n) { index ->
            val res =
                when (index) {
                    0 -> R.drawable.egg_white_up
                    1 -> R.drawable.egg_pink_up
                    else -> R.drawable.egg_purple_up
                }
            Image(
                painter = painterResource(id = res),
                contentDescription = null,
                modifier = Modifier.size(EggImageSize),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
