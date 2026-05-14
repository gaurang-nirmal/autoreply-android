package com.psspl.autoreply.ui.screens.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.SettingsItem
import com.psspl.autoreply.ui.theme.DialogDefaults
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun AiSettingsScreen(
    appId: Int,
    onBack: () -> Unit,
    viewModel: AiSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showHistoryCountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(appId) { viewModel.init(appId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.clearSuccess) {
        uiState.clearSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    if (showClearConfirm) {
        ConfirmationDialog(
            title = "Clear AI History",
            message = "All AI conversation history for this app will be permanently deleted. This cannot be undone.",
            confirmLabel = "Clear",
            isDestructive = true,
            onConfirm = { viewModel.clearHistory(appId) },
            onDismiss = { showClearConfirm = false },
        )
    }

    if (showHistoryCountDialog) {
        HistoryCountDialog(
            current = uiState.historyTurns,
            onConfirm = { turns ->
                viewModel.updateHistoryTurns(appId, turns)
                showHistoryCountDialog = false
            },
            onDismiss = { showHistoryCountDialog = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "AI Settings",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {

            // ── Conversation History ──────────────────────────────────────────
            SectionLabel("Conversation History")

            AppCard {
                // Use History toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Use History",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Include previous messages as context for AI replies",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.useHistory,
                        onCheckedChange = { viewModel.toggleUseHistory(appId) },
                        enabled = uiState.hasConfig && !uiState.isSaving,
                    )
                }

                // History count — only shown when history is on
                if (uiState.useHistory) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
                    SettingsItem(
                        icon = Icons.Filled.History,
                        title = "History Count",
                        subtitle = "Last ${uiState.historyTurns} message${if (uiState.historyTurns == 1) "" else "s"} used as context",
                        onClick = { showHistoryCountDialog = true },
                    )
                }
            }

            if (!uiState.useHistory) {
                Text(
                    text = "AI will reply without any context from previous messages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // ── Data Management ───────────────────────────────────────────────
            SectionLabel("Data Management")

            AppCard {
                SettingsItem(
                    icon = Icons.Filled.DeleteSweep,
                    title = "Clear AI History",
                    subtitle = "Delete all AI conversation history for this app",
                    onClick = { showClearConfirm = true },
                )
            }

            // Hint when no config exists
            if (!uiState.hasConfig && !uiState.isLoading) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HistoryToggleOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "No AI configuration found. Set up AI Reply first to manage history settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = Spacing.xs),
    )
}

@Composable
private fun HistoryCountDialog(
    current: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputText by remember { mutableStateOf(current.toString()) }
    var turns by remember { mutableIntStateOf(current) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        tonalElevation = DialogDefaults.tonalElevation,
        title = {
            Text(
                text = "History Count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DialogDefaults.titleColor,
            )
        },
        text = {
            Column {
                Text(
                    text = "Number of recent messages sent to the AI as conversation context.",
                    style = DialogDefaults.bodyStyle,
                    color = DialogDefaults.bodyColor,
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { input ->
                        inputText = input
                        val parsed = input.toIntOrNull()
                        isError = parsed == null || parsed < 1 || parsed > 50
                        if (!isError && parsed != null) turns = parsed
                    },
                    label = { Text("Messages (1–50)") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a number between 1 and 50") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isError) onConfirm(turns) },
                enabled = !isError,
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
