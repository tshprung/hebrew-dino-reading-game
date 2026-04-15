package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.BeachChapter
import com.tal.hebrewdino.ui.domain.Question

@Composable
fun LevelScreen(
    levelId: Int,
    onBack: () -> Unit,
    onComplete: (levelId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val questions = remember(levelId) { BeachChapter.generateQuestions(levelId) }
    var index by remember(levelId) { mutableStateOf(0) }
    var correct by remember(levelId) { mutableStateOf(0) }
    var mistakes by remember(levelId) { mutableStateOf(0) }
    var feedback by remember(levelId) { mutableStateOf<String?>(null) }

    val current: Question? = questions.getOrNull(index)

    if (current == null) {
        onComplete(levelId, correct, mistakes)
        return
    }

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
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = current.prompt,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
            Text(
                text = feedback ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        LetterOptions(
            options = current.options,
            onPick = { picked ->
                if (picked == current.targetLetter) {
                    correct += 1
                    feedback = "כל הכבוד!"
                    index += 1
                } else {
                    mistakes += 1
                    feedback = "כמעט… בוא ננסה שוב"
                }
            },
        )

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה למפה") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterOptions(
    options: List<String>,
    onPick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        options.forEach { letter ->
            Button(onClick = { onPick(letter) }) {
                Text(text = letter, fontSize = 42.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

