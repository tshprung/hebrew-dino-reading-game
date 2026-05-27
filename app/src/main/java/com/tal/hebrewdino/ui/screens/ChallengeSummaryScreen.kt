package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.InteractionAudio
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.components.particles.ConfettiBurstOverlay
import com.tal.hebrewdino.ui.components.particles.shouldShowConfettiForCue
import com.tal.hebrewdino.ui.economy.RewardEngine
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.delay

private const val POST_FANFARE_AUTO_ADVANCE_MS = 5_000L

@Composable
fun ChallengeSummaryScreen(
    rewardEngine: RewardEngine,
    onBackToDinoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: ChallengeSummaryViewModel =
        viewModel(
            factory = remember(rewardEngine) { ChallengeSummaryViewModel.Factory(rewardEngine) },
        )
    val uiState by vm.uiState.collectAsState()
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var navigated by remember { mutableStateOf(false) }
    var skipPresentation by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val appContext = remember(context) { context.applicationContext }

    fun goHome() {
        if (navigated) return
        skipPresentation = true
        InteractionAudio.stopAllNow(appContext)
        navigated = true
        onBackToDinoHome()
    }
    val localSfx = remember(context) { SfxManager(context.applicationContext) }
    val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
    DisposableEffect(localSfx) {
        onDispose { localSfx.release() }
    }

    LaunchedEffect(uiState.event?.eventId) {
        val event = uiState.event ?: return@LaunchedEffect
        if (uiState.presentationStarted || skipPresentation) return@LaunchedEffect
        vm.onPresentationStarted()
        if (shouldShowConfettiForCue(event.visualCue)) {
            confettiTrigger += 1
        }
        withFrameNanos { }
        withFrameNanos { }
        if (skipPresentation) return@LaunchedEffect
        val accessorySpeech = uiState.accessoryCelebrationSpeech
        if (accessorySpeech.isNotBlank()) {
            tts.speakFully(accessorySpeech, navigationSettleMs = 0L)
        }
        if (skipPresentation) return@LaunchedEffect
        if (event.applesCount > 0) {
            tts.speakFully(event.fanfareText, navigationSettleMs = 0L)
            localSfx.playFanfare()
        }
        if (skipPresentation) return@LaunchedEffect
        vm.onPresentationFinished()
        delay(POST_FANFARE_AUTO_ADVANCE_MS)
        if (!skipPresentation) goHome()
    }

    val displayText = uiState.displayText

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(onClick = ::goHome),
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        ConfettiBurstOverlay(
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.24f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = rtl("כל הכבוד!"),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                )
            }

            if (displayText.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.22f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = rtl(displayText),
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                            ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
