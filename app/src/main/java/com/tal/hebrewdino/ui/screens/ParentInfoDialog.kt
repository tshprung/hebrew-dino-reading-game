package com.tal.hebrewdino.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tal.hebrewdino.ui.domain.ParentInfoCopy

@Composable
fun ParentInfoDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "\u200F${ParentInfoCopy.DialogTitle}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Text(
                text = ParentInfoCopy.dialogBodyLines().joinToString("\n\n"),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "\u200F${ParentInfoCopy.ContinueLabel}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        },
    )
}
