package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.Chapter3Station5ReplayColumn
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal fun Chapter3Station5ReplayOverlay(
    chapterId: Int,
    stationId: Int,
    episode4HelpSt15: Boolean,
    phase: GamePhase,
    audioEnabled: Boolean,
    session: LevelSession,
    scope: CoroutineScope,
    voice: VoicePlayer,
    cancelFeedbackVoice: () -> Unit,
    setFeedbackVoiceJob: (Job?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!((chapterId == 3 || chapterId == 6) && stationId == 5 && !episode4HelpSt15)) return
    Chapter3Station5ReplayColumn(
        replayEnabled = phase == GamePhase.Play,
        onReplayLetter = {
            if (!audioEnabled || phase != GamePhase.Play) return@Chapter3Station5ReplayColumn
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
            cancelFeedbackVoice()
            val letterClip = AudioClips.letterNameClip(q.correctAnswer) ?: return@Chapter3Station5ReplayColumn
            if (voice.hasAsset(letterClip)) {
                setFeedbackVoiceJob(scope.launch { voice.playBlocking(letterClip) })
            }
        },
        onReplayFull = {
            if (!audioEnabled || phase != GamePhase.Play) return@Chapter3Station5ReplayColumn
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
            cancelFeedbackVoice()
            setFeedbackVoiceJob(
                scope.launch {
                    val instruction = AudioClips.VoChooseLetter
                    val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                    val parts =
                        buildList {
                            if (voice.hasAsset(instruction)) add(instruction)
                            if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
                        }
                    if (parts.isNotEmpty()) voice.playSequenceBlocking(*parts.toTypedArray())
                },
            )
        },
        modifier = modifier,
    )
}
