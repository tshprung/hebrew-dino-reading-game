package com.tal.hebrewdino.ui.game

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
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
    /** Scales only the prompt word text (under the picture). */
    promptWordSizeMultiplier: Float = 1f,
    /** Same as [ImageMatchGame.pictureSizeMultiplier]: scales each card width after [ScreenFit.rowChildWidthDp]. */
    pictureSizeMultiplier: Float = 1f,
    /** Max width for the picture card row. If null, uses full [maxWidth] like station 5 (three-card row math). */
    pictureFrameMaxWidthFraction: Float? = null,
    /** Minimum width when [pictureFrameMaxWidthFraction] caps the row (legacy / narrow layouts). */
    pictureFrameMinWidth: Dp = 200.dp,
    /** Scales picture/emoji inside the frame, not the frame itself (same knob as [ImageMatchGame.innerPictureScaleForChoice]). */
    innerPictureScale: Float = 1f,
    /** After 2 wrong taps, pulse the correct answer (subtle hint). */
    hintCorrectLetter: String? = null,
    hintPulseEpoch: Int = 0,
    /** Pulse the tapped correct letter (tiny positive bounce). */
    correctPulseLetter: String? = null,
    correctPulseEpoch: Int = 0,
    /** Flash the last wrong-picked letter button (stronger feedback). */
    wrongFlashLetter: String? = null,
    wrongFlashEpoch: Int = 0,
    /** Optional saga context for [captionFontSizeForWordCard]. */
    chapterId: Int? = null,
    stationId: Int? = null,
    /** Hebrew prompt above the picture (Episode 3 wording tweak allowed). */
    instructionText: String = "באיזו אות המילה מתחילה?",
    /** Episode 4 station 4: white readability panel behind instruction (same treatment as Episode 3). */
    instructionReadablePanel: Boolean = false,
    /** When false, picture card shows image only (episode 4–5 listen-first station 4). */
    showWordCaption: Boolean = true,
    /** Episode 4 station 4: tapping the picture replays the word audio (same as help "שוב"). */
    onPictureTapReplayWord: (() -> Unit)? = null,
    /** Episode 4 help רמז: briefly show the word’s starting letter above the picture. */
    temporaryStartingLetterHint: String? = null,
    /** Episode 4 station 4: after correct pick, hide other letter buttons for clarity. */
    pinnedCorrectLetter: String? = null,
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
            text = instructionText,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * 2),
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
            modifier =
                if (chapterId == 3 || instructionReadablePanel) {
                    Modifier
                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                } else {
                    Modifier
                },
        )
        Spacer(modifier = Modifier.height(12.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            val rowInnerWidth = maxWidth
            val frameCap =
                pictureFrameMaxWidthFraction?.let { f ->
                    (rowInnerWidth * f.coerceIn(0.20f, 1f)).coerceAtLeast(pictureFrameMinWidth)
                } ?: rowInnerWidth
            val rowForSizing = rowInnerWidth.coerceAtMost(frameCap)
            val choiceCount = 3
            val density = LocalDensity.current
            Column(
                modifier =
                    Modifier
                        .then(
                            if (pictureFrameMaxWidthFraction != null) {
                                Modifier.widthIn(max = frameCap)
                            } else {
                                Modifier.fillMaxWidth()
                            },
                        )
                        .padding(horizontal = 0.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (temporaryStartingLetterHint != null) {
                    TargetLetterHeaderChip(
                        letter = temporaryStartingLetterHint,
                        fontSize = if (rowInnerWidth < 380.dp) 48.sp else 54.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                val choice =
                    LessonChoice(
                        id = question.catalogEntryId,
                        letter = question.correctLetter,
                        word = question.word,
                        tintArgb = question.tintArgb,
                        tileDrawable = question.tileDrawable,
                    )

                // Same width math as [ImageMatchGame] for three choices (one visible card, same outer size as st 5/6).
                val cardGap = 10.dp
                var cardW =
                    ScreenFit.rowChildWidthDp(
                        rowInnerWidth = rowForSizing,
                        count = choiceCount,
                        gap = cardGap,
                        minEach = 72.dp,
                        maxEach = 168.dp,
                    )
                cardW =
                    (cardW * pictureSizeMultiplier).coerceAtMost(
                        (rowForSizing - cardGap * (choiceCount - 1)) / choiceCount,
                    )
                val cardH = cardW * LessonChoiceCardPictureAspect
                val captionSp =
                    captionFontSizeForWordCard(
                        density = density,
                        cardWidth = cardW,
                        word = question.word,
                        sizeMultiplier = promptWordSizeMultiplier,
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                val pictureTapReplays = onPictureTapReplayWord != null
                LessonChoiceCard(
                    choice = choice,
                    enabled = enabled && pictureTapReplays,
                    scale = 1f,
                    showWordCaption = showWordCaption,
                    cardWidth = cardW,
                    // Use station-5 aspect ratio; this also fixes "too tall" without a custom frame.
                    cardHeight = cardH,
                    captionFontSize = captionSp,
                    innerPictureScale = innerPictureScale,
                    isCorrectPick = false,
                    onClick = { if (pictureTapReplays) onPictureTapReplayWord?.invoke() },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val orderedLetters =
            if (chapterId == 3 && stationId == 1) {
                question.optionLetters.sorted()
            } else {
                question.optionLetters
            }
        val displayLetters =
            if (pinnedCorrectLetter != null) {
                listOf(pinnedCorrectLetter)
            } else {
                orderedLetters
            }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            displayLetters.forEach { letter ->
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
