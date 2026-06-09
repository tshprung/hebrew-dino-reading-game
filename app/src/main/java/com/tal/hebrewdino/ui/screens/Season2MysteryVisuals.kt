package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.Season2Copy

private val FrostWhite = Color(0xFFF8F4EE)
private val FrostMist = Color(0xFFE8E0D4)

/** Strong obscuring filter — child should not recognize the dinosaur silhouette. */
@Composable
fun rememberSeason2FrostedColorFilter(): ColorFilter {
    return remember {
        val matrix =
            ColorMatrix().apply {
                setToSaturation(0.04f)
                val darken =
                    ColorMatrix(
                        floatArrayOf(
                            0.68f, 0f, 0f, 0f, 18f,
                            0f, 0.68f, 0f, 0f, 18f,
                            0f, 0f, 0.68f, 0f, 18f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    )
                timesAssign(darken)
            }
        ColorFilter.colorMatrix(matrix)
    }
}

@Composable
fun Season2FrostedPosterPreview(
    posterResId: Int,
    revealed: Boolean,
    modifier: Modifier = Modifier,
    showMysteryGlyph: Boolean = true,
) {
    val frostedFilter = rememberSeason2FrostedColorFilter()
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = posterResId),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            colorFilter = if (revealed) null else frostedFilter,
            alpha = if (revealed) 1f else 0.72f,
        )
        if (!revealed) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    FrostWhite.copy(alpha = 0.58f),
                                    FrostMist.copy(alpha = 0.72f),
                                    FrostWhite.copy(alpha = 0.50f),
                                ),
                            ),
                        ),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.22f)),
            )
            if (showMysteryGlyph) {
                Text(
                    text = Season2Copy.WhoHidesHere,
                    modifier = Modifier.align(Alignment.Center).padding(8.dp),
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = Color(0xFF5A4A3E).copy(alpha = 0.82f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
fun Season2MysteryPlaceholderCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF0E8DC),
                            Color(0xFFD8CEC0),
                        ),
                    ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.35f)),
        )
        Text(
            text = "\u200F?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF7A6A5C).copy(alpha = 0.55f),
        )
    }
}
