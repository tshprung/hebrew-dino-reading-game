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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.LessonChoice
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MatchLetterToWordGame(
    choices: List<LessonChoice>,
    enabled: Boolean,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxPairs = choices.take(3)
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    val locked = remember { mutableStateMapOf<String, String>() } // letter -> choiceId
    val shake = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val letterRects = remember { mutableStateMapOf<String, Rect>() }
    val itemRects = remember { mutableStateMapOf<String, Rect>() } // choiceId -> rect

    LaunchedEffect(locked.size) {
        if (locked.size == maxPairs.size && maxPairs.isNotEmpty()) {
            glow.snapTo(0f)
            glow.animateTo(1f, tween(160))
            glow.animateTo(0f, tween(220))
            delay(120)
            onSolved()
        }
    }

    fun isLockedLetter(letter: String): Boolean = locked.containsKey(letter)
    fun isLockedChoice(choiceId: String): Boolean = locked.values.contains(choiceId)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val innerW = maxWidth
        val gap = 12.dp
        val colW = ((innerW - gap) / 2f).coerceAtLeast(140.dp)
        val tileH = 88.dp
        val tileShape = RoundedCornerShape(22.dp)

        Box(modifier = Modifier.fillMaxWidth()) {
            // Connection lines
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                locked.forEach { (letter, choiceId) ->
                    val lr = letterRects[letter]
                    val ir = itemRects[choiceId]
                    val a = lr?.let { Offset(it.left, it.center.y) }
                    val b = ir?.let { Offset(it.right, it.center.y) }
                    if (a != null && b != null) {
                        drawLine(
                            color = Color(0xFF2E7D32).copy(alpha = 0.92f),
                            start = a,
                            end = b,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round,
                        )
                        // small glow pulse
                        if (glow.value > 0f) {
                            drawLine(
                                color = Color(0xFFFFD54F).copy(alpha = 0.55f * glow.value),
                                start = a,
                                end = b,
                                strokeWidth = 18f,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }

            // RTL layout: letters on RIGHT, items on LEFT.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .offset { IntOffset(shake.value.roundToInt(), 0) },
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.Top,
            ) {
                // LEFT: items (image + word)
                Column(
                    modifier = Modifier.width(colW),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    maxPairs.forEach { ch ->
                        val lockedThis = isLockedChoice(ch.id)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier
                                    .width(colW)
                                    .background(Color.White.copy(alpha = 0.86f), tileShape)
                                    .border(3.dp, Color(0xFF0B2B3D).copy(alpha = if (lockedThis) 0.10f else 0.18f), tileShape)
                                    .clickable(enabled = enabled && !lockedThis && selectedLetter != null) {
                                        val picked = selectedLetter ?: return@clickable
                                        if (picked == ch.letter && !isLockedLetter(picked)) {
                                            locked[picked] = ch.id
                                            selectedLetter = null
                                        } else {
                                            selectedLetter = null
                                            scope.launch {
                                                shake.snapTo(0f)
                                                shake.animateTo(16f, tween(55))
                                                shake.animateTo(-12f, tween(55))
                                                shake.animateTo(8f, tween(55))
                                                shake.animateTo(0f, tween(80))
                                            }
                                        }
                                    }
                                    .padding(10.dp)
                                    .onGloballyPositioned { coords ->
                                        val p = coords.positionInRoot()
                                        itemRects[ch.id] = Rect(p, Size(coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    },
                        ) {
                            Image(
                                painter = painterResource(id = ch.tileDrawable),
                                contentDescription = ch.word,
                                modifier = Modifier.size(width = colW * 0.86f, height = 110.dp),
                                contentScale = ContentScale.Fit,
                                alpha = if (lockedThis) 0.55f else 1f,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.material3.Text(
                                text = ch.word,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // RIGHT: letters
                Column(
                    modifier = Modifier.width(colW),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    maxPairs.map { it.letter }.distinct().forEach { letter ->
                        val lockedThis = isLockedLetter(letter)
                        val selected = selectedLetter == letter
                        val scale = remember(letter) { Animatable(1f) }
                        LaunchedEffect(selected) {
                            if (selected) {
                                scale.snapTo(1f)
                                scale.animateTo(1.08f, spring(dampingRatio = 0.55f, stiffness = 520f))
                            } else {
                                scale.animateTo(1f, tween(120))
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .width(colW)
                                    .height(tileH)
                                    .scale(scale.value)
                                    .background(
                                        when {
                                            lockedThis -> Color(0xFF2E7D32).copy(alpha = 0.22f)
                                            selected -> Color(0xFFFFF3C4).copy(alpha = 0.92f)
                                            else -> Color.White.copy(alpha = 0.88f)
                                        },
                                        tileShape,
                                    )
                                    .border(
                                        4.dp,
                                        when {
                                            lockedThis -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                                            selected -> Color(0xFFFFC400)
                                            else -> Color(0xFF0B2B3D).copy(alpha = 0.16f)
                                        },
                                        tileShape,
                                    )
                                    .clickable(enabled = enabled && !lockedThis) {
                                        selectedLetter = if (selected) null else letter
                                    }
                                    .onGloballyPositioned { coords ->
                                        val p = coords.positionInRoot()
                                        letterRects[letter] = Rect(p, Size(coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Text(
                                text = letter,
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B2B3D),
                            )
                        }
                    }
                }
            }
        }
    }
}

