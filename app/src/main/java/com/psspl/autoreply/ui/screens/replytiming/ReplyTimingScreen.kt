package com.psspl.autoreply.ui.screens.replytiming

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun String.toScreenTitle(): String = when (this) {
    "keyword" -> "Keyword Reply Time"
    "menu" -> "Menu Reply Time"
    "spreadsheet" -> "Spreadsheet Reply Time"
    else -> "${replaceFirstChar { it.uppercase() }} Reply Time"
}

private fun String.toSourceLabel(): String = when (this) {
    "keyword" -> "Keyword Reply"
    "menu" -> "Menu Reply"
    "spreadsheet" -> "Spreadsheet Reply"
    else -> "${replaceFirstChar { it.uppercase() }} Reply"
}

/** "65" → "1 min 5 sec", "45" → "45 sec", "120" → "2 min" */
private fun formatSeconds(seconds: Int): String {
    if (seconds < 60) return "$seconds sec"
    val m = seconds / 60
    val s = seconds % 60
    return if (s == 0) "$m min" else "$m min $s sec"
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ReplyTimingScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToLimitList: () -> Unit = {},
    viewModel: ReplyTimingViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val selectedMode =
        runCatching { ReplyMode.valueOf(config.replyMode) }.getOrDefault(ReplyMode.EVERY_TIME)

    var showWaitDialog by remember { mutableStateOf(false) }
    var showDelayDialog by remember { mutableStateOf(false) }
    var showMaxRepliesDialog by remember { mutableStateOf(false) }
    var infoMode by remember { mutableStateOf<ReplyMode?>(null) }

    if (showWaitDialog) {
        SecondsInputDialog(
            title = "Wait duration",
            currentSeconds = config.waitSeconds,
            onConfirm = { viewModel.setWaitSeconds(it); showWaitDialog = false },
            onDismiss = { showWaitDialog = false },
        )
    }
    if (showDelayDialog) {
        SecondsInputDialog(
            title = "Delay duration",
            currentSeconds = config.delaySeconds,
            onConfirm = { viewModel.setDelaySeconds(it); showDelayDialog = false },
            onDismiss = { showDelayDialog = false },
        )
    }
    if (showMaxRepliesDialog) {
        MaxRepliesDialog(
            current = config.maxReplies,
            onConfirm = { viewModel.setMaxReplies(it); showMaxRepliesDialog = false },
            onDismiss = { showMaxRepliesDialog = false },
        )
    }
    infoMode?.let { mode ->
        ReplyModeInfoDialog(mode = mode, onDismiss = { infoMode = null })
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = viewModel.replyType.toScreenTitle(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {

            // ── Section 1: Reply message source ───────────────────────────────
            item { SectionHeader("Reply message") }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = viewModel.replyType.toSourceLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }

            item { Spacer(Modifier.height(Spacing.md)) }

            // ── Section 2: How often? ─────────────────────────────────────────
            item {
                SectionHeader(
                    "How often should the ${viewModel.replyType.toSourceLabel().lowercase()} " +
                            "be sent per chat?"
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    ReplyMode.entries.forEachIndexed { index, mode ->
                        ReplyModeOption(
                            mode = mode,
                            selected = selectedMode == mode,
                            onSelect = { viewModel.setReplyMode(mode) },
                            onInfoClick = { infoMode = mode },
                        )

                        // Duration sub-row for configurable modes
                        if (selectedMode == mode) {
                            when (mode) {
                                ReplyMode.REPLY_AND_WAIT -> DurationSubRow(
                                    label = "Wait for",
                                    value = formatSeconds(config.waitSeconds),
                                    onClick = { showWaitDialog = true },
                                )

                                ReplyMode.REPLY_AFTER_DELAY -> DurationSubRow(
                                    label = "Delay",
                                    value = formatSeconds(config.delaySeconds),
                                    onClick = { showDelayDialog = true },
                                )

                                else -> Unit
                            }
                        }

                        if (index < ReplyMode.entries.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(Spacing.md)) }

            // ── Section 3: Reply limit ─────────────────────────────────────────
            item { SectionHeader("Reply limit") }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    // Toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reply limit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Send a limited number of replies per chat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = config.replyLimitEnabled,
                            onCheckedChange = viewModel::setReplyLimitEnabled,
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))

                    // Max Replies row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMaxRepliesDialog = true }
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    ) {
                        Text(
                            text = "Max Replies",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${config.maxReplies}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))

                    // Reply Limit List row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToLimitList)
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reply Limit List",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "View contacts with reply limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(Spacing.xxl)) }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TealAccent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    )
}

// ─── Reply Mode Option ────────────────────────────────────────────────────────

@Composable
private fun ReplyModeOption(
    mode: ReplyMode,
    selected: Boolean,
    onSelect: () -> Unit,
    onInfoClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(start = Spacing.xs, end = Spacing.xs, top = Spacing.xs, bottom = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = TealAccent),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp),
        ) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "More info about ${mode.label}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Duration Sub-Row ─────────────────────────────────────────────────────────

@Composable
private fun DurationSubRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 56.dp, end = Spacing.md, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = " $value",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TealAccent,
        )
        Text(
            text = "  (tap to change)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Dialogs ──────────────────────────────────────────────────────────────────

private fun ReplyMode.infoDescription(): String = when (this) {
    ReplyMode.EVERY_TIME ->
        "Sends an auto-reply every single time a new message arrives from any contact. " +
                "There are no restrictions or cooldowns — every incoming message triggers a reply. " +
                "Best suited for always-on responses such as customer service bots or away notifications."

    ReplyMode.REPLY_AND_WAIT ->
        "Sends one auto-reply per contact, then pauses for a configured wait period before " +
                "replying to that same contact again. If the contact sends another message during " +
                "the wait window, it is ignored. Use this to avoid flooding a chat with repeated " +
                "messages while still staying responsive."

    ReplyMode.REPLY_AFTER_DELAY ->
        "Delays the auto-reply by a configurable number of seconds after the message is received. " +
                "The reply is still sent every time, but with a pause before each one. This can make " +
                "responses feel more natural or give your server/AI time to process before answering."

    ReplyMode.REPLY_ONCE ->
        "Sends an auto-reply only once per contact, then permanently stops replying to them. " +
                "To reset a contact and allow another reply, clear the reply limit list or restart " +
                "auto-reply. Ideal for one-time welcome messages, promotional announcements, or " +
                "opt-in confirmation flows."
}

@Composable
private fun ReplyModeInfoDialog(mode: ReplyMode, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = TealAccent,
            )
        },
        title = {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = mode.infoDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        },
    )
}

@Composable
private fun SecondsInputDialog(
    title: String,
    currentSeconds: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(currentSeconds.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter duration in seconds (1 – 86400).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input
                        isError = input.toIntOrNull()?.let { it < 1 || it > 86_400 } ?: true
                    },
                    label = { Text("Seconds") },
                    isError = isError,
                    supportingText = if (isError) ({ Text("Enter 1–86400") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { text.toIntOrNull()?.let { onConfirm(it) } },
                enabled = !isError && text.isNotBlank(),
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

@Composable
private fun MaxRepliesDialog(
    current: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(current.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Max Replies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "Maximum number of replies to send per contact/chat (1 – 10000).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input
                        isError = input.toIntOrNull()?.let { it < 1 || it > 10_000 } ?: true
                    },
                    label = { Text("Max replies") },
                    isError = isError,
                    supportingText = if (isError) ({ Text("Enter 1–10000") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { text.toIntOrNull()?.let { onConfirm(it) } },
                enabled = !isError && text.isNotBlank(),
            ) { Text("OK") }
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
private fun ReplyTimingScreenPreview() {
    AutoReplyTheme {
        ReplyTimingScreen()
    }
}
