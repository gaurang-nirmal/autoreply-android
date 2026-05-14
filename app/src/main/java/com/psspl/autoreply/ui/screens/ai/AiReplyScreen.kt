package com.psspl.autoreply.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.SettingsItem
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun AiReplyScreen(
    appId: Int,
    onBack: () -> Unit,
    onNavigateToTrainAi: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToAiParameters: () -> Unit,
    viewModel: AiReplyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(appId) { viewModel.init(appId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Configuration saved")
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "AI Reply",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {

            // ── Provider chips ────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                uiState.providers.forEach { provider ->
                    val selected = provider.id == uiState.selectedProviderId
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectProvider(provider.id) },
                        label = { Text(providerChipLabel(provider.id)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            // ── Provider info card ────────────────────────────────────────────
            uiState.selectedProvider?.let { provider ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(providerColor(provider.id)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = providerInitial(provider.id),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = providerDisplayName(provider.id),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Connect an AI assistant to send intelligent auto-replies.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = providerPoweredBy(provider.id),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // ── API Key field ─────────────────────────────────────────────────
            AppCard {
                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::onApiKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (uiState.hasExistingConfig) "API Key (saved — leave blank to keep)" else "API Key")
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::toggleApiKeyVisibility) {
                            Icon(
                                imageVector = if (uiState.isApiKeyVisible)
                                    Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle visibility",
                            )
                        }
                    },
                    visualTransformation = if (uiState.isApiKeyVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "GET API KEY",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { /* open provider's API key URL */ }
                        .padding(vertical = Spacing.xs),
                )
            }

            // ── Model dropdown ────────────────────────────────────────────────
            AppCard {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Model") },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleModelDropdown(!uiState.isModelDropdownExpanded) }) {
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "Select model"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { viewModel.toggleModelDropdown(true) },
                    )
                    DropdownMenu(
                        expanded = uiState.isModelDropdownExpanded,
                        onDismissRequest = { viewModel.toggleModelDropdown(false) },
                        modifier = Modifier.fillMaxWidth(0.9f),
                    ) {
                        uiState.selectedProvider?.availableModels?.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = { viewModel.onModelSelected(model) },
                            )
                        }
                    }
                }
            }

            // ── Save / Reset ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { viewModel.save(appId) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = !uiState.isSaving,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("SAVE", style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(
                    onClick = { viewModel.reset(appId) },
                    enabled = uiState.hasExistingConfig && !uiState.isSaving,
                ) {
                    Text(
                        text = "RESET",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            // ── Navigation items ──────────────────────────────────────────────
            SectionLabel("Train AI")
            AppCard {
                SettingsItem(
                    icon = Icons.Filled.Psychology,
                    title = "Train AI",
                    subtitle = "Add custom prompts to shape the AI's reply style",
                    onClick = onNavigateToTrainAi,
                )
            }

            SectionLabel("Settings")
            AppCard {
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "AI Settings",
                    subtitle = "Manage conversation history and reply limits",
                    onClick = onNavigateToAiSettings,
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                SettingsItem(
                    icon = Icons.Filled.Tune,
                    title = "AI Parameters",
                    subtitle = "Temperature, max tokens and provider-specific options",
                    onClick = onNavigateToAiParameters,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
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

// ── Provider display helpers ──────────────────────────────────────────────────

private fun providerChipLabel(id: String) = when (id) {
    "openai" -> "ChatGPT"
    "gemini" -> "Gemini"
    "deepseek" -> "DeepSeek"
    else -> id.replaceFirstChar { it.uppercase() }
}

private fun providerDisplayName(id: String) = when (id) {
    "openai" -> "ChatGPT"
    "gemini" -> "Gemini"
    "deepseek" -> "DeepSeek"
    else -> id.replaceFirstChar { it.uppercase() }
}

private fun providerPoweredBy(id: String) = when (id) {
    "openai" -> "Powered by OpenAI"
    "gemini" -> "Powered by Google"
    "deepseek" -> "Powered by DeepSeek"
    else -> "Powered by $id"
}

private fun providerInitial(id: String) = when (id) {
    "deepseek" -> "D"
    else -> id.first().uppercaseChar().toString()
}

private fun providerColor(id: String) = when (id) {
    "openai" -> Color(0xFF10A37F)
    "gemini" -> Color(0xFF4285F4)
    "deepseek" -> Color(0xFF1A56DB)
    else -> Color(0xFF6B7280)
}
