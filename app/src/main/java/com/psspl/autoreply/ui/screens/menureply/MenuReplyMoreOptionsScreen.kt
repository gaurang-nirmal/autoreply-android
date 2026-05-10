package com.psspl.autoreply.ui.screens.menureply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val MenuItemGreen = Color(0xFFDCF8C6)
private val TealAccent = Color(0xFF128C7E)
private val ChatBackground = Color(0xFFECE5DD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuReplyMoreOptionsScreen(
    itemId: Int = 0,
    onBack: () -> Unit = {},
    viewModel: MenuReplyViewModel = hiltViewModel(),
) {
    val item by viewModel.getItemById(itemId).collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "More options",
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
                .padding(innerPadding),
        ) {
            // ── Chat preview area ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(ChatBackground)
                    .padding(Spacing.md),
                contentAlignment = Alignment.CenterEnd,
            ) {
                item?.let { menuItem ->
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 2.dp,
                            bottomEnd = 12.dp,
                            bottomStart = 12.dp,
                        ),
                        color = MenuItemGreen,
                    ) {
                        Text(
                            text = menuItem.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(
                                horizontal = Spacing.md,
                                vertical = Spacing.sm,
                            ),
                        )
                    }
                }
            }

            // ── Stop Reply section header ──────────────────────────────────────
            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Block,
                    contentDescription = null,
                    tint = TealAccent,
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = "Stop Reply",
                    style = MaterialTheme.typography.labelLarge,
                    color = TealAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider()

            // ── Stop Reply toggle row ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stop Reply",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Stop sending a reply to a particular chat if this menu option is chosen. " +
                                "Turn off and on auto-reply to start sending a reply back.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                Switch(
                    checked = item?.stopReply ?: false,
                    onCheckedChange = { checked ->
                        viewModel.setStopReply(itemId, checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = TealAccent,
                        checkedThumbColor = Color.White,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuReplyMoreOptionsScreenPreview() {
    AutoReplyTheme {
        MenuReplyMoreOptionsScreen()
    }
}
