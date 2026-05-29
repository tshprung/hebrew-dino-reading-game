package com.tal.hebrewdino.ui.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.layout.ScreenFit

/** Found egg with gentle pulsing glow — Ch.1 chapter finale. */
@Composable
fun FoundEggCelebration(
    modifier: Modifier = Modifier,
    eggSize: Dp? = null,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val resolvedEggSize = eggSize ?: if (isCompact) 100.dp else 128.dp
    val transition = rememberInfiniteTransition(label = "foundEggGlow")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse",
    )
    val glowSize = resolvedEggSize * 1.65f * pulse

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .size(glowSize)
                    .graphicsLayer { alpha = 0.9f }
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFF9C4).copy(alpha = 0.95f),
                                        Color(0xFFFFE082).copy(alpha = 0.5f),
                                        Color(0xFFFFB74D).copy(alpha = 0.12f),
                                        Color.Transparent,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .size(resolvedEggSize * 1.12f)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFFDE7).copy(alpha = 0.7f),
                                        Color.Transparent,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.egg_found),
            contentDescription = null,
            modifier = Modifier.size(resolvedEggSize),
            contentScale = ContentScale.Fit,
        )
    }
}
