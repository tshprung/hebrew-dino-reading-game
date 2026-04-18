package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onResetAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "הגדרות",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )
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
                    text = "זה יאפס את ההתקדמות ויחזיר לשלב הראשון. להמשיך?",
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
