package com.psspl.autoreply.ui.screens.notes

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.NoteEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.ConfirmationDialog
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val AccentGreen = Color(0xFF25D366)

/** 7 light pastel colours cycled by note.id so each card has a stable, distinct hue. */
private val NoteCardColors = listOf(
    Color(0xFFFFF9C4), // soft yellow
    Color(0xFFD7F5D0), // soft mint green
    Color(0xFFD4EEFF), // soft sky blue
    Color(0xFFFEE0E0), // soft rose
    Color(0xFFEDE7F6), // soft lavender
    Color(0xFFFFE0CC), // soft peach
    Color(0xFFE0F7FA), // soft cyan
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToAddNote: () -> Unit = {},
    onNavigateToEditNote: (Int) -> Unit = {},
    viewModel: NoteViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // System back exits selection mode instead of popping the back stack
    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Notes",
            message = "Delete ${selectedIds.size} selected note(s)? This cannot be undone.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSelected()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // ── Selection mode top bar ─────────────────────────────────────
                AppTopBar(
                    title = "${selectedIds.size} selected",
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear selection",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
            } else {
                // ── Normal top bar ─────────────────────────────────────────────
                AppTopBar(
                    title = "Notes",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onNavigateToAddNote,
                    containerColor = AccentGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add note")
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        if (notes.isEmpty()) {
            EmptyState(
                title = "No notes yet",
                description = "Tap + to create your first note",
                icon = Icons.Filled.EditNote,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = Spacing.sm,
                    end = Spacing.sm,
                    top = Spacing.sm + innerPadding.calculateTopPadding(),
                    bottom = Spacing.sm + innerPadding.calculateBottomPadding(),
                ),
                verticalItemSpacing = Spacing.sm,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = note.id in selectedIds,
                        isSelectionMode = isSelectionMode,
                        onTap = {
                            if (isSelectionMode) {
                                viewModel.toggleSelection(note.id)
                            } else {
                                onNavigateToEditNote(note.id)
                            }
                        },
                        onLongPress = {
                            if (!isSelectionMode) {
                                viewModel.enterSelectionMode(note.id)
                            } else {
                                viewModel.toggleSelection(note.id)
                            }
                        },
                        onShare = {
                            shareText(context = context, text = note.content)
                        },
                    )
                }
            }
        }
    }
}

// ─── Note Card ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: NoteEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = NoteCardColors[note.id % NoteCardColors.size]

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = cardColor,
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress,
            ),
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            // Note content preview
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Footer: timestamp + share / selection indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatRelativeTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                // Fixed-size slot so the row height never shifts between states
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelectionMode) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share note",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L
    val weeks = days / 7L
    val months = days / 30L
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        weeks == 1L -> "1 week ago"
        weeks < 5 -> "$weeks weeks ago"
        months == 1L -> "1 month ago"
        else -> "$months months ago"
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    AutoReplyTheme {
        NotesScreen()
    }
}
