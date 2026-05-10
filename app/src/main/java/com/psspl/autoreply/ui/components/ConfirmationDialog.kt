package com.psspl.autoreply.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.DialogDefaults

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val confirmColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        tonalElevation = DialogDefaults.tonalElevation,
        title = {
            Text(
                text = title,
                style = DialogDefaults.titleStyle,
                color = DialogDefaults.titleColor,
            )
        },
        text = {
            Text(
                text = message,
                style = DialogDefaults.bodyStyle,
                color = DialogDefaults.bodyColor,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Preview
@Composable
private fun ConfirmationDialogDestructivePreview() {
    AutoReplyTheme {
        ConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            confirmLabel = "Sign Out",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun ConfirmationDialogNeutralPreview() {
    AutoReplyTheme {
        ConfirmationDialog(
            title = "Clear History",
            message = "This will remove all message history. Continue?",
            confirmLabel = "Clear",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
