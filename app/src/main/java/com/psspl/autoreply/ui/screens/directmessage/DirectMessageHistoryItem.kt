package com.psspl.autoreply.ui.screens.directmessage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.psspl.autoreply.database.entity.DirectMessageEntity
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())

@Composable
fun DirectMessageHistoryItem(
    entity: DirectMessageEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appLabel = WhatsAppTarget.entries
        .firstOrNull { it.packageName == entity.appPackage }
        ?.displayName ?: entity.appPackage

    AppCard(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = appLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (entity.message.isNotBlank()) {
                    Text(
                        text = entity.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = timeFormat.format(Date(entity.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DirectMessageHistoryItemPreview() {
    AutoReplyTheme {
        DirectMessageHistoryItem(
            entity = DirectMessageEntity(
                phoneNumber = "+919876543210",
                message = "Hello, I need help with my order",
                appPackage = "com.whatsapp",
            ),
            onClick = {},
        )
    }
}
