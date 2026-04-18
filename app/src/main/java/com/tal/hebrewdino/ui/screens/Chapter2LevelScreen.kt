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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.StationMatchPlaceholders
import com.tal.hebrewdino.ui.components.learning.ChapterStartsWithPrompts
import com.tal.hebrewdino.ui.components.learning.PictureLetterMatchStation
import com.tal.hebrewdino.ui.components.learning.PictureStartsWithLetterPanel
import com.tal.hebrewdino.ui.components.learning.LearningUxTiming
import com.tal.hebrewdino.ui.components.learning.PathLetterPathChoice
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter2LetterPoolSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun Chapter2LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter2Config.STATION_COUNT)
    when (station) {
        6 ->
            PictureLetterMatchStation(
                stationId = station,
                chapterTitle = "פרק 2 - חוזרים למערה",
                questionCount = 3,
                initialGroupIndex = 0,
                letterPoolSpec = Chapter2LetterPoolSpec,
                matchPlaceholders = StationMatchPlaceholders.forest,
                onBack = onBack,
                onComplete = onComplete,
                modifier = modifier,
            )
        4 ->
            Chapter2PictureStartsWithStation(
                station = station,
                modifier = modifier,
                onBack = onBack,
                onComplete = onComplete,
            )
        else ->
            Chapter2PathOnlyStation(
                station = station,
                modifier = modifier,
                onBack = onBack,
                onComplete = onComplete,
            )
    }
}

@Composable
private fun Chapter2PictureStartsWithStation(
    station: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letters = Chapter2Config.letters
    val scope = rememberCoroutineScope()
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
    var dinoState by remember(station) { mutableStateOf(DinoState.Idle) }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "pulsePic",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_level_overlay),
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
                text = "פרק 2 - חוזרים למערה",
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
                            dinoState = DinoState.Jump
                            dinoScale.snapTo(1f)
                            dinoScale.animateTo(1.14f, tween(120))
                            dinoScale.animateTo(1f, tween(160))
                            delay(LearningUxTiming.AfterCorrectHoldMs)
                            onComplete(station, 1, wrongCount)
                        }
                    } else {
                        scope.launch {
                            locked = true
                            wrongCount += 1
                            dinoState = DinoState.Think
                            shake.snapTo(0f)
                            val amp = 18f
                            repeat(4) { i ->
                                shake.animateTo(if (i % 2 == 0) amp else -amp, tween(45))
                            }
                            shake.animateTo(0f, tween(60))
                            delay(LearningUxTiming.AfterWrongHoldMs)
                            dinoState = DinoState.Idle
                            locked = false
                        }
                    }
                },
                modifier = Modifier.scale(pulse),
            )
            Spacer(modifier = Modifier.height(18.dp))
            val dinoRes =
                when (dinoState) {
                    DinoState.Idle -> R.drawable.dino_idle
                    DinoState.Think -> R.drawable.dino_try_again
                    DinoState.Jump -> R.drawable.dino_jump_1
                }
            Image(
                painter = painterResource(id = dinoRes),
                contentDescription = null,
                modifier = Modifier.size(140.dp).scale(dinoScale.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun Chapter2PathOnlyStation(
    station: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val letters = Chapter2Config.letters
    val targetLetter = remember(station) { letters[(station - 1) % letters.size] }
    val basePathCount =
        remember(station) {
            when (station) {
                1, 5 -> 2
                else -> 3
            }
        }
    val initialOptions =
        remember(station) {
            val others = letters.filter { it != targetLetter }
            buildList {
                add(targetLetter)
                addAll(others.take(basePathCount - 1))
            }.shuffled()
        }
    var options by remember(station) { mutableStateOf(initialOptions) }
    var pathCount by remember(station) { mutableIntStateOf(basePathCount) }
    var wrongCount by remember(station) { mutableIntStateOf(0) }
    var locked by remember(station) { mutableStateOf(false) }
    val dinoScale = remember(station) { Animatable(1f) }
    val shake = remember(station) { Animatable(0f) }
    var dinoState by remember(station) { mutableStateOf(DinoState.Idle) }
    val detour = remember(station) { Animatable(0f) }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "pulseTarget",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_level_overlay),
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
                text = "פרק 2 - חוזרים למערה",
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
                        .width(180.dp)
                        .height(110.dp)
                        .scale(pulse)
                        .background(Color.White.copy(alpha = 0.90f), RoundedCornerShape(22.dp))
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = targetLetter,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0B2B3D),
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            val shortcutLaneIndex =
                if (station == 5) options.indexOf(targetLetter).takeIf { it >= 0 } else null
            PathLetterPathChoice(
                options = options,
                targetLetter = targetLetter,
                pathCount = pathCount,
                locked = locked,
                shakePx = shake.value,
                detourT = detour.value,
                shortcutLaneIndex = shortcutLaneIndex,
                pathHazeAlpha = 0f,
                enabled = true,
                onPickResolved = { _, correct ->
                    if (!locked) {
                        if (correct) {
                            scope.launch {
                                locked = true
                                dinoState = DinoState.Jump
                                dinoScale.snapTo(1f)
                                val jumpPeak = if (station == 5) 1.22f else 1.16f
                                dinoScale.animateTo(jumpPeak, tween(if (station == 5) 90 else 120))
                                dinoScale.animateTo(1f, tween(if (station == 5) 130 else 160))
                                if (station == 5) delay(90)
                                delay(LearningUxTiming.AfterCorrectHoldMs)
                                onComplete(station, 1, wrongCount)
                            }
                        } else {
                            scope.launch {
                                locked = true
                                wrongCount += 1
                                dinoState = DinoState.Think
                                if (wrongCount >= 2 && pathCount > 2) {
                                    pathCount = 2
                                    options = listOf(targetLetter, options.first { it != targetLetter }).shuffled()
                                }
                                shake.snapTo(0f)
                                val amp = 18f
                                repeat(4) { i ->
                                    shake.animateTo(if (i % 2 == 0) amp else -amp, tween(45))
                                }
                                shake.animateTo(0f, tween(60))
                                if (station == 3) {
                                    detour.snapTo(0f)
                                    detour.animateTo(1f, tween(280))
                                    detour.animateTo(0f, tween(320))
                                } else if (station == 5) {
                                    detour.snapTo(0f)
                                    detour.animateTo(1f, tween(420))
                                    delay(120)
                                    detour.animateTo(0f, tween(520))
                                }
                                delay(LearningUxTiming.AfterWrongHoldMs)
                                dinoState = DinoState.Idle
                                locked = false
                            }
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(18.dp))
            val dinoRes =
                when (dinoState) {
                    DinoState.Idle -> R.drawable.dino_idle
                    DinoState.Think -> R.drawable.dino_try_again
                    DinoState.Jump -> R.drawable.dino_jump_1
                }
            Image(
                painter = painterResource(id = dinoRes),
                contentDescription = null,
                modifier = Modifier.size(140.dp).scale(dinoScale.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private enum class DinoState { Idle, Think, Jump }
