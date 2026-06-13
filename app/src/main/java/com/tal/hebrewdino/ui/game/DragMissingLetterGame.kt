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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.domain.HebrewLetterOrder
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.gestures.DropTargetRegistry
import com.tal.hebrewdino.ui.gestures.hebrewDraggable
import com.tal.hebrewdino.ui.gestures.registerDropTarget
import com.tal.hebrewdino.ui.gestures.rememberDropTargetRegistry
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlin.math.roundToInt

const val DragMissingLetterRootTag = "drag_missing_letter_root"
const val DragMissingLetterSlotTag = "drag_missing_letter_slot"
const val DragMissingLetterChipTagPrefix = "drag_missing_letter_chip_"

/** ~0.5 cm below instruction row at baseline density (32dp). */
private val SideBySideWordDownOffset = 32.dp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DragMissingLetterGame(
    question: Question.DragMissingLetterQuestion,
    contentKey: Int,
    instructionText: String,
    enabled: Boolean,
    /** Ch5 st2: smaller picture beside partial word; letter bank pinned to bottom. */
    sideBySideLayout: Boolean = false,
    shakeEpoch: Int = 0,
    onPictureTapReplayWord: (() -> Unit)?,
    onLetterSelected: ((letter: String) -> Unit)? = null,
    onLetterPlaced: (letter: String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val compact = ScreenFit.isCompactLandscapePhone()
    val dropRegistry = rememberDropTargetRegistry()
    val density = LocalDensity.current
    val dropPaddingPx = with(density) { DragMissingLetterDropPolicy.DROP_HIT_PADDING_DP.dp.toPx() }
    val graphemes = remember(question.word) { question.word.map { it.toString() } }
    val sortedOptions = remember(question.optionLetters) {
        HebrewLetterOrder.sortForDisplay(question.optionLetters)
    }

    var filledLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var selectedLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var draggingLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var dragPosition by remember(contentKey) { mutableStateOf<Offset?>(null) }
    var dragRootAnchor by remember(contentKey) { mutableStateOf<Offset?>(null) }
    var dragChipSize by remember(contentKey) { mutableStateOf(IntSize.Zero) }
    var dragScreenAccum by remember(contentKey) { mutableStateOf(Offset.Zero) }
    var wrongFlashLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableIntStateOf(0) }
    val shakeOffset = remember { Animatable(0f) }

    val interactionEnabled = enabled && filledLetter == null

    LaunchedEffect(contentKey) {
        filledLetter = null
        selectedLetter = null
        draggingLetter = null
        dragPosition = null
        dragRootAnchor = null
        dragChipSize = IntSize.Zero
        dragScreenAccum = Offset.Zero
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
        dragRootAnchor = null
        dragChipSize = IntSize.Zero
        dragScreenAccum = Offset.Zero
    }

    fun updateDragPosition(rootAnchor: Offset, accumulatedScreenDelta: Offset) {
        dragPosition = rootAnchor + accumulatedScreenDelta
    }

    fun tryPlaceLetter(letter: String) {
        if (!interactionEnabled) return
        if (onLetterPlaced(letter)) {
            filledLetter = letter
            wrongFlashLetter = null
        } else {
            wrongFlashLetter = letter
            wrongFlashEpoch += 1
        }
        selectedLetter = null
        resetDrag()
    }

    fun selectLetter(letter: String) {
        if (!interactionEnabled || draggingLetter != null) return
        selectedLetter = letter
        onLetterSelected?.invoke(letter)
    }

    fun toggleLetterSelection(letter: String) {
        if (!interactionEnabled || draggingLetter != null) return
        if (selectedLetter == letter) {
            selectedLetter = null
        } else {
            selectLetter(letter)
        }
    }

    fun finishDragAt(position: Offset) {
        val letter = draggingLetter ?: return
        val onSlot =
            DragMissingLetterDropPolicy.isDropOnSlot(
                registry = dropRegistry,
                position = position,
                paddingPx = dropPaddingPx,
            )
        resetDrag()
        if (onSlot) {
            tryPlaceLetter(letter)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                ) * if (sideBySideLayout) 0.7f else 1f
            val cardH = cardW * LessonChoiceCardPictureAspect
            val letterFontSize = if (compact) 44.sp else 52.sp
            val slotSize = DragMissingLetterDropPolicy.slotSizeDp(compact)

            val choice =
                LessonChoice(
                    id = question.catalogEntryId,
                    letter = question.correctLetter,
                    word = question.word,
                    tintArgb = question.tintArgb,
                    tileDrawable = question.tileDrawable,
                )

            val screenPadding =
                if (sideBySideLayout) {
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = if (compact) 4.dp else 8.dp,
                        bottom = if (compact) 16.dp else 20.dp,
                    )
                } else {
                    PaddingValues(
                        start = 12.dp,
                        end = 80.dp,
                        top = if (compact) 4.dp else 8.dp,
                        bottom = if (compact) 12.dp else 16.dp,
                    )
                }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(screenPadding)
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                horizontalAlignment = if (sideBySideLayout) Alignment.CenterHorizontally else Alignment.End,
                verticalArrangement =
                    if (sideBySideLayout) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.Top
                    },
            ) {
                Column(
                    modifier = if (sideBySideLayout) Modifier.fillMaxWidth() else Modifier,
                    horizontalAlignment = if (sideBySideLayout) Alignment.CenterHorizontally else Alignment.End,
                ) {
                    if (sideBySideLayout) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = instructionText,
                                fontSize = if (compact) 20.sp else 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B2B3D),
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    } else {
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
                    }

                    if (sideBySideLayout) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        ) {
                            DragMissingLetterPartialWordRow(
                                graphemes = graphemes,
                                question = question,
                                filledLetter = filledLetter,
                                selectedLetter = selectedLetter,
                                interactionEnabled = interactionEnabled,
                                letterFontSize = letterFontSize,
                                slotSize = slotSize,
                                dropRegistry = dropRegistry,
                                onTryPlaceLetter = ::tryPlaceLetter,
                                modifier = Modifier.offset(y = SideBySideWordDownOffset),
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
                        }
                    } else {
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

                        DragMissingLetterPartialWordRow(
                            graphemes = graphemes,
                            question = question,
                            filledLetter = filledLetter,
                            selectedLetter = selectedLetter,
                            interactionEnabled = interactionEnabled,
                            letterFontSize = letterFontSize,
                            slotSize = slotSize,
                            dropRegistry = dropRegistry,
                            onTryPlaceLetter = ::tryPlaceLetter,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = rowWidth - 104.dp)
                                    .padding(bottom = if (compact) 10.dp else 16.dp),
                        )
                    }
                }

                val gap = 12.dp
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(gap),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = if (sideBySideLayout && compact) 6.dp else 0.dp),
                ) {
                    sortedOptions.forEach { letter ->
                        val isSelected = selectedLetter == letter
                        val isWrongFlash = wrongFlashLetter == letter
                        val isDraggingThis = draggingLetter == letter
                        var chipRootTopLeft by remember(letter, contentKey) { mutableStateOf(Offset.Zero) }
                        var chipSize by remember(letter, contentKey) { mutableStateOf(IntSize.Zero) }
                        val tapEnabled = interactionEnabled && draggingLetter == null
                        DragMissingLetterOptionChip(
                            letter = letter,
                            compact = compact,
                            selected = isSelected,
                            wrongFlash = isWrongFlash,
                            dragging = isDraggingThis,
                            modifier =
                                Modifier
                                    .testTag("$DragMissingLetterChipTagPrefix$letter")
                                    .onGloballyPositioned { coordinates ->
                                        chipRootTopLeft = coordinates.positionInRoot()
                                        chipSize = coordinates.size
                                    }
                                    .clickable(enabled = tapEnabled) {
                                        toggleLetterSelection(letter)
                                    }
                                    .hebrewDraggable(
                                        enabled = interactionEnabled,
                                        onDragStart = { localStart ->
                                            if (!interactionEnabled) return@hebrewDraggable
                                            wrongFlashLetter = null
                                            dragScreenAccum = Offset.Zero
                                            dragChipSize = chipSize
                                            val touchX = chipSize.width - localStart.x
                                            dragRootAnchor = chipRootTopLeft + Offset(touchX, localStart.y)
                                            dragRootAnchor?.let {
                                                updateDragPosition(it, dragScreenAccum)
                                            }
                                            selectedLetter = letter
                                            onLetterSelected?.invoke(letter)
                                            draggingLetter = letter
                                        },
                                        onDrag = { dragAmount ->
                                            dragScreenAccum += dragAmount
                                            dragRootAnchor?.let {
                                                updateDragPosition(it, dragScreenAccum)
                                            }
                                        },
                                        onDragEnd = {
                                            dragPosition?.let { finishDragAt(it) } ?: resetDrag()
                                        },
                                        onDragCancel = { resetDrag() },
                                    ),
                        )
                    }
                }
            }

            val ghostLetter = draggingLetter
            val ghostPosition = dragPosition
            if (ghostLetter != null && ghostPosition != null) {
                val fallbackGhostWidth = if (compact) 64.dp else 72.dp
                val fallbackGhostHeight = if (compact) 48.dp else 56.dp
                val ghostWidthPx =
                    if (dragChipSize.width > 0) {
                        dragChipSize.width.toFloat()
                    } else {
                        with(density) { fallbackGhostWidth.toPx() }
                    }
                val ghostHeightPx =
                    if (dragChipSize.height > 0) {
                        dragChipSize.height.toFloat()
                    } else {
                        with(density) { fallbackGhostHeight.toPx() }
                    }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                                    .offset {
                                        IntOffset(
                                            (ghostPosition.x - ghostWidthPx / 2f).roundToInt(),
                                            (ghostPosition.y - ghostHeightPx / 2f).roundToInt(),
                                        )
                                    }
                                    .widthIn(min = fallbackGhostWidth, max = fallbackGhostWidth + 16.dp)
                                    .background(Color(0xFFBBDEFB), RoundedCornerShape(14.dp))
                                    .border(3.dp, Color(0xFF1E88E5), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .graphicsLayer { alpha = 0.95f },
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
    }
}

@Composable
private fun DragMissingLetterOptionChip(
    letter: String,
    compact: Boolean,
    selected: Boolean,
    wrongFlash: Boolean,
    dragging: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when {
            wrongFlash -> Color(0xFFFFCDD2)
            selected -> Color(0xFFBBDEFB)
            else -> Color(0xFFECEFF1)
        }
    val borderColor =
        when {
            wrongFlash -> Color(0xFFE53935)
            selected || dragging -> Color(0xFF1E88E5)
            else -> Color(0xFF90A4AE).copy(alpha = 0.55f)
        }
    val borderWidth = if (selected || wrongFlash || dragging) 3.dp else 2.dp
    Box(
        modifier =
            modifier
                .graphicsLayer { alpha = if (dragging) 0.72f else 1f }
                .background(backgroundColor, RoundedCornerShape(14.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            fontSize = if (compact) 46.sp else 42.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DragMissingLetterPartialWordRow(
    graphemes: List<String>,
    question: Question.DragMissingLetterQuestion,
    filledLetter: String?,
    selectedLetter: String?,
    interactionEnabled: Boolean,
    letterFontSize: TextUnit,
    slotSize: Dp,
    dropRegistry: DropTargetRegistry,
    onTryPlaceLetter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            graphemes.forEachIndexed { index, grapheme ->
                if (index == question.missingIndex) {
                val slotLetter = filledLetter
                val slotSelected = selectedLetter != null && slotLetter == null
                Box(
                    modifier =
                        Modifier
                            .testTag(DragMissingLetterSlotTag)
                            .size(slotSize)
                            .registerDropTarget(
                                id = DragMissingLetterDropPolicy.SLOT_TARGET_ID,
                                registry = dropRegistry,
                                enabled = interactionEnabled,
                            )
                            .clickable(
                                enabled = interactionEnabled && selectedLetter != null,
                            ) {
                                selectedLetter?.let { onTryPlaceLetter(it) }
                            }
                            .border(
                                width = 3.dp,
                                color =
                                    when {
                                        slotLetter != null -> Color(0xFF43A047)
                                        slotSelected -> Color(0xFF1E88E5)
                                        else -> Color(0xFF546E7A)
                                    },
                                shape = RoundedCornerShape(14.dp),
                            )
                            .background(
                                when {
                                    slotLetter != null -> Color(0xFFE8F5E9)
                                    slotSelected -> Color(0xFFE3F2FD)
                                    else -> Color.White.copy(alpha = 0.96f)
                                },
                                RoundedCornerShape(14.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = slotLetter ?: "_",
                        fontSize = letterFontSize,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (slotLetter != null) {
                                Color(0xFF0B2B3D)
                            } else {
                                Color(0xFF90A4AE)
                            },
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
}
