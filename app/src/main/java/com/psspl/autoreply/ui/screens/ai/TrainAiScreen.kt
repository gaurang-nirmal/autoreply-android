package com.psspl.autoreply.ui.screens.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.data.network.model.TrainingPromptItem
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun TrainAiScreen(
    appId: Int,
    onBack: () -> Unit,
    onNavigateToAddPrompt: () -> Unit,
    onNavigateToEditPrompt: (String) -> Unit,
    viewModel: TrainAiViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appId) { viewModel.load(appId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────

    pendingDeleteId?.let { id ->
        ConfirmationDialog(
            title = "Delete Prompt",
            message = "This training prompt will be permanently deleted.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.deletePrompt(appId, id); pendingDeleteId = null },
            onDismiss = { pendingDeleteId = null },
        )
    }

    if (showDeleteAllConfirm) {
        ConfirmationDialog(
            title = "Clear All Prompts",
            message = "All ${uiState.prompts.size} training prompts will be permanently deleted.",
            confirmLabel = "Clear All",
            isDestructive = true,
            onConfirm = { viewModel.deleteAll(appId); showDeleteAllConfirm = false },
            onDismiss = { showDeleteAllConfirm = false },
        )
    }

    if (showRestoreConfirm) {
        ConfirmationDialog(
            title = "Restore Prompts",
            message = "Current prompts will be replaced with the last backup (${uiState.backupData?.size ?: 0} prompt${if ((uiState.backupData?.size ?: 0) == 1) "" else "s"}).",
            confirmLabel = "Restore",
            onConfirm = { viewModel.restore(appId); showRestoreConfirm = false },
            onDismiss = { showRestoreConfirm = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Train AI",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isActionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.sm),
                            strokeWidth = 2.dp,
                        )
                    }
                    TopbarMenu(
                        items = buildList {
                            add(
                                TopbarMenuItem(
                                    label = "Backup",
                                    icon = Icons.Filled.Save,
                                    onClick = { viewModel.backup(appId) },
                                ),
                            )
                            if (uiState.backupData != null) {
                                add(
                                    TopbarMenuItem(
                                        label = "Restore (${uiState.backupData!!.size})",
                                        icon = Icons.Filled.Restore,
                                        isDividerAfter = true,
                                        onClick = { showRestoreConfirm = true },
                                    ),
                                )
                            }
                            if (uiState.prompts.isNotEmpty()) {
                                add(
                                    TopbarMenuItem(
                                        label = "Clear All",
                                        icon = Icons.Filled.DeleteSweep,
                                        onClick = { showDeleteAllConfirm = true },
                                    ),
                                )
                            }
                        },
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPrompt,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add prompt")
            }
        },
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.prompts.isEmpty()) {
            EmptyState(
                title = "No training prompts yet",
                description = "Tap + to add instructions that shape the AI's reply style",
                icon = Icons.Filled.Psychology,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.md,
                end = Spacing.md,
                top = Spacing.md,
                bottom = Spacing.md + 80.dp, // clear FAB
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                Text(
                    text = "${uiState.prompts.size} prompt${if (uiState.prompts.size == 1) "" else "s"} — applied in order shown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                )
            }
            items(items = uiState.prompts, key = { it.id }) { prompt ->
                PromptCard(
                    prompt = prompt,
                    onToggle = { viewModel.toggleEnabled(appId, prompt.id, it) },
                    onEdit = { onNavigateToEditPrompt(prompt.id) },
                    onDelete = { pendingDeleteId = prompt.id },
                )
            }
        }
    }
}

@Composable
private fun PromptCard(
    prompt: TrainingPromptItem,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            // Content preview
            Text(
                text = prompt.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (prompt.isEnabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            HorizontalDivider()

            // Footer row: enable toggle + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Switch(
                        checked = prompt.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.size(36.dp, 20.dp),
                    )
                    Text(
                        text = if (prompt.isEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (prompt.isEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit prompt",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete prompt",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
