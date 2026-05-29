package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Random celebratory dino pose on the stage-complete screen (not a single static "nice" moment). */
private val RewardStageMascotDrawables =
    intArrayOf(
        R.drawable.dino_idle,
        R.drawable.dino_jump_0,
        R.drawable.dino_jump_1,
        R.drawable.dino_jump_2,
        R.drawable.dino_talk_0,
        R.drawable.dino_talk_1,
        R.drawable.dino_talk_2,
        R.drawable.dino_talk_3,
        R.drawable.dino_walk_0,
        R.drawable.dino_walk_1,
        R.drawable.dino_walk_2,
        R.drawable.dino_walk_3,
    )

@Composable
fun RewardScreen(
    levelId: Int,
    @Suppress("UNUSED_PARAMETER") correct: Int,
    @Suppress("UNUSED_PARAMETER") mistakes: Int,
    onBackToMap: () -> Unit,
    /** Full-bleed station-complete backdrop (chapter-specific). */
    backgroundRes: Int = R.drawable.forest_bg_reward,
    /** Season 1 Chapter 1 pilot: happy Dino + short raw success clip (no station VO). */
    chapter1DinoCompanionPilot: Boolean = false,
    modifier: Modifier = Modifier,
) {
    fun rtl(text: String): String = "\u200F$text"

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val voice = remember { VoicePlayer(context = context) }
    val rawVoice = remember(chapter1DinoCompanionPilot) { if (chapter1DinoCompanionPilot) RawVoicePlayer(context) else null }
    var navigatedAway by remember(levelId) { mutableStateOf(false) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val mascotRes =
        remember(levelId) {
            RewardStageMascotDrawables[Random.nextInt(RewardStageMascotDrawables.size)]
        }
    val companionPortraitW =
        if (isCompactLandscapePhone) 160.dp else Chapter1DinoCompanionPilot.portraitWidthDp.dp
    val companionPortraitH =
        if (isCompactLandscapePhone) 160.dp else Chapter1DinoCompanionPilot.portraitHeightDp.dp

    DisposableEffect(lifecycleOwner, levelId) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    voice.stopNow()
                    rawVoice?.stopNow()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voice.stopNow()
            voice.release()
            rawVoice?.stopNow()
            rawVoice?.release()
        }
    }

    LaunchedEffect(levelId, chapter1DinoCompanionPilot) {
        coroutineScope {
            val voiceJob =
                launch {
                    if (chapter1DinoCompanionPilot) {
                        val clip = Chapter1DinoCompanionPilot.successClipForStation(levelId)
                        rawVoice?.playRawBlocking(clip)
                        delay(Chapter1DinoCompanionPilot.REWARD_POST_AUDIO_MS)
                    } else {
                        // Level complete: say "finished level" then one random short praise (not only "יפה").
                        val praisePool = AudioClips.rewardStagePraiseTailCandidates()
                        voice.warmUp(AudioClips.VoLevelDone, *praisePool)
                        voice.playBlocking(AudioClips.VoLevelDone)
                        voice.playFirstAvailableBlockingRandomized(praisePool)
                    }
                }
            if (chapter1DinoCompanionPilot) {
                GameAudioActions.await(voiceJob, Chapter1DinoCompanionPilot.REWARD_AUDIO_MAX_WAIT_MS)
            } else {
                GameAudioActions.await(voiceJob, 9000L)
            }
            if (!navigatedAway) {
                navigatedAway = true
                onBackToMap()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        if (isCompactLandscapePhone) {
            RewardFireworksLayer(modifier = Modifier.fillMaxSize())
        }

        if (isCompactLandscapePhone) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            ) {
                if (chapter1DinoCompanionPilot) {
                    CompanionDinoPortrait(
                        poseRes = Chapter1DinoCompanionPilot.poseHappy,
                        modifier = Modifier.size(width = companionPortraitW, height = companionPortraitH),
                        contentDescription = "דינו",
                    )
                } else {
                    Image(
                        painter = painterResource(id = mascotRes),
                        contentDescription = null,
                        modifier = Modifier.width(160.dp).height(160.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = rtl("כל הכבוד!"),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "שלב $levelId הסתיים",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
                Spacer(modifier = Modifier.height(8.dp))
                if (chapter1DinoCompanionPilot) {
                    CompanionDinoPortrait(
                        poseRes = Chapter1DinoCompanionPilot.poseHappy,
                        modifier = Modifier.size(width = companionPortraitW, height = companionPortraitH),
                        contentDescription = "דינו",
                    )
                } else {
                    Image(
                        painter = painterResource(id = mascotRes),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .height(168.dp)
                                .fillMaxWidth(),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

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
}
