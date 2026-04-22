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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import com.tal.hebrewdino.ui.components.learning.StoryEggStrip
import com.tal.hebrewdino.ui.data.DinoCharacter

private val momTalkFrames =
    listOf(
        R.drawable.mom_talk_0,
        R.drawable.mom_talk_1,
        R.drawable.mom_talk_2,
        R.drawable.mom_talk_3_flipped,
    )

private val dinoTalkFrames =
    listOf(
        R.drawable.dino_talk_0,
        R.drawable.dino_talk_1,
        R.drawable.dino_talk_2,
        R.drawable.dino_talk_3,
    )

@Composable
fun ForestIntroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForestStoryScreen(
        backgroundRes = R.drawable.forest_bg_story_intro,
        dinoContentDescription = if (character == DinoCharacter.Dina) "דינה" else "דינו",
        title = "פרק 1 - מצא את הביצה",
        body =
            "אמא דינוזאור שמרה על הביצים שלה…\n" +
                "\n" +
                "אבל פתאום — הן נעלמו!\n" +
                "\n" +
                "\"אוי לא! איפה הביצים שלי?\", אמרה אמא דינוזאור\n" +
                "\n" +
                "בואו נעזור לה!\n" +
                "\n" +
                "נצא לדרך ונפתור משימות.",
        voiceAssetPath = AudioClips.StoryForestIntro,
        eggStripCount = 0,
        showMomCharacter = true,
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ForestOutroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForestStoryScreen(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        dinoContentDescription = if (character == DinoCharacter.Dina) "דינה" else "דינו",
        title = "יש!",
        body =
            "מצאנו את הביצה הראשונה!\n" +
                "\n" +
                "אמא תשמח!\n" +
                "\n" +
                "אבל נשארו עוד 2 ביצים למצוא!\n" +
                "\n" +
                "בואו נמשיך!",
        voiceAssetPath = AudioClips.StoryEggOutro,
        eggStripCount = 1,
        showMomCharacter = false,
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun ForestStoryScreen(
    backgroundRes: Int,
    dinoContentDescription: String,
    title: String,
    body: String,
    voiceAssetPath: String,
    eggStripCount: Int,
    showMomCharacter: Boolean,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    var narrationPlaying by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose { voice.release() }
    }

    LaunchedEffect(voiceAssetPath) {
        narrationPlaying = true
        voice.playBlocking(voiceAssetPath)
        narrationPlaying = false
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
                            .background(Color.White.copy(alpha = 0.86f))
                            .padding(18.dp)
                            .width(520.dp),
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
                                isTalking = narrationPlaying,
                                modifier = Modifier.size(92.dp),
                                contentDescription = dinoContentDescription,
                            )
                            if (showMomCharacter) {
                                Spacer(modifier = Modifier.width(14.dp))
                                AnimatedTalkingCharacter(
                                    idleRes = R.drawable.mom_idle,
                                    talkFrameResIds = momTalkFrames,
                                    isTalking = narrationPlaying,
                                    modifier = Modifier.size(92.dp),
                                    contentDescription = "אמא דינוזאור",
                                )
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.width(140.dp),
                    colors =
                        androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) {
                    Text("חזור")
                }
                Button(onClick = onContinue, modifier = Modifier.width(160.dp)) {
                    Text("המשך")
                }
            }
        }
    }
}
