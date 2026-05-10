package com.psspl.autoreply.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.DialogDefaults

@Composable
fun AppAlertDialog(
    title: String,
    message: String,
    confirmLabel: String = "OK",
    icon: ImageVector? = null,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        tonalElevation = DialogDefaults.tonalElevation,
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else null,
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
            TextButton(onClick = onDismiss) {
                Text(
                    text = confirmLabel,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Preview
@Composable
private fun AppAlertDialogPreview() {
    AutoReplyTheme {
        AppAlertDialog(
            title = "Notification Access Required",
            message = "Please grant notification access to enable auto-replies.",
            icon = Icons.Filled.Info,
            onDismiss = {},
        )
    }
}
