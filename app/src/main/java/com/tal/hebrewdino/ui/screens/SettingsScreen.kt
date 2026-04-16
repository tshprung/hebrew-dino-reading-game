package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun SettingsScreen(
    selectedCharacter: DinoCharacter?,
    onPick: (DinoCharacter) -> Unit,
    onResetAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "הגדרות",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "בחירת דמות", style = MaterialTheme.typography.titleLarge, color = Color(0xFF0B2B3D))
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            CharacterCard(
                title = "דינו",
                subtitle = "בן",
                imageRes = R.drawable.dino_boy,
                selected = selectedCharacter == DinoCharacter.Dino,
                showRibbon = false,
                onClick = { onPick(DinoCharacter.Dino) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            CharacterCard(
                title = "דינה",
                subtitle = "בת",
                imageRes = R.drawable.dino_girl,
                selected = selectedCharacter == DinoCharacter.Dina,
                showRibbon = true,
                onClick = { onPick(DinoCharacter.Dina) },
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.width(260.dp),
        ) {
            Text("איפוס משחק")
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.width(260.dp)) { Text("חזרה") }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "איפוס משחק") },
            text = {
                Text(
                    text = "זה יאפס את ההתקדמות ויחזיר אותך למסך בחירת דמות. להמשיך?",
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetAll()
                    },
                ) {
                    Text("כן, לאפס")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("ביטול")
                }
            },
        )
    }
}

@Composable
private fun CharacterCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    selected: Boolean,
    showRibbon: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Color(0xFF2E7D32) else Color.Transparent
    val borderWidth = if (selected) 3.dp else 0.dp

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp)
            .height(250.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                if (showRibbon) {
                    PinkRibbon(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 12.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 38.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (selected) "נבחר ✓" else "לחץ/י לבחירה",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) Color(0xFF2E7D32) else Color(0xFF0B2B3D),
            )
        }
    }
}

@Composable
private fun PinkRibbon(modifier: Modifier = Modifier) {
    // Simple “ribbon” marker (no asset): two pink circles and a small center.
    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color(0xFFFF66B2), shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Spacer(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color(0xFFFF66B2), shape = CircleShape),
            )
        }
        Spacer(
            modifier = Modifier
                .align(Alignment.Center)
                .size(9.dp)
                .background(Color(0xFFFF2D95), shape = CircleShape),
        )
    }
}

