package com.psspl.autoreply.ui.screens.ai

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.data.network.model.AiParamSpec
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.SettingsItem
import com.psspl.autoreply.ui.theme.DialogDefaults
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun AiParametersScreen(
    appId: Int,
    onBack: () -> Unit,
    viewModel: AiParametersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showModelDialog by remember { mutableStateOf(false) }
    var showTempDialog by remember { mutableStateOf(false) }
    var showMaxTokensDialog by remember { mutableStateOf(false) }
    var editingParamSpec by remember { mutableStateOf<AiParamSpec?>(null) }

    LaunchedEffect(appId) { viewModel.init(appId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────

    if (showModelDialog) {
        ModelPickerDialog(
            current = uiState.model,
            models = uiState.availableModels,
            onConfirm = { viewModel.updateModel(appId, it); showModelDialog = false },
            onDismiss = { showModelDialog = false },
        )
    }

    if (showTempDialog) {
        NumberParamDialog(
            title = "Temperature",
            description = "Controls randomness. Lower values produce more focused replies; higher values are more creative.",
            current = uiState.temperature,
            min = 0.0,
            max = 2.0,
            isInteger = false,
            onConfirm = { viewModel.updateTemperature(appId, it); showTempDialog = false },
            onDismiss = { showTempDialog = false },
        )
    }

    if (showMaxTokensDialog) {
        NumberParamDialog(
            title = "Max Tokens",
            description = "Maximum number of tokens the AI can generate in a single reply.",
            current = uiState.maxTokens.toDouble(),
            min = 256.0,
            max = 32768.0,
            isInteger = true,
            onConfirm = {
                viewModel.updateMaxTokens(appId, it.toInt()); showMaxTokensDialog = false
            },
            onDismiss = { showMaxTokensDialog = false },
        )
    }

    editingParamSpec?.let { spec ->
        NumberParamDialog(
            title = spec.label,
            description = "Allowed range: ${formatValue(spec.min ?: 0.0, false)} – ${
                formatValue(
                    spec.max ?: 1.0,
                    false
                )
            }",
            current = uiState.extraParams[spec.key] ?: (spec.default as? Number)?.toDouble() ?: 0.0,
            min = spec.min ?: 0.0,
            max = spec.max ?: 1.0,
            isInteger = false,
            onConfirm = {
                viewModel.updateExtraParam(appId, spec.key, it); editingParamSpec = null
            },
            onDismiss = { editingParamSpec = null },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "AI Parameters",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.sm),
                            strokeWidth = 2.dp,
                        )
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
            ) { CircularProgressIndicator() }
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

            if (!uiState.hasConfig) {
                AppCard {
                    Text(
                        text = "No AI configuration found. Set up AI Reply first to adjust parameters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            // ── Generation ────────────────────────────────────────────────────
            SectionLabel("Generation")

            AppCard {
                SettingsItem(
                    icon = Icons.Filled.Memory,
                    title = "Model",
                    subtitle = uiState.model,
                    onClick = { showModelDialog = true },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
                SettingsItem(
                    icon = Icons.Filled.Thermostat,
                    title = "Temperature",
                    subtitle = "%.2f  ·  0.0 – 2.0".format(uiState.temperature),
                    onClick = { showTempDialog = true },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
                SettingsItem(
                    icon = Icons.Filled.Numbers,
                    title = "Max Tokens",
                    subtitle = "%,d tokens".format(uiState.maxTokens),
                    onClick = { showMaxTokensDialog = true },
                )
            }

            // ── Provider-specific extra params ────────────────────────────────
            if (uiState.paramSpecs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                SectionLabel("${uiState.providerLabel} Parameters")

                AppCard {
                    uiState.paramSpecs.forEachIndexed { index, spec ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
                        }
                        val current = uiState.extraParams[spec.key]
                            ?: (spec.default as? Number)?.toDouble()
                        SettingsItem(
                            icon = Icons.Filled.Tune,
                            title = spec.label,
                            subtitle = if (current != null) {
                                "${formatValue(current, false)}  ·  ${
                                    formatValue(
                                        spec.min ?: 0.0,
                                        false
                                    )
                                } – ${formatValue(spec.max ?: 1.0, false)}"
                            } else {
                                "Not set  ·  ${formatValue(spec.min ?: 0.0, false)} – ${
                                    formatValue(
                                        spec.max ?: 1.0,
                                        false
                                    )
                                }"
                            },
                            onClick = { editingParamSpec = spec },
                        )
                    }
                }
            }
        }
    }
}

private fun formatValue(value: Double, isInteger: Boolean): String =
    if (isInteger || value == kotlin.math.floor(value)) value.toInt().toString()
    else "%.2f".format(value)

// ── Reused section label from AiSettingsScreen ────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = Spacing.xs),
    )
}

// ── Model picker ──────────────────────────────────────────────────────────────
@Composable
private fun ModelPickerDialog(
    current: String,
    models: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        tonalElevation = DialogDefaults.tonalElevation,
        title = {
            Text(
                text = "Select Model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DialogDefaults.titleColor,
            )
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = model == selected,
                                onClick = { selected = model },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        RadioButton(selected = model == selected, onClick = null)
                        Text(
                            text = model,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) { Text("Select") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

// ── Number param dialog (shared for temperature, maxTokens, extraParams) ──────
@Composable
private fun NumberParamDialog(
    title: String,
    description: String,
    current: Double,
    min: Double,
    max: Double,
    isInteger: Boolean,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputText by remember { mutableStateOf(formatValue(current, isInteger)) }
    var isError by remember { mutableStateOf(false) }
    var parsed by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        tonalElevation = DialogDefaults.tonalElevation,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DialogDefaults.titleColor,
            )
        },
        text = {
            Column {
                Text(
                    text = description,
                    style = DialogDefaults.bodyStyle,
                    color = DialogDefaults.bodyColor,
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { input ->
                        inputText = input
                        val v = input.toDoubleOrNull()
                        isError = v == null || v < min || v > max
                        if (!isError && v != null) parsed = v
                    },
                    label = {
                        Text(
                            if (isInteger) "${min.toInt()} – ${max.toInt()}"
                            else "${"%.2f".format(min)} – ${"%.2f".format(max)}"
                        )
                    },
                    isError = isError,
                    supportingText = if (isError) {
                        {
                            Text(
                                "Enter a value between ${
                                    formatValue(
                                        min,
                                        isInteger
                                    )
                                } and ${formatValue(max, isInteger)}"
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isError) onConfirm(parsed) },
                enabled = !isError,
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
