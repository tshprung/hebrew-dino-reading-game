package com.tal.hebrewdino.ui.layout

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Edge-to-edge + immersive gameplay: satisfies Android 15 / SDK 35 requirements while hiding
 * system bars during play. Call from [android.app.Activity.onCreate] before [setContent].
 */
fun ComponentActivity.enableImmersiveFullscreen() {
    enableEdgeToEdge()
    applyImmersiveSystemBarsHidden()
}

fun Activity.applyImmersiveSystemBarsHidden() {
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

/**
 * Edge padding for top chrome (back, settings) when drawing under the status bar —
 * keeps controls out of the notch only, without reserving a full status-bar band.
 */
@Composable
fun Modifier.topChromeInsetsPadding(): Modifier =
    windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))

/**
 * Edge padding for bottom controls when transient navigation bars appear (swipe from edge).
 */
@Composable
fun Modifier.bottomChromeInsetsPadding(): Modifier =
    windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout))

/**
 * Full safe-area padding for scrollable / form screens (e.g. settings).
 */
@Composable
fun Modifier.safeContentInsetsPadding(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing)
