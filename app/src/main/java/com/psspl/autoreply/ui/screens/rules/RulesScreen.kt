package com.psspl.autoreply.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.theme.AutoReplyTheme

@Composable
fun RulesScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { AppTopBar(title = "Rules") },
        modifier = modifier,
    ) { innerPadding ->
        EmptyState(
            title = "No rules yet",
            description = "Add your first auto-reply rule to get started",
            icon = Icons.Filled.Edit,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RulesScreenPreview() {
    AutoReplyTheme {
        RulesScreen()
    }
}
