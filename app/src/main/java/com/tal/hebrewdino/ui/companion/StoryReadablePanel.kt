package com.tal.hebrewdino.ui.companion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.layout.ScreenFit

/** Warm story card used across Season 1 Ch.1 narrative and reward copy. */
@Composable
fun StoryReadablePanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier =
            modifier
                .shadow(elevation = 6.dp, shape = shape, clip = false)
                .background(color = Color(0xFFFFF8E8), shape = shape)
                .padding(horizontal = if (isCompact) 14.dp else 20.dp, vertical = if (isCompact) 12.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
fun CompanionDinoSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val style =
        textStyle
            ?: MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 18.sp else 22.sp,
                lineHeight = if (isCompact) 26.sp else 32.sp,
            )
    StoryReadablePanel(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = style,
            color = Color(0xFF1A2E3D),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
