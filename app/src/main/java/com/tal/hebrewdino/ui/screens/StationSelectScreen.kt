package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding

@Composable
fun StationSelectScreen(
    onBackToDinoHome: () -> Unit,
    onOpenParents: () -> Unit,
    onOpenWordChallengeStation: (Int) -> Unit,
    onOpenFallingLettersStation3: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = androidx.compose.runtime.remember(context) { CharacterRepository(context.applicationContext) }
    val maxCompletedStation by repo.chapter1MaxCompletedStationFlow.collectAsState(initial = 0)
    val tts = androidx.compose.runtime.remember(context) { TextToSpeechManager.get(context.applicationContext) }
    DisposableEffect(tts) { onDispose { tts.stop() } }

    val station2Unlocked = maxCompletedStation >= 1
    val station3Unlocked = maxCompletedStation >= 2

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        val scroll = rememberScrollState()
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = rtl("בחרו תחנה"),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = androidx.compose.ui.graphics.Color(0xFF0B2B3D),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))

            StationCard(
                title = rtl("זיהוי אות — תחנה 1"),
                subtitle = rtl("איפה האות הנכונה?"),
                buttonText = rtl("התחל"),
                locked = false,
                onClick = { onOpenWordChallengeStation(1) },
                onLockedAttempt = { },
            )
            StationCard(
                title = rtl("בידוד צליל — תחנה 2"),
                subtitle = rtl("איזו אות עושה את הצליל?"),
                buttonText = rtl("התחל"),
                locked = !station2Unlocked,
                onClick = { onOpenWordChallengeStation(2) },
                onLockedAttempt = { tts.speak("קודם צריך לסיים את התחנה הקודמת!") },
            )
            StationCard(
                title = rtl("אתגר האותיות — תחנה 3"),
                subtitle = rtl("תפסו את האות הנכונה"),
                buttonText = rtl("התחל"),
                locked = !station3Unlocked,
                onClick = onOpenFallingLettersStation3,
                onLockedAttempt = { tts.speak("קודם צריך לסיים את התחנה הקודמת!") },
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 4.dp, start = 8.dp),
        ) {
            OutlinedButton(
                onClick = onBackToDinoHome,
                colors = ChapterNavChipStyles.outlinedButtonColors(),
            ) {
                Text("יציאה", style = ChapterNavChipStyles.labelTextStyle())
            }
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .topChromeInsetsPadding()
                    .padding(top = 4.dp, end = 8.dp),
        ) {
            OutlinedButton(
                onClick = onOpenParents,
                colors = ChapterNavChipStyles.outlinedButtonColors(),
            ) {
                Text("הורים", style = ChapterNavChipStyles.labelTextStyle())
            }
        }
    }
}

@Composable
private fun StationCard(
    title: String,
    subtitle: String,
    buttonText: String,
    locked: Boolean,
    onClick: () -> Unit,
    onLockedAttempt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shakeX = androidx.compose.runtime.remember { Animatable(0f) }
    var shakeEpoch by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    LaunchedEffect(shakeEpoch) {
        if (shakeEpoch == 0) return@LaunchedEffect
        shakeX.snapTo(0f)
        shakeX.animateTo(-10f, tween(45))
        shakeX.animateTo(10f, tween(80))
        shakeX.animateTo(-7f, tween(70))
        shakeX.animateTo(7f, tween(70))
        shakeX.animateTo(0f, tween(55))
    }

    val cardAlpha = if (locked) 0.55f else 1f
    val baseColor = if (locked) Color.White.copy(alpha = 0.62f) else Color.White.copy(alpha = 0.78f)
    val titleColor = if (locked) Color(0xFF0B2B3D).copy(alpha = 0.45f) else Color(0xFF0B2B3D)
    val subtitleColor = if (locked) Color(0xFF0B2B3D).copy(alpha = 0.35f) else Color(0xFF0B2B3D).copy(alpha = 0.86f)
    val ctaColor = if (locked) Color(0xFF2DB86E).copy(alpha = 0.35f) else Color(0xFF2DB86E)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = baseColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
            modifier
                .fillMaxWidth(0.88f)
                .alpha(cardAlpha)
                .graphicsLayer { translationX = shakeX.value },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (locked) {
                            shakeEpoch += 1
                            onLockedAttempt()
                        } else {
                            onClick()
                        }
                    },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = titleColor,
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = subtitleColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ctaColor,
                )
            }

            if (locked) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Black.copy(alpha = 0.18f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp),
                ) {
                    Text(
                        text = "🔒",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
