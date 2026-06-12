package com.tal.hebrewdino.ui.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompanionDinoRewardCelebration(
    style: CompanionRewardCelebrationStyle,
    isTalking: Boolean,
    companionCharacter: DinoCharacter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val assets = CompanionAssets.forCharacter(companionCharacter)
    val resolvedContentDescription = contentDescription ?: companionCharacter.displayNameHebrew()
    val transition = rememberInfiniteTransition(label = "ch1Reward")
    val bounceY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(420, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "bounce",
    )
    val wiggle by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(280, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "wiggle",
    )
    val sparklePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "sparkle",
    )

    val poseRes =
        when (style) {
            CompanionRewardCelebrationStyle.Happy,
            CompanionRewardCelebrationStyle.Bounce,
            CompanionRewardCelebrationStyle.Wiggle,
            CompanionRewardCelebrationStyle.Sparkle,
            CompanionRewardCelebrationStyle.GrandFinale,
            -> assets.poseHappy
            CompanionRewardCelebrationStyle.Talk -> assets.poseHappy
        }
    val talkDuringVoice =
        style == CompanionRewardCelebrationStyle.Talk && isTalking

    val bounceOffsetDp =
        when (style) {
            CompanionRewardCelebrationStyle.Bounce -> (-18f * bounceY).dp
            CompanionRewardCelebrationStyle.GrandFinale -> (-12f * bounceY).dp
            else -> 0.dp
        }
    val wiggleRotation =
        when (style) {
            CompanionRewardCelebrationStyle.Wiggle -> wiggle * 12f
            else -> 0f
        }
    val celebrationScale =
        when (style) {
            CompanionRewardCelebrationStyle.GrandFinale -> 1f + bounceY * 0.06f
            CompanionRewardCelebrationStyle.Sparkle -> 1f + bounceY * 0.03f
            else -> 1f
        }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        if (style == CompanionRewardCelebrationStyle.Sparkle || style == CompanionRewardCelebrationStyle.GrandFinale) {
            GentleRewardSparkles(
                modifier = Modifier.matchParentSize(),
                intensity =
                    when (style) {
                        CompanionRewardCelebrationStyle.GrandFinale -> 1.75f
                        CompanionRewardCelebrationStyle.Sparkle -> 1.45f
                    },
                phase = sparklePhase,
            )
        }
        CompanionDinoPortrait(
            poseRes = poseRes,
            talkFrameResIds = assets.talkFrameResIds,
            isTalking = talkDuringVoice,
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = bounceOffsetDp)
                    .graphicsLayer {
                        scaleX = celebrationScale
                        scaleY = celebrationScale
                        if (wiggleRotation != 0f) rotationZ = wiggleRotation
                    },
            contentDescription = resolvedContentDescription,
        )
    }
}

@Composable
private fun GentleRewardSparkles(
    modifier: Modifier,
    intensity: Float,
    phase: Float,
) {
    val colors =
        listOf(
            Color(0xFFFFE082),
            Color(0xFFFFF59D),
            Color(0xFF81D4FA),
            Color(0xFFE1BEE7),
        )
    Canvas(modifier = modifier) {
        val count = (10 * intensity).toInt().coerceIn(8, 16)
        for (i in 0 until count) {
            val t = (phase + i * 0.07f) % 1f
            val cx = size.width * (0.2f + (i * 0.61f % 0.6f))
            val cy = size.height * (0.25f + sin((i + t) * 1.7f) * 0.12f)
            val alpha = (0.35f + 0.45f * (1f - kotlin.math.abs(t - 0.5f) * 2f)).coerceIn(0.15f, 0.75f)
            val radius = (4f + (i % 3) * 1.5f) * intensity
            drawCircle(color = colors[i % colors.size].copy(alpha = alpha), radius = radius, center = Offset(cx, cy))
            if (i % 4 == 0) {
                val ang = i * PI.toFloat() / 3f + t * 6.28f
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.5f),
                    radius = 2f,
                    center = Offset(cx + cos(ang) * 14f, cy + sin(ang) * 10f),
                )
            }
        }
    }
}
