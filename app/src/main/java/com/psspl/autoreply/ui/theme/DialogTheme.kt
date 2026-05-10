package com.psspl.autoreply.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DialogDefaults {
    val tonalElevation: Dp = 6.dp

    val titleStyle: TextStyle
        @Composable get() = MaterialTheme.typography.headlineSmall

    val bodyStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val titleColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val bodyColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}
