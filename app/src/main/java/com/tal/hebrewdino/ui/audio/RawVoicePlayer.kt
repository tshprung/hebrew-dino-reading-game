package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Short voice lines from `res/raw` (companion pilot clips). */
class RawVoicePlayer(context: Context) {
    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private var player: MediaPlayer? = null

    @Volatile
    private var activeWaiter: CancellableContinuation<Unit>? = null

    fun stopNow() {
        val waiter = activeWaiter
        if (waiter != null && waiter.isActive) {
            runCatching { waiter.resume(Unit) }
        }
        activeWaiter = null
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

    suspend fun playRawBlocking(@RawRes rawResId: Int) {
        if (rawResId == 0) return
        mutex.withLock {
            stopLocked()
            val mp =
                withContext(Dispatchers.IO) {
                    runCatching {
                        MediaPlayer.create(appContext, rawResId)?.apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .build(),
                            )
                        }
                    }.getOrNull()
                } ?: return
            player = mp
            suspendCancellableCoroutine { cont ->
                activeWaiter = cont
                mp.setOnCompletionListener {
                    if (activeWaiter === cont) activeWaiter = null
                    if (cont.isActive) cont.resume(Unit)
                }
                mp.setOnErrorListener { _, _, _ ->
                    if (activeWaiter === cont) activeWaiter = null
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                cont.invokeOnCancellation {
                    if (activeWaiter === cont) activeWaiter = null
                    stopLocked()
                }
                mp.start()
            }
            stopLocked()
        }
    }

    fun release() {
        stopNow()
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
}
