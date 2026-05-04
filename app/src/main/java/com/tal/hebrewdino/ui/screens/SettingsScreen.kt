package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class ChapterResetRow(
    val id: Int,
    val label: String,
)

private val chapterResetRows =
    listOf(
        ChapterResetRow(1, "פרק 1 — מצא את הביצה"),
        ChapterResetRow(2, "פרק 2 — עקבות לביצה הורודה"),
        ChapterResetRow(3, "פרק 3 — הביצה הורודה"),
        ChapterResetRow(4, "פרק 4 — סיבוך בדרך"),
        ChapterResetRow(5, "פרק 5 — הביצה השלישית"),
    )

@Composable
fun SettingsScreen(
    onResetAll: () -> Unit,
    onResetChapters: (Set<Int>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showResetChaptersDialog by remember { mutableStateOf(false) }
    var selectedChapters by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "הגדרות",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFF0B2B3D),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "איפוס פרקים",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0B2B3D),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "סמנו פרק אחד או יותר, ואז יאופסו רק ההתקדמות שלהם (תחנות, אינטרו, ביצים של הפרק).",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF0B2B3D).copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
        )

        chapterResetRows.forEach { row ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Checkbox(
                    checked = row.id in selectedChapters,
                    onCheckedChange = { checked ->
                        selectedChapters =
                            if (checked) {
                                selectedChapters + row.id
                            } else {
                                selectedChapters - row.id
                            }
                    },
                )
                Text(
                    text = row.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF0B2B3D),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = { selectedChapters = chapterResetRows.map { it.id }.toSet() },
            ) {
                Text("בחר הכל")
            }
            TextButton(onClick = { selectedChapters = emptySet() }) {
                Text("נקה בחירה")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { if (selectedChapters.isNotEmpty()) showResetChaptersDialog = true },
            enabled = selectedChapters.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("אפס פרקים נבחרים")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "איפוס מלא",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0B2B3D),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "מאפס את כל הפרקים ומחזיר למצב התחלה.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF0B2B3D).copy(alpha = 0.88f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
        )

        OutlinedButton(
            onClick = { showResetAllDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("איפוס משחק מלא")
        }

        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("חזרה")
        }
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text(text = "איפוס משחק מלא") },
            text = {
                Text(
                    text = "זה יאפס את ההתקדמות בכל הפרקים ויחזיר לשלב הראשון. להמשיך?",
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetAllDialog = false
                        selectedChapters = emptySet()
                        onResetAll()
                    },
                ) {
                    Text("כן, לאפס הכל")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("ביטול")
                }
            },
        )
    }

    if (showResetChaptersDialog) {
        val sorted = selectedChapters.sorted()
        val names = sorted.map { id -> chapterResetRows.first { it.id == id }.label }
        AlertDialog(
            onDismissRequest = { showResetChaptersDialog = false },
            title = { Text(text = "איפוס פרקים נבחרים") },
            text = {
                Text(
                    text =
                        "לאפס את ההתקדמות בפרקים הבאים?\n\n" +
                            names.joinToString("\n") +
                            "\n\nפעולה זו לא ניתנת לביטול.",
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetChaptersDialog = false
                        onResetChapters(selectedChapters.toSet())
                        selectedChapters = emptySet()
                    },
                ) {
                    Text("כן, לאפס")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetChaptersDialog = false }) {
                    Text("ביטול")
                }
            },
        )
    }
}
