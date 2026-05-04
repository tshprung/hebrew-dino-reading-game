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
 * Right-edge help controls for Episode 4 stations 1–5 (RTL layout: [Alignment.CenterStart] on the root [Box]).
 * Parent gates visibility and wires replay / hint actions.
 */
@Composable
fun Episode4Stations15HelpColumn(
    replayEnabled: Boolean,
    hintEnabled: Boolean,
    onReplay: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 118.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledTonalButton(
            onClick = onReplay,
            enabled = replayEnabled,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text("🔊 שוב", fontSize = 22.sp, style = ChapterNavChipStyles.labelTextStyle())
        }
        OutlinedButton(
            onClick = onHint,
            enabled = hintEnabled,
            colors = ChapterNavChipStyles.outlinedButtonColors(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text("רמז", fontSize = 22.sp, style = ChapterNavChipStyles.labelTextStyle())
        }
    }
}
