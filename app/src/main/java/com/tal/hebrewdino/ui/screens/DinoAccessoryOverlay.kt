package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryCatalog

@Composable
fun DinoAccessoryOverlay(
    equippedAccessoryId: String?,
    dinoSize: Dp,
    modifier: Modifier = Modifier,
) {
    val item = equippedAccessoryId?.let { AccessoryCatalog.find(it) } ?: return
    val badgeSize = (dinoSize * 0.22f).coerceIn(28.dp, 56.dp)

    Box(modifier = modifier.size(dinoSize)) {
        when (item.id) {
            AccessoryCatalog.hat.id ->
                AccessoryBadge(
                    emoji = item.emoji,
                    size = badgeSize,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = dinoSize * 0.02f),
                )
            AccessoryCatalog.sunglasses.id ->
                AccessoryBadge(
                    emoji = item.emoji,
                    size = badgeSize,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .offset(y = -dinoSize * 0.12f),
                )
            AccessoryCatalog.bowtie.id ->
                AccessoryBadge(
                    emoji = item.emoji,
                    size = badgeSize,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .offset(y = dinoSize * 0.18f),
                )
            else ->
                AccessoryBadge(
                    emoji = item.emoji,
                    size = badgeSize,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
        }
    }
}

@Composable
private fun AccessoryBadge(
    emoji: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(size),
        color = Color.White.copy(alpha = 0.88f),
        shape = androidx.compose.foundation.shape.CircleShape,
        tonalElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.52f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
