package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Episode4Help
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationReplayMode
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

internal object SideHelpActions {
    fun startReplay(
        audioEnabled: Boolean,
        isPlayPhase: Boolean,
        episode4HelpEnabled: Boolean,
        popBalloonsHelpEnabled: Boolean,
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
        stationUiSpec: StationUiSpec,
        session: LevelSession,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        cancelFeedbackVoice: () -> Unit,
        stopStagingSfx: (stopAllStreams: Boolean) -> Unit,
        scope: CoroutineScope,
    ): Job? {
        if (!isPlayPhase) return null
        if (episode4HelpEnabled) {
            if (!audioEnabled) return null
            val q = session.currentQuestion ?: return null
            cancelFeedbackVoice()
            sfx.stopAllStreams()
            stopStagingSfx(false)
            return scope.launch {
                if (plan.highlightedLetterInWordPickLetter && q is Question.PopBalloonsQuestion) {
                    val round = session.highlightedLetterInWordRound() ?: return@launch
                    val wordPath = AudioClips.wordClipByCatalogId(round.catalogId)
                    val letterClip = AudioClips.letterNameClip(round.correctLetter)
                    val parts =
                        buildList {
                            if (voice.hasAsset(wordPath)) add(wordPath)
                            if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
                        }
                    if (parts.isNotEmpty()) {
                        voice.playSequenceBlocking(*parts.toTypedArray())
                    } else {
                        if (letterClip != null) speakLetterPrompt(voice, round.correctLetter)
                    }
                    return@launch
                }
                if (plan.popAllLettersInWord && q is Question.PopBalloonsQuestion) {
                    val catalogId = session.chapter3PopAllLettersCurrentWord()?.second
                    if (!catalogId.isNullOrBlank()) {
                        val wordPath = AudioClips.wordClipByCatalogId(catalogId)
                        if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                    }
                    return@launch
                }
                when (stationUiSpec.replayMode) {
                    StationReplayMode.TargetLetterOnly -> {
                        val letter = Episode4Help.targetLetterForHelpHint(q) ?: return@launch
                        val letterClip = AudioClips.letterNameClip(letter)
                        if (letterClip != null) {
                            val id = sfx.playReturningStreamId(letterClip, volume = 1f)
                            if (id != null) return@launch
                            if (voice.hasAsset(letterClip)) {
                                voice.playBlocking(letterClip)
                                return@launch
                            }
                        }
                        if (chapterId == TrainingV1Config.CHAPTER_ID) return@launch
                        speakLetterPrompt(voice, letter)
                    }
                    StationReplayMode.TargetWordOnly -> {
                        if (q is Question.PictureStartsWithQuestion) {
                            val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                            if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                        }
                    }
                    else -> replayEpisode4Stations15RoundAudio(sfx = sfx, voice = voice, stationId = stationId, q = q)
                }
            }
        }
        if (popBalloonsHelpEnabled) {
            if (!audioEnabled) return null
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return null
            cancelFeedbackVoice()
            val letter = q.correctAnswer
            val letterClip = AudioClips.letterNameClip(letter)
            return scope.launch {
                if (letterClip != null && voice.hasAsset(letterClip)) {
                    voice.playBlocking(letterClip)
                    return@launch
                }
                speakLetterPrompt(voice, letter)
            }
        }
        return null
    }

    fun performHint(
        isPlayPhase: Boolean,
        episode4HelpEnabled: Boolean,
        popBalloonsHelpEnabled: Boolean,
        stationId: Int,
        stationUiSpec: StationUiSpec,
        session: LevelSession,
        gameViewModel: GameViewModel,
        scope: CoroutineScope,
    ) {
        if (!isPlayPhase) return
        if (episode4HelpEnabled) {
            val q = session.currentQuestion ?: return
            val letter = Episode4Help.targetLetterForHelpHint(q)
            if (gameViewModel.episode4HelpLocksChoices) return
            gameViewModel.episode4HelpLocksChoices = true
            gameViewModel.episode4HelpActiveHintLetter = letter
            if (stationId == Chapter1StationOrder.BALLOON_POP) {
                gameViewModel.episode4Station2BalloonHintEpoch += 1
            }
            if (stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE) {
                gameViewModel.episode4Station3GridHintEpoch += 1
            }
            val duration = stationUiSpec.hintDurationMs ?: Episode4Help.HINT_REVEAL_FALLBACK_MS
            gameViewModel.episode4HelpClearJob?.cancel()
            gameViewModel.episode4HelpClearJob =
                scope.launch {
                    delay(duration)
                    gameViewModel.episode4HelpActiveHintLetter = null
                    gameViewModel.episode4HelpLocksChoices = false
                    gameViewModel.episode4HelpClearJob = null
                }
            return
        }
        if (popBalloonsHelpEnabled) {
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return
            if (gameViewModel.balloonHelpLocksChoices) return
            gameViewModel.balloonHelpLocksChoices = true
            gameViewModel.balloonHelpHintLetter = q.correctAnswer
            gameViewModel.balloonHelpClearJob?.cancel()
            gameViewModel.balloonHelpClearJob =
                scope.launch {
                    delay(Episode4Help.HINT_REVEAL_FALLBACK_MS)
                    gameViewModel.balloonHelpHintLetter = null
                    gameViewModel.balloonHelpLocksChoices = false
                    gameViewModel.balloonHelpClearJob = null
                }
        }
    }
}
