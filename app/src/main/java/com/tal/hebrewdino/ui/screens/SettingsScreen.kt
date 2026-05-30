package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.semantics.Role
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.RoundedCornerShape

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
        ChapterResetRow(6, "פרק 6 — חוזרים הביתה"),
    )

internal fun chapterResetRowIdsForTest(): List<Int> = chapterResetRows.map { it.id }

private const val TestTagBackgroundMusicToggle: String = "settings_bg_music_toggle"

@Composable
fun SettingsScreen(
    backgroundMusicEnabled: Boolean,
    onBackgroundMusicEnabledChange: (Boolean) -> Unit,
    companionCharacter: DinoCharacter,
    playerAddress: PlayerAddress,
    onCompanionCharacterChange: (DinoCharacter) -> Unit,
    onPlayerAddressChange: (PlayerAddress) -> Unit,
    onResetAll: () -> Unit,
    onResetChapters: (Set<Int>) -> Unit,
    onResetSeason2: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showResetChaptersDialog by remember { mutableStateOf(false) }
    var showResetSeason2Dialog by remember { mutableStateOf(false) }
    var selectedChapters by remember { mutableStateOf(setOf<Int>()) }
    var progressExpanded by remember { mutableStateOf(false) }
    var managementExpanded by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "אזור הורים",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF0B2B3D),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            Text(
                text = "כאן אפשר לשנות את בחירות המשחק, הקול וההתקדמות.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0B2B3D).copy(alpha = 0.76f),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSectionCard(
                title = "בחירות משחק",
                helperText = "בחירות אלו משפיעות על הדמות ועל אופן הפנייה לאורך המשחק.",
            ) {
                Text(
                    text = "חבר למסע",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0B2B3D).copy(alpha = 0.92f),
                )
                SegmentedChoiceRow(
                    options = listOf("דינו" to DinoCharacter.Dino, "דינה" to DinoCharacter.Dina),
                    selected = companionCharacter,
                    onSelect = onCompanionCharacterChange,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "אופן פנייה",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0B2B3D).copy(alpha = 0.92f),
                )
                SegmentedChoiceRow(
                    options = listOf("שחקן" to PlayerAddress.Boy, "שחקנית" to PlayerAddress.Girl),
                    selected = playerAddress,
                    onSelect = onPlayerAddressChange,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSectionCard(
                title = "קול ומוזיקה",
                helperText = "מוזיקת רקע מנוגנת במסכי כניסה/בחירה בלבד.",
            ) {
                SettingsToggleRow(
                    title = "מוזיקת רקע",
                    checked = backgroundMusicEnabled,
                    onCheckedChange = onBackgroundMusicEnabledChange,
                    testTag = TestTagBackgroundMusicToggle,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsExpandableSectionCard(
                title = "התקדמות",
                helperTextCollapsed = "איפוס פרקים נבחרים בלבד.",
                helperTextExpanded = "סמנו פרק אחד או יותר, ואז יאופסו רק ההתקדמות שלהם.",
                expanded = progressExpanded,
                onExpandedChange = { progressExpanded = it },
            ) {
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsExpandableSectionCard(
                title = "איפוס וניהול",
                helperTextCollapsed = "אפשרויות איפוס וניהול ההתקדמות.",
                helperTextExpanded = "פעולות באזור זה מוחקות נתונים. מומלץ לבצע רק בהשגחת הורה.",
                expanded = managementExpanded,
                onExpandedChange = { managementExpanded = it },
                accentColor = Color(0xFF7A1E1E),
            ) {
                OutlinedButton(
                    onClick = { showResetAllDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("איפוס משחק מלא")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "איפוס עונה 2 מאפס רק את ההתקדמות של עונה 2.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0B2B3D).copy(alpha = 0.84f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetSeason2Dialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("איפוס עונה 2")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("חזרה")
            }
        }
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text(text = "איפוס משחק מלא") },
            text = {
                Text(
                    text =
                        "פעולה זו תמחק את ההתקדמות ותדרוש לבחור שוב דמות ואופן פנייה. להמשיך?",
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
                    Text("כן, לאפס")
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

    if (showResetSeason2Dialog) {
        AlertDialog(
            onDismissRequest = { showResetSeason2Dialog = false },
            title = { Text(text = "איפוס עונה 2") },
            text = {
                Text(
                    text = "לאפס את ההתקדמות של עונה 2 בלבד?\n\nפעולה זו לא משפיעה על עונה 1.",
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetSeason2Dialog = false
                        onResetSeason2()
                    },
                ) {
                    Text("כן, לאפס עונה 2")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSeason2Dialog = false }) {
                    Text("ביטול")
                }
            },
        )
    }
}

@Composable
private fun <T> SegmentedChoiceRow(
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(18.dp)),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEach { (label, value) ->
            val isSelected = value == selected
            val containerColor =
                if (isSelected) {
                    Color(0xFF2AA6C9).copy(alpha = 0.20f)
                } else {
                    Color.White.copy(alpha = 0.90f)
                }
            val border =
                if (isSelected) {
                    BorderStroke(1.dp, Color(0xFF2AA6C9).copy(alpha = 0.55f))
                } else {
                    BorderStroke(1.dp, Color(0xFF0B2B3D).copy(alpha = 0.14f))
                }
            OutlinedButton(
                onClick = { onSelect(value) },
                modifier =
                    Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(value) },
                            role = Role.RadioButton,
                        ),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = containerColor,
                        contentColor = Color(0xFF0B2B3D),
                    ),
                border = border,
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    helperText: String? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF0B2B3D),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = titleColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            if (!helperText.isNullOrBlank()) {
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0B2B3D).copy(alpha = 0.78f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                    textAlign = TextAlign.Start,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            content()
        }
    }
}

@Composable
private fun SettingsExpandableSectionCard(
    title: String,
    helperTextCollapsed: String,
    helperTextExpanded: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF0B2B3D),
    content: @Composable () -> Unit,
) {
    SettingsSectionCard(
        title = title,
        helperText = if (expanded) helperTextExpanded else helperTextCollapsed,
        modifier = modifier,
        titleColor = accentColor,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = if (expanded) 10.dp else 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onExpandedChange(!expanded) }) {
                Text(if (expanded) "סגור" else "פתח")
            }
        }
        if (expanded) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0B2B3D),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
        )
    }
}
