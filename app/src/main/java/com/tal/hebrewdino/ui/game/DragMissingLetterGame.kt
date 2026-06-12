package com.tal.hebrewdino.ui.game

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.domain.HebrewLetterOrder
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.gestures.hebrewDraggable
import com.tal.hebrewdino.ui.gestures.registerDropTarget
import com.tal.hebrewdino.ui.gestures.rememberDropTargetRegistry
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlin.math.roundToInt

private const val DragMissingLetterRootTag = "drag_missing_letter_root"
private const val DragMissingLetterSlotTag = "drag_missing_letter_slot"
private const val DragMissingLetterChipTagPrefix = "drag_missing_letter_chip_"
private const val MissingSlotTargetId = "missing_letter_slot"

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DragMissingLetterGame(
    question: Question.DragMissingLetterQuestion,
    contentKey: Int,
    instructionText: String,
    enabled: Boolean,
    shakeEpoch: Int = 0,
    onPictureTapReplayWord: (() -> Unit)?,
    onLetterPlaced: (letter: String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val compact = ScreenFit.isCompactLandscapePhone()
    val dropRegistry = rememberDropTargetRegistry()
    val density = LocalDensity.current
    val graphemes = remember(question.word) { question.word.map { it.toString() } }
    val sortedOptions = remember(question.optionLetters) {
        HebrewLetterOrder.sortForDisplay(question.optionLetters)
    }

    var filledLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var selectedLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var draggingLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var dragPosition by remember(contentKey) { mutableStateOf<Offset?>(null) }
    var wrongFlashLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableIntStateOf(0) }
    val shakeOffset = remember { Animatable(0f) }

    val interactionEnabled = enabled && filledLetter == null

    LaunchedEffect(contentKey) {
        filledLetter = null
        selectedLetter = null
        draggingLetter = null
        dragPosition = null
        wrongFlashLetter = null
        wrongFlashEpoch = 0
    }

    LaunchedEffect(shakeEpoch) {
        if (shakeEpoch == 0) return@LaunchedEffect
        repeat(3) {
            shakeOffset.snapTo(8f)
            shakeOffset.animateTo(-8f, tween(45))
        }
        shakeOffset.animateTo(0f, tween(45))
    }

    fun resetDrag() {
        draggingLetter = null
        dragPosition = null
    }

    fun updateDragPosition(chipRootTopLeft: Offset, localStart: Offset, dragAccum: Offset) {
        dragPosition = chipRootTopLeft + localStart + dragAccum
    }

    fun tryPlaceLetter(letter: String) {
        if (!interactionEnabled || draggingLetter != null) return
        if (onLetterPlaced(letter)) {
            filledLetter = letter
        } else {
            wrongFlashLetter = letter
            wrongFlashEpoch += 1
        }
        selectedLetter = null
    }

    fun finishDragAt(position: Offset) {
        val letter = draggingLetter ?: return
        val targetId = dropRegistry.findTarget(position)
        resetDrag()
        if (targetId == MissingSlotTargetId) {
            tryPlaceLetter(letter)
        }
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(DragMissingLetterRootTag),
    ) {
        val rowWidth = maxWidth
        val cardW =
            ScreenFit.rowChildWidthDp(
                rowInnerWidth = rowWidth,
                count = 1,
                gap = 0.dp,
                minEach = 120.dp,
                maxEach = 200.dp,
            )
        val cardH = cardW * LessonChoiceCardPictureAspect
        val letterFontSize = if (compact) 44.sp else 52.sp
        val slotMinSize = if (compact) 52.dp else 60.dp

        val choice =
            LessonChoice(
                id = question.catalogEntryId,
                letter = question.correctLetter,
                word = question.word,
                tintArgb = question.tintArgb,
                tileDrawable = question.tileDrawable,
            )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 12.dp,
                        end = 80.dp,
                        top = if (compact) 4.dp else 8.dp,
                        bottom = if (compact) 4.dp else 8.dp,
                    )
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = instructionText,
                fontSize = if (compact) 20.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.End,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )

            LessonChoiceCard(
                choice = choice,
                enabled = interactionEnabled && onPictureTapReplayWord != null,
                scale = 1f,
                showWordCaption = false,
                cardWidth = cardW,
                cardHeight = cardH,
                captionFontSize = 1.sp,
                innerPictureScale = if (compact) 1.4f else 1.2f,
                onClick = { onPictureTapReplayWord?.invoke() },
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 16.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = rowWidth - 104.dp)
                            .padding(bottom = if (compact) 10.dp else 16.dp),
                ) {
                    graphemes.forEachIndexed { index, grapheme ->
                        if (index == question.missingIndex) {
                            val slotLetter = filledLetter
                            val slotSelected = selectedLetter != null && slotLetter == null
                            Box(
                                modifier =
                                    Modifier
                                        .testTag(DragMissingLetterSlotTag)
                                        .widthIn(min = slotMinSize, max = slotMinSize + 12.dp)
                                        .heightIn(min = slotMinSize)
                                        .border(
                                            width = if (slotSelected) 3.dp else 2.dp,
                                            color =
                                                when {
                                                    slotLetter != null -> Color(0xFF43A047)
                                                    slotSelected -> Color(0xFF1E88E5)
                                                    else -> Color(0xFF90A4AE)
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                        )
                                        .background(
                                            when {
                                                slotLetter != null -> Color(0xFFE8F5E9)
                                                slotSelected -> Color(0xFFE3F2FD)
                                                else -> Color(0xFFF5F5F5)
                                            },
                                            RoundedCornerShape(12.dp),
                                        )
                                        .clickable(
                                            enabled = interactionEnabled && selectedLetter != null,
                                        ) {
                                            selectedLetter?.let { tryPlaceLetter(it) }
                                        }
                                        .registerDropTarget(
                                            id = MissingSlotTargetId,
                                            registry = dropRegistry,
                                            enabled = interactionEnabled,
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = slotLetter ?: "_",
                                    fontSize = letterFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B2B3D),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            }
                        } else {
                            Text(
                                text = grapheme,
                                fontSize = letterFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 2.dp),
                            )
                        }
                    }
                }
            }

            val gap = 12.dp
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                sortedOptions.forEach { letter ->
                    val isSelected = selectedLetter == letter
                    val isWrongFlash = wrongFlashLetter == letter
                    var chipRootTopLeft by remember(letter, contentKey) { mutableStateOf(Offset.Zero) }
                    var dragLocalStart by remember(letter, contentKey) { mutableStateOf(Offset.Zero) }
                    var dragAccum by remember(letter, contentKey) { mutableStateOf(Offset.Zero) }
                    val chipEnabled = interactionEnabled && draggingLetter == null
                    val letterChipSize = if (compact) 46.sp else 42.sp
                    Button(
                        onClick = {
                            if (!chipEnabled) return@Button
                            selectedLetter = if (isSelected) null else letter
                        },
                        enabled = chipEnabled,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    when {
                                        isSelected -> Color(0xFFBBDEFB)
                                        isWrongFlash -> Color(0xFFFFCDD2)
                                        else -> Color(0xFFECEFF1)
                                    },
                            ),
                        contentPadding =
                            androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                        modifier =
                            Modifier
                                .testTag("$DragMissingLetterChipTagPrefix$letter")
                                .border(
                                    width = if (isSelected) 3.dp else if (isWrongFlash) 3.dp else 0.dp,
                                    color =
                                        when {
                                            isSelected -> Color(0xFF1E88E5)
                                            isWrongFlash -> Color(0xFFE53935).copy(alpha = 0.7f)
                                            else -> Color.Transparent
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .onGloballyPositioned { coordinates ->
                                    chipRootTopLeft = coordinates.positionInRoot()
                                }
                                .hebrewDraggable(
                                    enabled = chipEnabled,
                                    onDragStart = { localStart ->
                                        if (!chipEnabled) return@hebrewDraggable
                                        draggingLetter = letter
                                        selectedLetter = letter
                                        dragLocalStart = localStart
                                        dragAccum = Offset.Zero
                                        updateDragPosition(chipRootTopLeft, dragLocalStart, dragAccum)
                                    },
                                    onDrag = { dragAmount ->
                                        dragAccum += dragAmount
                                        updateDragPosition(chipRootTopLeft, dragLocalStart, dragAccum)
                                    },
                                    onDragEnd = {
                                        dragPosition?.let { finishDragAt(it) } ?: resetDrag()
                                    },
                                    onDragCancel = { resetDrag() },
                                ),
                    ) {
                        Text(
                            text = letter,
                            fontSize = letterChipSize,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0B2B3D),
                        )
                    }
                }
            }
        }

        val ghostLetter = draggingLetter
        val ghostPosition = dragPosition
        if (ghostLetter != null && ghostPosition != null) {
            val chipWidth = if (compact) 64.dp else 72.dp
            val ghostOffsetX = with(density) { (ghostPosition.x - chipWidth.toPx() / 2f).toDp() }
            val ghostOffsetY = with(density) { (ghostPosition.y - 24.dp.toPx()).toDp() }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .zIndex(10f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset(x = ghostOffsetX, y = ghostOffsetY)
                            .widthIn(min = chipWidth, max = chipWidth + 16.dp)
                            .background(Color(0xFFBBDEFB), RoundedCornerShape(14.dp))
                            .border(2.dp, Color(0xFF1E88E5), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .graphicsLayer { alpha = 0.92f },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ghostLetter,
                        fontSize = if (compact) 46.sp else 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                    )
                }
            }
        }
    }
}
