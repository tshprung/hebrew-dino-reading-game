package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared “home cave” for journey end + chapters map — reads as a natural rocky cave opening. */
@Composable
fun CaveHomeMark(
    modifier: Modifier = Modifier,
    width: Dp = 118.dp,
    height: Dp = 102.dp,
) {
    Canvas(modifier = modifier.size(width, height)) {
        val w = size.width
        val h = size.height
        val mouthCx = w * 0.50f
        val mouthBottom = h * 0.88f

        // Distant cliff / hillside
        drawPath(
            path =
                Path().apply {
                    moveTo(w * 0.05f, h * 0.72f)
                    quadraticBezierTo(w * 0.22f, h * 0.38f, w * 0.48f, h * 0.42f)
                    quadraticBezierTo(w * 0.78f, h * 0.36f, w * 0.95f, h * 0.70f)
                    lineTo(w * 0.95f, h * 0.95f)
                    lineTo(w * 0.05f, h * 0.95f)
                    close()
                },
            brush =
                Brush.verticalGradient(
                    colors =
                        listOf(
                            Color(0xFF5D4037).copy(alpha = 0.55f),
                            Color(0xFF3E2723).copy(alpha = 0.85f),
                        ),
                ),
            style = Fill,
        )

        // Main cave mass (rounded arch)
        drawPath(
            path =
                Path().apply {
                    moveTo(w * 0.08f, mouthBottom)
                    cubicTo(
                        w * 0.08f,
                        h * 0.38f,
                        w * 0.42f,
                        h * 0.18f,
                        mouthCx,
                        h * 0.22f,
                    )
                    cubicTo(
                        w * 0.58f,
                        h * 0.18f,
                        w * 0.92f,
                        h * 0.40f,
                        w * 0.92f,
                        mouthBottom,
                    )
                    lineTo(w * 0.08f, mouthBottom)
                    close()
                },
            brush =
                Brush.linearGradient(
                    colors =
                        listOf(
                            Color(0xFF6D4C41),
                            Color(0xFF4E342E),
                            Color(0xFF3E2723),
                        ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h * 0.9f),
                ),
            style = Fill,
        )

        // Dark hollow (mouth)
        drawOval(
            color = Color(0xFF120A08),
            topLeft = Offset(mouthCx - w * 0.22f, h * 0.48f),
            size = Size(w * 0.44f, h * 0.38f),
            style = Fill,
        )

        // Inner depth gradient
        drawOval(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            Color(0xFF1B120E),
                            Color(0xFF050302),
                        ),
                    center = Offset(mouthCx, h * 0.66f),
                    radius = w * 0.28f,
                ),
            topLeft = Offset(mouthCx - w * 0.18f, h * 0.52f),
            size = Size(w * 0.36f, h * 0.30f),
            style = Fill,
        )

        // Ground lip in front of cave
        drawOval(
            color = Color(0xFF5D4037).copy(alpha = 0.9f),
            topLeft = Offset(w * 0.12f, h * 0.80f),
            size = Size(w * 0.76f, h * 0.16f),
            style = Fill,
        )

        // Rocky rim highlight
        drawPath(
            path =
                Path().apply {
                    moveTo(w * 0.14f, h * 0.62f)
                    quadraticBezierTo(w * 0.32f, h * 0.30f, mouthCx, h * 0.26f)
                    quadraticBezierTo(w * 0.68f, h * 0.30f, w * 0.86f, h * 0.62f)
                },
            color = Color(0xFF8D6E63).copy(alpha = 0.55f),
            style = Stroke(width = w * 0.045f),
        )

        // A few “stalactite” hints
        repeat(4) { i ->
            val x = w * (0.34f + i * 0.11f)
            drawLine(
                color = Color(0xFF2D1E18).copy(alpha = 0.65f),
                start = Offset(x, h * 0.28f + i * 3f),
                end = Offset(x + 2f, h * 0.42f + i * 4f),
                strokeWidth = 4f,
            )
        }
    }
}
