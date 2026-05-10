package com.psspl.autoreply.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
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
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
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
                                onClick = onNavigateToNotWorking
                            ),
                            TopbarMenuItem(
                                "Help",
                                Icons.AutoMirrored.Filled.Help,
                                onClick = onNavigateToHelp
                            ),
                            TopbarMenuItem(
                                "Settings",
                                Icons.Filled.Settings,
                                onClick = onNavigateToSettings
                            ),
                            TopbarMenuItem(
                                "Upgrade",
                                Icons.Filled.Star,
                                onClick = onNavigateToUpgrade
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
                .padding(Spacing.md),
        ) {
            Text(
                text = "Auto-reply is inactive",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
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
            Spacer(modifier = Modifier.height(Spacing.lg))
            EmptyState(
                title = "No auto-replies configured",
                description = "Create a rule to start sending automated replies",
                icon = Icons.Filled.Notifications,
                modifier = Modifier.weight(1f),
            )
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
