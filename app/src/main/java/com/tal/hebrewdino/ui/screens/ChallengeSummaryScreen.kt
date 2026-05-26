package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.flow.first

@Composable
fun ChallengeSummaryScreen(
    repo: CharacterRepository,
    onBackToDinoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var rewardDelta by remember { mutableIntStateOf(0) }

    LaunchedEffect(repo) {
        val delta = repo.pendingRewardFoodDeltaFlow.first().coerceAtLeast(0)
        rewardDelta = delta
        if (delta > 0) repo.clearPendingRewardFoodDelta()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.24f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = rtl("כל הכבוד!"),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.22f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = rtl("קיבלתם $rewardDelta תפוחים! 🍎"),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, fontSize = 26.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    )
                }
            }

            ThemedSummaryButton(
                text = "הולכים לדינו",
                onClick = onBackToDinoHome,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
            )
        }
    }
}

@Composable
private fun ThemedSummaryButton(
    text: String,
    onClick: () -> Unit,
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
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFFFFE27A).copy(alpha = 0.62f),
                                    Color(0xFFFFB82E).copy(alpha = 0.70f),
                                    Color(0xFFFF9A1A).copy(alpha = 0.76f),
                                ),
                        ),
                        shape = pillShape,
                    )
                    .background(Color.White.copy(alpha = 0.10f), shape = pillShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rtl(text),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF102A43),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
