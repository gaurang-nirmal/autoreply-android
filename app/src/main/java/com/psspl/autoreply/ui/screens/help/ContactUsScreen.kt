package com.psspl.autoreply.ui.screens.help

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.GreenContainer
import com.psspl.autoreply.ui.theme.GreenPrimary
import com.psspl.autoreply.ui.theme.Spacing

private const val MAX_ATTACHMENT_BYTES = 5 * 1024 * 1024 // 5 MB

@Composable
fun ContactUsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HelpViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val contactState by viewModel.contactState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var attachmentBase64 by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val stream = context.contentResolver.openInputStream(uri)
                ?: return@rememberLauncherForActivityResult
            val bytes = stream.readBytes()
            stream.close()
            if (bytes.size > MAX_ATTACHMENT_BYTES) {
                // silently discard oversized file — handled below via snackbar
                return@rememberLauncherForActivityResult
            }
            attachmentBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            attachmentName = uri.lastPathSegment ?: "attachment"
            attachmentUri = uri
        } catch (_: Exception) { /* ignore */
        }
    }

    LaunchedEffect(contactState) {
        when (val s = contactState) {
            is ContactUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetContactState()
            }

            else -> {}
        }
    }

    val isSuccess = contactState is ContactUiState.Success
    val isLoading = contactState is ContactUiState.Loading

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Contact Us",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
        ) {
            if (isSuccess) {
                // ── Success state ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(GreenContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Text(
                        text = "Message Sent!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val ticketId = (contactState as ContactUiState.Success).ticketId
                    Text(
                        text = if (ticketId != null) "Your ticket ID is #$ticketId.\nWe'll get back to you via email."
                        else "Our support team will get back to you shortly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Button(
                        onClick = {
                            viewModel.resetContactState()
                            subject = ""
                            message = ""
                            attachmentUri = null
                            attachmentBase64 = null
                            attachmentName = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    ) {
                        Text("Send another message")
                    }
                }
            } else {
                // ── Form ──────────────────────────────────────────────────────
                HeaderSection()
                Spacer(Modifier.height(Spacing.lg))

                // Email (read-only, from session)
                Text(
                    text = "FROM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    modifier = Modifier.padding(bottom = 4.dp),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(
                        1.5f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    ),
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Email,
                            null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = currentUser?.email ?: "Not signed in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // Subject
                FieldLabel("SUBJECT")
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = { Text("Brief description of your issue") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedColors(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.md))

                // Message
                FieldLabel("MESSAGE")
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Describe your issue in detail…") },
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedColors(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.md))

                // Attachment
                FieldLabel("ATTACHMENT (OPTIONAL)")
                if (attachmentUri != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GreenPrimary.copy(alpha = 0.08f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                GreenPrimary.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.AttachFile,
                                null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = attachmentName ?: "attachment",
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = {
                                    attachmentUri = null; attachmentBase64 = null; attachmentName =
                                    null
                                },
                                modifier = Modifier.size(20.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { filePicker.launch("*/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GreenPrimary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.AttachFile, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Attach a file (max 5 MB)")
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                // Submit
                Button(
                    onClick = {
                        viewModel.submitContact(subject, message, attachmentBase64, attachmentName)
                    },
                    enabled = subject.isNotBlank() && message.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Send Message", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(GreenContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.SupportAgent,
                null,
                tint = GreenPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column {
            Text(
                text = "Get in touch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "We typically respond within 24 hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = GreenPrimary,
        modifier = Modifier.padding(bottom = 4.dp),
        letterSpacing = androidx.compose.ui.unit.TextUnit(
            1.5f,
            androidx.compose.ui.unit.TextUnitType.Sp
        ),
    )
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GreenPrimary,
    cursorColor = GreenPrimary,
    focusedLabelColor = GreenPrimary,
)

@Preview(showBackground = true)
@Composable
private fun ContactUsPreview() {
    AutoReplyTheme { ContactUsScreen(onBack = {}) }
}
