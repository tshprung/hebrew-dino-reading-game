package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** ~½ cm: shared layout nudges for the six-station arc (chapters 1–4); letters/art/intros differ per chapter. */
internal val SixStationArcHalfCmNudge = 19.dp

@Composable
internal fun GameScreenBackgroundLayer(
    chapterId: Int,
    backgroundRes: Int,
) {
    Image(
        painter = painterResource(id = backgroundRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
    if (chapterId == 2) {
        // Chapter 2 PNG reads a bit flat on-device; a very light warm veil keeps the scene readable
        // while restoring some "sun" without fighting the authored art.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF3E0).copy(alpha = 0.10f),
                            Color(0xFFFFFDE7).copy(alpha = 0.06f),
                            Color(0xFFE1F5FE).copy(alpha = 0.05f),
                        ),
                    ),
                ),
        )
    }
    if (chapterId == 3) {
        Chapter3LevelOverlayScrim(modifier = Modifier.fillMaxSize())
    }
}

@Composable
internal fun GameScreenTopChrome(
    onBack: () -> Unit,
    questionNumber: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Keep back on the physical right; put the progress line immediately to its left.
            OutlinedButton(
                onClick = onBack,
                colors = ChapterNavChipStyles.outlinedButtonColors(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("חזור", style = ChapterNavChipStyles.labelTextStyle())
            }
            LinearProgressIndicator(
                progress = {
                    (questionNumber.toFloat() / totalQuestions.coerceAtLeast(1))
                        .coerceIn(0f, 1f)
                },
                modifier = Modifier.weight(1f).height(9.dp),
                color = Color(0xFF2E7D32),
                trackColor = Color(0xFF0B2B3D).copy(alpha = 0.12f),
            )
        }
    }
}

@Composable
internal fun GameScreenDinoLayer(
    idleRes: Int,
    talkFrameResIds: List<Int>,
    isTalking: Boolean,
    dinoForward: Animatable<Float, AnimationVector1D>,
    dinoSlip: Animatable<Float, AnimationVector1D>,
    dinoTilt: Animatable<Float, AnimationVector1D>,
    dinoScale: Animatable<Float, AnimationVector1D>,
) {
    AnimatedTalkingCharacter(
        idleRes = idleRes,
        talkFrameResIds = talkFrameResIds,
        isTalking = isTalking,
        modifier =
            Modifier
                .offset { IntOffset((dinoForward.value + dinoSlip.value).toInt(), 0) }
                .graphicsLayer { rotationZ = dinoTilt.value }
                .size(88.dp)
                .scale(dinoScale.value),
        contentDescription = "דינו",
    )
}

@Composable
private fun Chapter3LevelOverlayScrim(
    modifier: Modifier = Modifier,
) {
    // Teal-tinted, kid-friendly readability scrim:
    // - Vertical gradient (top/bottom stronger, center lighter)
    // - Soft center “window” that *reduces* the scrim
    // - Very light edge vignette to frame the center
    val tint = Color(0xFF081C26) // RGB(8, 28, 38)
    Canvas(
        modifier =
            modifier.graphicsLayer {
                // Required for BlendMode.DstOut to punch a soft window in the scrim.
                compositingStrategy = CompositingStrategy.Offscreen
            },
    ) {
        val w = size.width
        val h = size.height

        // Base vertical gradient: keep it light so instructions stay readable.
        drawRect(
            brush =
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            0.00f to tint.copy(alpha = 0.16f),
                            0.50f to tint.copy(alpha = 0.10f),
                            1.00f to tint.copy(alpha = 0.20f),
                        ),
                    startY = 0f,
                    endY = h,
                ),
        )

        // Light edge vignette (+4% alpha at sides).
        drawRect(
            brush =
                Brush.horizontalGradient(
                    colorStops =
                        arrayOf(
                            0.00f to tint.copy(alpha = 0.04f),
                            0.15f to Color.Transparent,
                            0.85f to Color.Transparent,
                            1.00f to tint.copy(alpha = 0.04f),
                        ),
                    startX = 0f,
                    endX = w,
                ),
        )

        // Center “readability window”: subtract a bit more alpha in the center, fading out smoothly.
        val center = Offset(w * 0.50f, h * 0.52f)
        val rx = w * 0.55f
        val ry = h * 0.38f
        // Use DstOut so source alpha removes destination alpha.
        drawOval(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.12f),
                            0.55f to Color.Black.copy(alpha = 0.06f),
                            1.00f to Color.Transparent,
                        ),
                    center = center,
                    radius = maxOf(rx, ry),
                ),
            topLeft = Offset(center.x - rx, center.y - ry),
            size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
            blendMode = BlendMode.DstOut,
        )
    }
}

@Composable
internal fun IntroPulse(
    stationId: Int,
    question: Question,
    modifier: Modifier = Modifier,
) {
    val pulse =
        rememberInfiniteTransition(label = "introPulse").animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
            label = "introPulseScale",
        )
    val label =
        when (question) {
            is Question.FindLetterGridQuestion -> question.targetLetter
            is Question.PopBalloonsQuestion -> question.correctAnswer
            is Question.PictureStartsWithQuestion -> question.word
            is Question.ImageMatchQuestion ->
                when {
                    stationId == 6 -> "ליחצו על אות והמילה שמתחילה באותה האות"
                    stationId in 4..6 -> question.targetLetter
                    else -> question.targetWord
                }
            is Question.FinaleSlotQuestion -> "★"
        }
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val narrow = maxWidth < 380.dp
        val medium = maxWidth < 520.dp
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = label,
                fontSize =
                    when (question) {
                        is Question.PictureStartsWithQuestion ->
                            if (narrow) 40.sp else 48.sp
                        is Question.ImageMatchQuestion ->
                            when {
                                stationId in 4..6 && narrow -> 52.sp
                                stationId in 4..6 -> 64.sp
                                narrow -> 44.sp
                                medium -> 48.sp
                                else -> 56.sp
                            }
                        is Question.FinaleSlotQuestion ->
                            if (narrow) 54.sp else 72.sp
                        is Question.FindLetterGridQuestion ->
                            if (narrow) 68.sp else 87.sp
                        else ->
                            if (narrow) 72.sp else 96.sp
                    },
                fontWeight = FontWeight.Black,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(pulse.value),
                maxLines = 2,
            )
            if (question is Question.FinaleSlotQuestion) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "בנו את המילים",
                    style =
                        if (narrow) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

internal fun playSuccessPulse(
    scope: CoroutineScope,
    dinoScale: Animatable<Float, AnimationVector1D>,
    peakScale: Float = 1.14f,
): Job =
    scope.launch {
        dinoScale.snapTo(1f)
        dinoScale.animateTo(peakScale, tween(120))
        dinoScale.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 560f))
    }

internal fun playShake(
    scope: CoroutineScope,
    optionsShake: Animatable<Float, AnimationVector1D>,
    baseShakeAmplitudePx: Float,
    strength: Float = 1f,
): Job =
    scope.launch {
        optionsShake.snapTo(0f)
        val amp = baseShakeAmplitudePx * strength.coerceIn(0.8f, 1.6f)
        repeat(5) { i ->
            optionsShake.animateTo(
                if (i % 2 == 0) amp else -amp,
                tween(45),
            )
        }
        optionsShake.animateTo(0f, tween(60))
    }
