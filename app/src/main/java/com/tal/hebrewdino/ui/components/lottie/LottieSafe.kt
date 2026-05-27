package com.tal.hebrewdino.ui.components.lottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Loads a Lottie asset once; returns null on failure so callers can show a static fallback
 * without spinning failed compositions or tight progress-driven effects.
 */
@Composable
fun rememberSafeLottieComposition(assetPath: String): LottieComposition? {
    val result by rememberLottieComposition(LottieCompositionSpec.Asset(assetPath))
    return result
}

fun LottieComposition?.durationMsOr(defaultMs: Long = 600L): Long {
    if (this == null) return defaultMs
    val seconds = duration.takeIf { it.isFinite() && it > 0.0 } ?: return defaultMs
    return (seconds * 1000.0).toLong().coerceIn(200L, 5_000L)
}
