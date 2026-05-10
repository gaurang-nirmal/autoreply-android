package com.psspl.autoreply.ui.screens.menureply

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.MenuReplyEntity
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

// ─── Colors (WhatsApp-aligned) ────────────────────────────────────────────────
private val MenuItemGreen = Color(0xFFDCF8C6)
private val TealAccent = Color(0xFF128C7E)
private val ConnectorGray = Color(0xFFB0BEC5)
private val AccentGreen = Color(0xFF25D366)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuReplyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToAddTrigger: () -> Unit = {},
    onNavigateToEditTrigger: (Int) -> Unit = {},
    onNavigateToAddItem: (menuReplyId: Int, parentItemId: Int) -> Unit = { _, _ -> },
    onNavigateToEditItem: (menuReplyId: Int, itemId: Int) -> Unit = { _, _ -> },
    onNavigateToMoreOptions: (itemId: Int) -> Unit = {},
    onNavigateToItemChildren: (menuReplyId: Int, itemId: Int) -> Unit = { _, _ -> },
    onNavigateToReplyTiming: () -> Unit = {},
    viewModel: MenuReplyViewModel = hiltViewModel(),
) {
    val repliesWithItems by viewModel.menuRepliesWithItems.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // ── Local UI state ────────────────────────────────────────────────────────
    var replyToDelete by remember { mutableStateOf<MenuReplyEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<MenuReplyItemEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // ── File pickers ──────────────────────────────────────────────────────────
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportReplies(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importReplies(it) } }

    // ── Snackbar display ──────────────────────────────────────────────────────
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    // ── Confirmation dialogs ──────────────────────────────────────────────────
    replyToDelete?.let { reply ->
        ConfirmationDialog(
            title = "Delete Menu Reply",
            message = "Delete trigger \"${reply.triggerMessage}\" and all its menu options?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.deleteReply(reply) },
            onDismiss = { replyToDelete = null },
        )
    }

    itemToDelete?.let { item ->
        ConfirmationDialog(
            title = "Delete Menu Option",
            message = "Delete \"${item.text}\"?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.deleteItem(item) },
            onDismiss = { itemToDelete = null },
        )
    }

    if (showClearAllDialog) {
        ConfirmationDialog(
            title = "Clear All Menu Replies",
            message = "This will permanently delete all menu replies and their items. This action cannot be undone.",
            confirmLabel = "Clear All",
            isDestructive = true,
            onConfirm = { viewModel.clearAll() },
            onDismiss = { showClearAllDialog = false },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Menu Reply",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                                    backupLauncher.launch("menu_reply_backup.json")
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
                onClick = onNavigateToAddTrigger,
                containerColor = AccentGreen,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add menu reply")
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        if (repliesWithItems.isEmpty()) {
            EmptyState(
                title = "No menu replies yet",
                description = "Tap + to create your first interactive menu reply",
                icon = Icons.Filled.GridView,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.md),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                items(
                    items = repliesWithItems,
                    key = { it.reply.id },
                ) { replyWithItems ->
                    MenuReplyCard(
                        replyWithItems = replyWithItems,
                        onEditTrigger = { onNavigateToEditTrigger(replyWithItems.reply.id) },
                        onDeleteTrigger = { replyToDelete = replyWithItems.reply },
                        onAddItem = { onNavigateToAddItem(replyWithItems.reply.id, 0) },
                        onEditItem = { item ->
                            onNavigateToEditItem(
                                replyWithItems.reply.id,
                                item.id
                            )
                        },
                        onDeleteItem = { item -> itemToDelete = item },
                        onMoreOptions = { item -> onNavigateToMoreOptions(item.id) },
                        onItemClick = { item ->
                            onNavigateToItemChildren(
                                replyWithItems.reply.id,
                                item.id
                            )
                        },
                    )
                }

                item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
            }
        }
    }
}

// ─── Menu Reply Card ──────────────────────────────────────────────────────────

@Composable
private fun MenuReplyCard(
    replyWithItems: MenuReplyWithItems,
    onEditTrigger: () -> Unit,
    onDeleteTrigger: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (MenuReplyItemEntity) -> Unit,
    onDeleteItem: (MenuReplyItemEntity) -> Unit,
    onMoreOptions: (MenuReplyItemEntity) -> Unit,
    onItemClick: (MenuReplyItemEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Trigger bubble
        TriggerBubble(
            reply = replyWithItems.reply,
            onEdit = onEditTrigger,
            onDelete = onDeleteTrigger,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Items with left tree connector line
        val connectorColor = ConnectorGray
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.xl)
                .drawBehind {
                    // Vertical connector line on the left edge
                    val lineX = -Spacing.md.toPx()
                    val stopY = size.height - 52.dp.toPx() // stop before ADD LIST button
                    drawLine(
                        color = connectorColor,
                        start = Offset(lineX, 0f),
                        end = Offset(lineX, stopY.coerceAtLeast(0f)),
                        strokeWidth = 2.dp.toPx(),
                    )
                },
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            replyWithItems.items.forEachIndexed { index, itemWithChildren ->
                MenuItemCard(
                    item = itemWithChildren.item,
                    position = index + 1,
                    hasChildren = itemWithChildren.hasChildren,
                    onItemClick = { onItemClick(itemWithChildren.item) },
                    onEdit = { onEditItem(itemWithChildren.item) },
                    onDelete = { onDeleteItem(itemWithChildren.item) },
                    onMoreOptions = { onMoreOptions(itemWithChildren.item) },
                )
            }

            // ADD LIST button
            OutlinedButton(
                onClick = onAddItem,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TealAccent,
                ),
                modifier = Modifier.padding(top = Spacing.xs),
            ) {
                Text(
                    text = "ADD LIST",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TealAccent,
                )
            }
        }
    }
}

// ─── Trigger Bubble ───────────────────────────────────────────────────────────

@Composable
private fun TriggerBubble(
    reply: MenuReplyEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MenuItemGreen,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                start = Spacing.md,
                top = Spacing.sm,
                bottom = Spacing.sm,
                end = Spacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = reply.triggerMessage,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Trigger options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onEdit() },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                    )
                }
            }
        }
    }
}

// ─── Menu Item Card ───────────────────────────────────────────────────────────

@Composable
private fun MenuItemCard(
    item: MenuReplyItemEntity,
    position: Int,
    hasChildren: Boolean,
    onItemClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MenuItemGreen,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                start = Spacing.md,
                top = Spacing.sm,
                bottom = Spacing.sm,
                end = Spacing.xs
            ),
            verticalAlignment = Alignment.Top,
        ) {
            // Tapping the content area (text + badge) navigates into sub-menu
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onItemClick),
            ) {
                Text(
                    text = "$position. ${item.text}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasChildren) "SUBMENU ›" else "REPLY ›",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TealAccent,
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Item options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onEdit() },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("More options") },
                        onClick = { showMenu = false; onMoreOptions() },
                    )
                }
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun MenuReplyScreenPreview() {
    AutoReplyTheme {
        MenuReplyScreen()
    }
}
