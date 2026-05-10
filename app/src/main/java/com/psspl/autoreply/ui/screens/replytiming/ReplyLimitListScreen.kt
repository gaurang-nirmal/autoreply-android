package com.psspl.autoreply.ui.screens.replytiming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.ReplyLimitTrackingEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReplyLimitListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: ReplyTimingViewModel = hiltViewModel(),
) {
    val tracking by viewModel.limitTracking.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        ConfirmationDialog(
            title = "Reset All Counts",
            message = "This will reset the reply count for all contacts. They will be eligible to receive replies again.",
            confirmLabel = "Reset",
            isDestructive = true,
            onConfirm = { viewModel.clearAllTracking(); showClearAllDialog = false },
            onDismiss = { showClearAllDialog = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reply Limit List",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (tracking.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Reset all",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        if (tracking.isEmpty()) {
            EmptyState(
                title = "No contacts tracked yet",
                description = "Contacts appear here once they have received at least one reply.",
                icon = Icons.Filled.Person,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
            ) {
                // Header row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Contact",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "Replies / Max",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                items(items = tracking, key = { "${it.replyType}_${it.contactKey}" }) { record ->
                    TrackingItem(
                        record = record,
                        maxReplies = config.maxReplies,
                        onReset = { viewModel.clearContactTracking(record.contactKey) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

// ─── Tracking Item ────────────────────────────────────────────────────────────

@Composable
private fun TrackingItem(
    record: ReplyLimitTrackingEntity,
    maxReplies: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateStr = remember(record.lastReplyAt) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            .format(Date(record.lastReplyAt))
    }
    val limitReached = record.replyCount >= maxReplies

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.contactKey,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Last reply: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.replyCount} / $maxReplies",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (limitReached) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (limitReached) {
                    Text(
                        text = "Limit reached",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Reset count for ${record.contactKey}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ReplyLimitListScreenPreview() {
    AutoReplyTheme {
        ReplyLimitListScreen()
    }
}
