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
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RewardScreen(
    levelId: Int,
    @Suppress("UNUSED_PARAMETER") correct: Int,
    @Suppress("UNUSED_PARAMETER") mistakes: Int,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun rtl(text: String): String = "\u200F$text"

    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    var navigatedAway by remember(levelId) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
        }
    }

    LaunchedEffect(levelId) {
        coroutineScope {
            launch { voice.playBlocking(AudioClips.VoLevelDone) }
            delay(4800)
            if (!navigatedAway) {
                navigatedAway = true
                onBackToMap()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_reward),
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
            val chapterOneFinale = levelId == Chapter1Config.STATION_COUNT
            if (chapterOneFinale) {
                Text(
                    text = rtl("מצאתם את הביצה!"),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rtl("כל הכבוד!"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0B2B3D),
                )
            } else {
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
            }
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(10.dp))

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
