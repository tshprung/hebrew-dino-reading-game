package com.tal.hebrewdino.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.layout.ScreenFit
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PictureStartsWithGame(
    question: Question.PictureStartsWithQuestion,
    enabled: Boolean,
    shakePx: Float,
    /** Subtle guidance pulse when the round appears (buttons). */
    entryPulseEpoch: Int = 0,
    pictureImageHeight: Dp = 140.dp,
    /** Scales only the prompt word text (under the picture). */
    promptWordSizeMultiplier: Float = 1f,
    /** Max width for the picture card. If null, uses a reasonable default. */
    pictureFrameMaxWidthFraction: Float? = null,
    /** Minimum width for the picture card (so very small screens don't collapse it). */
    pictureFrameMinWidth: Dp = 200.dp,
    /** Scales picture/emoji inside the frame, not the frame itself. */
    pictureInnerScale: (word: String, tileDrawable: Int) -> Float = { _, _ -> 1f },
    /** After 2 wrong taps, pulse the correct answer (subtle hint). */
    hintCorrectLetter: String? = null,
    hintPulseEpoch: Int = 0,
    /** Pulse the tapped correct letter (tiny positive bounce). */
    correctPulseLetter: String? = null,
    correctPulseEpoch: Int = 0,
    /** Flash the last wrong-picked letter button (stronger feedback). */
    wrongFlashLetter: String? = null,
    wrongFlashEpoch: Int = 0,
    onPickLetter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .offset { IntOffset(shakePx.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "באיזו אות המילה מתחילה?",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * 2),
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val frameMaxW =
                pictureFrameMaxWidthFraction?.let { f ->
                    (maxWidth * f.coerceIn(0.20f, 1f)).coerceAtLeast(pictureFrameMinWidth)
                } ?: 280.dp
            val availableW = maxWidth
            val density = LocalDensity.current
            Column(
                modifier =
                    Modifier
                        .widthIn(max = frameMaxW)
                        .padding(horizontal = 0.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val choice =
                    LessonChoice(
                        id = question.catalogEntryId,
                        letter = question.correctLetter,
                        word = question.word,
                        tintArgb = question.tintArgb,
                        tileDrawable = question.tileDrawable,
                    )

                // Match Station 5 sizing rules (computed width/height + caption size from card width).
                val cardGap = 0.dp
                var cardW =
                    ScreenFit.rowChildWidthDp(
                        rowInnerWidth = availableW.coerceAtMost(frameMaxW),
                        count = 1,
                        gap = cardGap,
                        minEach = 72.dp,
                        maxEach = 168.dp,
                    )
                // Respect authored max width constraints.
                cardW = cardW.coerceAtMost(frameMaxW).coerceAtMost(availableW)
                val cardH = cardW * (110f / 160f)
                val captionSp =
                    with(density) {
                        (cardW.toPx() * 0.22f * promptWordSizeMultiplier).coerceIn(
                            22f * fontScale * promptWordSizeMultiplier,
                            40f * fontScale * promptWordSizeMultiplier,
                        ).toSp()
                    }
                LessonChoiceCard(
                    choice = choice,
                    enabled = false,
                    scale = 1f,
                    showWordCaption = true,
                    cardWidth = cardW,
                    // Use station-5 aspect ratio; this also fixes "too tall" without a custom frame.
                    cardHeight = cardH,
                    captionFontSize = captionSp,
                    innerPictureScale = pictureInnerScale(question.word, question.tileDrawable),
                    isCorrectPick = false,
                    onClick = {},
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            question.optionLetters.forEach { letter ->
                val pop = remember(letter, question) { Animatable(1f) }
                val flash = remember(letter, question) { Animatable(0f) }
                LaunchedEffect(entryPulseEpoch, letter, question) {
                    if (entryPulseEpoch <= 0) return@LaunchedEffect
                    // Very subtle guidance: tiny pulse once per round.
                    pop.animateTo(1.06f, tween(120))
                    pop.animateTo(1f, spring(dampingRatio = 0.70f, stiffness = 420f))
                }
                LaunchedEffect(hintPulseEpoch, hintCorrectLetter, correctPulseEpoch, correctPulseLetter, question) {
                    val shouldPulse =
                        (hintPulseEpoch > 0 && hintCorrectLetter == letter) ||
                            (correctPulseEpoch > 0 && correctPulseLetter == letter)
                    if (!shouldPulse) return@LaunchedEffect
                    pop.snapTo(1f)
                    pop.animateTo(0.90f, tween(70))
                    // Slightly stronger feedback for station 4 (still fast).
                    pop.animateTo(1.28f, tween(120))
                    pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 520f))
                }
                LaunchedEffect(wrongFlashEpoch, wrongFlashLetter, letter, question) {
                    if (wrongFlashEpoch <= 0 || wrongFlashLetter != letter) return@LaunchedEffect
                    flash.snapTo(1f)
                    flash.animateTo(0f, tween(220))
                }
                Button(
                    onClick = { onPickLetter(letter) },
                    enabled = enabled,
                    modifier =
                        Modifier
                            .widthIn(min = 64.dp, max = 84.dp)
                            .scale(pop.value)
                            .border(
                                width = 3.dp,
                                color = Color(0xFFE53935).copy(alpha = 0.55f * flash.value),
                                shape = RoundedCornerShape(14.dp),
                            ),
                ) {
                    Text(text = letter, fontSize = 38.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// Removed local picture-tile renderer in favor of [LessonChoiceCard] to keep picture formatting
// identical across stations 4, 5, and 6.
