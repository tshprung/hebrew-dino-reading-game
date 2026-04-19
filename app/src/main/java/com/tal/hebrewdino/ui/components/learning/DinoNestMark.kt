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

/** Stylized open dinosaur nest — twigs, moss, eggs (replaces cave for “way home to the nest”). */
@Composable
fun DinoNestMark(
    modifier: Modifier = Modifier,
    width: Dp = 208.dp,
    height: Dp = 144.dp,
) {
    Canvas(modifier = modifier.size(width, height)) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val baseY = h * 0.88f

        // Ground shadow
        drawOval(
            color = Color(0xFF3E2723).copy(alpha = 0.18f),
            topLeft = Offset(w * 0.18f, h * 0.78f),
            size = Size(w * 0.64f, h * 0.12f),
        )

        // Outer twig ring (ellipse bowl)
        drawPath(
            path =
                Path().apply {
                    moveTo(w * 0.12f, baseY)
                    quadraticBezierTo(w * 0.08f, h * 0.52f, cx, h * 0.42f)
                    quadraticBezierTo(w * 0.92f, h * 0.52f, w * 0.88f, baseY)
                    close()
                },
            brush =
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF6D4C41),
                        Color(0xFF4E342E),
                    ),
                ),
            style = Fill,
        )

        // Inner lining
        drawPath(
            path =
                Path().apply {
                    moveTo(w * 0.18f, baseY - 4f)
                    quadraticBezierTo(w * 0.16f, h * 0.56f, cx, h * 0.48f)
                    quadraticBezierTo(w * 0.84f, h * 0.56f, w * 0.82f, baseY - 4f)
                    close()
                },
            brush =
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF8D6E63).copy(alpha = 0.95f),
                        Color(0xFF5D4037).copy(alpha = 0.88f),
                    ),
                ),
            style = Fill,
        )

        // Moss
        drawCircle(color = Color(0xFF558B2F).copy(alpha = 0.35f), radius = w * 0.06f, center = Offset(w * 0.28f, h * 0.62f))
        drawCircle(color = Color(0xFF689F38).copy(alpha = 0.28f), radius = w * 0.05f, center = Offset(w * 0.72f, h * 0.58f))

        // Eggs (two ovals)
        drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFE082))),
            topLeft = Offset(cx - w * 0.22f, h * 0.48f),
            size = Size(w * 0.18f, h * 0.26f),
        )
        drawOval(
            color = Color(0xFF5D4037).copy(alpha = 0.22f),
            topLeft = Offset(cx - w * 0.22f, h * 0.48f),
            size = Size(w * 0.18f, h * 0.26f),
            style = Stroke(width = 2f),
        )
        drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFECB3))),
            topLeft = Offset(cx + w * 0.02f, h * 0.52f),
            size = Size(w * 0.16f, h * 0.22f),
        )
        drawOval(
            color = Color(0xFF5D4037).copy(alpha = 0.18f),
            topLeft = Offset(cx + w * 0.02f, h * 0.52f),
            size = Size(w * 0.16f, h * 0.22f),
            style = Stroke(width = 2f),
        )

        // Cross twigs detail
        for (i in 0 until 7) {
            val t = i / 6f
            val x = w * (0.22f + t * 0.56f)
            val y = baseY - w * (0.04f + (i % 3) * 0.02f)
            drawLine(
                color = Color(0xFF3E2723).copy(alpha = 0.25f),
                start = Offset(x - 10f, y + 6f),
                end = Offset(x + 10f, y - 6f),
                strokeWidth = 3f,
            )
        }
    }
}
