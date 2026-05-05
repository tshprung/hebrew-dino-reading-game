package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Episode4Help
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Holds Episode 4 stations 1–5 help UI state: hint lockout, temporary hint letter, and hint epochs.
 * Replay/hint **audio** stays in [GameScreen]; this class only centralizes mutable state + hint timer.
 */
@Stable
class Episode4HelpController internal constructor(
    private val scope: CoroutineScope,
    private val defaultHintRevealMs: Long,
) {
    var hintLocksChoices by mutableStateOf(false)
        private set

    var activeHintLetter by mutableStateOf<String?>(null)
        private set

    var station2BalloonHintEpoch by mutableIntStateOf(0)
        private set

    var station3GridHintEpoch by mutableIntStateOf(0)
        private set

    /** Clears transient hint UI when advancing to the next question (matches GameScreen LaunchedEffect). */
    fun resetForNewQuestion() {
        hintLocksChoices = false
        activeHintLetter = null
    }

    /**
     * רמז: same guards and side effects as legacy `performEpisode4HelpHint` (including epoch bumps).
     * Does not extract target letter — caller passes [letter] from [Episode4Help.targetLetterForHelpHint].
     */
    fun performHint(
        isHelpEnabled: Boolean,
        isPlayPhase: Boolean,
        letter: String?,
        stationId: Int,
        hintDurationMs: Long?,
    ) {
        if (!isHelpEnabled || !isPlayPhase) return
        if (hintLocksChoices) return
        hintLocksChoices = true
        activeHintLetter = letter
        if (stationId == Chapter1StationOrder.BALLOON_POP) {
            station2BalloonHintEpoch++
        }
        if (stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE) {
            station3GridHintEpoch++
        }
        val duration = hintDurationMs ?: defaultHintRevealMs
        scope.launch {
            delay(duration)
            activeHintLetter = null
            hintLocksChoices = false
        }
    }
}

@Composable
fun rememberEpisode4HelpController(
    stationId: Int,
    scope: CoroutineScope,
): Episode4HelpController {
    return remember(stationId) {
        Episode4HelpController(
            scope = scope,
            defaultHintRevealMs = Episode4Help.HINT_REVEAL_FALLBACK_MS,
        )
    }
}
