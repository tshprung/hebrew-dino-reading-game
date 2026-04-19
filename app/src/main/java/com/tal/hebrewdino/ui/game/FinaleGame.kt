package com.tal.hebrewdino.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.PictureLetterPair
import com.tal.hebrewdino.ui.domain.Question

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinaleGame(
    question: Question.FinaleSlotQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakeEpoch: Int,
    onWrongPlacement: () -> Unit,
    onSolved: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expected =
        remember(question, contentKey) {
            question.words.flatMap { w -> w.map { it.toString() } }
        }
    val slotLetters: SnapshotStateList<String?> =
        remember(question, contentKey) {
            mutableStateListOf<String?>().apply { repeat(expected.size) { add(null) } }
        }
    val pool: SnapshotStateList<String> =
        remember(question, contentKey) {
            mutableStateListOf<String>().apply { addAll(question.letterPool) }
        }
    var selectedPoolIndex by remember(question, contentKey) { mutableIntStateOf(-1) }

    LaunchedEffect(question, contentKey) {
        slotLetters.clear()
        repeat(expected.size) { slotLetters.add(null) }
        pool.clear()
        pool.addAll(question.letterPool)
        selectedPoolIndex = -1
    }

    LaunchedEffect(shakeEpoch) {
        if (shakeEpoch <= 0) return@LaunchedEffect
        selectedPoolIndex = -1
    }

    fun tryPlace(slotIndex: Int) {
        val pi = selectedPoolIndex
        if (pi < 0 || pi >= pool.size) return
        val letter = pool[pi]
        if (letter != expected[slotIndex]) {
            onWrongPlacement()
            selectedPoolIndex = -1
            return
        }
        slotLetters[slotIndex] = letter
        pool.removeAt(pi)
        selectedPoolIndex = -1
        if (slotLetters.all { it != null }) {
            val l0 = question.words[0].length
            val w1 = (0 until l0).joinToString("") { i -> slotLetters[i]!! }
            val w2 = (l0 until expected.size).joinToString("") { i -> slotLetters[i]!! }
            if (w1 == question.words[0] && w2 == question.words[1]) {
                onSolved(listOf(w1, w2))
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            question.pairHints.forEach { hint ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FinaleHintImage(pair = hint, modifier = Modifier.size(100.dp))
                    hint.caption?.let { cap ->
                        Text(cap, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0B2B3D))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        question.words.forEachIndexed { wi, word ->
            val start = question.words.take(wi).sumOf { it.length }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(word.length) { j ->
                    val idx = start + j
                    val ch = slotLetters[idx]
                    Box(
                        modifier =
                            Modifier
                                .width(52.dp)
                                .height(58.dp)
                                .border(3.dp, Color(0xFF1565C0).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                                .clickable(enabled = enabled) {
                                    if (ch != null) {
                                        pool.add(ch)
                                        slotLetters[idx] = null
                                    } else if (selectedPoolIndex >= 0) {
                                        tryPlace(idx)
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = ch ?: " ",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            pool.forEachIndexed { i, letter ->
                val sel = i == selectedPoolIndex
                Box(
                    modifier =
                        Modifier
                            .width(56.dp)
                            .height(56.dp)
                            .border(
                                width = if (sel) 4.dp else 2.dp,
                                color = if (sel) Color(0xFFFFC400) else Color(0xFF0B2B3D).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(Color(0xFFFFF9C4).copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                            .clickable(enabled = enabled) {
                                selectedPoolIndex = if (sel) -1 else i
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(letter, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
                }
            }
        }
    }
}

@Composable
private fun FinaleHintImage(
    pair: PictureLetterPair,
    modifier: Modifier = Modifier,
) {
    val argb = pair.tintArgb ?: 0xFFE0E0E0.toInt()
    if (pair.imageRes == R.drawable.lesson_word_tile) {
        Box(
            modifier =
                modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Color(
                            red = (argb shr 16 and 0xFF) / 255f,
                            green = (argb shr 8 and 0xFF) / 255f,
                            blue = (argb and 0xFF) / 255f,
                            alpha = (argb ushr 24 and 0xFF) / 255f,
                        ),
                    ),
        )
    } else {
        Image(
            painter = painterResource(id = pair.imageRes),
            contentDescription = pair.caption,
            modifier = modifier.clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}
