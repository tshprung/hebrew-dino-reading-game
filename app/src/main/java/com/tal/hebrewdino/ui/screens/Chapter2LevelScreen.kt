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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.tal.hebrewdino.ui.domain.Chapter2Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Chapter2LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val station = stationId.coerceIn(1, Chapter2Config.STATION_COUNT)
    val letters = Chapter2Config.letters

    // Rotate targets so every letter repeats across stations.
    val targetLetter = remember(station) { letters[(station - 1) % letters.size] }

    // Station UX:
    // 1 -> 2 paths
    // 2 -> 3 paths
    // 3 -> 3 paths + safe detour loop
    // 4 -> memory moment + 3 paths
    // 5 -> 2 paths + “קיצור” on the correct letter
    // 6 -> three correct picks in a row (path clears between steps)
    val basePathCount =
        remember(station) {
            when (station) {
                1, 5 -> 2
                else -> 3
            }
        }

    // Build path options (include target + others).
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

    // Station 4: memory reveal moment.
    var memoryPhase by rememberSaveable(station) { mutableStateOf(if (station == 4) MemoryPhase.Show else MemoryPhase.Done) }
    val hideCurtain = remember(station) { Animatable(0f) }

    // Station 6: three targets in one screen.
    val station6Targets = remember(station) { listOf(letters[4], letters[0], letters[2]) }
    var station6Step by rememberSaveable(station) { mutableIntStateOf(0) }
    val station6Target =
        remember(station, station6Step) {
            if (station == 6) station6Targets[station6Step.coerceIn(0, station6Targets.lastIndex)] else targetLetter
        }
    val optionsStation6 =
        remember(station, station6Step) {
            if (station != 6) emptyList()
            else {
                val t = station6Targets[station6Step.coerceIn(0, station6Targets.lastIndex)]
                val others = letters.filter { it != t }.shuffled()
                listOf(t, others[0], others[1]).shuffled()
            }
        }

    LaunchedEffect(station) {
        if (station != 4) return@LaunchedEffect
        memoryPhase = MemoryPhase.Show
        hideCurtain.snapTo(0f)
        delay(650)
        memoryPhase = MemoryPhase.Hide
        hideCurtain.animateTo(1f, tween(220))
        delay(220)
        memoryPhase = MemoryPhase.Choose
    }

    LaunchedEffect(station) {
        if (station == 6) station6Step = 0
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

            // Target letter bubble (visual hint instead of audio)
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
                    text = if (station == 6) station6Target else targetLetter,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0B2B3D),
                )
            }

            if (station == 6) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rtl("שלב ${station6Step + 1} מתוך 3 — ממשיכים באותו מסך"),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Station 4: memory moment (show -> hide -> choose).
            if (station == 4 && memoryPhase != MemoryPhase.Choose) {
                MemoryMomentCard(
                    letter = targetLetter,
                    phase = memoryPhase,
                    curtainT = hideCurtain.value,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val activeTarget = if (station == 6) station6Target else targetLetter
            val activeOptions = if (station == 6) optionsStation6 else options
            val shortcutLaneIndex =
                if (station == 5) activeOptions.indexOf(activeTarget).takeIf { it >= 0 } else null
            val pathHaze =
                if (station == 6) {
                    (0.55f - 0.14f * station6Step).coerceIn(0.18f, 0.55f)
                } else {
                    0f
                }

            // Paths mini-game area
            PathChoiceArea(
                options = activeOptions,
                targetLetter = activeTarget,
                pathCount = if (station == 6) 3 else pathCount,
                locked = locked,
                shakePx = shake.value,
                detourT = detour.value,
                shortcutLaneIndex = shortcutLaneIndex,
                pathHazeAlpha = pathHaze,
                enabled = station != 4 || memoryPhase == MemoryPhase.Choose,
                onPick = { picked ->
                    if (locked) return@PathChoiceArea
                    if (station == 4 && memoryPhase != MemoryPhase.Choose) return@PathChoiceArea
                    if (picked == activeTarget) {
                        scope.launch {
                            locked = true
                            dinoState = DinoState.Jump
                            dinoScale.snapTo(1f)
                            val jumpPeak = if (station == 5) 1.22f else 1.16f
                            dinoScale.animateTo(jumpPeak, tween(if (station == 5) 90 else 120))
                            dinoScale.animateTo(1f, tween(if (station == 5) 130 else 160))
                            if (station == 5) {
                                // “קיצור”: קפיצה קצרה קדימה במסלול
                                delay(90)
                            }
                            delay(if (station == 5) 180 else 260)
                            when (station) {
                                6 -> {
                                    if (station6Step < 2) {
                                        station6Step += 1
                                        dinoState = DinoState.Idle
                                        locked = false
                                    } else {
                                        onComplete(station, 3, wrongCount)
                                    }
                                }
                                else -> onComplete(station, 1, wrongCount)
                            }
                        }
                    } else {
                        scope.launch {
                            locked = true
                            wrongCount += 1
                            dinoState = DinoState.Think
                            // Hidden difficulty: after repeated mistakes, reduce options (3 -> 2).
                            if (station != 6 && wrongCount >= 2 && pathCount > 2) {
                                pathCount = 2
                                options = listOf(targetLetter, options.first { it != targetLetter }).shuffled()
                            }
                            shake.snapTo(0f)
                            val amp = 18f
                            repeat(4) { i ->
                                shake.animateTo(if (i % 2 == 0) amp else -amp, tween(45))
                            }
                            shake.animateTo(0f, tween(60))
                            // Station 3: safe detour loop (walk a bit, then return).
                            if (station == 3) {
                                detour.snapTo(0f)
                                detour.animateTo(1f, tween(280))
                                detour.animateTo(0f, tween(320))
                            } else if (station == 5) {
                                // טעות בקיצור: מסלול ארוך יותר
                                detour.snapTo(0f)
                                detour.animateTo(1f, tween(420))
                                delay(120)
                                detour.animateTo(0f, tween(520))
                            } else {
                                // Safe pause (no punishment)
                                delay(220)
                            }
                            dinoState = DinoState.Idle
                            locked = false
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dino near the interaction, mild neutral reactions
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
private enum class MemoryPhase { Show, Hide, Choose, Done }

@Composable
private fun MemoryMomentCard(
    letter: String,
    phase: MemoryPhase,
    curtainT: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(140.dp)
                .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
                .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        // The letter itself (briefly shown)
        val showLetter = phase == MemoryPhase.Show
        Text(
            text = if (showLetter) letter else "",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )

        // Playful “leaf/curtain” hiding animation
        if (phase == MemoryPhase.Hide) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val coverW = w * (0.25f + 0.80f * curtainT)
                drawRoundRect(
                    color = Color(0xFF2E7D32).copy(alpha = 0.30f),
                    topLeft = Offset(w - coverW, 0f),
                    size = androidx.compose.ui.geometry.Size(coverW, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f),
                )
                drawCircle(
                    color = Color(0xFF2E7D32).copy(alpha = 0.25f),
                    radius = 42f,
                    center = Offset(w - coverW * 0.6f, h * 0.35f),
                )
                drawCircle(
                    color = Color(0xFF2E7D32).copy(alpha = 0.18f),
                    radius = 58f,
                    center = Offset(w - coverW * 0.35f, h * 0.70f),
                )
            }
        }
    }
}

@Composable
private fun PathChoiceArea(
    options: List<String>,
    targetLetter: String,
    pathCount: Int,
    locked: Boolean,
    shakePx: Float,
    detourT: Float,
    shortcutLaneIndex: Int? = null,
    pathHazeAlpha: Float = 0f,
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
        // Draw 2–3 curved sandy paths (RTL feeling: paths go from right -> left).
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
                drawPath(
                    path = p,
                    color = Color.White.copy(alpha = 0.18f),
                    style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            // Station 3 detour overlay: a faint “side path” that appears during detour.
            if (detourT > 0.01f) {
                val y = h * 0.22f
                val p = Path()
                p.moveTo(w * 0.70f, y)
                p.cubicTo(w * 0.60f, y + h * 0.10f, w * 0.46f, y + h * 0.20f, w * 0.34f, y + h * 0.24f)
                drawPath(
                    path = p,
                    color = Color(0xFF2AA6C9).copy(alpha = 0.12f * detourT),
                    style = Stroke(width = (24f * detourT).coerceAtLeast(1f), cap = StrokeCap.Round),
                )
            }

            if (pathHazeAlpha > 0.01f) {
                drawRect(color = Color(0xFFECEFF1).copy(alpha = 0.45f * pathHazeAlpha.coerceIn(0f, 1f)))
            }
        }

        // Letter markers (tap targets)
        val laneYs =
            if (pathCount == 2) listOf(0.28f, 0.62f) else listOf(0.20f, 0.46f, 0.74f)

        options.take(pathCount).forEachIndexed { idx, letter ->
            val yFrac = laneYs[idx]
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-14).dp, y = (yFrac * 240f).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (shortcutLaneIndex != null && shortcutLaneIndex == idx) {
                    Text(
                        text = "קיצור",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF1565C0),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Box(
                    modifier =
                        Modifier
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
}

