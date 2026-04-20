package com.tal.hebrewdino.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.LessonWordIllustrations
import com.tal.hebrewdino.ui.domain.Question
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PictureStartsWithGame(
    question: Question.PictureStartsWithQuestion,
    enabled: Boolean,
    shakePx: Float,
    onPickLetter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .offset { IntOffset(shakePx.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "באיזו אות המילה מתחילה?",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier =
                Modifier
                    .widthIn(max = 280.dp)
                    .border(2.dp, Color(0xFF0B2B3D).copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LessonWordPictureTile(
                word = question.word,
                tileDrawable = question.tileDrawable,
                tintArgb = question.tintArgb,
                imageHeight = 140.dp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.word,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            question.optionLetters.forEach { letter ->
                Button(
                    onClick = { onPickLetter(letter) },
                    enabled = enabled,
                    modifier = Modifier.widthIn(min = 64.dp, max = 84.dp),
                ) {
                    Text(text = letter, fontSize = 38.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun LessonWordPictureTile(
    word: String,
    tileDrawable: Int,
    tintArgb: Int,
    imageHeight: Dp,
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val tileW = maxWidth.coerceAtMost(220.dp)
        if (tileDrawable == R.drawable.lesson_word_tile) {
            Box(
                modifier =
                    Modifier
                        .size(width = tileW, height = imageHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((tintArgb shr 16) and 0xFF) / 255f,
                                green = ((tintArgb shr 8) and 0xFF) / 255f,
                                blue = (tintArgb and 0xFF) / 255f,
                                alpha = 1f,
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = word.first().toString(),
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
        } else if (tileDrawable == R.drawable.lesson_pic_placeholder) {
            val emoji = LessonWordIllustrations.emojiForWord(word)
            val emojiSp =
                with(density) {
                    (tileW.toPx() * 0.34f).coerceIn(40f * fontScale, 72f * fontScale).toSp()
                }
            Box(
                modifier =
                    Modifier
                        .size(width = tileW, height = imageHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((tintArgb shr 16) and 0xFF) / 255f,
                                green = ((tintArgb shr 8) and 0xFF) / 255f,
                                blue = (tintArgb and 0xFF) / 255f,
                                alpha = 1f,
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = emojiSp, textAlign = TextAlign.Center)
            }
        } else {
            Image(
                painter = painterResource(id = tileDrawable),
                contentDescription = word,
                modifier = Modifier.size(width = tileW, height = imageHeight),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
