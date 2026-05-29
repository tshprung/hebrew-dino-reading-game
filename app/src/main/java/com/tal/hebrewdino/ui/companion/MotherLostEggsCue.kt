package com.tal.hebrewdino.ui.companion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.layout.ScreenFit

/** Clean upright egg art (no nest/marker background). */
private val LostEggDrawable = R.drawable.egg_white_up

/**
 * Three story eggs grouped near the mother's feet — Ch.1 forest intro.
 * Uses [egg_white_up] with a soft warm glow so they read as important objects.
 */
@Composable
fun MotherLostEggsCue(modifier: Modifier = Modifier) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val eggSize = if (isCompact) 44.dp else 52.dp
    val glowSize = eggSize * 1.5f
    val eggSpacing = if (isCompact) 8.dp else 10.dp
    val rowWidth = eggSize * 3 + eggSpacing * 2

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .width(rowWidth * 0.92f)
                    .height(10.dp)
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF5D4037).copy(alpha = 0.22f),
                                        Color(0xFF5D4037).copy(alpha = 0.28f),
                                        Color(0xFF5D4037).copy(alpha = 0.22f),
                                        Color.Transparent,
                                    ),
                            ),
                        shape = RoundedCornerShape(50),
                    ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(eggSpacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            LostEggWithGlow(eggSize = eggSize, glowSize = glowSize)
            LostEggWithGlow(eggSize = eggSize, glowSize = glowSize)
            LostEggWithGlow(eggSize = eggSize, glowSize = glowSize)
        }
    }
}

@Composable
private fun LostEggWithGlow(
    eggSize: androidx.compose.ui.unit.Dp,
    glowSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .size(glowSize)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFF3C4).copy(alpha = 0.9f),
                                        Color(0xFFFFE082).copy(alpha = 0.4f),
                                        Color.Transparent,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
        )
        Image(
            painter = painterResource(id = LostEggDrawable),
            contentDescription = null,
            modifier = Modifier.size(eggSize),
            contentScale = ContentScale.Fit,
        )
    }
}
