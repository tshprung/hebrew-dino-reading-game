package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.game.MatchLetterToWordGame

/**
 * Match-letter ↔ word station UI (Ch3 st2 / saga finale) for [com.tal.hebrewdino.ui.screens.GameScreen].
 * Caller owns session completion, praise jobs, and tap audio.
 */
@Composable
fun MatchLetterToWordStationContent(
    choices: List<LessonChoice>,
    choicePairLimit: Int,
    contentKey: Int,
    enabled: Boolean,
    compactWideSpread: Boolean,
    onWordPressed: (String) -> Unit,
    onLetterPressed: (String) -> Unit,
    onCorrectMatch: (String) -> Unit,
    onWrongMatch: (String, String) -> Unit,
    onMatchAttempt: (Boolean) -> Unit,
    innerPictureScaleForChoice: (LessonChoice) -> Float,
    captionSizeMultiplier: Float,
    chapterId: Int?,
    stationId: Int?,
    instructionReadablePanel: Boolean,
    instructions: String,
    onSolved: () -> Unit,
    entryPulseScale: Float,
    verticalNudgeDp: Dp,
    modifier: Modifier = Modifier,
) {
    MatchLetterToWordGame(
        choices = choices,
        choicePairLimit = choicePairLimit,
        contentKey = contentKey,
        enabled = enabled,
        compactWideSpread = compactWideSpread,
        onWordPressed = onWordPressed,
        onLetterPressed = onLetterPressed,
        onCorrectMatch = onCorrectMatch,
        onWrongMatch = onWrongMatch,
        onMatchAttempt = onMatchAttempt,
        innerPictureScaleForChoice = innerPictureScaleForChoice,
        captionSizeMultiplier = captionSizeMultiplier,
        chapterId = chapterId,
        stationId = stationId,
        instructionReadablePanel = instructionReadablePanel,
        instructions = instructions,
        onSolved = onSolved,
        modifier =
            modifier
                .fillMaxSize()
                .scale(entryPulseScale)
                .then(
                    if (verticalNudgeDp > 0.dp) {
                        Modifier.offset(y = verticalNudgeDp)
                    } else {
                        Modifier
                    },
                ),
    )
}
