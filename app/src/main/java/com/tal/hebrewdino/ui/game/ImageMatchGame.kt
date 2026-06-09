package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardCaptionAreaHeight
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardCaptionSpacerHeight
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Chapter1Station4To6LessonChoiceCardSpec
import com.tal.hebrewdino.ui.domain.Season2StationUx
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageMatchGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    /** Subtle guidance pulse when the round appears (cards). */
    entryPulseEpoch: Int = 0,
    /** After 2 wrong taps, pulse the correct answer (subtle hint). */
    hintCorrectChoiceId: String? = null,
    hintPulseEpoch: Int = 0,
    onAttempt: (String) -> Boolean,
    /** Optional: play word audio when a choice card is tapped (Season 2 which-word finale). */
    onChoiceWordPreview: ((String) -> Unit)? = null,
    /** When false, cards show only the picture (for pre-readers). */
    showWordCaptions: Boolean = true,
    captionSizeMultiplier: Float = 1f,
    pictureSizeMultiplier: Float = 1f,
    /** Scales only the illustration inside the card frame (card outer size unchanged). */
    innerPictureScaleForChoice: (LessonChoice) -> Float = { 1f },
    /** Optional saga context for [captionFontSizeForWordCard] (chapter/station tweaks). */
    chapterId: Int? = null,
    stationId: Int? = null,
    /** When set, shown above the target-letter chip (Episode 3 “which picture is this word?”). */
    headerInstructionText: String? = null,
    /** Scales the header instruction text (Episode 1/2 station 5 sizing tweaks). */
    headerInstructionFontScale: Float = 1.35f,
    /** Optional big, prominent word prompt (Episode 3 station 5). */
    headerPromptWord: String? = null,
    /** When false, hides the large letter chip (word-only header). */
    showTargetLetterChip: Boolean = true,
    /** Listen-first help: show this letter in the header chip briefly (e.g. Episode 4 station 5 רמז). */
    listenOnlyTemporaryHintLetter: String? = null,
    /** Adjusts the letter chip vertical placement (Episode 1 station 5). */
    targetLetterChipOffsetYDp: Int = -10,
    /** Adds extra top padding before header (Episode 1 station 5). */
    headerTopPaddingDp: Int = 0,
    /** Episode 4 station 5: white readability panel behind header instruction (matches Episode 3). */
    readableInstructionHeaderPanel: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var successChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableStateOf(0) }
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
    val isSeason2WhichWordFinale =
        chapterId != null &&
            stationId != null &&
            Season2StationUx.isWhichWordStartsWithStation(chapterId, stationId)
    val isCompactLandscapePhoneSixStationArcStation5 =
        isCompactLandscapePhone &&
            (
                (
                    (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
                        stationId == Chapter1StationOrder.PICTURE_PICK_ALL
                ) || isSeason2WhichWordFinale
            )
    isCompactLandscapePhone &&
        chapterId == 1 &&
        stationId == Chapter1StationOrder.PICTURE_PICK_ALL
    LaunchedEffect(contentKey) {
        successChoiceId = null
        wrongFlashChoiceId = null
        wrongFlashEpoch = 0
    }
    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .then(
                        if (isCompactLandscapePhoneSixStationArcStation5) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.fillMaxWidth()
                        },
                    )
                    .align(Alignment.TopCenter),
        ) {
            val rowInnerWidth = maxWidth
            val choiceCount = question.choices.size.coerceAtLeast(1)
            val cardGap = if (isCompactLandscapePhoneSixStationArcStation5) 8.dp else 10.dp
            var cardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = rowInnerWidth,
                    count = choiceCount,
                    gap = cardGap,
                    minEach = 72.dp,
                    maxEach = 168.dp,
                )
            val effectivePictureSizeMultiplier =
                if (isCompactLandscapePhoneSixStationArcStation5) {
                    pictureSizeMultiplier * 0.88f
                } else {
                    pictureSizeMultiplier
                }
            cardW =
                (cardW * effectivePictureSizeMultiplier).coerceAtMost(
                    (rowInnerWidth - cardGap * (choiceCount - 1)) / choiceCount,
                )
            val cardH = cardW * LessonChoiceCardPictureAspect
            val density = LocalDensity.current
            val narrowRow = rowInnerWidth < 380.dp
            if (isCompactLandscapePhoneSixStationArcStation5) {
                val columnGap = 10.dp
                val sidePanelW =
                    if (rowInnerWidth < 480.dp) {
                        170.dp
                    } else {
                        200.dp
                    }
                val cardsAreaW = (rowInnerWidth - sidePanelW - columnGap).coerceAtLeast(240.dp)
                val cardGapTwo = 8.dp
                var cardWTwo =
                    ScreenFit.rowChildWidthDp(
                        rowInnerWidth = cardsAreaW,
                        count = choiceCount,
                        gap = cardGapTwo,
                        minEach = 68.dp,
                        maxEach = 150.dp,
                    )
                cardWTwo =
                    (cardWTwo * (pictureSizeMultiplier * 0.90f)).coerceAtMost(
                        (cardsAreaW - cardGapTwo * (choiceCount - 1)) / choiceCount,
                    )
                val cardsAreaVerticalPadding = 6.dp
                val perCardExtraHeight =
                    24.dp +
                        if (showWordCaptions) {
                            LessonChoiceCardCaptionSpacerHeight + LessonChoiceCardCaptionAreaHeight
                        } else {
                            0.dp
                        }
                val maxPictureHeight =
                    (maxHeight - cardsAreaVerticalPadding * 2 - perCardExtraHeight)
                        .coerceAtLeast(60.dp)
                maxPictureHeight / LessonChoiceCardPictureAspect
                val sharedCardSize =
                    Chapter1Station4To6LessonChoiceCardSpec.station5And6CardSize(
                        maxWidth = rowInnerWidth,
                        maxHeight = maxHeight,
                        choiceCount = choiceCount,
                        pictureSizeMultiplier = pictureSizeMultiplier,
                        showWordCaption = showWordCaptions,
                    )
                val effectiveCardWTwo =
                    sharedCardSize.width
                val cardHTwo = sharedCardSize.height

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(top = 0.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(columnGap),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .weight(1f, fill = true)
                                    .fillMaxHeight()
                                    .padding(top = cardsAreaVerticalPadding)
                                    .offset { IntOffset(shakePx.roundToInt(), 0) },
                            horizontalArrangement = Arrangement.spacedBy(cardGapTwo, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.Top,
                        ) {
                            question.choices.forEach { choice ->
                                val scale = remember(choice.id, contentKey) { Animatable(1f) }
                                val flash = remember(choice.id, contentKey) { Animatable(0f) }
                                LaunchedEffect(entryPulseEpoch, choice.id, contentKey) {
                                    if (entryPulseEpoch <= 0) return@LaunchedEffect
                                    scale.animateTo(1.05f, tween(120))
                                    scale.animateTo(1f, spring(dampingRatio = 0.70f, stiffness = 420f))
                                }
                                LaunchedEffect(hintPulseEpoch, hintCorrectChoiceId, choice.id, contentKey) {
                                    if (hintPulseEpoch <= 0 || hintCorrectChoiceId != choice.id) return@LaunchedEffect
                                    scale.snapTo(1f)
                                    scale.animateTo(1.22f, tween(120))
                                    scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
                                }
                                LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, choice.id, contentKey) {
                                    if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != choice.id) return@LaunchedEffect
                                    flash.snapTo(1f)
                                    flash.animateTo(0f, tween(220))
                                }
                                val captionSp =
                                    captionFontSizeForWordCard(
                                        density = density,
                                        cardWidth = effectiveCardWTwo,
                                        word = choice.word,
                                        sizeMultiplier =
                                            captionSizeMultiplier *
                                                0.92f *
                                                if (chapterId == 2 && choice.word == "היפופוטם") {
                                                    0.95f
                                                } else {
                                                    1f
                                                },
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                val innerScale = innerPictureScaleForChoice(choice)
                                Chapter1Station4To6LessonChoiceCardSpec.Card(
                                    choice = choice,
                                    enabled = enabled,
                                    scale = scale.value,
                                    showWordCaption = showWordCaptions,
                                    cardWidth = effectiveCardWTwo,
                                    cardHeight = cardHTwo,
                                    captionFontSize = captionSp,
                                    innerPictureScale = innerScale,
                                    onClick = {
                                        onChoiceWordPreview?.invoke(choice.id)
                                        val ok = onAttempt(choice.id)
                                        if (ok) {
                                            successChoiceId = choice.id
                                            scope.launch {
                                                scale.snapTo(1f)
                                                scale.animateTo(1.28f, tween(100))
                                                scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 420f))
                                            }
                                        } else {
                                            wrongFlashChoiceId = choice.id
                                            wrongFlashEpoch += 1
                                            scope.launch {
                                                flash.snapTo(1f)
                                                flash.animateTo(0f, tween(220))
                                            }
                                        }
                                    },
                                    isCorrectPick = choice.id == successChoiceId,
                                    wrongFlashAlpha = flash.value,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.width(sidePanelW).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                        ) {
                        val compactHeaderExtraOffsetY = (headerTopPaddingDp - 10).dp
                        if (headerInstructionText != null) {
                            val effectiveFontSize =
                                if (rowInnerWidth < 480.dp) 24.sp else 28.sp
                            Text(
                                text = headerInstructionText,
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontSize = effectiveFontSize,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = (effectiveFontSize.value * 1.10f).sp,
                                    ),
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .then(
                                            Modifier.offset(y = (-18).dp + compactHeaderExtraOffsetY),
                                        )
                                        .padding(horizontal = 6.dp)
                                        .then(
                                            if (readableInstructionHeaderPanel) {
                                                Modifier
                                                    .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            } else {
                                                Modifier
                                            },
                                        ),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (headerPromptWord != null) {
                            Text(
                                text = headerPromptWord,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .background(Color(0xFFFFF59D).copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (showTargetLetterChip) {
                            TargetLetterHeaderChip(
                                letter = question.targetLetter,
                                fontSize = 46.sp,
                                modifier = Modifier.offset(y = targetLetterChipOffsetYDp.dp + compactHeaderExtraOffsetY),
                            )
                        } else if (listenOnlyTemporaryHintLetter != null) {
                            TargetLetterHeaderChip(
                                letter = listenOnlyTemporaryHintLetter,
                                fontSize = 46.sp,
                                modifier = Modifier.offset(y = targetLetterChipOffsetYDp.dp + compactHeaderExtraOffsetY),
                            )
                        }
                    }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = headerTopPaddingDp.dp),
                ) {
                    if (headerInstructionText != null) {
                        val effectiveHeaderScale =
                            headerInstructionFontScale
                        Text(
                            text = headerInstructionText,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize * effectiveHeaderScale,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                                ),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .then(
                                        if (readableInstructionHeaderPanel) {
                                            Modifier
                                                .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                                .padding(
                                                    horizontal = 14.dp,
                                                    vertical = 8.dp,
                                                )
                                        } else {
                                            Modifier
                                        },
                                    ),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (headerPromptWord != null) {
                        Text(
                            text = headerPromptWord,
                            fontSize =
                                if (rowInnerWidth < 420.dp) 52.sp else 60.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier
                                    .background(Color(0xFFFFF59D).copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
                                    .padding(horizontal = 18.dp, vertical = 6.dp),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (showTargetLetterChip) {
                        // Station 5 (all chapters): match station 3 target-letter chip style, and sit a bit lower.
                        TargetLetterHeaderChip(
                            letter = question.targetLetter,
                            fontSize =
                                if (rowInnerWidth < 420.dp) 52.sp else 56.sp,
                            modifier =
                                Modifier
                                    .padding(top = 2.dp)
                                    .offset(y = targetLetterChipOffsetYDp.dp),
                        )
                    } else if (listenOnlyTemporaryHintLetter != null) {
                        TargetLetterHeaderChip(
                            letter = listenOnlyTemporaryHintLetter,
                            fontSize =
                                if (rowInnerWidth < 420.dp) 52.sp else 56.sp,
                            modifier =
                                Modifier
                                    .padding(top = 2.dp)
                                    .offset(y = targetLetterChipOffsetYDp.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(if (narrowRow) 16.dp else 20.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(shakePx.roundToInt(), 0) },
                        horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                    ) {
                        question.choices.forEach { choice ->
                            val scale = remember(choice.id, contentKey) { Animatable(1f) }
                            val flash = remember(choice.id, contentKey) { Animatable(0f) }
                            LaunchedEffect(entryPulseEpoch, choice.id, contentKey) {
                                if (entryPulseEpoch <= 0) return@LaunchedEffect
                                // Very subtle guidance: tiny pulse once per round.
                                scale.animateTo(1.05f, tween(120))
                                scale.animateTo(1f, spring(dampingRatio = 0.70f, stiffness = 420f))
                            }
                            LaunchedEffect(hintPulseEpoch, hintCorrectChoiceId, choice.id, contentKey) {
                                if (hintPulseEpoch <= 0 || hintCorrectChoiceId != choice.id) return@LaunchedEffect
                                scale.snapTo(1f)
                                scale.animateTo(1.22f, tween(120))
                                scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
                            }
                            LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, choice.id, contentKey) {
                                if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != choice.id) return@LaunchedEffect
                                flash.snapTo(1f)
                                flash.animateTo(0f, tween(220))
                            }
                            val captionSp =
                                captionFontSizeForWordCard(
                                    density = density,
                                    cardWidth = cardW,
                                    word = choice.word,
                                    sizeMultiplier =
                                        captionSizeMultiplier *
                                            if (isCompactLandscapePhone && chapterId == 2 && choice.word == "היפופוטם") {
                                                0.95f
                                            } else {
                                                1f
                                            },
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            val innerScale = innerPictureScaleForChoice(choice)
                            Chapter1Station4To6LessonChoiceCardSpec.Card(
                                choice = choice,
                                enabled = enabled,
                                scale = scale.value,
                                showWordCaption = showWordCaptions,
                                cardWidth = cardW,
                                cardHeight = cardH,
                                captionFontSize = captionSp,
                                innerPictureScale = innerScale,
                                onClick = {
                                    onChoiceWordPreview?.invoke(choice.id)
                                    val ok = onAttempt(choice.id)
                                    if (ok) {
                                        successChoiceId = choice.id
                                        scope.launch {
                                            scale.snapTo(1f)
                                            scale.animateTo(1.28f, tween(100))
                                            scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 420f))
                                        }
                                    } else {
                                        wrongFlashChoiceId = choice.id
                                        wrongFlashEpoch += 1
                                        scope.launch {
                                            flash.snapTo(1f)
                                            flash.animateTo(0f, tween(220))
                                        }
                                    }
                                },
                                isCorrectPick = choice.id == successChoiceId,
                                wrongFlashAlpha = flash.value,
                            )
                        }
                    }
                }
            }
        }
    }
}
