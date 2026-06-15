package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.BackgroundMusicVoiceDuck
import com.tal.hebrewdino.ui.audio.LocalBackgroundMusic
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.Season2StoryAudio
import com.tal.hebrewdino.ui.audio.withVoiceDuck
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait
import com.tal.hebrewdino.ui.companion.StoryReadablePanel
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2Copy
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding

private val CelebrationGold = Color(0xFFFFD54F)
private val CelebrationGreen = Color(0xFF2E7D32)
private val StoryInk = Color(0xFF1A2E3D)

/** Finale screen after Season 2 Chapter 7 Station 6 — season arc complete. */
@Composable
fun Season2SeasonCompleteSummaryScreen(
    companionCharacter: DinoCharacter,
    onBackToSeasons: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bgm = LocalBackgroundMusic.current
    val rawVoice = remember { RawVoicePlayer(context.applicationContext) }
    val assets = remember(companionCharacter) { CompanionAssets.forCharacter(companionCharacter) }
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val portraitSize = if (isCompact) 88.dp else 104.dp

    BackgroundMusicVoiceDuck(active = true)

    DisposableEffect(Unit) {
        onDispose {
            rawVoice.stopNow()
            rawVoice.release()
        }
    }

    LaunchedEffect(Unit) {
        bgm?.withVoiceDuck {
            rawVoice.playRawBlocking(Season2StoryAudio.SeasonCompleteSummary)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CelebrationGreen.copy(alpha = 0.18f),
                                Color.Transparent,
                                CelebrationGold.copy(alpha = 0.14f),
                            ),
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = if (isCompact) 12.dp else 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    StoryReadablePanel(
                        modifier = Modifier.fillMaxWidth(0.96f),
                    ) {
                        Text(
                            text = Season2Copy.seasonCompleteSummaryTitle(),
                            modifier = Modifier.fillMaxWidth(),
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isCompact) 22.sp else 26.sp,
                                    lineHeight = if (isCompact) 28.sp else 32.sp,
                                ),
                            color = CelebrationGreen,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Season2Copy.seasonCompleteSummaryStoryLines().forEachIndexed { index, line ->
                            if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = line,
                                modifier = Modifier.fillMaxWidth(),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = if (isCompact) 17.sp else 18.sp,
                                        lineHeight = if (isCompact) 24.sp else 26.sp,
                                    ),
                                color = StoryInk,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))

                CompanionDinoPortrait(
                    poseRes = assets.poseHappy,
                    talkFrameResIds = assets.talkFrameResIds,
                    isTalking = true,
                    modifier = Modifier.size(portraitSize),
                    contentDescription = companionCharacter.displayNameHebrew(),
                )
            }

            FilledTonalButton(
                onClick = {
                    rawVoice.stopNow()
                    onBackToSeasons()
                },
                modifier =
                    Modifier
                        .widthIn(min = 200.dp, max = 340.dp)
                        .height(52.dp),
            ) {
                Text(
                    text = Season2Copy.seasonCompleteSummaryContinueLabel(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}
