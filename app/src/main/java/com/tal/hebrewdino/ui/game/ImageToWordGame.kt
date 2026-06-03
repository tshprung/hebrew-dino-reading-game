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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
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
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TrainingV1ImageToWordReplayButtonTag: String = "training_v1_image_to_word_replay"
private const val TrainingV1ImageToWordHintButtonTag: String = "training_v1_image_to_word_hint"

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageToWordGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    instructionText: String,
    chapterId: Int? = null,
    stationId: Int? = null,
    /** Training chapter: 1-based round index (e.g. 3 and 8 are ImageToWord). */
    trainingRoundIndex: Int? = null,
    onPictureTapReplayWord: (() -> Unit)? = null,
    onWordPressed: ((choiceId: String) -> Unit)? = null,
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
        val isTrainingStation3 =
            chapterId == TrainingV1Config.CHAPTER_ID && stationId == TrainingV1Config.STATION_PICTURE_CHOOSE_WORD
        val isTrainingImageToWordRound38 =
            isTrainingStation3 && trainingRoundIndex in setOf(3, 8)
        val isChapter3Station6ImageToWord = chapterId == 3 && stationId == 6
        val isChapter6Station6ImageToWord = chapterId == 6 && stationId == 6
        val isChapter3Or6Station6ImageToWord =
            isChapter3Station6ImageToWord || isChapter6Station6ImageToWord
        val isInstructionWrapContent = isTrainingImageToWordRound38 || isChapter3Or6Station6ImageToWord

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
        if (isTrainingImageToWordRound38) {
            pictureCardW = (pictureCardW * 1.30f).coerceAtMost(w - 48.dp)
        }
        val pictureCardH = pictureCardW * LessonChoiceCardPictureAspect
        val chapter3Station6ExtraDown = if (chapterId == 3 && stationId == 6) 19.dp else 0.dp
        val chapter6Station6ExtraDown = if (chapterId == 6 && stationId == 6) 38.dp else 0.dp
        // Training rounds 3 and 8 (ImageToWord): ~5 mm lower than default (~19 dp).
        val trainingImageToWordExtraDown =
            if (isTrainingImageToWordRound38) {
                19.dp
            } else {
                0.dp
            }
        val baseDown = if (isCompactLandscapePhone) (-10).dp else 0.dp
        val totalDown = baseDown + chapter3Station6ExtraDown + chapter6Station6ExtraDown + trainingImageToWordExtraDown
        var hintLetter by remember(contentKey) { mutableStateOf<String?>(null) }
        var hintEpoch by remember(contentKey) { mutableIntStateOf(0) }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = if (isCompactLandscapePhone) 0.dp else 8.dp)
                    .then(if (totalDown != 0.dp) Modifier.offset(y = totalDown) else Modifier),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = instructionText,
                    fontSize =
                        when {
                            isTrainingStation3 && isCompactLandscapePhone -> 18.sp
                            isCompactLandscapePhone -> 20.sp
                            else -> 24.sp
                        },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .then(
                                if (isInstructionWrapContent) {
                                    Modifier.wrapContentWidth(Alignment.CenterHorizontally)
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
                                isChapter3Station6ImageToWord -> 1.40f
                                isChapter6Station6ImageToWord -> 1.40f * 0.70f
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
                if ((chapterId == 3 || chapterId == 6) && stationId == 6) {
                    optionWBase * 0.80f
                } else {
                    optionWBase
                }

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val hasLockedCorrectChoice =
                    (
                        (chapterId == 3 && stationId == 6) ||
                            (chapterId == 6 && stationId == 6) ||
                            isTrainingStation3
                    ) && successChoiceId != null
                question.choices.forEach { choice ->
                    val scale = remember(choice.id, contentKey) { Animatable(1f) }
                    val flash = remember(choice.id, contentKey) { Animatable(0f) }

                    LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, choice.id, contentKey) {
                        if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != choice.id) return@LaunchedEffect
                        flash.snapTo(1f)
                        flash.animateTo(0f, tween(220))
                    }

                    val optionCardH = (optionW * 0.54f).coerceAtLeast(if (isCompactLandscapePhone) 56.dp else 64.dp)
                    val captionSp =
                        captionFontSizeForWordCard(
                            density = density,
                            cardWidth = optionW,
                            word = choice.word,
                            sizeMultiplier =
                                when {
                                    isTrainingStation3 && choice.word == "היפופוטם" -> 1.05f
                                    (chapterId == 3 || chapterId == 6) && stationId == 6 -> 1.25f * 0.70f
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
                                    onWordPressed?.invoke(choice.id)
                                    val ok = onAttempt(choice.id)
                                    if (ok) {
                                        successChoiceId = choice.id
                                        scope.launch {
                                            scale.snapTo(1f)
                                            scale.animateTo(1.08f, tween(90))
                                            scale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = 520f))
                                        }
                                    } else {
                                        wrongFlashChoiceId = choice.id
                                        wrongFlashEpoch += 1
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = if (isCompactLandscapePhone) 4.dp else 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isTrainingStation3 || isChapter3Or6Station6ImageToWord) {
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

        if (isTrainingStation3) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
                        .widthIn(max = 118.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FilledTonalButton(
                    onClick = { onPictureTapReplayWord?.invoke() },
                    enabled = enabled && onPictureTapReplayWord != null,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.testTag(TrainingV1ImageToWordReplayButtonTag),
                ) {
                    Text("🔊 שוב", fontSize = 22.sp)
                }
                FilledTonalButton(
                    onClick = {
                        val first = question.targetWord.firstOrNull()?.toString() ?: return@FilledTonalButton
                        hintLetter = first
                        hintEpoch += 1
                        val epoch = hintEpoch
                        scope.launch {
                            delay(2100L)
                            if (hintEpoch == epoch) hintLetter = null
                        }
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.testTag(TrainingV1ImageToWordHintButtonTag),
                ) {
                    Text("רמז", fontSize = 22.sp)
                }
            }
        }
    }
    }
}

