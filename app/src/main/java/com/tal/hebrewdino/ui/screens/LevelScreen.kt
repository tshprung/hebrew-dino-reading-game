package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.BeachChapter
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val voice = remember { VoicePlayer(context = context) }
    val characterPrefs = remember { CharacterPrefs(context) }
    val character by characterPrefs.characterFlow.collectAsState(initial = DinoCharacter.Dino)

    val dinoRes = if (character == DinoCharacter.Dina) R.drawable.dino_girl else R.drawable.dino_boy

    // Animations
    val dinoScale = remember(levelId) { androidx.compose.animation.core.Animatable(1f) }
    val optionsShake = remember(levelId) { androidx.compose.animation.core.Animatable(0f) }
    val showConfetti = remember(levelId) { mutableStateOf(false) }
    var inputLocked by remember(levelId) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { voice.release() }
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
            voice.playBlocking(chooseSpecific)
        } else {
            // Fallback: "choose letter" + letter name
            val name = AudioClips.letterNameClip(target)
            voice.playBlocking(AudioClips.VoChooseLetter)
            if (name != null) {
                voice.playBlocking(name)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_level_beach),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        val bob by rememberInfiniteTransition(label = "bob").animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bobOffset",
        )

        // Dino avatar (idle bob + reacts to success)
        Image(
            painter = painterResource(id = dinoRes),
            contentDescription = null,
            modifier = Modifier
                .padding(12.dp)
                .size(96.dp)
                .align(Alignment.TopStart)
                .offset(y = bob.dp)
                .scale(dinoScale.value),
            contentScale = ContentScale.Fit,
        )

        if (showConfetti.value) {
            ConfettiOverlay(
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
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
            enabled = !inputLocked,
            shakePx = optionsShake.value,
            onPick = { picked ->
                if (picked == current.targetLetter) {
                    // Advance ONLY after "good job" finishes (no overlaps).
                    scope.launch {
                        inputLocked = true
                        correct += 1
                        feedback = "כל הכבוד!"
                        playSuccessAnimation(scope, dinoScale, showConfetti)
                        voice.playFirstAvailableBlocking(AudioClips.VoGoodJob1, AudioClips.VoGoodJob2)
                        index += 1
                        inputLocked = false
                    }
                } else {
                    scope.launch {
                        inputLocked = true
                        mistakes += 1
                        feedback = "כמעט… בוא ננסה שוב"
                        playMistakeAnimation(scope, optionsShake)
                        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                        inputLocked = false
                    }
                }
            },
        )

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה למפה") }
    }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterOptions(
    options: List<String>,
    enabled: Boolean,
    shakePx: Float,
    onPick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        options.forEach { letter ->
            Button(onClick = { onPick(letter) }, enabled = enabled) {
                Text(text = letter, fontSize = 42.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun playSuccessAnimation(
    scope: CoroutineScope,
    dinoScale: androidx.compose.animation.core.Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    showConfetti: MutableState<Boolean>,
): Job = scope.launch {
    showConfetti.value = true
    dinoScale.snapTo(1f)
    dinoScale.animateTo(
        targetValue = 1.18f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 140),
    )
    dinoScale.animateTo(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.45f, stiffness = 600f),
    )
    delay(450)
    showConfetti.value = false
}

private fun playMistakeAnimation(
    scope: CoroutineScope,
    optionsShake: androidx.compose.animation.core.Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
): Job = scope.launch {
    optionsShake.snapTo(0f)
    val amp = 18f
    repeat(5) { i ->
        optionsShake.animateTo(
            targetValue = if (i % 2 == 0) amp else -amp,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 45),
        )
    }
    optionsShake.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 60))
}

@Composable
private fun ConfettiOverlay(modifier: Modifier = Modifier) {
    // Very lightweight "confetti": a few translucent circles; enough for MVP.
    Box(modifier = modifier) {
        repeat(14) { idx ->
            val x = (idx * 23) % 100
            val y = (idx * 37) % 100
            val color = when (idx % 5) {
                0 -> Color(0x66FF6B6B)
                1 -> Color(0x66FFD93D)
                2 -> Color(0x666BCB77)
                3 -> Color(0x664D96FF)
                else -> Color(0x66B983FF)
            }
            Spacer(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x.dp * 3, y.dp * 5)
                    .size(18.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}

