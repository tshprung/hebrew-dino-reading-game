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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.InteractionAudio
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.economy.RewardEngine
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.domain.hebrewLetterNameForSpeech
import com.tal.hebrewdino.ui.domain.letterNameSpokenForTts
import com.tal.hebrewdino.ui.domain.phonemeForDisplay
import com.tal.hebrewdino.ui.domain.phonemeSpokenForTts
import com.tal.hebrewdino.ui.domain.requireHebrewLetterNameForTts
import com.tal.hebrewdino.ui.domain.applyChildFriendlyTtsWorkarounds
import com.tal.hebrewdino.ui.domain.targetSuccessSpeech
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WordChallengeScreen(
    onExitToHome: () -> Unit,
    onRoundCompleteToSummary: () -> Unit,
    challengeType: ChallengeType = ChallengeType.ODD_ONE_OUT,
    chapterIndex: Int = 0,
    rewardEngine: RewardEngine,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val stopInteractionAudio = remember(appContext) { { InteractionAudio.stopAllNow(appContext) } }
    val sfx = remember(appContext) { SfxManager(appContext) }
    val tts = remember(appContext) { TextToSpeechManager.get(appContext) }
    val scope = rememberCoroutineScope()

    DisposableEffect(sfx) {
        onDispose {
            sfx.release()
        }
    }
    val viewModel: WordChallengeViewModel =
        viewModel(
            factory =
                remember(challengeType, chapterIndex, rewardEngine) {
                    WordChallengeViewModel.Factory(
                        challengeType = challengeType,
                        chapterIndex = chapterIndex,
                        rewardEngine = rewardEngine,
                    )
                },
        )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.finishEvents.collect {
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

    val topInstructionText =
        when (state.challengeType) {
            ChallengeType.LETTER_RECOGNITION -> "איפה האות?"
            ChallengeType.PHONEMIC_ISOLATION -> "חַפְּשׂוּ אֶת הַצְּלִיל..."
            else -> ""
        }

    val targetBadgeText =
        when (state.challengeType) {
            ChallengeType.PHONEMIC_ISOLATION -> phonemeForDisplay(targetLetterSymbol).ifBlank { targetLetterSymbol }
            ChallengeType.LETTER_RECOGNITION -> targetLetterSymbol
            else -> null
        }

    var replayInstructionEpoch by remember { mutableIntStateOf(0) }
    val requestInstructionReplay = {
        if (interactionEnabled) replayInstructionEpoch += 1
    }
    val bumpInstructionActivity =
        rememberInstructionIdleReplay(
            enabled = interactionEnabled,
            resetKey = state.index,
            onReplay = requestInstructionReplay,
        )
    val currentQuestionIndex = state.index
    LaunchedEffect(currentQuestionIndex, state.challengeType) {
        bumpInstructionActivity()
        val question = state.challenges.getOrNull(currentQuestionIndex) ?: return@LaunchedEffect
        val letter = question.questionText.trim().firstOrNull()?.toString().orEmpty()
        val txt = voiceInstructionForQuestion(state.challengeType, question, letter)
        if (txt.isNotBlank()) tts.speakFully(txt, navigationSettleMs = 0L)
    }

    LaunchedEffect(replayInstructionEpoch) {
        if (replayInstructionEpoch == 0) return@LaunchedEffect
        val question = state.challenges.getOrNull(currentQuestionIndex) ?: return@LaunchedEffect
        val letter = question.questionText.trim().firstOrNull()?.toString().orEmpty()
        val txt = voiceInstructionForQuestion(state.challengeType, question, letter)
        if (txt.isNotBlank()) {
            tts.interruptAndSpeak(applyChildFriendlyTtsWorkarounds(txt))
        }
    }

    LaunchedEffect(state.pendingCorrectToken, state.selectedCorrectOption) {
        val selected = state.selectedCorrectOption?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (state.isRoundComplete || state.pendingCorrectToken == 0) return@LaunchedEffect
        val question = state.challenges.getOrNull(state.index) ?: return@LaunchedEffect
        val targetLetter = question.questionText.trim().firstOrNull()?.toString().orEmpty()
        val expectedToken = state.pendingCorrectToken
        val reinforcement = targetSuccessSpeech(state.challengeType, targetLetter)
        tts.speakFullyThen(reinforcement, navigationSettleMs = 80L) {
            scope.launch {
                viewModel.confirmAdvanceAfterCorrect(expectedToken)
            }
        }
    }

    WordChallengeContent(
        challengeType = state.challengeType,
        instructionText = topInstructionText,
        targetBadgeText = targetBadgeText,
        options = options,
        correctOption = correctOption,
        selectedCorrectOption = state.selectedCorrectOption,
        wrongAttemptToken = state.wrongAttemptToken,
        lastWrongOption = state.lastWrongOption,
        questionNumber = state.questionNumber,
        totalQuestions = state.total,
        showEarnedReward = false,
        onExit = {
            stopInteractionAudio()
            onExitToHome()
        },
        onReplayInstruction = {
            stopInteractionAudio()
            bumpInstructionActivity()
            requestInstructionReplay()
        },
        onOptionSelected = { option ->
            if (!interactionEnabled) return@WordChallengeContent
            bumpInstructionActivity()
            localTapLocked = true
            val correct = correctOption
            val isLetterStation =
                state.challengeType == ChallengeType.LETTER_RECOGNITION ||
                    state.challengeType == ChallengeType.PHONEMIC_ISOLATION
            val isWrongTap =
                state.selectedCorrectOption == null &&
                    option.isNotBlank() &&
                    !correct.isNullOrBlank() &&
                    option != correct
            if (isLetterStation && isWrongTap) {
                val spoken =
                    when (state.challengeType) {
                        ChallengeType.PHONEMIC_ISOLATION ->
                            phonemeSpokenForTts(option).ifBlank { option }
                        else -> letterNameSpokenForTts(option).ifBlank { option }
                    }
                tts.interruptAndSpeak(applyChildFriendlyTtsWorkarounds(spoken))
            } else {
                stopInteractionAudio()
            }
            if (state.selectedCorrectOption == null && option.isNotBlank() && !correct.isNullOrBlank()) {
                scope.launch {
                    if (option == correct) sfx.playCorrect() else sfx.playWrong()
                }
            }
            viewModel.onOptionSelected(option)
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

private fun voiceInstructionForQuestion(
    challengeType: ChallengeType,
    question: com.tal.hebrewdino.ui.domain.WordChallenge,
    letter: String,
): String =
    when (challengeType) {
        ChallengeType.LETTER_RECOGNITION -> {
            val name = requireHebrewLetterNameForTts(letter)
            "איפה האות $name?"
        }
        ChallengeType.PHONEMIC_ISOLATION -> {
            val phoneme = phonemeSpokenForTts(letter)
            if (phoneme.isBlank()) {
                "חַפְּשׂוּ אֶת הַצְּלִיל"
            } else {
                "חַפְּשׂוּ אֶת הַצְּלִיל, $phoneme"
            }
        }
        ChallengeType.RHYME ->
            "מצאו את המילה המתחרזת עם: ${question.questionText}".trim()
        ChallengeType.ODD_ONE_OUT ->
            buildOddOneOutInstruction(
                options = question.options,
                correctOption = question.correctOption,
            )
        else -> question.questionText.trim()
    }

@Composable
private fun WordChallengeContent(
    challengeType: ChallengeType,
    instructionText: String,
    targetBadgeText: String?,
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

        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            val topReserved = 44.dp
            val safeH = (maxHeight - topReserved).coerceAtLeast(220.dp)
            val compactHeader =
                challengeType == ChallengeType.LETTER_RECOGNITION ||
                    challengeType == ChallengeType.PHONEMIC_ISOLATION
            val headerH =
                if (compactHeader) {
                    (safeH * 0.34f).coerceAtLeast(118.dp).coerceAtMost(148.dp)
                } else {
                    (safeH * 0.30f).coerceAtLeast(100.dp).coerceAtMost(132.dp)
                }
            val gridH = (safeH - headerH).coerceAtLeast(safeH * 0.66f)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(topReserved))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.22f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth().height(headerH),
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 6.dp)) {
                        if (compactHeader) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(end = 40.dp)
                                        .clickable(onClick = onReplayInstruction),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = rtl(instructionText),
                                    style =
                                        MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize =
                                                if (challengeType == ChallengeType.LETTER_RECOGNITION) {
                                                    26.sp
                                                } else {
                                                    17.sp
                                                },
                                        ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                )
                                if (!targetBadgeText.isNullOrBlank()) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        StationTargetLetterBadge(
                                            text = targetBadgeText,
                                            challengeType = challengeType,
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = rtl(instructionText),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .clickable(onClick = onReplayInstruction)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                        Surface(
                            onClick = onReplayInstruction,
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier =
                                Modifier
                                    .align(Alignment.CenterEnd)
                                    .height(34.dp),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "🔊",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                val gridModifier = Modifier.fillMaxWidth().height(gridH)
                when (challengeType) {
                    ChallengeType.LETTER_RECOGNITION -> {
                        Station1ThreeCards(
                            options = options,
                            correctOption = correctOption,
                            selectedCorrectOption = selectedCorrectOption,
                            wrongAttemptToken = wrongAttemptToken,
                            lastWrongOption = lastWrongOption,
                            onOptionSelected = onOptionSelected,
                            interactionEnabled = interactionEnabled,
                            modifier = gridModifier,
                        )
                    }
                    ChallengeType.PHONEMIC_ISOLATION -> {
                        Station2Bubbles(
                            options = options,
                            correctOption = correctOption,
                            selectedCorrectOption = selectedCorrectOption,
                            wrongAttemptToken = wrongAttemptToken,
                            lastWrongOption = lastWrongOption,
                            onOptionSelected = onOptionSelected,
                            interactionEnabled = interactionEnabled,
                            modifier = gridModifier,
                        )
                    }
                    else -> {
                        OptionsGrid(
                            challengeType = challengeType,
                            options = options,
                            correctOption = correctOption,
                            selectedCorrectOption = selectedCorrectOption,
                            wrongAttemptToken = wrongAttemptToken,
                            lastWrongOption = lastWrongOption,
                            onOptionSelected = onOptionSelected,
                            interactionEnabled = interactionEnabled,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
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

private fun padOptionsToThree(options: List<String>): List<String> =
    options + List((3 - options.size).coerceAtLeast(0)) { "" }

@Composable
private fun Station1ThreeCards(
    options: List<String>,
    correctOption: String?,
    selectedCorrectOption: String?,
    wrongAttemptToken: Int,
    lastWrongOption: String?,
    onOptionSelected: (String) -> Unit,
    interactionEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val three = padOptionsToThree(options)
    val globalEnabled = interactionEnabled && selectedCorrectOption == null
    BoxWithConstraints(modifier = modifier) {
        val cardH = minOf(maxHeight, 156.dp)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 0..2) {
                val text = three[i]
                OptionCard(
                    challengeType = ChallengeType.LETTER_RECOGNITION,
                    text = text,
                    isCorrect = text.isNotEmpty() && text == correctOption,
                    isSelectedCorrect = text.isNotEmpty() && text == selectedCorrectOption,
                    shouldShake = text.isNotEmpty() && text == lastWrongOption,
                    wrongAttemptToken = wrongAttemptToken,
                    enabled = text.isNotEmpty() && globalEnabled,
                    onClick = { onOptionSelected(text) },
                    modifier = Modifier.weight(1f).height(cardH),
                )
            }
        }
    }
}

@Composable
private fun Station2Bubbles(
    options: List<String>,
    correctOption: String?,
    selectedCorrectOption: String?,
    wrongAttemptToken: Int,
    lastWrongOption: String?,
    onOptionSelected: (String) -> Unit,
    interactionEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val three = padOptionsToThree(options)
    val globalEnabled = interactionEnabled && selectedCorrectOption == null
    BoxWithConstraints(modifier = modifier) {
        val size = minOf(maxHeight, (maxWidth - 16.dp) / 3f).coerceAtLeast(72.dp).coerceAtMost(120.dp)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 0..2) {
                val text = three[i]
                BubbleOption(
                    text = text,
                    index = i,
                    isCorrect = text.isNotEmpty() && text == correctOption,
                    isSelectedCorrect = text.isNotEmpty() && text == selectedCorrectOption,
                    shouldShake = text.isNotEmpty() && text == lastWrongOption,
                    wrongAttemptToken = wrongAttemptToken,
                    enabled = text.isNotEmpty() && globalEnabled,
                    onClick = { onOptionSelected(text) },
                    modifier = Modifier.size(size),
                )
            }
        }
    }
}

@Composable
private fun BubbleOption(
    text: String,
    index: Int,
    isCorrect: Boolean,
    isSelectedCorrect: Boolean,
    shouldShake: Boolean,
    wrongAttemptToken: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shakeX = remember { Animatable(0f) }
    val successScale = remember { Animatable(1f) }
    LaunchedEffect(shouldShake, wrongAttemptToken) {
        if (!shouldShake) return@LaunchedEffect
        shakeX.snapTo(0f)
        shakeX.animateTo(-10f, tween(40))
        shakeX.animateTo(10f, tween(70))
        shakeX.animateTo(-7f, tween(60))
        shakeX.animateTo(7f, tween(60))
        shakeX.animateTo(0f, tween(50))
    }
    LaunchedEffect(isSelectedCorrect) {
        if (!isSelectedCorrect) return@LaunchedEffect
        successScale.snapTo(1f)
        successScale.animateTo(1.12f, tween(130))
        successScale.animateTo(1.04f, tween(120))
    }

    val colors =
        remember(isSelectedCorrect) {
            if (isSelectedCorrect) {
                listOf(Color(0xFFB6F2C1), Color(0xFF2DB86E))
            } else {
                listOf(Color(0xFFE8F4FC), Color(0xFFD4E8F7))
            }
        }

    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
            modifier
                .graphicsLayer {
                    translationX = shakeX.value
                    scaleX = successScale.value
                    scaleY = successScale.value
                },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (isSelectedCorrect) {
                            Modifier.border(4.dp, Color(0xFF2DB86E), CircleShape)
                        } else {
                            Modifier
                        },
                    )
                    .background(brush = Brush.radialGradient(colors = colors), shape = CircleShape)
                    .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                    .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rtl(text),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 48.sp),
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
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
    val successScale = remember { Animatable(1f) }
    LaunchedEffect(isSelectedCorrect) {
        if (!isSelectedCorrect) return@LaunchedEffect
        flashAlpha.snapTo(0.0f)
        successScale.snapTo(1f)
        successScale.animateTo(1.1f, tween(120))
        flashAlpha.animateTo(0.55f, tween(90))
        flashAlpha.animateTo(0.0f, tween(190))
        successScale.animateTo(1.04f, tween(140))
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
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
            modifier
                .graphicsLayer { translationX = shakeX.value }
                .scale(successScale.value),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (isSelectedCorrect) {
                            Modifier.border(4.dp, Color(0xFF2DB86E), RoundedCornerShape(14.dp))
                        } else {
                            Modifier
                        },
                    )
                    .background(
                        brush = Brush.verticalGradient(colors = baseColors),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .background(Color.Black.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                    .then(
                        if (enabled) {
                            Modifier.clickable(onClick = onClick)
                        } else {
                            Modifier
                        },
                    )
                    .padding(
                        horizontal = 8.dp,
                        vertical =
                            if (challengeType == ChallengeType.LETTER_RECOGNITION ||
                                challengeType == ChallengeType.PHONEMIC_ISOLATION
                            ) {
                                4.dp
                            } else {
                                8.dp
                            },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            val textStyle =
                if (challengeType == ChallengeType.LETTER_RECOGNITION || challengeType == ChallengeType.PHONEMIC_ISOLATION) {
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 52.sp,
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

@Composable
private fun StationTargetLetterBadge(
    text: String,
    challengeType: ChallengeType,
    modifier: Modifier = Modifier,
) {
    val letterSize =
        if (challengeType == ChallengeType.PHONEMIC_ISOLATION) {
            40.sp
        } else {
            44.sp
        }
    Box(
        modifier =
            modifier
                .size(88.dp)
                .background(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color(0xFFFFE27A),
                                    Color(0xFFFFB82E),
                                    Color(0xFFFF9A1A),
                                ),
                        ),
                    shape = CircleShape,
                )
                .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rtl(text),
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = letterSize,
                    lineHeight = letterSize,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle =
                        LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                ),
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
        )
    }
}

private fun rtl(text: String): String = "\u200F$text"

