package com.psspl.autoreply.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import com.psspl.autoreply.utils.MatchType
import com.psspl.autoreply.utils.REPLY_TAGS
import com.psspl.autoreply.utils.ReplyOption

private val GreenBubble = Color(0xFFDCF8C6)
private val AccentGreen = Color(0xFF25D366)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordReplyFormScreen(
    ruleId: Int = 0,
    onBack: () -> Unit = {},
    viewModel: KeywordReplyFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load existing rule when editing
    LaunchedEffect(ruleId) {
        if (ruleId > 0) viewModel.loadRule(ruleId)
    }

    // Navigate back after successful save
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (ruleId == 0) "Add Keyword Rule" else "Edit Keyword Rule",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Chat Preview ─────────────────────────────────────────────────
            ChatPreview(
                keyword = state.keyword,
                replyText = state.replyText,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {

                // ── Keyword Input ─────────────────────────────────────────────
                SectionLabel(text = "Keyword / Incoming Message")
                OutlinedTextField(
                    value = state.keyword,
                    onValueChange = viewModel::onKeywordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Hello, Hi, How are you") },
                    singleLine = true,
                    isError = state.errorMessage != null && state.keyword.isBlank(),
                )

                // ── Reply Message Input ───────────────────────────────────────
                SectionLabel(text = "Reply Message")
                OutlinedTextField(
                    value = state.replyText,
                    onValueChange = viewModel::onReplyTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Type your auto-reply here…") },
                    maxLines = 6,
                    isError = state.errorMessage != null && state.replyText.isBlank(),
                )

                // ── Tag Chips ─────────────────────────────────────────────────
                SectionLabel(text = "Insert Tag")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    REPLY_TAGS.forEach { tag ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.insertTag(tag) },
                            label = { Text("{$tag}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }

                // ── Reply Options ─────────────────────────────────────────────
                SectionLabel(text = "Reply Options")
                ReplyOptionsSection(
                    selected = state.selectedReplyOptions,
                    onToggle = viewModel::onReplyOptionToggled,
                )

                // ── Match Options ─────────────────────────────────────────────
                SectionLabel(text = "Match Options")
                MatchOptionsSection(
                    selected = state.matchType,
                    onSelect = viewModel::onMatchTypeChange,
                )

                // ── Reply Alert (Send Email) ───────────────────────────────────
                SectionLabel(text = "Reply Alert")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = state.sendEmail,
                        onCheckedChange = viewModel::onSendEmailChange,
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Column {
                        Text(
                            text = "Send Email on Reply",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Receive an email notification when this rule triggers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Error Message ─────────────────────────────────────────────
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // ── Save Button ───────────────────────────────────────────────
                Button(
                    onClick = { viewModel.saveRule(ruleId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = if (ruleId == 0) "Add Rule" else "Update Rule",
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

// ─── Chat Preview ─────────────────────────────────────────────────────────────

@Composable
private fun ChatPreview(
    keyword: String,
    replyText: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFECE5DD), // WhatsApp chat background
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Incoming message (keyword)
            Box(modifier = Modifier.fillMaxWidth(0.75f)) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 2.dp,
                        topEnd = 12.dp,
                        bottomEnd = 12.dp,
                        bottomStart = 12.dp,
                    ),
                    color = Color.White,
                ) {
                    Text(
                        text = keyword.ifBlank { "Incoming keyword…" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (keyword.isBlank()) Color.Gray else Color.Black,
                        modifier = Modifier.padding(
                            horizontal = Spacing.sm,
                            vertical = Spacing.xs,
                        ),
                    )
                }
            }

            // Outgoing reply
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.End),
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 2.dp,
                        bottomEnd = 12.dp,
                        bottomStart = 12.dp,
                    ),
                    color = GreenBubble,
                ) {
                    Text(
                        text = replyText.ifBlank { "Auto-reply message…" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (replyText.isBlank()) Color.Gray else Color.Black,
                        modifier = Modifier.padding(
                            horizontal = Spacing.sm,
                            vertical = Spacing.xs,
                        ),
                    )
                }
            }
        }
    }
}

// ─── Reply Options Section ────────────────────────────────────────────────────

@Composable
private fun ReplyOptionsSection(
    selected: Set<ReplyOption>,
    onToggle: (ReplyOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        ReplyOption.entries.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = option in selected,
                    onCheckedChange = { onToggle(option) },
                )
                Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

// ─── Match Options Section ────────────────────────────────────────────────────

@Composable
private fun MatchOptionsSection(
    selected: MatchType,
    onSelect: (MatchType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(Spacing.sm),
    ) {
        MatchType.entries.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AccentGreen,
                    ),
                )
                Column {
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = when (type) {
                            MatchType.EXACT -> "Only triggers when message matches keyword exactly"
                            MatchType.CONTAINS -> "Triggers when message contains the keyword"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun KeywordReplyFormScreenPreview() {
    AutoReplyTheme {
        KeywordReplyFormScreen()
    }
}
