package com.psspl.autoreply.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.SettingsItem
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    onNavigateToAutomaticOn: () -> Unit = {},
    onNavigateToReplyTime: () -> Unit = {},
    onNavigateToReplyHeaderFooter: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToAppSecurity: () -> Unit = {},
    onNavigateToDisplay: () -> Unit = {},
    onNavigateToInviteFriend: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        ConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            confirmLabel = "Sign Out",
            isDestructive = true,
            onConfirm = onSignOut,
            onDismiss = { showSignOutDialog = false },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Settings") },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            item {
                ProfileSection(
                    displayName = uiState.displayName,
                    accountType = uiState.accountType,
                    photoUrl = uiState.photoUrl,
                    onSignOut = { showSignOutDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.FlashOn,
                    title = "Automatic On",
                    subtitle = "Schedule time, Bluetooth mode.",
                    onClick = onNavigateToAutomaticOn,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Schedule,
                    title = "Reply Time",
                    subtitle = "Choose how often to send the auto-reply message",
                    onClick = onNavigateToReplyTime,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Notes,
                    title = "Reply Header & Footer",
                    subtitle = "Customize the reply message header and footer.",
                    onClick = onNavigateToReplyHeaderFooter,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Backup,
                    title = "Backup & Restore",
                    subtitle = "Backup and restore your AutoReply.",
                    onClick = onNavigateToBackupRestore,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "App Security",
                    subtitle = "Screen lock security",
                    onClick = onNavigateToAppSecurity,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "Display",
                    subtitle = "Theme",
                    onClick = onNavigateToDisplay,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.PersonAdd,
                    title = "Invite a Friend",
                    subtitle = "Share the app with your friends and family.",
                    onClick = onNavigateToInviteFriend,
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    displayName: String,
    accountType: String,
    photoUrl: String?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
            )
        }

        Spacer(modifier = Modifier.width(Spacing.xs))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hi, $displayName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Account type: $accountType",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(onClick = onSignOut) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign out",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AutoReplyTheme {
        SettingsScreen()
    }
}
