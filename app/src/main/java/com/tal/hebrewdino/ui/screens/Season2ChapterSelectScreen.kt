package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlin.math.max

private val MuseumWallTop = Color(0xFFF4EDE3)
private val MuseumWallBottom = Color(0xFFE6D9C8)
private val TitleBrown = Color(0xFF4A3A2E)
private val CardIvoryTop = Color(0xFFFFFBF2)
private val CardIvoryBottom = Color(0xFFF2E8DA)
private val CardOutline = Color(0xFF7B6858).copy(alpha = 0.35f)
private val PremiumGold = Color(0xFFCF9D4A)
private val NextGlow = Color(0xFF2E7D32)

private enum class ChapterState { Locked, Unlocked, Completed }

private data class Season2ChapterCard(
    val chapterIndex: Int,
    val hebName: String,
    val thumbRes: Int,
    val state: ChapterState,
    val isFlying: Boolean = false,
)

@Composable
fun Season2ChapterSelectScreen(
    onBack: () -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }
    val completedChapters by season2Progress.completedChaptersFlow.collectAsState(initial = emptySet())

    val nextSuggestedChapter: Int? = (1..6).firstOrNull { it !in completedChapters }

    val chapters =
        listOf(
            Season2ChapterCard(
                1,
                "טירנוזאורוס",
                R.drawable.season2_trex_thumb,
                if (1 in completedChapters) ChapterState.Completed else if (nextSuggestedChapter == 1) ChapterState.Unlocked else ChapterState.Locked,
            ),
            Season2ChapterCard(
                2,
                "טריצרטופס",
                R.drawable.season2_triceratops_thumb,
                if (2 in completedChapters) ChapterState.Completed else if (nextSuggestedChapter == 2) ChapterState.Unlocked else ChapterState.Locked,
            ),
            Season2ChapterCard(
                3,
                "סטגוזאורוס",
                R.drawable.season2_stegosaurus_thumb,
                if (3 in completedChapters) ChapterState.Completed else if (nextSuggestedChapter == 3) ChapterState.Unlocked else ChapterState.Locked,
            ),
            Season2ChapterCard(
                4,
                "ברכיוזאורוס",
                R.drawable.season2_brachiosaurus_thumb,
                if (4 in completedChapters) ChapterState.Completed else if (nextSuggestedChapter == 4) ChapterState.Unlocked else ChapterState.Locked,
            ),
            Season2ChapterCard(
                5,
                "אנקילוזאורוס",
                R.drawable.season2_ankylosaurus_thumb,
                if (5 in completedChapters) ChapterState.Completed else if (nextSuggestedChapter == 5) ChapterState.Unlocked else ChapterState.Locked,
            ),
            Season2ChapterCard(
                6,
                "פטרנודון",
                R.drawable.season2_pteranodon_thumb,
                if (6 in completedChapters) ChapterState.Completed else if (nextSuggestedChapter == 6) ChapterState.Unlocked else ChapterState.Locked,
                isFlying = true,
            ),
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MuseumWallTop, MuseumWallBottom))),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackPill(onClick = onBack)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = rtl("עונה 2 · בחר דינוזאור"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TitleBrown,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val gridW = maxWidth
                val gridH = maxHeight
                val hGap = 10.dp
                val vGap = 10.dp
                val cellW = (gridW - hGap * 2) / 3f
                val cellH = (gridH - vGap) / 2f
                val targetCardH = max(120.dp.value, cellH.value).dp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(vGap),
                    horizontalArrangement = Arrangement.spacedBy(hGap),
                ) {
                    items(chapters) { chapter ->
                        ChapterCardView(
                            chapter = chapter,
                            onClick = {
                                if (chapter.state != ChapterState.Locked) onOpenChapter(chapter.chapterIndex)
                            },
                            isNextSuggested = chapter.state == ChapterState.Unlocked && chapter.chapterIndex == nextSuggestedChapter,
                            modifier =
                                Modifier
                                    .width(cellW)
                                    .height(targetCardH),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.55f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = rtl("חזור"),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TitleBrown,
        )
    }
}

@Composable
private fun ChapterCardView(
    chapter: Season2ChapterCard,
    onClick: () -> Unit,
    isNextSuggested: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val enabled = chapter.state != ChapterState.Locked
    val cardAlpha = if (enabled) 1f else 0.72f

    val cardBg =
        if (chapter.isFlying) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFBF6EA),
                    Color(0xFFE9F1FF).copy(alpha = 0.95f),
                ),
            )
        } else {
            Brush.verticalGradient(listOf(CardIvoryTop, CardIvoryBottom))
        }

    val outline =
        when (chapter.state) {
            ChapterState.Completed -> PremiumGold.copy(alpha = 0.65f)
            ChapterState.Unlocked -> CardOutline
            ChapterState.Locked -> CardOutline.copy(alpha = 0.22f)
        }

    val pulse = rememberInfiniteTransition(label = "nextPulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.70f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1_650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glowAlpha",
    )
    val glowWidth by pulse.animateFloat(
        initialValue = 3.5f,
        targetValue = 5.5f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1_650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glowWidth",
    )

    Surface(
        modifier =
            modifier
                .defaultMinSize(minHeight = 120.dp)
                .shadow(elevation = if (enabled) 8.dp else 4.dp, shape = shape, clip = false)
                .clip(shape)
                .background(cardBg)
                .border(1.5.dp, outline, shape)
                .then(
                    if (enabled && isNextSuggested) {
                        // Draw pulse ON TOP (not under border/clip) so it's obvious to kids.
                        Modifier.drawWithContent {
                            drawContent()
                            val stroke = glowWidth.dp.toPx()
                            val inset = stroke / 2f
                            drawRoundRect(
                                color = NextGlow.copy(alpha = glowAlpha),
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(18.dp.toPx() - inset),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                            )
                        }
                    } else {
                        Modifier
                    },
                )
                .alpha(cardAlpha)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(8.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Reserve space: image takes remaining height, labels stay visible.
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center,
            ) {
                val lockedFilter =
                    rememberLockedThumbFilter()
                val shouldGray = chapter.state != ChapterState.Completed
                val colorFilter = if (shouldGray) lockedFilter else null

                if (chapter.chapterIndex == 1) {
                    // T-Rex: use the real poster image (grayed) instead of the abstract placeholder.
                    val trexGray =
                        remember {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0.08f) },
                            )
                        }
                    Image(
                        painter = painterResource(id = R.drawable.season2_trex_puzzle_full),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (shouldGray) trexGray else null,
                    )
                } else if (chapter.chapterIndex == 2) {
                    // Triceratops: use the real poster image as the thumb (fixes "missing" thumbnail).
                    Image(
                        painter = painterResource(id = R.drawable.season2_triceratops_puzzle_full),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = colorFilter,
                    )
                } else {
                    Image(
                        painter = painterResource(id = chapter.thumbRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = colorFilter,
                    )
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.60f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = rtl("פרק ${chapter.chapterIndex}"),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = TitleBrown,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                }

                if (chapter.isFlying) {
                    Text(
                        text = rtl("מעופף"),
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color(0xFF243B53).copy(alpha = 0.32f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                CompletionTag(
                    state = chapter.state,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = rtl(chapter.hebName),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TitleBrown,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CompletionTag(
    state: ChapterState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ChapterState.Completed -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.62f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(PremiumGold.copy(alpha = 0.95f)),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rtl("הושלם"),
                        color = TitleBrown,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
        ChapterState.Locked -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.22f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = rtl("נעול"),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        ChapterState.Unlocked -> Unit
    }
}

@Composable
private fun rememberLockedThumbFilter(): ColorFilter {
    // Locked = calm fossil: very low saturation + slight darkening.
    val m =
        ColorMatrix().apply {
            setToSaturation(0.12f)
            // Multiply RGB a little darker (simple, stable).
            val darken = ColorMatrix(
                floatArrayOf(
                    0.82f, 0f, 0f, 0f, 0f,
                    0f, 0.82f, 0f, 0f, 0f,
                    0f, 0f, 0.82f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
            timesAssign(darken)
        }
    return ColorFilter.colorMatrix(m)
}

private fun rtl(text: String): String = "\u200F$text"

