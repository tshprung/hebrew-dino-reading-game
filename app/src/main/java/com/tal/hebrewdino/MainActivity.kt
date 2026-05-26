package com.tal.hebrewdino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.tal.hebrewdino.ui.AppNav
import com.tal.hebrewdino.ui.AppAnalytics
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.layout.applyImmersiveSystemBarsHidden
import com.tal.hebrewdino.ui.layout.enableImmersiveFullscreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppAnalytics.init(applicationContext)
        TextToSpeechManager.get(applicationContext).warmUp()
        enableImmersiveFullscreen()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val barColor = Color(0xFF0B2B3D)
                Surface(color = barColor) {
                    Box(modifier = Modifier.fillMaxSize().background(barColor)) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val targetAspect = 16f / 9f
                            val currentAspect = maxWidth / maxHeight
                            val contentW =
                                if (currentAspect > targetAspect) {
                                    maxHeight * targetAspect
                                } else {
                                    maxWidth
                                }
                            val contentH =
                                if (currentAspect > targetAspect) {
                                    maxHeight
                                } else {
                                    maxWidth / targetAspect
                                }
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .width(contentW)
                                        .height(contentH)
                                        .background(MaterialTheme.colorScheme.background),
                            ) {
                                AppNav()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveSystemBarsHidden()
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 450)
@Composable
@Suppress("unused")
private fun PreviewApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppNav()
    }
}
