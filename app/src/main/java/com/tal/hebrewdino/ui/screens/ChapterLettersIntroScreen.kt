package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.learning.LetterChoiceTile
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.launch

@Composable
fun Chapter1LettersIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 1 - מצא את הביצה",
        letters = listOf("א", "ב", "מ", "ל", "ש"),
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun Chapter2LettersIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 2 - חוזרים הביתה",
        letters = listOf("ב", "מ", "ל", "ק", "ט"),
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun Chapter3LettersIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 3 - מצא את החבר",
        letters = listOf("ק", "ט", "נ", "ה", "ר"),
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ChapterLettersIntroScreen(
    chapterTitle: String,
    letters: List<String>,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stableLetters = remember(letters) { letters.distinct() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    val scope = rememberCoroutineScope()
    var playing by remember { mutableStateOf(false) }
    var highlightedLetter by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { voice.release() }
    }

    suspend fun playAll() {
        playing = true
        try {
            for (l in stableLetters) {
                highlightedLetter = l
                val clip = AudioClips.letterNameClip(l)
                if (clip != null) voice.playBlocking(clip)
                kotlinx.coroutines.delay(180)
            }
        } finally {
            highlightedLetter = null
            playing = false
        }
    }

    LaunchedEffect(Unit) {
        // Auto-play once when the screen appears.
        playAll()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .padding(18.dp)
                        .width(560.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "בפרק הזה נלמד את האותיות",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        stableLetters.forEach { l ->
                            LetterChoiceTile(
                                letter = l,
                                tileSize = 88.dp,
                                haloActive = highlightedLetter == l,
                                enabled = false,
                                onClick = { },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { if (!playing) scope.launch { playAll() } },
                        enabled = !playing,
                    ) {
                        Text("שמע/י את האותיות שוב")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "עכשיו נצא לדרך — ובשלבים לא נחזור על ההגייה.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.width(160.dp)) {
                    Text("חזור")
                }
                Button(onClick = onContinue, modifier = Modifier.width(160.dp)) {
                    Text("המשך")
                }
            }
        }
    }
}

