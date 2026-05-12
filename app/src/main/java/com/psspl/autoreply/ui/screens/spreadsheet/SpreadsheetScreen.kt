package com.psspl.autoreply.ui.screens.spreadsheet

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.SpreadsheetEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SheetGreen = Color(0xFF0F9D58)
private val SheetGreenLight = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToAddSpreadsheet: () -> Unit = {},
    onNavigateToViewSpreadsheet: (spreadsheetId: String, sheetName: String) -> Unit = { _, _ -> },
    onNavigateToReplyTiming: () -> Unit = {},
    viewModel: SpreadsheetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheets by viewModel.sheets.collectAsStateWithLifecycle()
    val isAutoSync by viewModel.isAutoSync.collectAsStateWithLifecycle()
    val isAutoSave by viewModel.isAutoSave.collectAsStateWithLifecycle()
    val saveSheetId by viewModel.saveSheetId.collectAsStateWithLifecycle()
    val syncIntervalHours by viewModel.syncIntervalHours.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf<SpreadsheetEntity?>(null) }
    var showSyncIntervalDialog by remember { mutableStateOf(false) }

    // ── Google auth resolution launcher ───────────────────────────────────────
    val authResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val intent = if (result.resultCode == Activity.RESULT_OK) result.data else null
        viewModel.onAuthorizationResult(intent) {
            onNavigateToAddSpreadsheet()
        }
    }

    // Observe one-shot auth events
    LaunchedEffect(Unit) {
        viewModel.authEvent.collect { event ->
            when (event) {
                is GoogleAuthEvent.NeedsResolution -> {
                    authResolutionLauncher.launch(
                        IntentSenderRequest.Builder(event.pendingIntent.intentSender).build()
                    )
                }
            }
        }
    }

    // Snackbar
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { sheet ->
        ConfirmationDialog(
            title = "Remove Spreadsheet",
            message = "Remove \"${sheet.name}\"? All locally cached rules from this sheet will be deleted. The original Google Sheet is not affected.",
            confirmLabel = "Remove",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSheet(sheet.id, sheet.name)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null },
        )
    }

    // Sync interval dialog
    if (showSyncIntervalDialog) {
        SyncIntervalDialog(
            currentHours = syncIntervalHours,
            onConfirm = { hours ->
                viewModel.setSyncInterval(hours)
                showSyncIntervalDialog = false
            },
            onDismiss = { showSyncIntervalDialog = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Spreadsheet",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    // Overflow: Reply Time (same pattern as Keyword/Menu screens)
                    TopbarMenu(
                        items = listOf(
                            TopbarMenuItem(
                                label = "Reply Time",
                                icon = Icons.Filled.Timer,
                                onClick = onNavigateToReplyTiming,
                            ),
                            TopbarMenuItem(
                                label = "Sync All Now",
                                icon = Icons.Filled.CloudSync,
                                onClick = { viewModel.syncAllSheets() },
                            ),
                        ),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.requestGoogleAuthorization {
                        onNavigateToAddSpreadsheet()
                    }
                },
                containerColor = SheetGreen,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add spreadsheet")
            }
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

            // ── Feature icon header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = SheetGreenLight,
                    modifier = Modifier.size(80.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = SheetGreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                    )
                }
            }

            // ── Info banner ──────────────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = SheetGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Link your Google Sheets to auto-reply based on keyword → reply_message rows. " +
                                "Your sheet needs two columns: keyword (col A) and reply_message (col B).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ── Spreadsheets section ─────────────────────────────────────────
            SectionHeader(title = "Spreadsheets")

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    if (sheets.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xl),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.TableChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(48.dp),
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                Text(
                                    text = "No spreadsheets linked yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text = "Tap + to add your Google Sheet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    } else {
                        sheets.forEachIndexed { index, sheet ->
                            SheetListItem(
                                sheet = sheet,
                                isSaveSheet = saveSheetId == sheet.id,
                                isSyncing = uiState.isSyncing,
                                onView = { onNavigateToViewSpreadsheet(sheet.id, sheet.name) },
                                onSync = { viewModel.syncSheet(sheet.id, sheet.name) },
                                onDelete = { showDeleteDialog = sheet },
                                onSetSaveSheet = {
                                    val newId = if (saveSheetId == sheet.id) "" else sheet.id
                                    viewModel.setSaveSheetId(newId)
                                },
                            )
                            if (index < sheets.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ── Spreadsheet Sync section ─────────────────────────────────────
            SectionHeader(title = "Spreadsheet Sync")

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Auto Sync toggle
                    SettingRow(
                        title = "Auto Sync",
                        subtitle = "Automatically sync rules from Google Sheets",
                        trailing = {
                            Switch(
                                checked = isAutoSync,
                                onCheckedChange = viewModel::setAutoSync,
                            )
                        },
                    )
                    HorizontalDivider()

                    // Sync Interval (only shown when Auto Sync is ON)
                    AnimatedVisibility(
                        visible = isAutoSync,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            SettingRow(
                                title = "Sync Interval",
                                subtitle = "Every $syncIntervalHours hour${if (syncIntervalHours > 1) "s" else ""}",
                                trailing = {
                                    TextButton(onClick = { showSyncIntervalDialog = true }) {
                                        Text(
                                            text = "CHANGE",
                                            color = SheetGreen,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }

                    // Manual sync button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    ) {
                        Button(
                            onClick = { viewModel.syncAllSheets() },
                            enabled = sheets.isNotEmpty() && !uiState.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = SheetGreen),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                            }
                            Text(if (uiState.isSyncing) "Syncing…" else "Sync Now")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ── Save Reply Messages section ───────────────────────────────────
            SectionHeader(title = "Save Reply Messages")

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Auto Save toggle
                    SettingRow(
                        title = "Auto Save",
                        subtitle = "Automatically append each sent reply to the selected sheet",
                        trailing = {
                            Switch(
                                checked = isAutoSave,
                                onCheckedChange = viewModel::setAutoSave,
                                enabled = saveSheetId.isNotEmpty(),
                            )
                        },
                    )
                    HorizontalDivider()

                    // Save sheet selection info
                    SettingRow(
                        title = "Save to Sheet",
                        subtitle = if (saveSheetId.isEmpty()) {
                            "Tap the ☁ icon on a sheet above to select it as the save target"
                        } else {
                            sheets.find { it.id == saveSheetId }?.name
                                ?.let { "\"$it\" selected" }
                                ?: "Sheet selected"
                        },
                        trailing = {
                            if (saveSheetId.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = SheetGreen,
                                )
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = SheetGreen,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    )
}

// ─── Setting Row ──────────────────────────────────────────────────────────────

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}

// ─── Sheet List Item ──────────────────────────────────────────────────────────

@Composable
private fun SheetListItem(
    sheet: SpreadsheetEntity,
    isSaveSheet: Boolean,
    isSyncing: Boolean,
    onView: () -> Unit,
    onSync: () -> Unit,
    onDelete: () -> Unit,
    onSetSaveSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Sheet icon
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SheetGreenLight,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.TableChart,
                contentDescription = null,
                tint = SheetGreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            )
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        // Sheet name + last sync
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sheet.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (sheet.lastSyncAt == 0L) {
                    "Never synced"
                } else {
                    "Last sync: ${formatRelativeTime(sheet.lastSyncAt)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSaveSheet) {
                Text(
                    text = "• Save target",
                    style = MaterialTheme.typography.labelSmall,
                    color = SheetGreen,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Overflow menu
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Sheet options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("View rules") },
                    leadingIcon = {
                        Icon(Icons.Filled.Visibility, contentDescription = null)
                    },
                    onClick = { menuExpanded = false; onView() },
                )
                DropdownMenuItem(
                    text = { Text("Sync now") },
                    leadingIcon = {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Sync, contentDescription = null)
                        }
                    },
                    onClick = { menuExpanded = false; onSync() },
                    enabled = !isSyncing,
                )
                DropdownMenuItem(
                    text = {
                        Text(if (isSaveSheet) "Unset save target" else "Set as save target")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            tint = if (isSaveSheet) SheetGreen else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = { menuExpanded = false; onSetSaveSheet() },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = { menuExpanded = false; onDelete() },
                )
            }
        }
    }
}

// ─── Sync Interval Dialog ─────────────────────────────────────────────────────

@Composable
private fun SyncIntervalDialog(
    currentHours: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputText by remember { mutableStateOf(currentHours.toString()) }
    var hours by remember { mutableStateOf(currentHours) }
    var isError by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sync Interval",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column {
                Text(
                    text = "Set how often to automatically sync your spreadsheets (1–168 hours).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { input ->
                        inputText = input
                        val parsed = input.toIntOrNull()
                        isError = parsed == null || parsed < 1 || parsed > 168
                        if (!isError && parsed != null) hours = parsed
                    },
                    label = { Text("Hours (1–168)") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a number between 1 and 168") }
                    } else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isError) onConfirm(hours) },
                enabled = !isError,
                colors = ButtonDefaults.buttonColors(containerColor = SheetGreen),
            ) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatRelativeTime(epochMs: Long): String {
    val diffMs = System.currentTimeMillis() - epochMs
    return when {
        diffMs < 60_000 -> "just now"
        diffMs < 3_600_000 -> "${diffMs / 60_000} min ago"
        diffMs < 86_400_000 -> "${diffMs / 3_600_000} hr ago"
        else -> SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(epochMs))
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SpreadsheetScreenPreview() {
    AutoReplyTheme {
        SpreadsheetScreen()
    }
}
