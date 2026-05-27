package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.audio.InteractionAudio
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryCatalog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PendingAccessorySideGift(
    accessoryId: String,
    onEquipAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = AccessoryCatalog.find(accessoryId) ?: return
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val sfx = remember(appContext) { SfxManager(appContext) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var flying by remember(accessoryId) { mutableStateOf(false) }
    val flyProgress = remember(accessoryId) { Animatable(0f) }
    val pulse =
        rememberInfiniteTransition(label = "accessory_gift_pulse")
            .animateFloat(
                initialValue = 0.94f,
                targetValue = 1.08f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 850, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "gift_scale",
            )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val sideOffsetPx = with(density) { (maxWidth * 0.34f).toPx() }
        val liftPx = with(density) { 24.dp.toPx() }
        val startX = sideOffsetPx
        val endX = 0f
        val xPx = startX + (endX - startX) * flyProgress.value
        val yPx = -liftPx * flyProgress.value

        if (!flying || flyProgress.value < 0.98f) {
            AccessoryGiftBadge(
                emoji = item.emoji,
                scale = if (flying) 1f else pulse.value,
                onClick =
                    if (flying) {
                        {}
                    } else {
                        {
                            InteractionAudio.stopAllNow(appContext)
                            flying = true
                            scope.launch {
                                flyProgress.animateTo(1f, tween(durationMillis = 520))
                                sfx.playFanfare()
                                onEquipAnimationFinished()
                            }
                        }
                    },
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
                        .zIndex(4f),
            )
        }
    }
}

@Composable
private fun AccessoryGiftBadge(
    emoji: String,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .scale(scale)
                .size(92.dp)
                .border(3.dp, Color(0xFFFFE27A), CircleShape),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = emoji,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
