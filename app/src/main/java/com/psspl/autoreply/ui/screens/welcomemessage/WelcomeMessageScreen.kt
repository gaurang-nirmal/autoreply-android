package com.psspl.autoreply.ui.screens.welcomemessage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val TealAccent = Color(0xFF128C7E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeMessageScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToEditMessage: () -> Unit = {},
    viewModel: WelcomeMessageViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    var showChangeDaysDialog by remember { mutableStateOf(false) }

    if (showChangeDaysDialog) {
        ChangeDaysDialog(
            currentDays = config.cooldownDays,
            onConfirm = { days ->
                viewModel.setCooldownDays(days)
                showChangeDaysDialog = false
            },
            onDismiss = { showChangeDaysDialog = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Welcome Message",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Feature icon header ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xxl),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(88.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEmotions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                    )
                }
            }

            // ── Toggle section ────────────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Send welcome message",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Send a welcome message when someone messages you " +
                                        "the first time or Every ${config.cooldownDays} days once.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = config.isEnabled,
                            onCheckedChange = viewModel::setEnabled,
                            modifier = Modifier.padding(start = Spacing.sm),
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    TextButton(
                        onClick = { showChangeDaysDialog = true },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = "CHANGE DAYS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TealAccent,
                        )
                    }
                }
            }

            HorizontalDivider()

            // ── Welcome message display / edit ─────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome Message",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = config.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onNavigateToEditMessage) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ─── Change Days Dialog ───────────────────────────────────────────────────────

@Composable
private fun ChangeDaysDialog(
    currentDays: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputText by remember { mutableStateOf(currentDays.toString()) }
    var days by remember { mutableIntStateOf(currentDays) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Cooldown Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column {
                Text(
                    text = "Set the number of days before the welcome message is sent again to the same contact.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { input ->
                        inputText = input
                        val parsed = input.toIntOrNull()
                        isError = parsed == null || parsed < 1 || parsed > 365
                        if (!isError && parsed != null) days = parsed
                    },
                    label = { Text("Days (1–365)") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a number between 1 and 365") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isError) onConfirm(days) },
                enabled = !isError,
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun WelcomeMessageScreenPreview() {
    AutoReplyTheme {
        WelcomeMessageScreen()
    }
}
