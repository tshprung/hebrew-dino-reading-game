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
import androidx.compose.runtime.remember
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
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun BeachIntroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StoryScreenScaffold(
        // No "beach chapter" anymore; reuse the forest story intro background.
        backgroundRes = R.drawable.forest_bg_story_intro,
        character = character,
        title = "פרק 1: החוף",
        body =
            "אוי לא! הביצה של אמא נעלמה.\n" +
                "בוא/י נעזור למצוא אותה.\n" +
                "כדי להתקדם, נבחר אותיות נכון.",
        voiceAssetPath = AudioClips.StoryForestIntro,
        onContinue = onContinue,
        onSkip = onSkip,
        modifier = modifier,
    )
}

@Composable
fun BeachOutroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StoryScreenScaffold(
        backgroundRes = R.drawable.forest_bg_reward,
        character = character,
        title = "כל הכבוד!",
        body =
            "סיימת את פרק החוף!\n" +
                "אמא ממש גאה בך.\n" +
                "בקרוב נמשיך להרפתקה הבאה…",
        voiceAssetPath = AudioClips.StoryEggOutro,
        onContinue = onContinue,
        onSkip = onSkip,
        modifier = modifier,
    )
}

@Composable
private fun StoryScreenScaffold(
    backgroundRes: Int,
    character: DinoCharacter,
    title: String,
    body: String,
    voiceAssetPath: String,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }

    DisposableEffect(Unit) {
        onDispose {
            voice.stopNow()
            voice.release()
        }
    }

    LaunchedEffect(voiceAssetPath) {
        // Optional narration. If the file doesn't exist, VoicePlayer will no-op.
        voice.playBlocking(voiceAssetPath)
    }

    // We currently ship a single kid dino sprite set. (Character selection can map to unique sprites later.)
    val dinoRes = R.drawable.dino_idle

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
                        Image(
                            painter = painterResource(id = R.drawable.dino_talk_0),
                            contentDescription = null,
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Image(
                            painter = painterResource(id = dinoRes),
                            contentDescription = null,
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        // UX: stop intro immediately when leaving.
                        voice.stopNow()
                        onSkip()
                    },
                    modifier = Modifier.width(160.dp),
                ) {
                    Text("דלג")
                }
                Button(
                    onClick = {
                        // UX: stop intro immediately when continuing.
                        voice.stopNow()
                        onContinue()
                    },
                    modifier = Modifier.width(160.dp),
                ) {
                    Text("המשך")
                }
            }
        }
    }
}

