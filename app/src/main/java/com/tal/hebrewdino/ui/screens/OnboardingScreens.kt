package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.companion.StoryReadablePanel
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.layout.ScreenFit

@Composable
fun OnboardingCompanionScreen(
    onNext: (DinoCharacter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<DinoCharacter?>(null) }
    OnboardingShell(
        title = "\u200Fבחרו חבר או חברה למסע",
        continueLabel = "\u200Fהבא",
        continueEnabled = selected != null,
        onContinue = { selected?.let(onNext) },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            CompanionPickCard(
                label = "\u200Fדינו",
                previewRes = CompanionAssets.forCharacter(DinoCharacter.Dino).poseIdle,
                selected = selected == DinoCharacter.Dino,
                onClick = { selected = DinoCharacter.Dino },
            )
            CompanionPickCard(
                label = "\u200Fדינה",
                previewRes = CompanionAssets.forCharacter(DinoCharacter.Dina).poseIdle,
                selected = selected == DinoCharacter.Dina,
                onClick = { selected = DinoCharacter.Dina },
            )
        }
    }
}

@Composable
fun OnboardingPlayerAddressScreen(
    onStart: (PlayerAddress) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<PlayerAddress?>(null) }
    OnboardingShell(
        title = "\u200Fאיך לפנות אליכם?",
        continueLabel = "\u200Fהתחלה",
        continueEnabled = selected != null,
        onContinue = { selected?.let(onStart) },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            TextPickCard(
                label = "\u200Fשחקן",
                selected = selected == PlayerAddress.Boy,
                onClick = { selected = PlayerAddress.Boy },
            )
            TextPickCard(
                label = "\u200Fשחקנית",
                selected = selected == PlayerAddress.Girl,
                onClick = { selected = PlayerAddress.Girl },
            )
        }
    }
}

@Composable
private fun OnboardingShell(
    title: String,
    continueLabel: String,
    continueEnabled: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(if (isCompact) 16.dp else 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            StoryReadablePanel(modifier = Modifier.fillMaxWidth(if (isCompact) 1f else 0.72f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF1A2E3D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 28.dp))
            content()
        }
        Button(
            onClick = onContinue,
            enabled = continueEnabled,
            modifier =
                Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
        ) {
            Text(text = continueLabel, style = MaterialTheme.typography.titleMedium)
        }
    }
    }
}

@Composable
private fun CompanionPickCard(
    label: String,
    previewRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Color(0xFF2AA6C9) else Color(0xFFE0E0E0)
    Surface(
        modifier =
            modifier
                .size(width = 140.dp, height = 168.dp)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF8E8),
        shadowElevation = if (selected) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(3.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = previewRes),
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A2E3D),
            )
        }
    }
}

@Composable
private fun TextPickCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Color(0xFF2AA6C9) else Color(0xFFE0E0E0)
    Surface(
        modifier =
            modifier
                .size(width = 140.dp, height = 100.dp)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF8E8),
        shadowElevation = if (selected) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(3.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A2E3D),
            )
        }
    }
}
