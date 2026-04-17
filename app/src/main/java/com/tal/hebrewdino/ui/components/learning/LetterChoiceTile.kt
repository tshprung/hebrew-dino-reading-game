package com.tal.hebrewdino.ui.components.learning

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Large, readable Hebrew letter tile with optional focus halo (intro / guided tasks).
 * Touch target is at least [tileSize]; halo padding sits outside that box.
 */
@Composable
fun LetterChoiceTile(
    letter: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tileSize: Dp = 96.dp,
    haloActive: Boolean = false,
    enabled: Boolean = true,
    /** Brief wrong feedback: dims tile without hiding it. */
    wrongDimmed: Boolean = false,
    /** Correct burst: slight scale + green border (keep subtle). */
    correctPulse: Boolean = false,
) {
    val dimAlpha by animateFloatAsState(
        targetValue = if (wrongDimmed) 0.58f else 1f,
        animationSpec = tween(220),
        label = "wrongDim",
    )
    val pulseScale by animateFloatAsState(
        targetValue = if (correctPulse) 1.12f else 1f,
        animationSpec = tween(200),
        label = "correctPulse",
    )
    LetterTileWithHalo(haloActive = haloActive, modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .size(tileSize)
                    .scale(pulseScale)
                    .alpha(dimAlpha)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (haloActive) Color(0xFFFFE082).copy(alpha = 0.95f) else Color(0xFFFFF3C4).copy(alpha = 0.96f),
                    )
                    .border(
                        width = if (correctPulse) 4.dp else 2.dp,
                        color =
                            when {
                                correctPulse -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                                haloActive -> Color(0xFF0B2B3D).copy(alpha = 0.75f)
                                else -> Color.White.copy(alpha = 0.65f)
                            },
                        shape = RoundedCornerShape(22.dp),
                    )
                    .semantics { role = Role.Button }
                    .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
        }
    }
}
