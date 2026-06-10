package com.tal.hebrewdino.ui.screens



import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.widthIn

import androidx.compose.runtime.Composable

import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

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
import android.util.Log

import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio

import com.tal.hebrewdino.ui.companion.Chapter1CompanionCopy

import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot

import com.tal.hebrewdino.ui.companion.CompanionDinoSpeechBubble
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait

import com.tal.hebrewdino.ui.companion.FoundEggCelebration

import com.tal.hebrewdino.ui.companion.StoryReadablePanel

import com.tal.hebrewdino.ui.companion.displayNameHebrew

import com.tal.hebrewdino.ui.data.DinoCharacter

import com.tal.hebrewdino.ui.data.PlayerAddress

import com.tal.hebrewdino.ui.layout.ScreenFit



/** Season 1 Ch.1 finale: selected companion + one glowing found egg. */

@Composable

fun Chapter1DinoCompanionEggOutroScreen(

    companionCharacter: DinoCharacter,

    playerAddress: PlayerAddress,

    onContinue: () -> Unit,

    modifier: Modifier = Modifier,

) {

    val context = LocalContext.current
    val isDebuggable = remember {
        (context.applicationContext.applicationInfo.flags and 0x2) != 0
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    val rawVoice = remember { RawVoicePlayer(context = context) }

    var voicePlaying by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(OutroPhase.Companion) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()

    val (portraitW, portraitH) = Chapter1DinoCompanionPilot.finalePortraitSize(isCompactLandscapePhone)

    val foundEggSize = if (isCompactLandscapePhone) 140.dp else 176.dp
    val bubblePortraitW = if (isCompactLandscapePhone) 148.dp else 168.dp
    val bubblePortraitH = bubblePortraitW

    val assets = remember(companionCharacter) { Chapter1DinoCompanionPilot.assets(companionCharacter) }

    val finaleText =

        remember(companionCharacter, playerAddress) {

            Chapter1CompanionCopy.finaleBody(companionCharacter)

        }

    val storyOutroRaw =

        remember(companionCharacter) {

            Chapter1AddressAwareAudio.storyOutroRawRes(companionCharacter)

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



    LaunchedEffect(storyOutroRaw) {

        phase = OutroPhase.Companion
        voicePlaying = true
        val chapterCompleteRaw = Chapter1DinoCompanionPilot.chapterComplete
        if (chapterCompleteRaw == 0) {
            val msg =
                "Missing required story narration raw resource. chapterId=1 storyContext=Chapter1DinoCompanionEggOutroScreen(chapterComplete) rawResId=0"
            Log.e("MissingContent", msg)
            if (isDebuggable) throw IllegalStateException(msg)
        } else {
            val failure =
                runCatching { rawVoice.playRawBlocking(chapterCompleteRaw) }
                    .exceptionOrNull()
            if (failure != null) {
                Log.e(
                    "MissingContent",
                    "Required story narration raw playback failed. chapterId=1 storyContext=Chapter1DinoCompanionEggOutroScreen(chapterComplete) rawResId=$chapterCompleteRaw",
                    failure,
                )
                if (isDebuggable) throw failure
            }
        }
        phase = OutroPhase.Narrator
        if (storyOutroRaw == 0) {
            val msg =
                "Missing required story narration raw resource. chapterId=1 storyContext=Chapter1DinoCompanionEggOutroScreen(narratorOutro) rawResId=0"
            Log.e("MissingContent", msg)
            if (isDebuggable) throw IllegalStateException(msg)
        } else {
            val failure =
                runCatching { rawVoice.playRawBlocking(storyOutroRaw) }
                    .exceptionOrNull()
            if (failure != null) {
                Log.e(
                    "MissingContent",
                    "Required story narration raw playback failed. chapterId=1 storyContext=Chapter1DinoCompanionEggOutroScreen(narratorOutro) rawResId=$storyOutroRaw",
                    failure,
                )
                if (isDebuggable) throw failure
            }
        }
        voicePlaying = false
        phase = OutroPhase.Done
    }



    fun continueFromFinale() {

        rawVoice.stopNow()

        voicePlaying = false
        onContinue()

    }



    Box(modifier = modifier.fillMaxSize()) {

        Image(

            painter = painterResource(id = R.drawable.forest_bg_story_outro_egg),
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

                modifier =
                    Modifier
                        .weight(1f)
                        .offset(y = (-10).dp),

                verticalArrangement = Arrangement.Center,

                horizontalAlignment = Alignment.CenterHorizontally,

            ) {

                if (phase == OutroPhase.Companion) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(if (isCompactLandscapePhone) 1f else 0.94f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CompanionDinoSpeechBubble(
                            text = Chapter1CompanionCopy.chapter1CompletionSpeechBubbleText,
                            modifier = Modifier.weight(1f, fill = true),
                        )
                        CompanionDinoPortrait(
                            poseRes = assets.poseHappy,
                            talkFrameResIds = assets.talkFrameResIds,
                            isTalking = voicePlaying,
                            modifier = Modifier.size(width = bubblePortraitW, height = bubblePortraitH),
                            contentDescription = companionCharacter.displayNameHebrew(),
                        )
                    }
                } else {
                    StoryReadablePanel(modifier = Modifier.fillMaxWidth(if (isCompactLandscapePhone) 1f else 0.88f)) {
                        Text(
                            text = "\u200F$finaleText",
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


                Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 10.dp else 14.dp))

                FoundEggCelebration(eggSize = foundEggSize)
                if (phase != OutroPhase.Companion) {
                    Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 8.dp else 12.dp))
                    CompanionDinoPortrait(
                        poseRes = assets.poseHappy,
                        talkFrameResIds = assets.talkFrameResIds,
                        isTalking = voicePlaying,
                        modifier = Modifier.size(width = portraitW.dp, height = portraitH.dp),
                        contentDescription = companionCharacter.displayNameHebrew(),
                    )
                }
            }



            FilledTonalButton(
                onClick = { continueFromFinale() },

                modifier =

                    Modifier

                        .widthIn(min = 180.dp, max = 320.dp)
                        .height(52.dp),

            ) {

                Text(

                    text = if (voicePlaying) "\u200Fהמשך" else "\u200Fנמשיך במסע!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),

                )

            }

        }

    }

}


private enum class OutroPhase {
    Companion,
    Narrator,
    Done,
}
