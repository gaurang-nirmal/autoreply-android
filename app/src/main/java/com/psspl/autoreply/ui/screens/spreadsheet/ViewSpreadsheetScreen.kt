package com.psspl.autoreply.ui.screens.spreadsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.autoreply.database.entity.SpreadsheetRuleEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

private val ChatBackground = Color(0xFFECE5DD)
private val GreenBubble = Color(0xFFDCF8C6)
private val SheetGreen = Color(0xFF0F9D58)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSpreadsheetScreen(
    spreadsheetId: String,
    sheetName: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SpreadsheetViewModel = hiltViewModel(),
) {
    // Observe rules for this specific sheet.
    // remember(spreadsheetId) ensures the same StateFlow instance is reused across
    // recompositions — without it, each recomposition creates a new StateFlow that
    // starts with emptyList() before Room emits, causing a visible blink loop.
    val rulesFlow = remember(spreadsheetId) { viewModel.getRulesForSheet(spreadsheetId) }
    val allRules by rulesFlow.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Filter rules by search query locally (fast, no DB round-trip needed)
    val displayedRules = remember(allRules, searchQuery) {
        if (searchQuery.isBlank()) allRules
        else allRules.filter { rule ->
            rule.keyword.contains(searchQuery, ignoreCase = true) ||
                    rule.replyMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(showSearch) {
        if (showSearch) searchFocusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = sheetName,
                navigationIcon = {
                    IconButton(onClick = {
                        if (showSearch) {
                            showSearch = false
                            searchQuery = ""
                            keyboardController?.hide()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (showSearch) "Close search" else "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search rules",
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            // ── Sheet name + rule count chip ─────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TableChart,
                            contentDescription = null,
                            tint = SheetGreen,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = Spacing.sm),
                    ) {
                        Text(
                            text = sheetName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${allRules.size} rule${if (allRules.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Search bar ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showSearch,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                        .focusRequester(searchFocusRequester),
                    placeholder = { Text("Search keyword or reply…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SheetGreen,
                        cursorColor = SheetGreen,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() },
                    ),
                )
            }

            // ── Rules list or empty state ────────────────────────────────────
            if (displayedRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No results for \"$searchQuery\""
                            } else {
                                "No rules synced yet"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Try a different search term"
                            } else {
                                "Go back and tap Sync to load rules from this sheet"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = displayedRules, key = { it.id }) { rule ->
                        SheetRuleItem(rule = rule)
                    }
                }
            }
        }
    }
}

// ─── Chat-bubble style rule item (matches RulesScreen design exactly) ─────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SheetRuleItem(
    rule: SpreadsheetRuleEntity,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Surface(
            color = ChatBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // Incoming keyword bubble — left-aligned
                Box(modifier = Modifier.fillMaxWidth(0.75f)) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 2.dp, topEnd = 12.dp,
                            bottomEnd = 12.dp, bottomStart = 12.dp,
                        ),
                        color = Color.White,
                    ) {
                        Text(
                            text = rule.keyword,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical = Spacing.xs,
                            ),
                        )
                    }
                }

                // Reply bubble — right-aligned, green
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .align(Alignment.End),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 12.dp, topEnd = 2.dp,
                            bottomEnd = 12.dp, bottomStart = 12.dp,
                        ),
                        color = GreenBubble,
                    ) {
                        Text(
                            text = rule.replyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical = Spacing.xs,
                            ),
                        )
                    }
                }
            }
        }

        // Row divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ViewSpreadsheetScreenPreview() {
    AutoReplyTheme {
        ViewSpreadsheetScreen(
            spreadsheetId = "demo",
            sheetName = "Sample Sheet",
        )
    }
}
