package com.tal.hebrewdino.ui.components.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.LessonWordIllustrations

/**
 * Shared sizing constants for picture+caption lesson cards.
 *
 * These are referenced from multiple stations/games to keep a unified design and to avoid
 * "caption gets clipped" issues when any screen estimates required layout height.
 */
const val LessonChoiceCardPictureAspect: Float = 121f / 160f // +~10% taller than the old 110/160
// Taller reserved band so Hebrew glyphs never clip on small devices.
val LessonChoiceCardCaptionAreaHeight: Dp = 66.dp
val LessonChoiceCardCaptionSpacerHeight: Dp = 8.dp

@Composable
fun LessonChoiceCard(
    choice: LessonChoice,
    enabled: Boolean,
    scale: Float = 1f,
    showWordCaption: Boolean = true,
    cardWidth: Dp,
    cardHeight: Dp,
    captionFontSize: TextUnit = 24.sp,
    innerPictureScale: Float = 1f,
    isCorrectPick: Boolean = false,
    isSelected: Boolean = false,
    /** 0..1 red flash overlay for wrong pick feedback. */
    wrongFlashAlpha: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val borderColor =
        if (wrongFlashAlpha > 0.01f) {
            Color(0xFFE53935).copy(alpha = 0.90f)
        } else if (isCorrectPick) {
            Color(0xFF2E7D32).copy(alpha = 0.95f)
        } else if (isSelected) {
            Color(0xFF2E7D32).copy(alpha = 0.70f)
        } else {
            Color(0xFF0B2B3D).copy(alpha = 0.15f)
        }
    val bgBrush =
        when {
            wrongFlashAlpha > 0.01f ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFCDD2).copy(alpha = 0.92f),
                        Color(0xFFFFEBEE).copy(alpha = 0.72f),
                    ),
                )
            isCorrectPick ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFC8E6C9).copy(alpha = 0.96f),
                        Color(0xFFA5D6A7).copy(alpha = 0.86f),
                    ),
                )
            isSelected ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F5E9).copy(alpha = 0.98f),
                        Color(0xFFC8E6C9).copy(alpha = 0.80f),
                    ),
                )
            else ->
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFFE8F5E9).copy(alpha = 0.45f),
                    ),
                )
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .scale(scale)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(22.dp),
                    clip = false,
                )
                .border(4.dp, borderColor, RoundedCornerShape(22.dp))
                .background(
                    bgBrush,
                    RoundedCornerShape(22.dp),
                )
                .clickable(enabled = enabled, onClick = onClick)
                .padding(12.dp),
    ) {
        if (choice.tileDrawable == R.drawable.lesson_word_tile) {
            Box(
                modifier =
                    Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((choice.tintArgb shr 16) and 0xFF) / 255f,
                                green = ((choice.tintArgb shr 8) and 0xFF) / 255f,
                                blue = (choice.tintArgb and 0xFF) / 255f,
                                alpha = 1f,
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = choice.letter,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D).copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .graphicsLayer {
                                scaleX = innerPictureScale
                                scaleY = innerPictureScale
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            }
                            .background(Color.White.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        } else if (choice.tileDrawable == R.drawable.lesson_pic_placeholder) {
            val emoji = LessonWordIllustrations.emojiForWord(choice.word)
            val emojiSp =
                with(density) {
                    (cardWidth.toPx() * 0.34f).coerceIn(40f * fontScale, 72f * fontScale).toSp()
                }
            Box(
                modifier =
                    Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Color(
                                red = ((choice.tintArgb shr 16) and 0xFF) / 255f,
                                green = ((choice.tintArgb shr 8) and 0xFF) / 255f,
                                blue = (choice.tintArgb and 0xFF) / 255f,
                                alpha = 1f,
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emoji,
                    fontSize = emojiSp,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.graphicsLayer {
                            scaleX = innerPictureScale
                            scaleY = innerPictureScale
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        },
                )
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .clip(RoundedCornerShape(18.dp))
                        // Unify illustration presentation: all vector pictures sit on the same soft
                        // background (also fixes medusa looking "cut out" vs others).
                        .background(Color.White.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = choice.tileDrawable),
                    contentDescription = choice.word,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = innerPictureScale
                                scaleY = innerPictureScale
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            },
                    // For our vector lesson illustrations, Fit keeps all pictures visually consistent
                    // inside the same card frame (no surprise cropping / perceived size jumps).
                    contentScale = ContentScale.Fit,
                )
            }
        }
        if (showWordCaption) {
            Spacer(modifier = Modifier.height(LessonChoiceCardCaptionSpacerHeight))
            val isPictureWord =
                choice.tileDrawable != R.drawable.lesson_word_tile
            val targetCaptionSize =
                if (isPictureWord) {
                    // Global tuning: picture-word captions read large; shrink baseline by ~10%,
                    // then AutoFit will further reduce for long words.
                    (captionFontSize.value * 0.85f).sp
                } else {
                    captionFontSize
                }
            // Keep *outer card height* identical across choices: reserve a fixed one-line caption area.
            // This must NOT depend on the word-specific target font size; otherwise cards get different heights.
            // Fixed caption band height: must be tall enough for the *largest* caption size
            // (otherwise glyphs get clipped on some devices).
            BoxWithConstraints(
                modifier =
                    Modifier
                        .width(cardWidth + 8.dp)
                        .height(LessonChoiceCardCaptionAreaHeight),
                // Keep the caption slightly higher so descenders don't get clipped on small devices.
                contentAlignment = Alignment.TopCenter,
            ) {
                AutoFitSingleLineText(
                    text = choice.word,
                    maxWidth = maxWidth,
                    targetFontSize = targetCaptionSize,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B2B3D),
                        ),
                    // Fill the reserved area so layout is stable; AutoFit handles font shrink.
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            // Picture-card captions: lift baseline ~20% within the reserved caption band
                            // so text stays visible and readable under the picture frame on small devices.
                            .offset(y = if (isPictureWord) (-0.20f * maxHeight.value).dp else 0.dp),
                    minFontSize = 10.sp,
                )
            }
        }
    }
}

