package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
// (no lazy grid needed; honeycomb is manual rows)
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.components.learning.DinoNestMark
import com.tal.hebrewdino.ui.domain.ChaptersPathLayout
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

data class ChapterCard(
    val id: Int,
    val title: String,
    val subtitle: String,
)

private enum class ChapterEggState {
    Locked,
    Unlocked,
    Completed,
}

data class ChaptersProgress(
    val chapter1Completed: Boolean,
    val chapter2Completed: Boolean,
    val chapter3Completed: Boolean,
    val chapter4Completed: Boolean = false,
)

/** Must match [ChapterVerticalPath] map height for initial scroll math. */
private val ChaptersMapPathHeight =
    200.dp + (ChaptersPathLayout.CHAPTER_COUNT * 210).dp + 160.dp

/**
 * Upright egg silhouette (close to a dinosaur egg photo): rounded bottom, narrower top, slight asymmetry.
 */
private object ChapterEggShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val bottomY = h * 0.94f
        val topY = h * 0.06f
        val path =
            Path().apply {
                moveTo(cx, bottomY)
                cubicTo(
                    w * 0.06f,
                    h * 0.88f,
                    w * 0.06f,
                    h * 0.38f,
                    w * 0.42f,
                    topY + h * 0.01f,
                )
                cubicTo(
                    w * 0.86f,
                    h * 0.30f,
                    w * 0.95f,
                    h * 0.74f,
                    cx,
                    bottomY,
                )
                close()
            }
        return Outline.Generic(path)
    }
}

private val ChapterEggWidth = 190.dp
private val ChapterEggHeight = 250.dp

// Back-compat: legacy map egg used by old vertical path code.
private val EggShape: Shape = ChapterEggShape

/** Minimum clearance egg ↔ dino along path normal (no overlap). */
private val DinoEggGap = 10.dp

@Composable
fun ChaptersScreen(
    unlockedChapter: Int,
    chapter4ComingSoon: Boolean = false,
    /** Highest chapter tile (1–10) that can be opened from the map when unlocked. */
    maxSelectableChapterId: Int = 3,
    chaptersProgress: ChaptersProgress,
    onOpenSettings: () -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()

    val chapters =
        listOf(
            ChapterCard(1, "פרק 1 - מצא את הביצה", "היער: תחנות בדרך + אותיות"),
            ChapterCard(2, "פרק 2 - מצא את הביצה הורודה", "בדרך לביצה הורודה — אותיות בדרך"),
            ChapterCard(3, "פרק 3 - מצא את הביצה הסגולה", "בדרך לביצה הסגולה — תחנות כמו בפרק 1"),
            ChapterCard(
                4,
                "פרק 4 - חיזוק חכם",
                if (chapter4ComingSoon) "בקרוב" else "תחנות כמו בפרק 1 — אותיות חוזרות בחוכמה",
            ),
            ChapterCard(5, "פרק 5", ""),
            ChapterCard(6, "פרק 6", ""),
            ChapterCard(7, "פרק 7", ""),
            ChapterCard(8, "פרק 8", ""),
            ChapterCard(9, "פרק 9", ""),
            ChapterCard(10, "פרק 10", ""),
        )

    Box(modifier = modifier.fillMaxSize()) {
        ChaptersAmbientBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            ChaptersHexHoneycomb(
                chapters = chapters,
                unlockedChapter = unlockedChapter,
                chaptersProgress = chaptersProgress,
                maxSelectableChapterId = maxSelectableChapterId,
                onOpenChapter = onOpenChapter,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 4.dp, end = 8.dp)
                    .zIndex(1f),
        ) {
            OutlinedButton(
                onClick = onOpenSettings,
                colors = ChapterNavChipStyles.outlinedButtonColors(),
            ) {
                Text("הגדרות", style = ChapterNavChipStyles.labelTextStyle())
            }
        }
    }
}

private object ChapterHexShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val r = minOf(w, h) / 2f
        val cx = w / 2f
        val cy = h / 2f
        val k = 0.86f // cos(30°)
        val path =
            Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r * k, cy - r * 0.5f)
                lineTo(cx + r * k, cy + r * 0.5f)
                lineTo(cx, cy + r)
                lineTo(cx - r * k, cy + r * 0.5f)
                lineTo(cx - r * k, cy - r * 0.5f)
                close()
            }
        return Outline.Generic(path)
    }
}

@Composable
private fun ChaptersHexHoneycomb(
    chapters: List<ChapterCard>,
    unlockedChapter: Int,
    chaptersProgress: ChaptersProgress,
    maxSelectableChapterId: Int,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val tile = (maxWidth / 3.1f).coerceIn(128.dp, 190.dp)
        val h = tile
        val w = tile
        val rowGap = (tile * 0.12f).coerceIn(10.dp, 22.dp)
        val colGap = (tile * 0.08f).coerceIn(8.dp, 18.dp)

        // 3-2-3 honeycomb like the reference screenshot.
        val rows = listOf(3, 2, 3, 2)
        val maxCount = rows.sum()
        val shown = chapters.take(maxCount)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var idx = 0
            rows.forEachIndexed { rowIdx, count ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(colGap),
                    modifier =
                        Modifier
                            .padding(top = if (rowIdx == 0) 0.dp else rowGap)
                            .offset(x = if (count == 2) (w * 0.52f) else 0.dp),
                ) {
                    repeat(count) {
                        val ch = shown.getOrNull(idx++)
                        if (ch == null) return@repeat
                        val state =
                            chapterEggState(
                                chapterId = ch.id,
                                unlockedChapter = unlockedChapter,
                                progress = chaptersProgress,
                            )
                        val openable = state != ChapterEggState.Locked && ch.id <= maxSelectableChapterId
                        ChapterHexTile(
                            title = ch.title,
                            imageRes =
                                when (ch.id) {
                                    2 -> R.drawable.mountain_bg_chapter2
                                    3 -> R.drawable.mountain_bg_chapter3
                                    4 -> R.drawable.mountain_bg_chapter4
                                    else -> R.drawable.forest_bg_journey_road
                                },
                            state = state,
                            enabled = openable,
                            size = w,
                            onClick = { if (openable) onOpenChapter(ch.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterHexTile(
    title: String,
    imageRes: Int,
    state: ChapterEggState,
    enabled: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val locked = state == ChapterEggState.Locked
    Box(
        modifier =
            Modifier
                .size(size)
                .shadow(10.dp, ChapterHexShape)
                .clip(ChapterHexShape)
                .background(Color.White)
                .border(10.dp, Color.White, ChapterHexShape)
                .clickable(enabled = enabled, onClick = onClick),
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Keep all tiles in full color; locked tiles only get a subtle dim + lock badge.
        if (locked) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
        }

        if (locked) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .background(Color.Black.copy(alpha = 0.40f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
        }

        Text(
            text = title,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 14.dp)
                    .background(Color.Black.copy(alpha = if (locked) 0.08f else 0.30f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = if (locked) Color(0xFF263238) else Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun ChaptersAmbientBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFFB8E0FF),
                                    Color(0xFFE8F4FF),
                                    Color(0xFFFFF4E0),
                                    Color(0xFFE8F0DC),
                                ),
                        ),
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.14f),
            contentScale = ContentScale.Crop,
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundH = size.height * 0.34f
            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFF6D8F5A).copy(alpha = 0f),
                                Color(0xFF5C7A4A).copy(alpha = 0.22f),
                                Color(0xFF4A5F38).copy(alpha = 0.38f),
                            ),
                        startY = size.height - groundH * 1.15f,
                        endY = size.height,
                    ),
                topLeft = Offset(0f, size.height - groundH),
                size = Size(size.width, groundH),
            )
            val silhouettes = 7
            for (i in 0 until silhouettes) {
                val x = size.width * (0.08f + i * 0.14f + (i % 3) * 0.04f)
                val baseY = size.height - groundH * 0.35f
                drawCircle(
                    color = Color(0xFF3D5A3A).copy(alpha = 0.12f + (i % 2) * 0.04f),
                    radius = size.width * (0.06f + (i % 3) * 0.02f),
                    center = Offset(x, baseY - i * 2f),
                )
                drawCircle(
                    color = Color(0xFF3D5A3A).copy(alpha = 0.10f),
                    radius = size.width * 0.04f,
                    center = Offset(x + size.width * 0.03f, baseY + size.height * 0.01f),
                )
            }
        }
    }
}

@Composable
private fun ChapterVerticalPath(
    chapters: List<ChapterCard>,
    unlockedChapter: Int,
    chaptersProgress: ChaptersProgress,
    maxSelectableChapterId: Int,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    var prevProgress by remember { mutableStateOf(chaptersProgress) }
    val bump1 = remember { mutableIntStateOf(0) }
    val bump2 = remember { mutableIntStateOf(0) }
    val bump3 = remember { mutableIntStateOf(0) }
    val bump4 = remember { mutableIntStateOf(0) }
    LaunchedEffect(chaptersProgress) {
        if (!prevProgress.chapter1Completed && chaptersProgress.chapter1Completed) bump1.intValue++
        if (!prevProgress.chapter2Completed && chaptersProgress.chapter2Completed) bump2.intValue++
        if (!prevProgress.chapter3Completed && chaptersProgress.chapter3Completed) bump3.intValue++
        if (!prevProgress.chapter4Completed && chaptersProgress.chapter4Completed) bump4.intValue++
        prevProgress = chaptersProgress
    }

    BoxWithConstraints(modifier = modifier.height(ChaptersMapPathHeight)) {
        val w = maxWidth
        val h = maxHeight

        val eggW = ChapterEggWidth
        val eggH = ChapterEggHeight
        val eggHalfW = eggW / 2
        val eggHalfH = eggH / 2
        val dinoMapSize = 64.dp

        Box(modifier = Modifier.fillMaxSize()) {
            ChaptersRoadCanvas(modifier = Modifier.fillMaxSize().zIndex(0f))

            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-10).dp)
                        .width(260.dp)
                        .height(188.dp)
                        .zIndex(0.25f),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val glowCx = size.width * 0.5f
                    val glowCy = size.height * 0.42f
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFF3C4).copy(alpha = 0.55f),
                                        Color(0xFFFFE082).copy(alpha = 0.12f),
                                        Color.Transparent,
                                    ),
                                center = Offset(glowCx, glowCy),
                                radius = size.width * 0.52f,
                            ),
                        radius = size.width * 0.48f,
                        center = Offset(glowCx, glowCy),
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DinoNestMark(modifier = Modifier.size(width = 208.dp, height = 144.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "הקן — הבית",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF0D2535),
                    )
                }
            }

            chapters.forEachIndexed { idx, ch ->
                val t = ChaptersPathLayout.tForChapterIndex(idx)
                val pt = ChaptersPathLayout.pointOnPath(t)
                val state =
                    chapterEggState(
                        chapterId = ch.id,
                        unlockedChapter = unlockedChapter,
                        progress = chaptersProgress,
                    )
                val openable = state != ChapterEggState.Locked && ch.id <= maxSelectableChapterId
                val isCurrent = ch.id == unlockedChapter && state == ChapterEggState.Unlocked
                val showSubtitle =
                    ch.id <= maxSelectableChapterId &&
                        ch.subtitle.isNotBlank() &&
                        ch.subtitle.length <= 42
                val completionBump =
                    when (ch.id) {
                        1 -> bump1.intValue
                        2 -> bump2.intValue
                        3 -> bump3.intValue
                        4 -> bump4.intValue
                        else -> 0
                    }

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * pt.x - eggHalfW, y = h * pt.y - eggHalfH)
                            .width(eggW)
                            .zIndex(if (isCurrent) 2.6f else 2f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCurrent && state == ChapterEggState.Unlocked) {
                                Canvas(
                                    modifier =
                                        Modifier
                                            .size(eggW + 36.dp, eggH + 22.dp),
                                ) {
                                    drawCircle(
                                        brush =
                                            Brush.radialGradient(
                                                colors =
                                                    listOf(
                                                        Color(0xFFFFF59D).copy(alpha = 0.42f),
                                                        Color(0xFFFFE082).copy(alpha = 0.12f),
                                                        Color.Transparent,
                                                    ),
                                                center = Offset(size.width * 0.5f, size.height * 0.48f),
                                                radius = size.maxDimension * 0.52f,
                                            ),
                                        radius = size.maxDimension * 0.5f,
                                        center = Offset(size.width * 0.5f, size.height * 0.48f),
                                    )
                                }
                            }
                            ChapterEgg(
                                chapterId = ch.id,
                                state = state,
                                isCurrent = isCurrent,
                                enabled = openable,
                                completionBump = completionBump,
                                onClick = { if (openable) onOpenChapter(ch.id) },
                                modifier =
                                    Modifier
                                        .size(width = eggW, height = eggH)
                                        .zIndex(1f),
                            )
                        }
                        if (ch.title.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ch.title,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF0D2535),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                            if (showSubtitle) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ch.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF1A3A4A).copy(alpha = 0.72f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                )
                            }
                        }
                    }

                    if (isCurrent) {
                        val tEgg = ChaptersPathLayout.tForChapterIndex(idx)
                        val dt = 0.02f
                        val pA = ChaptersPathLayout.pointOnPath((tEgg - dt).coerceIn(0f, 1f))
                        val pB = ChaptersPathLayout.pointOnPath((tEgg + dt).coerceIn(0f, 1f))
                        val wPx = with(density) { w.toPx() }
                        val hPx = with(density) { h.toPx() }
                        val dPx = (pB.x - pA.x) * wPx
                        val dPy = (pB.y - pA.y) * hPx
                        val len = hypot(dPx, dPy).coerceAtLeast(1f)
                        val perpX = -dPy / len
                        val perpY = dPx / len
                        val rtl = layoutDirection == LayoutDirection.Rtl
                        val side = if (rtl) -1f else 1f
                        val eggHalfWPx = with(density) { eggHalfW.toPx() }
                        val eggHalfHPx = with(density) { eggHalfH.toPx() }
                        val dinoSizePx = with(density) { dinoMapSize.toPx() }
                        val eggClearance =
                            with(density) {
                                max(eggHalfW.toPx(), eggHalfH.toPx()) + dinoSizePx * 0.48f + DinoEggGap.toPx()
                            }
                        val alongPathPx = with(density) { 14.dp.toPx() }
                        // Egg center in this column’s coords is (eggHalfW, eggHalfH); offset dino along path normal only.
                        val dinoRelCx =
                            eggHalfWPx + side * perpX * eggClearance + perpY * eggClearance * 0.02f
                        val dinoRelCy =
                            eggHalfHPx + perpY * eggClearance * 0.12f + (dPx / len) * alongPathPx
                        val breath by rememberInfiniteTransition(label = "mapDinoBreath").animateFloat(
                            initialValue = 0.97f,
                            targetValue = 1.03f,
                            animationSpec =
                                infiniteRepeatable(
                                    animation = tween(720, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse,
                                ),
                            label = "mapDinoBreathScale",
                        )
                        Image(
                            painter = painterResource(id = R.drawable.dino_idle),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .align(Alignment.TopStart)
                                    .zIndex(3f)
                                    .offset {
                                        IntOffset(
                                            x = (dinoRelCx - dinoSizePx / 2f).roundToInt(),
                                            y = (dinoRelCy - dinoSizePx * 0.42f).roundToInt(),
                                        )
                                    }
                                    .size(dinoMapSize)
                                    .scale(breath),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

private fun buildRoadRibbonPath(size: Size, halfWidthPx: Float, widthWobble: Boolean): Path {
    val samples = 96
    val pts =
        List(samples) { i ->
            val t = i / (samples - 1).coerceAtLeast(1).toFloat()
            val p = ChaptersPathLayout.pointOnPath(t)
            Offset(p.x * size.width, p.y * size.height)
        }
    val halfWidths =
        List(samples) { i ->
            val t = i / (samples - 1).coerceAtLeast(1).toFloat()
            val wobble = if (widthWobble) 1f + 0.03f * sin(t * 34.5f) else 1f
            halfWidthPx * wobble
        }
    val left = ArrayList<Offset>(samples)
    val right = ArrayList<Offset>(samples)
    for (i in 0 until samples) {
        val p = pts[i]
        val tan =
            when (i) {
                0 -> Offset(pts[1].x - pts[0].x, pts[1].y - pts[0].y)
                samples - 1 -> Offset(pts[i].x - pts[i - 1].x, pts[i].y - pts[i - 1].y)
                else -> Offset(pts[i + 1].x - pts[i - 1].x, pts[i + 1].y - pts[i - 1].y)
            }
        val tLen = hypot(tan.x, tan.y).coerceAtLeast(0.001f)
        val nx = -tan.y / tLen
        val ny = tan.x / tLen
        val hw = halfWidths[i]
        left.add(Offset(p.x + nx * hw, p.y + ny * hw))
        right.add(Offset(p.x - nx * hw, p.y - ny * hw))
    }
    val path = Path()
    path.moveTo(left[0].x, left[0].y)
    for (i in 1 until samples) {
        path.lineTo(left[i].x, left[i].y)
    }
    for (i in samples - 1 downTo 0) {
        path.lineTo(right[i].x, right[i].y)
    }
    path.close()
    return path
}

/** Same centerline as the road ribbon — must match [ChaptersPathLayout.pointOnPath]. */
private fun buildCenterlinePath(size: Size, samples: Int = 96): Path {
    val path = Path()
    val pts =
        List(samples) { i ->
            val t = i / (samples - 1).coerceAtLeast(1).toFloat()
            val p = ChaptersPathLayout.pointOnPath(t)
            Offset(p.x * size.width, p.y * size.height)
        }
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until samples) {
        path.lineTo(pts[i].x, pts[i].y)
    }
    return path
}

@Composable
private fun ChaptersRoadCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val m = size.minDimension
        val wScale = 1.52f
        // Wider dirt ribbon + readable contrast (still behind eggs / UI).
        val edgePath = buildRoadRibbonPath(size, halfWidthPx = m * 0.0168f * wScale, widthWobble = true)
        drawPath(path = edgePath, color = Color(0xFF5D4037).copy(alpha = 0.22f), style = Fill)

        val basePath = buildRoadRibbonPath(size, halfWidthPx = m * 0.0136f * wScale, widthWobble = true)
        drawPath(path = basePath, color = Color(0xFF8D6E63).copy(alpha = 0.55f), style = Fill)

        val innerPath = buildRoadRibbonPath(size, halfWidthPx = m * 0.0104f * wScale, widthWobble = true)
        drawPath(path = innerPath, color = Color(0xFFD7CCC8).copy(alpha = 0.42f), style = Fill)

        val centerPath = buildCenterlinePath(size)
        drawPath(
            path = centerPath,
            color = Color(0xFFFFFFFF).copy(alpha = 0.045f),
            style = Stroke(width = m * 0.006f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        for (i in 0 until 42) {
            val t = (i * 37 % 100) / 100f
            val p = ChaptersPathLayout.pointOnPath(t)
            val cx = p.x * size.width
            val cy = p.y * size.height
            val jitterX = (i % 5 - 2) * 2.4f
            val jitterY = ((i * 11) % 7 - 3) * 2.0f
            val pebbleW = 2.5f + (i % 4)
            val pebbleH = 1.8f + (i % 3)
            drawOval(
                color =
                    when (i % 3) {
                        0 -> Color(0xFF6D4C41).copy(alpha = 0.16f)
                        1 -> Color(0xFF8D6E63).copy(alpha = 0.14f)
                        else -> Color(0xFFBCAAA4).copy(alpha = 0.12f)
                    },
                topLeft = Offset(cx + jitterX - pebbleW, cy + jitterY - pebbleH),
                size = Size(pebbleW * 2f, pebbleH * 2f),
            )
        }
    }
}

@Composable
private fun ChapterEgg(
    chapterId: Int,
    state: ChapterEggState,
    isCurrent: Boolean,
    enabled: Boolean,
    completionBump: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseHue =
        remember(chapterId) {
            listOf(
                Color(0xFFFFB74D),
                Color(0xFF81D4FA),
                Color(0xFFA5D6A7),
                Color(0xFFCE93D8),
                Color(0xFFFFAB91),
                Color(0xFFFFF59D),
                Color(0xFFB39DDB),
                Color(0xFF80CBC4),
                Color(0xFFFFCC80),
                Color(0xFF90CAF9),
            )[(chapterId - 1).coerceIn(0, 9)]
        }
    val patternHue = remember(baseHue) { darken(baseHue, 0.22f) }

    val shellBrush =
        remember(state, baseHue, isCurrent) {
            when (state) {
                ChapterEggState.Locked ->
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFFC6C6C6),
                                Color(0xFF8E8E8E),
                            ),
                    )
                ChapterEggState.Unlocked -> {
                    val top = lighten(baseHue, if (isCurrent) 0.30f else 0.18f)
                    val mid = if (isCurrent) lighten(baseHue, 0.08f) else baseHue
                    val bot = darken(baseHue, if (isCurrent) 0.22f else 0.16f)
                    Brush.verticalGradient(colors = listOf(top, mid, bot))
                }
                ChapterEggState.Completed ->
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                lighten(baseHue, 0.34f),
                                lighten(baseHue, 0.14f),
                                darken(baseHue, 0.10f),
                            ),
                    )
            }
        }

    val elev =
        when (state) {
            ChapterEggState.Locked -> 1.dp
            ChapterEggState.Completed -> 4.dp
            ChapterEggState.Unlocked -> if (isCurrent) 7.dp else 4.dp
        }

    val celebrate = remember { Animatable(1f) }
    LaunchedEffect(completionBump) {
        if (completionBump <= 0) return@LaunchedEffect
        celebrate.snapTo(1f)
        celebrate.animateTo(1.10f, tween(durationMillis = 100))
        celebrate.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
    }
    val breath by rememberInfiniteTransition(label = "eggIdle$chapterId").animateFloat(
        initialValue = 0.99f,
        targetValue = 1.02f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(880, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "eggBreath",
    )
    val liveScale =
        celebrate.value *
            if (isCurrent && state == ChapterEggState.Unlocked) {
                breath
            } else {
                1f
            }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = liveScale
                    scaleY = liveScale
                    transformOrigin = TransformOrigin(0.5f, 0.55f)
                }
                .shadow(
                    elevation = elev,
                    shape = EggShape,
                    ambientColor = Color(0xFF1A1208).copy(alpha = 0.18f),
                    spotColor = Color(0xFF1A1208).copy(alpha = 0.24f),
                )
                .clip(EggShape)
                .background(shellBrush)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (state != ChapterEggState.Locked) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val hi = if (isCurrent && state == ChapterEggState.Unlocked) 0.32f else 0.18f
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = hi),
                                    Color.White.copy(alpha = 0f),
                                ),
                            center = Offset(size.width * 0.28f, size.height * 0.24f),
                            radius = size.minDimension * 0.48f,
                        ),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.28f, size.height * 0.24f),
                )
            }
        }

        if (state == ChapterEggState.Unlocked) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val r = size.minDimension * 0.034f
                val spots =
                    listOf(
                        Offset(w * 0.22f, h * 0.38f),
                        Offset(w * 0.72f, h * 0.34f),
                        Offset(w * 0.48f, h * 0.52f),
                        Offset(w * 0.30f, h * 0.66f),
                        Offset(w * 0.68f, h * 0.62f),
                    )
                for ((i, o) in spots.withIndex()) {
                    drawCircle(
                        color = patternHue.copy(alpha = 0.22f + (i % 2) * 0.06f),
                        radius = r * (0.85f + (i % 3) * 0.08f),
                        center = o,
                    )
                }
                drawLine(
                    color = patternHue.copy(alpha = 0.18f),
                    start = Offset(w * 0.18f, h * 0.48f),
                    end = Offset(w * 0.82f, h * 0.42f),
                    strokeWidth = size.minDimension * 0.018f,
                    cap = StrokeCap.Round,
                )
            }
        }

        if (state == ChapterEggState.Completed) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // “Hatched” opening (interior only — not the outer egg silhouette).
                drawOval(
                    color = Color(0xFF1E1810).copy(alpha = 0.22f),
                    topLeft = Offset(w * 0.38f, h * 0.08f),
                    size = Size(w * 0.24f, h * 0.16f),
                )
                drawOval(
                    color = Color(0xFFFFF8E1).copy(alpha = 0.35f),
                    topLeft = Offset(w * 0.40f, h * 0.10f),
                    size = Size(w * 0.20f, h * 0.11f),
                )
                val crack = Color(0xFF2C2418).copy(alpha = 0.40f)
                val sw = size.minDimension * 0.022f
                drawLine(
                    color = crack,
                    start = Offset(w * 0.36f, h * 0.20f),
                    end = Offset(w * 0.54f, h * 0.62f),
                    strokeWidth = sw,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = crack,
                    start = Offset(w * 0.50f, h * 0.26f),
                    end = Offset(w * 0.32f, h * 0.54f),
                    strokeWidth = sw * 0.85f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = crack,
                    start = Offset(w * 0.56f, h * 0.34f),
                    end = Offset(w * 0.70f, h * 0.52f),
                    strokeWidth = sw * 0.75f,
                    cap = StrokeCap.Round,
                )
            }
        }

        when (state) {
            ChapterEggState.Locked ->
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val s = size.minDimension
                    val bodyW = s * 0.38f
                    val bodyH = s * 0.30f
                    val bodyLeft = c.x - bodyW / 2f
                    val bodyTop = c.y - bodyH * 0.15f
                    val lockGray = Color(0xFF3A3A3A).copy(alpha = 0.72f)
                    val shackle =
                        Path().apply {
                            val w = bodyW * 0.42f
                            val archTop = c.y - bodyH * 0.85f
                            moveTo(c.x - w, c.y - bodyH * 0.35f)
                            lineTo(c.x - w, archTop + bodyH * 0.12f)
                            cubicTo(
                                c.x - w,
                                archTop,
                                c.x + w,
                                archTop,
                                c.x + w,
                                archTop + bodyH * 0.12f,
                            )
                            lineTo(c.x + w, c.y - bodyH * 0.35f)
                        }
                    drawPath(
                        path = shackle,
                        color = lockGray,
                        style = Stroke(width = s * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                    drawRoundRect(
                        color = lockGray,
                        topLeft = Offset(bodyLeft, bodyTop),
                        size = Size(bodyW, bodyH),
                        cornerRadius = CornerRadius(s * 0.06f, s * 0.06f),
                    )
                    val keyholeY = bodyTop + bodyH * 0.38f
                    drawCircle(
                        color = Color(0xFF8A8A8A).copy(alpha = 0.5f),
                        radius = s * 0.045f,
                        center = Offset(c.x, keyholeY),
                    )
                    drawRect(
                        color = Color(0xFF8A8A8A).copy(alpha = 0.5f),
                        topLeft = Offset(c.x - s * 0.022f, keyholeY),
                        size = Size(s * 0.044f, s * 0.09f),
                    )
                }
            ChapterEggState.Unlocked ->
                Text(
                    text = chapterId.toString(),
                    style =
                        if (isCurrent) {
                            MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)
                        } else {
                            MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                        },
                    color = Color(0xFF1A2430).copy(alpha = 0.92f),
                )
            ChapterEggState.Completed -> Unit
        }
    }
}

private fun lighten(c: Color, f: Float): Color {
    return Color(
        red = (c.red + (1f - c.red) * f).coerceIn(0f, 1f),
        green = (c.green + (1f - c.green) * f).coerceIn(0f, 1f),
        blue = (c.blue + (1f - c.blue) * f).coerceIn(0f, 1f),
        alpha = c.alpha,
    )
}

private fun darken(c: Color, f: Float): Color {
    return Color(
        red = (c.red * (1f - f)).coerceIn(0f, 1f),
        green = (c.green * (1f - f)).coerceIn(0f, 1f),
        blue = (c.blue * (1f - f)).coerceIn(0f, 1f),
        alpha = c.alpha,
    )
}

private fun chapterEggState(
    chapterId: Int,
    unlockedChapter: Int,
    progress: ChaptersProgress,
): ChapterEggState {
    if (chapterId >= 5) return ChapterEggState.Locked
    if (chapterId == 4) {
        if (!progress.chapter3Completed) return ChapterEggState.Locked
        if (progress.chapter4Completed) return ChapterEggState.Completed
        return ChapterEggState.Unlocked
    }
    val done =
        when (chapterId) {
            1 -> progress.chapter1Completed
            2 -> progress.chapter2Completed
            3 -> progress.chapter3Completed
            else -> false
        }
    if (done) return ChapterEggState.Completed
    if (chapterId == unlockedChapter) return ChapterEggState.Unlocked
    if (chapterId < unlockedChapter) return ChapterEggState.Completed
    return ChapterEggState.Locked
}
