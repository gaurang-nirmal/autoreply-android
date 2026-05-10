package com.psspl.autoreply.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.psspl.autoreply.ui.theme.MenuDefaults

data class TopbarMenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val isDividerAfter: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun TopbarMenu(items: List<TopbarMenuItem>) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MenuDefaults.containerColor,
    ) {
        items.forEachIndexed { index, item ->
            val itemIcon = item.icon
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.label,
                        style = MenuDefaults.itemTextStyle,
                        color = MenuDefaults.itemContentColor,
                    )
                },
                leadingIcon = if (itemIcon != null) {
                    {
                        Icon(
                            imageVector = itemIcon,
                            contentDescription = null,
                            tint = MenuDefaults.iconTint,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else null,
                onClick = {
                    expanded = false
                    item.onClick()
                },
            )
            if (item.isDividerAfter && index < items.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
