package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RewardScreen(
    levelId: Int,
    correct: Int,
    mistakes: Int,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember { SoundPoolPlayer(context = context) }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    val balloonCount = remember(levelId, correct, mistakes) {
        val base = 5
        val bonus = max(0, correct - mistakes) / 2
        min(12, base + bonus)
    }
    val balloons = remember(levelId) {
        mutableStateListOf(*Array(balloonCount) { true })
    }

    LaunchedEffect(levelId) {
        player.play(AudioClips.VoLevelDone)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "כל הכבוד!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "שלב $levelId הסתיים",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "תפוצץ/י בלונים!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(18.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            balloons.forEachIndexed { idx, alive ->
                if (alive) {
                    Balloon(
                        color = BALLOON_COLORS[idx % BALLOON_COLORS.size],
                        onPop = { balloons[idx] = false },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Button(onClick = onBackToMap) {
            Text(text = "חזרה למפה", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Balloon(
    color: Color,
    onPop: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onPop),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "בלון", style = MaterialTheme.typography.bodyMedium)
    }
}

private val BALLOON_COLORS = listOf(
    Color(0xFFFF6B6B),
    Color(0xFFFFD93D),
    Color(0xFF6BCB77),
    Color(0xFF4D96FF),
    Color(0xFFB983FF),
)

