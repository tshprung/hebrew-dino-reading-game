package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun SettingsScreen(
    onPick: (DinoCharacter) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "הגדרות",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "בחירת דמות", style = MaterialTheme.typography.titleLarge, color = Color(0xFF0B2B3D))
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            CharacterCard(title = "דינו", subtitle = "בן", imageRes = R.drawable.dino_boy) {
                onPick(DinoCharacter.Dino)
            }
            Spacer(modifier = Modifier.width(16.dp))
            CharacterCard(title = "דינה", subtitle = "בת", imageRes = R.drawable.dino_girl) {
                onPick(DinoCharacter.Dina)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        OutlinedButton(onClick = onBack) { Text("חזרה") }
    }
}

@Composable
private fun CharacterCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .width(140.dp)
                    .height(140.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onClick) { Text("בחר") }
        }
    }
}

