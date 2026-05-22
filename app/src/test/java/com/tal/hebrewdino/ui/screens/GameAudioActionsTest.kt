package com.tal.hebrewdino.ui.screens

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameAudioActionsTest {
    @Test
    fun launchFeedbackVoiceNoCancel_tracksJob_whilePlaying_thenClearsOnCompletion() = runBlocking {
        val runtime = GameAudioRuntimeState()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val gate = CompletableDeferred<Unit>()

        val job =
            GameAudioActions.launchFeedbackVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = runtime,
            ) { gate.await() }

        assertNotNull(job)
        assertTrue(runtime.feedbackVoiceJob === job)

        gate.complete(Unit)
        job!!.join()
        repeat(10) { yield() }

        assertNull(runtime.feedbackVoiceJob)
        scope.cancel()
    }

    @Test
    fun launchFeedbackVoiceNoCancel_oldCompletionDoesNotClearNewJob() = runBlocking {
        val runtime = GameAudioRuntimeState()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val gate1 = CompletableDeferred<Unit>()
        val gate2 = CompletableDeferred<Unit>()

        val job1 =
            GameAudioActions.launchFeedbackVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = runtime,
            ) { gate1.await() }

        val job2 =
            GameAudioActions.launchFeedbackVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = runtime,
            ) { gate2.await() }

        assertNotNull(job1)
        assertNotNull(job2)
        assertTrue(runtime.feedbackVoiceJob === job2)

        gate1.complete(Unit)
        job1!!.join()
        repeat(10) { yield() }

        assertTrue(runtime.feedbackVoiceJob === job2)

        gate2.complete(Unit)
        job2!!.join()
        repeat(10) { yield() }

        assertNull(runtime.feedbackVoiceJob)
        scope.cancel()
    }

    @Test
    fun launchPromptVoiceNoCancel_tracksJob_whilePlaying_thenClearsOnCompletion() = runBlocking {
        val runtime = GameAudioRuntimeState()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val gate = CompletableDeferred<Unit>()

        val job =
            GameAudioActions.launchPromptVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = runtime,
            ) { gate.await() }

        assertNotNull(job)
        assertTrue(runtime.promptVoiceJob === job)

        gate.complete(Unit)
        job!!.join()
        repeat(10) { yield() }

        assertNull(runtime.promptVoiceJob)
        scope.cancel()
    }
}

