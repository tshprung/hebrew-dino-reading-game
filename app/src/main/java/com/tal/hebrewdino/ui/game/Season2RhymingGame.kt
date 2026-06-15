package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.layout.ScreenFit

/** Physical-left nudge so rhyming cards sit clear of the bottom-right companion. */
private val ContentPhysicalLeftDp = 34.dp
private val DinoSidePaddingDp = 100.dp
private val OppositeSidePaddingDp = 40.dp

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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val gap = if (isCompact) 8.dp else 10.dp
            val contentWidth = maxWidth - DinoSidePaddingDp - OppositeSidePaddingDp
            val optionsRowWidth = (contentWidth - ContentPhysicalLeftDp).coerceAtLeast(200.dp)

            val targetCardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = contentWidth * 0.40f,
                    count = 1,
                    gap = 0.dp,
                    minEach = 84.dp,
                    maxEach = if (isCompact) 112.dp else 132.dp,
                )
            val choiceCardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = optionsRowWidth,
                    count = 3,
                    gap = gap,
                    minEach = 68.dp,
                    maxEach = if (isCompact) 108.dp else 124.dp,
                )
            val targetCaptionSp =
                captionFontSizeForWordCard(
                    density = density,
                    cardWidth = targetCardW,
                    sizeMultiplier = if (isCompact) 1.12f else 1.22f,
                )
            val choiceCaptionSp =
                captionFontSizeForWordCard(
                    density = density,
                    cardWidth = choiceCardW,
                    sizeMultiplier = if (isCompact) 1.14f else 1.20f,
                )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = DinoSidePaddingDp,
                            end = OppositeSidePaddingDp,
                            top = if (isCompact) 6.dp else 10.dp,
                            bottom = if (isCompact) 4.dp else 8.dp,
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = instructionText,
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .absoluteOffset(x = -ContentPhysicalLeftDp / 2)
                            .padding(bottom = if (isCompact) 6.dp else 8.dp)
                            .background(Color.White.copy(alpha = 0.84f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = if (isCompact) 6.dp else 8.dp),
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .absoluteOffset(x = -ContentPhysicalLeftDp / 2),
                    contentAlignment = Alignment.Center,
                ) {
                    LessonChoiceCard(
                        choice = targetChoice,
                        enabled = enabled && onTargetTapReplayWord != null,
                        scale = 1f,
                        showWordCaption = true,
                        cardWidth = targetCardW,
                        cardHeight = targetCardW * LessonChoiceCardPictureAspect,
                        captionFontSize = targetCaptionSp,
                        innerPictureScale = if (isCompact) 1.05f else 0.98f,
                        onClick = { onTargetTapReplayWord?.invoke() },
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 10.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .absoluteOffset(
                                x = -ContentPhysicalLeftDp,
                                y = if (isCompact) (-6).dp else (-8).dp,
                            ),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top,
                ) {
                    question.choices.forEach { choice ->
                        LessonChoiceCard(
                            choice = choice,
                            enabled = enabled,
                            scale = 1f,
                            showWordCaption = true,
                            cardWidth = choiceCardW,
                            cardHeight = choiceCardW * LessonChoiceCardPictureAspect,
                            captionFontSize = choiceCaptionSp,
                            innerPictureScale = if (isCompact) 1.05f else 1f,
                            onClick = { onPickChoice(choice.id) },
                        )
                    }
                }
            }
        }
    }
}
