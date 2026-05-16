package com.psspl.autoreply.ui.screens.statistics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppCard
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.LoadingIndicator
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import com.psspl.autoreply.utils.ShareManager

private data class KnownApp(
    val packageName: String,
    val label: String,
    val abbr: String,
    val color: Color,
)

private val knownApps = listOf(
    KnownApp("com.whatsapp", "WhatsApp", "WA", Color(0xFF25D366)),
    KnownApp("com.whatsapp.w4b", "WA Biz", "WB", Color(0xFF00A884)),
    KnownApp("org.telegram.messenger", "Telegram", "TG", Color(0xFF2AABEE)),
    KnownApp("com.facebook.orca", "Messenger", "MS", Color(0xFF0084FF)),
    KnownApp("com.facebook.mlite", "Msngr Lite", "ML", Color(0xFF5B9BD5)),
    KnownApp("com.instagram.android", "Instagram", "IG", Color(0xFFE1306C)),
    KnownApp("com.twitter.android", "Twitter", "TW", Color(0xFF1DA1F2)),
    KnownApp("com.linkedin.android", "LinkedIn", "LI", Color(0xFF0077B5)),
    KnownApp("org.thoughtcrime.securesms", "Signal", "SG", Color(0xFF2090EA)),
    KnownApp("com.facebook.pages.app", "Meta Biz", "MB", Color(0xFF1877F2)),
    KnownApp("com.viber.voip", "Viber", "VB", Color(0xFF7360F2)),
)

@Composable
fun StatisticsScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Statistics",
                actions = {
                    if (!uiState.isLoading && uiState.totalCount > 0) {
                        IconButton(
                            onClick = {
                                ShareManager.shareStatisticsCsv(context, viewModel.exportCsv())
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = "Export statistics",
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

            uiState.totalCount == 0 -> EmptyState(
                title = "No replies sent yet",
                description = "Statistics will appear here once auto-replies are sent",
                icon = Icons.Filled.BarChart,
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
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item {
                    SummaryCard(
                        totalCount = uiState.totalCount,
                        appCounts = uiState.appCounts,
                    )
                }

                item {
                    SectionHeader(
                        title = "Reply messages",
                        onDownload = {
                            ShareManager.shareStatisticsCsv(context, viewModel.exportCsv())
                        },
                    )
                }

                items(items = uiState.messageStats, key = { it.replyText }) { stat ->
                    MessageStatCard(
                        stat = stat,
                        onClick = { onNavigateToDetail(stat.replyText) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalCount: Int,
    appCounts: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Auto reply messages sent",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = totalCount.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(items = knownApps, key = { it.packageName }) { app ->
                AppCountChip(
                    app = app,
                    count = appCounts[app.packageName] ?: 0,
                )
            }
        }
    }
}

@Composable
private fun AppCountChip(
    app: KnownApp,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(app.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = app.abbr,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = app.color,
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onDownload) {
            Icon(
                imageVector = Icons.Filled.FileDownload,
                contentDescription = "Download messages",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageStatCard(
    stat: MessageStat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = stat.replyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            CountBadge(icon = "↩", count = stat.sendCount, label = "replies")
            CountBadge(icon = "👤", count = stat.contactCount, label = "contacts")
        }
    }
}

@Composable
private fun CountBadge(
    icon: String,
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenPreview() {
    AutoReplyTheme {
        StatisticsScreen(onNavigateToDetail = {})
    }
}
