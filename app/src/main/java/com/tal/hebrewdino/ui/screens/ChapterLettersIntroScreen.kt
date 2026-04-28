package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import kotlinx.coroutines.Job
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.components.learning.LetterChoiceTile
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import kotlinx.coroutines.launch

@Composable
fun Chapter1LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 1 - מצא את הביצה",
        letters = listOf("א", "ב", "ד", "ל", "מ"),
        backgroundRes = R.drawable.forest_bg_story_intro,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter2LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 2 - מצא את הביצה הורודה",
        letters = listOf("ג", "ה", "ו", "ר", "ש"),
        backgroundRes = R.drawable.chapter2_journey_road,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 3 — האותיות במסע",
        letters = Chapter3Config.letters,
        backgroundRes = R.drawable.ch3_journey_bg,
        letterGridRows = 2,
        lettersAreaMinHeight = 228.dp,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterTitle = "פרק 4 - חיזוק חכם",
        letters = listOf("א", "מ", "ש", "ר", "ת"),
        backgroundRes = R.drawable.mountain_bg_chapter4,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun ChapterLettersIntroScreen(
    chapterTitle: String,
    letters: List<String>,
    backgroundRes: Int,
    /** 2 = two horizontal rows of letter tiles (Episode 3 layout). */
    letterGridRows: Int = 1,
    /** Ensures enough vertical space so rows do not overlap. */
    lettersAreaMinHeight: Dp? = null,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stableLetters = remember(letters) { letters.distinct() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoicePlayer(context = context) }
    val scope = rememberCoroutineScope()
    var playing by remember { mutableStateOf(false) }
    var highlightedLetter by remember { mutableStateOf<String?>(null) }
    var playJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            playJob?.cancel()
            voice.stopNow()
            voice.release()
        }
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
        playJob?.cancel()
        playJob = scope.launch { playAll() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
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
                        .width(560.dp)
                        .then(
                            if (lettersAreaMinHeight != null) {
                                Modifier.heightIn(min = lettersAreaMinHeight)
                            } else {
                                Modifier
                            },
                        ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "בפרק הזה נלמד את האותיות",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val letterRows: List<List<String>> =
                        if (letterGridRows >= 2 && stableLetters.size > 1) {
                            val mid = (stableLetters.size + 1) / 2
                            listOf(
                                stableLetters.take(mid),
                                stableLetters.drop(mid),
                            )
                        } else {
                            listOf(stableLetters)
                        }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        letterRows.forEach { rowLetters ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                rowLetters.forEach { l ->
                                    LetterChoiceTile(
                                        letter = l,
                                        tileSize = 88.dp,
                                        haloActive = highlightedLetter == l,
                                        enabled = false,
                                        onClick = { },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            if (!playing) {
                                playJob?.cancel()
                                playJob = scope.launch { playAll() }
                            }
                        },
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

            Button(
                onClick = {
                    // UX: stop intro immediately when continuing (don't wait for dispose/navigation).
                    playJob?.cancel()
                    voice.stopNow()
                    playing = false
                    highlightedLetter = null
                    onContinue()
                },
                modifier = Modifier.width(180.dp),
            ) {
                Text("המשך")
            }
        }
    }
}

