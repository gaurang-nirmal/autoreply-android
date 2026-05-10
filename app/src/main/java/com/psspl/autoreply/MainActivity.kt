package com.psspl.autoreply

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
import com.psspl.autoreply.utils.AppLockManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appLockManager: AppLockManager

    private val mainViewModel: MainViewModel by viewModels()

    private var isUnlocked = false
    private var isAuthenticating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            AutoReplyTheme(themeMode = themeMode) {
                AppContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticating) return
        // Read directly from the DB — avoids race conditions where the StateFlow's
        // initialValue (false) is still present before the first Room emission.
        lifecycleScope.launch {
            val lockEnabled = mainViewModel.isAppLockEnabled()
            if (lockEnabled && !isUnlocked && !isAuthenticating) {
                isAuthenticating = true
                appLockManager.authenticate(
                    activity = this@MainActivity,
                    onSuccess = {
                        isUnlocked = true
                        isAuthenticating = false
                    },
                    onError = {
                        isAuthenticating = false
                        finish()
                    },
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't reset isUnlocked while the biometric prompt itself is showing —
        // the prompt triggers onPause, and resetting here would cause an auth loop.
        if (!isAuthenticating) {
            isUnlocked = false
        }
    }
}

@Composable
private fun AppContent(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        AuthState.Unauthenticated -> {
            LoginScreen()
        }

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
                        .consumeWindowInsets(innerPadding),
                )
            }
        }
    }
}
