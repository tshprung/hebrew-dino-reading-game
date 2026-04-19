package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
    /** When set, wrong letter after choosing a picture calls this instead of [onWrongPair] (softer finale). */
    onSoftLetterMismatch: (() -> Unit)? = null,
    /**
     * Chapter 1 station 6 only: warmer frame, tighter grid, and a very obvious “picture selected” state
     * so the finale reads as a short climax, not a worksheet row.
     */
    chapter1FinalePresentation: Boolean = false,
) {
    val pairs = question.pairs
    val letterRow = remember(pairs, contentKey) { pairs.map { it.letter }.shuffled() }
    var selectedPictureLetter by remember(pairs, contentKey) { mutableStateOf<String?>(null) }
    var matchedLetters by remember(pairs, contentKey) { mutableStateOf<Set<String>>(emptySet()) }

    val picTileWidth = if (chapter1FinalePresentation) 124.dp else 118.dp
    val picImageH = if (chapter1FinalePresentation) 76.dp else 72.dp
    val picRowHGap = if (chapter1FinalePresentation) 10.dp else 12.dp
    val picLetterGap = if (chapter1FinalePresentation) 12.dp else 18.dp

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (chapter1FinalePresentation) {
                        Modifier.background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color(0xFFFFF8E1).copy(alpha = 0.92f),
                                            Color(0xFFFFFDE7).copy(alpha = 0.55f),
                                        ),
                                ),
                            shape = RoundedCornerShape(22.dp),
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(if (chapter1FinalePresentation) 10.dp else 0.dp)
                .offset { IntOffset(shakePx.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (chapter1FinalePresentation) {
            Text(
                text = "✨",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "חיבור קטן",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D).copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = "תמונות",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF0B2B3D).copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(if (chapter1FinalePresentation) 4.dp else 6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(picRowHGap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(picRowHGap),
        ) {
            pairs.forEach { pair ->
                val done = pair.letter in matchedLetters
                val selected = pair.letter == selectedPictureLetter
                val picInteraction = remember(pair.letter, contentKey) { MutableInteractionSource() }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .width(picTileWidth)
                            .border(
                                width =
                                    when {
                                        done -> 4.dp
                                        selected ->
                                            if (chapter1FinalePresentation) {
                                                6.dp
                                            } else {
                                                3.dp
                                            }
                                        else -> 2.dp
                                    },
                                color =
                                    when {
                                        done -> Color(0xFF2E7D32)
                                        selected ->
                                            if (chapter1FinalePresentation) {
                                                Color(0xFFFFC400)
                                            } else {
                                                Color(0xFF1976D2)
                                            }
                                        else -> Color(0xFF0B2B3D).copy(alpha = 0.22f)
                                    },
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(
                                if (chapter1FinalePresentation && selected && !done) {
                                    Color(0xFFFFECB3).copy(alpha = 0.55f)
                                } else {
                                    Color.White.copy(alpha = 0.9f)
                                },
                                RoundedCornerShape(16.dp),
                            )
                            .padding(8.dp)
                            .clickable(
                                interactionSource = picInteraction,
                                indication = null,
                                enabled = enabled && !done,
                            ) {
                                selectedPictureLetter = pair.letter
                            },
                ) {
                    Image(
                        painter = painterResource(id = pair.imageRes),
                        contentDescription = pair.caption,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(picImageH),
                        contentScale = ContentScale.Fit,
                        colorFilter =
                            pair.tintArgb?.let { argb ->
                                ColorFilter.tint(Color(argb))
                            },
                    )
                    pair.caption?.let { cap ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cap,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(picLetterGap))
        Text(
            text = "אותיות",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF0B2B3D).copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(if (chapter1FinalePresentation) 4.dp else 6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(picRowHGap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(picRowHGap),
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
                            if (onSoftLetterMismatch != null) {
                                onSoftLetterMismatch.invoke()
                            } else {
                                onWrongPair()
                            }
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
