package com.tal.hebrewdino.ui.feedback

import android.view.HapticFeedbackConstants
import android.view.View
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified audio + light haptics for kid-facing stations. Visuals stay in composables
 * (scale, shake, sparkles) triggered alongside these calls.
 */
class GameFeedback(
    private val scope: CoroutineScope,
    private val sfx: SoundPoolPlayer,
    private val view: View?,
) {
    fun playCorrect() {
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        scope.launch {
            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
        }
    }

    fun playWrong() {
        view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        scope.launch {
            sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.36f)
        }
    }

    /** End-of-question / station climax: louder SFX + short stagger (confetti handled in UI). */
    fun playSuccessBig() {
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        scope.launch {
            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.84f)
            delay(95)
            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.52f)
        }
    }
}
