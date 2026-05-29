package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Stable companion Dino slot: fixed box, bottom-centered [ContentScale.Fit], gentle talk swap.
 */
@Composable
fun CompanionDinoPortrait(
    @DrawableRes poseRes: Int,
    modifier: Modifier = Modifier,
    talkFrameResIds: List<Int> = emptyList(),
    isTalking: Boolean = false,
    contentDescription: String? = null,
    frameMillis: Long = 380L,
) {
    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTalking, talkFrameResIds.size) {
        if (!isTalking || talkFrameResIds.isEmpty()) {
            frameIndex = 0
            return@LaunchedEffect
        }
        while (isActive) {
            delay(frameMillis)
            frameIndex = (frameIndex + 1) % talkFrameResIds.size
        }
    }

    val imageRes =
        if (isTalking && talkFrameResIds.isNotEmpty()) {
            talkFrameResIds[frameIndex.coerceIn(0, talkFrameResIds.lastIndex)]
        } else {
            poseRes
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}
