package com.psspl.autoreply.ui.screens.menureply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import kotlinx.coroutines.flow.first

private val AccentGreen = Color(0xFF25D366)

/**
 * Add / Edit screen for both triggers and menu items.
 *
 * Route params:
 *  - [menuReplyId] = 0  →  creating a new trigger
 *  - [menuReplyId] > 0, [itemId] = 0, [parentItemId] = -1  →  editing existing trigger
 *  - [menuReplyId] > 0, [itemId] = 0, [parentItemId] >= 0  →  adding a new item
 *  - [menuReplyId] > 0, [itemId] > 0                       →  editing existing item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMenuReplyScreen(
    menuReplyId: Int = 0,
    itemId: Int = 0,
    parentItemId: Int = -1,       // -1 = trigger form; 0 = top-level item; > 0 = nested item
    onBack: () -> Unit = {},
    viewModel: MenuReplyViewModel = hiltViewModel(),
) {
    // ── Classify the mode ─────────────────────────────────────────────────────
    val isNewTrigger = menuReplyId == 0
    val isEditingTrigger = menuReplyId > 0 && itemId == 0 && parentItemId == -1
    val isAddingItem = menuReplyId > 0 && itemId == 0 && parentItemId >= 0
    val isEditingItem = menuReplyId > 0 && itemId > 0

    val screenTitle = when {
        isNewTrigger -> "Add Menu Reply"
        isEditingTrigger -> "Edit Trigger"
        isAddingItem -> "Add Menu Option"
        else -> "Edit Menu Option"
    }
    val fieldLabel = if (isNewTrigger || isEditingTrigger) "Trigger Message" else "Menu Option Text"
    val fieldHint = if (isNewTrigger || isEditingTrigger)
        "e.g. Hi, Hello, Support" else "e.g. 1. Billing, 2. Technical"

    // ── Local form state ──────────────────────────────────────────────────────
    var text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pre-populate when editing
    LaunchedEffect(menuReplyId, itemId) {
        when {
            isEditingTrigger -> {
                viewModel.getReplyById(menuReplyId).first()?.let { text = it.triggerMessage }
            }

            isEditingItem -> {
                viewModel.getItemById(itemId).first()?.let { text = it.text }
            }
        }
    }

    // ── Save handler ──────────────────────────────────────────────────────────
    fun onSave() {
        if (text.isBlank()) {
            errorMessage = "This field cannot be empty"
            return
        }
        when {
            isNewTrigger -> viewModel.addReply(text)
            isEditingTrigger -> viewModel.editTrigger(menuReplyId, text)
            isAddingItem -> viewModel.addItem(
                menuReplyId = menuReplyId,
                parentItemId = if (parentItemId > 0) parentItemId else null,
                text = text,
            )

            isEditingItem -> viewModel.editItem(itemId, text)
        }
        onBack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = screenTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(Spacing.md),
        ) {
            Text(
                text = fieldLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(fieldHint) },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { msg ->
                    { Text(msg, color = MaterialTheme.colorScheme.error) }
                },
                shape = RoundedCornerShape(8.dp),
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = ::onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.padding(start = Spacing.xs))
                Text(
                    text = if (isNewTrigger || isAddingItem) "Add" else "Update",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditMenuReplyScreenPreview() {
    AutoReplyTheme {
        AddEditMenuReplyScreen()
    }
}
