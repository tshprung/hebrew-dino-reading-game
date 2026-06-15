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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.layout.ScreenFit

/** Physical mm → dp at 160dpi (standard Android density). */
private fun physicalMmToDp(mm: Float): Dp = (mm * 160f / 25.4f).dp

/** 2mm below the top chrome / progress area. */
private val InstructionTopGap = physicalMmToDp(2f)

/** 1mm between the target card row and the choice row. */
private val BetweenRowsGap = physicalMmToDp(1f)

/** 1cm between choice cards, reduced by 30% to bring them closer. */
private val BottomRowChoiceGap = physicalMmToDp(10f) * 0.7f

private val ContentPhysicalLeftDp = 34.dp
private val DinoSidePaddingDp = 100.dp
private val OppositeSidePaddingDp = 40.dp

/**
 * Single source of truth for rhyming-station card geometry.
 * All cards (target + choices) share identical picture and caption sizing.
 */
private object RhymingCardLayout {
    val contentPadding = 4.dp
    val captionSpacer = 0.dp

    fun captionFontSize(
        density: Density,
        cardWidth: Dp,
    ): TextUnit =
        with(density) {
            (cardWidth.toPx() * 0.20f).coerceIn(
                16f * fontScale,
                28f * fontScale,
            ).sp
        }

    /** Fixed caption band — tall enough for Hebrew descenders (ק/ך/ץ/ן/ף). */
    fun captionAreaHeight(
        density: Density,
        captionSp: TextUnit,
    ): Dp =
        with(density) {
            (captionSp.toPx() * 1.55f).toDp().coerceIn(28.dp, 40.dp)
        }

    fun outerHeight(
        pictureWidth: Dp,
        captionBand: Dp,
    ): Dp =
        pictureWidth * LessonChoiceCardPictureAspect +
            captionSpacer +
            captionBand +
            contentPadding * 2

    data class Sizes(
        val cardWidth: Dp,
        val cardHeight: Dp,
        val captionSp: TextUnit,
        val captionBand: Dp,
    )

    fun computeSizes(
        optionsRowWidth: Dp,
        cardsVerticalBudget: Dp,
        bottomRowGap: Dp,
        betweenRowsGap: Dp,
        isCompact: Boolean,
        density: Density,
    ): Sizes {
        val cardMin = if (isCompact) 68.dp else 76.dp

        var cardW = ((optionsRowWidth - bottomRowGap * 2) / 3f).coerceAtLeast(cardMin)

        fun cardBlockHeight(width: Dp): Dp {
            val captionSp = captionFontSize(density, width)
            return outerHeight(width, captionAreaHeight(density, captionSp))
        }

        while (cardW > cardMin && cardBlockHeight(cardW) * 2 + betweenRowsGap > cardsVerticalBudget) {
            cardW = (cardW - 1.dp).coerceAtLeast(cardMin)
        }

        val captionSp = captionFontSize(density, cardW)
        val captionBand = captionAreaHeight(density, captionSp)
        return Sizes(
            cardWidth = cardW,
            cardHeight = cardW * LessonChoiceCardPictureAspect,
            captionSp = captionSp,
            captionBand = captionBand,
        )
    }
}

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
    val shortSide = ScreenFit.shortSideDp()
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
            val contentWidth = maxWidth - DinoSidePaddingDp - OppositeSidePaddingDp
            val optionsRowWidth = (contentWidth - ContentPhysicalLeftDp).coerceAtLeast(180.dp)
            val usableHeight =
                ScreenFit.effectiveMaxHeight(
                    hasBoundedHeight = true,
                    boxMaxHeight = maxHeight,
                    shortSideDp = shortSide,
                )
            val instructionBlockH = if (isCompact) 34.dp else 38.dp
            val cardsVerticalBudget =
                (usableHeight - instructionBlockH - InstructionTopGap).coerceAtLeast(120.dp)
            val sizes =
                RhymingCardLayout.computeSizes(
                    optionsRowWidth = optionsRowWidth,
                    cardsVerticalBudget = cardsVerticalBudget,
                    bottomRowGap = BottomRowChoiceGap,
                    betweenRowsGap = BetweenRowsGap,
                    isCompact = isCompact,
                    density = density,
                )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = DinoSidePaddingDp,
                            end = OppositeSidePaddingDp,
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = InstructionTopGap)
                            .absoluteOffset(x = -ContentPhysicalLeftDp / 2),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = instructionText,
                        fontSize = if (isCompact) 17.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .background(Color.White.copy(alpha = 0.90f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = if (isCompact) 4.dp else 6.dp),
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .absoluteOffset(x = -ContentPhysicalLeftDp / 2),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    RhymingChoiceCard(
                        choice = targetChoice,
                        enabled = enabled && onTargetTapReplayWord != null,
                        cardWidth = sizes.cardWidth,
                        cardHeight = sizes.cardHeight,
                        captionFontSize = sizes.captionSp,
                        captionAreaHeight = sizes.captionBand,
                        onClick = { onTargetTapReplayWord?.invoke() },
                    )

                    Spacer(modifier = Modifier.height(BetweenRowsGap))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(BottomRowChoiceGap, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Top,
                    ) {
                        question.choices.forEach { choice ->
                            RhymingChoiceCard(
                                choice = choice,
                                enabled = enabled,
                                cardWidth = sizes.cardWidth,
                                cardHeight = sizes.cardHeight,
                                captionFontSize = sizes.captionSp,
                                captionAreaHeight = sizes.captionBand,
                                onClick = { onPickChoice(choice.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RhymingChoiceCard(
    choice: LessonChoice,
    enabled: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    captionFontSize: TextUnit,
    captionAreaHeight: Dp,
    onClick: () -> Unit,
) {
    LessonChoiceCard(
        choice = choice,
        enabled = enabled,
        scale = 1f,
        showWordCaption = true,
        cardWidth = cardWidth,
        cardHeight = cardHeight,
        captionFontSize = captionFontSize,
        captionSpacerHeight = RhymingCardLayout.captionSpacer,
        captionAreaHeight = captionAreaHeight,
        captionWrapContent = false,
        pictureCaptionScale = 1f,
        contentPadding = RhymingCardLayout.contentPadding,
        innerPictureScale = 1f,
        onClick = onClick,
    )
}
