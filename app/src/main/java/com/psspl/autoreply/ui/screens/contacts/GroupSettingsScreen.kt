package com.psspl.autoreply.ui.screens.contacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.GroupEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.GreenContainer
import com.psspl.autoreply.ui.theme.GreenPrimary
import com.psspl.autoreply.ui.theme.Spacing
import com.psspl.autoreply.utils.GroupMode

@Composable
fun GroupSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: GroupSettingsViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    var showAddGroupDialog by remember { mutableStateOf(false) }

    val selectedMode = runCatching { GroupMode.valueOf(config.groupMode) }
        .getOrDefault(GroupMode.ALL_GROUPS)

    val showGroupList =
        selectedMode == GroupMode.MY_GROUP_LIST || selectedMode == GroupMode.EXCEPT_MY_GROUP_LIST

    if (showAddGroupDialog) {
        AddGroupDialog(
            onConfirm = { name -> viewModel.addGroup(name); showAddGroupDialog = false },
            onDismiss = { showAddGroupDialog = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Group Settings",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {

            // ── Section label ─────────────────────────────────────────────────
            Text(
                text = "AUTO REPLY TO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sm),
                letterSpacing = androidx.compose.ui.unit.TextUnit(
                    1.5f,
                    androidx.compose.ui.unit.TextUnitType.Sp,
                ),
            )

            // ── Mode selection cards ──────────────────────────────────────────
            GroupModeCard(
                icon = Icons.Filled.Groups,
                title = "All groups",
                subtitle = "Automatically reply in every group chat",
                selected = selectedMode == GroupMode.ALL_GROUPS,
                onClick = { viewModel.setGroupMode(GroupMode.ALL_GROUPS) },
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            GroupModeCard(
                icon = Icons.Filled.GroupAdd,
                title = "My group list",
                subtitle = "Reply only in groups you've saved below",
                selected = selectedMode == GroupMode.MY_GROUP_LIST,
                onClick = { viewModel.setGroupMode(GroupMode.MY_GROUP_LIST) },
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            GroupModeCard(
                icon = Icons.Filled.Block,
                title = "Except my group list",
                subtitle = "Reply in all groups except those saved below",
                selected = selectedMode == GroupMode.EXCEPT_MY_GROUP_LIST,
                onClick = { viewModel.setGroupMode(GroupMode.EXCEPT_MY_GROUP_LIST) },
            )

            // ── Group list section (animated) ─────────────────────────────────
            AnimatedVisibility(
                visible = showGroupList,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = Spacing.xs, bottom = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "GROUP LIST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(
                                1.5f,
                                androidx.compose.ui.unit.TextUnitType.Sp,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GreenPrimary)
                                .clickable { showAddGroupDialog = true }
                                .padding(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add group",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        if (groups.isEmpty()) {
                            val emptyText = if (selectedMode == GroupMode.MY_GROUP_LIST)
                                "Add groups to reply only in them"
                            else
                                "Add groups to exclude from auto reply"
                            EmptyState(
                                icon = Icons.Filled.Groups,
                                title = emptyText,
                                description = "Tap + to add a group name",
                            )
                        } else {
                            groups.forEachIndexed { index, group ->
                                GroupListItem(
                                    group = group,
                                    onDelete = { viewModel.deleteGroup(group) },
                                )
                                if (index < groups.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ── Group mode card ───────────────────────────────────────────────────────────

@Composable
private fun GroupModeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) GreenPrimary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        // No background change on selection — border + icon + text colour is sufficient
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (selected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) GreenPrimary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Group list item ───────────────────────────────────────────────────────────

@Composable
private fun GroupListItem(group: GroupEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = group.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
            )
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text = group.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ── Add group dialog ──────────────────────────────────────────────────────────

@Composable
private fun AddGroupDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GreenContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        title = {
            Text(
                text = "Add Group",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = "Enter the group name exactly as it appears in the notification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Group name") },
                    supportingText = { Text("Case insensitive") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (input.isNotBlank()) onConfirm(input) },
                enabled = input.isNotBlank()
            ) {
                Text("ADD", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun GroupSettingsScreenPreview() {
    AutoReplyTheme { GroupSettingsScreen() }
}
