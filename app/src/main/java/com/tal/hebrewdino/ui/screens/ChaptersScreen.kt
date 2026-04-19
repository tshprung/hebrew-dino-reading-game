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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.learning.CaveHomeMark
import com.tal.hebrewdino.ui.domain.ChaptersPathLayout
import kotlin.math.hypot
import kotlin.math.roundToInt

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
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "מפת הפרקים",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
            )
            Spacer(modifier = Modifier.height(12.dp))

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
                        containerColor = Color.White.copy(alpha = 0.90f),
                        contentColor = Color(0xFF0B2B3D),
                    ),
            ) {
                Text("הגדרות", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
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
    val pathHeight = remember { 220.dp + (ChaptersPathLayout.CHAPTER_COUNT * 148).dp + 120.dp }

    BoxWithConstraints(modifier = modifier.height(pathHeight)) {
        val w = maxWidth
        val h = maxHeight

        Box(modifier = Modifier.fillMaxSize()) {
            ChaptersRoadCanvas(modifier = Modifier.fillMaxSize())

            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-8).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CaveHomeMark(modifier = Modifier.size(width = 120.dp, height = 88.dp))
                Text(
                    text = "הקן — הבית",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0B2B3D),
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
            val lateralPx = with(density) { 96.dp.toPx() }
            val eggCxPx = pEgg.x * wPx
            val eggCyPx = pEgg.y * hPx
            val dinoSizePx = with(density) { 90.dp.toPx() }
            val dinoCx = eggCxPx + side * perpX * lateralPx + perpY * lateralPx * 0.12f
            val dinoCy = eggCyPx + perpY * lateralPx * 0.35f

            Image(
                painter = painterResource(id = R.drawable.dino_idle),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                x = (dinoCx - dinoSizePx / 2f).roundToInt(),
                                y = (dinoCy - dinoSizePx * 0.55f).roundToInt(),
                            )
                        }
                        .size(90.dp),
                contentScale = ContentScale.Fit,
            )

            val eggW = 78.dp
            val eggH = 96.dp
            val eggHalfW = eggW / 2
            val eggHalfH = eggH / 2

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

                Column(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * pt.x - eggHalfW, y = h * pt.y - eggHalfH)
                            .width(200.dp)
                            .zIndex(1f),
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
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                        if (ch.subtitle.isNotBlank()) {
                            Text(
                                text = ch.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF0B2B3D).copy(alpha = 0.75f),
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

@Composable
private fun ChaptersRoadCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path()
        val start = Offset(size.width * 0.66f, size.height * 0.06f)
        path.moveTo(start.x, start.y)
        val p1 = Offset(size.width * 0.44f, size.height * 0.18f)
        val p2 = Offset(size.width * 0.70f, size.height * 0.34f)
        val p3 = Offset(size.width * 0.38f, size.height * 0.46f)
        val p4 = Offset(size.width * 0.72f, size.height * 0.62f)
        val p5 = Offset(size.width * 0.40f, size.height * 0.78f)
        val end = Offset(size.width * 0.64f, size.height * 0.94f)
        path.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
        path.cubicTo(p4.x, p4.y, p5.x, p5.y, end.x, end.y)

        drawPath(
            path = path,
            color = Color(0xFFE2C999).copy(alpha = 0.92f),
            style = Stroke(width = 56f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawPath(
            path = path,
            color = Color(0xFF6B4A2A).copy(alpha = 0.26f),
            style = Stroke(width = 62f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(width = 26f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
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
    val eggShape = RoundedCornerShape(percent = 50)

    Box(
        modifier =
            modifier
                .clip(eggShape)
                .background(
                    when (state) {
                        ChapterEggState.Locked -> Color(0xFF8D8D8D).copy(alpha = 0.85f)
                        ChapterEggState.Unlocked -> baseHue.copy(alpha = 0.92f)
                        ChapterEggState.Completed -> baseHue.copy(alpha = 0.55f)
                    },
                )
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (state == ChapterEggState.Unlocked) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val r = size.minDimension * 0.06f
                val dots =
                    listOf(
                        Offset(size.width * 0.25f, size.height * 0.35f),
                        Offset(size.width * 0.72f, size.height * 0.30f),
                        Offset(size.width * 0.45f, size.height * 0.55f),
                        Offset(size.width * 0.68f, size.height * 0.62f),
                    )
                dots.forEach { c ->
                    drawCircle(color = Color.White.copy(alpha = 0.55f), radius = r, center = c)
                }
            }
        }

        when (state) {
            ChapterEggState.Locked ->
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.titleLarge,
                )
            ChapterEggState.Unlocked ->
                Text(
                    text = chapterId.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0B2B3D),
                )
            ChapterEggState.Completed ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val stroke = 3.dp.toPx()
                        drawLine(
                            color = Color(0xFF4E342E).copy(alpha = 0.75f),
                            start = Offset(size.width * 0.18f, size.height * 0.22f),
                            end = Offset(size.width * 0.82f, size.height * 0.78f),
                            strokeWidth = stroke,
                        )
                        drawLine(
                            color = Color(0xFF4E342E).copy(alpha = 0.55f),
                            start = Offset(size.width * 0.72f, size.height * 0.18f),
                            end = Offset(size.width * 0.28f, size.height * 0.62f),
                            strokeWidth = stroke * 0.7f,
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.dino_idle),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 2.dp)
                                .size(40.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
        }
    }
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
