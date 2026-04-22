package com.tal.hebrewdino.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun ImageMatchGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    /** After 2 wrong taps, pulse the correct answer (subtle hint). */
    hintCorrectChoiceId: String? = null,
    hintPulseEpoch: Int = 0,
    onAttempt: (String) -> Boolean,
    /** When false, cards show only the picture (for pre-readers). */
    showWordCaptions: Boolean = true,
    captionSizeMultiplier: Float = 1f,
    pictureSizeMultiplier: Float = 1f,
    /** Scales only the illustration inside the card frame (card outer size unchanged). */
    innerPictureScaleForChoice: (LessonChoice) -> Float = { 1f },
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var successChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    LaunchedEffect(contentKey) {
        successChoiceId = null
    }
    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
        ) {
            val rowInnerWidth = maxWidth
            val choiceCount = question.choices.size.coerceAtLeast(1)
            val cardGap = 10.dp
            var cardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = rowInnerWidth,
                    count = choiceCount,
                    gap = cardGap,
                    minEach = 72.dp,
                    maxEach = 168.dp,
                )
            cardW = (cardW * pictureSizeMultiplier).coerceAtMost((rowInnerWidth - cardGap * (choiceCount - 1)) / choiceCount)
            val cardH = cardW * (110f / 160f)
            val density = LocalDensity.current
            val narrowRow = rowInnerWidth < 380.dp
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Large target letter, kept high so picture cards do not cover it.
                Text(
                    text = question.targetLetter,
                    style =
                        if (rowInnerWidth < 420.dp) {
                            androidx.compose.material3.MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                        } else {
                            androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black)
                        },
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .padding(top = 2.dp)
                            .offset(y = (-22).dp),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(if (narrowRow) 22.dp else 28.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(shakePx.roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                ) {
                    question.choices.forEach { choice ->
                        val scale = remember(choice.id, contentKey) { Animatable(1f) }
                        LaunchedEffect(hintPulseEpoch, hintCorrectChoiceId, choice.id, contentKey) {
                            if (hintPulseEpoch <= 0 || hintCorrectChoiceId != choice.id) return@LaunchedEffect
                            scale.snapTo(1f)
                            scale.animateTo(1.14f, tween(120))
                            scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
                        }
                        val captionSp =
                            with(density) {
                                (cardW.toPx() * 0.22f * captionSizeMultiplier).coerceIn(
                                    22f * fontScale * captionSizeMultiplier,
                                    40f * fontScale * captionSizeMultiplier,
                                ).toSp()
                            }
                        LessonChoiceCard(
                            choice = choice,
                            enabled = enabled,
                            scale = scale.value,
                            showWordCaption = showWordCaptions,
                            cardWidth = cardW,
                            cardHeight = cardH,
                            captionFontSize = captionSp,
                            innerPictureScale = innerPictureScaleForChoice(choice),
                            onClick = {
                                val ok = onAttempt(choice.id)
                                if (ok) {
                                    successChoiceId = choice.id
                                    scope.launch {
                                        scale.snapTo(1f)
                                        scale.animateTo(1.16f, tween(100))
                                        scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 420f))
                                    }
                                }
                            },
                            isCorrectPick = choice.id == successChoiceId,
                        )
                    }
                }
            }
        }
    }
}
