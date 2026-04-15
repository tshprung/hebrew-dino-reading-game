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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.domain.BeachChapter
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember { SoundPoolPlayer(context = context) }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    val current: Question? = questions.getOrNull(index)

    if (current == null) {
        onComplete(levelId, correct, mistakes)
        return
    }

    LaunchedEffect(levelId, index) {
        feedback = null
        // Let UI settle a bit before speaking.
        delay(120)
        val target = current.targetLetter
        val chooseSpecific = AudioClips.chooseLetterClip(target)
        if (chooseSpecific != null) {
            player.play(chooseSpecific)
        } else {
            // Fallback: "choose letter" + letter name
            val name = AudioClips.letterNameClip(target)
            player.play(AudioClips.VoChooseLetter)
            if (name != null) {
                delay(250)
                player.play(name)
            }
        }
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
                    scope.launch { player.playFirstAvailable(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2) }
                    index += 1
                } else {
                    mistakes += 1
                    feedback = "כמעט… בוא ננסה שוב"
                    scope.launch { player.playFirstAvailable(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1) }
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

