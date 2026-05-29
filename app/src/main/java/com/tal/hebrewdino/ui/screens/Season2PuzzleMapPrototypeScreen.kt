package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

private const val TILE_COLUMNS = 3
private const val TILE_ROWS = 2
private const val TILE_COUNT = TILE_COLUMNS * TILE_ROWS

private val MuseumWallTop = Color(0xFFF4EDE3)
private val MuseumWallBottom = Color(0xFFE6D9C8)
private val TitleBrown = Color(0xFF4A3A2E)
private val TitlePlum = Color(0xFF4A2A4E)
private val GlowAmber = Color(0xFF2E7D32)
private val SparkleGold = Color(0xFFFFF2D6)
private val GridLineColor = Color.Black.copy(alpha = 0.12f)

private const val LOCKED_SATURATION = 0.02f
private val FossilDust = Color(0xFFB8AFA5)
private val FossilShade = Color(0xFF2B2724)

private data class Season2ChapterPoster(
    val title: String,
    val posterResId: Int,
)

private fun season2PosterForChapter(chapterId: Int): Season2ChapterPoster =
    when (chapterId) {
        1 -> Season2ChapterPoster(title = "פרק 1 · טירנוזאורוס", posterResId = R.drawable.season2_trex_puzzle_full)
        2 ->
            Season2ChapterPoster(
                title = "פרק 2 · טריצרטופס",
                posterResId = R.drawable.season2_triceratops_puzzle_full,
            )
        // Fallback until other posters are added.
        3 -> Season2ChapterPoster(title = "פרק 3 · סטגוזאורוס", posterResId = R.drawable.season2_trex_puzzle_full)
        4 -> Season2ChapterPoster(title = "פרק 4 · ברכיוזאורוס", posterResId = R.drawable.season2_trex_puzzle_full)
        5 -> Season2ChapterPoster(title = "פרק 5 · אנקילוזאורוס", posterResId = R.drawable.season2_trex_puzzle_full)
        6 -> Season2ChapterPoster(title = "פרק 6 · פטרנודון", posterResId = R.drawable.season2_trex_puzzle_full)
        else -> Season2ChapterPoster(title = "פרק · דינוזאור", posterResId = R.drawable.season2_trex_puzzle_full)
    }

/**
 * Season 2 puzzle map UX prototype — one continuous poster, per-tile color reveal on top.
 */
@Composable
fun Season2PuzzleMapPrototypeScreen(
    chapterId: Int,
    onBack: () -> Unit,
    onOpenStation: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }
    val completedChapters by season2Progress.completedChaptersFlow.collectAsState(initial = emptySet())
    val completedStations by season2Progress.completedStationsFlow(chapterId).collectAsState(initial = emptySet())
    val chapterPoster = remember(chapterId) { season2PosterForChapter(chapterId) }
    val posterPainter = painterResource(id = chapterPoster.posterResId)

    var coloredThrough by remember { mutableIntStateOf(0) }
    var nextPlayableTile by remember { mutableIntStateOf(1) }
    var revealingTile by remember { mutableStateOf<Int?>(null) }
    val revealProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val isRevealing = revealingTile != null

    fun startReveal(tile: Int) {
        if (isRevealing) return
        revealingTile = tile
        scope.launch {
            revealProgress.snapTo(0f)
            revealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1_450, easing = FastOutSlowInEasing),
            )
            revealingTile = null
            revealProgress.snapTo(0f)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MuseumWallTop, MuseumWallBottom))),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    val boardWidth = maxWidth
                    val boardHeight = maxHeight
                    val cellWidth = boardWidth / TILE_COLUMNS
                    val cellHeight = boardHeight / TILE_ROWS

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ContinuousPosterBoard(
                            posterPainter = posterPainter,
                            boardWidth = boardWidth,
                            boardHeight = boardHeight,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            coloredThrough = coloredThrough,
                            nextPlayableTile = nextPlayableTile,
                            revealingTile = revealingTile,
                            revealProgress = revealProgress.value,
                            isRevealing = isRevealing,
                            onTileTap = { tile -> if (!isRevealing) onOpenStation(tile) },
                        )
                    }
                }

                FloatingMapChrome(
                    onBack = onBack,
                    title = rtl(chapterPoster.title),
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }

    val isChapterCompleted = chapterId in completedChapters
    LaunchedEffect(chapterId, isChapterCompleted, completedStations) {
        if (isChapterCompleted) {
            // Re-entering a completed chapter: full dinosaur in color; all stations remain replayable.
            coloredThrough = TILE_COUNT
            nextPlayableTile = TILE_COUNT + 1
            revealingTile = null
            revealProgress.snapTo(0f)
        } else {
            val completedCount = completedStations.size.coerceIn(0, TILE_COUNT)
            coloredThrough = completedCount
            nextPlayableTile =
                if (completedCount >= TILE_COUNT) {
                    TILE_COUNT + 1
                } else {
                    (completedCount + 1).coerceAtMost(TILE_COUNT)
                }
            revealingTile = null
            revealProgress.snapTo(0f)
        }
    }

    var lastRevealedThrough by remember(chapterId) { mutableIntStateOf(0) }
    LaunchedEffect(chapterId, coloredThrough) {
        if (coloredThrough > lastRevealedThrough && coloredThrough in 1..TILE_COUNT) {
            startReveal(coloredThrough)
            lastRevealedThrough = coloredThrough
        } else if (coloredThrough <= 0) {
            lastRevealedThrough = 0
        }
        if (coloredThrough >= TILE_COUNT) {
            season2Progress.markChapterCompleted(chapterId)
        }
    }
}

@Composable
private fun FloatingMapChrome(
    onBack: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.55f),
        ) {
            Text(
                text = rtl("חזור"),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TitleBrown,
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            // Title sits as high as possible; smallest white wrap behind it (no bar).
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.45f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            shadow =
                                Shadow(
                                    color = Color.White.copy(alpha = 0.35f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 6f,
                                ),
                        ),
                    color = TitlePlum,
                )
            }
        }
        Spacer(modifier = Modifier.size(44.dp))
    }
}

/**
 * One [season2_trex_puzzle_full] poster (Crop = fills board), drawn once; color is clipped per tile only.
 * Tile 1 = top-left (head), then 2…6 left-to-right, top-to-bottom.
 */
@Composable
private fun ContinuousPosterBoard(
    posterPainter: Painter,
    boardWidth: Dp,
    boardHeight: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    coloredThrough: Int,
    nextPlayableTile: Int,
    revealingTile: Int?,
    revealProgress: Float,
    isRevealing: Boolean,
    onTileTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val painter = posterPainter
    val grayFilter = remember { ColorFilter.colorMatrix(saturationColorMatrix(LOCKED_SATURATION)) }

    Box(
        modifier =
            modifier
                .size(boardWidth, boardHeight)
                .clip(RoundedCornerShape(8.dp)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size
            // Locked = sleeping fossil: near-zero saturation, slightly lower opacity, dusty mist on top.
            drawPosterCrop(painter, boardSize, grayFilter, alpha = 0.84f)
            drawRect(FossilShade.copy(alpha = 0.10f))
            drawRect(FossilDust.copy(alpha = 0.18f))
            drawRect(Color.White.copy(alpha = 0.08f))

            for (tile in 1..TILE_COUNT) {
                val isColored = tile <= coloredThrough
                val isThisRevealing = revealingTile == tile
                if (!isColored && !isThisRevealing) continue

                val saturation =
                    if (isThisRevealing) {
                        LOCKED_SATURATION + (1f - LOCKED_SATURATION) * revealProgress
                    } else {
                        1f
                    }
                val tileFilter = ColorFilter.colorMatrix(saturationColorMatrix(saturation))

                val clip = tileClipRect(tile, boardSize)
                clipRect(clip.left, clip.top, clip.right, clip.bottom) {
                    drawPosterCrop(painter, boardSize, tileFilter, alpha = 1f)
                }
            }
        }

        revealingTile?.let { tile ->
            if (revealProgress in 0.62f..0.92f) {
                val row = (tile - 1) / TILE_COLUMNS
                val col = (tile - 1) % TILE_COLUMNS
                TileSparkleOverlay(
                    phase = revealProgress,
                    modifier =
                        Modifier
                            .offset(x = cellWidth * col, y = cellHeight * row)
                            .size(cellWidth, cellHeight),
                )
            }
        }

        GridDividerOverlay(modifier = Modifier.fillMaxSize())

        for (tile in 1..TILE_COUNT) {
            val row = (tile - 1) / TILE_COLUMNS
            val col = (tile - 1) % TILE_COLUMNS
            val isNext = tile == (coloredThrough + 1).coerceIn(1, TILE_COUNT)
            val chapterFullyRevealed = coloredThrough >= TILE_COUNT
            // UX: replay any completed tile; pulse only the next uncompleted station while in progress.
            val enabled =
                !isRevealing &&
                    (
                        chapterFullyRevealed ||
                            tile <= coloredThrough ||
                            (tile == nextPlayableTile && isNext)
                    )
            TileInteractionLayer(
                tileIndex = tile,
                row = row,
                col = col,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                highlighted =
                    enabled &&
                        !chapterFullyRevealed &&
                        isNext &&
                        tile == nextPlayableTile,
                enabled = enabled,
                onTap = { onTileTap(tile) },
            )
        }
    }
}

@Composable
private fun GridDividerOverlay(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val stroke = with(density) { 1.dp.toPx() }
        val thirdW = size.width / TILE_COLUMNS
        val halfH = size.height / TILE_ROWS
        drawLine(GridLineColor, Offset(thirdW, 0f), Offset(thirdW, size.height), stroke)
        drawLine(GridLineColor, Offset(thirdW * 2f, 0f), Offset(thirdW * 2f, size.height), stroke)
        drawLine(GridLineColor, Offset(0f, halfH), Offset(size.width, halfH), stroke)
    }
}

@Composable
private fun TileInteractionLayer(
    tileIndex: Int,
    row: Int,
    col: Int,
    cellWidth: Dp,
    cellHeight: Dp,
    highlighted: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
) {
    val pulseTransition = rememberInfiniteTransition(label = "highlightPulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.55f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2_600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glowAlpha",
    )
    val borderPulse by pulseTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2_600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "borderPulse",
    )

    Box(
        modifier =
            Modifier
                .offset(x = cellWidth * col, y = cellHeight * row)
                .size(cellWidth, cellHeight)
                .then(
                    if (highlighted) {
                        Modifier.drawBehind {
                            val inset = 2.dp.toPx()
                            val stroke = 3.dp.toPx() * borderPulse
                            drawRoundRect(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                GlowAmber.copy(alpha = glowAlpha * 0.5f),
                                                Color.Transparent,
                                            ),
                                        center = center,
                                        radius = size.maxDimension * 0.9f,
                                    ),
                                topLeft = Offset(-inset, -inset),
                                size = Size(size.width + inset * 2, size.height + inset * 2),
                                cornerRadius = CornerRadius(4.dp.toPx()),
                            )
                            drawRoundRect(
                                color = GlowAmber.copy(alpha = glowAlpha),
                                topLeft = Offset(stroke / 2, stroke / 2),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(2.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                            )
                        }
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTap,
                ),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            text = tileIndex.toString(),
            modifier =
                Modifier
                    .padding(5.dp)
                    .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TileSparkleOverlay(
    phase: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val sparkleAlpha =
        when {
            phase < 0.62f -> 0f
            phase > 0.92f -> 0f
            else -> sin(((phase - 0.62f) / 0.3f) * PI).toFloat().coerceIn(0f, 1f) * 0.85f
        }
    if (sparkleAlpha <= 0.01f) return

    Canvas(modifier = modifier) {
        val r = with(density) { 3.5.dp.toPx() }
        val offsets =
            listOf(
                Offset(0.35f, 0.28f),
                Offset(0.62f, 0.38f),
                Offset(0.48f, 0.55f),
                Offset(0.72f, 0.62f),
                Offset(0.28f, 0.68f),
            )
        for ((i, rel) in offsets.withIndex()) {
            val twinkle = (0.55f + 0.45f * sin((phase * 12f + i) * PI).toFloat()).coerceIn(0f, 1f)
            val alpha = sparkleAlpha * twinkle
            val center = Offset(size.width * rel.x, size.height * rel.y)
            drawCircle(SparkleGold.copy(alpha = alpha), r * (0.85f + i * 0.06f), center)
            drawCircle(Color.White.copy(alpha = alpha * 0.6f), r * 0.4f, center)
        }
    }
}

private fun saturationColorMatrix(saturation: Float): ColorMatrix =
    ColorMatrix().apply { setToSaturation(saturation.coerceIn(0f, 1f)) }

/** Same Crop math for grayscale base and every tile clip — avoids a second misaligned dinosaur. */
private fun DrawScope.drawPosterCrop(
    painter: Painter,
    dstSize: Size,
    colorFilter: ColorFilter?,
    alpha: Float = 1f,
) {
    val intrinsic = painter.intrinsicSize
    if (!intrinsic.isSpecified || intrinsic.width <= 0f || intrinsic.height <= 0f) return

    val scale = max(dstSize.width / intrinsic.width, dstSize.height / intrinsic.height)
    val scaledW = intrinsic.width * scale
    val scaledH = intrinsic.height * scale
    val offsetX = (dstSize.width - scaledW) / 2f
    val offsetY = (dstSize.height - scaledH) / 2f

    translate(left = offsetX, top = offsetY) {
        with(painter) {
            draw(size = Size(scaledW, scaledH), alpha = alpha, colorFilter = colorFilter)
        }
    }
}

private fun tileClipRect(tile: Int, boardSize: Size): Rect {
    val col = (tile - 1) % TILE_COLUMNS
    val row = (tile - 1) / TILE_COLUMNS
    val cellW = boardSize.width / TILE_COLUMNS
    val cellH = boardSize.height / TILE_ROWS
    return Rect(
        left = cellW * col,
        top = cellH * row,
        right = cellW * (col + 1),
        bottom = cellH * (row + 1),
    )
}

private fun rtl(text: String): String = "\u200F$text"
