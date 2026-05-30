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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
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
    var navigationLocked by remember { mutableStateOf(false) }
    OnboardingShell(
        title = "\u200Fבחרו חבר או חברה למסע",
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
                onClick = {
                    if (navigationLocked) return@CompanionPickCard
                    navigationLocked = true
                    selected = DinoCharacter.Dino
                    onNext(DinoCharacter.Dino)
                },
            )
            CompanionPickCard(
                label = "\u200Fדינה",
                previewRes = CompanionAssets.forCharacter(DinoCharacter.Dina).poseIdle,
                selected = selected == DinoCharacter.Dina,
                onClick = {
                    if (navigationLocked) return@CompanionPickCard
                    navigationLocked = true
                    selected = DinoCharacter.Dina
                    onNext(DinoCharacter.Dina)
                },
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
    var navigationLocked by remember { mutableStateOf(false) }
    OnboardingShell(
        title = "\u200Fאיך לפנות אליכם?",
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            AddressPickCard(
                label = "\u200Fשחקן",
                variant = AddressCardVariant.Boy,
                selected = selected == PlayerAddress.Boy,
                onClick = {
                    if (navigationLocked) return@AddressPickCard
                    navigationLocked = true
                    selected = PlayerAddress.Boy
                    onStart(PlayerAddress.Boy)
                },
            )
            AddressPickCard(
                label = "\u200Fשחקנית",
                variant = AddressCardVariant.Girl,
                selected = selected == PlayerAddress.Girl,
                onClick = {
                    if (navigationLocked) return@AddressPickCard
                    navigationLocked = true
                    selected = PlayerAddress.Girl
                    onStart(PlayerAddress.Girl)
                },
            )
        }
    }
}

@Composable
private fun OnboardingShell(
    title: String,
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
            StoryReadablePanel(
                modifier =
                    Modifier
                            .fillMaxWidth(if (isCompact) 0.90f else 0.72f)
                            .widthIn(max = 480.dp),
            ) {
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
            Spacer(modifier = Modifier.height(16.dp))
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

private enum class AddressCardVariant {
    Boy,
    Girl,
}

@Composable
private fun AddressPickCard(
    label: String,
    variant: AddressCardVariant,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val borderColor = if (selected) Color(0xFF2AA6C9) else Color(0xFFE0E0E0)
    // TODO: Add real child illustrations in res/drawable/onboarding_boy.png and res/drawable/onboarding_girl.png
    val resName =
        when (variant) {
            AddressCardVariant.Boy -> "onboarding_boy"
            AddressCardVariant.Girl -> "onboarding_girl"
        }
    val placeholderLabel =
        when (variant) {
            AddressCardVariant.Boy -> "\u200Fאיור ילד"
            AddressCardVariant.Girl -> "\u200Fאיור ילדה"
        }
    val resId =
        remember(resName) {
            context.resources.getIdentifier(resName, "drawable", context.packageName)
        }
    Surface(
        modifier =
            modifier
                .size(width = 148.dp, height = 168.dp)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFF8E8),
        shadowElevation = if (selected) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(3.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.size(92.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEEF5FF),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(92.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1A2E3D).copy(alpha = 0.10f)),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = placeholderLabel,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1A2E3D).copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A2E3D),
                textAlign = TextAlign.Center,
            )
        }
    }
}
