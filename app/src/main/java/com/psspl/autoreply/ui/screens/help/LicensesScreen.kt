package com.psspl.autoreply.ui.screens.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.GreenPrimary
import com.psspl.autoreply.ui.theme.Spacing

// ── License data ──────────────────────────────────────────────────────────────

private data class License(
    val library: String,
    val version: String,
    val license: String,
    val url: String,
    val licenseText: String,
)

private val APACHE_2 = """
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
""".trimIndent()

private val MIT = """
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
""".trimIndent()

private val licenses = listOf(
    License("Kotlin", "2.0", "Apache 2.0", "https://kotlinlang.org", APACHE_2),
    License(
        "Jetpack Compose",
        "1.7",
        "Apache 2.0",
        "https://developer.android.com/jetpack/compose",
        APACHE_2
    ),
    License("Hilt (Dagger)", "2.51", "Apache 2.0", "https://dagger.dev/hilt", APACHE_2),
    License(
        "Room",
        "2.6",
        "Apache 2.0",
        "https://developer.android.com/jetpack/androidx/releases/room",
        APACHE_2
    ),
    License("Retrofit", "2.9", "Apache 2.0", "https://square.github.io/retrofit", APACHE_2),
    License("OkHttp", "4.12", "Apache 2.0", "https://square.github.io/okhttp", APACHE_2),
    License("Gson", "2.10", "Apache 2.0", "https://github.com/google/gson", APACHE_2),
    License("Coil", "2.6", "Apache 2.0", "https://coil-kt.github.io/coil", APACHE_2),
    License(
        "DataStore Preferences",
        "1.1",
        "Apache 2.0",
        "https://developer.android.com/jetpack/androidx/releases/datastore",
        APACHE_2
    ),
    License(
        "Kotlinx Coroutines",
        "1.8",
        "Apache 2.0",
        "https://github.com/Kotlin/kotlinx.coroutines",
        APACHE_2
    ),
    License(
        "WorkManager",
        "2.9",
        "Apache 2.0",
        "https://developer.android.com/jetpack/androidx/releases/work",
        APACHE_2
    ),
    License(
        "Navigation Compose",
        "2.7",
        "Apache 2.0",
        "https://developer.android.com/jetpack/androidx/releases/navigation",
        APACHE_2
    ),
    License("Material 3", "1.2", "Apache 2.0", "https://m3.material.io", APACHE_2),
    License(
        "Credential Manager",
        "1.2",
        "Apache 2.0",
        "https://developer.android.com/jetpack/androidx/releases/credentials",
        APACHE_2
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Licenses",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            item {
                Text(
                    text = "This app uses the following open source libraries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md, top = Spacing.xs),
                )
            }
            items(licenses, key = { it.library }) { license ->
                LicenseCard(license)
                Spacer(Modifier.height(Spacing.sm))
            }
            item { Spacer(Modifier.height(Spacing.xl)) }
        }
    }
}

@Composable
private fun LicenseCard(license: License) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = Spacing.md, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = license.library,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "v${license.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "  ·  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = license.license,
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                    Text(
                        text = license.url,
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                    Text(
                        text = license.licenseText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .fillMaxWidth()
                            .padding(Spacing.md),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LicensesPreview() {
    AutoReplyTheme { LicensesScreen(onBack = {}) }
}
