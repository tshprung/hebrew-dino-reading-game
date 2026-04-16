package com.tal.hebrewdino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import com.tal.hebrewdino.ui.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNav()
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 450)
@Composable
private fun PreviewApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppNav()
    }
}

