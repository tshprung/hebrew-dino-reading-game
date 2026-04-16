package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun CharacterSelectScreen(
    onPick: (DinoCharacter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_beach),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Text(
            text = "בוחרים דמות",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "מי יצא לחפש את הביצה?",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            CharacterCard(
                title = "דינו",
                subtitle = "בן",
                imageRes = R.drawable.dino_boy,
                showRibbon = false,
                onClick = { onPick(DinoCharacter.Dino) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            CharacterCard(
                title = "דינה",
                subtitle = "בת",
                imageRes = R.drawable.dino_girl,
                showRibbon = true,
                onClick = { onPick(DinoCharacter.Dina) },
            )
        }
        }
    }
}

@Composable
private fun CharacterCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    showRibbon: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(18.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                if (showRibbon) {
                    PinkRibbon(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 8.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 44.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subtitle, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun PinkRibbon(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFFF66B2), shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Spacer(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFFF66B2), shape = CircleShape),
            )
        }
        Spacer(
            modifier = Modifier
                .align(Alignment.Center)
                .size(8.dp)
                .background(Color(0xFFFF2D95), shape = CircleShape),
        )
    }
}

