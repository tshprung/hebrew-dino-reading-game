package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    unlockedLevel: Int,
    completedLevels: Set<Int>,
    onPlayLevel: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_beach),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "פרק 1: החוף",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "בחר שלב",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF0B2B3D),
                )
                Spacer(modifier = Modifier.height(0.dp).weight(1f))
                OutlinedButton(onClick = onOpenSettings) {
                    Text(text = "הגדרות")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                (1..10).forEach { levelId ->
                    val enabled = levelId <= unlockedLevel
                    val completed = completedLevels.contains(levelId)
                    val isOpenButNotDone = enabled && !completed

                    val buttonColors =
                        when {
                            !enabled -> ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant)
                            isOpenButNotDone -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                            else -> ButtonDefaults.buttonColors()
                        }
                    Button(
                        onClick = { onPlayLevel(levelId) },
                        enabled = enabled,
                        colors = buttonColors,
                    ) {
                        val label =
                            when {
                                !enabled -> "נעול"
                                completed -> "שלב $levelId ✓"
                                else -> "שלב $levelId"
                            }
                        Text(text = label)
                    }
                }
            }
        }
    }
}

