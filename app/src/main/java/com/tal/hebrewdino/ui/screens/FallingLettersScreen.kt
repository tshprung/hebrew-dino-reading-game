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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
import com.tal.hebrewdino.ui.economy.RewardEngine
import com.tal.hebrewdino.ui.domain.HebrewSyllabus
import com.tal.hebrewdino.ui.domain.letterNameSpokenForTts
import com.tal.hebrewdino.ui.domain.letterSymbolForDisplay
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.domain.targetSuccessSpeech
import com.tal.hebrewdino.ui.domain.wrongLetterFeedbackSpeech
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.launch

@Composable
fun FallingLettersScreen(
    onExitToHome: () -> Unit,
    onRoundCompleteToSummary: () -> Unit,
    chapterIndex: Int = 0,
    rewardEngine: RewardEngine,
    modifier: Modifier = Modifier,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr,
    ) {
        val context = LocalContext.current
        val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
        val sfx = remember(context) { SfxManager(context.applicationContext) }
        val scope = rememberCoroutineScope()
        DisposableEffect(sfx) {
            onDispose { sfx.release() }
        }
        val chapterLetters =
            HebrewSyllabus.chapterOrNull(chapterIndex)?.letters
                ?: HebrewSyllabus.chapters.first().letters
        val vm: FallingLettersViewModel =
            viewModel(
                factory =
                    remember(rewardEngine, chapterIndex, chapterLetters) {
                        object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(FallingLettersViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return FallingLettersViewModel(
                                        chapterLetters = chapterLetters,
                                        rewardEngine = rewardEngine,
                                        stationRoundCompleted =
                                            StationRoundCompleted(
                                                chapterIndex = chapterIndex,
                                                stationId = 3,
                                            ),
                                    ) as T
                                }
                                error("Unknown ViewModel class: $modelClass")
                            }
                        }
                    },
            )

        val state by vm.uiState.collectAsState()

        var replayInstructionEpoch by remember { mutableIntStateOf(0) }
        val instructionInteractionEnabled = !state.isComplete && !state.inputsLocked
        val requestInstructionReplay = {
            if (instructionInteractionEnabled) replayInstructionEpoch += 1
        }
        val bumpInstructionActivity =
            rememberInstructionIdleReplay(
                enabled = instructionInteractionEnabled,
                resetKey = state.roundIndex,
                onReplay = requestInstructionReplay,
            )

        LaunchedEffect(state.roundIndex) {
            bumpInstructionActivity()
            tts.speakFully(fallingLettersInstructionSpeech(state.targetLetter))
        }

        LaunchedEffect(replayInstructionEpoch, state.roundIndex) {
            if (replayInstructionEpoch == 0) return@LaunchedEffect
            tts.speakFully(
                fallingLettersInstructionSpeech(state.targetLetter),
                navigationSettleMs = 80L,
            )
        }

        LaunchedEffect(state.feedbackToken, state.feedbackIsCorrect) {
            if (state.feedbackIsCorrect != false || state.feedbackToken == 0) return@LaunchedEffect
            val letterId = state.feedbackLetterId ?: return@LaunchedEffect
            val clicked = state.letters.firstOrNull { it.id == letterId } ?: return@LaunchedEffect
            scope.launch { sfx.playWrong() }
            tts.interruptAndSpeak(wrongLetterFeedbackSpeech(clicked.text))
        }

        LaunchedEffect(state.feedbackToken, state.feedbackIsCorrect, state.roundBreakToken) {
            if (state.feedbackIsCorrect != true || state.feedbackToken == 0) return@LaunchedEffect
            val reinforcement =
                targetSuccessSpeech(ChallengeType.LETTER_RECOGNITION, state.targetLetter)
            val breakToken = state.roundBreakToken
            if (breakToken > 0) {
                tts.interruptAndSpeakFully(reinforcement)
                val praise = RoundPraisePhrases[(0 until RoundPraisePhrases.size).random()]
                tts.interruptAndSpeakFully(praise)
                vm.onRoundBreakFinished(breakToken)
            } else {
                tts.interruptAndSpeak(reinforcement)
            }
        }

        LaunchedEffect(vm) {
            vm.finishEvents.collect {
                vm.stopTicker()
                onRoundCompleteToSummary()
            }
        }

        FallingLettersContent(
            state = state,
            onExit = {
                vm.stopTicker()
                onExitToHome()
            },
            onLetterClicked = { letterId ->
                bumpInstructionActivity()
                vm.onLetterClicked(letterId)
            },
            onReplayInstruction = {
                bumpInstructionActivity()
                requestInstructionReplay()
            },
            onViewport = vm::setViewport,
            sfx = sfx,
            modifier = modifier,
        )
    }
}

private fun fallingLettersInstructionSpeech(targetLetter: String): String {
    val name = letterNameSpokenForTts(targetLetter)
    return "תפסו את האות $name"
}

@Composable
private fun FallingLettersContent(
    state: FallingLettersUiState,
    onExit: () -> Unit,
    onLetterClicked: (Int) -> Unit,
    onReplayInstruction: () -> Unit,
    onViewport: (Float, Float) -> Unit,
    sfx: SfxManager,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
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
            onExit = onExit,
            onReplayInstruction = onReplayInstruction,
            instructionReplayEnabled = !state.isComplete && !state.inputsLocked,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp),
        )

        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 118.dp, bottom = 26.dp, start = 18.dp, end = 18.dp)
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
    onReplayInstruction: () -> Unit,
    instructionReplayEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetDisplay = letterSymbolForDisplay(targetLetter)
    val instructionText =
        ScreenFit.rtlUnicodeWrap("תפסו את האות $targetDisplay!")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.45f),
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
                onClick = onExit,
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.45f),
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Surface(
                onClick = { if (instructionReplayEnabled) onReplayInstruction() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0B2B3D).copy(alpha = 0.82f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.35f)),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = instructionText,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 26.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    if (instructionReplayEnabled) {
                        Text(
                            text = "🔊",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }
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
    LaunchedEffect(feedback?.token, feedback?.correct) {
        val fb = feedback ?: return@LaunchedEffect
        if (fb.correct) {
            starAlpha.snapTo(1f)
            starScale.snapTo(0.7f)
            starScale.animateTo(1.15f, tween(120))
            starScale.animateTo(1f, tween(90))
            starAlpha.animateTo(0f, tween(220))
        } else {
            shakeX.snapTo(0f)
            shakeX.animateTo(-10f, tween(45))
            shakeX.animateTo(10f, tween(80))
            shakeX.animateTo(-7f, tween(70))
            shakeX.animateTo(7f, tween(70))
            shakeX.animateTo(0f, tween(55))
        }
    }

    val bg =
        Brush.verticalGradient(
            colors =
                listOf(
                    Color.White.copy(alpha = 0.96f),
                    Color(0xFFE8F4FF).copy(alpha = 0.92f),
                ),
        )
    Box(
        modifier =
            modifier
                .graphicsLayer {
                    translationX = shakeX.value
                    alpha = if (enabled) 1f else 0.72f
                }
                .clickable(enabled = enabled, onClick = onClick),
    ) {
        Surface(
            shape = shape,
            color = Color.Transparent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(bg, shape),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0B2B3D).copy(alpha = 0.22f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = rtl(text),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 34.sp),
                    color = Color(0xFF0B2B3D),
                )
                if (feedback?.correct == true && starAlpha.value > 0.01f) {
                    Text(
                        text = "⭐",
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .scale(starScale.value)
                                .alpha(starAlpha.value),
                    )
                }
            }
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"

private data class LetterFeedback(
    val correct: Boolean,
    val token: Int,
)

private val RoundPraisePhrases: Array<String> =
    arrayOf(
        "כל הכבוד!",
        "יופי!",
    )
