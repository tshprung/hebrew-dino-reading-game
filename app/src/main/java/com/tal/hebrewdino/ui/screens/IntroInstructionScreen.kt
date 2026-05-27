package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.IntroMediaClips
import com.tal.hebrewdino.ui.audio.InteractionAudio
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.components.lottie.EggLottiePhase
import com.tal.hebrewdino.ui.components.lottie.EggLottieVisual
import com.tal.hebrewdino.ui.domain.DinoStoryScripts
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntroInstructionScreen(
    onHatchComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IntroInstructionViewModel =
        viewModel(factory = IntroInstructionViewModel.Factory(LocalContext.current)),
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val sfx = remember(appContext) { SfxManager(appContext) }

    val character by viewModel.character.collectAsState()
    val skipToHome by viewModel.skipToHome.collectAsState()

    var navigated by remember { mutableStateOf(false) }

    fun finishIntro() {
        if (navigated) return
        navigated = true
        InteractionAudio.stopAllNow(appContext)
        onHatchComplete()
    }

    LaunchedEffect(skipToHome) {
        if (skipToHome) finishIntro()
    }

    val lottiePhase =
        when (viewModel.hatchPhase) {
            IntroHatchPhase.IDLE -> EggLottiePhase.IDLE_WIGGLE
            IntroHatchPhase.CRACKING,
            IntroHatchPhase.DONE,
            -> EggLottiePhase.CRACK
        }

    LaunchedEffect(viewModel.hatchPhase) {
        if (viewModel.hatchPhase == IntroHatchPhase.CRACKING) {
            sfx.playEggCrack()
        }
    }

    DisposableEffect(Unit) {
        onDispose { sfx.release() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 4.dp, start = 8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    InteractionAudio.stopAllNow(appContext)
                    onBack()
                },
                colors = ChapterNavChipStyles.outlinedButtonColors(),
            ) {
                Text("חזור", style = ChapterNavChipStyles.labelTextStyle())
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.28f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DinoStoryScripts.part1IntroDisplayLines().forEach { line ->
                        Text(
                            text = rtl(line),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp,
                                ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val eggSize = maxWidth.coerceAtMost(280.dp)
                if (viewModel.isReady && !skipToHome) {
                    EggLottieVisual(
                        character = character,
                        phase = lottiePhase,
                        tapImpulseEpoch = viewModel.eggTapEpoch,
                        onEggTapped = {
                            scope.launch {
                                if (viewModel.hatchPhase == IntroHatchPhase.IDLE) {
                                    sfx.playEggTap()
                                }
                                viewModel.onEggTapped()
                            }
                        },
                        onCrackAnimationEnd = {
                            scope.launch {
                                viewModel.finalizeHatch()
                                delay(IntroMediaClips.CRACK_HOLD_MS)
                                finishIntro()
                            }
                        },
                        size = eggSize,
                    )
                }
            }

            Text(
                text = rtl("הקישו על הביצה - טוק, טוק, טוק!"),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
