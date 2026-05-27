package com.tal.hebrewdino.ui.components.lottie

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.DinoMediaClips
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.screens.DinoAccessoryOverlay
import kotlinx.coroutines.delay

enum class DinoLottiePlayback {
    IDLE,
    HUNGRY,
    EATING,
}

@Composable
fun DinoLottieVisual(
    character: DinoCharacter,
    isHungry: Boolean,
    eatPulseEpoch: Int,
    growEpoch: Int,
    equippedAccessoryId: String?,
    onDinoTapped: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp,
) {
    val fallbackRes = R.drawable.dino_idle
    val contentDescription = if (character == DinoCharacter.DINA_PINK) "דינה" else "דינו"

    val idleComposition = rememberSafeLottieComposition(DinoMediaClips.LOTTIE_DINO_IDLE)
    val hungryComposition = rememberSafeLottieComposition(DinoMediaClips.LOTTIE_DINO_HUNGRY)
    val eatComposition = rememberSafeLottieComposition(DinoMediaClips.LOTTIE_DINO_EAT)

    var playback by remember { mutableStateOf(if (isHungry) DinoLottiePlayback.HUNGRY else DinoLottiePlayback.IDLE) }

    LaunchedEffect(isHungry) {
        if (playback != DinoLottiePlayback.EATING) {
            playback = if (isHungry) DinoLottiePlayback.HUNGRY else DinoLottiePlayback.IDLE
        }
    }

    LaunchedEffect(eatPulseEpoch) {
        if (eatPulseEpoch == 0) return@LaunchedEffect
        playback = DinoLottiePlayback.EATING
    }

    val growScale = remember { Animatable(1f) }
    LaunchedEffect(growEpoch) {
        if (growEpoch == 0) return@LaunchedEffect
        growScale.snapTo(0.86f)
        growScale.animateTo(1f, tween(260))
    }

    LaunchedEffect(playback, eatComposition, isHungry) {
        if (playback != DinoLottiePlayback.EATING) return@LaunchedEffect
        if (eatComposition == null) {
            playback = if (isHungry) DinoLottiePlayback.HUNGRY else DinoLottiePlayback.IDLE
            return@LaunchedEffect
        }
        delay(eatComposition.durationMsOr())
        if (playback == DinoLottiePlayback.EATING) {
            playback = if (isHungry) DinoLottiePlayback.HUNGRY else DinoLottiePlayback.IDLE
        }
    }

    val idleProgress by animateLottieCompositionAsState(
        composition = idleComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = playback == DinoLottiePlayback.IDLE && idleComposition != null,
    )
    val hungryProgress by animateLottieCompositionAsState(
        composition = hungryComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = playback == DinoLottiePlayback.HUNGRY && hungryComposition != null,
    )
    val eatProgress by animateLottieCompositionAsState(
        composition = eatComposition,
        iterations = 1,
        isPlaying = playback == DinoLottiePlayback.EATING && eatComposition != null,
        restartOnPlay = true,
    )

    Box(
        modifier =
            modifier
                .size(size)
                .graphicsLayer {
                    scaleX = growScale.value
                    scaleY = growScale.value
                }
                .clickable(onClick = onDinoTapped),
        contentAlignment = Alignment.Center,
    ) {
        val compositionForPlayback =
            when (playback) {
                DinoLottiePlayback.IDLE -> idleComposition
                DinoLottiePlayback.HUNGRY -> hungryComposition
                DinoLottiePlayback.EATING -> eatComposition
            }
        if (compositionForPlayback != null) {
            val progressForPlayback =
                when (playback) {
                    DinoLottiePlayback.IDLE -> idleProgress
                    DinoLottiePlayback.HUNGRY -> hungryProgress
                    DinoLottiePlayback.EATING -> eatProgress
                }
            LottieAnimation(
                composition = compositionForPlayback,
                progress = { progressForPlayback },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = fallbackRes),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
        DinoAccessoryOverlay(
            equippedAccessoryId = equippedAccessoryId,
            dinoSize = size,
        )
    }
}
