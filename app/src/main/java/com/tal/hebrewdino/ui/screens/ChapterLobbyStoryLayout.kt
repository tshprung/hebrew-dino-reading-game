package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import com.tal.hebrewdino.ui.components.learning.StoryEggStrip
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class ChapterLobbyCompanion {
    DinoOnly,
    DinoAndMom,
}

private val dinoTalkFrames =
    listOf(
        R.drawable.dino_talk_0,
        R.drawable.dino_talk_1,
        R.drawable.dino_talk_2,
        R.drawable.dino_talk_3,
    )

private val momTalkFrames =
    listOf(
        R.drawable.mom_talk_0,
        R.drawable.mom_talk_1,
        R.drawable.mom_talk_2,
        R.drawable.mom_talk_3_flipped,
    )

@Composable
fun ChapterLobbyStoryLayout(
    backgroundRes: Int,
    title: String,
    body: String,
    eggStripCount: Int,
    companion: ChapterLobbyCompanion,
    narrationPlaying: Boolean = false,
    /** Optional narration WAV in assets (e.g. `audio/story_*.wav`). */
    voiceAssetPath: String? = null,
    dinoContentDescription: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voicePlayer = remember(voiceAssetPath) { voiceAssetPath?.let { VoicePlayer(context = context) } }
    var autoNarrationPlaying by remember(voiceAssetPath) { mutableStateOf(false) }

    DisposableEffect(voicePlayer) {
        onDispose { voicePlayer?.release() }
    }

    LaunchedEffect(voiceAssetPath) {
        if (voicePlayer == null || voiceAssetPath.isNullOrBlank()) return@LaunchedEffect
        autoNarrationPlaying = true
        if (voicePlayer.hasAsset(voiceAssetPath)) {
            voicePlayer.playBlocking(voiceAssetPath)
        }
        autoNarrationPlaying = false
    }

    val talking = if (voiceAssetPath != null) autoNarrationPlaying else narrationPlaying

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
                    .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (eggStripCount > 0) {
                StoryEggStrip(foundCount = eggStripCount, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.88f))
                            .padding(18.dp)
                            .width(560.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = body,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            AnimatedTalkingCharacter(
                                idleRes = R.drawable.dino_idle,
                                talkFrameResIds = dinoTalkFrames,
                                isTalking = talking,
                                modifier = Modifier.size(92.dp),
                                contentDescription = dinoContentDescription,
                            )
                            if (companion == ChapterLobbyCompanion.DinoAndMom) {
                                Spacer(modifier = Modifier.width(14.dp))
                                AnimatedTalkingCharacter(
                                    idleRes = R.drawable.mom_idle,
                                    talkFrameResIds = momTalkFrames,
                                    isTalking = talking,
                                    modifier = Modifier.size(92.dp),
                                    contentDescription = "אמא דינוזאור",
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    // UX: stop intro immediately when continuing (don't wait for dispose/navigation).
                    voicePlayer?.stopNow()
                    onContinue()
                },
                modifier = Modifier.width(180.dp),
            ) {
                Text("המשך")
            }
        }
    }
}
