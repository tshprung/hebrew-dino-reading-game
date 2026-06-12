package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import com.tal.hebrewdino.ui.domain.Season2ChapterRegistry
import com.tal.hebrewdino.ui.domain.Season2IntroFlow
import com.tal.hebrewdino.ui.domain.Season2Copy
import com.tal.hebrewdino.ui.domain.Season2StandardRevealOrder
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlin.math.max

private val TitleBrown = Color(0xFF4A3A2E)
private val CardIvoryTop = Color(0xFFFFFBF2)
private val CardIvoryBottom = Color(0xFFF2E8DA)
private val CardOutline = Color(0xFF7B6858).copy(alpha = 0.35f)
private val PremiumGold = Color(0xFFCF9D4A)
private val NextGlow = Color(0xFF2E7D32)
private val SoonGlow = Color(0xFF7B5BA8)

private const val DISPLAY_CHAPTER_COUNT = Season2ChapterRegistry.CHAPTER_COUNT

private enum class ChapterState { Locked, Unlocked, Completed }

private data class Season2ChapterCard(
    val chapterIndex: Int,
    val displayLabel: String,
    val posterResId: Int?,
    val state: ChapterState,
    val isComingSoon: Boolean = false,
    val isPrerequisiteLocked: Boolean = false,
)

@Composable
fun Season2ChapterSelectScreen(
    companionCharacter: DinoCharacter,
    requestSeasonIntro: Boolean = false,
    onSeasonIntroConsumed: () -> Unit = {},
    onBack: () -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }
    val completedChapters by season2Progress.completedChaptersFlow.collectAsState(initial = emptySet())
    val chapter1Stations by season2Progress.completedStationsFlow(1).collectAsState(initial = emptySet())
    val chapter2Stations by season2Progress.completedStationsFlow(2).collectAsState(initial = emptySet())
    var showSeasonIntro by remember { mutableStateOf(false) }

    LaunchedEffect(requestSeasonIntro) {
        if (Season2IntroFlow.shouldShowSeasonIntro(requestSeasonIntro)) {
            showSeasonIntro = true
            onSeasonIntroConsumed()
        }
    }

    fun stationsForChapter(index: Int): Set<Int> =
        when (index) {
            1 -> chapter1Stations
            2 -> chapter2Stations
            else -> emptySet()
        }

    LaunchedEffect(chapter1Stations, chapter2Stations, completedChapters) {
        for (index in Season2ChapterRegistry.playableChapterIndices()) {
            val stations = stationsForChapter(index)
            if (
                stations.size >= Season2StandardRevealOrder.STATION_COUNT &&
                index !in completedChapters
            ) {
                season2Progress.markChapterCompleted(index)
            }
        }
    }

    val nextSuggestedChapter =
        remember(completedChapters, chapter1Stations, chapter2Stations) {
            Season2ChapterRegistry.playableChapterIndices().firstOrNull { index ->
                Season2ChapterRegistry.isChapterUnlocked(index, completedChapters) &&
                    !Season2Copy.isChapterComplete(
                        chapterIndex = index,
                        completedChapters = completedChapters,
                        completedStations = stationsForChapter(index),
                    )
            }
        }

    val chapters =
        remember(completedChapters, chapter1Stations, chapter2Stations) {
            (1..DISPLAY_CHAPTER_COUNT).map { index ->
                val isPlayable = Season2ChapterRegistry.isPlayable(index)
                val isUnlocked = Season2ChapterRegistry.isChapterUnlocked(index, completedChapters)
                val completed =
                    Season2Copy.isChapterComplete(
                        chapterIndex = index,
                        completedChapters = completedChapters,
                        completedStations = stationsForChapter(index),
                    )
                val state =
                    when {
                        !isPlayable || !isUnlocked -> ChapterState.Locked
                        completed -> ChapterState.Completed
                        else -> ChapterState.Unlocked
                    }
                Season2ChapterCard(
                    chapterIndex = index,
                    displayLabel = Season2Copy.chapterSelectLabel(index, completed),
                    posterResId = Season2ChapterRegistry.posterResId(index),
                    state = state,
                    isComingSoon = !isPlayable,
                    isPrerequisiteLocked = isPlayable && !isUnlocked,
                )
            }
        }

    Box(modifier = modifier.fillMaxSize()) {
        Season2TreasureMapBackground()
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
                Column {
                    Text(
                        text = "\u200F${Season2Copy.ChapterSelectTitle}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TitleBrown,
                    )
                    Text(
                        text = "\u200F${Season2Copy.SeasonSubtitle}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TitleBrown.copy(alpha = 0.78f),
                    )
                }
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
                            isNextSuggested =
                                chapter.state == ChapterState.Unlocked &&
                                    chapter.chapterIndex == nextSuggestedChapter,
                            isComingSoonTeaser =
                                chapter.isComingSoon &&
                                    chapter.chapterIndex == Season2ChapterRegistry.playableChapterIndices().maxOrNull()?.plus(1),
                            modifier =
                                Modifier
                                    .width(cellW)
                                    .height(targetCardH),
                        )
                    }
                }
            }
        }

        if (showSeasonIntro) {
            Season2SeasonIntroOverlay(
                companionCharacter = companionCharacter,
                onContinue = { showSeasonIntro = false },
            )
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
            text = "\u200Fחזור",
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
    isComingSoonTeaser: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val enabled = chapter.state != ChapterState.Locked
    val cardAlpha = if (enabled) 1f else 0.78f
    val cardBg = Brush.verticalGradient(listOf(CardIvoryTop, CardIvoryBottom))

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
                    when {
                        enabled && isNextSuggested ->
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
                        isComingSoonTeaser ->
                            Modifier.drawWithContent {
                                drawContent()
                                val stroke = (glowWidth * 0.65f).dp.toPx()
                                val inset = stroke / 2f
                                drawRoundRect(
                                    color = SoonGlow.copy(alpha = glowAlpha * 0.55f),
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - stroke, size.height - stroke),
                                    cornerRadius = CornerRadius(18.dp.toPx() - inset),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                                )
                            }
                        else -> Modifier
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
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    chapter.isComingSoon -> {
                        Season2MysteryPlaceholderCard(modifier = Modifier.fillMaxSize())
                    }
                    chapter.posterResId != null -> {
                        Season2FrostedPosterPreview(
                            posterResId = chapter.posterResId,
                            revealed = chapter.state == ChapterState.Completed,
                            modifier = Modifier.fillMaxSize(),
                            showMysteryGlyph = chapter.state != ChapterState.Completed,
                        )
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.60f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = "\u200F${Season2Copy.ChapterLabelPrefix} ${chapter.chapterIndex}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = TitleBrown,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                }

                CompletionTag(
                    chapterIndex = chapter.chapterIndex,
                    state = chapter.state,
                    isComingSoon = chapter.isComingSoon,
                    isPrerequisiteLocked = chapter.isPrerequisiteLocked,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = chapter.displayLabel,
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
    chapterIndex: Int,
    state: ChapterState,
    isComingSoon: Boolean,
    isPrerequisiteLocked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    when {
        isComingSoon -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFF243B53).copy(alpha = 0.30f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text =
                        "\u200F${
                            if (chapterIndex == 2) {
                                Season2Copy.NextChapterComingSoon
                            } else {
                                Season2Copy.ComingSoon
                            }
                        }",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        state == ChapterState.Completed -> {
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
                        text = "\u200F${Season2Copy.ChapterRevealedBadge}",
                        color = TitleBrown,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
        state == ChapterState.Locked -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.22f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = "\u200Fנעול",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        else -> Unit
    }
}
