package com.psspl.autoreply.ui.screens.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.ui.screens.login.components.GoogleSignInButton
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private const val PLAY_SERVICES_PKG = "com.google.android.gms"
private const val PLAY_STORE_PKG = "com.android.vending"

/**
 * Full-screen sign-in screen.
 *
 * Navigation after a successful sign-in is handled automatically by [AuthViewModel]
 * in [MainActivity] — as soon as Firebase reports a new authenticated user,
 * [AuthState] transitions to [AuthState.Authenticated] and this screen is replaced
 * by the main app shell.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LoginScreenContent(
        uiState = uiState,
        onSignIn = { viewModel.signIn(context) },
        onOpenPlayStore = {
            // Open the Play Store listing for Google Play Services.
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$PLAY_SERVICES_PKG")
                setPackage(PLAY_STORE_PKG)
            }
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                // Play Store not available — fall back to browser
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$PLAY_SERVICES_PKG"),
                    )
                )
            }
        },
    )
}

/**
 * Stateless inner composable — extracted so [Preview] can be used without Hilt.
 */
@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onSignIn: () -> Unit,
    onOpenPlayStore: () -> Unit,
) {
    val isLoading = uiState is LoginUiState.Loading
    val isPlayServicesError = uiState is LoginUiState.PlayServicesError

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Top: brand area ──────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(Spacing.xxxl))

                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                Text(
                    text = "AutoReply",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Automated messaging, simplified",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Bottom: sign-in controls ─────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // ── Generic error banner ─────────────────────────────────
                AnimatedVisibility(visible = uiState is LoginUiState.Error) {
                    if (uiState is LoginUiState.Error) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.md),
                        )
                    }
                }

                // ── Play Services error banner + Update button ───────────
                AnimatedVisibility(visible = isPlayServicesError) {
                    if (uiState is LoginUiState.PlayServicesError) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp),
                            )

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            TextButton(
                                onClick = onOpenPlayStore,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.size(Spacing.xs))
                                Text(
                                    text = "Update Google Play Services",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }

                // ── Sign-In button / loading indicator ───────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        GoogleSignInButton(
                            onClick = onSignIn,
                            enabled = !isPlayServicesError,  // grey out while error is visible
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun LoginScreenIdlePreview() {
    AutoReplyTheme {
        LoginScreenContent(uiState = LoginUiState.Idle, onSignIn = {}, onOpenPlayStore = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    AutoReplyTheme {
        LoginScreenContent(uiState = LoginUiState.Loading, onSignIn = {}, onOpenPlayStore = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    AutoReplyTheme {
        LoginScreenContent(
            uiState = LoginUiState.Error("Google Sign-In failed. Please try again."),
            onSignIn = {},
            onOpenPlayStore = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPlayServicesErrorPreview() {
    AutoReplyTheme {
        LoginScreenContent(
            uiState = LoginUiState.PlayServicesError(
                "Google Play Services needs to be updated. " +
                        "Please update it from the Play Store and try again."
            ),
            onSignIn = {},
            onOpenPlayStore = {},
        )
    }
}
