package com.tal.hebrewdino.ui.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Subtle breathing / bob for static companion story characters during narration. */
@Composable
fun CompanionGentleIdleMotion(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "companionGentleIdle")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "phase",
    )
    val breathe = if (active) phase else 0f
    val scale = 1f + breathe * 0.028f
    val offsetY = (-3f * breathe).dp

    Box(
        modifier =
            modifier
                .offset(y = offsetY)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
    ) {
        content()
    }
}
