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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.LoadingIndicator
import com.psspl.autoreply.ui.screens.statistics.StatisticsDetailViewModel.Companion.toDateLabel
import com.psspl.autoreply.ui.screens.statistics.StatisticsDetailViewModel.Companion.toTimeLabel
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing
import com.psspl.autoreply.utils.ShareManager

private val TealBubble = Color(0xFFDCF8EA)
private val TealBubbleDark = Color(0xFF1A3D32)

@Composable
fun StatisticsDetailScreen(
    replyText: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(replyText) {
        viewModel.setReplyText(replyText)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = replyText.replace("\n", " ").take(40).let {
                    if (replyText.length > 40) "$it…" else it
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (!uiState.isLoading && uiState.sends.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                ShareManager.shareStatisticsCsv(context, viewModel.exportCsv())
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = "Export",
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

            uiState.sends.isEmpty() -> EmptyState(
                title = "No sends recorded",
                description = "No contacts have received this reply yet",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> {
                val groupedSends = uiState.sends.groupBy { it.timestamp.toDateLabel() }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    item {
                        ReplyBubble(text = uiState.replyText)
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    groupedSends.forEach { (dateLabel, sends) ->
                        item(key = "header_$dateLabel") {
                            DateGroupHeader(label = dateLabel)
                        }
                        items(items = sends, key = { it.id }) { entity ->
                            SendRow(entity = entity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bubbleColor = if (isDark) TealBubbleDark else TealBubble

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 4.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp,
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFFD1F7E7) else Color(0xFF0D3D26),
            )
        }
    }
}

@Composable
private fun DateGroupHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Divider(modifier = Modifier.weight(1f))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Divider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SendRow(
    entity: ReplyNotificationEntity,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        ContactAvatar(name = entity.senderName)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entity.senderName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = StatisticsDetailViewModel.appDisplayName(entity.appPackage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = entity.timestamp.toTimeLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AppDot(packageName = entity.appPackage)
    }
}

@Composable
private fun ContactAvatar(
    name: String,
    modifier: Modifier = Modifier,
) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppDot(
    packageName: String,
    modifier: Modifier = Modifier,
) {
    val (abbr, color) = appDotInfo(packageName)
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = abbr,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = androidx.compose.ui.unit.TextUnit(
                    8f,
                    androidx.compose.ui.unit.TextUnitType.Sp,
                ),
            ),
            color = color,
        )
    }
}

private fun appDotInfo(packageName: String): Pair<String, Color> = when (packageName) {
    "com.whatsapp" -> "WA" to Color(0xFF25D366)
    "com.whatsapp.w4b" -> "WB" to Color(0xFF00A884)
    "org.telegram.messenger" -> "TG" to Color(0xFF2AABEE)
    "com.facebook.orca" -> "MS" to Color(0xFF0084FF)
    "com.facebook.mlite" -> "ML" to Color(0xFF5B9BD5)
    "com.instagram.android" -> "IG" to Color(0xFFE1306C)
    "com.twitter.android" -> "TW" to Color(0xFF1DA1F2)
    "com.linkedin.android" -> "LI" to Color(0xFF0077B5)
    "org.thoughtcrime.securesms" -> "SG" to Color(0xFF2090EA)
    "com.facebook.pages.app" -> "MB" to Color(0xFF1877F2)
    "com.viber.voip" -> "VB" to Color(0xFF7360F2)
    else -> packageName.take(2).uppercase() to Color(0xFF128C7E)
}

private fun Color.luminance(): Float {
    val r = red * 0.2126f
    val g = green * 0.7152f
    val b = blue * 0.0722f
    return r + g + b
}

@Preview(showBackground = true)
@Composable
private fun StatisticsDetailScreenPreview() {
    AutoReplyTheme {
        StatisticsDetailScreen(replyText = "Hello! How can I help you?", onBack = {})
    }
}
