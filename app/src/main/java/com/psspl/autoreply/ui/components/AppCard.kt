package com.psspl.autoreply.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.psspl.autoreply.ui.theme.Spacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = modifier.fillMaxWidth()
    val cardShape = MaterialTheme.shapes.medium
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )
    val cardElevation = CardDefaults.cardElevation(defaultElevation = 1.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = cardShape,
            colors = cardColors,
            elevation = cardElevation,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md),
                content = content,
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = cardShape,
            colors = cardColors,
            elevation = cardElevation,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md),
                content = content,
            )
        }
    }
}
