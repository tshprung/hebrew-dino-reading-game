package com.tal.hebrewdino.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.Season2StoryAudio
import com.tal.hebrewdino.ui.audio.withVoiceDuck
import com.tal.hebrewdino.ui.companion.CompanionDinoRewardCelebration
import com.tal.hebrewdino.ui.companion.CompanionRewardCelebrationStyle
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Season2Copy
import com.tal.hebrewdino.ui.domain.Season2RewardLayout
import com.tal.hebrewdino.ui.audio.BackgroundMusicVoiceDuck
import com.tal.hebrewdino.ui.audio.LocalBackgroundMusic
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val OverlayScrim = Color(0xFF2B2724).copy(alpha = 0.72f)
private val MuseumWallTop = Color(0xFFF4EDE3)
private val MuseumWallBottom = Color(0xFFE6D9C8)
private val CelebrationGold = Color(0xFFCF9D4A)

@Composable
fun Season2SeasonIntroOverlay(
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Season2StoryIntroPanel(
        title = "\u200Fעונה 2: ${Season2Copy.SeasonTitle}",
        storyLines = Season2Copy.seasonIntroStoryLines(),
        companionCharacter = companionCharacter,
        continueLabel = Season2Copy.seasonIntroContinueLabel(),
        voiceRawResId = Season2StoryAudio.StoryIntro,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Season2MapIntroOverlay(
    chapterId: Int,
    companionCharacter: DinoCharacter,
    playerAddress: PlayerAddress,
    onContinue: () -> Unit,
    storyLines: List<String> = Season2Copy.mapIntroStoryLines(playerAddress),
    modifier: Modifier = Modifier,
) {
    Season2StoryIntroPanel(
        title = "\u200F${Season2Copy.MysteryMapTitle}",
        storyLines = storyLines,
        companionCharacter = companionCharacter,
        continueLabel = "\u200Fבואו נגלה!",
        voiceRawResId = Season2StoryAudio.optionalChapterIntroRawRes(chapterId),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Season2ChapterCompleteOverlay(
    chapterId: Int,
    @DrawableRes posterResId: Int,
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val bgm = LocalBackgroundMusic.current
    val rawVoice = remember { RawVoicePlayer(context = context) }
    var voicePlaying by remember { mutableStateOf(true) }

    val isCompact = ScreenFit.isCompactLandscapePhone()
    val posterMaxWidth =
        if (isCompact) {
            Season2RewardLayout.POSTER_MAX_WIDTH_COMPACT_DP.dp
        } else {
            Season2RewardLayout.POSTER_MAX_WIDTH_REGULAR_DP.dp
        }
    val posterMaxHeight =
        if (isCompact) {
            Season2RewardLayout.POSTER_MAX_HEIGHT_COMPACT_DP.dp
        } else {
            Season2RewardLayout.POSTER_MAX_HEIGHT_REGULAR_DP.dp
        }
    val companionSize =
        if (isCompact) {
            Season2RewardLayout.COMPANION_SIZE_COMPACT_DP.dp
        } else {
            Season2RewardLayout.COMPANION_SIZE_REGULAR_DP.dp
        }

    BackgroundMusicVoiceDuck(active = true)

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

    LaunchedEffect(chapterId) {
        voicePlaying = true
        val clip = Season2StoryAudio.chapterCompleteRawRes(chapterId)
        bgm?.withVoiceDuck {
            rawVoice.playRawBlocking(clip)
        }
        voicePlaying = false
        delay(800.milliseconds)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(OverlayScrim.copy(alpha = 0.82f)),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = Season2Copy.completionHeadline(chapterId),
                    modifier = Modifier.fillMaxWidth(0.96f),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize =
                                if (isCompact) {
                                    Season2RewardLayout.HEADLINE_SP_COMPACT.sp
                                } else {
                                    Season2RewardLayout.HEADLINE_SP_REGULAR.sp
                                },
                        ),
                    color = Color(0xFF2E7D32),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = Season2Copy.completionSubline(),
                    modifier = Modifier.fillMaxWidth(0.94f),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize =
                                if (isCompact) {
                                    Season2RewardLayout.SUBLINE_SP_COMPACT.sp
                                } else {
                                    Season2RewardLayout.SUBLINE_SP_REGULAR.sp
                                },
                        ),
                    color = Color(0xFFF4EDE3),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(Season2RewardLayout.TEXT_TO_HERO_GAP_DP.dp))
                Surface(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(if (isCompact) 0.98f else 0.92f)
                            .border(
                                width = 2.dp,
                                brush =
                                    Brush.linearGradient(
                                        listOf(CelebrationGold, Color(0xFF2E7D32), CelebrationGold),
                                    ),
                                shape = RoundedCornerShape(20.dp),
                            ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    shadowElevation = 10.dp,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(MuseumWallTop, MuseumWallBottom)))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            Season2RewardLayout.HERO_GAP_DP.dp,
                            Alignment.CenterHorizontally,
                        ),
                    ) {
                        Image(
                            painter = painterResource(id = posterResId),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(0.94f)
                                    .widthIn(max = posterMaxWidth)
                                    .heightIn(max = posterMaxHeight)
                                    .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                        )
                        Box(
                            modifier = Modifier.size(companionSize),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            CompanionDinoRewardCelebration(
                                style = CompanionRewardCelebrationStyle.Happy,
                                isTalking = voicePlaying,
                                companionCharacter = companionCharacter,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Season2RewardLayout.HERO_TO_BUTTON_GAP_DP.dp))
                FilledTonalButton(
                    onClick = onContinue,
                    modifier =
                        Modifier
                            .widthIn(
                                min = Season2RewardLayout.CONTINUE_MIN_WIDTH_DP.dp,
                                max = Season2RewardLayout.CONTINUE_MAX_WIDTH_DP.dp,
                            )
                            .height(Season2RewardLayout.CONTINUE_HEIGHT_DP.dp),
                ) {
                    Text(
                        text = Season2Copy.completionContinueLabel(),
                        fontWeight = FontWeight.Bold,
                        fontSize =
                            if (isCompact) {
                                Season2RewardLayout.CONTINUE_FONT_SP_COMPACT.sp
                            } else {
                                Season2RewardLayout.CONTINUE_FONT_SP_REGULAR.sp
                            },
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
