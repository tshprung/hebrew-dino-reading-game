package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait
import com.tal.hebrewdino.ui.companion.FoundEggCelebration
import com.tal.hebrewdino.ui.companion.StoryReadablePanel
import com.tal.hebrewdino.ui.layout.ScreenFit

/** Season 1 Ch.1 finale: companion Dino + one glowing found egg (no legacy story-layout Dino art). */
@Composable
fun Chapter1DinoCompanionEggOutroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val rawVoice = remember { RawVoicePlayer(context = context) }
    var narrationPlaying by remember { mutableStateOf(false) }
    var narrationFinished by remember { mutableStateOf(false) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val (portraitW, portraitH) = Chapter1DinoCompanionPilot.finalePortraitSize(isCompactLandscapePhone)
    val foundEggSize = if (isCompactLandscapePhone) 108.dp else 132.dp

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    rawVoice.stopNow()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            rawVoice.stopNow()
            rawVoice.release()
        }
    }

    LaunchedEffect(Unit) {
        narrationPlaying = true
        rawVoice.playRawBlocking(Chapter1DinoCompanionPilot.chapterComplete)
        narrationPlaying = false
        narrationFinished = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(if (isCompactLandscapePhone) 12.dp else 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StoryReadablePanel(modifier = Modifier.fillMaxWidth(if (isCompactLandscapePhone) 1f else 0.5f)) {
                    Text(
                        text = "\u200Fיש!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF1A2E3D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 12.dp else 16.dp))

                FoundEggCelebration(eggSize = foundEggSize)

                Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 8.dp else 12.dp))

                CompanionDinoPortrait(
                    poseRes = Chapter1DinoCompanionPilot.poseHappy,
                    talkFrameResIds = Chapter1DinoCompanionPilot.talkFrameResIds,
                    isTalking = narrationPlaying,
                    modifier = Modifier.size(width = portraitW.dp, height = portraitH.dp),
                    contentDescription = "דינו",
                )

                Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 10.dp else 14.dp))

                StoryReadablePanel(modifier = Modifier.fillMaxWidth(if (isCompactLandscapePhone) 1f else 0.88f)) {
                    Text(
                        text =
                            if (isCompactLandscapePhone) {
                                "\u200Fמצאנו את הביצה הראשונה!\n\n" +
                                    "אמא תשמח!\n\n" +
                                    "אבל נשארו עוד 2 ביצים למצוא!"
                            } else {
                                "\u200Fמצאנו את הביצה הראשונה!\n\n" +
                                    "אמא תשמח!\n\n" +
                                    "אבל נשארו עוד 2 ביצים למצוא!\n\n" +
                                    "בואו נמשיך!"
                            },
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                lineHeight = if (isCompactLandscapePhone) 24.sp else 28.sp,
                            ),
                        color = Color(0xFF1A2E3D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Button(
                onClick = onContinue,
                enabled = narrationFinished,
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
                Text(text = "\u200Fהמשך")
            }
        }
    }
}
