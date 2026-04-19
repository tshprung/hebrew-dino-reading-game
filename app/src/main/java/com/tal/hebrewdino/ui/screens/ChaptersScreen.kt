package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.learning.CaveHomeMark
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
)

/** Must match [ChapterVerticalPath] map height for initial scroll math. */
private val ChaptersMapPathHeight =
    288.dp + (ChaptersPathLayout.CHAPTER_COUNT * 218).dp + 176.dp

/**
 * Wide, low egg silhouette: slightly narrower top, full rounded bottom, no sharp tip.
 * Control points are in normalized egg space (0..1 × 0..1 of the clipped rect).
 */
private object EggShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val path =
            Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                cubicTo(w * 0.70f, h * 0.12f, w * 0.88f, h * 0.26f, w * 0.88f, h * 0.46f)
                cubicTo(w * 0.88f, h * 0.70f, w * 0.72f, h * 0.90f, w * 0.5f, h * 0.92f)
                cubicTo(w * 0.28f, h * 0.90f, w * 0.12f, h * 0.70f, w * 0.12f, h * 0.46f)
                cubicTo(w * 0.12f, h * 0.26f, w * 0.30f, h * 0.12f, w * 0.5f, h * 0.12f)
                close()
            }
        return Outline.Generic(path)
    }
}

/** Chapter node on the map: wider than tall so it reads as an egg, not a drop. */
private val ChapterEggWidth = 168.dp
private val ChapterEggHeight = 96.dp

@Composable
fun ChaptersScreen(
    unlockedChapter: Int,
    chapter4ComingSoon: Boolean = false,
    chaptersProgress: ChaptersProgress,
    onOpenSettings: () -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    LaunchedEffect(unlockedChapter) {
        delay(48)
        var guard = 0
        while (scroll.maxValue == 0 && guard < 40) {
            delay(24)
            guard++
        }
        if (scroll.maxValue <= 0) return@LaunchedEffect

        val idx = (unlockedChapter - 1).coerceIn(0, ChaptersPathLayout.CHAPTER_COUNT - 1)
        val t = ChaptersPathLayout.tForChapterIndex(idx)
        val pt = ChaptersPathLayout.pointOnPath(t)
        val pathPx = with(density) { ChaptersMapPathHeight.toPx() }
        val topPaddingPx = with(density) { 48.dp.toPx() }
        val titleBlockPx = with(density) { 88.dp.toPx() }
        val eggCenterY = topPaddingPx + titleBlockPx + pt.y * pathPx
        val screenPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        // Chapter 1 lives at low t (top of path); bias scroll so that egg sits a bit below top bar.
        val target = (eggCenterY - screenPx * 0.26f).roundToInt().coerceIn(0, scroll.maxValue)
        scroll.scrollTo(target)
    }

    val chapters =
        listOf(
            ChapterCard(1, "פרק 1 - מצא את הביצה", "היער: תחנות בדרך + אותיות"),
            ChapterCard(2, "פרק 2 - חוזרים הביתה", "הדרך חזרה לקן — אותיות בדרך"),
            ChapterCard(3, "פרק 3 - מצא את החבר", "מי קרא? אותיות כרמזים"),
            ChapterCard(4, "פרק 4", if (chapter4ComingSoon) "בקרוב" else ""),
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
                    .padding(start = 12.dp, end = 12.dp, top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "מפת הפרקים",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF1A3A4A),
            )
            Spacer(modifier = Modifier.height(14.dp))

            ChapterVerticalPath(
                chapters = chapters,
                unlockedChapter = unlockedChapter,
                chaptersProgress = chaptersProgress,
                onOpenChapter = onOpenChapter,
                modifier = Modifier.fillMaxWidth(),
            )
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
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.92f),
                        contentColor = Color(0xFF0B2B3D),
                    ),
            ) {
                Text("הגדרות", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
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
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(modifier = modifier.height(ChaptersMapPathHeight)) {
        val w = maxWidth
        val h = maxHeight

        val eggW = ChapterEggWidth
        val eggH = ChapterEggHeight
        val eggHalfW = eggW / 2
        val eggHalfH = eggH / 2
        val dinoMapSize = 92.dp

        Box(modifier = Modifier.fillMaxSize()) {
            ChaptersRoadCanvas(modifier = Modifier.fillMaxSize().zIndex(0f))

            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-6).dp)
                        .zIndex(0.25f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CaveHomeMark(modifier = Modifier.size(width = 128.dp, height = 92.dp))
                Text(
                    text = "הקן — הבית",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF1A3A4A).copy(alpha = 0.88f),
                )
            }

            val curIdx = (unlockedChapter - 1).coerceIn(0, ChaptersPathLayout.CHAPTER_COUNT - 1)
            val tEgg = ChaptersPathLayout.tForChapterIndex(curIdx)
            val pEgg = ChaptersPathLayout.pointOnPath(tEgg)
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
            val eggRadiusPx = with(density) { eggHalfW.toPx() }
            val dinoRadiusPx = with(density) { (dinoMapSize / 2).toPx() }
            val lateralPx =
                max(
                    with(density) { 118.dp.toPx() },
                    eggRadiusPx + dinoRadiusPx + with(density) { 14.dp.toPx() },
                )
            val alongPathPx = with(density) { 18.dp.toPx() }
            val eggCxPx = pEgg.x * wPx
            val eggCyPx = pEgg.y * hPx
            val dinoSizePx = with(density) { dinoMapSize.toPx() }
            val dinoCx = eggCxPx + side * perpX * lateralPx + perpY * lateralPx * 0.05f
            val dinoCy = eggCyPx + perpY * lateralPx * 0.22f + (dPx / len) * alongPathPx

            Image(
                painter = painterResource(id = R.drawable.dino_idle),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .zIndex(1f)
                        .offset {
                            IntOffset(
                                x = (dinoCx - dinoSizePx / 2f).roundToInt(),
                                y = (dinoCy - dinoSizePx * 0.48f).roundToInt(),
                            )
                        }
                        .size(dinoMapSize),
                contentScale = ContentScale.Fit,
            )

            chapters.forEachIndexed { idx, ch ->
                val t = ChaptersPathLayout.tForChapterIndex(idx)
                val pt = ChaptersPathLayout.pointOnPath(t)
                val state =
                    chapterEggState(
                        chapterId = ch.id,
                        unlockedChapter = unlockedChapter,
                        progress = chaptersProgress,
                    )
                val openable = state != ChapterEggState.Locked && ch.id <= 3
                val showSubtitle =
                    ch.id <= 3 &&
                        ch.subtitle.isNotBlank() &&
                        ch.subtitle.length <= 42

                Column(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * pt.x - eggHalfW, y = h * pt.y - eggHalfH)
                            .width(eggW)
                            .zIndex(2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ChapterEgg(
                        chapterId = ch.id,
                        state = state,
                        enabled = openable,
                        onClick = { if (openable) onOpenChapter(ch.id) },
                        modifier = Modifier.size(width = eggW, height = eggH),
                    )
                    if (ch.title.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ch.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1A3A4A).copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                        if (showSubtitle) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ch.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF1A3A4A).copy(alpha = 0.62f),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
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
            val wobble = if (widthWobble) 1f + 0.04f * sin(t * 34.5f) else 1f
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

@Composable
private fun ChaptersRoadCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // ~35% narrower than before; lower contrast so road reads as background behind eggs.
        val edgePath = buildRoadRibbonPath(size, halfWidthPx = size.minDimension * 0.046f, widthWobble = true)
        drawPath(path = edgePath, color = Color(0xFF7A5A3A).copy(alpha = 0.38f), style = Fill)

        val basePath = buildRoadRibbonPath(size, halfWidthPx = size.minDimension * 0.038f, widthWobble = true)
        drawPath(path = basePath, color = Color(0xFFD8C49A).copy(alpha = 0.58f), style = Fill)

        val innerPath = buildRoadRibbonPath(size, halfWidthPx = size.minDimension * 0.029f, widthWobble = true)
        drawPath(path = innerPath, color = Color(0xFFEADDBA).copy(alpha = 0.32f), style = Fill)

        val centerPath = Path()
        val start = Offset(size.width * 0.66f, size.height * 0.06f)
        centerPath.moveTo(start.x, start.y)
        val p1 = Offset(size.width * 0.44f, size.height * 0.18f)
        val p2 = Offset(size.width * 0.70f, size.height * 0.34f)
        val p3 = Offset(size.width * 0.38f, size.height * 0.46f)
        val p4 = Offset(size.width * 0.72f, size.height * 0.62f)
        val p5 = Offset(size.width * 0.40f, size.height * 0.78f)
        val end = Offset(size.width * 0.64f, size.height * 0.94f)
        centerPath.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
        centerPath.cubicTo(p4.x, p4.y, p5.x, p5.y, end.x, end.y)
        drawPath(
            path = centerPath,
            color = Color(0xFFFFFDF8).copy(alpha = 0.14f),
            style = Stroke(width = size.minDimension * 0.018f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        for (i in 0 until 72) {
            val t = (i * 37 % 100) / 100f
            val p = ChaptersPathLayout.pointOnPath(t)
            val cx = p.x * size.width
            val cy = p.y * size.height
            val jitterX = (i % 5 - 2) * 3.2f
            val jitterY = ((i * 11) % 7 - 3) * 2.8f
            drawCircle(
                color = Color(0xFF5C4A30).copy(alpha = 0.035f + (i % 3) * 0.012f),
                radius = 2.0f + (i % 3),
                center = Offset(cx + jitterX, cy + jitterY),
            )
        }
    }
}

@Composable
private fun ChapterEgg(
    chapterId: Int,
    state: ChapterEggState,
    enabled: Boolean,
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

    val brush =
        remember(state, baseHue) {
            when (state) {
                ChapterEggState.Locked ->
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFFB0B0B0),
                                Color(0xFF7A7A7A),
                                Color(0xFF5C5C5C),
                            ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 700f),
                    )
                ChapterEggState.Unlocked ->
                    Brush.linearGradient(
                        colors =
                            listOf(
                                lighten(baseHue, 0.22f),
                                baseHue,
                                darken(baseHue, 0.18f),
                            ),
                        start = Offset(0f, 0f),
                        end = Offset(420f, 620f),
                    )
                ChapterEggState.Completed ->
                    Brush.linearGradient(
                        colors =
                            listOf(
                                lighten(baseHue, 0.12f).copy(alpha = 0.88f),
                                baseHue.copy(alpha = 0.75f),
                                darken(baseHue, 0.22f).copy(alpha = 0.72f),
                            ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 600f),
                    )
            }
        }

    Box(
        modifier =
            modifier
                .clip(EggShape)
                .background(brush)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (state != ChapterEggState.Locked) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawOval(
                    color = Color.White.copy(alpha = if (state == ChapterEggState.Unlocked) 0.26f else 0.14f),
                    topLeft = Offset(size.width * 0.16f, size.height * 0.10f),
                    size = Size(size.width * 0.40f, size.height * 0.20f),
                )
            }
        }

        when (state) {
            ChapterEggState.Locked ->
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.headlineMedium,
                )
            ChapterEggState.Unlocked ->
                Text(
                    text = chapterId.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF1A2430).copy(alpha = 0.92f),
                )
            ChapterEggState.Completed ->
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF1B5E20).copy(alpha = 0.88f),
                )
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
    if (chapterId >= 4) return ChapterEggState.Locked
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
