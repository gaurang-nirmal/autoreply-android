package com.psspl.autoreply.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MenuDefaults {
    val shadowElevation: Dp = 8.dp
    val minWidth: Dp = 180.dp

    val itemTextStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val itemContentColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val iconTint: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}
