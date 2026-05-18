package com.psspl.autoreply.ui.screens.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psspl.autoreply.database.entity.ContactEntity
import com.psspl.autoreply.ui.components.AppTopBar
import com.psspl.autoreply.ui.components.EmptyState
import com.psspl.autoreply.ui.components.LoadingIndicator
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.GreenContainer
import com.psspl.autoreply.ui.theme.GreenPrimary
import com.psspl.autoreply.ui.theme.Spacing
import com.psspl.autoreply.utils.ContactMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    onNavigateToGroupSettings: () -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }

    val selectedMode = runCatching { ContactMode.valueOf(config.contactMode) }
        .getOrDefault(ContactMode.EVERYONE)
    val showContactList =
        selectedMode == ContactMode.MY_LIST || selectedMode == ContactMode.EXCEPT_MY_LIST

    // Runtime READ_CONTACTS permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) showContactPicker = true }

    fun onAddContactTapped() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) showContactPicker = true
        else permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    if (showInfoDialog) ContactInfoDialog(onDismiss = { showInfoDialog = false })
    if (showContactPicker) {
        ContactPickerDialog(
            alreadyAdded = contacts.map { it.name },
            onContactSelected = { name -> viewModel.addContact(name) },
            onDismiss = { showContactPicker = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Contacts",
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            // ── Section label ─────────────────────────────────────────────────
            SectionLabel(text = "AUTO REPLY TO")

            // ── Mode selection cards ──────────────────────────────────────────
            ModeCard(
                icon = Icons.Filled.People,
                title = "Everyone",
                subtitle = "Reply to all incoming messages",
                selected = selectedMode == ContactMode.EVERYONE,
                onClick = { viewModel.setContactMode(ContactMode.EVERYONE) },
            )
            Spacer(Modifier.height(Spacing.xs))
            ModeCard(
                icon = Icons.Filled.Person,
                title = "My contact list",
                subtitle = "Reply only to contacts you've saved here",
                selected = selectedMode == ContactMode.MY_LIST,
                onClick = { viewModel.setContactMode(ContactMode.MY_LIST) },
            )
            Spacer(Modifier.height(Spacing.xs))
            ModeCard(
                icon = Icons.Filled.PersonOff,
                title = "Except my contact list",
                subtitle = "Reply to everyone except your saved contacts",
                selected = selectedMode == ContactMode.EXCEPT_MY_LIST,
                onClick = { viewModel.setContactMode(ContactMode.EXCEPT_MY_LIST) },
            )
            Spacer(Modifier.height(Spacing.xs))
            ModeCard(
                icon = Icons.Filled.PhoneAndroid,
                title = "Except phone contacts",
                subtitle = "Skip anyone saved in your phone's contacts",
                selected = selectedMode == ContactMode.EXCEPT_PHONE_CONTACTS,
                onClick = { viewModel.setContactMode(ContactMode.EXCEPT_PHONE_CONTACTS) },
            )

            Spacer(Modifier.height(Spacing.lg))

            // ── Groups toggle card ────────────────────────────────────────────
            SectionLabel(text = "GROUPS")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (config.groupsEnabled) GreenPrimary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            tint = if (config.groupsEnabled) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Groups",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Apply auto reply rules to group chats",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = config.groupsEnabled,
                        onCheckedChange = { viewModel.setGroupsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenPrimary,
                        ),
                        modifier = Modifier.scale(0.8f),
                    )
                    if (config.groupsEnabled) {
                        IconButton(onClick = onNavigateToGroupSettings) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Group Settings",
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // ── Contact list section (animated) ───────────────────────────────
            AnimatedVisibility(
                visible = showContactList,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(Spacing.lg))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = Spacing.xs, bottom = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionLabel(
                            text = "CONTACT LIST",
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GreenPrimary)
                                .clickable { onAddContactTapped() }
                                .padding(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add contact",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        if (contacts.isEmpty()) {
                            EmptyState(
                                icon = Icons.Filled.Contacts,
                                title = if (selectedMode == ContactMode.MY_LIST)
                                    "Add contacts to reply only to them"
                                else
                                    "Add contacts to exclude from auto reply",
                                description = "Tap + to pick from your device contacts",
                            )
                        } else {
                            contacts.forEachIndexed { index, contact ->
                                ContactListItem(
                                    contact = contact,
                                    onDelete = { viewModel.deleteContact(contact) },
                                )
                                if (index < contacts.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = GreenPrimary,
        modifier = modifier.padding(start = Spacing.xs, bottom = Spacing.sm),
        letterSpacing = androidx.compose.ui.unit.TextUnit(
            1.5f, androidx.compose.ui.unit.TextUnitType.Sp,
        ),
    )
}

// ── Mode selection card ───────────────────────────────────────────────────────

@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) GreenPrimary else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        // No background change on selection — border + icon + text colour is enough
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) GreenPrimary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Contact list item ─────────────────────────────────────────────────────────

@Composable
private fun ContactListItem(contact: ContactEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = contact.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Device contact picker dialog ──────────────────────────────────────────────

@Composable
private fun ContactPickerDialog(
    alreadyAdded: List<String>,
    onContactSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var allContacts by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<String>()
            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
                "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0",
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC",
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx)
                    if (!name.isNullOrBlank()) list.add(name)
                }
            }
            allContacts = list.distinct()
            isLoading = false
        }
    }

    val filtered = remember(allContacts, searchQuery) {
        val base = if (searchQuery.isBlank()) allContacts
        else allContacts.filter { it.contains(searchQuery, ignoreCase = true) }
        base.filterNot { alreadyAdded.any { added -> added.equals(it, ignoreCase = true) } }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Contacts,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select Contact",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Pick from your device contacts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search contacts…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        cursorColor = GreenPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.sm))

                // Contacts list
                Box(modifier = Modifier.heightIn(min = 80.dp, max = 320.dp)) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                LoadingIndicator()
                            }
                        }

                        filtered.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "No contacts found"
                                    else "No results for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        else -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                items(filtered) { name ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                onContactSelected(name)
                                                onDismiss()
                                            }
                                            .padding(
                                                horizontal = Spacing.sm,
                                                vertical = Spacing.sm
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(GreenPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = name.take(1).uppercase(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = GreenPrimary,
                                            )
                                        }
                                        Spacer(Modifier.width(Spacing.sm))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                // Dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

// ── Info dialog ───────────────────────────────────────────────────────────────

@Composable
private fun ContactInfoDialog(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GreenContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        title = {
            Text(
                text = "Auto reply to",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                InfoEntry(
                    bold = "Everyone",
                    detail = " — Reply to all incoming messages from any sender.",
                    boldColor = onSurface,
                    detailColor = onSurfaceVariant,
                )
                InfoEntry(
                    bold = "My contact list",
                    detail = " — Reply only to senders you've manually added to your list.",
                    boldColor = onSurface,
                    detailColor = onSurfaceVariant,
                )
                InfoEntry(
                    bold = "Except my contact list",
                    detail = " — Reply to everyone except the contacts in your saved list.",
                    boldColor = onSurface,
                    detailColor = onSurfaceVariant,
                )
                InfoEntry(
                    bold = "Except phone contacts",
                    detail = " — Reply to everyone except contacts in your phone's address book.",
                    boldColor = onSurface,
                    detailColor = onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("GOT IT", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
private fun InfoEntry(bold: String, detail: String, boldColor: Color, detailColor: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) { append(bold) }
            withStyle(SpanStyle(color = detailColor)) { append(detail) }
        },
        style = MaterialTheme.typography.bodyMedium,
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ContactsScreenPreview() {
    AutoReplyTheme { ContactsScreen() }
}
