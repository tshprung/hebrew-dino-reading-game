package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.WeakHashMap
import kotlin.coroutines.resume

/**
 * Plays short narrator clips from `res/raw` with [SpeechFocusGate] (pure-focus BGM mute).
 */
class RawVoicePlayer(context: Context) {
    companion object {
        private const val TAG = "RawVoicePlayer"
        private const val MIN_PLAYABLE_BYTES = 256L
        private val registry: MutableSet<RawVoicePlayer> =
            Collections.newSetFromMap(WeakHashMap())

        fun stopAllNow() {
            val snapshot = synchronized(registry) { registry.toList() }
            for (player in snapshot) {
                player.stopNow()
            }
        }
    }

    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private var player: MediaPlayer? = null

    @Volatile
    private var activeWaiter: CancellableContinuation<Unit>? = null

    init {
        synchronized(registry) { registry.add(this) }
    }

    fun isPlayable(
        @RawRes resId: Int,
    ): Boolean {
        if (resId == 0) return false
        val length = rawResourceLength(resId) ?: return false
        return length >= MIN_PLAYABLE_BYTES
    }

    fun rawResourceLength(
        @RawRes resId: Int,
    ): Long? {
        if (resId == 0) return null
        return try {
            appContext.resources.openRawResourceFd(resId).use { it.length }
        } catch (_: Throwable) {
            null
        }
    }

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

    /**
     * Short feedback cue without [SpeechFocusGate] so background music stays audible.
     */
    suspend fun playSoftFeedback(
        @RawRes resId: Int,
        volume: Float = 0.42f,
    ) {
        if (!isPlayable(resId)) {
            Log.w(TAG, "Skipping missing or empty soft feedback: resId=$resId")
            return
        }
        mutex.withLock {
            stopLocked()
            val mp =
                withContext(Dispatchers.IO) {
                    runCatching { MediaPlayer.create(appContext, resId) }.getOrNull()
                } ?: return@withLock
            player = mp
            val vol = volume.coerceIn(0.05f, 1f)
            mp.setVolume(vol, vol)
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
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

    suspend fun playBlocking(
        @RawRes resId: Int,
    ) {
        if (!isPlayable(resId)) {
            Log.w(TAG, "Skipping missing or empty raw clip: resId=$resId")
            return
        }
        mutex.withLock {
            stopLocked()
            val mp =
                withContext(Dispatchers.IO) {
                    runCatching { MediaPlayer.create(appContext, resId) }.getOrNull()
                } ?: return@withLock
            player = mp
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            SpeechFocusGate.begin()
            try {
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
            } finally {
                stopLocked()
                SpeechFocusGate.end()
            }
        }
    }

    suspend fun playSequenceBlocking(vararg resIds: Int) {
        val playable = resIds.filter { isPlayable(it) }
        if (playable.isEmpty()) return
        mutex.withLock {
            SpeechFocusGate.begin()
            try {
                for (resId in playable) {
                    playOneLocked(resId)
                }
            } finally {
                stopLocked()
                SpeechFocusGate.end()
            }
        }
    }

    private suspend fun playOneLocked(
        @RawRes resId: Int,
    ) {
        stopLocked()
        val mp =
            withContext(Dispatchers.IO) {
                runCatching { MediaPlayer.create(appContext, resId) }.getOrNull()
            } ?: return
        player = mp
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
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

    fun release() {
        stopNow()
        synchronized(registry) { registry.remove(this) }
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
