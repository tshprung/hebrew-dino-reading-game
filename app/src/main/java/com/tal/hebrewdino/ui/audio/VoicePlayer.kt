package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

class VoicePlayer(context: Context) {
    companion object {
        /** Enable voice playback (assets under `audio/`). */
        const val ENABLED: Boolean = true
        private const val TAG: String = "VoicePlayer"
    }

    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private var player: MediaPlayer? = null
    @Volatile private var activeWaiter: CancellableContinuation<Unit>? = null

    /**
     * Best-effort warm-up for an asset so later playback starts faster.
     *
     * This does NOT play audio and does NOT take the playback mutex.
     * It only touches the asset on a background thread so it is likely cached by the time we need it.
     */
    suspend fun warmUp(assetPath: String) {
        if (!ENABLED) return
        if (assetPath.isBlank()) return
        withContext(Dispatchers.IO) {
            try {
                appContext.assets.openFd(assetPath).close()
            } catch (_: Throwable) {
                Log.w(TAG, "Warm-up failed (missing asset?): $assetPath")
            }
        }
    }

    suspend fun warmUp(vararg assetPaths: String) {
        if (!ENABLED) return
        for (p in assetPaths) warmUp(p)
    }

    /** True if [assetPath] exists under `assets/` (e.g. `audio/vo_choose_letter.wav`). */
    fun hasAsset(assetPath: String): Boolean = exists(assetPath)

    /**
     * Immediately stop any current voice playback.
     *
     * Used for UX: a new tap cancels the previous feedback mid-sentence.
     * Best-effort and non-blocking (does not acquire [mutex]).
     */
    fun stopNow() {
        // IMPORTANT: stopping a MediaPlayer does not guarantee onCompletion/onError fires.
        // If a coroutine is currently waiting inside playBlocking/playSequenceBlocking, unblock it so the mutex isn't held forever.
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

    suspend fun playBlocking(assetPath: String) {
        if (!ENABLED) return
        mutex.withLock {
            stopLocked()
            player = MediaPlayer()

            val success = withContext(Dispatchers.IO) { prepareLocked(assetPath) }
            if (!success) {
                stopLocked()
                return
            }

            val mp = player ?: return
            suspendCancellableCoroutine<Unit> { cont ->
                activeWaiter = cont
                // IMPORTANT: attach listeners BEFORE starting playback.
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

    suspend fun playFirstAvailableBlocking(vararg assetPaths: String) {
        if (!ENABLED) return
        for (p in assetPaths) {
            if (p.isBlank()) continue
            val ok = exists(p)
            if (ok) {
                playBlocking(p)
                return
            }
        }
    }

    /**
     * Plays multiple clips back-to-back as one atomic unit (no overlap with other voice).
     *
     * If the coroutine is cancelled (e.g. user taps again), playback stops immediately.
     */
    suspend fun playSequenceBlocking(vararg assetPaths: String) {
        if (!ENABLED) return
        // Keep the mutex for the whole sequence so nothing else can interleave.
        mutex.withLock {
            for (p in assetPaths) {
                if (p.isBlank()) continue
                val ok = exists(p)
                if (!ok) continue

                stopLocked()
                player = MediaPlayer()

                val success = withContext(Dispatchers.IO) { prepareLocked(p) }
                if (!success) {
                    stopLocked()
                    continue
                }

                val mp = player ?: continue
                suspendCancellableCoroutine<Unit> { cont ->
                    activeWaiter = cont
                    // IMPORTANT: attach listeners BEFORE starting playback.
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
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            mp.prepare()
            afd.close()
            true
        } catch (_: Throwable) {
            Log.w(TAG, "Missing/unplayable voice asset: $assetPath")
            false
        }
}

