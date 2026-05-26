package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun WordChallengeScreen(
    onExitToHome: () -> Unit,
    onRoundCompleteToSummary: () -> Unit,
    challengeType: ChallengeType = ChallengeType.ODD_ONE_OUT,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repo = remember(context) { CharacterRepository(context.applicationContext) }
    val sfx = remember(context) { SfxManager(context.applicationContext) }
    val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
    val scope = rememberCoroutineScope()

    DisposableEffect(sfx) {
        onDispose {
            sfx.release()
        }
    }
    DisposableEffect(tts) {
        onDispose {
            tts.stop()
        }
    }

    val viewModel: WordChallengeViewModel =
        viewModel(
            factory =
                remember(challengeType, repo) {
                    WordChallengeViewModel.Factory(
                        challengeType = challengeType,
                        rewardHandler = WordChallengeViewModel.RewardHandler {
                            if (challengeType == ChallengeType.LETTER_RECOGNITION) {
                                repo.markChapter1StationCompleted(1)
                            }
                            if (challengeType == ChallengeType.PHONEMIC_ISOLATION) {
                                repo.markChapter1StationCompleted(2)
                            }
                            repo.setFullUntilAtMs(0L)
                            repo.addFood(3)
                            repo.setPendingRewardFoodDelta(3)
                        },
                    )
                },
        )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.finishEvents.collect {
            tts.stop()
            onRoundCompleteToSummary()
        }
    }

    val current = state.current
    val options = current?.options.orEmpty()
    val correctOption = current?.correctOption
    var localTapLocked by remember { mutableStateOf(false) }
    LaunchedEffect(state.isInputLocked, state.selectedCorrectOption, state.isRoundComplete) {
        if (state.isInputLocked) {
            localTapLocked = true
            return@LaunchedEffect
        }
        if (state.selectedCorrectOption == null && !state.isRoundComplete) {
            localTapLocked = false
        }
    }
    val interactionEnabled = current != null && !localTapLocked && !state.isRoundComplete

    val targetLetterSymbol = current?.questionText.orEmpty().trim().firstOrNull()?.toString().orEmpty()

    val voiceInstructionText =
        when (state.challengeType) {
            ChallengeType.LETTER_RECOGNITION -> {
                val name = hebrewLetterNameForSpeech(targetLetterSymbol) ?: targetLetterSymbol
                if (name.isBlank()) "איפה האות?" else "איפה האות $name?"
            }
            ChallengeType.PHONEMIC_ISOLATION -> {
                val name = hebrewLetterNameForSpeech(targetLetterSymbol) ?: targetLetterSymbol
                if (name.isBlank()) "איפה האות?" else "איפה האות $name?"
            }
            ChallengeType.RHYME ->
                "מצאו את המילה המתחרזת עם: ${current?.questionText.orEmpty()}".trim()
            ChallengeType.ODD_ONE_OUT ->
                buildOddOneOutInstruction(
                    options = options,
                    correctOption = correctOption,
                )
            else -> current?.questionText.orEmpty().trim()
        }

    val topInstructionText =
        when (state.challengeType) {
            ChallengeType.LETTER_RECOGNITION,
            ChallengeType.PHONEMIC_ISOLATION,
            -> "איפה האות?"
            else -> voiceInstructionText
        }

    var lastSpokenQuestionNumber by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.questionNumber, current?.questionText) {
        if (current == null) return@LaunchedEffect
        if (state.questionNumber == lastSpokenQuestionNumber) return@LaunchedEffect
        lastSpokenQuestionNumber = state.questionNumber
        val txt = voiceInstructionText
        if (txt.isNotBlank()) tts.speak(txt)
    }

    WordChallengeContent(
        challengeType = state.challengeType,
        instructionText = topInstructionText,
        targetLetterSymbol =
            if (state.challengeType == ChallengeType.LETTER_RECOGNITION ||
                state.challengeType == ChallengeType.PHONEMIC_ISOLATION
            ) {
                targetLetterSymbol
            } else {
                null
            },
        options = options,
        correctOption = correctOption,
        selectedCorrectOption = state.selectedCorrectOption,
        wrongAttemptToken = state.wrongAttemptToken,
        lastWrongOption = state.lastWrongOption,
        questionNumber = state.questionNumber,
        totalQuestions = state.total,
        showEarnedReward = false,
        onExit = {
            tts.stop()
            onExitToHome()
        },
        onReplayInstruction = { if (interactionEnabled) tts.speak(voiceInstructionText) },
        onOptionSelected = { option ->
            if (!interactionEnabled) return@WordChallengeContent
            localTapLocked = true
            val correct = correctOption
            val isCorrectNow = !correct.isNullOrBlank() && option.isNotBlank() && option == correct
            val expectedToken = if (isCorrectNow) state.pendingCorrectToken + 1 else null
            if (state.selectedCorrectOption == null && option.isNotBlank() && !correct.isNullOrBlank()) {
                scope.launch {
                    if (option == correct) sfx.playCorrect() else sfx.playWrong()
                }
            }
            viewModel.onOptionSelected(option)
            if (isCorrectNow && expectedToken != null && state.selectedCorrectOption == null) {
                scope.launch {
                    val praise = PraisePhrases[Random.nextInt(PraisePhrases.size)]
                    tts.speakAndWait(praise)
                    viewModel.confirmAdvanceAfterCorrect(expectedToken)
                }
            } else if (option.isNotBlank() && !correct.isNullOrBlank() && state.selectedCorrectOption == null) {
                val spoken =
                    if (state.challengeType == ChallengeType.LETTER_RECOGNITION ||
                        state.challengeType == ChallengeType.PHONEMIC_ISOLATION
                    ) {
                        hebrewLetterNameForSpeech(option.trim()) ?: option
                    } else {
                        option
                    }
                tts.speak(spoken)
            }
        },
        interactionEnabled = interactionEnabled,
        modifier = modifier,
    )
}

private fun buildOddOneOutInstruction(
    options: List<String>,
    correctOption: String?,
): String {
    if (options.size != 4 || correctOption.isNullOrBlank()) {
        return "איזו מילה מתחילה באות שונה מהאחרות?"
    }
    val correctLetter = firstLetterOrNull(correctOption) ?: return "איזו מילה מתחילה באות שונה מהאחרות?"
    val counts =
        options
            .mapNotNull(::firstLetterOrNull)
            .groupingBy { it }
            .eachCount()
    val correctCount = counts[correctLetter] ?: 0
    if (correctCount != 1) {
        return "איזו מילה מתחילה באות שונה מהאחרות?"
    }
    val (majorLetter, majorCount) = counts.entries.maxByOrNull { it.value } ?: return "איזו מילה מתחילה באות שונה מהאחרות?"
    if (majorCount != 3 || majorLetter == correctLetter) {
        return "איזו מילה מתחילה באות שונה מהאחרות?"
    }
    val name = hebrewLetterNameForSpeech(correctLetter) ?: return "איזו מילה מתחילה באות שונה מהאחרות?"
    return "איזו מילה מתחילה באות $name?"
}

private fun firstLetterOrNull(word: String): String? {
    val trimmed = word.trim()
    if (trimmed.isEmpty()) return null
    return trimmed.first().toString()
}

private fun hebrewLetterNameForSpeech(letter: String): String? =
    when (letter) {
        "א" -> "אָלֶף"
        "ב" -> "בֵּית"
        "ג" -> "גִּימֶל"
        "ד" -> "דָּלֶת"
        "ה" -> "הֵא"
        "ו" -> "וָו"
        "ז" -> "זַיִן"
        "ח" -> "חֵית"
        "ט" -> "טֵית"
        "י" -> "יוֹד"
        "כ", "ך" -> "כָּף"
        "ל" -> "לָמֶד"
        "מ", "ם" -> "מֵם"
        "נ", "ן" -> "נוּן"
        "ס" -> "סָמֶךְ"
        "ע" -> "עַיִן"
        "פ", "ף" -> "פֵּא"
        "צ", "ץ" -> "צַדִּי"
        "ק" -> "קוּף"
        "ר" -> "רֵישׁ"
        "ש" -> "שִׁין"
        "ת" -> "תָּו"
        else -> null
    }

@Composable
private fun WordChallengeContent(
    challengeType: ChallengeType,
    instructionText: String,
    targetLetterSymbol: String?,
    options: List<String>,
    correctOption: String?,
    selectedCorrectOption: String?,
    wrongAttemptToken: Int,
    lastWrongOption: String?,
    questionNumber: Int,
    totalQuestions: Int,
    showEarnedReward: Boolean,
    onExit: () -> Unit,
    onReplayInstruction: () -> Unit,
    onOptionSelected: (String) -> Unit,
    interactionEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        TopBar(
            progressText = rtl("שאלה $questionNumber מתוך $totalQuestions"),
            onExit = onExit,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.22f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = rtl(instructionText),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        onClick = onReplayInstruction,
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.height(44.dp),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "🔊",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            if (!targetLetterSymbol.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    border = BorderStroke(4.dp, Color(0xFF0B2B3D).copy(alpha = 0.22f)),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 104.dp, max = 168.dp)
                            .padding(top = 10.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = rtl(targetLetterSymbol),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 96.sp,
                            ),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            OptionsGrid(
                challengeType = challengeType,
                options = options,
                correctOption = correctOption,
                selectedCorrectOption = selectedCorrectOption,
                wrongAttemptToken = wrongAttemptToken,
                lastWrongOption = lastWrongOption,
                onOptionSelected = onOptionSelected,
                interactionEnabled = interactionEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (showEarnedReward) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.35f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 18.dp),
            ) {
                Text(
                    text = rtl("קיבלת +3 🍎!"),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    progressText: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.28f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }

        Surface(
            onClick = onExit,
            shape = RoundedCornerShape(999.dp),
            color = Color.Black.copy(alpha = 0.28f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = rtl("יציאה"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun OptionsGrid(
    challengeType: ChallengeType,
    options: List<String>,
    correctOption: String?,
    selectedCorrectOption: String?,
    wrongAttemptToken: Int,
    lastWrongOption: String?,
    onOptionSelected: (String) -> Unit,
    interactionEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val padded =
        when {
            options.size >= 4 -> options.take(4)
            else -> options + List(4 - options.size) { "" }
        }
    BoxWithConstraints(modifier = modifier) {
        val globalEnabled = interactionEnabled && selectedCorrectOption == null
        val desiredCardH =
            if (challengeType == ChallengeType.LETTER_RECOGNITION ||
                challengeType == ChallengeType.PHONEMIC_ISOLATION
            ) {
                110.dp
            } else {
                88.dp
            }
        val maxCardH = ((maxHeight - 12.dp) / 2f).coerceAtLeast(72.dp)
        val cardH = minOf(desiredCardH, maxCardH)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            OptionCard(
                challengeType = challengeType,
                text = padded[0],
                isCorrect = padded[0].isNotEmpty() && padded[0] == correctOption,
                isSelectedCorrect = padded[0].isNotEmpty() && padded[0] == selectedCorrectOption,
                shouldShake = padded[0].isNotEmpty() && padded[0] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[0].isNotEmpty() && globalEnabled,
                onClick = { onOptionSelected(padded[0]) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(cardH),
            )
            OptionCard(
                challengeType = challengeType,
                text = padded[1],
                isCorrect = padded[1].isNotEmpty() && padded[1] == correctOption,
                isSelectedCorrect = padded[1].isNotEmpty() && padded[1] == selectedCorrectOption,
                shouldShake = padded[1].isNotEmpty() && padded[1] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[1].isNotEmpty() && globalEnabled,
                onClick = { onOptionSelected(padded[1]) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(cardH),
            )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            OptionCard(
                challengeType = challengeType,
                text = padded[2],
                isCorrect = padded[2].isNotEmpty() && padded[2] == correctOption,
                isSelectedCorrect = padded[2].isNotEmpty() && padded[2] == selectedCorrectOption,
                shouldShake = padded[2].isNotEmpty() && padded[2] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[2].isNotEmpty() && globalEnabled,
                onClick = { onOptionSelected(padded[2]) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(cardH),
            )
            OptionCard(
                challengeType = challengeType,
                text = padded[3],
                isCorrect = padded[3].isNotEmpty() && padded[3] == correctOption,
                isSelectedCorrect = padded[3].isNotEmpty() && padded[3] == selectedCorrectOption,
                shouldShake = padded[3].isNotEmpty() && padded[3] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[3].isNotEmpty() && globalEnabled,
                onClick = { onOptionSelected(padded[3]) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(cardH),
            )
            }
        }
    }
}

@Composable
private fun OptionCard(
    challengeType: ChallengeType,
    text: String,
    isCorrect: Boolean,
    isSelectedCorrect: Boolean,
    shouldShake: Boolean,
    wrongAttemptToken: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(shouldShake, wrongAttemptToken) {
        if (!shouldShake) return@LaunchedEffect
        shakeX.snapTo(0f)
        shakeX.animateTo(-10f, tween(40))
        shakeX.animateTo(10f, tween(70))
        shakeX.animateTo(-7f, tween(60))
        shakeX.animateTo(7f, tween(60))
        shakeX.animateTo(0f, tween(50))
    }

    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(isSelectedCorrect) {
        if (!isSelectedCorrect) return@LaunchedEffect
        flashAlpha.snapTo(0.0f)
        flashAlpha.animateTo(0.55f, tween(90))
        flashAlpha.animateTo(0.0f, tween(190))
    }

    val baseColors =
        if (isSelectedCorrect) {
            listOf(
                Color(0xFFB6F2C1).copy(alpha = 0.65f),
                Color(0xFF59D98E).copy(alpha = 0.70f),
                Color(0xFF2DB86E).copy(alpha = 0.75f),
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.50f),
            )
        }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
            modifier
                .graphicsLayer { translationX = shakeX.value }
                .scale(if (isSelectedCorrect) 1.02f else 1f),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(colors = baseColors),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .background(Color.Black.copy(alpha = 0.06f), shape = RoundedCornerShape(18.dp))
                    .then(
                        if (enabled) {
                            Modifier.clickable(onClick = onClick)
                        } else {
                            Modifier
                        },
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical =
                            if (challengeType == ChallengeType.LETTER_RECOGNITION ||
                                challengeType == ChallengeType.PHONEMIC_ISOLATION
                            ) {
                                6.dp
                            } else {
                                10.dp
                            },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            val textStyle =
                if (challengeType == ChallengeType.LETTER_RECOGNITION || challengeType == ChallengeType.PHONEMIC_ISOLATION) {
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 56.sp,
                    )
                } else {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                }
            Text(
                text = rtl(text),
                style = textStyle,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
            if (flashAlpha.value > 0f) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .background(Color(0xFF2DB86E).copy(alpha = flashAlpha.value), RoundedCornerShape(18.dp)),
                )
            }
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"

private val PraisePhrases: Array<String> =
    arrayOf(
        "כל הכבוד!",
        "יפה מאוד!",
        "הצלחת!",
    )
