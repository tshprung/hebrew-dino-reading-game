package com.tal.hebrewdino.ui.game

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.runtime.CompositionLocalProvider
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
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.HebrewLetterOrder
import com.tal.hebrewdino.ui.layout.ScreenFit
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.tal.hebrewdino.ui.screens.LetterOptions
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
    /** White readability panel behind instruction (from [StationUiSpec] presentation flags). */
    instructionReadablePanel: Boolean = false,
    /** When true, letter options are sorted for display (e.g. Chapter 3 station 1). */
    sortOptionLetters: Boolean = false,
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
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val useTwoColumn = isCompactLandscapePhone && chapterId == 1 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE
    val instructionScale = if (isCompactLandscapePhone) 1.1f else 2f
    val phoneCardTextMultiplier =
        if (useTwoColumn) {
            promptWordSizeMultiplier * 0.80f
        } else {
            promptWordSizeMultiplier
        }

    if (useTwoColumn) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .offset { IntOffset(shakePx.roundToInt(), 0) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                val orderedLetters = pictureStartsWithOrderedLetters(sortOptionLetters, question.optionLetters)
                val displayLetters =
                    if (pinnedCorrectLetter != null) {
                        listOf(pinnedCorrectLetter)
                    } else {
                        orderedLetters
                    }
                Column(
                    modifier = Modifier.weight(1f, fill = true).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        LetterOptions(
                            options = displayLetters,
                            enabled = enabled,
                            shakePx = 0f,
                            entryPulseEpoch = entryPulseEpoch,
                            hintPulseLetter = hintCorrectLetter,
                            hintPulseEpoch = hintPulseEpoch,
                            correctPulseLetter = correctPulseLetter,
                            correctPulseEpoch = correctPulseEpoch,
                            wrongFlashLetter = wrongFlashLetter,
                            wrongFlashEpoch = wrongFlashEpoch,
                            onPick = onPickLetter,
                        )
                    }
                }
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 270.dp)
                        .offset(x = (-16).dp)
                        .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = instructionText,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * instructionScale,
                            fontWeight = if (chapterId == 6) FontWeight.Black else MaterialTheme.typography.titleMedium.fontWeight,
                        ),
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        if (instructionReadablePanel) {
                            Modifier
                                .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        } else {
                            Modifier
                        },
                )
                Spacer(modifier = Modifier.height(6.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    val rowInnerWidth = maxWidth
                    val frameCap =
                        pictureFrameMaxWidthFraction?.let { f ->
                            (rowInnerWidth * f.coerceIn(0.20f, 1f)).coerceAtLeast(pictureFrameMinWidth)
                        } ?: rowInnerWidth
                    val rowForSizing = rowInnerWidth.coerceAtMost(frameCap)
                    val choiceCount = 2
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
                                fontSize = if (rowInnerWidth < 380.dp) 40.sp else 44.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
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

                        val cardGap = 8.dp
                        var cardW =
                            ScreenFit.rowChildWidthDp(
                                rowInnerWidth = rowForSizing,
                                count = choiceCount,
                                gap = cardGap,
                                minEach = 72.dp,
                                maxEach = 168.dp,
                            )
                        val effectivePictureSizeMultiplier = pictureSizeMultiplier * 0.94f
                        cardW =
                            (cardW * effectivePictureSizeMultiplier).coerceAtMost(
                                (rowForSizing - cardGap * (choiceCount - 1)) / choiceCount,
                            )
                        val cardH = cardW * LessonChoiceCardPictureAspect
                        val captionSp =
                            captionFontSizeForWordCard(
                                density = density,
                                cardWidth = cardW,
                                word = question.word,
                                sizeMultiplier = phoneCardTextMultiplier,
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
                            cardHeight = cardH,
                            captionFontSize = captionSp,
                            innerPictureScale = innerPictureScale,
                            isCorrectPick = false,
                            onClick = { if (pictureTapReplays) onPictureTapReplayWord?.invoke() },
                        )
                    }
                }
            }
            }
        }
    } else {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakePx.roundToInt(), 0) },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = instructionText,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * instructionScale,
                        fontWeight = if (chapterId == 6) FontWeight.Black else MaterialTheme.typography.titleMedium.fontWeight,
                    ),
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                modifier =
                    if (instructionReadablePanel) {
                        Modifier
                            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                            .padding(
                                horizontal = 14.dp,
                                vertical = if (isCompactLandscapePhone) 5.dp else 8.dp,
                            )
                    } else {
                        Modifier
                    },
            )
            Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 6.dp else 12.dp))
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
                        fontSize =
                            if (isCompactLandscapePhone) {
                                if (rowInnerWidth < 380.dp) 40.sp else 44.sp
                            } else {
                                if (rowInnerWidth < 380.dp) 48.sp else 54.sp
                            },
                        modifier = Modifier.padding(bottom = if (isCompactLandscapePhone) 4.dp else 8.dp),
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
            Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 8.dp else 16.dp))
            val orderedLetters = pictureStartsWithOrderedLetters(sortOptionLetters, question.optionLetters)
            val displayLetters =
                if (pinnedCorrectLetter != null) {
                    listOf(pinnedCorrectLetter)
                } else {
                    orderedLetters
                }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                LetterOptions(
                    options = displayLetters,
                    enabled = enabled,
                    shakePx = 0f,
                    entryPulseEpoch = entryPulseEpoch,
                    hintPulseLetter = hintCorrectLetter,
                    hintPulseEpoch = hintPulseEpoch,
                    correctPulseLetter = correctPulseLetter,
                    correctPulseEpoch = correctPulseEpoch,
                    wrongFlashLetter = wrongFlashLetter,
                    wrongFlashEpoch = wrongFlashEpoch,
                    onPick = onPickLetter,
                )
            }
        }
    }
}

internal fun pictureStartsWithOrderedLetters(
    sortOptionLetters: Boolean,
    optionLetters: List<String>,
): List<String> =
    if (sortOptionLetters) {
        HebrewLetterOrder.sortForDisplay(optionLetters)
    } else {
        optionLetters
    }
