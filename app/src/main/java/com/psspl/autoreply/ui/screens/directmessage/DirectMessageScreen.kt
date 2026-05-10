package com.psspl.autoreply.ui.screens.directmessage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.PrimaryButton
import com.psspl.autoreply.ui.components.TopbarMenu
import com.psspl.autoreply.ui.components.TopbarMenuItem
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun DirectMessageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DirectMessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Direct Message",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TopbarMenu(
                        items = listOf(
                            TopbarMenuItem(
                                label = "Clear history",
                                icon = Icons.Filled.DeleteSweep,
                                onClick = viewModel::clearHistory,
                            ),
                        ),
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                ComposeForm(
                    countryCode = uiState.countryCode,
                    phoneNumber = uiState.phoneNumber,
                    message = uiState.message,
                    selectedApp = uiState.selectedApp,
                    onCountryCodeChange = viewModel::onCountryCodeChange,
                    onPhoneNumberChange = viewModel::onPhoneNumberChange,
                    onMessageChange = viewModel::onMessageChange,
                    onAppSelected = viewModel::onAppSelected,
                    onSend = viewModel::send,
                )
            }

            if (uiState.history.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Spacing.xs),
                    )
                }
                items(items = uiState.history, key = { it.id }) { entity ->
                    DirectMessageHistoryItem(
                        entity = entity,
                        onClick = { viewModel.fillFromHistory(entity) },
                    )
                }
            } else if (!uiState.isLoading) {
                item {
                    EmptyState(
                        title = "No history yet",
                        description = "Messages you send will appear here",
                        icon = Icons.AutoMirrored.Filled.Send,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComposeForm(
    countryCode: String,
    phoneNumber: String,
    message: String,
    selectedApp: WhatsAppTarget,
    onCountryCodeChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onAppSelected: (WhatsAppTarget) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {

            // Country code + phone number row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedTextField(
                    value = countryCode,
                    onValueChange = onCountryCodeChange,
                    label = { Text("Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.width(88.dp),
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                )
            }

            // Message input
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Message (optional)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            // App selector chips
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                WhatsAppTarget.entries.forEach { target ->
                    FilterChip(
                        selected = selectedApp == target,
                        onClick = { onAppSelected(target) },
                        label = {
                            Text(
                                target.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                    )
                }
            }

            PrimaryButton(text = "Open Chat", onClick = onSend)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DirectMessageScreenPreview() {
    AutoReplyTheme {
        DirectMessageScreen(onBack = {})
    }
}
