package com.psspl.autoreply.ui.screens.followup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.psspl.autoreply.database.entity.FollowUpContactEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun FollowUpManageScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: FollowUpViewModel = hiltViewModel(),
) {
    val specificContacts by viewModel.specificContacts.collectAsStateWithLifecycle()
    val excludeContacts by viewModel.excludeContacts.collectAsStateWithLifecycle()

    // "SPECIFIC" | "EXCLUDE" | null (dialog closed)
    var addingToList by remember { mutableStateOf<String?>(null) }
    var addText by remember { mutableStateOf("") }

    // Add message dialog
    if (addingToList != null) {
        AlertDialog(
            onDismissRequest = {
                addingToList = null
                addText = ""
            },
            title = {
                Text(
                    text = if (addingToList == "SPECIFIC") "Add to Specific list"
                    else "Add to Exclude list",
                )
            },
            text = {
                OutlinedTextField(
                    value = addText,
                    onValueChange = { addText = it },
                    label = { Text("Reply message text") },
                    placeholder = { Text("e.g. I am busy") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (addText.isNotBlank()) {
                            viewModel.addContact(addingToList!!, addText.trim())
                            addingToList = null
                            addText = ""
                        }
                    },
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { addingToList = null; addText = "" }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Reply Messages",
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
            contentPadding = PaddingValues(bottom = Spacing.xxl),
        ) {
            // ── Specific list ──────────────────────────────────────────────────
            item {
                ContactSectionHeader(
                    title = "Specific Reply Messages",
                    subtitle = "Follow-up is sent only for these messages",
                    onAdd = { addingToList = "SPECIFIC" },
                )
            }
            if (specificContacts.isEmpty()) {
                item {
                    EmptyState(
                        title = "No specific messages",
                        description = "Tap + to add reply messages for which follow-ups will be sent",
                        icon = Icons.Filled.List,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    )
                }
            } else {
                items(specificContacts, key = { it.id }) { contact ->
                    ContactItem(
                        contact = contact,
                        onDelete = { viewModel.deleteContact(contact) },
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))
            }

            // ── Exclude list ───────────────────────────────────────────────────
            item {
                ContactSectionHeader(
                    title = "Exclude Reply Messages",
                    subtitle = "Follow-up is NOT sent for these messages",
                    onAdd = { addingToList = "EXCLUDE" },
                )
            }
            if (excludeContacts.isEmpty()) {
                item {
                    EmptyState(
                        title = "No excluded messages",
                        description = "Tap + to add reply messages that should not trigger follow-ups",
                        icon = Icons.Filled.List,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    )
                }
            } else {
                items(excludeContacts, key = { it.id }) { contact ->
                    ContactItem(
                        contact = contact,
                        onDelete = { viewModel.deleteContact(contact) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactSectionHeader(
    title: String,
    subtitle: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onAdd) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ContactItem(
    contact: FollowUpContactEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = contact.messageText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FollowUpManageScreenPreview() {
    AutoReplyTheme { FollowUpManageScreen() }
}
