package com.psspl.autoreply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.psspl.autoreply.navigation.AppBottomNavBar
import com.psspl.autoreply.navigation.AppNavGraph
import com.psspl.autoreply.ui.auth.AuthState
import com.psspl.autoreply.ui.auth.AuthViewModel
import com.psspl.autoreply.ui.screens.login.LoginScreen
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoReplyTheme {
                AppContent()
            }
        }
    }
}

/**
 * Root composable. Observes [AuthState] and renders one of three branches:
 *
 * - [AuthState.Loading]         → Full-screen progress indicator (session restore in progress).
 * - [AuthState.Unauthenticated] → [LoginScreen] (no bottom nav).
 * - [AuthState.Authenticated]   → Main app shell with bottom navigation.
 *
 * Navigation after sign-in / sign-out is automatic: as soon as Firebase reports
 * a state change, [AuthViewModel.authState] updates and this composable re-composes.
 */
@Composable
private fun AppContent(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    when (authState) {
        // ── Restoring session ────────────────────────────────────────────
        AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        // ── Not authenticated ────────────────────────────────────────────
        AuthState.Unauthenticated -> {
            LoginScreen()
        }

        // ── Authenticated ────────────────────────────────────────────────
        is AuthState.Authenticated -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = { AppBottomNavBar(navController = navController) },
            ) { innerPadding ->
                AppNavGraph(
                    navController = navController,
                    onSignOut = { authViewModel.signOut() },
                    modifier = Modifier
                        .padding(innerPadding)
                        // Consume the insets so nested Scaffolds inside each screen
                        // do not apply the status-bar / nav-bar insets a second time.
                        .consumeWindowInsets(innerPadding),
                )
            }
        }
    }
}
