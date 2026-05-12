package com.psspl.autoreply.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.psspl.autoreply.utils.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenOnContainer,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = BlueOnContainer,
    background = NeutralBackground,
    onBackground = NeutralOnBackground,
    surface = NeutralSurface,
    onSurface = NeutralOnSurface,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralOnSurfaceVariant,
    outline = NeutralOutline,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = OnErrorRedContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenContainer,
    secondary = BlueSecondaryLight,
    onSecondary = BlueOnSecondaryDark,
    secondaryContainer = BlueContainerDark,
    onSecondaryContainer = BlueContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = ErrorRedLight,
    onError = Color(0xFF450A0A),
    errorContainer = ErrorRedContainerDark,
    onErrorContainer = OnErrorRedContainerDark,
)

@Composable
fun AutoReplyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Explicitly sync status bar and navigation bar icon appearance with the app's
    // resolved theme, not the system theme. This fixes the case where OS=Dark but
    // the app is forced to Light — enableEdgeToEdge() would otherwise leave white
    // icons on a light-colored status bar.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowInsetsControllerCompat(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
