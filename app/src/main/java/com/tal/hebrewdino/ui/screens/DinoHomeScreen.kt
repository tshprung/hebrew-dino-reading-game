package com.tal.hebrewdino.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
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

    val character: StateFlow<DinoCharacter?> =
        repo.characterFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    var growthStage: GrowthStage by mutableStateOf(GrowthStage.EGG)
        private set

    var foodCount: Int by mutableIntStateOf(3)
        private set

    var isHungry: Boolean by mutableStateOf(true)
        private set

    var growthProgress01: Float by mutableFloatStateOf(0f)
        private set

    init {
        viewModelScope.launch {
            repo.ensureTamagotchiInitialized()
        }
        viewModelScope.launch {
            repo.foodCountFlow.collect { stored ->
                val normalized = stored.coerceAtLeast(0)
                if (foodCount != normalized) foodCount = normalized
            }
        }
        viewModelScope.launch {
            repo.growthStageFlow.collect { stored ->
                val stage = parseGrowthStage(stored)
                if (growthStage != stage) growthStage = stage
            }
        }
    }

    fun feedOnce() {
        if (foodCount <= 0) return
        foodCount -= 1
        isHungry = false
        if (growthStage == GrowthStage.ADULT) {
            val newFoodCount = foodCount
            val newGrowthStage = growthStage
            viewModelScope.launch {
                repo.setFoodCount(newFoodCount)
                repo.setGrowthStage(newGrowthStage.name)
            }
            return
        }

        val next = (growthProgress01 + 0.34f).coerceAtMost(1f)
        growthProgress01 = next
        if (next >= 1f) {
            growthProgress01 = 0f
            isHungry = true
            growthStage =
                when (growthStage) {
                    GrowthStage.EGG -> GrowthStage.BABY
                    GrowthStage.BABY -> GrowthStage.ADULT
                    GrowthStage.ADULT -> GrowthStage.ADULT
                }
        }
        val newFoodCount = foodCount
        val newGrowthStage = growthStage
        viewModelScope.launch {
            repo.setFoodCount(newFoodCount)
            repo.setGrowthStage(newGrowthStage.name)
        }
    }

    private fun parseGrowthStage(name: String): GrowthStage =
        try {
            GrowthStage.valueOf(name)
        } catch (_: Throwable) {
            GrowthStage.EGG
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
    modifier: Modifier = Modifier,
) {
    val character by viewModel.character.collectAsState()
    var eatPulseEpoch by remember { mutableIntStateOf(0) }

    DinoHomeContent(
        character = character,
        foodCount = viewModel.foodCount,
        isHungry = viewModel.isHungry,
        growthProgress01 = viewModel.growthProgress01,
        canFeed = viewModel.foodCount > 0,
        onGoOnMission = onGoOnMission,
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
    character: DinoCharacter?,
    foodCount: Int,
    isHungry: Boolean,
    growthProgress01: Float,
    canFeed: Boolean,
    onGoOnMission: () -> Unit,
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

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        HungerChip(
            isHungry = isHungry,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, start = 12.dp),
        )

        ApplesChip(
            foodCount = foodCount,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, end = 12.dp),
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
                scale = eatScale.value,
                modifier = Modifier.weight(1f, fill = true),
            )

            GrowthBar(
                growthProgress01 = growthProgress01,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (canFeed) {
                    ThemedActionButton(
                        text = "האכיל",
                        onClick = onFeed,
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
                }

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
            Text(
                text = if (isHungry) "רעב" else "מלא",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = if (isHungry) Color(0xFFFF6B6B) else Color(0xFFB6F2C1),
            )
        }
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
    character: DinoCharacter?,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val dinoSize = maxWidth.coerceAtMost(280.dp)
        val dinoColorFilter =
            if (character == DinoCharacter.Dina) {
                ColorFilter.tint(Color(0xFFFFC1E3).copy(alpha = 0.28f))
            } else {
                null
            }

        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.dino_idle),
            contentDescription = if (character == DinoCharacter.Dina) "דינה" else "דינו",
            modifier = Modifier.size(dinoSize).scale(scale),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            colorFilter = dinoColorFilter,
        )
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
private fun ThemedActionButton(
    text: String,
    onClick: () -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(999.dp)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = pillShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(colors = colors),
                        shape = pillShape,
                    )
                    .background(Color.White.copy(alpha = 0.10f), shape = pillShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF102A43),
                textAlign = TextAlign.Center,
            )
        }
    }
}
