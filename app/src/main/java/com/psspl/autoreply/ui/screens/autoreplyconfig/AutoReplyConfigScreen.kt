package com.psspl.autoreply.ui.screens.autoreplyconfig

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val TealAccent = Color(0xFF128C7E)

// ─── Tags ─────────────────────────────────────────────────────────────────────

private val REPLY_TAGS = listOf(
    "{name}" to "name",
    "{first_name}" to "first name",
    "{last_name}" to "last name",
    "{date}" to "date",
    "{time}" to "time",
    "{message}" to "message",
)

@Composable
fun AutoReplyConfigScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToReplyTiming: (replyTypeKey: String) -> Unit = {},
    onNavigateToFollowUp: () -> Unit = {},
    viewModel: AutoReplyConfigViewModel = hiltViewModel(),
) {
    val loadedMessage by viewModel.autoReplyMessage.collectAsStateWithLifecycle()
    val selectedReplyType by viewModel.replyType.collectAsStateWithLifecycle()

    // Local text field state — initialised once when DB value arrives
    var messageField by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var initialised by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(loadedMessage) {
        if (!initialised && loadedMessage.isNotEmpty()) {
            messageField =
                TextFieldValue(loadedMessage, selection = TextRange(loadedMessage.length))
            initialised = true
        }
    }

    fun insertTag(tag: String) {
        val cursor = messageField.selection.end
        val before = messageField.text.substring(0, cursor)
        val after = messageField.text.substring(cursor)
        val newText = "$before$tag$after"
        messageField = TextFieldValue(newText, selection = TextRange(cursor + tag.length))
    }

    fun saveAndBack() {
        viewModel.setMessage(messageField.text)
        onBack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Auto reply text",
                navigationIcon = {
                    IconButton(onClick = { saveAndBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { saveAndBack() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
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
            // ── Custom message input ───────────────────────────────────────────
            OutlinedTextField(
                value = messageField,
                onValueChange = { messageField = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = Spacing.xxxl + Spacing.xxl)
                    .padding(Spacing.md),
                placeholder = {
                    Text(
                        text = "Enter auto reply message…",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = MaterialTheme.shapes.medium,
            )

            // ── Reply Tags ─────────────────────────────────────────────────────
            ConfigSectionHeader(title = "Reply Tags")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md)
                    .padding(bottom = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                REPLY_TAGS.forEach { (tag, display) ->
                    AssistChip(
                        onClick = { insertTag(tag) },
                        label = {
                            Text(
                                text = display,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        border = null,
                    )
                }
            }

            HorizontalDivider()

            // ── Reply options ──────────────────────────────────────────────────
            ConfigSectionHeader(title = "Reply options")
            Column(modifier = Modifier.selectableGroup()) {
                ReplyType.entries.forEach { type ->
                    ReplyTypeRow(
                        type = type,
                        selected = selectedReplyType == type,
                        onSelect = { viewModel.setReplyType(type) },
                    )
                }
            }

            HorizontalDivider()

            // ── Reply Time ─────────────────────────────────────────────────────
            // Navigate to the timing screen for the *currently selected* reply type
            // so Keyword/Menu/etc. each have their own independent timing config.
            ConfigSectionHeader(title = "Reply Time")
            ConfigNavRow(
                title = "Reply Time",
                subtitle = "Choose how often to send the auto-reply message",
                onClick = { onNavigateToReplyTiming(selectedReplyType.name.lowercase()) },
            )

            HorizontalDivider()

            // ── Follow-Up Message ──────────────────────────────────────────────
            ConfigSectionHeader(title = "Follow-Up Message")
            ConfigNavRow(
                title = "Follow-Up Message",
                subtitle = "Automatically send a reminder when the customer doesn't reply",
                onClick = onNavigateToFollowUp,
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

// ─── Private helpers ──────────────────────────────────────────────────────────

@Composable
private fun ConfigSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TealAccent,
        modifier = modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
    )
}

@Composable
private fun ReplyTypeRow(
    type: ReplyType,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = TealAccent),
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Column {
            Text(
                text = type.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = type.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConfigNavRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoReplyConfigScreenPreview() {
    AutoReplyTheme { AutoReplyConfigScreen() }
}
