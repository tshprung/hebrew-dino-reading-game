package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PicturePickOneBoard(
    question: Question.PicturePickOneQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    onPickId: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        question.choices.forEach { choice ->
            LessonPictureCard(
                choice = choice,
                selected = false,
                locked = false,
                enabled = enabled,
                onClick = { onPickId(choice.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PicturePickAllBoard(
    question: Question.PicturePickAllQuestion,
    contentKey: Int,
    resetEpoch: Int,
    enabled: Boolean,
    shakePx: Float,
    onTwoPicked: (Set<String>) -> Unit,
) {
    var selected by remember(question, contentKey, resetEpoch) { mutableStateOf<Set<String>>(emptySet()) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.offset { IntOffset(shakePx.roundToInt(), 0) },
    ) {
        question.choices.forEach { choice ->
            val on = choice.id in selected
            LessonPictureCard(
                choice = choice,
                selected = on,
                locked = false,
                enabled = enabled,
                onClick = {
                    if (!enabled) return@LessonPictureCard
                    val next =
                        if (on) {
                            selected - choice.id
                        } else if (selected.size < question.correctIds.size) {
                            selected + choice.id
                        } else {
                            selected
                        }
                    selected = next
                    if (selected.size == question.correctIds.size) {
                        onTwoPicked(selected)
                    }
                },
            )
        }
    }
}

@Composable
private fun LessonPictureCard(
    choice: LessonChoice,
    selected: Boolean,
    locked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember(choice.id) { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .width(132.dp)
                .border(
                    width = if (selected) 4.dp else 2.dp,
                    color =
                        when {
                            locked -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                            selected -> Color(0xFF2AA6C9)
                            else -> Color(0xFF0B2B3D).copy(alpha = 0.18f)
                        },
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled && !locked,
                    onClick = onClick,
                )
                .padding(10.dp),
    ) {
        Image(
            painter = painterResource(id = choice.tileDrawable),
            contentDescription = choice.word,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(choice.tintArgb)),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = choice.word,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 22.sp,
        )
    }
}
