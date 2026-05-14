package com.psspl.autoreply.ui.screens.menu

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.screens.menu.components.FeatureItem
import com.psspl.autoreply.ui.screens.menu.components.FeatureMenuCard
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun MenuScreen(
    onNavigateToSupportedApps: () -> Unit = {},
    onNavigateToKeywordReply: () -> Unit = {},
    onNavigateToMenuReply: () -> Unit = {},
    onNavigateToWelcomeMessage: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToSpreadsheet: () -> Unit = {},
    onNavigateToAiReply: () -> Unit = {},
    onNavigateToServerReply: () -> Unit = {},
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val features = remember {
        listOf(
            FeatureItem(
                icon = Icons.Filled.PhoneAndroid,
                title = "Supported Apps",
                description = "Manage apps",
            ),
            FeatureItem(
                icon = Icons.Filled.Edit,
                title = "Keyword Reply",
                description = "Trigger by keyword",
            ),
            FeatureItem(
                icon = Icons.Filled.GridView,
                title = "Menu Reply",
                description = "Interactive menus",
            ),
            FeatureItem(
                icon = Icons.Filled.TableChart,
                title = "Spreadsheet",
                description = "Sheet integration",
            ),
            FeatureItem(
                icon = Icons.Filled.AutoAwesome,
                title = "AI Reply",
                description = "Smart responses",
            ),
            FeatureItem(
                icon = Icons.Filled.EmojiEmotions,
                title = "Welcome Message",
                description = "Greet new contacts",
            ),
            FeatureItem(
                icon = Icons.Filled.Cloud,
                title = "Server",
                description = "Backend connection",
            ),
            FeatureItem(
                icon = Icons.Filled.PlayArrow,
                title = "Test Reply",
                description = "Preview rules",
            ),
            FeatureItem(
                icon = Icons.AutoMirrored.Filled.Note,
                title = "Notes",
                description = "Internal notes",
            ),
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "Features")
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(features) { feature ->
                FeatureMenuCard(
                    item = feature,
                    onClick = {
                        when (feature.title) {
                            "Supported Apps" -> onNavigateToSupportedApps()
                            "Keyword Reply" -> onNavigateToKeywordReply()
                            "Menu Reply" -> onNavigateToMenuReply()
                            "Welcome Message" -> onNavigateToWelcomeMessage()
                            "Notes" -> onNavigateToNotes()
                            "Spreadsheet" -> onNavigateToSpreadsheet()
                            "AI Reply" -> onNavigateToAiReply()
                            "Server" -> onNavigateToServerReply()
                            else -> Toast.makeText(
                                context,
                                "${feature.title} — coming soon",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    AutoReplyTheme {
        MenuScreen()
    }
}
