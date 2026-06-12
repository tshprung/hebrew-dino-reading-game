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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.tal.hebrewdino.ui.components.learning.AutoFitSingleLineText
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.gestures.DropTargetDisposable
import com.tal.hebrewdino.ui.gestures.hebrewDraggable
import com.tal.hebrewdino.ui.gestures.registerDropTarget
import com.tal.hebrewdino.ui.gestures.rememberDropTargetRegistry
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DragWordToPictureRootTag = "drag_word_to_picture_root"
private const val DragWordChipTagPrefix = "drag_word_chip_"
private const val DragPictureSlotTagPrefix = "drag_picture_slot_"

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DragWordToPictureGame(
    question: Question.DragWordToPictureQuestion,
    contentKey: Int,
    instructionText: String,
    enabled: Boolean,
    shakeEpoch: Int = 0,
    onPictureTapReplayWord: ((catalogEntryId: String) -> Unit)?,
    onDropAttempt: (wordCatalogId: String, pictureCatalogId: String) -> Boolean,
    onRoundComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val compact = ScreenFit.isCompactLandscapePhone()
    val dropRegistry = rememberDropTargetRegistry()
    val density = LocalDensity.current

    val lockedByPicture =
        remember(contentKey) { mutableStateMapOf<String, String>() }
    var selectedWordId by remember(contentKey) { mutableStateOf<String?>(null) }
    var draggingWordId by remember(contentKey) { mutableStateOf<String?>(null) }
    var dragPosition by remember(contentKey) { mutableStateOf<Offset?>(null) }
    var roundCompleteSent by remember(contentKey) { mutableStateOf(false) }
    var wrongFlashWordId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableIntStateOf(0) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(contentKey) {
        lockedByPicture.clear()
        selectedWordId = null
        draggingWordId = null
        dragPosition = null
        wrongFlashWordId = null
        wrongFlashEpoch = 0
        roundCompleteSent = false
    }

    LaunchedEffect(shakeEpoch) {
        if (shakeEpoch == 0) return@LaunchedEffect
        repeat(3) {
            shakeOffset.snapTo(8f)
            shakeOffset.animateTo(-8f, tween(45))
        }
        shakeOffset.animateTo(0f, tween(45))
    }

    LaunchedEffect(lockedByPicture.size, question.pairs.size) {
        if (
            !roundCompleteSent &&
                lockedByPicture.size == question.pairs.size &&
                question.pairs.isNotEmpty()
        ) {
            roundCompleteSent = true
            onRoundComplete()
        }
    }

    fun resetDrag() {
        draggingWordId = null
        dragPosition = null
    }

    fun attemptDrop(wordId: String, pictureId: String) {
        if (!enabled || wordId in lockedByPicture.values) return
        if (pictureId in lockedByPicture) return
        val accepted = onDropAttempt(wordId, pictureId)
        if (accepted) {
            lockedByPicture[pictureId] = wordId
            selectedWordId = null
            resetDrag()
        } else {
            wrongFlashWordId = wordId
            wrongFlashEpoch += 1
            selectedWordId = null
            resetDrag()
        }
    }

    fun finishDragAt(position: Offset) {
        val wordId = draggingWordId ?: return
        val pictureId = dropRegistry.findTarget(position)
        if (pictureId != null) {
            attemptDrop(wordId, pictureId)
        } else {
            wrongFlashWordId = wordId
            wrongFlashEpoch += 1
            resetDrag()
        }
    }

    fun updateDragPosition(chipRootTopLeft: Offset, localStart: Offset, accumulated: Offset) {
        dragPosition = chipRootTopLeft + localStart + accumulated
    }

    val availableWords =
        question.wordBank.filter { card -> card.catalogEntryId !in lockedByPicture.values }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(
            modifier =
                modifier
                    .fillMaxSize()
                    .testTag(DragWordToPictureRootTag),
        ) {
            val pictureCardWidth =
                when {
                    compact && question.pairs.size >= 3 -> (maxWidth * 0.26f).coerceAtMost(118.dp)
                    compact -> (maxWidth * 0.30f).coerceAtMost(132.dp)
                    question.pairs.size >= 3 -> (maxWidth * 0.24f).coerceAtMost(150.dp)
                    else -> (maxWidth * 0.28f).coerceAtMost(168.dp)
                }
            val pictureCardHeight = pictureCardWidth * 0.92f
            val wordChipHeight = if (compact) 44.dp else 52.dp

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (compact) 6.dp else 12.dp)
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = instructionText,
                    fontSize = if (compact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 4.dp else 8.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    question.pairs.forEach { pair ->
                        val choice =
                            LessonChoice(
                                id = pair.catalogEntryId,
                                letter = pair.word.firstOrNull()?.toString().orEmpty(),
                                word = pair.word,
                                tintArgb = pair.tintArgb,
                                tileDrawable = pair.tileDrawable,
                            )
                        val lockedWordId = lockedByPicture[pair.catalogEntryId]
                        val isTarget =
                            draggingWordId != null &&
                                dropRegistry.findTarget(dragPosition ?: Offset.Zero) == pair.catalogEntryId

                        Box(
                            modifier =
                                Modifier
                                    .widthIn(max = pictureCardWidth)
                                    .registerDropTarget(
                                        id = pair.catalogEntryId,
                                        registry = dropRegistry,
                                        enabled = enabled && lockedWordId == null,
                                    )
                                    .testTag("$DragPictureSlotTagPrefix${pair.catalogEntryId}")
                                    .clickable(enabled = enabled && selectedWordId != null && lockedWordId == null) {
                                        selectedWordId?.let { attemptDrop(it, pair.catalogEntryId) }
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            DropTargetDisposable(id = pair.catalogEntryId, registry = dropRegistry)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LessonChoiceCard(
                                    choice = choice,
                                    enabled = enabled && lockedWordId == null,
                                    showWordCaption = false,
                                    cardWidth = pictureCardWidth,
                                    cardHeight = pictureCardHeight,
                                    captionFontSize = 1.sp,
                                    onClick = {
                                        if (onPictureTapReplayWord != null && lockedWordId == null) {
                                            onPictureTapReplayWord(pair.catalogEntryId)
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .then(
                                                if (isTarget) {
                                                    Modifier.border(
                                                        width = 3.dp,
                                                        color = Color(0xFF43A047),
                                                        shape = RoundedCornerShape(16.dp),
                                                    )
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LockedWordChip(
                                    word =
                                        lockedWordId?.let { id ->
                                            question.wordBank.firstOrNull { it.catalogEntryId == id }?.word
                                        },
                                    width = pictureCardWidth,
                                    height = wordChipHeight,
                                    compact = compact,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = wordChipHeight + 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    availableWords.forEach { card ->
                        val isSelected = selectedWordId == card.catalogEntryId
                        val isWrongFlash = wrongFlashWordId == card.catalogEntryId
                        var chipRootTopLeft by remember(card.catalogEntryId, contentKey) {
                            mutableStateOf(Offset.Zero)
                        }
                        var dragLocalStart by remember(card.catalogEntryId, contentKey) {
                            mutableStateOf(Offset.Zero)
                        }
                        var dragAccum by remember(card.catalogEntryId, contentKey) {
                            mutableStateOf(Offset.Zero)
                        }
                        WordBankChip(
                            word = card.word,
                            compact = compact,
                            enabled = enabled && draggingWordId == null,
                            selected = isSelected,
                            wrongFlash = isWrongFlash,
                            wrongFlashEpoch = wrongFlashEpoch,
                            modifier =
                                Modifier
                                    .testTag("$DragWordChipTagPrefix${card.catalogEntryId}")
                                    .onGloballyPositioned { coordinates ->
                                        chipRootTopLeft = coordinates.positionInRoot()
                                    }
                                    .clickable(enabled = enabled && draggingWordId == null) {
                                        selectedWordId =
                                            if (selectedWordId == card.catalogEntryId) {
                                                null
                                            } else {
                                                card.catalogEntryId
                                            }
                                    }
                                    .hebrewDraggable(
                                        enabled = enabled,
                                        onDragStart = { localStart ->
                                            if (!enabled) return@hebrewDraggable
                                            draggingWordId = card.catalogEntryId
                                            selectedWordId = card.catalogEntryId
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
                        )
                    }
                }
            }

            val ghostWordId = draggingWordId
            val ghostPosition = dragPosition
            if (ghostWordId != null && ghostPosition != null) {
                val ghostWord =
                    question.wordBank.firstOrNull { it.catalogEntryId == ghostWordId }?.word.orEmpty()
                val chipWidth = if (compact) 96.dp else 112.dp
                val ghostOffsetX =
                    with(density) { (ghostPosition.x - chipWidth.toPx() / 2f).toDp() }
                val ghostOffsetY =
                    with(density) { (ghostPosition.y - 24.dp.toPx()).toDp() }
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .zIndex(10f),
                ) {
                    WordBankChip(
                        word = ghostWord,
                        compact = compact,
                        enabled = false,
                        selected = true,
                        wrongFlash = false,
                        wrongFlashEpoch = 0,
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .offset(x = ghostOffsetX, y = ghostOffsetY)
                                .widthIn(min = chipWidth, max = chipWidth + 24.dp)
                                .graphicsLayer { alpha = 0.92f },
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedWordChip(
    word: String?,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    compact: Boolean,
) {
    Box(
        modifier =
            Modifier
                .widthIn(min = width * 0.85f, max = width)
                .height(height)
                .background(
                    if (word != null) Color(0xFFE8F5E9) else Color.Transparent,
                    RoundedCornerShape(12.dp),
                )
                .border(
                    width = if (word != null) 2.dp else 0.dp,
                    color = Color(0xFF43A047).copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (word != null) {
            AutoFitSingleLineText(
                text = word,
                maxWidth = width - 12.dp,
                targetFontSize = if (compact) 20.sp else 24.sp,
                style =
                    androidx.compose.ui.text.TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20),
                    ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WordBankChip(
    word: String,
    compact: Boolean,
    enabled: Boolean,
    selected: Boolean,
    wrongFlash: Boolean,
    wrongFlashEpoch: Int,
    modifier: Modifier = Modifier,
) {
    val flashColor =
        if (wrongFlash) {
            Color(0xFFFFCDD2)
        } else if (selected) {
            Color(0xFFFFF9C4)
        } else {
            Color.White.copy(alpha = 0.95f)
        }
  val borderColor =
        when {
            wrongFlash -> Color(0xFFE53935)
            selected -> Color(0xFFFFA000)
            else -> Color(0xFF0B2B3D).copy(alpha = 0.18f)
        }

    LaunchedEffect(wrongFlashEpoch) {
        if (wrongFlashEpoch == 0) return@LaunchedEffect
    }

    Box(
        modifier =
            modifier
                .heightIn(min = if (compact) 40.dp else 48.dp)
                .background(flashColor, RoundedCornerShape(14.dp))
                .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        AutoFitSingleLineText(
            text = word,
            maxWidth = if (compact) 88.dp else 104.dp,
            targetFontSize = if (compact) 22.sp else 26.sp,
            style =
                androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B2B3D),
                ),
            textAlign = TextAlign.Center,
        )
    }
}
