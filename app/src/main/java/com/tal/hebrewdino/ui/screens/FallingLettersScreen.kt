package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.launch

@Composable
fun FallingLettersScreen(
    onExitToHome: () -> Unit,
    onRoundCompleteToSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr,
    ) {
        val context = LocalContext.current
        val repo = remember(context) { CharacterRepository(context.applicationContext) }
        val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
        val sfx = remember(context) { SfxManager(context.applicationContext) }
        DisposableEffect(sfx) {
            onDispose { sfx.release() }
        }
        DisposableEffect(tts) {
            onDispose { tts.stop() }
        }
        val vm: FallingLettersViewModel =
            viewModel(
                factory =
                    remember(repo) {
                        object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(FallingLettersViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return FallingLettersViewModel(
                                        rewardHandler =
                                            FallingLettersViewModel.RewardHandler {
                                                repo.markChapter1StationCompleted(3)
                                                repo.setFullUntilAtMs(0L)
                                                repo.addFood(3)
                                                repo.setPendingRewardFoodDelta(3)
                                            },
                                    ) as T
                                }
                                error("Unknown ViewModel class: $modelClass")
                            }
                        }
                    },
            )

        val state by vm.uiState.collectAsState()

        LaunchedEffect(vm) {
            vm.finishEvents.collect {
                tts.stop()
                vm.stopTicker()
                onRoundCompleteToSummary()
            }
        }
        LaunchedEffect(vm) {
            vm.roundBreakEvents.collect { token ->
                tts.speakAndWait(RoundPraisePhrases[(0 until RoundPraisePhrases.size).random()])
                vm.onRoundBreakFinished(token)
            }
        }

        FallingLettersContent(
            state = state,
            onExit = {
                tts.stop()
                vm.stopTicker()
                onExitToHome()
            },
            onLetterClicked = vm::onLetterClicked,
            onViewport = vm::setViewport,
            sfx = sfx,
            modifier = modifier,
        )
    }
}

@Composable
private fun FallingLettersContent(
    state: FallingLettersUiState,
    onExit: () -> Unit,
    onLetterClicked: (Int) -> Unit,
    onViewport: (Float, Float) -> Unit,
    sfx: SfxManager,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        TopBarFallingLetters(
            targetLetter = state.targetLetter,
            progressText =
                rtl("סיבוב ${state.roundIndex + 1} מתוך ${state.roundsTotal} · תפסת ${state.caughtInRound} מתוך ${state.quotaInRound}"),
            onExit = {
                onExit()
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp),
        )

        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 86.dp, bottom = 26.dp, start = 18.dp, end = 18.dp)
                    .clipToBounds(),
        ) {
            val heightDp = maxHeight
            val laneWidthDp = maxWidth / 3f
            val chipWidthDp = minOf(88.dp, (laneWidthDp - 10.dp).coerceAtLeast(56.dp))
            val heightPx = with(density) { heightDp.toPx() }
            val chipHeightPx = with(density) { 60.dp.toPx() }
            LaunchedEffect(heightPx, chipHeightPx) {
                onViewport(heightPx, chipHeightPx)
            }

            Row(modifier = Modifier.fillMaxSize()) {
                repeat(3) { lane ->
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clipToBounds(),
                    ) {
                        state.letters.forEach { letter ->
                            if (letter.lane != lane) return@forEach

                            val yDp = with(density) { letter.yPx.toDp() }
                            val freeX = (laneWidthDp - chipWidthDp).coerceAtLeast(0.dp)
                            val xDp = freeX * letter.xInLane01
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = xDp, y = yDp),
                            ) {
                                LetterChip(
                                    text = letter.text,
                                    onClick = {
                                        if (state.inputsLocked) return@LetterChip
                                        val correct = letter.text == state.targetLetter
                                        if (correct) {
                                            scope.launch { sfx.playCorrect() }
                                        }
                                        onLetterClicked(letter.id)
                                    },
                                    enabled = !state.inputsLocked,
                                    feedback =
                                        if (state.feedbackLetterId == letter.id && state.feedbackIsCorrect != null) {
                                            LetterFeedback(
                                                correct = state.feedbackIsCorrect == true,
                                                token = state.feedbackToken,
                                            )
                                        } else {
                                            null
                                        },
                                    modifier = Modifier.size(width = chipWidthDp, height = 60.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarFallingLetters(
    targetLetter: String,
    progressText: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.28f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.28f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = rtl("תפסו את האות: ${targetLetter}׳"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }

        Surface(
            onClick = onExit,
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.28f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = rtl("יציאה"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun LetterChip(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    feedback: LetterFeedback?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val shakeX = remember { Animatable(0f) }
    val starAlpha = remember { Animatable(0f) }
    val starScale = remember { Animatable(0.7f) }
    LaunchedEffect(feedback?.token) {
        val f = feedback ?: return@LaunchedEffect
        if (f.correct) {
            starAlpha.snapTo(0f)
            starScale.snapTo(0.7f)
            starAlpha.animateTo(1f, tween(70))
            starScale.animateTo(1f, tween(120))
            starAlpha.animateTo(0f, tween(260))
        } else {
            shakeX.snapTo(0f)
            shakeX.animateTo(-9f, tween(40))
            shakeX.animateTo(9f, tween(70))
            shakeX.animateTo(-6f, tween(60))
            shakeX.animateTo(6f, tween(60))
            shakeX.animateTo(0f, tween(50))
        }
    }

    val borderColor =
        when (feedback?.correct) {
            true -> Color(0xFF2DB86E)
            false -> Color(0xFFFF6B6B)
            null -> Color.Transparent
        }

    Surface(
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
            modifier
                .border(width = 3.dp, color = borderColor, shape = shape)
                .graphicsLayer { translationX = shakeX.value },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.92f), shape = shape)
                    .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rtl(text),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 48.sp),
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
            if (feedback?.correct == true && starAlpha.value > 0f) {
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-10).dp)
                            .alpha(starAlpha.value)
                            .scale(starScale.value),
                )
            }
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"

private data class LetterFeedback(
    val correct: Boolean,
    val token: Int,
)

private fun hebrewLetterNameForSpeech(letter: String): String? =
    when (letter) {
        "א" -> "אָלֶף"
        "ב" -> "בֵּית"
        "ג" -> "גִּימֶל"
        "ד" -> "דָּלֶת"
        "ה" -> "הֵא"
        "ו" -> "וָו"
        "ז" -> "זַיִן"
        "ח" -> "חֵית"
        "ט" -> "טֵית"
        "י" -> "יוֹד"
        "כ", "ך" -> "כָּף"
        "ל" -> "לָמֶד"
        "מ", "ם" -> "מֵם"
        "נ", "ן" -> "נוּן"
        "ס" -> "סָמֶךְ"
        "ע" -> "עַיִן"
        "פ", "ף" -> "פֵּא"
        "צ", "ץ" -> "צַדִּי"
        "ק" -> "קוּף"
        "ר" -> "רֵישׁ"
        "ש" -> "שִׁין"
        "ת" -> "תָּו"
        else -> null
    }

private val RoundPraisePhrases: Array<String> =
    arrayOf(
        "כל הכבוד!",
        "יופי!",
    )
