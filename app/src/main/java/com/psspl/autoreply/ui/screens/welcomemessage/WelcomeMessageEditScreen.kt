package com.psspl.autoreply.ui.screens.welcomemessage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeMessageEditScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: WelcomeMessageViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()

    // Initialise once from DB; edits are local until SAVE
    var text by rememberSaveable(config.message) { mutableStateOf(config.message) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Welcome Message",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.setMessage(text)
                            onBack()
                        },
                        enabled = text.isNotBlank(),
                    ) {
                        Text(
                            text = "SAVE",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Welcome message") },
            placeholder = { Text("Enter your welcome message…") },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.md),
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun WelcomeMessageEditScreenPreview() {
    AutoReplyTheme {
        WelcomeMessageEditScreen()
    }
}
