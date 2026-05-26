package com.tal.hebrewdino.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.tal.hebrewdino.R

enum class GrowthStage {
    EGG,
    BABY,
    ADULT,
}

class DinoHomeViewModel(context: Context) : ViewModel() {
    private val repo: CharacterRepository = CharacterRepository(context.applicationContext)

    val character: StateFlow<DinoCharacter> =
        repo.characterFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DinoCharacter.DINO_GREEN,
        )

    var growthStage: GrowthStage by mutableStateOf(GrowthStage.EGG)
        private set

    var foodCount: Int by mutableIntStateOf(0)
        private set

    var totalFoodEarned: Int by mutableIntStateOf(0)
        private set

    var fedFoodTotal: Int by mutableIntStateOf(0)
        private set

    var isHungry: Boolean by mutableStateOf(false)
        private set

    var growthProgress01: Float by mutableFloatStateOf(0f)
        private set

    var hatchEpoch: Int by mutableIntStateOf(0)
        private set

    var growEpoch: Int by mutableIntStateOf(0)
        private set

    var eggTapEpoch: Int by mutableIntStateOf(0)
        private set

    private var storedStage: GrowthStage = GrowthStage.EGG
    private var fullUntilAtMs: Long = 0L

    private fun updateHungerFromStore() {
        if (growthStage == GrowthStage.EGG) {
            isHungry = false
            return
        }
        val until = fullUntilAtMs.coerceAtLeast(0L)
        isHungry = until <= 0L
    }

    private fun recomputeProgressAndStage() {
        val fed = (totalFoodEarned - foodCount).coerceAtLeast(0)
        if (fedFoodTotal != fed) fedFoodTotal = fed
        val computed = computeGrowthStage(fed)
        val previous = growthStage
        if (growthStage != computed) {
            growthStage = computed
            if (previous == GrowthStage.EGG && computed == GrowthStage.BABY) {
                hatchEpoch += 1
            }
            if (previous == GrowthStage.BABY && computed == GrowthStage.ADULT) {
                growEpoch += 1
            }
            viewModelScope.launch { repo.setGrowthStage(computed.name) }
        }
        growthProgress01 = computeGrowthProgress01(fed, growthStage)
        updateHungerFromStore()
    }

    init {
        viewModelScope.launch {
            repo.ensureTamagotchiInitialized()
        }
        viewModelScope.launch {
            repo.foodCountFlow.collect { stored ->
                val normalized = stored.coerceAtLeast(0)
                if (foodCount != normalized) foodCount = normalized
                recomputeProgressAndStage()
            }
        }
        viewModelScope.launch {
            repo.growthStageFlow.collect { stored ->
                val stage = parseGrowthStage(stored)
                storedStage = stage
            }
        }
        viewModelScope.launch {
            repo.fullUntilAtMsFlow.collect { stored ->
                fullUntilAtMs = stored.coerceAtLeast(0L)
                updateHungerFromStore()
            }
        }
        viewModelScope.launch {
            repo.totalFoodEarnedFlow.collect { stored ->
                val normalized = stored.coerceAtLeast(0)
                if (totalFoodEarned != normalized) totalFoodEarned = normalized
                recomputeProgressAndStage()
            }
        }
    }

    fun feedOnce() {
        if (foodCount <= 0) return
        foodCount -= 1
        recomputeProgressAndStage()
        val until = Long.MAX_VALUE
        fullUntilAtMs = until
        updateHungerFromStore()
        viewModelScope.launch {
            repo.setFoodCount(foodCount)
            repo.setFullUntilAtMs(until)
        }
    }

    fun onEggTapped() {
        eggTapEpoch += 1
    }

    private fun parseGrowthStage(name: String): GrowthStage =
        try {
            GrowthStage.valueOf(name)
        } catch (_: Throwable) {
            GrowthStage.EGG
        }

    private fun computeGrowthStage(total: Int): GrowthStage =
        when {
            total < 3 -> GrowthStage.EGG
            total < 11 -> GrowthStage.BABY
            else -> GrowthStage.ADULT
        }

    private fun computeGrowthProgress01(total: Int, stage: GrowthStage): Float =
        when (stage) {
            GrowthStage.EGG -> (total / 3f).coerceIn(0f, 1f)
            GrowthStage.BABY -> ((total - 3) / 8f).coerceIn(0f, 1f)
            GrowthStage.ADULT -> 1f
        }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DinoHomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DinoHomeViewModel(context) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}

@Composable
fun DinoHomeScreen(
    viewModel: DinoHomeViewModel,
    onGoOnMission: () -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val character by viewModel.character.collectAsState()
    var eatPulseEpoch by remember { mutableIntStateOf(0) }

    DinoHomeContent(
        character = character,
        foodCount = viewModel.foodCount,
        totalFoodEarned = viewModel.totalFoodEarned,
        fedFoodTotal = viewModel.fedFoodTotal,
        isHungry = viewModel.isHungry,
        growthProgress01 = viewModel.growthProgress01,
        growthStage = viewModel.growthStage,
        canFeed = viewModel.foodCount > 0,
        onGoOnMission = onGoOnMission,
        onBackToMap = onBackToMap,
        hatchEpoch = viewModel.hatchEpoch,
        growEpoch = viewModel.growEpoch,
        eggTapEpoch = viewModel.eggTapEpoch,
        onEggTapped = viewModel::onEggTapped,
        eatPulseEpoch = eatPulseEpoch,
        onFeed = {
            viewModel.feedOnce()
            eatPulseEpoch += 1
        },
        modifier = modifier,
    )
}

@Composable
private fun DinoHomeContent(
    character: DinoCharacter,
    foodCount: Int,
    totalFoodEarned: Int,
    fedFoodTotal: Int,
    isHungry: Boolean,
    growthProgress01: Float,
    growthStage: GrowthStage,
    canFeed: Boolean,
    onGoOnMission: () -> Unit,
    onBackToMap: () -> Unit,
    hatchEpoch: Int,
    growEpoch: Int,
    eggTapEpoch: Int,
    onEggTapped: () -> Unit,
    eatPulseEpoch: Int,
    onFeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eatScale = remember { Animatable(1f) }
    LaunchedEffect(eatPulseEpoch) {
        if (eatPulseEpoch == 0) return@LaunchedEffect
        eatScale.animateTo(1.08f, tween(durationMillis = 110))
        eatScale.animateTo(1f, tween(durationMillis = 140))
    }

    val context = LocalContext.current
    val sfx = remember(context) { SfxManager(context.applicationContext) }
    val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
    val scope = rememberCoroutineScope()
    DisposableEffect(sfx) {
        onDispose { sfx.release() }
    }
    DisposableEffect(tts) {
        onDispose { tts.stop() }
    }
    val speakStatus =
        remember(isHungry, growthStage, fedFoodTotal) {
            {
                if (isHungry) {
                    tts.speak("דינו רעב! תאכילו אותו בתפוחים.")
                } else {
                    when (growthStage) {
                        GrowthStage.EGG -> {
                            val remaining = (3 - fedFoodTotal).coerceAtLeast(0)
                            if (remaining > 0) {
                                tts.speak("עוד $remaining תפוחים והביצה תבקע!")
                            } else {
                                tts.speak("דינו מלא ושמח!")
                            }
                        }
                        GrowthStage.BABY -> {
                            val remaining = (11 - fedFoodTotal).coerceAtLeast(0)
                            if (remaining > 0) {
                                tts.speak("עוד $remaining תפוחים ודינו יגדל!")
                            } else {
                                tts.speak("דינו מלא ושמח!")
                            }
                        }
                        GrowthStage.ADULT -> tts.speak("דינו מלא ושמח!")
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        speakStatus()
    }
    val onEggTap =
        remember(onEggTapped, scope, sfx) {
            {
                scope.launch { sfx.playEggTap() }
                speakStatus()
                onEggTapped()
            }
        }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, start = 12.dp),
        ) {
            MapChip(onClick = onBackToMap)
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, end = 12.dp),
        ) {
            ApplesChip(foodCount = foodCount)
        }

        HungerChip(
            growthStage = growthStage,
            isHungry = isHungry,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 58.dp, start = 12.dp),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            DinoCenterVisual(
                character = character,
                growthStage = growthStage,
                hatchEpoch = hatchEpoch,
                growEpoch = growEpoch,
                eggTapEpoch = eggTapEpoch,
                onEggTapped = onEggTap,
                onDinoTapped = speakStatus,
                scale = eatScale.value,
                modifier = Modifier.weight(1f, fill = true),
            )

            GrowthBar(
                growthProgress01 = growthProgress01,
                modifier =
                    Modifier
                        .clickable(onClick = speakStatus)
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
            )

            GrowthHint(
                growthStage = growthStage,
                fedFoodTotal = fedFoodTotal,
                modifier =
                    Modifier
                        .clickable(onClick = speakStatus)
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ThemedActionButton(
                    text = "האכיל",
                    onClick = onFeed,
                    enabled = canFeed,
                    modifier =
                        Modifier
                            .fillMaxWidth(0.46f)
                            .height(52.dp),
                    colors =
                        listOf(
                            Color(0xFFB6F2C1).copy(alpha = 0.70f),
                            Color(0xFF59D98E).copy(alpha = 0.74f),
                            Color(0xFF2DB86E).copy(alpha = 0.78f),
                        ),
                )

                ThemedActionButton(
                    text = "!צא למשימה",
                    onClick = onGoOnMission,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                    colors =
                        listOf(
                            Color(0xFFFFE27A).copy(alpha = 0.62f),
                            Color(0xFFFFB82E).copy(alpha = 0.70f),
                            Color(0xFFFF9A1A).copy(alpha = 0.76f),
                        ),
                )
            }
        }
    }
}

@Composable
private fun HungerChip(
    growthStage: GrowthStage,
    isHungry: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.28f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val label =
                when {
                    growthStage == GrowthStage.EGG -> "ביצה"
                    isHungry -> "רעב"
                    else -> "מלא ושמח"
                }
            val labelColor =
                when {
                    growthStage == GrowthStage.EGG -> Color(0xFFFFE27A)
                    isHungry -> Color(0xFFFF6B6B)
                    else -> Color(0xFFB6F2C1)
                }
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = labelColor,
            )
        }
    }
}

@Composable
private fun MapChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.34f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = rtl("למפות"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ApplesChip(
    foodCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.28f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "תפוחים: $foodCount",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White,
            )
            Text(text = "🍎", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DinoCenterVisual(
    character: DinoCharacter,
    growthStage: GrowthStage,
    hatchEpoch: Int,
    growEpoch: Int,
    eggTapEpoch: Int,
    onEggTapped: () -> Unit,
    onDinoTapped: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val dinoColorFilter =
            if (character == DinoCharacter.DINA_PINK) {
                ColorFilter.tint(Color(0xFFFF4FB3).copy(alpha = 0.55f))
            } else {
                null
            }

        val eggRes =
            if (character == DinoCharacter.DINA_PINK) {
                R.drawable.egg_pink
            } else {
                R.drawable.egg_white
            }

        val eggShake = remember { Animatable(0f) }
        LaunchedEffect(eggTapEpoch) {
            if (eggTapEpoch == 0) return@LaunchedEffect
            eggShake.snapTo(0f)
            eggShake.animateTo(-7f, tween(40))
            eggShake.animateTo(7f, tween(70))
            eggShake.animateTo(-5f, tween(60))
            eggShake.animateTo(5f, tween(60))
            eggShake.animateTo(0f, tween(50))
        }

        var hatchVisible by remember { mutableStateOf(false) }
        val hatchAlpha = remember { Animatable(0f) }
        LaunchedEffect(hatchEpoch) {
            if (hatchEpoch == 0) return@LaunchedEffect
            hatchVisible = true
            hatchAlpha.snapTo(1f)
            hatchAlpha.animateTo(0f, tween(650))
            hatchVisible = false
        }

        val growScale = remember { Animatable(1f) }
        LaunchedEffect(growEpoch) {
            if (growEpoch == 0) return@LaunchedEffect
            growScale.snapTo(0.86f)
            growScale.animateTo(1f, tween(260))
        }

        when (growthStage) {
            GrowthStage.EGG -> {
                val eggSize = maxWidth.coerceAtMost(280.dp)
                val idle = rememberInfiniteTransition(label = "egg_idle")
                val idleRot by idle.animateFloat(
                    initialValue = -1.6f,
                    targetValue = 1.6f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(durationMillis = 1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "rot",
                )
                val idleScale by idle.animateFloat(
                    initialValue = 0.985f,
                    targetValue = 1.015f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(durationMillis = 980, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "scale",
                )
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = eggRes),
                    contentDescription = "ביצה",
                    modifier =
                        Modifier
                            .size(eggSize)
                            .graphicsLayer {
                                rotationZ = idleRot + eggShake.value
                                scaleX = idleScale
                                scaleY = idleScale
                            }
                            .clickable(onClick = onEggTapped),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }

            GrowthStage.BABY,
            GrowthStage.ADULT,
            -> {
                val base = maxWidth.coerceAtMost(280.dp)
                val size = if (growthStage == GrowthStage.BABY) base * 0.72f else base
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.dino_idle),
                    contentDescription = if (character == DinoCharacter.DINA_PINK) "דינה" else "דינו",
                    modifier =
                        Modifier
                            .size(size)
                            .scale(scale * growScale.value)
                            .clickable(onClick = onDinoTapped),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    colorFilter = dinoColorFilter,
                )
                if (hatchVisible && hatchAlpha.value > 0f) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = eggRes),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(base)
                                .alpha(hatchAlpha.value)
                                .graphicsLayer { rotationZ = eggShake.value * 0.6f },
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthBar(
    growthProgress01: Float,
    modifier: Modifier = Modifier,
) {
    val pulse =
        rememberInfiniteTransition(label = "growth_pulse")
            .animateFloat(
                initialValue = 0.88f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween<Float>(durationMillis = 650),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "pulse",
            )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.26f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "גדילה",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            LinearProgressIndicator(
                progress = { growthProgress01.coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .alpha(pulse.value),
                color = Color(0xFF3CB371),
                trackColor = Color.White.copy(alpha = 0.25f),
            )
        }
    }
}

@Composable
private fun GrowthHint(
    growthStage: GrowthStage,
    fedFoodTotal: Int,
    modifier: Modifier = Modifier,
) {
    val text =
        when (growthStage) {
            GrowthStage.EGG -> {
                val remaining = (3 - fedFoodTotal).coerceAtLeast(0)
                if (remaining <= 0) {
                    "הביצה בוקעת!"
                } else if (remaining == 1) {
                    "עוד תפוח אחד והביצה בוקעת!"
                } else {
                    "עוד $remaining תפוחים והביצה בוקעת!"
                }
            }
            GrowthStage.BABY -> {
                val remaining = (11 - fedFoodTotal).coerceAtLeast(0)
                if (remaining <= 0) {
                    "דינו גדל!"
                } else if (remaining == 1) {
                    "עוד תפוח אחד ודינו גדל!"
                } else {
                    "עוד $remaining תפוחים ודינו גדל!"
                }
            }
            GrowthStage.ADULT -> "דינו בוגר!"
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.22f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = rtl(text),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ThemedActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(999.dp)
    Surface(
        modifier = modifier.alpha(if (enabled) 1f else 0.55f),
        shape = pillShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                    .background(
                        Brush.verticalGradient(colors = colors),
                        shape = pillShape,
                    )
                    .background(Color.White.copy(alpha = 0.10f), shape = pillShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rtl(text),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF102A43),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
