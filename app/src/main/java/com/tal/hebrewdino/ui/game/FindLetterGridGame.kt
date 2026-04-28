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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.Question
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Interactive grid for [Question.FindLetterGridQuestion]: tap all cells matching [Question.FindLetterGridQuestion.targetLetter].
 * Data model is read-only; parent handles [LevelSession] via [onCellTapped] / [onCompleted].
 */
@Composable
fun FindLetterGridGame(
    question: Question.FindLetterGridQuestion,
    /** Called for any tap (correct or wrong) with the tapped letter. */
    onLetterTapped: ((letter: String) -> Unit)? = null,
    /** When incremented, triggers a subtle pulse hint on the header chip. */
    hintPulseEpoch: Int = 0,
    /** Header hint peak scale (2nd wrong hint). */
    hintHeaderPeakScale: Float = 1.12f,
    onCellTapped: (index: Int) -> Unit,
    onCompleted: () -> Unit,
    enabled: Boolean = true,
    contentKey: Int = 0,
    /** Scales only the letter glyphs inside grid cells, not cell size or the header chip. */
    gridLetterSizeMultiplier: Float = 1f,
    /** Correct cell “pop” peak scale. */
    correctCellPeakScale: Float = 1.12f,
    /** Scales grid cell edge length after layout (1f = default). */
    cellSideScale: Float = 1f,
    /** Moves header chip + grid down as a fraction of parent max height (e.g. 0.05f). */
    contentNudgeDownFraction: Float = 0f,
    /** Episode 3: context word (“find a letter that appears in this word”). */
    contextWordHint: String? = null,
    /** When [contextWordHint] is set: hide the large explicit target letter in the header (letter is only in the grid). */
    suppressHeaderTargetLetter: Boolean = false,
    /** When set (Episode 1 station 3), shows an inline instruction next to the target letter. */
    inlineInstructionText: String? = null,
    /** Optional saga context for chapter-specific UI tweaks. */
    chapterId: Int? = null,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val targetIndices =
        remember(question, contentKey) {
            question.cells.mapIndexedNotNull { i, c -> if (c == question.targetLetter) i else null }.toSet()
        }
    var found by remember(question, contentKey) { mutableStateOf<Set<Int>>(emptySet()) }
    val scales = remember(question, contentKey) { List(question.cells.size) { Animatable(1f) } }
    val cellShake = remember(question, contentKey) { List(question.cells.size) { Animatable(0f) } }
    val headerScale = remember(question, contentKey) { Animatable(1f) }
    val gridScale = remember(question, contentKey) { Animatable(1f) }
    var wrongFlashIndex by remember(question, contentKey) { mutableIntStateOf(-1) }
    var completionFired by remember(question, contentKey) { mutableStateOf(false) }
    /** True as soon as the last required cell is tapped — blocks extra taps before [onCompleted] runs (avoids stuck rounds). */
    var gridFrozen by remember(question, contentKey) { mutableStateOf(false) }

    LaunchedEffect(question, contentKey) {
        found = emptySet()
        scales.forEach { it.snapTo(1f) }
        cellShake.forEach { it.snapTo(0f) }
        headerScale.snapTo(1f)
        gridScale.snapTo(1f)
        wrongFlashIndex = -1
        completionFired = false
        gridFrozen = false
    }

    LaunchedEffect(hintPulseEpoch, question, contentKey) {
        if (hintPulseEpoch <= 0 || completionFired) return@LaunchedEffect
        headerScale.snapTo(1f)
        headerScale.animateTo(hintHeaderPeakScale, tween(120))
        headerScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
    }

    LaunchedEffect(found, question, contentKey) {
        if (found.isEmpty() || found != targetIndices || completionFired) return@LaunchedEffect
        completionFired = true
        coroutineScope {
            launch {
                headerScale.animateTo(1.14f, tween(140))
                headerScale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 380f))
            }
            launch {
                gridScale.animateTo(1.05f, tween(120))
                gridScale.animateTo(1f, spring(dampingRatio = 0.58f, stiffness = 450f))
            }
        }
        // UX: slightly faster completion transition (kids lose attention fast).
        delay(210)
        onCompleted()
    }

    val gap: Dp = if (question.columns >= 4) 5.dp else 7.dp
    val gridHorizontalPadding = 6.dp
    val cellScale = cellSideScale.coerceIn(0.5f, 1.5f)
    val nudgeFrac = contentNudgeDownFraction.coerceIn(0f, 0.2f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val nudgeDown = maxHeight * nudgeFrac
        val headerBoxH =
            when {
                contextWordHint != null && suppressHeaderTargetLetter && question.columns >= 4 -> 88.dp
                contextWordHint != null && suppressHeaderTargetLetter -> 96.dp
                contextWordHint != null && question.columns >= 4 -> 118.dp
                contextWordHint != null -> 128.dp
                question.columns >= 4 -> 74.dp
                else -> 82.dp
            }
        val topPaddingH = 6.dp
        val outerW = maxWidth
        // Small safety margin so the last row never clips in landscape.
        val availableForGrid = (maxHeight - headerBoxH - topPaddingH - 6.dp).coerceAtLeast(120.dp)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .offset(y = nudgeDown),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (contextWordHint == null && inlineInstructionText != null) {
                Text(
                    text = inlineInstructionText,
                    fontSize = if (question.columns >= 4) 27.sp else 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(top = 6.dp, bottom = 6.dp)
                            .then(
                                if (chapterId == 3) {
                                    Modifier
                                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                } else {
                                    Modifier
                                },
                            ),
                )
            }
            Box(
                modifier =
                    Modifier
                        .scale(headerScale.value)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFFF59D).copy(alpha = 0.95f),
                                        Color(0xFFFFE082).copy(alpha = 0.88f),
                                    ),
                                ),
                        )
                        .border(2.dp, Color(0xFFFFA000).copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (contextWordHint != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (suppressHeaderTargetLetter) {
                            Text(
                                text = "מצאו את האות שמופיעה במילה $contextWordHint",
                                fontSize = if (question.columns >= 4) 15.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0B2B3D).copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            Text(
                                text = "מצאו את האות שמופיעה במילה:",
                                fontSize = if (question.columns >= 4) 14.sp else 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0B2B3D).copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contextWordHint,
                                fontSize = if (question.columns >= 4) 26.sp else 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = question.targetLetter,
                                fontSize = if (question.columns >= 4) 48.sp else 56.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    Text(
                        text = question.targetLetter,
                        fontSize = if (question.columns >= 4) 52.sp else 60.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(availableForGrid)
                        .padding(horizontal = gridHorizontalPadding)
                        .scale(gridScale.value),
            ) {
                val cols = question.columns
                val rows = question.rows
                val safeMaxHeight = availableForGrid
                val cellSideByWidth = (outerW - gridHorizontalPadding * 2 - gap * (cols - 1)) / cols
                val cellSideByHeight = (safeMaxHeight - gap * (rows - 1)) / rows
                val cellSide =
                    // Fill the shorter dimension (width/height) while staying symmetric.
                    minOf(cellSideByWidth, cellSideByHeight).coerceAtLeast(32.dp) * cellScale
                val gridW = cellSide * cols + gap * (cols - 1)
                val gridH = cellSide * rows + gap * (rows - 1)
                // Keep letters readable; shrink the *squares* to fit landscape. Scale glyphs only via [gridLetterSizeMultiplier].
                val letterSp =
                    ((if (cols >= 4) 34f else 40f) * gridLetterSizeMultiplier.coerceIn(0.75f, 1.75f)).sp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically),
                    userScrollEnabled = false,
                    modifier = Modifier.align(Alignment.TopCenter).size(gridW, gridH),
                ) {
                itemsIndexed(question.cells, key = { i, _ -> "${contentKey}_$i" }) { index, letter ->
                    val done = index in found
                    val scale = scales[index]
                    val shake = cellShake[index]
                    val showWrongFlash = wrongFlashIndex == index
                    Box(
                        modifier =
                            Modifier
                                .aspectRatio(1f)
                                .shadow(
                                    elevation = if (done) 8.dp else 6.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    clip = false,
                                )
                                .scale(scale.value)
                                .offset { IntOffset(shake.value.roundToInt(), 0) }
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (done) 3.5.dp else 2.dp,
                                    color =
                                        when {
                                            done -> Color(0xFF2E7D32).copy(alpha = 0.95f)
                                            else -> Color(0xFFFF8A65).copy(alpha = 0.35f)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .background(
                                    brush =
                                        if (done) {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFE8F5E9).copy(alpha = 0.98f),
                                                    Color(0xFF81C784).copy(alpha = 0.65f),
                                                ),
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFFFDE7).copy(alpha = 0.98f),
                                                    Color(0xFFFFF9C4).copy(alpha = 0.75f),
                                                    Color(0xFFFFECB3).copy(alpha = 0.55f),
                                                ),
                                            )
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable(enabled = enabled && !done && !gridFrozen) {
                                    onLetterTapped?.invoke(letter)
                                    if (letter == question.targetLetter) {
                                        val newFound = found + index
                                        found = newFound
                                        if (newFound == targetIndices) {
                                            gridFrozen = true
                                        }
                                        ChildGameAudioHooks.onCorrect()
                                        scope.launch {
                                            scale.snapTo(1f)
                                            scale.animateTo(correctCellPeakScale, tween(100))
                                            scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 400f))
                                        }
                                    } else {
                                        ChildGameAudioHooks.onWrong()
                                        onCellTapped(index)
                                        wrongFlashIndex = index
                                        scope.launch {
                                            delay(200)
                                            if (wrongFlashIndex == index) wrongFlashIndex = -1
                                        }
                                        scope.launch {
                                            val amp = 11f
                                            repeat(5) { i ->
                                                shake.animateTo(
                                                    if (i % 2 == 0) amp else -amp,
                                                    tween(44),
                                                )
                                            }
                                            shake.animateTo(0f, tween(55))
                                        }
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (showWrongFlash) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE53935).copy(alpha = 0.22f), RoundedCornerShape(16.dp)),
                            )
                        }
                        Text(
                            text = letter,
                            fontSize = letterSp,
                            fontWeight = FontWeight.Black,
                            color =
                                if (done) {
                                    Color(0xFF1B5E20)
                                } else {
                                    Color(0xFF0B2B3D)
                                },
                            textAlign = TextAlign.Center,
                        )
                        if (done) {
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(12.dp)
                                        .background(Color.White.copy(alpha = 0.92f), CircleShape)
                                        .border(1.5.dp, Color(0xFF2E7D32).copy(alpha = 0.5f), CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
