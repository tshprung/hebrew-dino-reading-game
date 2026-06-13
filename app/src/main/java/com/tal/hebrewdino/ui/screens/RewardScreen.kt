package com.tal.hebrewdino.ui.screens

import androidx.annotation.RawRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.RewardSuccessAudio
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1DinoCompanionPilot
import com.tal.hebrewdino.ui.companion.CompanionVisualPolicy
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.companion.CompanionDinoRewardCelebration
import com.tal.hebrewdino.ui.companion.CompanionRewardCelebrationStyle
import com.tal.hebrewdino.ui.companion.FoundEggCelebration
import com.tal.hebrewdino.ui.companion.StoryReadablePanel
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
private fun PilotRewardSparklesLayer(
    modifier: Modifier = Modifier,
) {
    val t by rememberInfiniteTransition(label = "pilotSparkles").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2600, easing = LinearEasing)),
        label = "pilotSparklesPhase",
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val xs = floatArrayOf(0.16f, 0.84f, 0.26f, 0.74f, 0.50f, 0.12f, 0.88f)
        val ys = floatArrayOf(0.18f, 0.20f, 0.42f, 0.40f, 0.14f, 0.66f, 0.64f)
        val phase = floatArrayOf(0.00f, 0.22f, 0.48f, 0.66f, 0.12f, 0.34f, 0.56f)
        val base = size.minDimension * 0.020f
        for (i in xs.indices) {
            val p = (t + phase[i]) % 1f
            val twinkle = (1f - (p - 0.5f) * (p - 0.5f) * 4f).coerceIn(0f, 1f)
            val alpha = (0.10f + 0.22f * twinkle).coerceIn(0f, 0.30f)
            val len = base * (0.55f + 1.10f * twinkle)
            val stroke = (base * 0.30f).coerceAtLeast(1.4f)
            val c = Color(0xFFFFF8E1).copy(alpha = alpha)
            val cx = w * xs[i]
            val cy = h * ys[i]
            drawLine(
                color = c,
                start = Offset(cx - len, cy),
                end = Offset(cx + len, cy),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = c,
                start = Offset(cx, cy - len),
                end = Offset(cx, cy + len),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun RewardScreen(
    levelId: Int,
    onBackToMap: () -> Unit,
    /** Full-bleed station-complete backdrop (chapter-specific). */
    backgroundRes: Int = R.drawable.forest_bg_reward,
    /** Season 1 Chapter 1 pilot: happy Dino + short raw success clip (no station VO). */
    chapter1DinoCompanionPilot: Boolean = false,
    chapter1CompanionCharacter: DinoCharacter? = null,
    showSelectedCompanionPortrait: Boolean = false,
    /** Ch.3 station rewards: glowing pink egg instead of companion portrait. */
    showPinkEggReward: Boolean = false,
    selectedCompanionCharacter: DinoCharacter? = null,
    /** Optional short raw success clip to play instead of generic reward voice (keeps visuals unchanged). */
    @RawRes rewardSuccessRawResId: Int? = null,
    rewardChapterId: Int? = null,
    requireRawSuccessAudio: Boolean = false,
    modifier: Modifier = Modifier,
) {
    fun rtl(text: String): String = "\u200F$text"

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val voice = remember { VoicePlayer(context = context) }
    val isDebuggable = remember { DevTools.enabled(context) }
    fun reportMissingSelectedCompanion(detail: String) {
        Log.e(
            "MissingContent",
            "Missing selected companion for RewardScreen. chapterId=$rewardChapterId stationId=$levelId detail=$detail",
        )
        if (isDebuggable) throw IllegalStateException("Missing selected companion for RewardScreen. chapterId=$rewardChapterId stationId=$levelId detail=$detail")
    }
    if (!chapter1DinoCompanionPilot && !showSelectedCompanionPortrait && !showPinkEggReward) {
        reportMissingSelectedCompanion("legacy reward mascot disabled; require companion portrait or pink egg flags")
    }
    if (chapter1DinoCompanionPilot && chapter1CompanionCharacter == null) {
        reportMissingSelectedCompanion("chapter1DinoCompanionPilot=true chapter1CompanionCharacter=null")
    }
    if ((showSelectedCompanionPortrait || showPinkEggReward) && selectedCompanionCharacter == null) {
        reportMissingSelectedCompanion(
            "showSelectedCompanionPortrait=$showSelectedCompanionPortrait showPinkEggReward=$showPinkEggReward selectedCompanionCharacter=null",
        )
    }
    fun reportRequiredRewardRawMissing(detail: String) {
        Log.e(
            "MissingContent",
            "Missing required reward raw success audio. chapterId=$rewardChapterId stationId=$levelId rewardSuccessRawResId=$rewardSuccessRawResId detail=$detail",
        )
        if (isDebuggable) {
            throw IllegalStateException(
                "Missing required reward raw success audio. chapterId=$rewardChapterId stationId=$levelId rewardSuccessRawResId=$rewardSuccessRawResId detail=$detail",
            )
        }
    }
    val rawVoice =
        remember(chapter1DinoCompanionPilot, rewardSuccessRawResId, requireRawSuccessAudio) {
            if (chapter1DinoCompanionPilot || rewardSuccessRawResId != null || requireRawSuccessAudio) {
                RawVoicePlayer(context)
            } else {
                null
            }
        }
    var navigatedAway by remember(levelId) { mutableStateOf(false) }
    var companionVoicePlaying by remember(levelId) { mutableStateOf(false) }
    val portraitCharacter =
        if (chapter1DinoCompanionPilot) {
            CompanionVisualPolicy.requireSelectedCompanion(
                character = chapter1CompanionCharacter,
                context = "RewardScreen.chapter1Pilot",
                devToolsEnabled = isDebuggable,
                chapterId = rewardChapterId,
                stationId = levelId,
            )
        } else if (showSelectedCompanionPortrait) {
            CompanionVisualPolicy.requireSelectedCompanion(
                character = selectedCompanionCharacter,
                context = "RewardScreen.selectedPortrait",
                devToolsEnabled = isDebuggable,
                chapterId = rewardChapterId,
                stationId = levelId,
            )
        } else {
            null
        }
    val companionRewardStyle =
        remember(levelId, chapter1DinoCompanionPilot, showSelectedCompanionPortrait) {
            if (chapter1DinoCompanionPilot) {
                Chapter1DinoCompanionPilot.rewardCelebrationForStation(levelId)
            } else if (showSelectedCompanionPortrait) {
                CompanionRewardCelebrationStyle.Happy
            } else {
                CompanionRewardCelebrationStyle.Happy
            }
        }
    val companionShowFireworks =
        chapter1DinoCompanionPilot &&
            (companionRewardStyle == CompanionRewardCelebrationStyle.Sparkle ||
                companionRewardStyle == CompanionRewardCelebrationStyle.GrandFinale)
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val companionPortraitW =
        if (!chapter1DinoCompanionPilot) {
            if (isCompactLandscapePhone) 160.dp else Chapter1DinoCompanionPilot.portraitWidthDp.dp
        } else {
            if (isCompactLandscapePhone) 176.dp else (Chapter1DinoCompanionPilot.portraitWidthDp + 20).dp
        }
    val companionPortraitH =
        if (!chapter1DinoCompanionPilot) {
            if (isCompactLandscapePhone) 160.dp else Chapter1DinoCompanionPilot.portraitHeightDp.dp
        } else {
            if (isCompactLandscapePhone) 176.dp else (Chapter1DinoCompanionPilot.portraitHeightDp + 20).dp
        }
    val journeyProgressCue =
        remember(levelId, chapter1DinoCompanionPilot) {
            if (chapter1DinoCompanionPilot) {
                Chapter1DinoCompanionPilot.journeyProgressCueForStation(levelId)
            } else {
                null
            }
        }
    val rewardVisitKey = remember(levelId, rewardChapterId) { "${rewardChapterId ?: 0}:$levelId" }
    val companionForRewardPicker by rememberUpdatedState(
        if (chapter1DinoCompanionPilot) {
            chapter1CompanionCharacter
        } else {
            selectedCompanionCharacter
        },
    )

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

    LaunchedEffect(
        rewardVisitKey,
        chapter1DinoCompanionPilot,
        rewardSuccessRawResId,
        requireRawSuccessAudio,
    ) {
        coroutineScope {
            val needsRawRewardVoice = chapter1DinoCompanionPilot || requireRawSuccessAudio || rewardSuccessRawResId != null
            val voiceJob =
                launch {
                    if (needsRawRewardVoice) {
                        val clip =
                            when {
                                rewardSuccessRawResId != null && rewardSuccessRawResId != 0 ->
                                    rewardSuccessRawResId
                                chapter1DinoCompanionPilot || requireRawSuccessAudio -> {
                                    val companion =
                                        companionForRewardPicker
                                            ?: run {
                                                reportRequiredRewardRawMissing("companionForRewardPicker==null")
                                                return@launch
                                            }
                                    RewardSuccessAudio.pickAndRemember(companion)
                                }
                                else -> {
                                    reportRequiredRewardRawMissing("rewardSuccessRawResId==null")
                                    return@launch
                                }
                            }
                        if (clip == 0) {
                            reportRequiredRewardRawMissing("rewardClip==0")
                            return@launch
                        }
                        if (isDebuggable) {
                            Log.d(
                                "RewardScreen",
                                "rewardVoiceStart chapterId=$rewardChapterId stationId=$levelId rawResId=$clip",
                            )
                        }
                        companionVoicePlaying = true
                        rawVoice?.playRawBlocking(clip)
                        companionVoicePlaying = false
                        delay(Chapter1DinoCompanionPilot.REWARD_POST_AUDIO_MS.milliseconds)
                    } else {
                        Log.e(
                            "MissingContent",
                            "Missing reward raw success audio. chapterId=$rewardChapterId stationId=$levelId rewardSuccessRawResId=null detail=legacy VoLevelDone fallback disabled",
                        )
                    }
                }
            if (needsRawRewardVoice) {
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
        if (chapter1DinoCompanionPilot) {
            PilotRewardSparklesLayer(modifier = Modifier.fillMaxSize())
        }

        if (!chapter1DinoCompanionPilot && isCompactLandscapePhone) {
            RewardFireworksLayer(modifier = Modifier.fillMaxSize())
        }
        val pilotShowGentleFireworks =
            chapter1DinoCompanionPilot &&
                !companionShowFireworks
        if (pilotShowGentleFireworks) {
            RewardFireworksLayer(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .alpha(0.32f),
                intensity = RewardFireworksIntensity.Gentle,
            )
        }
        if (companionShowFireworks) {
            RewardFireworksLayer(
                modifier = Modifier.fillMaxSize(),
                intensity =
                    if (companionRewardStyle == CompanionRewardCelebrationStyle.GrandFinale) {
                        RewardFireworksIntensity.Strong
                    } else {
                        RewardFireworksIntensity.Gentle
                    },
            )
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
                Box(
                    modifier =
                        Modifier
                            .size(width = companionPortraitW, height = companionPortraitH)
                            .then(
                                if (showPinkEggReward) {
                                    Modifier
                                } else {
                                    Modifier.drawBehind {
                                        val base = size.minDimension * 0.55f
                                        drawCircle(
                                            color = Color(0xFFFFF59D).copy(alpha = 0.16f),
                                            radius = base * 1.18f,
                                        )
                                        drawCircle(
                                            color = Color(0xFFFFD54F).copy(alpha = 0.12f),
                                            radius = base,
                                        )
                                    }
                                },
                            ),
                ) {
                    if (showPinkEggReward) {
                        FoundEggCelebration(
                            eggDrawableRes = R.drawable.egg_pink_up,
                            eggSize = minOf(companionPortraitW, companionPortraitH),
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CompanionDinoRewardCelebration(
                            style = companionRewardStyle,
                            isTalking = (chapter1DinoCompanionPilot || rewardSuccessRawResId != null) && companionVoicePlaying,
                            companionCharacter = portraitCharacter!!,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                if (chapter1DinoCompanionPilot) {
                    StoryReadablePanel(modifier = Modifier.weight(1f, fill = true)) {
                        Text(
                            text = rtl("כל הכבוד!"),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF1A2E3D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "שלב $levelId הסתיים",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1A2E3D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (journeyProgressCue != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = journeyProgressCue,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1565C0),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                } else {
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
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (chapter1DinoCompanionPilot) {
                    StoryReadablePanel(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Text(
                            text = rtl("כל הכבוד!"),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF1A2E3D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "שלב $levelId הסתיים",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1A2E3D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (journeyProgressCue != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = journeyProgressCue,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1565C0),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                } else {
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
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier =
                        Modifier
                            .size(width = companionPortraitW, height = companionPortraitH)
                            .then(
                                if (showPinkEggReward) {
                                    Modifier
                                } else {
                                    Modifier.drawBehind {
                                        val base = size.minDimension * 0.56f
                                        drawCircle(
                                            color = Color(0xFFFFF59D).copy(alpha = 0.15f),
                                            radius = base * 1.22f,
                                        )
                                        drawCircle(
                                            color = Color(0xFFFFD54F).copy(alpha = 0.11f),
                                            radius = base,
                                        )
                                    }
                                },
                            ),
                ) {
                    if (showPinkEggReward) {
                        FoundEggCelebration(
                            eggDrawableRes = R.drawable.egg_pink_up,
                            eggSize = minOf(companionPortraitW, companionPortraitH),
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CompanionDinoRewardCelebration(
                            style = companionRewardStyle,
                            isTalking = (chapter1DinoCompanionPilot || rewardSuccessRawResId != null) && companionVoicePlaying,
                            companionCharacter = portraitCharacter!!,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (!chapter1DinoCompanionPilot) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f, fill = true)
                                .fillMaxSize(),
                    ) {
                        RewardFireworksLayer(modifier = Modifier.fillMaxSize())
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f, fill = true))
                }
            }
        }
    }
}
