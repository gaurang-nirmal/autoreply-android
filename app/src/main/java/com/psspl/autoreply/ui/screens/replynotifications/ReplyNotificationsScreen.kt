package com.psspl.autoreply.ui.screens.replynotifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.LoadingIndicator
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import com.psspl.autoreply.utils.ShareManager

@Composable
fun ReplyNotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReplyNotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reply Notifications",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (!uiState.isLoading && uiState.totalCount > 0) {
                        IconButton(onClick = viewModel::showFilters) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter notifications",
                                tint = if (uiState.isFilterActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Unspecified
                                },
                            )
                        }
                        IconButton(
                            onClick = {
                                ShareManager.shareReplyNotificationHistory(
                                    context = context,
                                    text = viewModel.exportText(),
                                )
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = "Export filtered notifications",
                            )
                        }
                        IconButton(onClick = viewModel::clearHistory) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear history",
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator()

            uiState.isEmpty -> EmptyState(
                title = "No replies sent yet",
                description = "Auto-replies will appear here once sent",
                icon = Icons.Filled.Notifications,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            uiState.isFilteredEmpty -> EmptyState(
                title = "No matching replies",
                description = "Adjust or reset filters to see more reply notifications",
                icon = Icons.Filled.FilterList,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(
                    items = uiState.items,
                    key = { item ->
                        when (item) {
                            is ReplyNotificationListItem.Header -> "header_${item.date}"
                            is ReplyNotificationListItem.Entry -> "entry_${item.entity.id}"
                        }
                    },
                ) { item ->
                    when (item) {
                        is ReplyNotificationListItem.Header -> DateHeader(label = item.date)
                        is ReplyNotificationListItem.Entry -> ReplyNotificationItem(entity = item.entity)
                    }
                }
            }
        }
    }

    if (uiState.isFilterSheetVisible) {
        ReplyNotificationFilterSheet(
            filter = uiState.draftFilter,
            appOptions = uiState.appOptions,
            filteredCount = uiState.draftFilteredCount,
            totalCount = uiState.totalCount,
            onDateFilterChange = viewModel::updateDateFilter,
            onAppFilterChange = viewModel::updateAppFilter,
            onContactQueryChange = viewModel::updateContactQuery,
            onMessageQueryChange = viewModel::updateMessageQuery,
            onApply = viewModel::applyFilters,
            onReset = viewModel::resetFilters,
            onDismiss = viewModel::hideFilters,
        )
    }
}

@Composable
private fun DateHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm, bottom = Spacing.xs),
    )
}

@Preview(showBackground = true)
@Composable
private fun ReplyNotificationsScreenPreview() {
    AutoReplyTheme {
        ReplyNotificationsScreen(onBack = {})
    }
}
