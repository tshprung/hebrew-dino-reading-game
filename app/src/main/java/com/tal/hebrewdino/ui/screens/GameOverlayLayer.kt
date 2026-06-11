package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.Episode4Stations15HelpColumn
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.StationUiSpec
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun GameOverlayLayer(
    chapterId: Int,
    stationId: Int,
    episode4HelpSt15: Boolean,
    popBalloonsHelpControlsEnabled: Boolean,
    phase: GamePhase,
    inputLocked: Boolean,
    audioEnabled: Boolean,
    stationUiSpec: StationUiSpec,
    episode4HelpLocksChoices: Boolean,
    balloonHelpLocksChoices: Boolean,
    performSideHelpReplay: () -> Unit,
    performSideHelpHint: () -> Unit,
    session: LevelSession,
    scope: CoroutineScope,
    voice: VoicePlayer,
    rawVoice: RawVoicePlayer,
    cancelFeedbackVoice: () -> Unit,
    audioRuntime: GameAudioRuntimeState,
    chapter1PlayerAddress: PlayerAddress?,
    showHintButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val playEnabled = phase == GamePhase.Play && !inputLocked
    val showHelpColumn = episode4HelpSt15 || popBalloonsHelpControlsEnabled
    val choicesLocked =
        if (episode4HelpSt15) {
            episode4HelpLocksChoices
        } else {
            balloonHelpLocksChoices
        }

    Box(modifier = modifier.fillMaxSize()) {
        if (showHelpColumn) {
            Episode4Stations15HelpColumn(
                replayEnabled = playEnabled && !choicesLocked,
                hintEnabled = playEnabled && !choicesLocked,
                onReplay = performSideHelpReplay,
                onHint = performSideHelpHint,
                showHintButton = showHintButton,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .zIndex(5f),
            )
        }
        Chapter3Station5ReplayOverlay(
            chapterId = chapterId,
            stationId = stationId,
            episode4HelpSt15 = episode4HelpSt15,
            phase = phase,
            inputLocked = inputLocked,
            audioEnabled = audioEnabled,
            session = session,
            scope = scope,
            voice = voice,
            rawVoice = rawVoice,
            cancelFeedbackVoice = cancelFeedbackVoice,
            audioRuntime = audioRuntime,
            chapter1PlayerAddress = chapter1PlayerAddress,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .zIndex(5f),
        )
    }
}
