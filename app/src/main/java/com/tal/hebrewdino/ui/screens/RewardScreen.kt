package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.key
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RewardScreen(
    levelId: Int,
    correct: Int,
    mistakes: Int,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun rtl(text: String): String = "\u200F$text"

    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    val sfx = remember { SoundPoolPlayer(context = context) }
    var navigatedAway by remember(levelId) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voice.release()
            sfx.release()
        }
    }

    val balloonCount = remember(levelId, correct, mistakes) {
        val base = 5
        val bonus = max(0, correct - mistakes) / 2
        max(1, min(10, base + bonus))
    }
    val balloons = remember(levelId, balloonCount) {
        mutableStateListOf(*Array(balloonCount) { true })
    }

    val phases =
        remember(balloonCount) {
            List(balloonCount) {
                Random(it * 7919L + balloonCount).nextFloat() * 1000f
            }
        }

    LaunchedEffect(levelId) {
        voice.playBlocking(AudioClips.VoLevelDone)
    }

    LaunchedEffect(balloonCount) {
        if (balloonCount <= 0) return@LaunchedEffect
        snapshotFlow { balloons.count { it } }.first { it == 0 }
        delay(500)
        if (!navigatedAway) {
            navigatedAway = true
            onBackToMap()
        }
    }

    val drift = rememberInfiniteTransition(label = "drift")
    val driftPhase by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(14_000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "phase",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_reward),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = {
                        if (!navigatedAway) {
                            navigatedAway = true
                            onBackToMap()
                        }
                    },
                    modifier = Modifier.width(140.dp),
                    colors =
                        androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.86f),
                            contentColor = Color(0xFF0B2B3D),
                        ),
                ) {
                    Text("חזור")
                }
            }
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rtl("תפוצצו את הבלונים — הם טסים על המסך!"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(10.dp))

            BoxWithConstraints(
                modifier =
                    Modifier
                        .weight(1f, fill = true)
                        .fillMaxSize(),
            ) {
                val density = LocalDensity.current
                val wPx = with(density) { maxWidth.toPx() }
                val hPx = with(density) { maxHeight.toPx() }
                balloons.forEachIndexed { idx, alive ->
                    if (!alive) return@forEachIndexed
                    val baseXPx = wPx * (0.10f + (idx % 4) * 0.20f)
                    val baseYPx = hPx * (0.12f + ((idx / 4) % 3) * 0.25f)
                    val ampXPx = 40f + (idx % 3) * 26f
                    val ampYPx = 32f + (idx % 2) * 22f
                    val ph = phases[idx]

                    key(idx) {
                        FloatingRewardBalloon(
                            idx = idx,
                            color = BALLOON_COLORS[idx % BALLOON_COLORS.size],
                            driftPhase = driftPhase,
                            phase = ph,
                            ampXPx = ampXPx,
                            ampYPx = ampYPx,
                            baseXPx = baseXPx,
                            baseYPx = baseYPx,
                            containerWPx = wPx,
                            containerHPx = hPx,
                            onPop = { balloons[idx] = false },
                            onPopSfx = { sfx.playFirstAvailable(AudioClips.SfxBalloonPop, volume = 0.85f) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingRewardBalloon(
    idx: Int,
    color: Color,
    driftPhase: Float,
    phase: Float,
    ampXPx: Float,
    ampYPx: Float,
    baseXPx: Float,
    baseYPx: Float,
    containerWPx: Float,
    containerHPx: Float,
    onPop: () -> Unit,
    onPopSfx: suspend () -> Unit,
) {
    var popping by remember(idx) { mutableStateOf(false) }
    var visible by remember(idx) { mutableStateOf(true) }
    val scale = remember(idx) { androidx.compose.animation.core.Animatable(1f) }

    val t = driftPhase * 2f * PI.toFloat() + phase
    val ox = sin(t.toDouble()).toFloat() * ampXPx
    val oy = cos((t * 0.85f).toDouble()).toFloat() * ampYPx

    LaunchedEffect(popping) {
        if (!popping) return@LaunchedEffect
        scale.snapTo(1f)
        scale.animateTo(
            targetValue = 1.22f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 110),
        )
        scale.animateTo(
            targetValue = 0.12f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        )
        onPopSfx()
        visible = false
        onPop()
    }

    if (!visible) return

    // Clamp so balloons never drift fully off-screen.
    // We're in px space already; clamping with generous margins prevents “invisible unpoppable balloon”.
    val paddingPx = 10f
    val rawXPx = baseXPx + ox
    val rawYPx = baseYPx + oy
    val xPx = rawXPx.coerceIn(paddingPx, (containerWPx - 110f).coerceAtLeast(paddingPx))
    val yPx = rawYPx.coerceIn(paddingPx, (containerHPx - 140f).coerceAtLeast(paddingPx))

    Box(
        modifier =
            Modifier
                .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
                .size(96.dp, 118.dp)
                .clickable(enabled = !popping, onClick = { popping = true }),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .size(78.dp)
                        .scale(scale.value),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width
                    val ch = size.height
                    val center = Offset(cw / 2f, ch * 0.42f)
                    val r = cw * 0.46f

                    drawCircle(color = color, radius = r, center = center)
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                                center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                                radius = r * 0.95f,
                            ),
                        radius = r,
                        center = center,
                    )
                    drawCircle(
                        color = Color(0xFF0B2B3D).copy(alpha = 0.2f),
                        radius = r * 0.09f,
                        center = Offset(center.x, center.y + r * 0.52f),
                    )
                }
            }
            Canvas(modifier = Modifier.size(3.dp, 22.dp)) {
                drawLine(
                    color = Color(0xFF0B2B3D).copy(alpha = 0.35f),
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = 2.5f,
                )
            }
        }
    }
}

private val BALLOON_COLORS =
    listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFD93D),
        Color(0xFF6BCB77),
        Color(0xFF4D96FF),
        Color(0xFFB983FF),
    )
