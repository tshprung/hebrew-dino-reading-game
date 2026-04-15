package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

class VoicePlayer(context: Context) {
    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private var player: MediaPlayer? = null

    suspend fun playBlocking(assetPath: String) {
        mutex.withLock {
            stopLocked()
            player = MediaPlayer()

            val success = withContext(Dispatchers.IO) { prepareLocked(assetPath) }
            if (!success) {
                stopLocked()
                return
            }

            val mp = player ?: return
            mp.start()

            suspendCancellableCoroutine<Unit> { cont ->
                mp.setOnCompletionListener {
                    if (cont.isActive) cont.resume(Unit)
                }
                mp.setOnErrorListener { _, _, _ ->
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                cont.invokeOnCancellation { stopLocked() }
            }

            stopLocked()
        }
    }

    suspend fun playFirstAvailableBlocking(vararg assetPaths: String) {
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val ok = exists(p)
            if (ok) {
                playBlocking(p)
                return
            }
        }
    }

    fun release() {
        // Non-suspending; best-effort cleanup.
        try {
            player?.stop()
        } catch (_: Throwable) {
        }
        try {
            player?.release()
        } catch (_: Throwable) {
        }
        player = null
    }

    private fun stopLocked() {
        try {
            player?.stop()
        } catch (_: Throwable) {
        }
        try {
            player?.release()
        } catch (_: Throwable) {
        }
        player = null
    }

    private fun exists(assetPath: String): Boolean =
        try {
            appContext.assets.openFd(assetPath).close()
            true
        } catch (_: IOException) {
            false
        }

    private fun prepareLocked(assetPath: String): Boolean =
        try {
            val afd = appContext.assets.openFd(assetPath)
            val mp = player ?: return false
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mp.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
            mp.prepare()
            afd.close()
            true
        } catch (_: Throwable) {
            false
        }
}

