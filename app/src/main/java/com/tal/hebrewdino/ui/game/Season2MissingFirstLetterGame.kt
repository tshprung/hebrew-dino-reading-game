package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tal.hebrewdino.ui.domain.HebrewLetterOrder
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.screens.LetterOptions

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Season2MissingFirstLetterGame(
    question: Question.MissingFirstLetterQuestion,
    instructionText: String,
    enabled: Boolean,
    shakePx: Float,
    onPictureTapReplayWord: (() -> Unit)?,
    hintCorrectLetter: String? = null,
    hintPulseEpoch: Int = 0,
    correctPulseLetter: String? = null,
    correctPulseEpoch: Int = 0,
    wrongFlashLetter: String? = null,
    wrongFlashEpoch: Int = 0,
    onPickLetter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val choice =
        LessonChoice(
            id = question.catalogEntryId,
            letter = question.correctLetter,
            word = question.word,
            tintArgb = question.tintArgb,
            tileDrawable = question.tileDrawable,
        )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rowWidth = this.maxWidth
        val cardW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = rowWidth,
                count = 1,
                gap = 0.dp,
                minEach = 120.dp,
                maxEach = 200.dp,
            )
        val cardH = cardW * LessonChoiceCardPictureAspect

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 12.dp,
                        end = 80.dp,
                        top = if (isCompact) 4.dp else 8.dp,
                        bottom = if (isCompact) 4.dp else 8.dp,
                    ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = instructionText,
                fontSize = if (isCompact) 20.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.End,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )

            LessonChoiceCard(
                choice = choice,
                enabled = enabled && onPictureTapReplayWord != null,
                scale = 1f,
                showWordCaption = false,
                cardWidth = cardW,
                cardHeight = cardH,
                captionFontSize = 1.sp,
                innerPictureScale = if (isCompact) 1.4f else 1.2f,
                onClick = { onPictureTapReplayWord?.invoke() },
            )

            Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 16.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = question.partialWord,
                    fontSize = if (isCompact) 44.sp else 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = rowWidth - 104.dp)
                            .padding(bottom = if (isCompact) 10.dp else 16.dp),
                )
            }

            val sortedOptions = HebrewLetterOrder.sortForDisplay(question.optionLetters)
            LetterOptions(
                options = sortedOptions,
                enabled = enabled,
                shakePx = shakePx,
                hintPulseLetter = hintCorrectLetter,
                hintPulseEpoch = hintPulseEpoch,
                correctPulseLetter = correctPulseLetter,
                correctPulseEpoch = correctPulseEpoch,
                wrongFlashLetter = wrongFlashLetter,
                wrongFlashEpoch = wrongFlashEpoch,
                onPick = onPickLetter,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
