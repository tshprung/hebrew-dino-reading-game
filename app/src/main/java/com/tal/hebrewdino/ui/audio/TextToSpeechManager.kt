package com.tal.hebrewdino.ui.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

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

    private val appContext: Context = context.applicationContext
    private val ready: AtomicBoolean = AtomicBoolean(false)
    @Volatile private var tts: TextToSpeech? = null
    @Volatile private var pending: String? = null
    private val waiters: ConcurrentHashMap<String, () -> Unit> = ConcurrentHashMap()

    init {
        tts =
            TextToSpeech(appContext) { status ->
                if (status != TextToSpeech.SUCCESS) return@TextToSpeech
                val engine = tts ?: return@TextToSpeech
                engine.setSpeechRate(0.95f)
                engine.setPitch(1.05f)
                engine.language = Locale.forLanguageTag("he")
                engine.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {}

                        override fun onDone(utteranceId: String) {
                            waiters.remove(utteranceId)?.invoke()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String) {
                            waiters.remove(utteranceId)?.invoke()
                        }

                        override fun onError(
                            utteranceId: String,
                            errorCode: Int,
                        ) {
                            waiters.remove(utteranceId)?.invoke()
                        }
                    },
                )
                ready.set(true)
                val toSpeak = pending
                pending = null
                if (!toSpeak.isNullOrBlank()) {
                    engine.stop()
                    engine.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
                }
            }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (!ready.get()) {
            pending = text
            return
        }
        val engine = tts ?: return
        engine.stop()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }

    suspend fun speakAndWait(
        text: String,
        timeoutMs: Long = 2200L,
    ) {
        if (text.isBlank()) return
        if (!ready.get()) {
            pending = text
            return
        }
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
                    }
                    engine.stop()
                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                }
            }
        } catch (_: TimeoutCancellationException) {
            waiters.remove(utteranceId)
        }
    }

    fun warmUp() {
        if (ready.get()) {
            val engine = tts ?: return
            engine.speak(WarmupToken, TextToSpeech.QUEUE_FLUSH, null, "tts_warmup_${System.currentTimeMillis()}")
            return
        }
        if (pending == null) {
            pending = WarmupToken
        }
    }

    fun stop() {
        waiters.clear()
        tts?.stop()
    }

    fun shutdown() {
        val engine = tts
        tts = null
        ready.set(false)
        engine?.stop()
        engine?.shutdown()
    }
}
