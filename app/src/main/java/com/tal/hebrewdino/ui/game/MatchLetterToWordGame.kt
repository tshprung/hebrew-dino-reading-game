package com.tal.hebrewdino.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.components.learning.captionFontSizeForWordCard
import com.tal.hebrewdino.ui.components.learning.LessonChoiceCard
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.LessonWordIllustrations
import com.tal.hebrewdino.ui.layout.ScreenFit
import kotlin.math.roundToInt
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MatchLetterToWordGame(
    choices: List<LessonChoice>,
    contentKey: Int = 0,
    enabled: Boolean,
    /** Narrower tiles with more horizontal space between the two columns. */
    compactWideSpread: Boolean = false,
    /** Scales only the illustration inside the picture card (same idea as [ImageMatchGame]). */
    innerPictureScaleForChoice: (LessonChoice) -> Float = { ch ->
        when {
            ch.word == "מדוזה" || ch.id == "w_מ_3" || ch.tileDrawable == R.drawable.lesson_pic_medusa -> 0.5f
            else -> 1f
        }
    },
    /** Same caption sizing as [ImageMatchGame] (`cardW * 0.22f * multiplier`, bounds scaled). */
    captionSizeMultiplier: Float = 1f,
    /** Persistent instructions shown at the top (RTL). */
    instructions: String = "חברו מילה לתמונה",
    /** Called whenever the player taps a word card (choice id). */
    onWordPressed: ((choiceId: String) -> Unit)? = null,
    /** Called whenever the player taps a letter tile. */
    onLetterPressed: ((letter: String) -> Unit)? = null,
    /** Called when the player attempts a match (true=correct, false=wrong). */
    onMatchAttempt: ((correct: Boolean) -> Unit)? = null,
    /** Called when a correct match is made (choice id). */
    onCorrectMatch: ((choiceId: String) -> Unit)? = null,
    /** Called when a wrong match is attempted (picked letter + picked word choice id). */
    onWrongMatch: ((pickedLetter: String, pickedChoiceId: String) -> Unit)? = null,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxPairs = choices.take(3)
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    var selectedChoiceId by remember { mutableStateOf<String?>(null) }
    val locked = remember { mutableStateMapOf<String, String>() } // letter -> choiceId
    val shake = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    val boardScale = remember(contentKey) { Animatable(1f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var wrongAttemptsThisRound by remember(contentKey) { mutableIntStateOf(0) }
    var hintEpoch by remember(contentKey) { mutableIntStateOf(0) }
    var hintLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var hintChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var correctEpoch by remember(contentKey) { mutableIntStateOf(0) }
    var correctLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var correctChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashEpoch by remember(contentKey) { mutableIntStateOf(0) }
    var wrongFlashLetter by remember(contentKey) { mutableStateOf<String?>(null) }
    var wrongFlashChoiceId by remember(contentKey) { mutableStateOf<String?>(null) }

    val letterRects = remember { mutableStateMapOf<String, Rect>() }
    val itemRects = remember { mutableStateMapOf<String, Rect>() } // choiceId -> rect
        var boardOriginInRoot by remember(contentKey) { mutableStateOf(Offset.Zero) }

    val (wordColumn, letterColumn) =
        remember(maxPairs, contentKey) {
            val words = maxPairs.shuffled(Random(contentKey * 7919L + maxPairs.hashCode()))
            val baseLetters = maxPairs.map { it.letter }.distinct()
            // Avoid accidental “same index” alignment between letter i and word i (feels patterned).
            var letters = baseLetters.shuffled(Random(contentKey * 3571L + 17))
            repeat(12) { k ->
                val aligned =
                    letters.indices.any { i ->
                        val w = words.getOrNull(i) ?: return@any false
                        letters[i] == w.letter
                    }
                if (!aligned) return@repeat
                letters = baseLetters.shuffled(Random(contentKey * 3571L + 17 + k * 31))
            }
            words to letters
        }

    LaunchedEffect(locked.size) {
        if (locked.size == maxPairs.size && maxPairs.isNotEmpty()) {
            // End-of-round closure: quick glow + small board bounce, but never block advancing.
            scope.launch {
                glow.snapTo(0f)
                glow.animateTo(1f, tween(120))
                glow.animateTo(0f, tween(160))
            }
            scope.launch {
                boardScale.snapTo(1f)
                boardScale.animateTo(1.06f, tween(90))
                boardScale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = 520f))
            }
            onSolved()
        }
    }

    fun isLockedLetter(letter: String): Boolean = locked.containsKey(letter)
    fun isLockedChoice(choiceId: String): Boolean = locked.values.contains(choiceId)

    fun shakeWrongAndClear() {
        selectedLetter = null
        selectedChoiceId = null
        scope.launch {
            shake.snapTo(0f)
            shake.animateTo(16f, tween(55))
            shake.animateTo(-12f, tween(55))
            shake.animateTo(8f, tween(55))
            shake.animateTo(0f, tween(80))
        }
    }

    fun tryLockMatch(letter: String, choice: LessonChoice) {
        if (isLockedLetter(letter) || isLockedChoice(choice.id)) return
        if (choice.letter == letter) {
            correctEpoch += 1
            correctLetter = letter
            correctChoiceId = choice.id
            onCorrectMatch?.invoke(choice.id)
            onMatchAttempt?.invoke(true)
            locked[letter] = choice.id
            selectedLetter = null
            selectedChoiceId = null
        } else {
            wrongFlashEpoch += 1
            wrongFlashLetter = letter
            wrongFlashChoiceId = choice.id
            onWrongMatch?.invoke(letter, choice.id)
            onMatchAttempt?.invoke(false)
            wrongAttemptsThisRound += 1
            if (wrongAttemptsThisRound >= 2) {
                // Hint: pulse the correct pair for the tapped word (word + its real starting letter).
                hintLetter = choice.letter
                hintChoiceId = choice.id
                hintEpoch += 1
            }
            shakeWrongAndClear()
        }
    }

    // This station is Hebrew-first; enforce RTL so “start/end” and column sides are unambiguous.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val innerW = maxWidth
        val innerH = maxHeight
        val isLandscape = innerW > innerH

        // Header stays pinned; content below scales down if needed so nothing is clipped.
        val headerPadTop = 6.dp
        val headerPadBottom = 10.dp
        val headerFont = if (compactWideSpread) 22.sp else 26.sp
        val headerH = headerPadTop + headerPadBottom + if (compactWideSpread) 34.dp else 40.dp
        val bottomSafe = 18.dp

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = instructions,
                fontSize = headerFont,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = headerPadTop, bottom = headerPadBottom, start = 12.dp, end = 12.dp),
            )

            val availableH = (innerH - headerH - bottomSafe).coerceAtLeast(1.dp)

            // Estimate needed height so nothing clips. We scale the whole board uniformly.
            val landscapePictureLetterGap = 56.dp * 1.8f
            val rowNeeds =
                if (isLandscape) {
                    // pictures row + letters row + gaps
                    (110.dp + 6.dp + 26.dp) + landscapePictureLetterGap + 88.dp + 12.dp
                } else {
                    // 3 stacked picture cards with captions + some spacing
                    val perItemH = 110.dp + 6.dp + 26.dp + 12.dp
                    (perItemH * 3) + 8.dp
                }
            val scaleToFit = min(1f, availableH.value / rowNeeds.value)

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(availableH)
                        .scale(scaleToFit),
                contentAlignment = Alignment.TopCenter,
            ) {
        val gap = 12.dp
        val tileH = 88.dp
        val tileShape = RoundedCornerShape(22.dp)
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        val density = LocalDensity.current
        val lineInsetPx = with(density) { 6.dp.toPx() }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .scale(boardScale.value)
                    .onGloballyPositioned { coords ->
                        boardOriginInRoot = coords.positionInRoot()
                    },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                locked.forEach { (letter, choiceId) ->
                    val lr = letterRects[letter]
                    val ir = itemRects[choiceId]
                    val (a, b) =
                        if (isLandscape) {
                            // Landscape: pictures on top row, letters below. Connect vertically (bottom of picture → top of letter).
                            val from =
                                ir?.let { Offset(it.center.x, it.bottom - lineInsetPx) }
                            val to =
                                lr?.let { Offset(it.center.x, it.top + lineInsetPx) }
                            from to to
                        } else {
                            // Portrait: columns side-by-side. Connect between the “inside edges”.
                            val from =
                                lr?.let {
                                    val x = if (isRtl) it.left + lineInsetPx else it.right - lineInsetPx
                                    Offset(x, it.center.y)
                                }
                            val to =
                                ir?.let {
                                    val x = if (isRtl) it.right - lineInsetPx else it.left + lineInsetPx
                                    Offset(x, it.center.y)
                                }
                            from to to
                        }
                    if (a != null && b != null) {
                        val localA = a - boardOriginInRoot
                        val localB = b - boardOriginInRoot
                        drawLine(
                            color = Color(0xFF7E57C2).copy(alpha = 0.95f),
                            start = localA,
                            end = localB,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round,
                        )
                        if (glow.value > 0f) {
                            drawLine(
                                color = Color(0xFFB39DDB).copy(alpha = 0.70f * glow.value),
                                start = localA,
                                end = localB,
                                strokeWidth = 18f,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }

            if (isLandscape) {
                // Landscape: pictures row, then letters row underneath.
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .offset { IntOffset(shake.value.roundToInt(), 0) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Match station 5 sizing (computed width + fixed aspect ratio).
                    val cardW =
                        ScreenFit.rowChildWidthDp(
                            rowInnerWidth = innerW,
                            count = 3,
                            gap = gap,
                            minEach = 72.dp,
                            maxEach = 168.dp,
                        )
                    val cardH = cardW * (110f / 160f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    ) {
                        wordColumn.forEach { ch ->
                            val lockedThis = isLockedChoice(ch.id)
                            val selectedThis = selectedChoiceId == ch.id
                            val pop = remember(ch.id, contentKey) { Animatable(1f) }
                            val wrongFlash = remember(ch.id, contentKey) { Animatable(0f) }
                            LaunchedEffect(hintEpoch, hintChoiceId, ch.id, contentKey) {
                                if (hintEpoch <= 0 || hintChoiceId != ch.id) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.12f, tween(120))
                                pop.animateTo(1f, tween(160))
                            }
                            LaunchedEffect(correctEpoch, correctChoiceId, ch.id, contentKey) {
                                if (correctEpoch <= 0 || correctChoiceId != ch.id) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.18f, tween(90))
                                pop.animateTo(1f, tween(140))
                            }
                            LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, ch.id, contentKey) {
                                if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != ch.id) return@LaunchedEffect
                                wrongFlash.snapTo(1f)
                                wrongFlash.animateTo(0f, tween(220))
                            }
                            val captionSp =
                                captionFontSizeForWordCard(
                                    density = density,
                                    cardWidth = cardW,
                                    word = ch.word,
                                    sizeMultiplier = captionSizeMultiplier,
                                )
                            LessonChoiceCard(
                                choice = ch,
                                enabled = enabled && !lockedThis,
                                scale = pop.value,
                                showWordCaption = true,
                                cardWidth = cardW,
                                cardHeight = cardH,
                                captionFontSize = captionSp,
                                innerPictureScale = innerPictureScaleForChoice(ch),
                                isCorrectPick = lockedThis,
                                isSelected = !lockedThis && selectedThis,
                                wrongFlashAlpha = wrongFlash.value,
                                onClick = {
                                    if (!enabled || lockedThis) return@LessonChoiceCard
                                    onWordPressed?.invoke(ch.id)
                                    val picked = selectedLetter
                                    if (picked != null) {
                                        tryLockMatch(picked, ch)
                                    } else {
                                        selectedChoiceId = if (selectedChoiceId == ch.id) null else ch.id
                                    }
                                },
                                modifier =
                                    Modifier.onGloballyPositioned { coords ->
                                        val p = coords.positionInRoot()
                                        itemRects[ch.id] = Rect(p, Size(coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(landscapePictureLetterGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        letterColumn.forEach { letter ->
                            val lockedThis = isLockedLetter(letter)
                            val selected = selectedLetter == letter
                            val pop = remember(letter, contentKey) { Animatable(1f) }
                            val wrongFlash = remember(letter, contentKey) { Animatable(0f) }
                            LaunchedEffect(hintEpoch, hintLetter, letter, contentKey) {
                                if (hintEpoch <= 0 || hintLetter != letter) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.14f, tween(120))
                                pop.animateTo(1f, tween(160))
                            }
                            LaunchedEffect(correctEpoch, correctLetter, letter, contentKey) {
                                if (correctEpoch <= 0 || correctLetter != letter) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.20f, tween(90))
                                pop.animateTo(1f, tween(140))
                            }
                            LaunchedEffect(wrongFlashEpoch, wrongFlashLetter, letter, contentKey) {
                                if (wrongFlashEpoch <= 0 || wrongFlashLetter != letter) return@LaunchedEffect
                                wrongFlash.snapTo(1f)
                                wrongFlash.animateTo(0f, tween(220))
                            }
                            Box(
                                modifier =
                                    Modifier
                                        .width(cardW)
                                        .height(tileH)
                                        .scale(pop.value)
                                        .background(
                                            when {
                                                // Keep a solid light face after match; thin green tint reads as “done” without losing the tile.
                                                lockedThis -> Color(0xFFE8F5E9).copy(alpha = 0.98f)
                                                selected -> Color(0xFFC8E6C9).copy(alpha = 0.95f)
                                                else -> Color.White.copy(alpha = 0.88f)
                                            },
                                            tileShape,
                                        )
                                        .border(
                                            2.dp,
                                            when {
                                                wrongFlash.value > 0.01f -> Color(0xFFE53935).copy(alpha = 0.95f)
                                                lockedThis -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                                                selected -> Color(0xFF2E7D32).copy(alpha = 0.70f)
                                                else -> Color(0xFF0B2B3D).copy(alpha = 0.14f)
                                            },
                                            tileShape,
                                        )
                                        .clickable(enabled = enabled && !lockedThis) {
                                            onLetterPressed?.invoke(letter)
                                            val nowSelected = if (selectedLetter == letter) null else letter
                                            selectedLetter = nowSelected
                                            val pickedChoiceId = selectedChoiceId
                                            if (nowSelected != null && pickedChoiceId != null) {
                                                val choice = wordColumn.firstOrNull { it.id == pickedChoiceId }
                                                if (choice != null) {
                                                    tryLockMatch(nowSelected, choice)
                                                } else {
                                                    selectedChoiceId = null
                                                }
                                            }
                                        }
                                        .onGloballyPositioned { coords ->
                                            val p = coords.positionInRoot()
                                            letterRects[letter] = Rect(p, Size(coords.size.width.toFloat(), coords.size.height.toFloat()))
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (wrongFlash.value > 0.01f) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFE53935).copy(alpha = 0.18f * wrongFlash.value), tileShape),
                                    )
                                }
                                Text(
                                    text = letter,
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0B2B3D),
                                )
                            }
                        }
                    }
                }
            } else {
                // Portrait: vertical side-by-side columns (current behavior).
                val baseHalf = (innerW - gap) / 2f
                val layoutInset = if (compactWideSpread) innerW * 0.12f else 0.dp
                val layoutInsetEnd = if (compactWideSpread) innerW * 0.10f else 0.dp
                val wordColW =
                    if (compactWideSpread) (baseHalf * 0.5f).coerceAtLeast(56.dp) else baseHalf.coerceAtLeast(140.dp)
                val letterColW =
                    if (compactWideSpread) (baseHalf * 0.25f).coerceAtLeast(44.dp) else wordColW

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = (if (compactWideSpread) 4.dp else 10.dp) + layoutInset,
                                end = (if (compactWideSpread) 4.dp else 10.dp) + layoutInsetEnd,
                            )
                            .offset { IntOffset(shake.value.roundToInt(), 0) },
                    horizontalArrangement = if (compactWideSpread) Arrangement.SpaceBetween else Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.width(letterColW),
                        verticalArrangement = Arrangement.spacedBy(if (compactWideSpread) 60.dp else 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        letterColumn.forEach { letter ->
                            val lockedThis = isLockedLetter(letter)
                            val selected = selectedLetter == letter
                            val pop = remember(letter, contentKey) { Animatable(1f) }
                            val wrongFlash = remember(letter, contentKey) { Animatable(0f) }
                            LaunchedEffect(hintEpoch, hintLetter, letter, contentKey) {
                                if (hintEpoch <= 0 || hintLetter != letter) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.14f, tween(120))
                                pop.animateTo(1f, tween(160))
                            }
                            LaunchedEffect(correctEpoch, correctLetter, letter, contentKey) {
                                if (correctEpoch <= 0 || correctLetter != letter) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.20f, tween(90))
                                pop.animateTo(1f, tween(140))
                            }
                            LaunchedEffect(wrongFlashEpoch, wrongFlashLetter, letter, contentKey) {
                                if (wrongFlashEpoch <= 0 || wrongFlashLetter != letter) return@LaunchedEffect
                                wrongFlash.snapTo(1f)
                                wrongFlash.animateTo(0f, tween(220))
                            }
                            Box(
                                modifier =
                                    Modifier
                                        .width(letterColW)
                                        .height(tileH)
                                        .scale(pop.value)
                                        .background(
                                            when {
                                                lockedThis -> Color(0xFFE8F5E9).copy(alpha = 0.98f)
                                                selected -> Color(0xFFC8E6C9).copy(alpha = 0.95f)
                                                else -> Color.White.copy(alpha = 0.88f)
                                            },
                                            tileShape,
                                        )
                                        .border(
                                            2.dp,
                                            when {
                                                wrongFlash.value > 0.01f -> Color(0xFFE53935).copy(alpha = 0.95f)
                                                lockedThis -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                                                selected -> Color(0xFF2E7D32).copy(alpha = 0.70f)
                                                else -> Color(0xFF0B2B3D).copy(alpha = 0.14f)
                                            },
                                            tileShape,
                                        )
                                        .clickable(enabled = enabled && !lockedThis) {
                                            onLetterPressed?.invoke(letter)
                                            val nowSelected = if (selectedLetter == letter) null else letter
                                            selectedLetter = nowSelected
                                            val pickedChoiceId = selectedChoiceId
                                            if (nowSelected != null && pickedChoiceId != null) {
                                                val choice = wordColumn.firstOrNull { it.id == pickedChoiceId }
                                                if (choice != null) {
                                                    tryLockMatch(nowSelected, choice)
                                                } else {
                                                    selectedChoiceId = null
                                                }
                                            }
                                        }
                                        .onGloballyPositioned { coords ->
                                            val p = coords.positionInRoot()
                                            letterRects[letter] = Rect(p, Size(coords.size.width.toFloat(), coords.size.height.toFloat()))
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (wrongFlash.value > 0.01f) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFE53935).copy(alpha = 0.18f * wrongFlash.value), tileShape),
                                    )
                                }
                                Text(text = letter, fontSize = 46.sp, fontWeight = FontWeight.Black, color = Color(0xFF0B2B3D))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.width(wordColW),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        wordColumn.forEach { ch ->
                            val lockedThis = isLockedChoice(ch.id)
                            val selectedThis = selectedChoiceId == ch.id
                            // Match station 5 sizing for the card inside the word column.
                            val cardW = (wordColW * 0.86f).coerceAtMost(168.dp).coerceAtLeast(72.dp)
                            val cardH = cardW * (110f / 160f)
                            val captionSp =
                                captionFontSizeForWordCard(
                                    density = density,
                                    cardWidth = cardW,
                                    word = ch.word,
                                    sizeMultiplier = captionSizeMultiplier,
                                )
                            val pop = remember(ch.id, contentKey) { Animatable(1f) }
                            val wrongFlash = remember(ch.id, contentKey) { Animatable(0f) }
                            LaunchedEffect(hintEpoch, hintChoiceId, ch.id, contentKey) {
                                if (hintEpoch <= 0 || hintChoiceId != ch.id) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.12f, tween(120))
                                pop.animateTo(1f, tween(160))
                            }
                            LaunchedEffect(correctEpoch, correctChoiceId, ch.id, contentKey) {
                                if (correctEpoch <= 0 || correctChoiceId != ch.id) return@LaunchedEffect
                                pop.snapTo(1f)
                                pop.animateTo(1.18f, tween(90))
                                pop.animateTo(1f, tween(140))
                            }
                            LaunchedEffect(wrongFlashEpoch, wrongFlashChoiceId, ch.id, contentKey) {
                                if (wrongFlashEpoch <= 0 || wrongFlashChoiceId != ch.id) return@LaunchedEffect
                                wrongFlash.snapTo(1f)
                                wrongFlash.animateTo(0f, tween(220))
                            }
                            LessonChoiceCard(
                                choice = ch,
                                enabled = enabled && !lockedThis,
                                showWordCaption = true,
                                scale = pop.value,
                                cardWidth = cardW,
                                cardHeight = cardH,
                                captionFontSize = captionSp,
                                innerPictureScale = innerPictureScaleForChoice(ch),
                                isCorrectPick = lockedThis,
                                isSelected = !lockedThis && selectedThis,
                                wrongFlashAlpha = wrongFlash.value,
                                onClick = {
                                    if (!enabled || lockedThis) return@LessonChoiceCard
                                    onWordPressed?.invoke(ch.id)
                                    val picked = selectedLetter
                                    if (picked != null) {
                                        tryLockMatch(picked, ch)
                                    } else {
                                        selectedChoiceId = if (selectedChoiceId == ch.id) null else ch.id
                                    }
                                },
                                modifier =
                                    Modifier.onGloballyPositioned { coords ->
                                        val p = coords.positionInRoot()
                                        itemRects[ch.id] = Rect(p, Size(coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    },
                            )
                        }
                    }
                }
            }
        }
            }
        }
    }
    }
}

@Composable
private fun MatchWordPicture(
    choice: LessonChoice,
    width: Dp,
    height: Dp,
    lockedThis: Boolean,
) {
    val density = LocalDensity.current
    when {
        choice.tileDrawable == R.drawable.lesson_word_tile -> {
            Box(
                modifier =
                    Modifier
                        .size(width = width, height = height)
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
                            .background(Color.White.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
        choice.tileDrawable == R.drawable.lesson_pic_placeholder -> {
            val emoji = LessonWordIllustrations.emojiForWord(choice.word)
            val emojiSp =
                with(density) {
                    (height.toPx() * 0.55f).coerceIn(40f * fontScale, 72f * fontScale).toSp()
                }
            Box(
                modifier =
                    Modifier
                        .size(width = width, height = height)
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
                Text(text = emoji, fontSize = emojiSp, textAlign = TextAlign.Center, modifier = Modifier.alpha(if (lockedThis) 0.55f else 1f))
            }
        }
        else -> {
            // Use Crop to normalize padding differences between assets (so all pictures read the same size).
            val innerScale =
                when {
                    choice.word == "מדוזה" || choice.id == "w_מ_3" || choice.tileDrawable == R.drawable.lesson_pic_medusa -> 0.5f
                    else -> 1f
                }
            Image(
                painter = painterResource(id = choice.tileDrawable),
                contentDescription = choice.word,
                modifier =
                    Modifier
                        .size(width = width, height = height)
                        .clip(RoundedCornerShape(18.dp))
                        .graphicsLayer {
                            scaleX = innerScale
                            scaleY = innerScale
                        },
                contentScale = ContentScale.Crop,
                alpha = if (lockedThis) 0.55f else 1f,
            )
        }
    }
}
