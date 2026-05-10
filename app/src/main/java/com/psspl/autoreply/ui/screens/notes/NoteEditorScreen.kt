package com.psspl.autoreply.ui.screens.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import kotlinx.coroutines.launch

private val TealAccent = Color(0xFF128C7E)

@Composable
fun NoteEditorScreen(
    noteId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: NoteViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Persisted note ID — starts as noteId; updated to the Room-generated ID
    // after the first save of a new note so we update instead of re-inserting.
    var actualNoteId by rememberSaveable(noteId) { mutableIntStateOf(noteId) }

    // New notes open in edit mode; existing notes open in view (read-only) mode.
    var isEditing by rememberSaveable(noteId) { mutableStateOf(noteId == 0) }

    // Draft content
    var content by rememberSaveable(noteId) { mutableStateOf("") }
    var initialized by remember { mutableStateOf(noteId == 0) }

    // Load existing note content once the list is available
    LaunchedEffect(actualNoteId, notes) {
        if (!initialized && actualNoteId > 0) {
            notes.firstOrNull { it.id == actualNoteId }?.let { note ->
                content = note.content
                initialized = true
            }
        }
    }

    // Request focus whenever edit mode activates
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isEditing) {
        if (isEditing) runCatching { focusRequester.requestFocus() }
    }

    // Single back handler covering both modes:
    //  • Edit mode  → auto-save + switch to view mode (stay on screen)
    //  • View mode  → navigate back to the list
    BackHandler {
        if (isEditing) {
            scope.launch {
                actualNoteId = viewModel.saveNoteReturningId(actualNoteId, content)
            }
            isEditing = false
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
    ) {

        // ── Heading + edit icon row ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
        ) {
            Text(
                text = when {
                    !isEditing -> "Note"
                    noteId == 0 -> "Add a note"
                    else -> "Edit note"
                },
                color = TealAccent,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 40.dp), // prevent text running under the icon
            )

            // Edit icon — visible only in read-only (view) mode
            if (!isEditing) {
                IconButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit note",
                        tint = TealAccent,
                    )
                }
            }
        }

        // ── Content area ─────────────────────────────────────────────────────
        if (isEditing) {
            // Edit mode: editable field
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                ),
                cursorBrush = SolidColor(TealAccent),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp, bottom = 80.dp)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
                            Text(
                                text = "Type your note here…",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                ),
                            )
                        }
                        innerTextField()
                    }
                },
            )

            // Save FAB — visible only in edit mode
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        actualNoteId = viewModel.saveNoteReturningId(actualNoteId, content)
                    }
                    isEditing = false
                },
                containerColor = TealAccent,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Save note",
                )
            }
        } else {
            // View mode: read-only text; double-tap to re-enter edit mode
            Text(
                text = content,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { isEditing = true })
                    },
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun NoteEditorScreenPreview() {
    AutoReplyTheme {
        NoteEditorScreen(noteId = 0)
    }
}
