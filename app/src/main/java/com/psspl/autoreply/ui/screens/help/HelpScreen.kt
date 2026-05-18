package com.psspl.autoreply.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.GreenPrimary
import com.psspl.autoreply.ui.theme.Spacing

private const val FAQ_URL = "https://autoreplypro.app/faq"
private const val PRIVACY_URL = "https://autoreplypro.app/privacy"

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onNavigateToNotWorking: () -> Unit = {},
    onNavigateToContactUs: () -> Unit = {},
    onNavigateToAppInfo: () -> Unit = {},
    onNavigateToLicenses: () -> Unit = {},
    onNavigateToDeleteAccount: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Help",
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {

            // ── Help ──────────────────────────────────────────────────────────
            SectionHeader(icon = Icons.AutoMirrored.Filled.HelpOutline, label = "Help")

            HelpCard {
                HelpRow(
                    icon = Icons.Filled.QuestionAnswer,
                    title = "FAQ",
                    subtitle = "Browse frequently asked questions",
                    onClick = { openUrl(context, FAQ_URL) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                HelpRow(
                    icon = Icons.Filled.Warning,
                    title = "Not Working?",
                    subtitle = "Run the auto-reply troubleshooter",
                    onClick = onNavigateToNotWorking,
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // ── Contact ───────────────────────────────────────────────────────
            SectionHeader(icon = Icons.Filled.SupportAgent, label = "Contact")

            HelpCard {
                HelpRow(
                    icon = Icons.Filled.PersonOutline,
                    title = "Contact Us",
                    subtitle = "Submit a support request to our team",
                    onClick = onNavigateToContactUs,
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // ── About ─────────────────────────────────────────────────────────
            SectionHeader(icon = Icons.Filled.Info, label = "About")

            HelpCard {
                HelpRow(
                    icon = Icons.Filled.Info,
                    title = "App info",
                    subtitle = "Version and build information",
                    onClick = onNavigateToAppInfo,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                HelpRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = { openUrl(context, PRIVACY_URL) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                HelpRow(
                    icon = Icons.Filled.QuestionAnswer,
                    title = "Licenses",
                    subtitle = "Open source software licenses",
                    onClick = onNavigateToLicenses,
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // ── Account ───────────────────────────────────────────────────────
            SectionHeader(icon = Icons.Filled.PersonOutline, label = "Account")

            HelpCard {
                HelpRow(
                    icon = Icons.Filled.DeleteForever,
                    title = "Delete my account",
                    subtitle = "Permanently remove your account and data",
                    titleColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = onNavigateToDeleteAccount,
                )
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier.padding(bottom = Spacing.sm, top = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary,
        )
    }
}

// ── Card wrapper ──────────────────────────────────────────────────────────────

@Composable
private fun HelpCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column { content() }
    }
}

// ── Row item ──────────────────────────────────────────────────────────────────

@Composable
private fun HelpRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = GreenPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp),
        )
    }
}

// ── Utility ───────────────────────────────────────────────────────────────────

private fun openUrl(context: android.content.Context, url: String) {
    val intent = android.content.Intent(
        android.content.Intent.ACTION_VIEW,
        android.net.Uri.parse(url),
    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HelpScreenPreview() {
    AutoReplyTheme { HelpScreen(onBack = {}) }
}
