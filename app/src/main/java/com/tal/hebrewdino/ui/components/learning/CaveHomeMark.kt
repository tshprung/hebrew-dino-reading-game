package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared “home cave” drawing for journey end + chapters map (no duplicate logic). */
@Composable
fun CaveHomeMark(
    modifier: Modifier = Modifier,
    width: Dp = 118.dp,
    height: Dp = 102.dp,
) {
    Canvas(modifier = modifier.size(width, height)) {
        val cx = size.width * 0.52f
        val cy = size.height * 0.62f
        drawOval(
            color = Color(0xFF3E2723),
            topLeft = Offset(cx - size.width * 0.42f, cy - size.height * 0.18f),
            size = Size(size.width * 0.84f, size.height * 0.50f),
            style = Fill,
        )
        drawArc(
            color = Color(0xFF1B120E),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - size.width * 0.38f, cy - size.height * 0.55f),
            size = Size(size.width * 0.76f, size.height * 0.72f),
            style = Fill,
        )
        drawOval(
            color = Color(0xFF0D0705).copy(alpha = 0.35f),
            topLeft = Offset(cx - size.width * 0.22f, cy + size.height * 0.02f),
            size = Size(size.width * 0.44f, size.height * 0.22f),
            style = Fill,
        )
    }
}
