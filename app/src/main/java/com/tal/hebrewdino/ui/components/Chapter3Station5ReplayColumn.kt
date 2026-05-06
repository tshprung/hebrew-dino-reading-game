package com.tal.hebrewdino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Chapter 3 station 5: right-edge replay controls (replaces the in-panel "repeat letter" button).
 *
 * "שוב" replays the current target letter, and "חזור" replays the full station prompt.
 */
@Composable
fun Chapter3Station5ReplayColumn(
    replayEnabled: Boolean,
    onReplayLetter: () -> Unit,
    onReplayFull: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 118.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledTonalButton(
            onClick = onReplayLetter,
            enabled = replayEnabled,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text("🔊 שוב", fontSize = 22.sp, style = ChapterNavChipStyles.labelTextStyle())
        }
        OutlinedButton(
            onClick = onReplayFull,
            enabled = replayEnabled,
            colors = ChapterNavChipStyles.outlinedButtonColors(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text("חזור", fontSize = 22.sp, style = ChapterNavChipStyles.labelTextStyle())
        }
    }
}

