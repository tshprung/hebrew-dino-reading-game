package com.tal.hebrewdino.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AnimatedTalkingCharacter(
    idleRes: Int,
    talkFrameResIds: List<Int>,
    isTalking: Boolean,
    modifier: Modifier = Modifier,
    frameMillis: Long = 220,
    contentDescription: String? = null,
) {
    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTalking, talkFrameResIds.size) {
        if (!isTalking || talkFrameResIds.isEmpty()) {
            frameIndex = 0
            return@LaunchedEffect
        }
        while (isActive && isTalking) {
            delay(frameMillis)
            frameIndex = (frameIndex + 1) % talkFrameResIds.size
        }
    }

    val res =
        if (isTalking && talkFrameResIds.isNotEmpty()) {
            talkFrameResIds[frameIndex.coerceIn(0, talkFrameResIds.lastIndex)]
        } else {
            idleRes
        }

    Image(
        painter = painterResource(id = res),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
