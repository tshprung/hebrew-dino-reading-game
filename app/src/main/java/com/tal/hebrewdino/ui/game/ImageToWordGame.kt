package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.components.learning.AutoFitSingleLineText
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2ChapterIds
import com.tal.hebrewdino.ui.domain.Season2Ch1QaPolicy
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.TrainingV1SourceStation
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageToWordGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    instructionText: String,
    chapterId: Int? = null,
    stationId: Int? = null,
    /** Unused; kept for call-site stability. */
    trainingRoundIndex: Int? = null,
    onPictureTapReplayWord: (() -> Unit)? = null,
    innerPictureScaleForChoice: (LessonChoice) -> Float = { choice -> Chapter1Station5And6ImageMatchInnerScale.innerScale(choice) },
    onAttempt: (choiceId: String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var successChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableStateOf(0) }

    LaunchedEffect(contentKey) {
        successChoiceId = null
        wrongFlashChoiceId = null
        wrongFlashEpoch = 0
    }

    val correctChoice: LessonChoice? = question.choices.firstOrNull { it.id == question.correctChoiceId }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val w = maxWidth
        val density = LocalDensity.current
        val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
        val (layoutChapterId, layoutStationId) =
            if (chapterId != null && stationId != null) {
                TrainingV1SourceStation.resolve(chapterId, stationId)
            } else {
                chapterId to stationId
            }
        val isChapter3Station6ImageToWord = layoutChapterId == 3 && layoutStationId == 6
        val isChapter6Station6ImageToWord = layoutChapterId == 6 && layoutStationId == 6
        val isSeason2ImageToWord =
            chapterId != null &&
                stationId != null &&
                Season2StationAudio.isSeason2ImageToWordLayout(chapterId, stationId)
        val isCh1FinalePictureToWord =
            isSeason2ImageToWord && chapterId == Season2ChapterIds.Chapter1Tyrannosaurus
        val isChapter3Or6Station6ImageToWord =
            isChapter3Station6ImageToWord || isChapter6Station6ImageToWord || isSeason2ImageToWord
        val isInstructionWrapContent = isChapter3Or6Station6ImageToWord

        // Match the same card sizing math as PictureStartsWith/ImageMatch (Episode 1/2 station 4).
        val cardGap = 10.dp
        var pictureCardW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = w,
                count = 3,
                gap = cardGap,
                minEach = 72.dp,
                maxEach = 168.dp,
            )
        if (isCompactLandscapePhone) {
            pictureCardW *= 0.70f
        }
        if (isChapter3Or6Station6ImageToWord) {
            pictureCardW = (pictureCardW * 1.40f).coerceAtMost(w - 48.dp)
        }
        val pictureCardH = pictureCardW * LessonChoiceCardPictureAspect
        val chapter3Station6ExtraDown = if (layoutChapterId == 3 && layoutStationId == 6) 19.dp else 0.dp
        val chapter6Station6ExtraDown =
            when {
                chapterId == 6 && stationId == 6 -> 38.dp
                isSeason2ImageToWord && chapterId == Season2ChapterIds.Chapter6Mosasaurus -> 24.dp
                isSeason2ImageToWord && chapterId == Season2ChapterIds.Chapter1Tyrannosaurus ->
                    19.dp + Season2Ch1QaPolicy.FinaleExtraDownDp
                isSeason2ImageToWord -> 12.dp
                else -> 0.dp
            }
        val baseDown = if (isCompactLandscapePhone) (-10).dp else 0.dp
        val totalDown = baseDown + chapter3Station6ExtraDown + chapter6Station6ExtraDown
        var hintLetter by remember(contentKey) { mutableStateOf<String?>(null) }
        var hintEpoch by remember(contentKey) { mutableIntStateOf(0) }

        val optionCount = question.choices.size.coerceIn(2, 6)
        val optionWBase =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = w,
                count = optionCount,
                gap = cardGap,
                minEach = 80.dp,
                maxEach = 168.dp,
            )
        val optionW =
            if (isChapter3Or6Station6ImageToWord) {
                optionWBase * 0.80f
            } else {
                optionWBase
            }
        val optionCardH = (optionW * 0.54f).coerceAtLeast(if (isCompactLandscapePhone) 56.dp else 64.dp)

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 12.dp,
                        end =
                            if (isSeason2ImageToWord && !isCh1FinalePictureToWord) {
                                80.dp
                            } else {
                                12.dp
                            },
                        top = if (isCompactLandscapePhone) 0.dp else 8.dp,
                        bottom = if (isCompactLandscapePhone) 4.dp else 8.dp,
                    )
                    .then(if (totalDown != 0.dp) Modifier.offset(y = totalDown) else Modifier),
        ) {
            if (isCh1FinalePictureToWord) {
                Ch1FinaleImageToWordAlignedContent(
                    instructionText = instructionText,
                    isCompactLandscapePhone = isCompactLandscapePhone,
                    maxContentWidth = w - 24.dp,
                    correctChoice = correctChoice,
                    pictureCardW = pictureCardW,
                    pictureCardH = pictureCardH,
                    pictureTapReplays = onPictureTapReplayWord != null,
                    onPictureTapReplayWord = onPictureTapReplayWord,
                    innerPictureScaleForChoice = innerPictureScaleForChoice,
                    enabled = enabled,
                    optionW = optionW,
                    optionCardH = optionCardH,
                    cardGap = cardGap,
                    question = question,
                    contentKey = contentKey,
                    successChoiceId = successChoiceId,
                    wrongFlashChoiceId = wrongFlashChoiceId,
                    wrongFlashEpoch = wrongFlashEpoch,
                    density = density,
                    chapterId = chapterId,
                    stationId = stationId,
                    onAttempt = onAttempt,
                    onSuccessChoice = { successChoiceId = it },
                    onWrongChoice = { id ->
                        wrongFlashChoiceId = id
                        wrongFlashEpoch += 1
                    },
                )
            } else {
                val columnHorizontalAlignment = Alignment.CenterHorizontally
                val instructionTextAlign =
                    if (isSeason2ImageToWord) {
                        TextAlign.End
                    } else {
                        TextAlign.Center
                    }
                val instructionWrapAlignment =
                    if (isSeason2ImageToWord) {
                        Alignment.End
                    } else {
                        Alignment.CenterHorizontally
                    }
                val optionsRowAlignment = Alignment.CenterHorizontally

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = columnHorizontalAlignment,
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = instructionText,
                        fontSize =
                            when {
                                isCompactLandscapePhone -> 20.sp
                                else -> 24.sp
                            },
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B2B3D),
                        textAlign = instructionTextAlign,
                        modifier =
                            Modifier
                                .then(
                                    if (isInstructionWrapContent) {
                                        Modifier.wrapContentWidth(instructionWrapAlignment)
                                    } else {
                                        Modifier.fillMaxWidth()
                                    },
                                )
                                .widthIn(max = w - 24.dp)
                                .padding(top = if (isCompactLandscapePhone) 2.dp else 6.dp, bottom = if (isCompactLandscapePhone) 6.dp else 10.dp)
                                .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                    )

                    if (hintLetter != null) {
                        TargetLetterHeaderChip(
                            letter = hintLetter!!,
                            fontSize = if (isCompactLandscapePhone) 44.sp else 54.sp,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }

                    if (correctChoice != null) {
                        val pictureTapReplays = onPictureTapReplayWord != null
                        val innerScale =
                            innerPictureScaleForChoice(correctChoice) *
                                when {
                                    isChapter3Station6ImageToWord && !isSeason2ImageToWord -> 1.40f
                                    isChapter6Station6ImageToWord && !isSeason2ImageToWord -> 1.40f * 0.70f
                                    isSeason2ImageToWord -> 1.35f
                                    isCompactLandscapePhone -> 1.50f
                                    else -> 1f
                                }
                        LessonChoiceCard(
                            choice = correctChoice,
                            enabled = enabled && pictureTapReplays,
                            scale = 1f,
                            showWordCaption = false,
                            cardWidth = pictureCardW,
                            cardHeight = pictureCardH,
                            captionFontSize = 1.sp,
                            innerPictureScale = innerScale,
                            onClick = { if (pictureTapReplays) onPictureTapReplayWord.invoke() },
                        )
                    } else {
                        Spacer(modifier = Modifier.height(pictureCardH))
                    }

                    Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 6.dp else 14.dp))

                    ImageToWordOptionsRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(cardGap, optionsRowAlignment),
                        question = question,
                        contentKey = contentKey,
                        enabled = enabled,
                        optionW = optionW,
                        optionCardH = optionCardH,
                        density = density,
                        chapterId = chapterId,
                        stationId = stationId,
                        isChapter3Or6Station6ImageToWord = isChapter3Or6Station6ImageToWord,
                        successChoiceId = successChoiceId,
                        wrongFlashChoiceId = wrongFlashChoiceId,
                        wrongFlashEpoch = wrongFlashEpoch,
                        onAttempt = onAttempt,
                        onSuccessChoice = { successChoiceId = it },
                        onWrongChoice = { id ->
                            wrongFlashChoiceId = id
                            wrongFlashEpoch += 1
                        },
                    )
                }
            }
        }
    }
}

/**
 * S2 Ch1 finale alignment:
 * 1. LTR shell Row — weighted content area + fixed [FinaleDinoReservedWidthDp] spacer (physical right).
 * 2. RTL Column inside content area with [Alignment.CenterHorizontally] — shared anchor X for all blocks.
 * 3. Instruction wrap-content at column top defines anchor width; image centered on same axis.
 * 4. Options [Row] wrap-content centered in column — middle option x-center == image x-center (odd counts).
 */
@Composable
private fun Ch1FinaleImageToWordAlignedContent(
    instructionText: String,
    isCompactLandscapePhone: Boolean,
    maxContentWidth: androidx.compose.ui.unit.Dp,
    correctChoice: LessonChoice?,
    pictureCardW: androidx.compose.ui.unit.Dp,
    pictureCardH: androidx.compose.ui.unit.Dp,
    pictureTapReplays: Boolean,
    onPictureTapReplayWord: (() -> Unit)?,
    innerPictureScaleForChoice: (LessonChoice) -> Float,
    enabled: Boolean,
    optionW: androidx.compose.ui.unit.Dp,
    optionCardH: androidx.compose.ui.unit.Dp,
    cardGap: androidx.compose.ui.unit.Dp,
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    successChoiceId: String?,
    wrongFlashChoiceId: String?,
    wrongFlashEpoch: Int,
    density: androidx.compose.ui.unit.Density,
    chapterId: Int?,
    stationId: Int?,
    onAttempt: (String) -> Boolean,
    onSuccessChoice: (String) -> Unit,
    onWrongChoice: (String) -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Text(
                            text = instructionText,
                            fontSize = if (isCompactLandscapePhone) 20.sp else 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                    .widthIn(max = maxContentWidth)
                                    .padding(top = if (isCompactLandscapePhone) 2.dp else 6.dp, bottom = if (isCompactLandscapePhone) 6.dp else 10.dp)
                                    .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                        )

                        if (correctChoice != null) {
                            val innerScale = innerPictureScaleForChoice(correctChoice) * 1.35f
                            LessonChoiceCard(
                                choice = correctChoice,
                                enabled = enabled && pictureTapReplays,
                                scale = 1f,
                                showWordCaption = false,
                                cardWidth = pictureCardW,
                                cardHeight = pictureCardH,
                                captionFontSize = 1.sp,
                                innerPictureScale = innerScale,
                                onClick = { if (pictureTapReplays) onPictureTapReplayWord?.invoke() },
                            )
                        } else {
                            Spacer(modifier = Modifier.height(pictureCardH))
                        }

                        Spacer(modifier = Modifier.height(if (isCompactLandscapePhone) 6.dp else 14.dp))

                        ImageToWordOptionsRow(
                            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                            horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                            question = question,
                            contentKey = contentKey,
                            enabled = enabled,
                            optionW = optionW,
                            optionCardH = optionCardH,
                            density = density,
                            chapterId = chapterId,
                            stationId = stationId,
                            isChapter3Or6Station6ImageToWord = true,
                            successChoiceId = successChoiceId,
                            wrongFlashChoiceId = wrongFlashChoiceId,
                            wrongFlashEpoch = wrongFlashEpoch,
                            onAttempt = onAttempt,
                            onSuccessChoice = onSuccessChoice,
                            onWrongChoice = onWrongChoice,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(Season2Ch1QaPolicy.FinaleDinoReservedWidthDp))
        }
    }
}

@Composable
private fun ImageToWordOptionsRow(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    optionW: androidx.compose.ui.unit.Dp,
    optionCardH: androidx.compose.ui.unit.Dp,
    density: androidx.compose.ui.unit.Density,
    chapterId: Int?,
    stationId: Int?,
    isChapter3Or6Station6ImageToWord: Boolean,
    successChoiceId: String?,
    wrongFlashChoiceId: String?,
    wrongFlashEpoch: Int,
    onAttempt: (String) -> Boolean,
    onSuccessChoice: (String) -> Unit,
    onWrongChoice: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val hasLockedCorrectChoice =
            isChapter3Or6Station6ImageToWord && successChoiceId != null
        question.choices.forEach { choice ->
            val scale = remember(choice.id, contentKey) { Animatable(1f) }
            val flash = remember(choice.id, contentKey) { Animatable(0f) }

            LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, choice.id, contentKey) {
                if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != choice.id) return@LaunchedEffect
                flash.snapTo(1f)
                flash.animateTo(0f, tween(220))
            }

            val captionSp =
                captionFontSizeForWordCard(
                    density = density,
                    cardWidth = optionW,
                    word = choice.word,
                    sizeMultiplier =
                        when {
                            isChapter3Or6Station6ImageToWord -> 1.25f * 0.70f
                            else -> 1.25f
                        },
                    chapterId = chapterId,
                    stationId = stationId,
                )
            val captionStyle =
                androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                )

            val isLockedCorrectThisChoice = hasLockedCorrectChoice && choice.id == successChoiceId
            Box(
                modifier =
                    Modifier
                        .width(optionW)
                        .height(optionCardH)
                        .background(
                            if (!hasLockedCorrectChoice) {
                                Color.White.copy(alpha = 0.92f)
                            } else if (isLockedCorrectThisChoice) {
                                Color.White.copy(alpha = 0.92f)
                            } else {
                                Color.Transparent
                            },
                            RoundedCornerShape(16.dp),
                        )
                        .border(
                            width =
                                when {
                                    flash.value > 0.01f -> 2.dp
                                    isLockedCorrectThisChoice -> 4.dp
                                    else -> 2.dp
                                },
                            color =
                                when {
                                    isLockedCorrectThisChoice -> Color(0xFF2E7D32).copy(alpha = 0.95f)
                                    flash.value > 0.01f -> Color(0xFFE53935).copy(alpha = 0.90f)
                                    hasLockedCorrectChoice -> Color(0xFF0B2B3D).copy(alpha = 0.10f)
                                    else -> Color(0xFF0B2B3D).copy(alpha = 0.18f)
                                },
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable(enabled = enabled) {
                            val ok = onAttempt(choice.id)
                            if (ok) {
                                onSuccessChoice(choice.id)
                                scope.launch {
                                    scale.snapTo(1f)
                                    scale.animateTo(1.08f, tween(90))
                                    scale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = 520f))
                                }
                            } else {
                                onWrongChoice(choice.id)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isChapter3Or6Station6ImageToWord) {
                    AutoFitSingleLineText(
                        text = choice.word,
                        maxWidth = optionW - 20.dp,
                        maxHeight = optionCardH - 12.dp,
                        targetFontSize = captionSp,
                        style = captionStyle,
                        minFontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = choice.word,
                        fontSize = captionSp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

