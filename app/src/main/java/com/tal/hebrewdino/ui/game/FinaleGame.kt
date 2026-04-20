package com.tal.hebrewdino.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.layout.ScreenFit
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

    val shortSide = ScreenFit.shortSideDp()
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val rowW = maxWidth
            val hintCount = question.pairHints.size.coerceAtLeast(1)
            val hintImg =
                ((rowW - 16.dp) / hintCount * 0.72f).coerceIn(56.dp, 100.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                question.pairHints.forEach { hint ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FinaleHintImage(pair = hint, modifier = Modifier.size(hintImg))
                        hint.caption?.let { cap ->
                            Text(
                                cap,
                                fontSize = if (rowW < 380.dp) 13.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0B2B3D),
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp).widthIn(max = rowW * 0.42f),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val poolMax = question.letterPool.size.coerceAtLeast(4)
            val poolTile =
                ((rowW - 32.dp) / poolMax).coerceIn(40.dp, 56.dp)
            val letterFont =
                if (poolTile < 48.dp) 22.sp else 26.sp
            question.words.forEachIndexed { wi, word ->
                val start = question.words.take(wi).sumOf { it.length }
                val len = word.length.coerceAtLeast(1)
                val slotW: Dp =
                    ((rowW - 8.dp * (len - 1).coerceAtLeast(0)) / len).coerceIn(34.dp, 54.dp)
                val slotH = (slotW * 1.12f).coerceAtMost(shortSide * 0.14f).coerceAtLeast(44.dp)
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
                                    .width(slotW)
                                    .height(slotH)
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
                                fontSize = if (slotW < 44.dp) 22.sp else 26.sp,
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
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                pool.forEachIndexed { i, letter ->
                    val sel = i == selectedPoolIndex
                    Box(
                        modifier =
                            Modifier
                                .width(poolTile)
                                .height(poolTile)
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
                        Text(letter, fontSize = letterFont, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
                    }
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
