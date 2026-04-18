package com.tal.hebrewdino.ui.components.learning

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
 * Large, readable Hebrew letter tile. When [haloActive] is true, the **entire tile** uses a strong
 * filled background (current / spoken letter), not a separate glow layer.
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
    val fillColor =
        when {
            haloActive -> Color(0xFFFFC400).copy(alpha = 0.96f)
            else -> Color(0xFFFFF3C4).copy(alpha = 0.96f)
        }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .size(tileSize)
                    .scale(pulseScale)
                    .alpha(dimAlpha)
                    .clip(RoundedCornerShape(22.dp))
                    .background(fillColor)
                    .border(
                        width = if (correctPulse) 4.dp else if (haloActive) 3.dp else 2.dp,
                        color =
                            when {
                                correctPulse -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                                haloActive -> Color(0xFF1565C0).copy(alpha = 0.55f)
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
