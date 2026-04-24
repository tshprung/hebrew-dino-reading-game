package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R

@Composable
fun MapScreen(
    unlockedLevel: Int,
    completedLevels: Set<Int>,
    onPlayLevel: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val totalLevels = 10
    val nextSuggested =
        (1..totalLevels).firstOrNull { !completedLevels.contains(it) } ?: totalLevels
    val quickPlayLevel = nextSuggested.coerceAtMost(unlockedLevel)

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
                .verticalScroll(scroll)
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
            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onPlayLevel(quickPlayLevel) },
                modifier = Modifier.width(260.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC400).copy(alpha = 0.95f),
                        contentColor = Color(0xFF0B2B3D),
                    ),
            ) {
                Text(
                    text = "שחק עכשיו (שלב $quickPlayLevel)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Stepping-stones path (kid-friendly): alternating left/right stones.
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                (1..totalLevels).forEach { levelId ->
                    val enabled = levelId <= unlockedLevel
                    val completed = completedLevels.contains(levelId)
                    val isSuggested = enabled && !completed && levelId == nextSuggested
                    val alignToStart = levelId % 2 == 1

                    PathStepRow(
                        levelId = levelId,
                        enabled = enabled,
                        completed = completed,
                        suggested = isSuggested,
                        alignToStart = alignToStart,
                        onClick = { onPlayLevel(levelId) },
                    )

                    if (levelId != totalLevels) {
                        PathConnector(alignToStart = !alignToStart)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Composable
private fun PathStepRow(
    levelId: Int,
    enabled: Boolean,
    completed: Boolean,
    suggested: Boolean,
    alignToStart: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignToStart) Arrangement.Start else Arrangement.End,
    ) {
        PathStone(
            levelId = levelId,
            enabled = enabled,
            completed = completed,
            suggested = suggested,
            onClick = onClick,
        )
    }
}

@Composable
private fun PathStone(
    levelId: Int,
    enabled: Boolean,
    completed: Boolean,
    suggested: Boolean,
    onClick: () -> Unit,
) {
    val baseColor =
        when {
            !enabled -> Color(0xFF7E8A93).copy(alpha = 0.35f)
            completed -> Color(0xFF2E7D32).copy(alpha = 0.85f)
            suggested -> Color(0xFF2AA6C9).copy(alpha = 0.95f)
            else -> Color(0xFF2AA6C9).copy(alpha = 0.80f)
        }
    val borderColor =
        when {
            suggested -> Color(0xFFFFC400)
            enabled -> Color.White.copy(alpha = 0.85f)
            else -> Color.Transparent
        }
    val label =
        when {
            !enabled -> "🔒"
            completed -> "✓"
            else -> levelId.toString()
        }
    val subtitle =
        when {
            !enabled -> "נעול"
            completed -> "בוצע"
            suggested -> "הבא"
            else -> "שלב"
        }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = baseColor,
            shape = CircleShape,
            shadowElevation = if (enabled) 8.dp else 0.dp,
            modifier =
                Modifier
                    .size(if (suggested) 86.dp else 78.dp)
                    .border(width = if (suggested) 4.dp else 2.dp, color = borderColor, shape = CircleShape)
                    .clickable(enabled = enabled, onClick = onClick),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .width(92.dp)
                    .background(
                        when {
                            !enabled -> Color.White.copy(alpha = 0.60f)
                            suggested -> Color(0xFFFFF3C4).copy(alpha = 0.95f)
                            else -> Color.White.copy(alpha = 0.70f)
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = subtitle,
                style =
                    run {
                        val base = MaterialTheme.typography.bodyMedium
                        if (completed) {
                            base.copy(
                                fontSize = base.fontSize * 2,
                                lineHeight = base.lineHeight * 2,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            base.copy(fontWeight = FontWeight.Bold)
                        }
                    },
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PathConnector(alignToStart: Boolean) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = if (alignToStart) Arrangement.Start else Arrangement.End,
    ) {
        Spacer(
            modifier =
                Modifier
                    .padding(horizontal = 28.dp)
                    .width(12.dp)
                    .height(26.dp)
                    .background(Color.White.copy(alpha = 0.55f), shape = RoundedCornerShape(12.dp)),
        )
    }
}

