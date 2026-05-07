package com.psspl.autoreply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.psspl.autoreply.navigation.AppBottomNavBar
import com.psspl.autoreply.navigation.AppNavGraph
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
 * Root composable. Renamed from AutoReplyApp to avoid name collision
 * with the AutoReplyApp Application class in the same package.
 */
@Composable
private fun AppContent(navController: NavHostController = rememberNavController()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { AppBottomNavBar(navController = navController) },
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                // Consume the insets so nested Scaffolds inside each screen
                // do not apply the status-bar / nav-bar insets a second time.
                .consumeWindowInsets(innerPadding),
        )
    }
}
