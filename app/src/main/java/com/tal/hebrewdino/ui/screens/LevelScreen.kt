package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // MVP: Level 1 is a real playable question, other levels show placeholder.
    if (levelId != 1) {
        PlaceholderLevel(levelId = levelId, onBack = onBack, modifier = modifier)
        return
    }

    var message by remember { mutableStateOf("בחר את האות: א") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "שלב 1",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Row {
            BigLetterButton(letter = "א") { message = "כל הכבוד! זו א" }
            Spacer(modifier = Modifier.width(12.dp))
            BigLetterButton(letter = "ב") { message = "כמעט… בוא ננסה שוב" }
            Spacer(modifier = Modifier.width(12.dp))
            BigLetterButton(letter = "מ") { message = "כמעט… בוא ננסה שוב" }
        }

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה למפה") }
    }
}

@Composable
private fun PlaceholderLevel(
    levelId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "שלב $levelId",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "ב־MVP כרגע בנינו רק את שלב 1", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה למפה") }
    }
}

@Composable
private fun BigLetterButton(
    letter: String,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text = letter, fontSize = 42.sp, fontWeight = FontWeight.Black)
    }
}

