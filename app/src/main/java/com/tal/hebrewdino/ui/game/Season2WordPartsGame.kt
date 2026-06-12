package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2WordPartsUxPolicy
import com.tal.hebrewdino.ui.domain.Season2WordPartsPresentationMode
import com.tal.hebrewdino.ui.layout.ScreenFit

private const val OptionScale = 1.5f

@Composable
private fun WordPartsSplitOptionRow(
    firstPart: String,
    secondPart: String,
    highlighted: Boolean,
    enabled: Boolean,
    compact: Boolean,
    onRowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg =
        if (highlighted) {
            Color(0xFFFFF8E1).copy(alpha = 0.98f)
        } else {
            Color.White.copy(alpha = 0.94f)
        }
    val hPad = scaledDp(if (compact) 8.dp else 14.dp)
    val vPad = scaledDp(if (compact) 6.dp else 8.dp)
    val gap = scaledDp(if (compact) 4.dp else 8.dp)
    Row(
        modifier =
            modifier
                .clickable(enabled = enabled, onClick = onRowClick)
                .background(bg, RoundedCornerShape(scaledDp(14.dp)))
                .padding(horizontal = hPad, vertical = vPad),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SplitPartLabel(text = firstPart, compact = compact)
        Text(
            text = "+",
            fontSize = scaledSp(if (compact) 20.sp else 26.sp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B2B3D),
        )
        SplitPartLabel(text = secondPart, compact = compact)
    }
}

@Composable
private fun SplitPartLabel(
    text: String,
    compact: Boolean,
) {
    Text(
        text = text,
        fontSize = scaledSp(if (compact) 20.sp else 28.sp),
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0B2B3D),
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .background(Color(0xFFE3F2FD).copy(alpha = 0.95f), RoundedCornerShape(scaledDp(10.dp)))
                .padding(
                    horizontal = scaledDp(if (compact) 8.dp else 12.dp),
                    vertical = scaledDp(if (compact) 4.dp else 6.dp),
                ),
    )
}

@Composable
private fun WordPartsTargetWordBox(
    word: String,
    compact: Boolean,
    minHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Text(
        text = word,
        fontSize = if (compact) 32.sp else 36.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1B5E20),
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .wrapContentWidth(Alignment.Start)
                .heightIn(min = minHeight)
                .background(Color(0xFFE8F5E9).copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 6.dp else 8.dp),
    )
}

@Composable
private fun WordPartsHintEquationPill(
    equationText: String,
    compact: Boolean,
    wrapContent: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = equationText,
        fontSize = scaledSp(if (compact) 13.sp else 15.sp),
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0B2B3D),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            modifier
                .then(
                    if (wrapContent) {
                        Modifier.wrapContentWidth(Alignment.Start)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                )
                .background(Color(0xFFFFF8E1).copy(alpha = 0.96f), RoundedCornerShape(999.dp))
                .padding(
                    horizontal = scaledDp(if (compact) 8.dp else 10.dp),
                    vertical = scaledDp(if (compact) 4.dp else 5.dp),
                ),
    )
}

private fun scaledDp(base: Dp): Dp = (base.value * OptionScale).dp

private fun scaledSp(base: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit =
    (base.value * OptionScale).sp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Season2WordPartsGame(
    question: Question.WordPartsQuestion,
    instructionText: String,
    enabled: Boolean,
    completedEquation: String? = null,
    hintRevealWord: String? = null,
    onPictureTapReplayWord: (() -> Unit)?,
    onHintRevealAudio: (() -> Unit)? = null,
    onPickSplit: (Question.WordPartsSplitOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(hintRevealWord) {
        if (hintRevealWord != null) {
            onHintRevealAudio?.invoke()
        }
    }
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val mode = question.presentationMode
    val isHidden = mode == Season2WordPartsPresentationMode.HiddenWordPartsChallenge
    val isCorrectState = completedEquation != null
    val showFullWord =
        isCorrectState ||
            hintRevealWord != null ||
            mode == Season2WordPartsPresentationMode.VisibleWordParts ||
            mode == Season2WordPartsPresentationMode.GuidedWordParts
    val hintEquationText =
        if (hintRevealWord != null && !isCorrectState) {
            "${question.word} = ${question.firstPart} + ${question.correctPart}"
        } else {
            null
        }
    val correctSplit = Question.WordPartsSplitOption(question.firstPart, question.correctPart)
    val optionsToShow =
        if (isCorrectState) {
            listOf(correctSplit)
        } else {
            question.splitOptions
        }
    val showInfoColumn = showFullWord || hintEquationText != null

    val choice =
        LessonChoice(
            id = question.catalogEntryId,
            letter = question.word.first().toString(),
            word = question.word,
            tintArgb = question.tintArgb,
            tileDrawable = question.tileDrawable,
        )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val contentWidth = maxWidth - 84.dp
            val cardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = contentWidth * 0.52f,
                    count = 1,
                    gap = 0.dp,
                    minEach = 88.dp,
                    maxEach = if (isCompact) 124.dp else 156.dp,
                )
            val cardH = cardW * LessonChoiceCardPictureAspect
            val wordLineMinHeight = if (isCompact) 52.dp else 58.dp
            val hintLineHeight = if (isCompact) 28.dp else 32.dp
            val hintGap = if (isCompact) 4.dp else 6.dp
            val hintReserve =
                if (!isHidden) {
                    hintLineHeight + hintGap
                } else {
                    0.dp
                }
            val infoColumnHeight =
                when {
                    showFullWord -> wordLineMinHeight + hintReserve
                    hintEquationText != null -> hintLineHeight
                    else -> 0.dp
                }
            val heroHeight = maxOf(cardH, infoColumnHeight)
            val optionsGapBelowHero =
                (if (isCompact) 10.dp else 12.dp) + Season2WordPartsUxPolicy.OptionsDownDp

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 12.dp,
                            end = 80.dp,
                            top = if (isCompact) 20.dp else 26.dp,
                            bottom = if (isCompact) 4.dp else 8.dp,
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = instructionText,
                    fontSize = if (isCompact) 17.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .absoluteOffset(
                                x = -Season2WordPartsUxPolicy.InstructionPhysicalLeftDp,
                            )
                            .padding(bottom = if (isCompact) 6.dp else 8.dp)
                            .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = if (isCompact) 5.dp else 7.dp),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(heroHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .absoluteOffset(
                                    x = -Season2WordPartsUxPolicy.ImagePhysicalLeftDp,
                                    y = Season2WordPartsUxPolicy.ImageDownDp,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        LessonChoiceCard(
                            choice = choice,
                            enabled = enabled && onPictureTapReplayWord != null,
                            scale = 1f,
                            showWordCaption = false,
                            cardWidth = cardW,
                            cardHeight = cardH,
                            captionFontSize = 1.sp,
                            innerPictureScale = if (isCompact) 1.18f else 1.06f,
                            onClick = { onPictureTapReplayWord?.invoke() },
                        )
                    }

                    if (showInfoColumn) {
                        Column(
                            modifier =
                                Modifier
                                    .weight(0.48f)
                                    .fillMaxHeight()
                                    .padding(end = if (isCompact) 4.dp else 8.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            if (showFullWord) {
                                WordPartsTargetWordBox(
                                    word = question.word,
                                    compact = isCompact,
                                    minHeight = wordLineMinHeight,
                                    modifier =
                                        Modifier.absoluteOffset(
                                            y = Season2WordPartsUxPolicy.TargetWordDownDp,
                                        ),
                                )
                            }
                            if (hintEquationText != null) {
                                if (showFullWord) {
                                    Spacer(modifier = Modifier.height(hintGap))
                                }
                                WordPartsHintEquationPill(
                                    equationText = hintEquationText,
                                    compact = isCompact,
                                    wrapContent = true,
                                    modifier =
                                        Modifier.absoluteOffset(
                                            x = -Season2WordPartsUxPolicy.HintPhysicalLeftDp,
                                        ),
                                )
                            } else if (showFullWord && hintReserve > 0.dp) {
                                Spacer(modifier = Modifier.height(hintReserve))
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(0.48f))
                    }
                }

                Spacer(modifier = Modifier.height(optionsGapBelowHero))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .absoluteOffset(
                                x =
                                    -Season2WordPartsUxPolicy.OptionsPhysicalLeftDp -
                                        if (hintEquationText != null) {
                                            Season2WordPartsUxPolicy.HintOptionsExtraPhysicalLeftDp
                                        } else {
                                            0.dp
                                        },
                            ),
                    horizontalArrangement = Arrangement.spacedBy(scaledDp(if (isCompact) 6.dp else 8.dp), Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    optionsToShow.forEach { option ->
                        val isCorrectSplit =
                            option.firstPart == correctSplit.firstPart &&
                                option.secondPart == correctSplit.secondPart
                        val highlightOption = hintRevealWord != null && isCorrectSplit && !isCorrectState
                        WordPartsSplitOptionRow(
                            firstPart = option.firstPart,
                            secondPart = option.secondPart,
                            highlighted = highlightOption,
                            enabled = enabled && !isCorrectState,
                            compact = isCompact,
                            onRowClick = { onPickSplit(option) },
                            modifier =
                                Modifier
                                    .widthIn(max = contentWidth / 3.2f)
                                    .background(
                                        Color(0xFFE8F5E9).copy(alpha = 0.92f),
                                        if (isCompact) RoundedCornerShape(scaledDp(14.dp)) else RoundedCornerShape(scaledDp(16.dp)),
                                    ),
                        )
                    }
                }
            }
        }
    }
}
