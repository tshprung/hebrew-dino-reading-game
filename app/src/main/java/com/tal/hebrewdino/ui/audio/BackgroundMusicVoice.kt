package com.tal.hebrewdino.ui.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope

val LocalBackgroundMusic = staticCompositionLocalOf<BackgroundMusicPlayer?> { null }

@Composable
fun ProvideBackgroundMusic(
    player: BackgroundMusicPlayer,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBackgroundMusic provides player, content = content)
}

/** Mutes BGM while [active] is true (ref-counted on the player). */
@Composable
fun BackgroundMusicVoiceDuck(active: Boolean) {
    val bgm = LocalBackgroundMusic.current ?: return
    DisposableEffect(active, bgm) {
        if (active) bgm.beginVoiceDuck()
        onDispose {
            if (active) bgm.endVoiceDuck()
        }
    }
}

suspend fun BackgroundMusicPlayer.withVoiceDuck(block: suspend CoroutineScope.() -> Unit) {
    beginVoiceDuck()
    try {
        kotlinx.coroutines.coroutineScope { block() }
    } finally {
        endVoiceDuck()
    }
}
