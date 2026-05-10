package com.psspl.autoreply.ui.screens.automaticon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState

@Composable
fun AutomaticOnScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Automatic On",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { _ ->
        EmptyState(
            title = "Automatic On",
            description = "Schedule when auto-reply activates and deactivates automatically",
            icon = Icons.Filled.FlashOn,
        )
    }
}
