package com.psspl.autoreply.ui.screens.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
import com.psspl.autoreply.ui.screens.autoreplyconfig.ReplyType
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun DashboardScreen(
    onNavigateToReplyNotifications: () -> Unit = {},
    onNavigateToDirectMessage: () -> Unit = {},
    onNavigateToNotWorking: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToUpgrade: () -> Unit = {},
    onNavigateToAutoReplyConfig: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val isAutoReplyEnabled by viewModel.isAutoReplyEnabled.collectAsStateWithLifecycle()
    val autoReplyMessage by viewModel.autoReplyMessage.collectAsStateWithLifecycle()
    val selectedReplyType by viewModel.selectedReplyType.collectAsStateWithLifecycle()
    val sentRepliesCount by viewModel.sentRepliesCount.collectAsStateWithLifecycle()
    val defaultMessages by viewModel.defaultMessages.collectAsStateWithLifecycle()
    val messagesExpanded by viewModel.messagesExpanded.collectAsStateWithLifecycle()

    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        ConfirmationDialog(
            title = "Clear All",
            message = "This will remove all custom messages. Seeded default messages will be preserved.",
            confirmLabel = "Clear All",
            isDestructive = true,
            onConfirm = { viewModel.clearCustomMessages() },
            onDismiss = { showClearAllDialog = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Dashboard",
                actions = {
                    IconButton(onClick = onNavigateToDirectMessage) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Direct message",
                        )
                    }
                    IconButton(onClick = onNavigateToReplyNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Reply history",
                            )
                        }
                    }
                    TopbarMenu(
                        items = listOf(
                            TopbarMenuItem(
                                "Not Working?",
                                Icons.Filled.Build,
                                isDividerAfter = true,
                                onClick = onNavigateToNotWorking,
                            ),
                            TopbarMenuItem(
                                "Help",
                                Icons.AutoMirrored.Filled.Help,
                                onClick = onNavigateToHelp,
                            ),
                            TopbarMenuItem(
                                "Settings",
                                Icons.Filled.Settings,
                                onClick = onNavigateToSettings,
                            ),
                            TopbarMenuItem(
                                "Upgrade",
                                Icons.Filled.Star,
                                onClick = onNavigateToUpgrade,
                            ),
                        ),
                    )
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
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // ── Auto reply toggle ──────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Auto reply",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = if (isAutoReplyEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAutoReplyEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Switch(
                        checked = isAutoReplyEnabled,
                        onCheckedChange = { viewModel.toggleAutoReply(it) },
                    )
                }
            }

            // ── Auto reply text (clickable → config) ───────────────────────────
            AppCard(onClick = onNavigateToAutoReplyConfig) {
                Text(
                    text = "Auto reply text",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                if (selectedReplyType == ReplyType.CUSTOM) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = autoReplyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit auto reply",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedReplyType.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Configure ${selectedReplyType.label}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Stats row ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                AppCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Rules",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                AppCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Apps Enabled",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // ── Sent replies counter ───────────────────────────────────────────
            AppCard {
                Text(
                    text = "Sent auto replies",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = sentRepliesCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // ── Default Messages ───────────────────────────────────────────────
            MessagesCard(
                messages = defaultMessages,
                expanded = messagesExpanded,
                onToggleExpand = { viewModel.toggleMessagesExpanded() },
                onMessageClick = { viewModel.selectMessage(it) },
                onClearAll = { showClearAllDialog = true },
            )

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

// ─── Messages card ────────────────────────────────────────────────────────────

@Composable
private fun MessagesCard(
    messages: List<com.psspl.autoreply.database.entity.DefaultMessageEntity>,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onMessageClick: (com.psspl.autoreply.database.entity.DefaultMessageEntity) -> Unit,
    onClearAll: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        // ── Header row ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.md, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )

            // Overflow menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (expanded) "Hide Messages" else "Show Messages",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            onToggleExpand()
                            menuExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            onClearAll()
                            menuExpanded = false
                        },
                    )
                }
            }

            // Expand / collapse icon
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                    else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse messages" else "Expand messages",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Message list ──────────────────────────────────────────────────────
        if (expanded) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
            if (messages.isEmpty()) {
                Text(
                    text = "No messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
            } else {
                messages.forEachIndexed { index, message ->
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMessageClick(message) }
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    )
                    if (index < messages.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    AutoReplyTheme {
        DashboardScreen()
    }
}
