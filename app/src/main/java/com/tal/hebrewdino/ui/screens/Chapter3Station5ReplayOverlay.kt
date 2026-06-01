package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.components.Chapter3Station5ReplayColumn
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    rawVoice: RawVoicePlayer,
    cancelFeedbackVoice: () -> Unit,
    audioRuntime: GameAudioRuntimeState,
    chapter1PlayerAddress: PlayerAddress? = null,
    modifier: Modifier = Modifier,
) {
    if (!((chapterId == 3 || chapterId == 6) && stationId == 5 && !episode4HelpSt15)) return
    val replayEnabled = phase == GamePhase.Play && !inputLocked
    Chapter3Station5ReplayColumn(
        replayEnabled = replayEnabled,
        onReplayLetter = {
            if (!audioEnabled || !replayEnabled) return@Chapter3Station5ReplayColumn
            val q = session.currentQuestion as? Question.PopBalloonsQuestion ?: return@Chapter3Station5ReplayColumn
            val resId = AudioClips.letterNameRawResId(q.correctAnswer)
            if (resId == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=Chapter3Station5ReplayOverlay.onReplayLetter stage=missing raw letter-name mapping letter='${q.correctAnswer}'",
                )
                scope.launch { rawVoice.playRawBlocking(0) }
                return@Chapter3Station5ReplayColumn
            }
            GameAudioActions.launchPromptVoice(
                audioEnabled = audioEnabled,
                scope = scope,
                audioRuntime = audioRuntime,
                cancelFeedbackVoice = cancelFeedbackVoice,
            ) {
                rawVoice.playRawBlocking(resId)
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
                if (chapter1PlayerAddress == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required pop-balloons instruction audio. chapterId=$chapterId stationId=$stationId context=Chapter3Station5ReplayOverlay.onReplayFull stage=chapter1PlayerAddress=null expectedRawRes=Chapter1AddressAwareAudio.instructionRawRes(PopBalloons, address)",
                    )
                    rawVoice.playRawBlocking(0)
                    return@launchPromptVoice
                }
                rawVoice.playRawBlocking(
                    Chapter1AddressAwareAudio.instructionRawRes(
                        kind = Chapter1AddressAwareAudio.InstructionKind.PopBalloons,
                        address = chapter1PlayerAddress,
                    ),
                )
                val resId = AudioClips.letterNameRawResId(q.correctAnswer)
                if (resId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=Chapter3Station5ReplayOverlay.onReplayFull stage=missing raw letter-name mapping letter='${q.correctAnswer}'",
                    )
                    rawVoice.playRawBlocking(0)
                    return@launchPromptVoice
                }
                rawVoice.playRawBlocking(resId)
            }
        },
        modifier = modifier,
    )
}
