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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.layout.ScreenFit

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Season2RhymingGame(
    question: Question.RhymingQuestion,
    instructionText: String,
    enabled: Boolean,
    onTargetTapReplayWord: (() -> Unit)?,
    onPickChoice: (choiceId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val density = LocalDensity.current
    val targetChoice =
        LessonChoice(
            id = question.targetCatalogEntryId,
            letter = question.targetWord.first().toString(),
            word = question.targetWord,
            tintArgb = question.targetTintArgb,
            tileDrawable = question.targetTileDrawable,
        )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gap = 10.dp
        val targetCardW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = maxWidth,
                count = 1,
                gap = gap,
                minEach = 100.dp,
                maxEach = 180.dp,
            )
        val choiceCardW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = maxWidth,
                count = 3,
                gap = gap,
                minEach = 72.dp,
                maxEach = 140.dp,
            )

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
                choice = targetChoice,
                enabled = enabled && onTargetTapReplayWord != null,
                scale = 1f,
                showWordCaption = false,
                cardWidth = targetCardW,
                cardHeight = targetCardW * LessonChoiceCardPictureAspect,
                captionFontSize =
                    captionFontSizeForWordCard(
                        density = density,
                        cardWidth = targetCardW,
                        sizeMultiplier = if (isCompact) 1.1f else 1.2f,
                    ),
                innerPictureScale = if (isCompact) 1.3f else 1.1f,
                onClick = { onTargetTapReplayWord?.invoke() },
            )

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                question.choices.forEach { choice ->
                    LessonChoiceCard(
                        choice = choice,
                        enabled = enabled,
                        scale = 1f,
                        showWordCaption = false,
                        cardWidth = choiceCardW,
                        cardHeight = choiceCardW * LessonChoiceCardPictureAspect,
                        captionFontSize = 1.sp,
                        innerPictureScale = if (isCompact) 1.2f else 1f,
                        onClick = { onPickChoice(choice.id) },
                    )
                }
            }
        }
    }
}
