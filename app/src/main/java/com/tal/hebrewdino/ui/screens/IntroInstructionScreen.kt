package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.domain.DinoStoryScripts
import com.tal.hebrewdino.ui.domain.introInstructionSpokenForTts
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding

@Composable
fun IntroInstructionScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
    var continued by remember { mutableStateOf(false) }

    fun continueOnce() {
        if (continued) return
        continued = true
        onContinue()
    }

    LaunchedEffect(Unit) {
        tts.speakFully(introInstructionSpokenForTts())
    }

    DisposableEffect(Unit) {
        onDispose { tts.stop() }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(onClick = ::continueOnce),
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 4.dp, start = 8.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ChapterNavChipStyles.outlinedButtonColors(),
            ) {
                Text("חזור", style = ChapterNavChipStyles.labelTextStyle())
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .topChromeInsetsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.28f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DinoStoryScripts.part1IntroDisplayLines().forEach { line ->
                        Text(
                            text = rtl(line),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    lineHeight = 32.sp,
                                ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = rtl("המשיכו ←"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
