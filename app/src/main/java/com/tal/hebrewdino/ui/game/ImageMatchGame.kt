package com.tal.hebrewdino.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.LessonWordIllustrations
import com.tal.hebrewdino.ui.domain.Question
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun ImageMatchGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    onAttempt: (String) -> Boolean,
    /** When false, cards show only the picture (for pre-readers). */
    showWordCaptions: Boolean = true,
    captionSizeMultiplier: Float = 1f,
    pictureSizeMultiplier: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var successChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    LaunchedEffect(contentKey) {
        successChoiceId = null
    }
    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
        ) {
            val rowInnerWidth = maxWidth
            val choiceCount = question.choices.size.coerceAtLeast(1)
            val cardGap = 10.dp
            var cardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = rowInnerWidth,
                    count = choiceCount,
                    gap = cardGap,
                    minEach = 72.dp,
                    maxEach = 168.dp,
                )
            cardW = (cardW * pictureSizeMultiplier).coerceAtMost((rowInnerWidth - cardGap * (choiceCount - 1)) / choiceCount)
            val cardH = cardW * (110f / 160f)
            val density = LocalDensity.current
            val narrowRow = rowInnerWidth < 380.dp
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Large target letter, kept high so picture cards do not cover it.
                Text(
                    text = question.targetLetter,
                    style =
                        if (rowInnerWidth < 420.dp) {
                            androidx.compose.material3.MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                        } else {
                            androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black)
                        },
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .padding(top = 2.dp)
                            .offset(y = (-22).dp),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(if (narrowRow) 22.dp else 28.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(shakePx.roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                ) {
                    question.choices.forEach { choice ->
                        val scale = remember(choice.id, contentKey) { Animatable(1f) }
                        val captionSp =
                            with(density) {
                                (cardW.toPx() * 0.22f * captionSizeMultiplier).coerceIn(
                                    22f * fontScale * captionSizeMultiplier,
                                    40f * fontScale * captionSizeMultiplier,
                                ).toSp()
                            }
                        ImageMatchCard(
                            choice = choice,
                            enabled = enabled,
                            scale = scale.value,
                            showWordCaption = showWordCaptions,
                            cardWidth = cardW,
                            cardHeight = cardH,
                            captionFontSize = captionSp,
                            onClick = {
                                val ok = onAttempt(choice.id)
                                if (ok) {
                                    successChoiceId = choice.id
                                    scope.launch {
                                        scale.snapTo(1f)
                                        scale.animateTo(1.16f, tween(100))
                                        scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 420f))
                                    }
                                }
                            },
                            isCorrectPick = choice.id == successChoiceId,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageMatchCard(
    choice: LessonChoice,
    enabled: Boolean,
    scale: Float,
    showWordCaption: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    captionFontSize: TextUnit,
    isCorrectPick: Boolean,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val borderColor =
        if (isCorrectPick) {
            Color(0xFF2E7D32).copy(alpha = 0.95f)
        } else {
            Color(0xFF0B2B3D).copy(alpha = 0.15f)
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .scale(scale)
                .border(4.dp, borderColor, RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color(0xFFE8F5E9).copy(alpha = 0.45f)),
                    ),
                    RoundedCornerShape(22.dp),
                )
                .clickable(enabled = enabled, onClick = onClick)
                .padding(12.dp),
    ) {
        if (choice.tileDrawable == R.drawable.lesson_word_tile) {
            // `lesson_word_tile` is a ShapeDrawable XML (not supported by painterResource as an Image),
            // so render a nice placeholder tile ourselves.
            Box(
                modifier =
                    Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((choice.tintArgb shr 16) and 0xFF) / 255f,
                                green = ((choice.tintArgb shr 8) and 0xFF) / 255f,
                                blue = (choice.tintArgb and 0xFF) / 255f,
                                alpha = 1f,
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = choice.letter,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D).copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(Color.White.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        } else if (choice.tileDrawable == R.drawable.lesson_pic_placeholder) {
            val emoji = LessonWordIllustrations.emojiForWord(choice.word)
            val emojiSp =
                with(density) {
                    (cardWidth.toPx() * 0.34f).coerceIn(40f * fontScale, 72f * fontScale).toSp()
                }
            Box(
                modifier =
                    Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((choice.tintArgb shr 16) and 0xFF) / 255f,
                                green = ((choice.tintArgb shr 8) and 0xFF) / 255f,
                                blue = (choice.tintArgb and 0xFF) / 255f,
                                alpha = 1f,
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emoji,
                    fontSize = emojiSp,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Image(
                painter = painterResource(id = choice.tileDrawable),
                contentDescription = choice.word,
                modifier = Modifier.size(width = cardWidth, height = cardHeight),
                contentScale = ContentScale.Fit,
            )
        }
        if (showWordCaption) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = choice.word,
                fontSize = captionFontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.widthIn(max = cardWidth + 8.dp),
            )
        }
    }
}
