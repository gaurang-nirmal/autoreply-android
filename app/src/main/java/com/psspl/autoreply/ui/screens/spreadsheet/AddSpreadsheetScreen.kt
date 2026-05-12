package com.psspl.autoreply.ui.screens.spreadsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.data.remote.model.DriveFile
import com.psspl.autoreply.database.entity.SpreadsheetEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val SheetGreen = Color(0xFF0F9D58)
private val SheetGreenLight = Color(0xFFE8F5E9)

/**
 * Two-step screen:
 *  Step 1 = pick an existing Drive sheet OR tap "Create a New Spreadsheet"
 *  Step 2 = enter a name for the new sheet → create → success state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpreadsheetScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSheetLinked: () -> Unit = {},
    viewModel: SpreadsheetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val linkedSheets by viewModel.sheets.collectAsStateWithLifecycle()

    // Track local UI sub-state
    var mode by remember { mutableStateOf<AddMode>(AddMode.PickOrCreate) }

    // Load Drive files when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadDriveFiles()
    }

    when (val currentMode = mode) {
        AddMode.PickOrCreate -> PickOrCreateContent(
            modifier = modifier,
            onBack = onBack,
            driveFiles = uiState.driveFiles,
            isLoading = uiState.isLoadingDriveFiles,
            linkedSheets = linkedSheets,
            onPickExisting = { file ->
                viewModel.linkSheet(file.id, file.name)
                onSheetLinked()
            },
            onCreateNew = { mode = AddMode.CreateNew },
        )

        AddMode.CreateNew -> CreateNewContent(
            modifier = modifier,
            onBack = { mode = AddMode.PickOrCreate },
            isCreating = uiState.isSyncing,
            onCreateConfirm = { name ->
                viewModel.createSpreadsheet(name) { _ ->
                    mode = AddMode.Success(name)
                }
            },
        )

        is AddMode.Success -> SuccessContent(
            sheetName = currentMode.name,
            onDone = onSheetLinked,
        )
    }
}

// ─── Mode ─────────────────────────────────────────────────────────────────────

private sealed class AddMode {
    object PickOrCreate : AddMode()
    object CreateNew : AddMode()
    data class Success(val name: String) : AddMode()
}

// ─── Step 1: Pick or Create ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickOrCreateContent(
    driveFiles: List<DriveFile>,
    isLoading: Boolean,
    linkedSheets: List<SpreadsheetEntity>,
    onBack: () -> Unit,
    onPickExisting: (DriveFile) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val linkedIds = linkedSheets.map { it.id }.toSet()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add Spreadsheet",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
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
                .padding(innerPadding),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SheetGreen)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = "Loading your Google Drive files…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Existing Drive sheets
                    if (driveFiles.isNotEmpty()) {
                        items(driveFiles) { file ->
                            val alreadyLinked = file.id in linkedIds
                            DriveFileItem(
                                file = file,
                                alreadyLinked = alreadyLinked,
                                onClick = {
                                    if (!alreadyLinked) onPickExisting(file)
                                },
                            )
                            HorizontalDivider()
                        }
                    }

                    // "Create a New Spreadsheet" always at the bottom
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCreateNew() }
                                .padding(horizontal = Spacing.md, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SheetGreenLight,
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = SheetGreen,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Text(
                                text = "Create a New Spreadsheet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SheetGreen,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Drive file list item ─────────────────────────────────────────────────────

@Composable
private fun DriveFileItem(
    file: DriveFile,
    alreadyLinked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !alreadyLinked, onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = SheetGreenLight,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.TableChart,
                contentDescription = null,
                tint = SheetGreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            )
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text = file.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (alreadyLinked) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (alreadyLinked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Already linked",
                tint = SheetGreen,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Step 2: Create new sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNewContent(
    isCreating: Boolean,
    onBack: () -> Unit,
    onCreateConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheetName by remember { mutableStateOf("") }
    val isNameValid = sheetName.trim().isNotEmpty()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Create Spreadsheet",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
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
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Icon
            Surface(
                shape = CircleShape,
                color = SheetGreenLight,
                modifier = Modifier.size(88.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.TableChart,
                    contentDescription = null,
                    tint = SheetGreen,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Name your spreadsheet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "The sheet will be created in your Google Drive with keyword and reply_message columns pre-seeded.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = sheetName,
                onValueChange = { sheetName = it },
                label = { Text("Sheet name") },
                singleLine = true,
                enabled = !isCreating,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { if (isNameValid) onCreateConfirm(sheetName.trim()) },
                enabled = isNameValid && !isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = SheetGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Creating…")
                } else {
                    Text("Create & Link")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            TextButton(
                onClick = onBack,
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

// ─── Step 3: Success ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    sheetName: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            // Animated success icon
            Surface(
                shape = CircleShape,
                color = SheetGreenLight,
                modifier = Modifier.size(100.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = SheetGreen,
                        modifier = Modifier.size(56.dp),
                    )
                    // Checkmark badge
                    Surface(
                        shape = CircleShape,
                        color = SheetGreen,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Spreadsheet Created!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SheetGreen,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "\"$sheetName\" was created in Google Sheets and linked to the app. " +
                        "Open it in Google Sheets to add your keyword → reply_message rows, " +
                        "then come back and tap Sync.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = SheetGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("Done")
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AddSpreadsheetScreenPreview() {
    AutoReplyTheme {
        AddSpreadsheetScreen()
    }
}
