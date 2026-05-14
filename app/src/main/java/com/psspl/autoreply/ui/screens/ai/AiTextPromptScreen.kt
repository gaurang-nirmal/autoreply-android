package com.psspl.autoreply.ui.screens.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun AiTextPromptScreen(
    appId: Int,
    promptId: String?,
    onBack: () -> Unit,
    viewModel: TrainAiViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = promptId != null
    val existingPrompt = remember(promptId, uiState.prompts) {
        if (promptId != null) uiState.prompts.firstOrNull { it.id == promptId } else null
    }

    var content by rememberSaveable(promptId) { mutableStateOf("") }
    var isEnabled by rememberSaveable(promptId) { mutableStateOf(true) }
    var initialized by remember { mutableStateOf(!isEditMode) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Populate fields once the prompt list arrives (edit mode)
    LaunchedEffect(existingPrompt) {
        if (!initialized && existingPrompt != null) {
            content = existingPrompt.content
            isEnabled = existingPrompt.isEnabled
            initialized = true
        }
    }

    // Navigate back after a successful save
    LaunchedEffect(uiState.promptSaved) {
        if (uiState.promptSaved) {
            viewModel.clearPromptSaved()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    val isDirty = when {
        isEditMode -> content != (existingPrompt?.content
            ?: "") || isEnabled != (existingPrompt?.isEnabled ?: true)

        else -> content.isNotBlank()
    }

    BackHandler(enabled = isDirty && !uiState.promptSaved) { showDiscardDialog = true }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = "Discard Changes",
            message = "Your unsaved changes will be lost.",
            confirmLabel = "Discard",
            isDestructive = true,
            onConfirm = onBack,
            onDismiss = { showDiscardDialog = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = if (isEditMode) "Edit Prompt" else "Add Prompt",
                navigationIcon = {
                    IconButton(onClick = { if (isDirty) showDiscardDialog = true else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (content.isBlank()) return@FloatingActionButton
                    if (isEditMode && promptId != null) {
                        viewModel.updatePromptContent(appId, promptId, content, isEnabled)
                    } else {
                        viewModel.createPrompt(appId, content, isEnabled)
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                if (uiState.isActionInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(Spacing.xs),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5f.dp,
                    )
                } else {
                    Icon(Icons.Filled.Check, contentDescription = "Save prompt")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = "Write a clear instruction to guide the AI's tone, style, or behaviour.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.xs),
            )

            // ── Prompt text area ──────────────────────────────────────────────
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Prompt") },
                placeholder = {
                    Text(
                        "e.g. Always reply in a friendly, professional tone. Keep responses concise.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                minLines = 6,
                maxLines = 20,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                supportingText = {
                    Text(
                        text = "${content.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            // ── Active toggle ─────────────────────────────────────────────────
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Include this prompt in AI reply generation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                    )
                }
            }
        }
    }
}

// Workaround: `2.5f.dp` doesn't parse — use Dp extension directly
private val Float.dp get() = androidx.compose.ui.unit.Dp(this)
