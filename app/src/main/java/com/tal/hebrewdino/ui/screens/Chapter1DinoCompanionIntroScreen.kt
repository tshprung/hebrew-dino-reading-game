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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait
import com.tal.hebrewdino.ui.companion.CompanionDinoSpeechBubble
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.layout.ScreenFit

@Composable
fun Chapter1DinoCompanionIntroScreen(
    companionCharacter: DinoCharacter,
    playerAddress: PlayerAddress,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val rawVoice = remember { RawVoicePlayer(context = context) }
    var introPlaying by remember { mutableStateOf(false) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val (portraitW, portraitH) = Chapter1DinoCompanionPilot.introPortraitSize(isCompactLandscapePhone)
    val assets = remember(companionCharacter) { Chapter1DinoCompanionPilot.assets(companionCharacter) }
    val introClip =
        remember(companionCharacter, playerAddress) {
            Chapter1DinoCompanionPilot.introRawRes(companionCharacter, playerAddress)
        }
    val introText =
        remember(companionCharacter, playerAddress) {
            Chapter1DinoCompanionPilot.introSpeechText(companionCharacter, playerAddress)
        }

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

    LaunchedEffect(introClip) {
        introPlaying = true
        rawVoice.playRawBlocking(introClip)
        introPlaying = false
    }

    val poseRes =
        when {
            introPlaying -> assets.poseHelp
            else -> assets.poseIdle
        }

    fun continueFromIntro() {
        rawVoice.stopNow()
        introPlaying = false
        onContinue()
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
                CompanionDinoSpeechBubble(
                    text = introText,
                    modifier = Modifier.fillMaxWidth(0.92f),
                )

                Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 10.dp else 14.dp))

                CompanionDinoPortrait(
                    poseRes = poseRes,
                    talkFrameResIds = assets.talkFrameResIds,
                    isTalking = introPlaying,
                    modifier = Modifier.size(width = portraitW.dp, height = portraitH.dp),
                    contentDescription = companionCharacter.displayNameHebrew(),
                )
            }

            Button(
                onClick = { continueFromIntro() },
                modifier =
                    Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp),
            ) {
                Text(
                    text = if (introPlaying) "\u200Fהמשך" else "\u200Fבואו נתחיל!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}
