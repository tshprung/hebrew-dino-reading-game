package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
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
    companion object {
        private const val TAG: String = "RawVoicePlayer"
        private const val FLAG_DEBUGGABLE: Int = 0x2
    }

    private val appContext = context.applicationContext
    private val isDebuggable: Boolean =
        (appContext.applicationInfo.flags and FLAG_DEBUGGABLE) != 0
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
        } catch (t: Throwable) {
            Log.w(TAG, "stopNow: MediaPlayer.stop failed", t)
        }
        try {
            player?.release()
        } catch (t: Throwable) {
            Log.w(TAG, "stopNow: MediaPlayer.release failed", t)
        }
        player = null
    }

    suspend fun playRawBlocking(@RawRes rawResId: Int) {
        if (rawResId == 0) {
            reportFailure(
                rawResId = rawResId,
                stage = "playRawBlocking called with rawResId=0",
                cause = null,
            )
            return
        }
        mutex.withLock {
            stopLocked()
            val mpResult =
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
                    }
                }
            val mp = mpResult.getOrNull()
            if (mp == null) {
                reportFailure(
                    rawResId = rawResId,
                    stage = "MediaPlayer.create returned null (missing/corrupt raw?)",
                    cause = mpResult.exceptionOrNull(),
                )
                return
            }
            player = mp
            suspendCancellableCoroutine { cont ->
                activeWaiter = cont
                mp.setOnCompletionListener {
                    if (activeWaiter === cont) activeWaiter = null
                    if (cont.isActive) cont.resume(Unit)
                }
                mp.setOnErrorListener { _, what, extra ->
                    reportFailure(
                        rawResId = rawResId,
                        stage = "MediaPlayer.onError what=$what extra=$extra",
                        cause = null,
                    )
                    if (activeWaiter === cont) activeWaiter = null
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                cont.invokeOnCancellation {
                    if (activeWaiter === cont) activeWaiter = null
                    stopLocked()
                }
                val started =
                    runCatching { mp.start() }
                        .onFailure { t ->
                            reportFailure(
                                rawResId = rawResId,
                                stage = "MediaPlayer.start failed",
                                cause = t,
                            )
                            if (activeWaiter === cont) activeWaiter = null
                            if (cont.isActive) cont.resume(Unit)
                        }
                if (started.isFailure) return@suspendCancellableCoroutine
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
        } catch (t: Throwable) {
            Log.w(TAG, "stopLocked: MediaPlayer.stop failed", t)
        }
        try {
            player?.release()
        } catch (t: Throwable) {
            Log.w(TAG, "stopLocked: MediaPlayer.release failed", t)
        }
        player = null
    }

    private fun reportFailure(
        @RawRes rawResId: Int,
        stage: String,
        cause: Throwable?,
    ) {
        val message = "Required raw voice failed. rawResId=$rawResId stage=$stage"
        if (cause != null) {
            Log.e(TAG, message, cause)
        } else {
            Log.e(TAG, message)
        }
        if (isDebuggable) {
            if (cause != null) throw IllegalStateException(message, cause)
            throw IllegalStateException(message)
        }
    }
}
