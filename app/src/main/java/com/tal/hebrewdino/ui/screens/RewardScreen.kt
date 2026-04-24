package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

/** Random celebratory dino pose on the stage-complete screen (not a single static "nice" moment). */
private val RewardStageMascotDrawables =
    intArrayOf(
        R.drawable.dino_idle,
        R.drawable.dino_jump_0,
        R.drawable.dino_jump_1,
        R.drawable.dino_jump_2,
        R.drawable.dino_talk_0,
        R.drawable.dino_talk_1,
        R.drawable.dino_talk_2,
        R.drawable.dino_talk_3,
        R.drawable.dino_walk_0,
        R.drawable.dino_walk_1,
        R.drawable.dino_walk_2,
        R.drawable.dino_walk_3,
    )

@Composable
fun RewardScreen(
    levelId: Int,
    @Suppress("UNUSED_PARAMETER") correct: Int,
    @Suppress("UNUSED_PARAMETER") mistakes: Int,
    onBackToMap: () -> Unit,
    /** Full-bleed station-complete backdrop (chapter-specific). */
    backgroundRes: Int = R.drawable.forest_bg_reward,
    modifier: Modifier = Modifier,
) {
    fun rtl(text: String): String = "\u200F$text"

    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    var navigatedAway by remember(levelId) { mutableStateOf(false) }
    val mascotRes =
        remember {
            RewardStageMascotDrawables[Random.nextInt(RewardStageMascotDrawables.size)]
        }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
        }
    }

    LaunchedEffect(levelId) {
        coroutineScope {
            // Level complete: say "finished level" then one random short praise (not only "יפה").
            val voiceJob =
                launch {
                    val praisePool = AudioClips.rewardStagePraiseTailCandidates()
                    voice.warmUp(AudioClips.VoLevelDone, *praisePool)
                    voice.playBlocking(AudioClips.VoLevelDone)
                    val tail = praisePool.toMutableList()
                    tail.shuffle()
                    voice.playFirstAvailableBlocking(*tail.toTypedArray())
                }
            // Safety: always navigate back even if audio hangs.
            withTimeoutOrNull(9000) { voiceJob.join() }
            if (!navigatedAway) {
                navigatedAway = true
                onBackToMap()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = rtl("כל הכבוד!"),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "שלב $levelId הסתיים",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = mascotRes),
                contentDescription = null,
                modifier =
                    Modifier
                        .height(168.dp)
                        .fillMaxWidth(),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier =
                    Modifier
                        .weight(1f, fill = true)
                        .fillMaxSize(),
            ) {
                RewardFireworksLayer(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
