package com.psspl.autoreply.ui.screens.replynotifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.psspl.autoreply.ui.components.PrimaryButton
import com.psspl.autoreply.ui.components.SecondaryButton
import com.psspl.autoreply.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyNotificationFilterSheet(
    filter: ReplyNotificationFilter,
    appOptions: List<ReplyNotificationAppOption>,
    filteredCount: Int,
    totalCount: Int,
    onDateFilterChange: (ReplyNotificationDateFilter) -> Unit,
    onAppFilterChange: (String?) -> Unit,
    onContactQueryChange: (String) -> Unit,
    onMessageQueryChange: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            FilterSheetHeader(
                filteredCount = filteredCount,
                totalCount = totalCount,
                onDismiss = onDismiss,
            )

            DateFilterSection(
                selected = filter.dateFilter,
                onSelected = onDateFilterChange,
            )

            AppFilterSection(
                selectedPackage = filter.appPackage,
                appOptions = appOptions,
                onSelected = onAppFilterChange,
            )

            QueryFilterSection(
                title = "Contact",
                value = filter.contactQuery,
                placeholder = "Contact name",
                onValueChange = onContactQueryChange,
            )

            QueryFilterSection(
                title = "Message",
                value = filter.messageQuery,
                placeholder = "Message text",
                onValueChange = onMessageQueryChange,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                SecondaryButton(
                    text = "Reset",
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = "Apply",
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FilterSheetHeader(
    filteredCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Filter Notifications",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$filteredCount of $totalCount replies",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close filters",
            )
        }
    }
}

@Composable
private fun DateFilterSection(
    selected: ReplyNotificationDateFilter,
    onSelected: (ReplyNotificationDateFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterSection(title = "Date", modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ReplyNotificationDateFilter.entries.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    rowOptions.forEach { option ->
                        FilterChip(
                            selected = option == selected,
                            onClick = { onSelected(option) },
                            label = { Text(option.label) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppFilterSection(
    selectedPackage: String?,
    appOptions: List<ReplyNotificationAppOption>,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = appOptions.firstOrNull { it.appPackage == selectedPackage }?.displayName
        ?: "Show All"

    FilterSection(title = "Messaging App", modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("App") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                    )
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Show All") },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    },
                )
                appOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            onSelected(option.appPackage)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun QueryFilterSection(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterSection(title = title, modifier = modifier) {
        if (value.isBlank()) {
            AssistChip(
                onClick = {},
                label = { Text("Show All") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Custom $title") },
            placeholder = { Text(placeholder) },
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}
