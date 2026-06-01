package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.audio.GameAudioEngine
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MuseumWallTop = Color(0xFFF4EDE3)
private val MuseumWallBottom = Color(0xFFE6D9C8)
private val TitleBrown = Color(0xFF4A3A2E)
private val FossilCardTop = Color(0xFFF8F2E7)
private val FossilCardBottom = Color(0xFFE9DECF)
private val FossilOutline = Color(0xFF7B6858).copy(alpha = 0.28f)
private val RevealGreen = Color(0xFF2E7D32)

@Composable
fun Season2MemoryMatchStationScreen(
    letters: List<String>,
    rounds: Int = 3,
    onBack: () -> Unit,
    onMarkCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val devToolsEnabled = DevTools.enabled(context)
    val audio = remember { GameAudioEngine(context = context) }
    val voice = audio.voice
    val rawVoice = remember { RawVoicePlayer(context = context) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            voice.stopNow()
            audio.release()
            rawVoice.release()
        }
    }

    LaunchedEffect(Unit) {
        val p = AudioClips.Season2Ch1St4MemoryMatchInstructions
        if (voice.hasAsset(p)) {
            voice.playBlocking(p)
        }
    }

    val totalRounds = rounds.coerceIn(1, 3)
    var roundIndex by remember { mutableIntStateOf(0) }

    fun roundLetters(index: Int): List<String> {
        val base = letters.distinct()
        if (base.size <= 4) return base.take(4)
        // Rotate through subsets of 4 so the game feels fresh.
        val start = (index * 2) % base.size
        val out = ArrayList<String>(4)
        var i = 0
        while (out.size < 4 && i < base.size * 2) {
            val l = base[(start + i) % base.size]
            if (l !in out) out.add(l)
            i++
        }
        return out.take(4)
    }

    var deck by remember(roundIndex, letters) { mutableStateOf((roundLetters(roundIndex) + roundLetters(roundIndex)).shuffled()) }
    val matched = remember(roundIndex) { mutableStateListOf<Boolean>().apply { repeat(deck.size) { add(false) } } }
    val flipped = remember(roundIndex) { mutableStateListOf<Boolean>().apply { repeat(deck.size) { add(false) } } }

    var firstIndex by remember { mutableIntStateOf(-1) }
    var secondIndex by remember { mutableIntStateOf(-1) }
    var inputEnabled by remember { mutableStateOf(true) }
    var letterPlayJob by remember { mutableStateOf<Job?>(null) }

    fun revealIndex(i: Int) {
        if (!inputEnabled) return
        if (i !in deck.indices) return
        if (matched[i] || flipped[i]) return
        if (firstIndex != -1 && secondIndex != -1) return

        flipped[i] = true
        val letter = deck[i]
        letterPlayJob?.cancel()
        voice.stopNow()
        val resId = AudioClips.letterNameRawResId(letter)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required letter-name audio. chapterId=${com.tal.hebrewdino.ui.domain.Season2ChapterIds.Chapter1Tyrannosaurus} stationId=4 context=Season2MemoryMatchStationScreen.revealIndex stage=missing raw letter-name mapping letter='$letter'",
            )
            letterPlayJob =
                scope.launch {
                    rawVoice.playRawBlocking(0)
                }
        } else {
            letterPlayJob =
                scope.launch {
                    rawVoice.playRawBlocking(resId)
                }
        }
        if (firstIndex == -1) {
            firstIndex = i
        } else {
            secondIndex = i
        }
    }

    LaunchedEffect(firstIndex, secondIndex) {
        if (firstIndex == -1 || secondIndex == -1) return@LaunchedEffect
        inputEnabled = false
        delay(520)
        val a = deck[firstIndex]
        val b = deck[secondIndex]
        if (a == b) {
            matched[firstIndex] = true
            matched[secondIndex] = true
        } else {
            flipped[firstIndex] = false
            flipped[secondIndex] = false
        }
        firstIndex = -1
        secondIndex = -1
        inputEnabled = true
    }

    val allMatched = matched.all { it }
    LaunchedEffect(allMatched) {
        if (!allMatched) return@LaunchedEffect
        inputEnabled = false
        delay(420)
        if (roundIndex < totalRounds - 1) {
            roundIndex += 1
            firstIndex = -1
            secondIndex = -1
            inputEnabled = true
            // Replay instruction if present (nice for kids).
            val p = AudioClips.Season2Ch1St4MemoryMatchInstructions
            if (voice.hasAsset(p)) {
                voice.playBlocking(p)
            }
        } else {
            onMarkCompleted()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MuseumWallTop, MuseumWallBottom))),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = "\u200Fחזור",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TitleBrown,
                    )
                }
                if (devToolsEnabled) {
                    OutlinedButton(
                        onClick = {
                            letterPlayJob?.cancel()
                            voice.stopNow()
                            onMarkCompleted()
                        },
                        colors = ChapterNavChipStyles.outlinedButtonColors(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("בדיקה", style = ChapterNavChipStyles.labelTextStyle())
                    }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.45f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Text(
                            text = "\u200Fתחנה 4 · זיכרון אותיות",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    shadow =
                                        Shadow(
                                            color = Color.White.copy(alpha = 0.35f),
                                            blurRadius = 6f,
                                        ),
                                ),
                            color = Color(0xFF4A2A4E),
                        )
                    }
                }
                // Balance the back button so the title stays centered.
                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "\u200Fמצא זוגות של אותיות זהות",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TitleBrown.copy(alpha = 0.85f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val hGap = 8.dp
                val vGap = 8.dp
                val pad = 6.dp
                val cellW = (maxWidth - pad * 2 - hGap * 3) / 4
                val cellH = (maxHeight - pad * 2 - vGap) / 2
                val cell = if (cellW < cellH) cellW else cellH

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(pad),
                    verticalArrangement = Arrangement.spacedBy(vGap),
                    horizontalArrangement = Arrangement.spacedBy(hGap),
                ) {
                    itemsIndexed(deck) { idx, letter ->
                        MemoryCard(
                            letter = letter,
                            revealed = flipped[idx] || matched[idx],
                            matched = matched[idx],
                            enabled = inputEnabled && !matched[idx],
                            onClick = { revealIndex(idx) },
                            modifier = Modifier.size(cell),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(
    letter: String,
    revealed: Boolean,
    matched: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val glow = remember { Animatable(0f) }
    LaunchedEffect(matched) {
        if (!matched) return@LaunchedEffect
        glow.snapTo(0f)
        glow.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        glow.animateTo(0.4f, tween(650, easing = FastOutSlowInEasing))
    }

    val bg =
        if (revealed) {
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.55f)))
        } else {
            Brush.verticalGradient(listOf(FossilCardTop, FossilCardBottom))
        }

    Box(
        modifier =
            modifier
                .shadow(6.dp, shape, clip = false)
                .clip(shape)
                .background(bg)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (!revealed) {
            Text(
                text = "\u200F?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TitleBrown.copy(alpha = 0.55f),
            )
        } else {
            Text(
                text = letter,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = TitleBrown,
            )
        }
        if (matched) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(RevealGreen.copy(alpha = 0.10f + 0.10f * glow.value))
                        .alpha(0.95f),
            )
        }
    }
}

