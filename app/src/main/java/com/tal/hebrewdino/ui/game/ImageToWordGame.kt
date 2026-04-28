package com.tal.hebrewdino.ui.game

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun ImageToWordGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    instructionText: String,
    onWordPressed: ((choiceId: String) -> Unit)? = null,
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
        val h = maxHeight
        val density = LocalDensity.current

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
        val pictureCardH = pictureCardW * LessonChoiceCardPictureAspect

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = instructionText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(top = 6.dp, bottom = 10.dp)
                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )

            if (correctChoice != null) {
                LessonChoiceCard(
                    choice = correctChoice,
                    enabled = false,
                    scale = 1f,
                    showWordCaption = false,
                    cardWidth = pictureCardW,
                    cardHeight = pictureCardH,
                    captionFontSize = 1.sp,
                    innerPictureScale = Chapter1Station5And6ImageMatchInnerScale.innerScale(correctChoice),
                    onClick = { },
                )
            } else {
                Spacer(modifier = Modifier.height(pictureCardH))
            }

            Spacer(modifier = Modifier.height(14.dp))

            val optionCount = question.choices.size.coerceIn(2, 6)
            val optionW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = w,
                    count = optionCount,
                    gap = cardGap,
                    minEach = 80.dp,
                    maxEach = 168.dp,
                )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                            sizeMultiplier = 1.25f,
                            chapterId = null,
                            stationId = null,
                        )

                    Box(
                        modifier =
                            Modifier
                                .width(optionW)
                                .height((optionW * 0.54f).coerceAtLeast(64.dp))
                                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                                .border(
                                    width = 2.dp,
                                    color =
                                        when {
                                            choice.id == successChoiceId -> Color(0xFF2E7D32).copy(alpha = 0.95f)
                                            flash.value > 0.01f -> Color(0xFFE53935).copy(alpha = 0.90f)
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
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
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
}

