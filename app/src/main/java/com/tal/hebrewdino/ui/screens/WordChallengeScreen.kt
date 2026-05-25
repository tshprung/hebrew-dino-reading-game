package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.delay

@Composable
fun WordChallengeScreen(
    onExitToHome: () -> Unit,
    onRoundCompleteToHome: () -> Unit,
    challengeType: ChallengeType = ChallengeType.ODD_ONE_OUT,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repo = remember(context) { CharacterRepository(context.applicationContext) }
    var showEarned by remember { mutableStateOf(false) }

    val viewModel: WordChallengeViewModel =
        viewModel(
            factory =
                remember(challengeType, repo) {
                    WordChallengeViewModel.Factory(
                        challengeType = challengeType,
                        rewardHandler = WordChallengeViewModel.RewardHandler {
                            repo.addFood(3)
                            repo.setPendingRewardFoodDelta(3)
                        },
                    )
                },
        )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.finishEvents.collect {
            showEarned = true
            delay(900L)
            onRoundCompleteToHome()
        }
    }

    WordChallengeContent(
        challengeType = state.challengeType,
        questionText = state.current?.questionText.orEmpty(),
        options = state.current?.options.orEmpty(),
        correctOption = state.current?.correctOption,
        selectedCorrectOption = state.selectedCorrectOption,
        wrongAttemptToken = state.wrongAttemptToken,
        lastWrongOption = state.lastWrongOption,
        questionNumber = state.questionNumber,
        totalQuestions = state.total.coerceAtLeast(5),
        showEarnedReward = showEarned,
        onExit = onExitToHome,
        onOptionSelected = viewModel::onOptionSelected,
        modifier = modifier,
    )
}

@Composable
private fun WordChallengeContent(
    challengeType: ChallengeType,
    questionText: String,
    options: List<String>,
    correctOption: String?,
    selectedCorrectOption: String?,
    wrongAttemptToken: Int,
    lastWrongOption: String?,
    questionNumber: Int,
    totalQuestions: Int,
    showEarnedReward: Boolean,
    onExit: () -> Unit,
    onOptionSelected: (String) -> Unit,
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
                Text(
                    text =
                        rtl(
                            when (challengeType) {
                                ChallengeType.RHYME -> "מצאו את המילה המתחרזת עם: $questionText"
                                else -> questionText
                            },
                        ),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                )
            }

            OptionsGrid(
                options = options,
                correctOption = correctOption,
                selectedCorrectOption = selectedCorrectOption,
                wrongAttemptToken = wrongAttemptToken,
                lastWrongOption = lastWrongOption,
                onOptionSelected = onOptionSelected,
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
    options: List<String>,
    correctOption: String?,
    selectedCorrectOption: String?,
    wrongAttemptToken: Int,
    lastWrongOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padded =
        when {
            options.size >= 4 -> options.take(4)
            else -> options + List(4 - options.size) { "" }
        }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OptionCard(
                text = padded[0],
                isCorrect = padded[0].isNotEmpty() && padded[0] == correctOption,
                isSelectedCorrect = padded[0].isNotEmpty() && padded[0] == selectedCorrectOption,
                shouldShake = padded[0].isNotEmpty() && padded[0] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[0].isNotEmpty() && selectedCorrectOption == null,
                onClick = { onOptionSelected(padded[0]) },
                modifier = Modifier.weight(1f).height(88.dp),
            )
            OptionCard(
                text = padded[1],
                isCorrect = padded[1].isNotEmpty() && padded[1] == correctOption,
                isSelectedCorrect = padded[1].isNotEmpty() && padded[1] == selectedCorrectOption,
                shouldShake = padded[1].isNotEmpty() && padded[1] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[1].isNotEmpty() && selectedCorrectOption == null,
                onClick = { onOptionSelected(padded[1]) },
                modifier = Modifier.weight(1f).height(88.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OptionCard(
                text = padded[2],
                isCorrect = padded[2].isNotEmpty() && padded[2] == correctOption,
                isSelectedCorrect = padded[2].isNotEmpty() && padded[2] == selectedCorrectOption,
                shouldShake = padded[2].isNotEmpty() && padded[2] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[2].isNotEmpty() && selectedCorrectOption == null,
                onClick = { onOptionSelected(padded[2]) },
                modifier = Modifier.weight(1f).height(88.dp),
            )
            OptionCard(
                text = padded[3],
                isCorrect = padded[3].isNotEmpty() && padded[3] == correctOption,
                isSelectedCorrect = padded[3].isNotEmpty() && padded[3] == selectedCorrectOption,
                shouldShake = padded[3].isNotEmpty() && padded[3] == lastWrongOption,
                wrongAttemptToken = wrongAttemptToken,
                enabled = padded[3].isNotEmpty() && selectedCorrectOption == null,
                onClick = { onOptionSelected(padded[3]) },
                modifier = Modifier.weight(1f).height(88.dp),
            )
        }
    }
}

@Composable
private fun OptionCard(
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rtl(text),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
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
