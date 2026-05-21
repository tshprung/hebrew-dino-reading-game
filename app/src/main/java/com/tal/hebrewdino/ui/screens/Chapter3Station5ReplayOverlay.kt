package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.Chapter3Station5ReplayColumn
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun Chapter3Station5ReplayOverlay(
    chapterId: Int,
    stationId: Int,
    episode4HelpSt15: Boolean,
    phase: GamePhase,
    inputLocked: Boolean,
    audioEnabled: Boolean,
    session: LevelSession,
    scope: CoroutineScope,
    voice: VoicePlayer,
    cancelFeedbackVoice: () -> Unit,
    audioRuntime: GameAudioRuntimeState,
    modifier: Modifier = Modifier,
) {
    if (!((chapterId == 3 || chapterId == 6) && stationId == 5 && !episode4HelpSt15)) return
    val replayEnabled = phase == GamePhase.Play && !inputLocked
    Chapter3Station5ReplayColumn(
        replayEnabled = replayEnabled,
        onReplayLetter = {
            if (!audioEnabled || !replayEnabled) return@Chapter3Station5ReplayColumn
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
            val letterClip = AudioClips.letterNameClip(q.correctAnswer) ?: return@Chapter3Station5ReplayColumn
            if (voice.hasAsset(letterClip)) {
                GameAudioActions.launchPromptVoice(
                    audioEnabled = audioEnabled,
                    scope = scope,
                    audioRuntime = audioRuntime,
                    cancelFeedbackVoice = cancelFeedbackVoice,
                ) {
                    voice.playBlocking(letterClip)
                }
            }
        },
        onReplayFull = {
            if (!audioEnabled || !replayEnabled) return@Chapter3Station5ReplayColumn
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
            GameAudioActions.launchPromptVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) {
                val instruction = AudioClips.VoChooseLetter
                val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                val parts =
                    buildList {
                        if (voice.hasAsset(instruction)) add(instruction)
                        if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
                    }
                if (parts.isNotEmpty()) voice.playSequenceBlocking(*parts.toTypedArray())
            }
        },
        modifier = modifier,
    )
}
