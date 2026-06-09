package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

private val ParchmentTop = Color(0xFFF2E0BC)
private val ParchmentMid = Color(0xFFE2C896)
private val ParchmentBottom = Color(0xFFC9A86C)
private val MapInk = Color(0xFF4A3224).copy(alpha = 0.18f)
private val MapTrail = Color(0xFF9A6B1A).copy(alpha = 0.22f)
private val SeaTint = Color(0xFF2E6B8A).copy(alpha = 0.14f)
private val TreasureMark = Color(0xFFB8860B).copy(alpha = 0.28f)

/** Old treasure-map / adventure parchment backdrop for Season 2 chapter select. */
@Composable
fun Season2TreasureMapBackground(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ParchmentTop, ParchmentMid, ParchmentBottom),
                    ),
                ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val foldStroke = Stroke(width = 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f)))

            drawRect(
                brush =
                    Brush.radialGradient(
                        colors = listOf(SeaTint, Color.Transparent),
                        center = Offset(w * 0.08f, h * 0.12f),
                        radius = w * 0.35f,
                    ),
            )
            drawRect(
                brush =
                    Brush.radialGradient(
                        colors = listOf(SeaTint.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(w * 0.92f, h * 0.88f),
                        radius = w * 0.30f,
                    ),
            )

            for (i in 1..4) {
                val y = h * i / 5f
                drawLine(MapInk, Offset(0f, y), Offset(w, y), strokeWidth = 1.1f)
            }
            for (i in 1..5) {
                val x = w * i / 6f
                drawLine(MapInk.copy(alpha = 0.10f), Offset(x, 0f), Offset(x, h), strokeWidth = 0.9f)
            }

            drawLine(
                color = MapTrail,
                start = Offset(w * 0.06f, h * 0.78f),
                end = Offset(w * 0.88f, h * 0.22f),
                strokeWidth = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
            )
            drawLine(
                color = MapTrail.copy(alpha = 0.14f),
                start = Offset(w * 0.18f, h * 0.55f),
                end = Offset(w * 0.72f, h * 0.48f),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f)),
            )

            drawCircle(
                color = Color(0xFF8B4513).copy(alpha = 0.16f),
                radius = w * 0.10f,
                center = Offset(w * 0.80f, h * 0.32f),
                style = foldStroke,
            )
            drawCircle(
                color = Color(0xFF2E7D32).copy(alpha = 0.14f),
                radius = w * 0.07f,
                center = Offset(w * 0.20f, h * 0.64f),
                style = Stroke(width = 2f),
            )

            val xCenter = Offset(w * 0.62f, h * 0.58f)
            rotate(degrees = 18f, pivot = xCenter) {
                drawLine(TreasureMark, xCenter - Offset(10f, 10f), xCenter + Offset(10f, 10f), strokeWidth = 3f)
                drawLine(TreasureMark, xCenter - Offset(10f, -10f), xCenter + Offset(10f, -10f), strokeWidth = 3f)
            }

            rotate(degrees = -12f, pivot = Offset(w * 0.38f, h * 0.28f)) {
                drawCircle(
                    color = Color(0xFF6D4C2C).copy(alpha = 0.12f),
                    radius = w * 0.045f,
                    center = Offset(w * 0.38f, h * 0.28f),
                    style = Stroke(width = 2f),
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF3D2E1E).copy(alpha = 0.14f),
                                ),
                            radius = 900f,
                        ),
                    ),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFF3DC).copy(alpha = 0.22f),
                                Color.Transparent,
                                Color(0xFFFFF3DC).copy(alpha = 0.16f),
                            ),
                        ),
                    ),
        )
    }
}
