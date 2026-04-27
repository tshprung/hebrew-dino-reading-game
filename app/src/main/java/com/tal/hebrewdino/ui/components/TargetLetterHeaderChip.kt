package com.tal.hebrewdino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TargetLetterHeaderChip(
    letter: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 56.sp,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush =
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFF59D).copy(alpha = 0.95f),
                                Color(0xFFFFE082).copy(alpha = 0.88f),
                            ),
                        ),
                )
                .border(2.dp, Color(0xFFFFA000).copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
        )
    }
}

