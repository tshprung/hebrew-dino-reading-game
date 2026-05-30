package com.tal.hebrewdino.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tal.hebrewdino.R
import androidx.annotation.RawRes
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.companion.Chapter1ForestStoryCharacters
import com.tal.hebrewdino.ui.companion.CompanionGentleIdleMotion
import com.tal.hebrewdino.ui.companion.MotherLostEggsCue
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tal.hebrewdino.ui.layout.ScreenFit

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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChapterLobbyStoryLayout(
    backgroundRes: Int,
    title: String,
    body: String,
    companion: ChapterLobbyCompanion,
    narrationPlaying: Boolean = false,
    /** Optional narration WAV in assets (e.g. `audio/story_*.wav`). */
    voiceAssetPath: String? = null,
    /** Optional narration MP3 in `res/raw` (Season 1 Ch.1 companion story clips). */
    @RawRes voiceRawResId: Int? = null,
    bodyLineHeightOverride: TextUnit? = null,
    dinoContentDescription: String,
    /** Season 1 Ch.1: companion Dino/Dina art only. */
    useCompanionDinoArt: Boolean = false,
    companionCharacter: DinoCharacter = DinoCharacter.Dino,
    /** Season 1 Ch.1: companion mom art (static idle; no talk frames yet). */
    useCompanionMomArt: Boolean = false,
    /** Season 1 Ch.1: warm readable story card (matches Dino intro bubble). */
    useWarmReadableStoryPanel: Boolean = false,
    /** Season 1 Ch.1 forest intro: three small eggs near the mother. */
    showMotherLostEggsCue: Boolean = false,
    onContinue: () -> Unit,
    /** Optional content between title and body (e.g. Episode 4 clue row). */
    betweenTitleAndBody: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val voicePlayer = remember(voiceAssetPath) { voiceAssetPath?.let { VoicePlayer(context = context) } }
    val rawVoice = remember(voiceRawResId) { if (voiceRawResId != null) RawVoicePlayer(context = context) else null }
    var autoNarrationPlaying by remember(voiceAssetPath, voiceRawResId) { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, voicePlayer, rawVoice) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    voicePlayer?.stopNow()
                    rawVoice?.stopNow()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voicePlayer?.stopNow()
            voicePlayer?.release()
            rawVoice?.stopNow()
            rawVoice?.release()
        }
    }

    LaunchedEffect(voiceAssetPath, voiceRawResId) {
        when {
            voiceRawResId != null && voiceRawResId != 0 -> {
                autoNarrationPlaying = true
                rawVoice?.playRawBlocking(voiceRawResId)
                autoNarrationPlaying = false
            }
            voicePlayer != null && !voiceAssetPath.isNullOrBlank() -> {
                autoNarrationPlaying = true
                if (voicePlayer.hasAsset(voiceAssetPath)) {
                    voicePlayer.playBlocking(voiceAssetPath)
                }
                autoNarrationPlaying = false
            }
        }
    }

    val talking =
        when {
            voiceRawResId != null || voiceAssetPath != null -> autoNarrationPlaying
            else -> narrationPlaying
        }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val companionAssets =
        remember(companionCharacter) {
            CompanionAssets.forCharacter(companionCharacter)
        }
    val dinoIdleRes =
        if (useCompanionDinoArt) {
            companionAssets.poseIdle
        } else {
            R.drawable.dino_idle
        }
    val dinoTalkResIds =
        if (useCompanionDinoArt) {
            companionAssets.talkFrameResIds
        } else {
            dinoTalkFrames
        }
    val momIdleRes =
        if (useCompanionMomArt) {
            Chapter1DinoCompanionPilot.poseMomIdle
        } else {
            R.drawable.mom_idle
        }
    val momTalkResIds = if (useCompanionMomArt) emptyList() else momTalkFrames
    val momCharacterScale =
        if (useCompanionMomArt) {
            1f
        } else {
            0.85f
        }
    val storyTextColor = if (useWarmReadableStoryPanel) Color(0xFF1A2E3D) else Color(0xFF0B2B3D)
    val storyCardShape = RoundedCornerShape(24.dp)

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
                    .padding(if (isCompactLandscapePhone) 12.dp else 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier =
                            Modifier
                                .heightIn(max = maxHeight)
                                .fillMaxWidth()
                                .widthIn(max = 560.dp)
                                .then(
                                    if (useWarmReadableStoryPanel) {
                                        Modifier.shadow(elevation = 8.dp, shape = storyCardShape, clip = false)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clip(storyCardShape)
                                .background(
                                    if (useWarmReadableStoryPanel) {
                                        Color(0xFFFFF8E8)
                                    } else {
                                        Color.White.copy(alpha = 0.88f)
                                    },
                                )
                                .padding(if (isCompactLandscapePhone) 12.dp else 18.dp),
                    ) {
                        if (isCompactLandscapePhone) {
                            val scroll = rememberScrollState()
                            val characterSize = 112.dp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f, fill = true).verticalScroll(scroll),
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                        color = storyTextColor,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (betweenTitleAndBody != null) {
                                        betweenTitleAndBody()
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = body,
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = bodyLineHeightOverride ?: 18.sp,
                                            ),
                                        color = storyTextColor,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (showMotherLostEggsCue && useCompanionMomArt && companion == ChapterLobbyCompanion.DinoAndMom) {
                                    Chapter1ForestStoryCharacters(
                                        dinoIdleRes = dinoIdleRes,
                                        dinoTalkResIds = dinoTalkResIds,
                                        dinoTalking = talking,
                                        dinoContentDescription = dinoContentDescription,
                                        momIdleRes = momIdleRes,
                                        momTalkResIds = momTalkResIds,
                                        momTalking = talking,
                                        useCompanionMomArt = true,
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier =
                                            Modifier.width(
                                                characterSize *
                                                    if (companion == ChapterLobbyCompanion.DinoAndMom) {
                                                        1.5f
                                                    } else {
                                                        1.15f
                                                    },
                                            ),
                                    ) {
                                        AnimatedTalkingCharacter(
                                            idleRes = dinoIdleRes,
                                            talkFrameResIds = dinoTalkResIds,
                                            isTalking = talking,
                                            modifier = Modifier.size(characterSize),
                                            contentDescription = dinoContentDescription,
                                        )
                                        if (companion == ChapterLobbyCompanion.DinoAndMom) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            ChapterLobbyMomCharacter(
                                                momIdleRes = momIdleRes,
                                                momTalkResIds = momTalkResIds,
                                                talking = talking,
                                                useCompanionMomArt = useCompanionMomArt,
                                                size = characterSize,
                                                momCharacterScale = momCharacterScale,
                                            )
                                            if (showMotherLostEggsCue) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                MotherLostEggsCue()
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val scroll = rememberScrollState()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.verticalScroll(scroll),
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                    color = storyTextColor,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (betweenTitleAndBody != null) {
                                    betweenTitleAndBody()
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                Text(
                                    text = body,
                                    style = MaterialTheme.typography.titleLarge.copy(lineHeight = bodyLineHeightOverride ?: 30.sp),
                                    color = storyTextColor,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                if (showMotherLostEggsCue && useCompanionMomArt && companion == ChapterLobbyCompanion.DinoAndMom) {
                                    Chapter1ForestStoryCharacters(
                                        dinoIdleRes = dinoIdleRes,
                                        dinoTalkResIds = dinoTalkResIds,
                                        dinoTalking = talking,
                                        dinoContentDescription = dinoContentDescription,
                                        momIdleRes = momIdleRes,
                                        momTalkResIds = momTalkResIds,
                                        momTalking = talking,
                                        useCompanionMomArt = true,
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        AnimatedTalkingCharacter(
                                            idleRes = dinoIdleRes,
                                            talkFrameResIds = dinoTalkResIds,
                                            isTalking = talking,
                                            modifier = Modifier.size(92.dp),
                                            contentDescription = dinoContentDescription,
                                        )
                                        if (companion == ChapterLobbyCompanion.DinoAndMom) {
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                ChapterLobbyMomCharacter(
                                                    momIdleRes = momIdleRes,
                                                    momTalkResIds = momTalkResIds,
                                                    talking = talking,
                                                    useCompanionMomArt = useCompanionMomArt,
                                                    size = 92.dp,
                                                    momCharacterScale = momCharacterScale,
                                                )
                                                if (showMotherLostEggsCue) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    MotherLostEggsCue()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = {
                    // UX: stop narration immediately when continuing (don't wait for dispose/navigation).
                    rawVoice?.stopNow()
                    voicePlayer?.stopNow()
                    onContinue()
                },
                modifier = Modifier.widthIn(min = 160.dp, max = 200.dp),
            ) {
                Text(
                    text = "המשך",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun ChapterLobbyMomCharacter(
    momIdleRes: Int,
    momTalkResIds: List<Int>,
    talking: Boolean,
    useCompanionMomArt: Boolean,
    size: androidx.compose.ui.unit.Dp,
    momCharacterScale: Float,
) {
    val characterSize = size * momCharacterScale
    if (useCompanionMomArt) {
        CompanionGentleIdleMotion(active = talking) {
            AnimatedTalkingCharacter(
                idleRes = momIdleRes,
                talkFrameResIds = momTalkResIds,
                isTalking = false,
                modifier = Modifier.size(characterSize),
                contentDescription = "אמא דינוזאורית",
            )
        }
    } else {
        AnimatedTalkingCharacter(
            idleRes = momIdleRes,
            talkFrameResIds = momTalkResIds,
            isTalking = talking,
            modifier = Modifier.size(characterSize),
            contentDescription = "אמא דינוזאורית",
        )
    }
}
