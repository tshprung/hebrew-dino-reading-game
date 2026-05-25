package com.tal.hebrewdino.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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

    fun feedOnce() {
        if (foodCount <= 0) return
        foodCount -= 1
        isHungry = false
        if (growthStage == GrowthStage.ADULT) return

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
    val titleText by remember(character) {
        derivedStateOf {
            when (character) {
                DinoCharacter.Dina -> "Dina"
                DinoCharacter.Dino -> "Dino"
                null -> "Dino"
            }
        }
    }

    val stageText by remember(character, viewModel) {
        derivedStateOf {
            val name =
                when (character) {
                    DinoCharacter.Dina -> "Dina"
                    DinoCharacter.Dino -> "Dino"
                    null -> "Dino"
                }
            when (viewModel.growthStage) {
                GrowthStage.EGG -> "🥚 Big Blue Egg"
                GrowthStage.BABY -> "🦖 Cute Baby $name"
                GrowthStage.ADULT -> "🦕 Grown Up $name"
            }
        }
    }

    DinoHomeContent(
        titleText = titleText,
        stageText = stageText,
        foodCount = viewModel.foodCount,
        isHungry = viewModel.isHungry,
        growthProgress01 = viewModel.growthProgress01,
        canFeed = viewModel.foodCount > 0,
        onGoOnMission = onGoOnMission,
        onFeed = { viewModel.feedOnce() },
        modifier = modifier,
    )
}

@Composable
private fun DinoHomeContent(
    titleText: String,
    stageText: String,
    foodCount: Int,
    isHungry: Boolean,
    growthProgress01: Float,
    canFeed: Boolean,
    onGoOnMission: () -> Unit,
    onFeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopBarFoodCount(
            foodCount = foodCount,
            modifier = Modifier.fillMaxWidth(),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFEAF2FF),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$titleText Home",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                        ),
                    color = Color(0xFF102A43),
                    textAlign = TextAlign.Center,
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFFFFFFFF), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stageText,
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                            ),
                        color = Color(0xFF102A43),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                }

                LinearProgressIndicator(
                    progress = { growthProgress01.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF3CB371),
                    trackColor = Color(0xFFD6E6D9),
                )

                Text(
                    text = if (isHungry) "Hungry" else "Full",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isHungry) Color(0xFFB00020) else Color(0xFF2E7D32),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onGoOnMission,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(58.dp),
            ) {
                Text(
                    text = "Go on a Mission!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                )
            }

            if (canFeed) {
                Button(
                    onClick = onFeed,
                    modifier =
                        Modifier
                            .width(120.dp)
                            .height(58.dp),
                ) {
                    Text(
                        text = "Feed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(120.dp))
            }
        }
    }
}

@Composable
private fun TopBarFoodCount(
    foodCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🍎",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = foodCount.toString(),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                ),
            color = Color(0xFF102A43),
        )
    }
}
