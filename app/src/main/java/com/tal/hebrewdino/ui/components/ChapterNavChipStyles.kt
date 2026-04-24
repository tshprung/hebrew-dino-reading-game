package com.tal.hebrewdino.ui.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/** Shared “חזור” / “בדיקה” / settings-style chips across chapter and game screens. */
object ChapterNavChipStyles {
    val containerColor: Color = Color.White.copy(alpha = 0.92f)
    val contentColor: Color = Color(0xFF0B2B3D)

    @Composable
    fun outlinedButtonColors() =
        ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )

    @Composable
    fun labelTextStyle(): TextStyle =
        MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
}
