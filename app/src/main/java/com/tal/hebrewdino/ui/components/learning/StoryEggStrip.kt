package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R

/**
 * Shows up to three found eggs (white → pink → purple) at the top of story screens.
 */
@Composable
fun StoryEggStrip(
    foundCount: Int,
    modifier: Modifier = Modifier,
) {
    if (foundCount <= 0) return
    val n = foundCount.coerceIn(1, 3)
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp).height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        repeat(n) { index ->
            val res =
                when (index) {
                    0 -> R.drawable.egg_white
                    1 -> R.drawable.egg_pink
                    else -> R.drawable.egg_purple
                }
            Image(
                painter = painterResource(id = res),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
