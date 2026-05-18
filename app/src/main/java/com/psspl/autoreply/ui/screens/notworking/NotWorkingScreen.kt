package com.psspl.autoreply.ui.screens.notworking

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.GreenPrimary
import com.psspl.autoreply.ui.theme.GreenPrimaryDark
import com.psspl.autoreply.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun NotWorkingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotWorkingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Measured height of the overlay bottom panel (options / restart)
    var bottomPanelHeightPx by remember { mutableIntStateOf(0) }
    val bottomPanelHeightDp = with(density) { bottomPanelHeightPx.toDp() }

    // Fix 1 — collision-free trigger: entries.size*2 + isTyping avoids the case where
    // "isTyping=true, N entries" and "isTyping=false, N+1 entries" both equal N+1.
    val scrollTrigger = state.entries.size * 2 + if (state.isTyping) 1 else 0
    LaunchedEffect(scrollTrigger) {
        if (state.entries.isEmpty() && !state.isTyping) return@LaunchedEffect
        val lastIndex = (state.entries.size - 1 + if (state.isTyping) 1 else 0)
            .coerceAtLeast(0)
        // scrollOffset=Int.MAX_VALUE/2 scrolls the item as far down as the list allows,
        // so the last message ends up at the BOTTOM of the viewport, not the top.
        listState.scrollToItem(lastIndex, scrollOffset = Int.MAX_VALUE / 2)
    }

    // Fix 2 — compensate for the options panel growing over the list.
    // As the overlay panel animates in (height 0 → ~180px), we scroll the list
    // down by the same delta each frame so the last message stays above it.
    LaunchedEffect(Unit) {
        var prevHeight = 0
        snapshotFlow { bottomPanelHeightPx }
            .distinctUntilChanged()
            .collect { currentHeight ->
                val delta = currentHeight - prevHeight
                if (delta > 0) listState.scrollBy(delta.toFloat())
                prevHeight = currentHeight
            }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Not Working?",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            // ── Bot identity header ───────────────────────────────────────────
            BotIdentityHeader()

            // ── Chat + overlay panel in a Box so the list never resizes ───────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                // Chat messages — fills the whole Box, bottom padding = panel height
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = Spacing.md,
                        end = Spacing.md,
                        top = Spacing.sm,
                        // Dynamic: always tall enough to scroll last item above the panel
                        bottom = bottomPanelHeightDp + Spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(
                        items = state.entries,
                        key = { entry ->
                            when (entry) {
                                is ChatEntry.BotText -> "bot_text_${entry.id}"
                                is ChatEntry.BotVisual -> "bot_visual_${entry.id}"
                                is ChatEntry.UserChoice -> "user_${entry.id}"
                            }
                        },
                    ) { entry ->
                        // animateItem() on each bubble handles entrance animation —
                        // no AnimatedVisibility needed (it requires ColumnScope).
                        when (entry) {
                            is ChatEntry.BotText -> BotBubble(
                                modifier = Modifier.animateItem(),
                            ) { BotTextContent(entry.text) }

                            is ChatEntry.BotVisual -> BotBubble(
                                modifier = Modifier.animateItem(),
                            ) { FakeNotificationCard() }

                            is ChatEntry.UserChoice -> UserChoiceBubble(
                                label = entry.label,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    if (state.isTyping) {
                        item(key = "typing") {
                            BotBubble(modifier = Modifier.animateItem()) { TypingIndicator() }
                        }
                    }
                }

                // Options / restart panel — overlaid at the bottom of the Box.
                // onSizeChanged keeps bottomPanelHeightDp in sync so the LazyColumn
                // contentPadding always clears the panel height precisely.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onSizeChanged { bottomPanelHeightPx = it.height },
                ) {
                    AnimatedVisibility(
                        visible = state.currentOptions.isNotEmpty(),
                        enter = expandVertically(tween(280)) + fadeIn(tween(280)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(200)),
                    ) {
                        OptionsPanel(
                            options = state.currentOptions,
                            onOptionSelected = { option ->
                                if (option.action is OptionAction.OpenNotificationSettings) {
                                    context.startActivity(
                                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                                viewModel.onOptionSelected(option)
                            },
                        )
                    }

                    AnimatedVisibility(
                        visible = state.isFinished,
                        enter = expandVertically(tween(280)) + fadeIn(tween(280)),
                    ) {
                        RestartPanel(onRestart = viewModel::restart)
                    }
                }
            }
        }
    }
}

// ── Bot identity header ───────────────────────────────────────────────────────

@Composable
private fun BotIdentityHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(GreenPrimary, GreenPrimaryDark))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Column {
                Text(
                    text = "AutoReply Assistant",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Online · Troubleshooting guide",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Bot bubble wrapper ────────────────────────────────────────────────────────

@Composable
private fun BotBubble(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Small avatar dot beside each bubble
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(GreenPrimary, GreenPrimaryDark))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp, topEnd = 16.dp,
                bottomStart = 16.dp, bottomEnd = 16.dp,
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                content()
            }
        }
    }
}

// ── Bot text content ──────────────────────────────────────────────────────────

@Composable
private fun BotTextContent(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp,
    )
}

// ── User choice bubble ────────────────────────────────────────────────────────

@Composable
private fun UserChoiceBubble(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 4.dp,
                        bottomStart = 16.dp, bottomEnd = 16.dp,
                    )
                )
                .background(GreenPrimary)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        }
    }
}

// ── Typing indicator ──────────────────────────────────────────────────────────

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")

    @Composable
    fun dot(delayMs: Int): Float {
        val alpha by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 900
                    0.3f at delayMs using LinearEasing
                    1f at delayMs + 300 using LinearEasing
                    0.3f at delayMs + 600 using LinearEasing
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "dot_$delayMs",
        )
        return alpha
    }

    val a1 = dot(0)
    val a2 = dot(150)
    val a3 = dot(300)

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        listOf(a1, a2, a3).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = alpha)),
            )
        }
    }
}

// ── Fake notification card (visual inside bubble) ─────────────────────────────

@Composable
private fun FakeNotificationCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // App row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Messaging App  •  now",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "John Doe",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Hi, how are you? 👋",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotifActionChip(
                    icon = { Icon(Icons.AutoMirrored.Filled.Reply, null, Modifier.size(14.dp)) },
                    label = "REPLY",
                    highlight = true,
                )
                NotifActionChip(
                    icon = { Icon(Icons.Filled.MarkEmailRead, null, Modifier.size(14.dp)) },
                    label = "MARK AS READ",
                    highlight = false,
                )
            }
        }
    }
}

@Composable
private fun NotifActionChip(
    icon: @Composable () -> Unit,
    label: String,
    highlight: Boolean,
) {
    val bg = if (highlight) GreenPrimary.copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (highlight) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(
                1.dp,
                if (highlight) GreenPrimary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(modifier = Modifier.size(14.dp)) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor,
            ) { icon() }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            letterSpacing = 0.3.sp,
        )
    }
}

// ── Options panel ─────────────────────────────────────────────────────────────

@Composable
private fun OptionsPanel(
    options: List<BotOption>,
    onOptionSelected: (BotOption) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = "CHOOSE AN ANSWER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = androidx.compose.ui.unit.TextUnit(
                    1.5f, androidx.compose.ui.unit.TextUnitType.Sp,
                ),
                modifier = Modifier.padding(bottom = 2.dp),
            )
            options.forEach { option ->
                OutlinedButton(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, GreenPrimary.copy(alpha = 0.5f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GreenPrimary,
                    ),
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

// ── Restart panel ─────────────────────────────────────────────────────────────

@Composable
private fun RestartPanel(onRestart: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = "Still not working?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = "Restart Troubleshooter",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun NotWorkingScreenPreview() {
    AutoReplyTheme { NotWorkingScreen(onBack = {}) }
}
