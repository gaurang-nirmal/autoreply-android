package com.psspl.autoreply.ui.screens.menureply

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

// ─── Shared colors ────────────────────────────────────────────────────────────
private val MenuItemGreen = Color(0xFFDCF8C6)
private val TealAccent = Color(0xFF128C7E)
private val ConnectorGray = Color(0xFFB0BEC5)
private val AccentGreen = Color(0xFF25D366)

/**
 * Displays the direct children of [parentItemId] in the same tree style as
 * [MenuReplyScreen]. Tapping a child item navigates to this same screen again
 * with that child as the new parent — enabling endless recursion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuReplyItemChildrenScreen(
    menuReplyId: Int = 0,
    parentItemId: Int = 0,
    onBack: () -> Unit = {},
    /** Navigate deeper into the same screen with the tapped item as new parent */
    onNavigateToChildren: (menuReplyId: Int, itemId: Int) -> Unit = { _, _ -> },
    onNavigateToAddItem: (menuReplyId: Int, parentItemId: Int) -> Unit = { _, _ -> },
    onNavigateToEditItem: (menuReplyId: Int, itemId: Int) -> Unit = { _, _ -> },
    onNavigateToMoreOptions: (itemId: Int) -> Unit = {},
    viewModel: MenuReplyViewModel = hiltViewModel(),
) {
    val parentItem by viewModel.getItemById(parentItemId)
        .collectAsStateWithLifecycle(initialValue = null)

    val children by viewModel.getChildrenWithFlags(parentItemId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var itemToDelete by remember { mutableStateOf<MenuReplyItemEntity?>(null) }

    // ── Confirmation dialog ───────────────────────────────────────────────────
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = parentItem?.text ?: "Sub Menu",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddItem(menuReplyId, parentItemId) },
                containerColor = AccentGreen,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add sub-menu option")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            // ── Parent item preview bubble ─────────────────────────────────────
            parentItem?.let { item ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MenuItemGreen,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // ── Children list with tree connector ─────────────────────────────
            val connectorColor = ConnectorGray
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = Spacing.xl)
                    .drawBehind {
                        val lineX = -Spacing.md.toPx()
                        val stopY = size.height - 52.dp.toPx()
                        drawLine(
                            color = connectorColor,
                            start = Offset(lineX, 0f),
                            end = Offset(lineX, stopY.coerceAtLeast(0f)),
                            strokeWidth = 2.dp.toPx(),
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                itemsIndexed(
                    items = children,
                    key = { _, it -> it.item.id },
                ) { index, childWithChildren ->
                    ChildItemCard(
                        item = childWithChildren.item,
                        position = index + 1,
                        hasChildren = childWithChildren.hasChildren,
                        onItemClick = {
                            onNavigateToChildren(menuReplyId, childWithChildren.item.id)
                        },
                        onEdit = { onNavigateToEditItem(menuReplyId, childWithChildren.item.id) },
                        onDelete = { itemToDelete = childWithChildren.item },
                        onMoreOptions = { onNavigateToMoreOptions(childWithChildren.item.id) },
                    )
                }

                // ADD LIST button row
                item {
                    OutlinedButton(
                        onClick = { onNavigateToAddItem(menuReplyId, parentItemId) },
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

                item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
            }
        }
    }
}

// ─── Child Item Card ──────────────────────────────────────────────────────────

@Composable
private fun ChildItemCard(
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
                end = Spacing.xs,
            ),
            verticalAlignment = Alignment.Top,
        ) {
            // Content area — tapping navigates deeper
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

            // ⋮ popup menu — independent of content tap
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
private fun MenuReplyItemChildrenScreenPreview() {
    AutoReplyTheme {
        MenuReplyItemChildrenScreen()
    }
}
