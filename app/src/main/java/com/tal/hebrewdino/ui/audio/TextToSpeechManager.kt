package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.math.max

class TextToSpeechManager(context: Context) {
    companion object {
        private const val WarmupToken: String = "\u2060"
        @Volatile private var instance: TextToSpeechManager? = null

        fun get(context: Context): TextToSpeechManager {
            val existing = instance
            if (existing != null) return existing
            return synchronized(this) {
                val again = instance
                if (again != null) return@synchronized again
                val created = TextToSpeechManager(context.applicationContext)
                instance = created
                created
            }
        }
    }

    private data class QueuedSpeech(
        val text: String,
        val queueMode: Int,
        val utteranceId: String,
        val onDone: (() -> Unit)?,
    )

    private val appContext: Context = context.applicationContext
    private val ready: AtomicBoolean = AtomicBoolean(false)
    @Volatile private var tts: TextToSpeech? = null
    private val preReadyQueue: ConcurrentLinkedQueue<QueuedSpeech> = ConcurrentLinkedQueue()
    private val waiters: ConcurrentHashMap<String, () -> Unit> = ConcurrentHashMap()
    private val speakMutex = Mutex()
    private val _isSpeaking = MutableStateFlow(false)

    val isReady: Boolean get() = ready.get()
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts =
            TextToSpeech(appContext) { status ->
                if (status != TextToSpeech.SUCCESS) return@TextToSpeech
                val engine = tts ?: return@TextToSpeech
                engine.setSpeechRate(0.92f)
                engine.setPitch(1.05f)
                engine.language = Locale.forLanguageTag("he")
                engine.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String) {
                            _isSpeaking.value = false
                            waiters.remove(utteranceId)?.invoke()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String) {
                            _isSpeaking.value = false
                            waiters.remove(utteranceId)?.invoke()
                        }

                        override fun onError(
                            utteranceId: String,
                            errorCode: Int,
                        ) {
                            _isSpeaking.value = false
                            waiters.remove(utteranceId)?.invoke()
                        }
                    },
                )
                ready.set(true)
                drainPreReadyQueue()
            }
    }

    suspend fun awaitReady(timeoutMs: Long = 8000L) {
        if (ready.get()) return
        withTimeout(timeoutMs) {
            while (!ready.get()) {
                delay(25)
            }
        }
    }

    /**
     * Primary entry for screen / round instructions. Waits for engine init, lets navigation
     * transitions finish, then speaks the full phrase without [stop] (avoids cross-screen clipping).
     */
    suspend fun speakFully(
        text: String,
        navigationSettleMs: Long = 350L,
    ) {
        if (text.isBlank()) return
        speakMutex.withLock {
            awaitReady()
            delay(navigationSettleMs)
            val timeoutMs = estimateTimeoutMs(text)
            speakAndWaitUnlocked(text, timeoutMs)
        }
    }

    suspend fun speakFullyThen(
        text: String,
        navigationSettleMs: Long = 200L,
        onDone: () -> Unit,
    ) {
        if (text.isBlank()) return
        speakMutex.withLock {
            awaitReady()
            delay(navigationSettleMs)
            val utteranceId = "tts_${System.currentTimeMillis()}"
            val engine = tts ?: return@withLock
            suspendCancellableCoroutine<Unit> { cont ->
                waiters[utteranceId] = {
                    if (cont.isActive) cont.resumeWith(Result.success(Unit))
                    onDone()
                }
                cont.invokeOnCancellation { waiters.remove(utteranceId) }
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        }
    }

    fun speakFeedback(text: String) {
        if (text.isBlank()) return
        schedule(text, TextToSpeech.QUEUE_ADD, null)
    }

    fun interruptAndSpeak(text: String) {
        if (text.isBlank()) return
        waiters.clear()
        _isSpeaking.value = false
        tts?.stop()
        schedule(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    suspend fun interruptAndSpeakFully(text: String) {
        if (text.isBlank()) return
        speakMutex.withLock {
            waiters.clear()
            _isSpeaking.value = false
            tts?.stop()
            awaitReady()
            speakAndWaitUnlocked(text, estimateTimeoutMs(text))
        }
    }

    fun speak(text: String) {
        schedule(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    fun speak(
        text: String,
        onDone: () -> Unit,
    ) {
        if (text.isBlank()) return
        schedule(text, TextToSpeech.QUEUE_FLUSH, onDone)
    }

    @Deprecated("Use speakFully in LaunchedEffect", ReplaceWith("speakFully(text)"))
    fun speakInstruction(text: String) {
        if (text.isBlank()) return
        schedule(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    fun clearInstructionDedupe() {
        // no-op — dedupe removed; kept for call-site compatibility
    }

    private fun schedule(
        text: String,
        queueMode: Int,
        onDone: (() -> Unit)?,
    ) {
        val utteranceId = "tts_${System.currentTimeMillis()}"
        val item = QueuedSpeech(text, queueMode, utteranceId, onDone)
        if (!ready.get()) {
            if (text != WarmupToken) {
                preReadyQueue.add(item)
            }
            return
        }
        playQueued(item)
    }

    private fun drainPreReadyQueue() {
        val pending = mutableListOf<QueuedSpeech>()
        while (true) {
            val next = preReadyQueue.poll() ?: break
            pending.add(next)
        }
        val real = pending.filter { it.text != WarmupToken }
        val toPlay = if (real.isNotEmpty()) real else pending
        toPlay.forEach { playQueued(it) }
    }

    private fun playQueued(item: QueuedSpeech) {
        if (item.text == WarmupToken) return
        val engine = tts ?: return
        item.onDone?.let { waiters[item.utteranceId] = it }
        engine.speak(item.text, item.queueMode, null, item.utteranceId)
    }

    suspend fun speakAndWait(
        text: String,
        timeoutMs: Long = 6000L,
    ) {
        if (text.isBlank()) return
        speakMutex.withLock {
            awaitReady()
            delay(350)
            speakAndWaitUnlocked(text, timeoutMs)
        }
    }

    private suspend fun speakAndWaitUnlocked(
        text: String,
        timeoutMs: Long,
    ) {
        if (text.isBlank()) return
        val engine = tts ?: return
        val utteranceId = "tts_${System.currentTimeMillis()}"
        try {
            withTimeout(timeoutMs) {
                suspendCancellableCoroutine<Unit> { cont ->
                    waiters[utteranceId] = {
                        if (cont.isActive) cont.resumeWith(Result.success(Unit))
                    }
                    cont.invokeOnCancellation {
                        waiters.remove(utteranceId)
                        engine.stop()
                    }
                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                }
            }
        } catch (_: TimeoutCancellationException) {
            waiters.remove(utteranceId)
        }
    }

    private fun estimateTimeoutMs(text: String): Long {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size.coerceAtLeast(1)
        return max(2800L, words * 900L + text.length * 80L).coerceAtMost(14000L)
    }

    fun warmUp() {
        if (ready.get()) {
            schedule(WarmupToken, TextToSpeech.QUEUE_ADD, null)
        }
    }

    /** Call only from app lifecycle (ON_STOP), not from Compose screen dispose. */
    fun stop() {
        waiters.clear()
        preReadyQueue.clear()
        _isSpeaking.value = false
        tts?.stop()
    }

    fun shutdown() {
        val engine = tts
        tts = null
        ready.set(false)
        preReadyQueue.clear()
        waiters.clear()
        _isSpeaking.value = false
        engine?.stop()
        engine?.shutdown()
    }
}
