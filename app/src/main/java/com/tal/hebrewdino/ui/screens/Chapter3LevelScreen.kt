package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.feedback.GameFeedback
import com.tal.hebrewdino.ui.components.learning.ChapterStartsWithPrompts
import com.tal.hebrewdino.ui.components.learning.LearningUxTiming
import com.tal.hebrewdino.ui.components.learning.LetterChoiceTile
import com.tal.hebrewdino.ui.components.learning.PictureLetterMatchStation
import com.tal.hebrewdino.ui.components.learning.PictureStartsWithLetterPanel
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter3LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun Chapter3LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter3Config.STATION_COUNT)
    when (station) {
        6 ->
            PictureLetterMatchStation(
                stationId = station,
                chapterTitle = "פרק 3 - מצא את החבר",
                questionCount = 3,
                initialGroupIndex = 0,
                letterPoolSpec = Chapter3LetterPoolSpec,
                onBack = onBack,
                onComplete = onComplete,
                onLettersHelp = onLettersHelp,
                onDebugStationAdvance = onDebugStationAdvance,
                modifier = modifier,
            )
        4 ->
            Chapter3PictureStartsWithStation(
                station = station,
                modifier = modifier,
                onBack = onBack,
                onComplete = onComplete,
            )
        2 ->
            Chapter3WhoCalledStation(
                station = station,
                modifier = modifier,
                onBack = onBack,
                onComplete = onComplete,
            )
        else -> {
            val plan =
                StationQuizPlans.chapter3LetterOnly(station)
                    ?: StationQuizPlans.chapter1(1)
            LetterQuizStationScreen(
                stationId = station,
                chapterTitle = "פרק 3 - מצא את החבר",
                stageLabel = "תחנה $station",
                plan = plan,
                letterPoolSpec = Chapter3LetterPoolSpec,
                onBack = onBack,
                onComplete = onComplete,
                onLettersHelp = onLettersHelp,
                onDebugStationAdvance = onDebugStationAdvance,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun Chapter3WhoCalledStation(
    station: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val sfx = remember(station) { SoundPoolPlayer(context) }
    val gameFeedback = remember(station, sfx, view) { GameFeedback(scope, sfx, view) }
    DisposableEffect(station) {
        onDispose { sfx.release() }
    }
    val letters = Chapter3Config.letters
    val targetLetter = remember(station) { letters[(station - 1) % letters.size] }
    val whoOptions =
        remember(station, targetLetter) {
            val others = letters.filter { it != targetLetter }.shuffled()
            listOf(targetLetter, others[0], others[1], others[2]).shuffled()
        }
    var wrongCount by remember(station) { mutableIntStateOf(0) }
    var locked by remember(station) { mutableStateOf(false) }
    val dinoScale = remember(station) { Animatable(1f) }
    val shake = remember(station) { Animatable(0f) }
    var dinoState by remember(station) { mutableStateOf(Ch3DinoVisual.Idle) }
    var lastTapMs by remember { mutableLongStateOf(0L) }
    var wrongFlashLetter by remember { mutableStateOf<String?>(null) }
    val dinoTilt = remember { Animatable(0f) }

    LaunchedEffect(wrongFlashLetter) {
        if (wrongFlashLetter != null) {
            delay(380)
            wrongFlashLetter = null
        }
    }

    fun tryConsumeTap(): Boolean {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastTapMs < LearningUxTiming.TapCooldownMs) return false
        lastTapMs = now
        return true
    }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.99f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(animation = tween(1400), repeatMode = RepeatMode.Reverse),
        label = "pulseTarget",
    )

    fun rtl(text: String) = "\u200F$text"

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.swamp_bg_chapter3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                OutlinedButton(
                    onClick = onBack,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) { Text("חזור") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "פרק 3 - מצא את החבר",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "תחנה $station",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier =
                    Modifier
                        .width(200.dp)
                        .height(110.dp)
                        .scale(pulse)
                        .background(Color.White.copy(alpha = 0.90f), RoundedCornerShape(22.dp))
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetLetter,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rtl("מי קרא?"),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0B2B3D),
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            WhoCalledGrid(
                letters = whoOptions,
                locked = locked,
                shakePx = shake.value,
                wrongFlashLetter = wrongFlashLetter,
                onPick = { picked ->
                    if (locked || !tryConsumeTap()) return@WhoCalledGrid
                    if (picked == targetLetter) {
                        scope.launch {
                            locked = true
                            dinoState = Ch3DinoVisual.Jump
                            gameFeedback.playSuccessBig()
                            dinoScale.snapTo(1f)
                            dinoScale.animateTo(1.12f, tween(120))
                            dinoScale.animateTo(1f, tween(160))
                            delay(LearningUxTiming.AfterCorrectHoldMs)
                            onComplete(station, 1, wrongCount)
                        }
                    } else {
                        scope.launch {
                            locked = true
                            wrongFlashLetter = picked
                            wrongCount += 1
                            gameFeedback.playWrong()
                            dinoState = Ch3DinoVisual.Think
                            dinoTilt.snapTo(0f)
                            dinoTilt.animateTo(-5f, tween(90))
                            dinoTilt.animateTo(5f, tween(90))
                            dinoTilt.animateTo(0f, tween(120))
                            shake.snapTo(0f)
                            val amp = 18f
                            repeat(4) { i ->
                                shake.animateTo(if (i % 2 == 0) amp else -amp, tween(45))
                            }
                            shake.animateTo(0f, tween(60))
                            delay(LearningUxTiming.AfterWrongHoldMs)
                            dinoState = Ch3DinoVisual.Idle
                            locked = false
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(18.dp))
            val dinoRes =
                when (dinoState) {
                    Ch3DinoVisual.Idle -> R.drawable.dino_idle
                    Ch3DinoVisual.Think -> R.drawable.dino_try_again
                    Ch3DinoVisual.Jump -> R.drawable.dino_jump_1
                }
            Image(
                painter = painterResource(id = dinoRes),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(140.dp)
                        .graphicsLayer { rotationZ = dinoTilt.value }
                        .scale(dinoScale.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun Chapter3PictureStartsWithStation(
    station: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letters = Chapter3Config.letters
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val sfx = remember(station) { SoundPoolPlayer(context) }
    val gameFeedback = remember(station, sfx, view) { GameFeedback(scope, sfx, view) }
    DisposableEffect(station) {
        onDispose { sfx.release() }
    }
    val prompt =
        remember(station) {
            ChapterStartsWithPrompts.all
                .filter { it.startingLetter in letters }
                .shuffled(Random(station * 7919L))
                .first()
        }
    val optionLetters =
        remember(prompt) {
            buildList {
                add(prompt.startingLetter)
                addAll(letters.filter { it != prompt.startingLetter }.shuffled().take(2))
            }.shuffled(Random((prompt.caption.hashCode() * 31 + station).toLong()))
        }
    var wrongCount by remember(station) { mutableIntStateOf(0) }
    var locked by remember(station) { mutableStateOf(false) }
    val dinoScale = remember(station) { Animatable(1f) }
    val shake = remember(station) { Animatable(0f) }
    var dinoState by remember(station) { mutableStateOf(Ch3DinoVisual.Idle) }
    val dinoTilt = remember { Animatable(0f) }

    val pulse by rememberInfiniteTransition(label = "pulsePic").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "pulsePicV",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.swamp_bg_chapter3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                OutlinedButton(
                    onClick = onBack,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) { Text("חזור") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "פרק 3 - מצא את החבר",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "תחנה $station",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(16.dp))
            PictureStartsWithLetterPanel(
                prompt = prompt,
                optionLetters = optionLetters,
                enabled = !locked,
                shakePx = shake.value,
                onPickLetter = { picked ->
                    if (locked) return@PictureStartsWithLetterPanel
                    if (picked == prompt.startingLetter) {
                        scope.launch {
                            locked = true
                            dinoState = Ch3DinoVisual.Jump
                            gameFeedback.playSuccessBig()
                            dinoScale.snapTo(1f)
                            dinoScale.animateTo(1.12f, tween(120))
                            dinoScale.animateTo(1f, tween(160))
                            delay(LearningUxTiming.AfterCorrectHoldMs)
                            onComplete(station, 1, wrongCount)
                        }
                    } else {
                        scope.launch {
                            locked = true
                            wrongCount += 1
                            gameFeedback.playWrong()
                            dinoState = Ch3DinoVisual.Think
                            dinoTilt.snapTo(0f)
                            dinoTilt.animateTo(-5f, tween(90))
                            dinoTilt.animateTo(5f, tween(90))
                            dinoTilt.animateTo(0f, tween(120))
                            shake.snapTo(0f)
                            val amp = 18f
                            repeat(4) { i ->
                                shake.animateTo(if (i % 2 == 0) amp else -amp, tween(45))
                            }
                            shake.animateTo(0f, tween(60))
                            delay(LearningUxTiming.AfterWrongHoldMs)
                            dinoState = Ch3DinoVisual.Idle
                            locked = false
                        }
                    }
                },
                modifier = Modifier.scale(pulse),
            )
            Spacer(modifier = Modifier.height(18.dp))
            val dinoRes =
                when (dinoState) {
                    Ch3DinoVisual.Idle -> R.drawable.dino_idle
                    Ch3DinoVisual.Think -> R.drawable.dino_try_again
                    Ch3DinoVisual.Jump -> R.drawable.dino_jump_1
                }
            Image(
                painter = painterResource(id = dinoRes),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(140.dp)
                        .graphicsLayer { rotationZ = dinoTilt.value }
                        .scale(dinoScale.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private enum class Ch3DinoVisual { Idle, Think, Jump }

@Composable
private fun WhoCalledGrid(
    letters: List<String>,
    locked: Boolean,
    shakePx: Float,
    wrongFlashLetter: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .offset(x = shakePx.dp)
                .background(Color.White.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            letters.take(2).forEach { l ->
                LetterChoiceTile(
                    letter = l,
                    tileSize = 88.dp,
                    haloActive = false,
                    enabled = !locked,
                    wrongDimmed = wrongFlashLetter == l,
                    onClick = { onPick(l) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            letters.drop(2).take(2).forEach { l ->
                LetterChoiceTile(
                    letter = l,
                    tileSize = 88.dp,
                    haloActive = false,
                    enabled = !locked,
                    wrongDimmed = wrongFlashLetter == l,
                    onClick = { onPick(l) },
                )
            }
        }
    }
}
