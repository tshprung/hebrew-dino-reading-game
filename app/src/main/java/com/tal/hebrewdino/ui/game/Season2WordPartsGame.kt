package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2WordPartsPresentationMode
import com.tal.hebrewdino.ui.layout.ScreenFit

@Composable
private fun WordPartsSplitOptionRow(
    firstPart: String,
    secondPart: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val bg =
        if (highlighted) {
            Color(0xFFFFF8E1).copy(alpha = 0.98f)
        } else {
            Color.White.copy(alpha = 0.94f)
        }
    Row(
        modifier =
            modifier
                .background(bg, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SplitPartChip(firstPart)
        Text(
            text = "+",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B2B3D),
        )
        SplitPartChip(secondPart)
    }
}

@Composable
private fun SplitPartChip(text: String) {
    Text(
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0B2B3D),
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .background(Color(0xFFE3F2FD).copy(alpha = 0.95f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Season2WordPartsGame(
    question: Question.WordPartsQuestion,
    instructionText: String,
    enabled: Boolean,
    shakePx: Float,
    completedEquation: String? = null,
    hintRevealWord: String? = null,
    onPictureTapReplayWord: (() -> Unit)?,
    onPickSplit: (Question.WordPartsSplitOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val mode = question.presentationMode
    val isHidden = mode == Season2WordPartsPresentationMode.HiddenWordPartsChallenge
    val showFullWord =
        completedEquation != null ||
            hintRevealWord != null ||
            mode == Season2WordPartsPresentationMode.VisibleWordParts ||
            mode == Season2WordPartsPresentationMode.GuidedWordParts
    val showHintEquation = hintRevealWord != null || completedEquation != null
    val correctSplit = Question.WordPartsSplitOption(question.firstPart, question.correctPart)

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
                    rowInnerWidth = contentWidth,
                    count = 1,
                    gap = 0.dp,
                    minEach = 88.dp,
                    maxEach = if (isCompact) 132.dp else 148.dp,
                )
            val cardH = cardW * LessonChoiceCardPictureAspect

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
                            .padding(bottom = if (isCompact) 6.dp else 8.dp)
                            .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = if (isCompact) 5.dp else 7.dp),
                )

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

                Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 8.dp))

                if (showFullWord) {
                    Text(
                        text = question.word,
                        fontSize = if (isCompact) 30.sp else 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .background(
                                    Color(0xFFE8F5E9).copy(alpha = 0.92f),
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(horizontal = 18.dp, vertical = if (isCompact) 5.dp else 7.dp),
                    )
                    Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))
                }

                if (showHintEquation) {
                    val equationText =
                        completedEquation
                            ?: "${question.word} = ${question.firstPart} + ${question.correctPart}"
                    Text(
                        text = equationText,
                        fontSize = if (isCompact) 24.sp else 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .background(
                                    Color(0xFFFFF8E1).copy(alpha = 0.95f),
                                    RoundedCornerShape(18.dp),
                                )
                                .padding(horizontal = 16.dp, vertical = if (isCompact) 6.dp else 8.dp),
                    )
                    Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 8.dp))
                } else if (!isHidden) {
                    Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp),
                ) {
                    question.splitOptions.forEach { option ->
                        val isCorrectSplit =
                            option.firstPart == correctSplit.firstPart &&
                                option.secondPart == correctSplit.secondPart
                        val highlightOption = hintRevealWord != null && isCorrectSplit
                        Button(
                            onClick = { if (enabled) onPickSplit(option) },
                            enabled = enabled,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE8F5E9).copy(alpha = 0.92f),
                                    contentColor = Color(0xFF0B2B3D),
                                ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                        ) {
                            WordPartsSplitOptionRow(
                                firstPart = option.firstPart,
                                secondPart = option.secondPart,
                                highlighted = highlightOption,
                            )
                        }
                    }
                }
            }
        }
    }
}
