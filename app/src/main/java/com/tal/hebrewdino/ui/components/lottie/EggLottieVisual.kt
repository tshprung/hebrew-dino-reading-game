package com.tal.hebrewdino.ui.components.lottie

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.IntroMediaClips
import com.tal.hebrewdino.ui.data.DinoCharacter

enum class EggLottiePhase {
    IDLE_WIGGLE,
    CRACK,
}

@Composable
fun EggLottieVisual(
    character: DinoCharacter,
    phase: EggLottiePhase,
    tapImpulseEpoch: Int,
    onEggTapped: () -> Unit,
    onCrackAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp,
) {
    val eggFallbackRes =
        if (character == DinoCharacter.DINA_PINK) {
            R.drawable.egg_pink
        } else {
            R.drawable.egg_white
        }

    val idleComposition by rememberLottieComposition(
        LottieCompositionSpec.Asset(IntroMediaClips.LOTTIE_EGG_IDLE_WIGGLE),
    )
    val crackComposition by rememberLottieComposition(
        LottieCompositionSpec.Asset(IntroMediaClips.LOTTIE_EGG_CRACK),
    )

    val tapScale = remember { Animatable(1f) }
    LaunchedEffect(tapImpulseEpoch) {
        if (tapImpulseEpoch == 0) return@LaunchedEffect
        tapScale.snapTo(1f)
        tapScale.animateTo(1.14f, tween(55))
        tapScale.animateTo(0.92f, tween(75))
        tapScale.animateTo(1.06f, tween(65))
        tapScale.animateTo(1f, tween(70))
    }

    val idleProgress by animateLottieCompositionAsState(
        composition = idleComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = phase == EggLottiePhase.IDLE_WIGGLE && idleComposition != null,
    )

    val crackProgress by animateLottieCompositionAsState(
        composition = crackComposition,
        iterations = 1,
        isPlaying = phase == EggLottiePhase.CRACK && crackComposition != null,
        restartOnPlay = true,
    )

    var crackEndReported by remember { mutableStateOf(false) }
    LaunchedEffect(phase, crackProgress, crackComposition) {
        if (phase != EggLottiePhase.CRACK) {
            crackEndReported = false
            return@LaunchedEffect
        }
        if (crackComposition == null) {
            if (!crackEndReported) {
                crackEndReported = true
                onCrackAnimationEnd()
            }
            return@LaunchedEffect
        }
        if (!crackEndReported && crackProgress >= 0.98f) {
            crackEndReported = true
            onCrackAnimationEnd()
        }
    }

    Box(
        modifier =
            modifier
                .size(size)
                .graphicsLayer {
                    scaleX = tapScale.value
                    scaleY = tapScale.value
                }
                .clickable(enabled = phase != EggLottiePhase.CRACK, onClick = onEggTapped),
        contentAlignment = Alignment.Center,
    ) {
        when (phase) {
            EggLottiePhase.IDLE_WIGGLE -> {
                if (idleComposition != null) {
                    LottieAnimation(
                        composition = idleComposition,
                        progress = { idleProgress },
                        modifier = Modifier.size(size),
                    )
                } else {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = eggFallbackRes),
                        contentDescription = "ביצה",
                        modifier = Modifier.size(size),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            EggLottiePhase.CRACK -> {
                if (crackComposition != null) {
                    LottieAnimation(
                        composition = crackComposition,
                        progress = { crackProgress },
                        modifier = Modifier.size(size),
                    )
                } else {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = eggFallbackRes),
                        contentDescription = "ביצה נסדקת",
                        modifier = Modifier.size(size),
                        contentScale = ContentScale.Fit,
                    )
                    LaunchedEffect(Unit) {
                        if (!crackEndReported) {
                            crackEndReported = true
                            onCrackAnimationEnd()
                        }
                    }
                }
            }
        }
    }
}
