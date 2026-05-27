package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryCatalog

@Composable
fun OwnedAccessoriesRail(
    ownedIds: Set<String>,
    equippedId: String?,
    pendingEquipId: String?,
    onAccessoryTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayIds =
        ownedIds
            .filter { it != pendingEquipId }
            .sorted()
    if (displayIds.isEmpty()) return

    Column(
        modifier = modifier.padding(start = 8.dp, top = 72.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        displayIds.forEach { id ->
            val item = AccessoryCatalog.find(id) ?: return@forEach
            val isEquipped = id == equippedId
            Surface(
                onClick = { onAccessoryTap(id) },
                modifier =
                    Modifier
                        .size(64.dp)
                        .then(
                            if (isEquipped) {
                                Modifier.border(3.dp, Color(0xFFFFE27A), CircleShape)
                            } else {
                                Modifier.border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            },
                        ),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.emoji,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            text = rtl("המתנות שלכם"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.75f),
        )
    }
}

private fun rtl(text: String): String = "\u200F$text"
