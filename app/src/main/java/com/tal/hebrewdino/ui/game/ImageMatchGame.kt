package com.tal.hebrewdino.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.LessonChoice
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
    /** When false, cards show only the picture (for pre-readers); header shows [Question.ImageMatchQuestion.targetLetter] only. */
    showWordCaptions: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var successChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    LaunchedEffect(contentKey) {
        successChoiceId = null
    }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (showWordCaptions) question.targetWord else question.targetLetter,
            style =
                if (showWordCaptions) {
                    androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)
                } else {
                    androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black)
                },
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakePx.roundToInt(), 0) },
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        ) {
            question.choices.forEach { choice ->
                val scale = remember(choice.id, contentKey) { Animatable(1f) }
                ImageMatchCard(
                    choice = choice,
                    enabled = enabled,
                    scale = scale.value,
                    showWordCaption = showWordCaptions,
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

@Composable
private fun ImageMatchCard(
    choice: LessonChoice,
    enabled: Boolean,
    scale: Float,
    showWordCaption: Boolean,
    isCorrectPick: Boolean,
    onClick: () -> Unit,
) {
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
            Box(
                modifier =
                    Modifier
                        .size(width = 160.dp, height = 110.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((choice.tintArgb shr 16) and 0xFF) / 255f,
                                green = ((choice.tintArgb shr 8) and 0xFF) / 255f,
                                blue = (choice.tintArgb and 0xFF) / 255f,
                                alpha = ((choice.tintArgb ushr 24) and 0xFF) / 255f,
                            ),
                        ),
            )
        } else {
            Image(
                painter = painterResource(id = choice.tileDrawable),
                contentDescription = choice.word,
                modifier = Modifier.size(width = 160.dp, height = 110.dp),
                contentScale = ContentScale.Fit,
            )
        }
        if (showWordCaption) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = choice.word,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
