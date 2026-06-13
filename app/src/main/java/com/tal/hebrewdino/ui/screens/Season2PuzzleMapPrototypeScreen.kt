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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import com.tal.hebrewdino.ui.domain.Season2Chapter1RevealOrder
import com.tal.hebrewdino.ui.domain.Season2ChapterRegistry
import com.tal.hebrewdino.ui.audio.GameAudioEngine
import com.tal.hebrewdino.ui.audio.LocalBackgroundMusic
import com.tal.hebrewdino.ui.audio.Season2StoryAudio
import com.tal.hebrewdino.ui.audio.withVoiceDuck
import com.tal.hebrewdino.ui.domain.Season2Copy
import com.tal.hebrewdino.ui.domain.Season2IntroFlow
import com.tal.hebrewdino.ui.domain.Season2MapEntryVoicePolicy
import com.tal.hebrewdino.ui.domain.Season2PuzzleMapTileClickPolicy
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private const val TILE_COLUMNS = 3
private const val TILE_ROWS = 2
private const val TILE_COUNT = TILE_COLUMNS * TILE_ROWS

private val TitleBrown = Color(0xFF4A3A2E)
private val GlowAmber = Color(0xFF2E7D32)
private val SparkleGold = Color(0xFFFFF2D6)
private val GridLineColor = Color.Black.copy(alpha = 0.12f)
private val MarkerGold = Color(0xFFCF9D4A)
private val MarkerGreen = Color(0xFF2E7D32)

private const val LOCKED_SATURATION = 0.02f
private val FossilDust = Color(0xFFB8AFA5)
private val FossilShade = Color(0xFF2B2724)
private val FrostOverlay = Color(0xFFF8F4EE)
private val FrostMist = Color(0xFFE8E0D4)

private fun season2PosterResForChapter(chapterId: Int): Int {
    val posterResId =
        Season2ChapterRegistry.posterResId(chapterId)
            ?: error("Chapter $chapterId has no poster — must not open puzzle map")
    return posterResId
}

/**
 * Season 2 puzzle map — manual reveal order, frosted mystery, station markers on completion.
 */
@Composable
fun Season2PuzzleMapPrototypeScreen(
    chapterId: Int,
    companionCharacter: DinoCharacter,
    playerAddress: PlayerAddress,
    onBack: () -> Unit,
    onOpenStation: (Int) -> Unit,
    requestChapterIntro: Boolean = false,
    onChapterIntroConsumed: () -> Unit = {},
    requestChapterCelebration: Boolean = false,
    onChapterCelebrationConsumed: () -> Unit = {},
    mapReturnCaptionEvent: Long = 0L,
    mapReturnCaptionCount: Int = 0,
    onMapReturnCaptionConsumed: () -> Unit = {},
    onRewardContinue: () -> Unit = onBack,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }
    val completedChapters by season2Progress.completedChaptersFlow.collectAsState(initial = emptySet())
    val completedStations by season2Progress.completedStationsFlow(chapterId).collectAsState(initial = emptySet())
    val puzzleMapExplainHeard by season2Progress.puzzleMapExplainHeardFlow(chapterId).collectAsState(initial = false)
    val posterResId = remember(chapterId) { season2PosterResForChapter(chapterId) }
    val posterPainter = painterResource(id = posterResId)
    val mapIntroLines =
        remember(chapterId, playerAddress) {
            Season2ChapterRegistry.chapter(chapterId)?.mapIntroStoryLines?.invoke(playerAddress)
                ?: Season2Copy.mapIntroStoryLines(playerAddress)
        }
    val isChapterCompleted =
        Season2Copy.isChapterComplete(
            chapterIndex = chapterId,
            completedChapters = completedChapters,
            completedStations = completedStations,
        )

    val revealedTiles =
        remember(completedStations) {
            Season2Chapter1RevealOrder.revealedPosterTiles(completedStations)
        }
    val nextStation =
        remember(completedStations) {
            Season2Chapter1RevealOrder.nextStation(completedStations)
        }
    val nextPlayablePosterTile =
        remember(nextStation) {
            nextStation?.let { Season2Chapter1RevealOrder.posterTileForStation(it) }
        }

    var revealingTile by remember { mutableStateOf<Int?>(null) }
    val revealProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val isRevealing = revealingTile != null

    var showIntro by remember(chapterId) { mutableStateOf(false) }
    var entryFromChapterSelect by remember(chapterId) { mutableStateOf(false) }
    var suppressMapEntryBecauseStationReturn by remember(chapterId) { mutableStateOf(false) }
    var progressHydrated by remember(chapterId) { mutableStateOf(false) }

    LaunchedEffect(requestChapterIntro, progressHydrated, isChapterCompleted) {
        if (!requestChapterIntro || !progressHydrated) return@LaunchedEffect
        entryFromChapterSelect = true
        suppressMapEntryBecauseStationReturn = false
        if (
            Season2IntroFlow.shouldShowChapterIntro(
                chapterId = chapterId,
                entryFromChapterSelect = true,
                chapterFullyRevealed = isChapterCompleted,
            )
        ) {
            showIntro = true
        }
        onChapterIntroConsumed()
    }
    var showCompletionCelebration by remember(chapterId) { mutableStateOf(false) }
    var mapReturnVoiceResId by remember { mutableIntStateOf(0) }
    var mapReturnVoiceEpoch by remember { mutableIntStateOf(0) }
    var lastMapPraiseRawResId by remember(chapterId) { mutableIntStateOf(0) }
    var previousStations by remember(chapterId) { mutableStateOf<Set<Int>?>(null) }

    fun playMapReturnVoiceForCompletedCount(completedCount: Int) {
        val voice =
            Season2StoryAudio.mapReturnVoice(
                completedCount = completedCount,
                companion = companionCharacter,
                avoidPraiseRawResId = lastMapPraiseRawResId,
            ) ?: return
        if (voice is Season2StoryAudio.MapReturnVoice.CompanionPraise) {
            lastMapPraiseRawResId = voice.rawResId
        }
        mapReturnVoiceResId = voice.rawResId
        mapReturnVoiceEpoch += 1
    }

    fun startReveal(tile: Int, triggerCelebrationOnFinish: Boolean) {
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
            if (triggerCelebrationOnFinish) {
                showCompletionCelebration = true
            }
        }
    }

    LaunchedEffect(requestChapterCelebration) {
        if (requestChapterCelebration) {
            showCompletionCelebration = true
            onChapterCelebrationConsumed()
        }
    }

    LaunchedEffect(chapterId, completedStations, isChapterCompleted, showIntro) {
        if (!progressHydrated) {
            previousStations = completedStations
            progressHydrated = true
            return@LaunchedEffect
        }

        val prev = previousStations ?: completedStations
        if (prev == completedStations) return@LaunchedEffect

        val added = completedStations - prev
        previousStations = completedStations

        if (added.isEmpty()) return@LaunchedEffect

        // Ignore bulk DataStore hydration (e.g. opening an already-completed chapter).
        if (added.size != 1) return@LaunchedEffect

        val newStation = added.single()
        val tile = Season2Chapter1RevealOrder.posterTileForStation(newStation)
        val triggerCelebration =
            Season2IntroFlow.shouldCelebrateFromStationProgress(
                addedStationCount = added.size,
                newStationId = newStation,
                previousCompletedCount = prev.size,
            )
        startReveal(tile, triggerCelebrationOnFinish = triggerCelebration)

        if (triggerCelebration) {
            season2Progress.markChapterCompleted(chapterId)
        }
    }

    val mapTitle = Season2Copy.puzzleMapTitle(chapterId, isChapterCompleted)
    val bgm = LocalBackgroundMusic.current
    val rawVoice = remember(context) { com.tal.hebrewdino.ui.audio.RawVoicePlayer(context = context.applicationContext) }
    var mapEntryInstructionSpoken by remember(chapterId) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            rawVoice.stopNow()
            rawVoice.release()
        }
    }

    LaunchedEffect(mapReturnCaptionEvent) {
        if (Season2MapEntryVoicePolicy.shouldSuppressBecauseStationReturn(mapReturnCaptionEvent)) {
            suppressMapEntryBecauseStationReturn = true
        }
    }

    LaunchedEffect(mapReturnCaptionEvent, showIntro) {
        if (mapReturnCaptionEvent == 0L || showIntro) return@LaunchedEffect
        if (mapReturnCaptionCount > 0) {
            rawVoice.stopNow()
            playMapReturnVoiceForCompletedCount(mapReturnCaptionCount)
            onMapReturnCaptionConsumed()
        }
    }

    LaunchedEffect(mapReturnVoiceEpoch, mapReturnVoiceResId, showIntro) {
        if (mapReturnVoiceResId == 0 || showIntro) return@LaunchedEffect
        bgm?.withVoiceDuck {
            rawVoice.playRawBlocking(mapReturnVoiceResId)
        }
    }

    LaunchedEffect(chapterId, progressHydrated, showIntro, mapReturnCaptionEvent) {
        if (
            !Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = progressHydrated,
                showChapterIntroOverlay = showIntro,
                entryFromChapterSelect = entryFromChapterSelect,
                mapReturnCaptionEvent = mapReturnCaptionEvent,
                mapEntryInstructionSpoken = mapEntryInstructionSpoken,
                suppressBecauseStationReturn = suppressMapEntryBecauseStationReturn,
            )
        ) {
            return@LaunchedEffect
        }
        val explainHeard = season2Progress.puzzleMapExplainHeardFlow(chapterId).first()
        val playPuzzleExplain =
            Season2MapEntryVoicePolicy.shouldPlayPuzzleExplainBeforeEntry(
                chapterId = chapterId,
                completedStationCount = completedStations.size,
                puzzleMapExplainHeard = explainHeard,
            )
        val mapEntryRawRes =
            Season2MapEntryVoicePolicy.mapEntryInstructionRawRes(
                chapterId = chapterId,
                chapterFullyRevealed = isChapterCompleted,
                nextPlayablePosterTile = nextPlayablePosterTile,
                entryFromChapterSelect = entryFromChapterSelect,
            )
        bgm?.withVoiceDuck {
            if (playPuzzleExplain) {
                rawVoice.playRawBlocking(Season2StoryAudio.PuzzleMapExplain)
            }
            rawVoice.playRawBlocking(mapEntryRawRes)
        }
        if (playPuzzleExplain) {
            season2Progress.markPuzzleMapExplainHeard(chapterId)
        }
        mapEntryInstructionSpoken = true
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(FossilShade),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding(),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boardWidth = maxWidth
                val boardHeight = maxHeight
                val cellWidth = boardWidth / TILE_COLUMNS
                val cellHeight = boardHeight / TILE_ROWS

                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ContinuousPosterBoard(
                            posterPainter = posterPainter,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            revealedTiles = revealedTiles,
                            nextPlayablePosterTile = nextPlayablePosterTile,
                            chapterFullyRevealed = isChapterCompleted,
                            revealingTile = revealingTile,
                            revealProgress = revealProgress.value,
                            isRevealing = isRevealing,
                            onPosterTileTap = { posterTile ->
                                if (!isRevealing) {
                                    val stationId =
                                        Season2Chapter1RevealOrder.stationForPosterTile(posterTile)
                                    onOpenStation(stationId)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Surface(
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .zIndex(3f),
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF1A1512).copy(alpha = 0.58f),
                            shadowElevation = 4.dp,
                        ) {
                            Text(
                                text = mapTitle,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                    ),
                                color = Color.White.copy(alpha = 0.96f),
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
            FloatingMapChrome(
                onBack = onBack,
                emphasizeBack = isChapterCompleted,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp, top = 2.dp)
                        .zIndex(4f),
            )
        }

        if (showIntro) {
            Season2MapIntroOverlay(
                chapterId = chapterId,
                companionCharacter = companionCharacter,
                playerAddress = playerAddress,
                storyLines = mapIntroLines,
                onContinue = { showIntro = false },
            )
        }

        if (showCompletionCelebration) {
            Season2ChapterCompleteOverlay(
                chapterId = chapterId,
                posterResId = posterResId,
                companionCharacter = companionCharacter,
                onContinue = {
                    showCompletionCelebration = false
                    onRewardContinue()
                },
                modifier = Modifier.zIndex(30f),
            )
        }
    }
}

@Composable
private fun FloatingMapChrome(
    onBack: () -> Unit,
    emphasizeBack: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val pulseTransition = rememberInfiniteTransition(label = "completedMapBackPulse")
    val backGlowAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.72f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1_400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "backGlowAlpha",
    )
    val backScale by pulseTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1_400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "backScale",
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(999.dp),
            modifier =
                if (emphasizeBack) {
                    Modifier
                        .drawBehind {
                            val glow = 6.dp.toPx() * backScale
                            drawRoundRect(
                                color = Color(0xFF66BB6A).copy(alpha = backGlowAlpha),
                                topLeft = Offset(-glow, -glow),
                                size = Size(size.width + glow * 2, size.height + glow * 2),
                                cornerRadius = CornerRadius(size.height),
                            )
                        }
                } else {
                    Modifier
                },
            color =
                if (emphasizeBack) {
                    Color(0xFF2E7D32)
                } else {
                    Color.White.copy(alpha = 0.55f)
                },
            shadowElevation = if (emphasizeBack) 8.dp else 0.dp,
        ) {
            Text(
                text = if (emphasizeBack) "\u200Fחזרה לפרקים" else "\u200Fחזור",
                modifier =
                    Modifier
                        .padding(
                            horizontal = if (emphasizeBack) 16.dp else 10.dp,
                            vertical = if (emphasizeBack) 9.dp else 5.dp,
                        ),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (emphasizeBack) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = if (emphasizeBack) 15.sp else 14.sp,
                    ),
                color = if (emphasizeBack) Color.White else TitleBrown,
            )
        }
    }
}

@Composable
private fun ContinuousPosterBoard(
    posterPainter: Painter,
    cellWidth: Dp,
    cellHeight: Dp,
    revealedTiles: Set<Int>,
    nextPlayablePosterTile: Int?,
    chapterFullyRevealed: Boolean,
    revealingTile: Int?,
    revealProgress: Float,
    isRevealing: Boolean,
    onPosterTileTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val painter = posterPainter
    val grayFilter = remember { ColorFilter.colorMatrix(saturationColorMatrix(LOCKED_SATURATION)) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size
            drawPosterCrop(painter, boardSize, grayFilter, alpha = 0.76f)
            drawRect(FossilShade.copy(alpha = 0.14f))
            drawRect(FossilDust.copy(alpha = 0.22f))
            drawRect(FrostOverlay.copy(alpha = 0.30f))
            drawRect(Color.White.copy(alpha = 0.14f))

            for (tile in 1..TILE_COUNT) {
                val isRevealed = tile in revealedTiles
                val isThisRevealing = revealingTile == tile
                if (!isRevealed && !isThisRevealing) continue

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

            for (tile in 1..TILE_COUNT) {
                val isRevealed = tile in revealedTiles
                val isThisRevealing = revealingTile == tile
                if (isRevealed || isThisRevealing) continue
                val clip = tileClipRect(tile, boardSize)
                val isHead = tile == Season2Chapter1RevealOrder.HEAD_POSTER_TILE
                clipRect(clip.left, clip.top, clip.right, clip.bottom) {
                    val frostA = if (isHead) 0.68f else 0.52f
                    val mistA = if (isHead) 0.52f else 0.38f
                    val whiteA = if (isHead) 0.42f else 0.30f
                    drawRect(FrostOverlay.copy(alpha = frostA))
                    drawRect(FrostMist.copy(alpha = mistA))
                    drawRect(Color.White.copy(alpha = whiteA))
                    if (isHead) {
                        drawRect(Color.White.copy(alpha = 0.22f))
                    }
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

        for (posterTile in 1..TILE_COUNT) {
            val row = (posterTile - 1) / TILE_COLUMNS
            val col = (posterTile - 1) % TILE_COLUMNS
            val isRevealed = posterTile in revealedTiles
            val enabled =
                Season2PuzzleMapTileClickPolicy.isTileClickable(
                    posterTile = posterTile,
                    revealedTiles = revealedTiles,
                    nextPlayablePosterTile = nextPlayablePosterTile,
                    chapterFullyRevealed = chapterFullyRevealed,
                    isRevealing = isRevealing,
                )
            val highlighted =
                Season2PuzzleMapTileClickPolicy.isNextTileHighlighted(
                    posterTile = posterTile,
                    nextPlayablePosterTile = nextPlayablePosterTile,
                    chapterFullyRevealed = chapterFullyRevealed,
                    isClickable = enabled,
                )
            val stationNumber = Season2Chapter1RevealOrder.stationForPosterTile(posterTile)
            TileInteractionLayer(
                row = row,
                col = col,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                highlighted = highlighted,
                enabled = enabled,
                replayStationNumber = stationNumber,
                onTap = { onPosterTileTap(posterTile) },
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
    row: Int,
    col: Int,
    cellWidth: Dp,
    cellHeight: Dp,
    highlighted: Boolean,
    enabled: Boolean,
    replayStationNumber: Int?,
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
    val markerPulse by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1_800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "markerPulse",
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
                                style = Stroke(width = stroke),
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
        contentAlignment = Alignment.BottomEnd,
    ) {
        replayStationNumber?.let { station ->
            ReplayStationNumberBadge(
                stationNumber = station,
                pulse = markerPulse,
                modifier = Modifier.padding(bottom = 4.dp, end = 4.dp),
            )
        }
    }
}

@Composable
private fun ReplayStationNumberBadge(
    stationNumber: Int,
    pulse: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f * pulse))
                .drawBehind {
                    drawCircle(
                        color = MarkerGreen.copy(alpha = 0.22f * pulse),
                        radius = size.minDimension * 0.55f,
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u200F$stationNumber",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = MarkerGold.copy(alpha = 0.98f),
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

private fun DrawScope.drawPosterCrop(
    painter: Painter,
    dstSize: Size,
    colorFilter: ColorFilter?,
    alpha: Float = 1f,
) {
    val intrinsic = painter.intrinsicSize
    if (!intrinsic.isSpecified || intrinsic.width <= 0f || intrinsic.height <= 0f) return

    // Cover-crop wide posters into the full map area — colorful fill, center crop, sides OK to trim.
    val scale = maxOf(dstSize.width / intrinsic.width, dstSize.height / intrinsic.height)
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
