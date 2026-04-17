package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter3Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Chapter3LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val station = stationId.coerceIn(1, Chapter3Config.STATION_COUNT)
    val letters = Chapter3Config.letters

    val targetLetter = remember(station) { letters[(station - 1) % letters.size] }

    val basePathCount = remember(station) { if (station == 1) 2 else 3 }
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
    var dinoState by remember(station) { mutableStateOf(Ch3DinoVisual.Idle) }

    val whoOptions =
        remember(station, targetLetter) {
            if (station != 2) emptyList()
            else {
                val others = letters.filter { it != targetLetter }.shuffled()
                listOf(targetLetter, others[0], others[1], others[2]).shuffled()
            }
        }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "pulseTarget",
    )

    fun rtl(text: String) = "\u200F$text"

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
                ) {
                    Text("חזור")
                }
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
                    if (station == 2) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rtl("מי קרא?"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0B2B3D),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when {
                station <= 1 -> {
                    PathChoiceAreaCh3(
                        options = options,
                        pathCount = pathCount,
                        locked = locked,
                        shakePx = shake.value,
                        enabled = true,
                        onPick = { picked ->
                            if (locked) return@PathChoiceAreaCh3
                            if (picked == targetLetter) {
                                scope.launch {
                                    locked = true
                                    dinoState = Ch3DinoVisual.Jump
                                    dinoScale.snapTo(1f)
                                    dinoScale.animateTo(1.16f, tween(120))
                                    dinoScale.animateTo(1f, tween(160))
                                    delay(260)
                                    onComplete(station, 1, wrongCount)
                                }
                            } else {
                                scope.launch {
                                    locked = true
                                    wrongCount += 1
                                    dinoState = Ch3DinoVisual.Think
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
                                    delay(220)
                                    dinoState = Ch3DinoVisual.Idle
                                    locked = false
                                }
                            }
                        },
                    )
                }
                station == 2 -> {
                    WhoCalledGrid(
                        letters = whoOptions,
                        locked = locked,
                        shakePx = shake.value,
                        onPick = { picked ->
                            if (locked) return@WhoCalledGrid
                            if (picked == targetLetter) {
                                scope.launch {
                                    locked = true
                                    dinoState = Ch3DinoVisual.Jump
                                    dinoScale.snapTo(1f)
                                    dinoScale.animateTo(1.16f, tween(120))
                                    dinoScale.animateTo(1f, tween(160))
                                    delay(260)
                                    onComplete(station, 1, wrongCount)
                                }
                            } else {
                                scope.launch {
                                    locked = true
                                    wrongCount += 1
                                    dinoState = Ch3DinoVisual.Think
                                    shake.snapTo(0f)
                                    val amp = 18f
                                    repeat(4) { i ->
                                        shake.animateTo(if (i % 2 == 0) amp else -amp, tween(45))
                                    }
                                    shake.animateTo(0f, tween(60))
                                    delay(220)
                                    dinoState = Ch3DinoVisual.Idle
                                    locked = false
                                }
                            }
                        },
                    )
                }
                else -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
                                .padding(16.dp),
                    ) {
                        Text(
                            text = rtl("תחנות 3–6 בפרק 3 יתווספו בהמשך."),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

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
                modifier = Modifier.size(140.dp).scale(dinoScale.value),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private enum class Ch3DinoVisual { Idle, Think, Jump }

@Composable
private fun PathChoiceAreaCh3(
    options: List<String>,
    pathCount: Int,
    locked: Boolean,
    shakePx: Float,
    enabled: Boolean,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(260.dp)
                .offset(x = shakePx.dp)
                .background(Color.White.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                .padding(14.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val startX = w * 0.92f
            val endX = w * 0.14f
            val lanes = pathCount.coerceIn(2, 3)
            val ys =
                when (lanes) {
                    2 -> listOf(h * 0.35f, h * 0.68f)
                    else -> listOf(h * 0.26f, h * 0.50f, h * 0.76f)
                }
            ys.forEachIndexed { idx, y ->
                val p = Path()
                p.moveTo(startX, y)
                val c1 = Offset(w * 0.70f, y + (if (idx % 2 == 0) -h * 0.10f else h * 0.10f))
                val c2 = Offset(w * 0.40f, y + (if (idx % 2 == 0) h * 0.08f else -h * 0.08f))
                p.cubicTo(c1.x, c1.y, c2.x, c2.y, endX, y)
                drawPath(
                    path = p,
                    color = Color(0xFFE2C999).copy(alpha = 0.92f),
                    style = Stroke(width = 44f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = p,
                    color = Color(0xFF6B4A2A).copy(alpha = 0.22f),
                    style = Stroke(width = 50f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
        val laneYs = if (pathCount == 2) listOf(0.28f, 0.62f) else listOf(0.20f, 0.46f, 0.74f)
        options.take(pathCount).forEachIndexed { idx, letter ->
            val yFrac = laneYs[idx]
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-14).dp, y = (yFrac * 240f).dp)
                        .size(78.dp)
                        .background(
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color(0xFFFFF3C4).copy(alpha = 0.92f),
                                    ),
                            ),
                            shape = CircleShape,
                        )
                        .clickable(enabled = enabled && !locked) { onPick(letter) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0B2B3D),
                )
            }
        }
    }
}

@Composable
private fun WhoCalledGrid(
    letters: List<String>,
    locked: Boolean,
    shakePx: Float,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette =
        listOf(
            Color(0xFFFFE082),
            Color(0xFF80DEEA),
            Color(0xFFC5E1A5),
            Color(0xFFFFAB91),
        )
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
            letters.take(2).forEachIndexed { i, l ->
                val c = palette.getOrElse(i) { Color(0xFFFFF3C4) }
                Box(
                    modifier =
                        Modifier
                            .size(92.dp)
                            .background(c.copy(alpha = 0.95f), CircleShape)
                            .clickable(enabled = !locked) { onPick(l) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = l,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            letters.drop(2).take(2).forEachIndexed { i, l ->
                val c = palette.getOrElse(i + 2) { Color(0xFFFFF3C4) }
                Box(
                    modifier =
                        Modifier
                            .size(92.dp)
                            .background(c.copy(alpha = 0.95f), CircleShape)
                            .clickable(enabled = !locked) { onPick(l) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = l,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                    )
                }
            }
        }
    }
}
