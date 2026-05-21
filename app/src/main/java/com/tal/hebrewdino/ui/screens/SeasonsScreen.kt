package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.R

private data class SeasonCardConfig(
    val seasonId: Int,
    val title: String,
    val subtitle: String,
    val status: String? = null,
    val enabled: Boolean,
)

@Composable
fun SeasonsScreen(
    onOpenSeason1: () -> Unit,
    onBackToOpening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val seasons =
        listOf(
            SeasonCardConfig(
                seasonId = 1,
                title = "עונה 1: המסע הראשון",
                subtitle = "6 פרקים + אימון",
                enabled = true,
            ),
            SeasonCardConfig(
                seasonId = 2,
                title = "עונה 2: מילים חדשות",
                subtitle = "עוד אותיות, עוד מילים, ועוד משחקים",
                status = "בפיתוח",
                enabled = false,
            ),
            SeasonCardConfig(
                seasonId = 3,
                title = "עונה 3: קוראים יותר",
                subtitle = "תרגול מתקדם יותר עם מילים מוכרות",
                status = "בפיתוח",
                enabled = false,
            ),
            SeasonCardConfig(
                seasonId = 4,
                title = "עונה 4: הרפתקה חדשה",
                subtitle = "עוד מסע עם דינו והאותיות",
                status = "בפיתוח",
                enabled = false,
            ),
        )

    Box(modifier = modifier.fillMaxSize()) {
        SeasonsAmbientBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                TopPillButton(
                    text = "חזור",
                    onClick = onBackToOpening,
                    modifier = Modifier.align(Alignment.CenterStart),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                )
                Text(
                    text = "בחר עונה",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                seasons.forEach { season ->
                    SeasonCard(
                        config = season,
                        onClick =
                            when {
                                season.seasonId == 1 -> onOpenSeason1
                                else -> null
                            },
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun TopPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .shadow(6.dp, RoundedCornerShape(999.dp))
                .clip(RoundedCornerShape(999.dp)),
        color = Color.White.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF102A43),
            )
        }
    }
}

@Composable
private fun SeasonCard(
    config: SeasonCardConfig,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val enabled = config.enabled && onClick != null
    val cardAlpha = if (enabled) 1f else 0.72f
    val outlineColor = if (enabled) Color(0xFF2E7D32) else Color(0xFF607D8B)
    val bg =
        if (enabled) {
            Brush.verticalGradient(listOf(Color(0xFFFFF4DA), Color(0xFFE8F7FF)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFF3F6F8), Color(0xFFE9EEF2)))
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = if (enabled) 10.dp else 4.dp, shape = shape, clip = false)
                .clip(shape)
                .background(bg)
                .border(width = 2.dp, color = outlineColor.copy(alpha = 0.55f), shape = shape)
                .alpha(cardAlpha)
                .then(
                    if (enabled) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(16.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (config.status != null) {
                Text(
                    text = config.status,
                    modifier =
                        Modifier
                            .align(Alignment.Start)
                            .background(Color.Black.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF455A64),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = config.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF102A43),
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = config.subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF334E68),
                textAlign = TextAlign.Start,
            )
            if (config.seasonId == 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onClick?.invoke() },
                    enabled = enabled,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B5E20),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1B5E20).copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f),
                        ),
                ) {
                    Text("המשך", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                }
            }
        }
    }
}

@Composable
private fun SeasonsAmbientBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFFB8E0FF),
                                    Color(0xFFE8F4FF),
                                    Color(0xFFFFF4E0),
                                    Color(0xFFE8F0DC),
                                ),
                        ),
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.forest_bg_story_intro),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.14f),
            contentScale = ContentScale.Crop,
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundH = size.height * 0.34f
            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFF6D8F5A).copy(alpha = 0f),
                                Color(0xFF5C7A4A).copy(alpha = 0.22f),
                                Color(0xFF4A5F38).copy(alpha = 0.38f),
                            ),
                        startY = size.height - groundH * 1.15f,
                        endY = size.height,
                    ),
                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - groundH),
                size = androidx.compose.ui.geometry.Size(size.width, groundH),
            )
            val silhouettes = 7
            for (i in 0 until silhouettes) {
                val x = size.width * (0.08f + i * 0.14f + (i % 3) * 0.04f)
                val baseY = size.height - groundH * 0.35f
                drawCircle(
                    color = Color(0xFF3D5A3A).copy(alpha = 0.12f + (i % 2) * 0.04f),
                    radius = size.width * (0.06f + (i % 3) * 0.02f),
                    center = androidx.compose.ui.geometry.Offset(x, baseY - i * 2f),
                )
                drawCircle(
                    color = Color(0xFF3D5A3A).copy(alpha = 0.10f),
                    radius = size.width * 0.04f,
                    center = androidx.compose.ui.geometry.Offset(x + size.width * 0.03f, baseY + size.height * 0.01f),
                )
            }
        }
    }
}

