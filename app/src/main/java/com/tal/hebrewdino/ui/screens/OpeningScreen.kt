package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R

@Composable
fun OpeningScreen(
    onPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "opening")
    val playPulse by
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 2200), repeatMode = RepeatMode.Reverse),
            label = "playPulse",
        )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.opening_splash_art),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
            )

            OpeningSettingsChip(
                text = "הגדרות",
                onClick = onOpenSettings,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 10.dp),
            )

            OpeningPlayButton(
                onClick = onPlay,
                pulse = playPulse,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 22.dp, bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun OpeningPlayButton(
    onClick: () -> Unit,
    pulse: Float,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(999.dp)
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .graphicsLayer(scaleX = pulse, scaleY = pulse)
                .widthIn(min = 220.dp, max = 340.dp)
                .fillMaxWidth(0.38f)
                .height(62.dp)
                .shadow(10.dp, pillShape)
                .clip(pillShape)
                .border(width = 2.dp, color = Color.White.copy(alpha = 0.55f), shape = pillShape)
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFFFFE27A).copy(alpha = 0.58f),
                                Color(0xFFFFB82E).copy(alpha = 0.66f),
                                Color(0xFFFF9A1A).copy(alpha = 0.72f),
                            ),
                    ),
                    shape = pillShape,
                ),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "בואו נשחק!",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        shadow =
                            Shadow(
                                color = Color.White.copy(alpha = 0.85f),
                                blurRadius = 6f,
                            ),
                    ),
                color = Color(0xFF102A43),
            )
        }
    }
}

@Composable
private fun OpeningSettingsChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .shadow(8.dp, RoundedCornerShape(999.dp))
                .clip(RoundedCornerShape(999.dp)),
        color = Color.White.copy(alpha = 0.72f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF102A43),
        )
    }
}
