package com.tal.hebrewdino.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.layout.ScreenFit
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCardPictureAspect
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun ImageMatchGame(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    /** Subtle guidance pulse when the round appears (cards). */
    entryPulseEpoch: Int = 0,
    /** After 2 wrong taps, pulse the correct answer (subtle hint). */
    hintCorrectChoiceId: String? = null,
    hintPulseEpoch: Int = 0,
    onAttempt: (String) -> Boolean,
    /** When false, cards show only the picture (for pre-readers). */
    showWordCaptions: Boolean = true,
    captionSizeMultiplier: Float = 1f,
    pictureSizeMultiplier: Float = 1f,
    /** Scales only the illustration inside the card frame (card outer size unchanged). */
    innerPictureScaleForChoice: (LessonChoice) -> Float = { 1f },
    /** Optional saga context for [captionFontSizeForWordCard] (chapter/station tweaks). */
    chapterId: Int? = null,
    stationId: Int? = null,
    /** When set, shown above the target-letter chip (Episode 3 “which picture is this word?”). */
    headerInstructionText: String? = null,
    /** Scales the header instruction text (Episode 1/2 station 5 sizing tweaks). */
    headerInstructionFontScale: Float = 1.35f,
    /** Optional big, prominent word prompt (Episode 3 station 5). */
    headerPromptWord: String? = null,
    /** When false, hides the large letter chip (word-only header). */
    showTargetLetterChip: Boolean = true,
    /** Adjusts the letter chip vertical placement (Episode 1 station 5). */
    targetLetterChipOffsetYDp: Int = -10,
    /** Adds extra top padding before header (Episode 1 station 5). */
    headerTopPaddingDp: Int = 0,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var successChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableStateOf(0) }
    LaunchedEffect(contentKey) {
        successChoiceId = null
        wrongFlashChoiceId = null
        wrongFlashEpoch = 0
    }
    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
        ) {
            val rowInnerWidth = maxWidth
            val choiceCount = question.choices.size.coerceAtLeast(1)
            val cardGap = 10.dp
            var cardW =
                ScreenFit.rowChildWidthDp(
                    rowInnerWidth = rowInnerWidth,
                    count = choiceCount,
                    gap = cardGap,
                    minEach = 72.dp,
                    maxEach = 168.dp,
                )
            cardW = (cardW * pictureSizeMultiplier).coerceAtMost((rowInnerWidth - cardGap * (choiceCount - 1)) / choiceCount)
            val cardH = cardW * LessonChoiceCardPictureAspect
            val density = LocalDensity.current
            val narrowRow = rowInnerWidth < 380.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = headerTopPaddingDp.dp),
            ) {
                if (headerInstructionText != null) {
                    Text(
                        text = headerInstructionText,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize * headerInstructionFontScale,
                            ),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (headerPromptWord != null) {
                    Text(
                        text = headerPromptWord,
                        fontSize = if (rowInnerWidth < 420.dp) 52.sp else 60.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .background(Color(0xFFFFF59D).copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (showTargetLetterChip) {
                    // Station 5 (all chapters): match station 3 target-letter chip style, and sit a bit lower.
                    TargetLetterHeaderChip(
                        letter = question.targetLetter,
                        fontSize = if (rowInnerWidth < 420.dp) 52.sp else 56.sp,
                        modifier =
                            Modifier
                                .padding(top = 2.dp)
                                .offset(y = targetLetterChipOffsetYDp.dp),
                    )
                }
                Spacer(modifier = Modifier.height(if (narrowRow) 16.dp else 20.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(shakePx.roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(cardGap, Alignment.CenterHorizontally),
                ) {
                    question.choices.forEach { choice ->
                        val scale = remember(choice.id, contentKey) { Animatable(1f) }
                        val flash = remember(choice.id, contentKey) { Animatable(0f) }
                        LaunchedEffect(entryPulseEpoch, choice.id, contentKey) {
                            if (entryPulseEpoch <= 0) return@LaunchedEffect
                            // Very subtle guidance: tiny pulse once per round.
                            scale.animateTo(1.05f, tween(120))
                            scale.animateTo(1f, spring(dampingRatio = 0.70f, stiffness = 420f))
                        }
                        LaunchedEffect(hintPulseEpoch, hintCorrectChoiceId, choice.id, contentKey) {
                            if (hintPulseEpoch <= 0 || hintCorrectChoiceId != choice.id) return@LaunchedEffect
                            scale.snapTo(1f)
                            scale.animateTo(1.22f, tween(120))
                            scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
                        }
                        LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, choice.id, contentKey) {
                            if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != choice.id) return@LaunchedEffect
                            flash.snapTo(1f)
                            flash.animateTo(0f, tween(220))
                        }
                        val captionSp =
                            captionFontSizeForWordCard(
                                density = density,
                                cardWidth = cardW,
                                word = choice.word,
                                sizeMultiplier = captionSizeMultiplier,
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        LessonChoiceCard(
                            choice = choice,
                            enabled = enabled,
                            scale = scale.value,
                            showWordCaption = showWordCaptions,
                            cardWidth = cardW,
                            cardHeight = cardH,
                            captionFontSize = captionSp,
                            innerPictureScale = innerPictureScaleForChoice(choice),
                            onClick = {
                                val ok = onAttempt(choice.id)
                                if (ok) {
                                    successChoiceId = choice.id
                                    scope.launch {
                                        scale.snapTo(1f)
                                        scale.animateTo(1.28f, tween(100))
                                        scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 420f))
                                    }
                                } else {
                                    wrongFlashChoiceId = choice.id
                                    wrongFlashEpoch += 1
                                    scope.launch {
                                        flash.snapTo(1f)
                                        flash.animateTo(0f, tween(220))
                                    }
                                }
                            },
                            isCorrectPick = choice.id == successChoiceId,
                            wrongFlashAlpha = flash.value,
                        )
                    }
                }
            }
        }
    }
}
