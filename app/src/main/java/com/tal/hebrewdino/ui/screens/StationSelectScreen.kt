package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.domain.HebrewSyllabus
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val activeChapter by repo.activeChapterIndexFlow.collectAsState(initial = 0)
    val highestUnlocked by repo.highestUnlockedChapterIndexFlow.collectAsState(initial = 0)
    val maxCompletedStation by repo.chapterMaxCompletedStationFlow(activeChapter).collectAsState(initial = 0)
    val tts = androidx.compose.runtime.remember(context) { TextToSpeechManager.get(context.applicationContext) }

    val chapter = HebrewSyllabus.chapterOrNull(activeChapter) ?: HebrewSyllabus.chapters.first()
    val station2Unlocked = maxCompletedStation >= 1
    val station3Unlocked = maxCompletedStation >= 2
    val nextStationId =
        when {
            maxCompletedStation < 1 -> 1
            maxCompletedStation < 2 -> 2
            maxCompletedStation < 3 -> 3
            else -> 3
        }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .padding(top = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                HebrewSyllabus.chapters.forEach { ch ->
                    val locked = ch.index > highestUnlocked
                    val selected = ch.index == activeChapter
                    ChapterTab(
                        label = ch.title,
                        selected = selected,
                        locked = locked,
                        onClick = {
                            if (locked) {
                                scope.launch {
                                    tts.speak("קודם צריך לסיים את הפרק הקודם!")
                                }
                            } else {
                                scope.launch { repo.setActiveChapterIndex(ch.index) }
                            }
                        },
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.94f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = rtl("${chapter.title} · ${chapter.subtitle}"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = rtl(HebrewSyllabus.lettersLabel(chapter.letters)),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 28.sp),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = rtl("בחרו תחנה"),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = Color(0xFFFFE27A),
            )

            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StationCard(
                    title = rtl("זיהוי אות — תחנה 1"),
                    subtitle = rtl("איפה האות הנכונה?"),
                    buttonText = rtl("התחל"),
                    locked = false,
                    highlighted = nextStationId == 1,
                    onClick = { onOpenWordChallengeStation(1) },
                    onLockedAttempt = { },
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                )
                StationCard(
                    title = rtl("בידוד צליל — תחנה 2"),
                    subtitle = rtl("חַפְּשׂוּ אֶת הַצְּלִיל..."),
                    buttonText = rtl("התחל"),
                    locked = !station2Unlocked,
                    highlighted = nextStationId == 2 && station2Unlocked,
                    onClick = { onOpenWordChallengeStation(2) },
                    onLockedAttempt = {
                        scope.launch {
                            tts.speak("קודם צריך לסיים את התחנה הקודמת!")
                        }
                    },
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                )
                StationCard(
                    title = rtl("אתגר האותיות — תחנה 3"),
                    subtitle = rtl("תפסו את האות הנכונה"),
                    buttonText = rtl("התחל"),
                    locked = !station3Unlocked,
                    highlighted = nextStationId == 3 && station3Unlocked,
                    onClick = onOpenFallingLettersStation3,
                    onLockedAttempt = {
                        scope.launch {
                            tts.speak("קודם צריך לסיים את התחנה הקודמת!")
                        }
                    },
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                )
            }
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
                Text("חזור", style = ChapterNavChipStyles.labelTextStyle())
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
private fun ChapterTab(
    label: String,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg =
        when {
            locked -> Color.Black.copy(alpha = 0.22f)
            selected -> Color(0xFF2DB86E).copy(alpha = 0.75f)
            else -> Color.Black.copy(alpha = 0.18f)
        }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier.alpha(if (locked) 0.55f else 1f),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (locked) {
                Text(text = "🔒", style = MaterialTheme.typography.labelSmall)
            }
            Text(
                text = rtl(label),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = if (selected) Color.White else Color.White.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun StationCard(
    title: String,
    subtitle: String,
    buttonText: String,
    locked: Boolean,
    highlighted: Boolean,
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

    val pulseScale =
        if (highlighted && !locked) {
            val idle = rememberInfiniteTransition(label = "station_pulse")
            idle.animateFloat(
                initialValue = 1f,
                targetValue = 1.04f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 900, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "scale",
            ).value
        } else {
            1f
        }

    val cardAlpha = if (locked) 0.5f else 1f
    val baseColor =
        when {
            locked -> Color.White.copy(alpha = 0.45f)
            highlighted -> Color(0xFF2DB86E).copy(alpha = 0.88f)
            else -> Color.White.copy(alpha = 0.72f)
        }
    val titleColor =
        when {
            locked -> Color(0xFF0B2B3D).copy(alpha = 0.4f)
            highlighted -> Color.White
            else -> Color(0xFF0B2B3D)
        }
    val subtitleColor =
        when {
            locked -> Color(0xFF0B2B3D).copy(alpha = 0.32f)
            highlighted -> Color.White.copy(alpha = 0.92f)
            else -> Color(0xFF0B2B3D).copy(alpha = 0.86f)
        }
    val ctaColor =
        when {
            locked -> Color(0xFF2DB86E).copy(alpha = 0.35f)
            highlighted -> Color.White
            else -> Color(0xFF2DB86E)
        }

  Surface(
        shape = RoundedCornerShape(14.dp),
        color = baseColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier =
            modifier
                .scale(pulseScale)
                .alpha(cardAlpha)
                .graphicsLayer { translationX = shakeX.value }
                .then(
                    if (highlighted && !locked) {
                        Modifier.border(3.dp, Color(0xFFFFE27A), RoundedCornerShape(14.dp))
                    } else {
                        Modifier
                    },
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
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
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = subtitleColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = ctaColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (locked) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Black.copy(alpha = 0.18f),
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                ) {
                    Text(
                        text = "🔒",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
