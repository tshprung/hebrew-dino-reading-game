package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter4Config
import com.tal.hebrewdino.ui.domain.Chapter5Config
import com.tal.hebrewdino.ui.domain.Chapter6Config
import com.tal.hebrewdino.ui.domain.HebrewLetterOrder
import com.tal.hebrewdino.ui.components.learning.LetterChoiceTile
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.layout.ScreenFit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Chapter1LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterId = 1,
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
        chapterId = 2,
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
        chapterId = 3,
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
        chapterId = 4,
        chapterTitle = "פרק 4 - סיבוך בדרך",
        letters = Chapter4Config.letters,
        backgroundRes = R.drawable.forest_bg_journey_road,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter5LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterId = 5,
        chapterTitle = "פרק 5 - הביצה השלישית",
        letters = Chapter5Config.letters,
        backgroundRes = R.drawable.forest_bg_journey_road,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6LettersIntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLettersIntroScreen(
        chapterId = 6,
        chapterTitle = "פרק 6 - חוזרים הביתה",
        letters = HebrewLetterOrder.sortForDisplay(Chapter6Config.letters),
        backgroundRes = R.drawable.forest_bg_journey_road,
        letterGridRows = 2,
        lettersAreaMinHeight = 228.dp,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun ChapterLettersIntroScreen(
    chapterId: Int? = null,
    chapterTitle: String,
    letters: List<String>,
    backgroundRes: Int,
    introHeadline: String = "בפרק הזה נלמד את האותיות",
    introSubhead: String? = null,
    /** 2 = two horizontal rows of letter tiles (Episode 3 layout). */
    letterGridRows: Int = 1,
    letterTileSize: Dp = 88.dp,
    /** Ensures enough vertical space so rows do not overlap. */
    lettersAreaMinHeight: Dp? = null,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stableLetters = remember(letters) { HebrewLetterOrder.sortForDisplay(letters.distinct()) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val usesRawLetterNames = chapterId == 1 || chapterId == 2 || chapterId == 3 || chapterId == 4 || chapterId == 5 || chapterId == 6

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val voice = remember(usesRawLetterNames) { if (usesRawLetterNames) null else VoicePlayer(context = context) }
    val rawVoice = remember(usesRawLetterNames) { if (usesRawLetterNames) RawVoicePlayer(context = context) else null }
    val scope = rememberCoroutineScope()
    var playing by remember { mutableStateOf(false) }
    var highlightedLetter by remember { mutableStateOf<String?>(null) }
    var playJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    playJob?.cancel()
                    voice?.stopNow()
                    rawVoice?.stopNow()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playJob?.cancel()
            voice?.stopNow()
            voice?.release()
            rawVoice?.release()
        }
    }

    suspend fun playAll() {
        playing = true
        try {
            for (l in stableLetters) {
                highlightedLetter = l
                if (usesRawLetterNames) {
                    val resId = AudioClips.letterNameRawResId(l)
                    if (resId == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required letter-name audio. chapterId=$chapterId stationId=null context=ChapterLettersIntroScreen.playAll stage=missing raw letter-name mapping letter='$l'",
                        )
                        rawVoice?.playRawBlocking(0)
                    } else {
                        rawVoice?.playRawBlocking(resId)
                    }
                } else {
                    val clip = AudioClips.letterNameClip(l)
                    if (clip != null) voice?.playBlocking(clip)
                }
                kotlinx.coroutines.delay(180.milliseconds)
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
                    .padding(if (isCompactLandscapePhone) 12.dp else 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                contentAlignment = if (isCompactLandscapePhone) Alignment.TopCenter else Alignment.Center,
            ) {
                val effectiveTileSize = if (isCompactLandscapePhone) minOf(letterTileSize, 66.dp) else letterTileSize
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.88f))
                            .padding(if (isCompactLandscapePhone) 12.dp else 18.dp)
                            .fillMaxWidth()
                            .widthIn(max = 560.dp)
                            .then(
                                if (!isCompactLandscapePhone && lettersAreaMinHeight != null) {
                                    Modifier.heightIn(min = lettersAreaMinHeight)
                                } else {
                                    Modifier
                                },
                            ),
                ) {
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

                    if (isCompactLandscapePhone) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = introHeadline,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                            )
                            if (introSubhead != null) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = introSubhead,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF0B2B3D).copy(alpha = 0.82f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = chapterTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1565C0),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    letterRows.forEach { rowLetters ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            rowLetters.forEach { l ->
                                                LetterChoiceTile(
                                                    letter = l,
                                                    tileSize = effectiveTileSize,
                                                    haloActive = highlightedLetter == l,
                                                    enabled = false,
                                                    onClick = { },
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
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
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "עכשיו נצא לדרך.",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = introHeadline,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                            )
                            if (introSubhead != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = introSubhead,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF0B2B3D).copy(alpha = 0.82f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = chapterTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1565C0),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    letterRows.forEach { rowLetters ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            rowLetters.forEach { l ->
                                                LetterChoiceTile(
                                                    letter = l,
                                                    tileSize = effectiveTileSize,
                                                    haloActive = highlightedLetter == l,
                                                    enabled = false,
                                                    onClick = { },
                                                )
                                            }
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
                }
            }

            Button(
                onClick = {
                    // UX: stop intro immediately when continuing (don't wait for dispose/navigation).
                    playJob?.cancel()
                    voice?.stopNow()
                    rawVoice?.stopNow()
                    playing = false
                    highlightedLetter = null
                    onContinue()
                },
                modifier = Modifier.width(if (isCompactLandscapePhone) 160.dp else 180.dp),
            ) {
                Text("המשך")
            }
        }
    }
}

