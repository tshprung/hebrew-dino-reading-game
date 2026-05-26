package com.tal.hebrewdino.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ParentalGateScreen(
    onBack: () -> Unit,
    onResetProgress: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val seed = rememberSaveable { Random.nextInt() }
    val rng = remember(seed) { Random(seed) }
    val context = LocalContext.current
    val prefs = remember(context) { CharacterPrefs(context.applicationContext) }
    val selectedCharacter by prefs.characterFlow.collectAsState(initial = DinoCharacter.DINO_GREEN)
    val scope = rememberCoroutineScope()

    var unlocked by rememberSaveable { mutableStateOf(false) }
    var a by rememberSaveable { mutableIntStateOf(0) }
    var b by rememberSaveable { mutableIntStateOf(0) }
    var op by rememberSaveable { mutableStateOf(Op.Add) }
    var answer by rememberSaveable { mutableStateOf("") }
    var showResetConfirm by rememberSaveable { mutableStateOf(false) }
    var resetStatus by rememberSaveable { mutableStateOf<ResetStatus?>(null) }

    fun regen() {
        val useMultiply = rng.nextFloat() < 0.35f
        if (useMultiply) {
            op = Op.Mul
            a = rng.nextInt(2, 10)
            b = rng.nextInt(2, 10)
        } else {
            op = Op.Add
            a = rng.nextInt(3, 10)
            b = rng.nextInt(5, 12)
        }
        answer = ""
        resetStatus = null
    }

    LaunchedEffect(Unit) { regen() }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF0B2B3D)),
    ) {
        Surface(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .topChromeInsetsPadding(),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF7F1E3),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = onBack) { Text(rtl("חזרה")) }
                    Text(
                        text = rtl("אזור הורים"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.width(72.dp))
                }

                if (!unlocked) {
                    Text(
                        text = rtl("כדי להיכנס, פתרו את השאלה:"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = rtl(questionText(a, b, op)),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF102A43),
                        textAlign = TextAlign.Center,
                    )
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { raw -> answer = raw.filter { it.isDigit() }.take(3) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        label = { Text(rtl("תשובה")) },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val expected = expected(a, b, op)
                                val got = answer.toIntOrNull()
                                if (got == expected) {
                                    unlocked = true
                                } else {
                                    regen()
                                }
                            },
                        ) {
                            Text(rtl("אישור"))
                        }
                        OutlinedButton(onClick = { regen() }) {
                            Text(rtl("שאלה אחרת"))
                        }
                    }
                } else {
                    Text(
                        text = rtl("ברוכים הבאים!"),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            val next =
                                when (selectedCharacter) {
                                    DinoCharacter.DINO_GREEN -> DinoCharacter.DINA_PINK
                                    DinoCharacter.DINA_PINK -> DinoCharacter.DINO_GREEN
                                }
                            scope.launch { prefs.setCharacter(next) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val label =
                            if (selectedCharacter == DinoCharacter.DINA_PINK) {
                                rtl("החלף לדינו")
                            } else {
                                rtl("החלף לדינה")
                            }
                        Text(label)
                    }
                    Button(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(rtl("איפוס התקדמות המשחק"))
                    }
                    if (resetStatus == ResetStatus.Done) {
                        Text(
                            text = rtl("האיפוס בוצע."),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        if (showResetConfirm) {
            Dialog(onDismissRequest = { showResetConfirm = false }) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF7F1E3),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = rtl("בטוחים? זה ימחק את כל ההתקדמות."),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF0B2B3D),
                            textAlign = TextAlign.Center,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showResetConfirm = false }) {
                                Text(rtl("ביטול"))
                            }
                            Button(
                                onClick = {
                                    showResetConfirm = false
                                    resetStatus = ResetStatus.Working
                                },
                            ) {
                                Text(rtl("איפוס"))
                            }
                        }
                    }
                }
            }
        }
    }

    if (resetStatus == ResetStatus.Working) {
        LaunchedEffect(Unit) {
            onResetProgress()
            resetStatus = ResetStatus.Done
        }
    }
}

private enum class Op { Add, Mul }

private fun questionText(a: Int, b: Int, op: Op): String =
    when (op) {
        Op.Add -> "כמה זה $a + $b?"
        Op.Mul -> "כמה זה $a × $b?"
    }

private fun expected(a: Int, b: Int, op: Op): Int =
    when (op) {
        Op.Add -> a + b
        Op.Mul -> a * b
    }

private enum class ResetStatus { Working, Done }

private fun rtl(text: String): String = "\u200F$text"
