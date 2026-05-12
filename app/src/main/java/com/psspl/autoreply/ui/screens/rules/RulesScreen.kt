package com.psspl.autoreply.ui.screens.rules

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val GreenBubble = Color(0xFFDCF8C6)
private val ModuleGreen = Color(0xFF25D366)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToAddRule: () -> Unit = {},
    onNavigateToEditRule: (Int) -> Unit = {},
    onNavigateToReplyTiming: () -> Unit = {},
    viewModel: RulesViewModel = hiltViewModel(),
) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    // TODO: Global auto-reply toggle lives on the Dashboard — temporarily removed from here
    // val isModuleEnabled by viewModel.isModuleEnabled.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // ── Local UI state ────────────────────────────────────────────────────────
    var showSearch by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── File pickers ──────────────────────────────────────────────────────────
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportRules(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importRules(it) } }

    // ── Snackbar display ──────────────────────────────────────────────────────
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    // ── Auto-focus search field when it opens ─────────────────────────────────
    LaunchedEffect(showSearch) {
        if (showSearch) searchFocusRequester.requestFocus()
    }

    // ── Clear all dialog ──────────────────────────────────────────────────────
    if (showClearAllDialog) {
        ConfirmationDialog(
            title = "Clear All Rules",
            message = "This will permanently delete all keyword rules. This action cannot be undone.",
            confirmLabel = "Clear All",
            isDestructive = true,
            onConfirm = { viewModel.clearAllRules() },
            onDismiss = { showClearAllDialog = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Keyword Reply",
                navigationIcon = {
                    IconButton(onClick = {
                        if (showSearch) {
                            showSearch = false
                            keyboardController?.hide()
                            viewModel.onSearchQueryChange("")
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (showSearch) "Close search" else "Back",
                        )
                    }
                },
                actions = {
                    // Search toggle
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) viewModel.onSearchQueryChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search rules",
                        )
                    }

                    // Overflow menu: Reply Time, Backup, Restore, Clear All
                    TopbarMenu(
                        items = listOf(
                            TopbarMenuItem(
                                label = "Reply Time",
                                icon = Icons.Filled.Timer,
                                isDividerAfter = true,
                                onClick = onNavigateToReplyTiming,
                            ),
                            TopbarMenuItem(
                                label = "Backup",
                                icon = Icons.Filled.Backup,
                                isDividerAfter = false,
                                onClick = {
                                    backupLauncher.launch("keyword_rules_backup.json")
                                },
                            ),
                            TopbarMenuItem(
                                label = "Restore",
                                icon = Icons.Filled.Restore,
                                isDividerAfter = true,
                                onClick = {
                                    restoreLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                            ),
                            TopbarMenuItem(
                                label = "Clear All",
                                icon = Icons.Filled.DeleteSweep,
                                isDividerAfter = false,
                                onClick = { showClearAllDialog = true },
                            ),
                        ),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddRule,
                containerColor = ModuleGreen,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Rule")
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            // ── Module toggle bar ─────────────────────────────────────────────
            // TODO: Global auto-reply on/off is controlled from the Dashboard screen.
            //       This per-screen toggle is temporarily hidden to avoid confusion.
            // ModuleToggleBar(
            //     enabled = isModuleEnabled,
            //     onToggle = viewModel::setModuleEnabled,
            // )

            // ── Search bar (animated) ─────────────────────────────────────────
            AnimatedVisibility(
                visible = showSearch,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                        .focusRequester(searchFocusRequester),
                    placeholder = { Text("Search keyword or reply…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search",
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ModuleGreen,
                        cursorColor = ModuleGreen,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() },
                    ),
                )
            }

            // ── List or empty state ───────────────────────────────────────────
            if (rules.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                    else "No keyword rules yet",
                    description = if (searchQuery.isNotBlank()) "Try a different search term"
                    else "Tap + to add your first auto-reply keyword rule",
                    icon = Icons.Filled.Edit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.md),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(
                        items = rules,
                        key = { it.id },
                    ) { rule ->
                        SwipeableRuleItem(
                            rule = rule,
                            onClick = { onNavigateToEditRule(rule.id) },
                            onDelete = { viewModel.deleteRule(rule) },
                        )
                    }
                }
            }
        }
    }
}

// ─── Module Toggle Bar ────────────────────────────────────────────────────────

@Composable
private fun ModuleToggleBar(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (enabled) ModuleGreen else MaterialTheme.colorScheme.surfaceVariant,
        label = "ModuleToggleBg",
    )
    val textColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (enabled) "Module ON" else "Module OFF",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF128C7E),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

// ─── List Header ─────────────────────────────────────────────────────────────

@Composable
private fun RuleListHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    ) {
        Text(
            text = "Incoming message",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Reply message",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

// ─── Swipeable Rule Item ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableRuleItem(
    rule: KeywordRuleEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
            }
            false // never auto-dismiss — dialog controls the outcome
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
    )

    // Reset swipe position when dialog is dismissed without deleting
    LaunchedEffect(showDeleteDialog) {
        if (!showDeleteDialog) dismissState.reset()
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Rule",
            message = "Delete \"${rule.keyword}\"? This action cannot be undone.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { onDelete() },
            onDismiss = { showDeleteDialog = false },
        )
    }

    val context = LocalContext.current

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { DeleteBackground() },
        modifier = modifier,
    ) {
        RuleListItem(
            rule = rule,
            onClick = onClick,
            onLongClick = {
                Toast.makeText(
                    context,
                    "•  Tap to edit\n•  Swipe left to delete",
                    Toast.LENGTH_SHORT,
                ).show()
            },
        )
    }
}

@Composable
private fun DeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD32F2F))
            .padding(horizontal = Spacing.md),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
            Text(
                text = "Delete",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─── Rule List Item (chat preview style) ─────────────────────────────────────

private val ChatBackground = Color(0xFFECE5DD)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RuleListItem(
    rule: KeywordRuleEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        // Chat preview area
        Surface(
            color = ChatBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // Incoming keyword bubble — left-aligned, wraps content
                Box(modifier = Modifier.fillMaxWidth(0.75f)) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 2.dp, topEnd = 12.dp,
                            bottomEnd = 12.dp, bottomStart = 12.dp,
                        ),
                        color = Color.White,
                    ) {
                        Text(
                            text = rule.keyword,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical = Spacing.xs
                            ),
                        )
                    }
                }

                // Outgoing reply bubble — right-aligned, wraps content
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .align(Alignment.End),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 12.dp, topEnd = 2.dp,
                            bottomEnd = 12.dp, bottomStart = 12.dp,
                        ),
                        color = GreenBubble,
                    ) {
                        Text(
                            text = rule.replyText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical = Spacing.xs
                            ),
                        )
                    }
                }
            }
        }

        // Row divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun RulesScreenPreview() {
    AutoReplyTheme {
        RulesScreen()
    }
}
