package com.tal.hebrewdino.ui.layout

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.enableImmersiveFullscreen() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
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
