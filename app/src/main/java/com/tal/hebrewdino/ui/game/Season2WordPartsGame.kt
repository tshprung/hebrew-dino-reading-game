package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Season2WordPartsGame(
    question: Question.WordPartsQuestion,
    instructionText: String,
    enabled: Boolean,
    shakePx: Float,
    completedEquation: String? = null,
    hintRevealWord: String? = null,
    onPictureTapReplayWord: (() -> Unit)?,
    onPickPart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val mode = question.presentationMode
    val showFullWord =
        completedEquation != null ||
            hintRevealWord != null ||
            mode == Season2WordPartsPresentationMode.VisibleWordParts ||
            mode == Season2WordPartsPresentationMode.GuidedWordParts
    val splitText =
        when {
            completedEquation != null -> completedEquation
            hintRevealWord != null || showFullWord ->
                "${question.firstPart} + ${question.correctPart}"
            else -> "${question.firstPart} + ___"
        }

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
                    minEach = 100.dp,
                    maxEach = if (isCompact) 156.dp else 176.dp,
                )
            val cardH = cardW * LessonChoiceCardPictureAspect

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 12.dp,
                            end = 80.dp,
                            top = if (isCompact) 2.dp else 6.dp,
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
                            .fillMaxWidth()
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
                    innerPictureScale = if (isCompact) 1.22f else 1.08f,
                    onClick = { onPictureTapReplayWord?.invoke() },
                )

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 10.dp))

                if (showFullWord) {
                    Text(
                        text = question.word,
                        fontSize = if (isCompact) 32.sp else 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .background(
                                    Color(0xFFE8F5E9).copy(alpha = 0.92f),
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(horizontal = 20.dp, vertical = if (isCompact) 6.dp else 8.dp),
                    )
                    Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 8.dp))
                }

                Text(
                    text = splitText,
                    fontSize = if (isCompact) 28.sp else 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(
                                Color(0xFFFFF8E1).copy(alpha = if (hintRevealWord != null) 0.95f else 0.88f),
                                RoundedCornerShape(18.dp),
                            )
                            .padding(horizontal = 18.dp, vertical = if (isCompact) 8.dp else 10.dp),
                )

                Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    question.partOptions.forEach { part ->
                        Button(
                            onClick = { if (enabled) onPickPart(part) },
                            enabled = enabled,
                            modifier = Modifier.padding(2.dp),
                        ) {
                            Text(
                                text = part,
                                fontSize = if (isCompact) 26.sp else 30.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
