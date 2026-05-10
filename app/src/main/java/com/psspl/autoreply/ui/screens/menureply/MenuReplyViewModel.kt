package com.psspl.autoreply.ui.screens.menureply

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.backup.MenuReplyBackupManager
import com.psspl.autoreply.database.entity.MenuReplyEntity
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import com.psspl.autoreply.repository.MenuReplyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI models ────────────────────────────────────────────────────────────────

data class MenuReplyWithItems(
    val reply: MenuReplyEntity,
    val items: List<MenuReplyItemWithChildren>,
)

data class MenuReplyItemWithChildren(
    val item: MenuReplyItemEntity,
    val hasChildren: Boolean,
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class MenuReplyViewModel @Inject constructor(
    private val repository: MenuReplyRepository,
    private val backupManager: MenuReplyBackupManager,
) : ViewModel() {

    /** Combined state: each reply with its top-level items and hasChildren flags */
    val menuRepliesWithItems: StateFlow<List<MenuReplyWithItems>> = combine(
        repository.getAllReplies(),
        repository.getAllTopLevelItems(),
        repository.getAllItems(),
    ) { replies, topItems, allItems ->
        replies.map { reply ->
            val items = topItems
                .filter { it.menuReplyId == reply.id }
                .sortedBy { it.position }
                .map { item ->
                    MenuReplyItemWithChildren(
                        item = item,
                        hasChildren = allItems.any { it.parentItemId == item.id },
                    )
                }
            MenuReplyWithItems(reply = reply, items = items)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    // ─── Snackbar feedback ────────────────────────────────────────────────────

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // ─── Flow accessors (for form / more-options / children screens) ─────────

    fun getReplyById(id: Int): Flow<MenuReplyEntity?> = repository.getReplyById(id)

    fun getItemById(id: Int): Flow<MenuReplyItemEntity?> = repository.getItemById(id)

    /**
     * Returns the direct children of [parentItemId], each annotated with a
     * [MenuReplyItemWithChildren.hasChildren] flag so the UI can show REPLY vs SUBMENU.
     * This function is used recursively for endless nesting.
     */
    fun getChildrenWithFlags(parentItemId: Int): Flow<List<MenuReplyItemWithChildren>> =
        repository.getAllItems().map { allItems ->
            allItems
                .filter { it.parentItemId == parentItemId }
                .sortedBy { it.position }
                .map { item ->
                    MenuReplyItemWithChildren(
                        item = item,
                        hasChildren = allItems.any { child -> child.parentItemId == item.id },
                    )
                }
        }

    // ─── Reply (trigger) CRUD ─────────────────────────────────────────────────

    fun addReply(triggerMessage: String) {
        viewModelScope.launch {
            repository.insertReply(MenuReplyEntity(triggerMessage = triggerMessage.trim()))
        }
    }

    fun editTrigger(replyId: Int, triggerMessage: String) {
        viewModelScope.launch {
            repository.getReplyById(replyId).first()?.let { existing ->
                repository.updateReply(existing.copy(triggerMessage = triggerMessage.trim()))
            }
        }
    }

    fun deleteReply(reply: MenuReplyEntity) {
        viewModelScope.launch {
            // Items cascade-delete via FK, but also explicit in case FK not enforced
            repository.deleteAllItemsForReply(reply.id)
            repository.deleteReply(reply)
        }
    }

    // ─── Item CRUD ────────────────────────────────────────────────────────────

    /**
     * @param parentItemId null = top-level item; >0 = nested child item
     */
    fun addItem(menuReplyId: Int, parentItemId: Int?, text: String) {
        viewModelScope.launch {
            val nextPosition = if (parentItemId == null) {
                repository.getTopLevelItemCount(menuReplyId) + 1
            } else {
                repository.getChildItemCount(parentItemId) + 1
            }
            repository.insertItem(
                MenuReplyItemEntity(
                    menuReplyId = menuReplyId,
                    parentItemId = parentItemId,
                    position = nextPosition,
                    text = text.trim(),
                )
            )
        }
    }

    fun editItem(itemId: Int, text: String) {
        viewModelScope.launch {
            repository.getItemById(itemId).first()?.let { existing ->
                repository.updateItem(existing.copy(text = text.trim()))
            }
        }
    }

    fun deleteItem(item: MenuReplyItemEntity) {
        viewModelScope.launch {
            repository.deleteChildItems(item.id) // remove nested children first
            repository.deleteItem(item)
        }
    }

    fun setStopReply(itemId: Int, stopReply: Boolean) {
        viewModelScope.launch {
            repository.getItemById(itemId).first()?.let { existing ->
                repository.updateItem(existing.copy(stopReply = stopReply))
            }
        }
    }

    // ─── Backup / Restore / Clear All ────────────────────────────────────────

    fun exportReplies(uri: Uri) {
        viewModelScope.launch {
            val success = backupManager.exportReplies(uri)
            _snackbarMessage.value = if (success) "Backup saved successfully" else "Backup failed"
        }
    }

    fun importReplies(uri: Uri) {
        viewModelScope.launch {
            val count = backupManager.importReplies(uri)
            _snackbarMessage.value = when {
                count > 0 -> "$count menu reply(s) imported"
                count == 0 -> "No new replies to import (all duplicates)"
                else -> "Restore failed — invalid file"
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAllReplies()
            _snackbarMessage.value = "All menu replies cleared"
        }
    }

    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }
}
