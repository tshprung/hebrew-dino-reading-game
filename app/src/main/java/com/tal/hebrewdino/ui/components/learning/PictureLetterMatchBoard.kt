package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.Question
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PictureLetterMatchBoard(
    question: Question.PictureLetterMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    onWrongPair: () -> Unit,
    onRoundComplete: () -> Unit,
) {
    val pairs = question.pairs
    val letterRow = remember(pairs, contentKey) { pairs.map { it.letter }.shuffled() }
    var selectedPictureLetter by remember(pairs, contentKey) { mutableStateOf<String?>(null) }
    var matchedLetters by remember(pairs, contentKey) { mutableStateOf<Set<String>>(emptySet()) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakePx.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "תמונות",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF0B2B3D).copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            pairs.forEach { pair ->
                val done = pair.letter in matchedLetters
                val selected = pair.letter == selectedPictureLetter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .width(118.dp)
                            .border(
                                width =
                                    when {
                                        done -> 4.dp
                                        selected -> 3.dp
                                        else -> 2.dp
                                    },
                                color =
                                    when {
                                        done -> Color(0xFF2E7D32)
                                        selected -> Color(0xFF1976D2)
                                        else -> Color(0xFF0B2B3D).copy(alpha = 0.22f)
                                    },
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                            .clickable(enabled = enabled && !done) {
                                selectedPictureLetter = pair.letter
                            },
                ) {
                    Image(
                        painter = painterResource(id = pair.imageRes),
                        contentDescription = pair.caption,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                        contentScale = ContentScale.Fit,
                    )
                    pair.caption?.let { cap ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cap,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "אותיות",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF0B2B3D).copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            letterRow.forEach { letter ->
                val done = letter in matchedLetters
                OutlinedButton(
                    onClick = {
                        val pic = selectedPictureLetter
                        if (pic == null) return@OutlinedButton
                        if (letter == pic) {
                            matchedLetters = matchedLetters + letter
                            selectedPictureLetter = null
                            if (matchedLetters.size >= pairs.size) {
                                onRoundComplete()
                            }
                        } else {
                            selectedPictureLetter = null
                            onWrongPair()
                        }
                    },
                    enabled = enabled && !done,
                    modifier = Modifier.width(76.dp),
                ) {
                    Text(text = letter, fontSize = 36.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
