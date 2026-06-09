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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.BackgroundMusicVoiceDuck
import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait
import com.tal.hebrewdino.ui.companion.StoryReadablePanel
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.layout.ScreenFit

private val StoryInk = Color(0xFF1A2E3D)

/**
 * Season 2 season/chapter intro — layout aligned with Season 1 Ch.1 [Chapter1DinoCompanionIntroScreen]:
 * forest background, warm story card, large companion portrait, bottom-centered continue button.
 */
@Composable
fun Season2StoryIntroPanel(
    title: String,
    storyLines: List<String>,
    companionCharacter: DinoCharacter,
    continueLabel: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val assets = remember(companionCharacter) { CompanionAssets.forCharacter(companionCharacter) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val (portraitW, portraitH) = Chapter1DinoCompanionPilot.introPortraitSize(isCompactLandscapePhone)

    BackgroundMusicVoiceDuck(active = true)

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
                    .padding(if (isCompactLandscapePhone) 12.dp else 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    StoryReadablePanel(
                        modifier = Modifier.fillMaxWidth(0.96f),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.fillMaxWidth(),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isCompactLandscapePhone) 20.sp else 22.sp,
                                    lineHeight = if (isCompactLandscapePhone) 26.sp else 28.sp,
                                ),
                            color = StoryInk,
                            textAlign = TextAlign.Start,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        storyLines.forEachIndexed { index, line ->
                            if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = line,
                                modifier = Modifier.fillMaxWidth(),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = if (isCompactLandscapePhone) 17.sp else 18.sp,
                                        lineHeight = if (isCompactLandscapePhone) 24.sp else 26.sp,
                                    ),
                                color = StoryInk,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 10.dp else 14.dp))

                CompanionDinoPortrait(
                    poseRes = assets.poseHelp,
                    talkFrameResIds = assets.talkFrameResIds,
                    isTalking = true,
                    modifier = Modifier.size(width = portraitW.dp, height = portraitH.dp),
                    contentDescription = companionCharacter.displayNameHebrew(),
                )
            }

            FilledTonalButton(
                onClick = onContinue,
                modifier =
                    Modifier
                        .widthIn(min = 180.dp, max = 320.dp)
                        .height(52.dp),
            ) {
                Text(
                    text = continueLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}
