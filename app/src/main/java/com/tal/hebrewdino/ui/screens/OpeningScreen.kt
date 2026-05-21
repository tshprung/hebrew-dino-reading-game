package com.tal.hebrewdino.ui.screens

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R

@Composable
fun OpeningScreen(
    onPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var parentsDialogOpen by remember { mutableStateOf(false) }
    val infinite = rememberInfiniteTransition(label = "opening")
    val cloudDrift by
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 52000), repeatMode = RepeatMode.Restart),
            label = "cloudDrift",
        )
    val playPulse by
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 2400), repeatMode = RepeatMode.Reverse),
            label = "playPulse",
        )
    val dinoBreath by
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 3800), repeatMode = RepeatMode.Reverse),
            label = "dinoBreath",
        )
    val glowPulse by
        infinite.animateFloat(
            initialValue = 0.55f,
            targetValue = 0.86f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 3200), repeatMode = RepeatMode.Reverse),
            label = "glowPulse",
        )

    Box(modifier = modifier.fillMaxSize()) {
        OpeningAmbientBackground(modifier = Modifier.fillMaxSize(), cloudDrift = cloudDrift)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "לומדים עם דינו",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp,
                        shadow = Shadow(color = Color(0xFF2B6FA8), blurRadius = 12f),
                    ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bookCenterX = size.width * 0.5f
                    val bookCenterY = size.height * 0.6f
                    val glowRadius = size.minDimension * 0.22f
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFE9A8).copy(alpha = 0.85f * glowPulse),
                                        Color(0xFFFFC857).copy(alpha = 0.42f * glowPulse),
                                        Color.Transparent,
                                    ),
                                center = androidx.compose.ui.geometry.Offset(bookCenterX, bookCenterY),
                                radius = glowRadius,
                            ),
                        radius = glowRadius,
                        center = androidx.compose.ui.geometry.Offset(bookCenterX, bookCenterY),
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.dino_idle),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(280.dp)
                            .padding(bottom = (6.dp + (dinoBreath * 4).dp)),
                    contentScale = ContentScale.Fit,
                )

                FloatingHebrewLetters(
                    modifier = Modifier.fillMaxSize(),
                    baseAlpha = 0.8f,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPlay,
                modifier =
                    Modifier
                        .graphicsLayer(
                            scaleX = playPulse,
                            scaleY = playPulse,
                        )
                        .shadow(10.dp, RoundedCornerShape(999.dp))
                        .clip(RoundedCornerShape(999.dp))
                        .padding(vertical = 2.dp)
                        .width(240.dp)
                        .height(60.dp)
                        .then(Modifier),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC54A),
                        contentColor = Color(0xFF102A43),
                    ),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "בואו נשחק",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.alpha(0.98f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                UtilityChip(
                    text = "הורים",
                    onClick = { parentsDialogOpen = true },
                )
                UtilityChip(
                    text = "הגדרות",
                    onClick = onOpenSettings,
                )
            }
        }
    }

    if (parentsDialogOpen) {
        AlertDialog(
            onDismissRequest = { parentsDialogOpen = false },
            confirmButton = {
                TextButton(onClick = { parentsDialogOpen = false }) {
                    Text("סגור")
                }
            },
            title = { Text("הורים", fontWeight = FontWeight.ExtraBold) },
            text = { Text("עמוד הורים יתווסף בהמשך.") },
        )
    }
}

@Composable
private fun UtilityChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF102A43),
        )
    }
}

@Composable
private fun OpeningAmbientBackground(
    cloudDrift: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFF9FD6FF),
                                    Color(0xFFE8F4FF),
                                    Color(0xFFFFF4E0),
                                    Color(0xFFE6F2DC),
                                ),
                        ),
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.1f),
            contentScale = ContentScale.Crop,
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val driftPx = (cloudDrift * w * 0.75f)

            fun cloud(x: Float, y: Float, scale: Float, alpha: Float) {
                val baseW = w * 0.22f * scale
                val baseH = h * 0.07f * scale
                val color = Color.White.copy(alpha = alpha)
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(baseW, baseH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(baseH, baseH),
                )
                drawCircle(
                    color = color,
                    radius = baseH * 0.55f,
                    center = androidx.compose.ui.geometry.Offset(x + baseW * 0.25f, y + baseH * 0.1f),
                )
                drawCircle(
                    color = color,
                    radius = baseH * 0.68f,
                    center = androidx.compose.ui.geometry.Offset(x + baseW * 0.52f, y),
                )
                drawCircle(
                    color = color,
                    radius = baseH * 0.5f,
                    center = androidx.compose.ui.geometry.Offset(x + baseW * 0.75f, y + baseH * 0.16f),
                )
            }

            translate(left = -driftPx) {
                cloud(x = w * 0.15f, y = h * 0.12f, scale = 1f, alpha = 0.22f)
                cloud(x = w * 0.65f, y = h * 0.18f, scale = 0.85f, alpha = 0.16f)
                cloud(x = w * 1.05f, y = h * 0.1f, scale = 0.95f, alpha = 0.18f)
            }
            translate(left = -driftPx * 0.65f) {
                cloud(x = w * 0.05f, y = h * 0.26f, scale = 0.8f, alpha = 0.14f)
                cloud(x = w * 0.85f, y = h * 0.3f, scale = 0.7f, alpha = 0.12f)
            }

            val groundH = h * 0.3f
            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFF6D8F5A).copy(alpha = 0f),
                                Color(0xFF5C7A4A).copy(alpha = 0.22f),
                                Color(0xFF4A5F38).copy(alpha = 0.38f),
                            ),
                        startY = h - groundH * 1.15f,
                        endY = h,
                    ),
                topLeft = androidx.compose.ui.geometry.Offset(0f, h - groundH),
                size = androidx.compose.ui.geometry.Size(w, groundH),
            )
        }
    }
}

@Composable
private fun FloatingHebrewLetters(
    modifier: Modifier = Modifier,
    baseAlpha: Float,
) {
    val infinite = rememberInfiniteTransition(label = "letters")
    val rise1 by
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 5200), repeatMode = RepeatMode.Restart),
            label = "rise1",
        )
    val rise2 by
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 6100), repeatMode = RepeatMode.Restart),
            label = "rise2",
        )
    val rise3 by
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 7000), repeatMode = RepeatMode.Restart),
            label = "rise3",
        )
    val rise4 by
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 5800), repeatMode = RepeatMode.Restart),
            label = "rise4",
        )

    BoxWithConstraints(modifier = modifier) {
        FloatingLetter(letter = "א", xFrac = 0.52f, yBaseFrac = 0.62f, t = rise1, baseAlpha = baseAlpha)
        FloatingLetter(letter = "ב", xFrac = 0.47f, yBaseFrac = 0.63f, t = rise2, baseAlpha = baseAlpha * 0.95f)
        FloatingLetter(letter = "ג", xFrac = 0.56f, yBaseFrac = 0.64f, t = rise3, baseAlpha = baseAlpha * 0.9f)
        FloatingLetter(letter = "ד", xFrac = 0.43f, yBaseFrac = 0.64f, t = rise4, baseAlpha = baseAlpha * 0.85f)
    }
}

@Composable
private fun FloatingLetter(
    letter: String,
    xFrac: Float,
    yBaseFrac: Float,
    t: Float,
    baseAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val yRiseFrac = (1f - t) * 0.14f
    val a = baseAlpha * (1f - (t * 0.92f))

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val x = maxWidth * xFrac
        val y = maxHeight * (yBaseFrac - yRiseFrac)
        Text(
            text = letter,
            modifier = Modifier.offset(x = x, y = y),
            color = Color.White.copy(alpha = a),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    shadow = Shadow(color = Color(0xFFFFD36A).copy(alpha = a), blurRadius = 18f),
                ),
            maxLines = 1,
            softWrap = false,
        )
    }
}
