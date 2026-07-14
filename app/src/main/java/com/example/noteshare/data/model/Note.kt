package com.example.noteshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a note in the shared journal.
 * Notes can be plain text, lists, or checklists — similar to Google Keep.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = "",
    val pairId: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val type: String = "text",         // "text", "list", "checklist"
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val displayStyle: String = "plain", // "plain", "big_picture", "framed"
    val imageUrl: String? = null,
    val scheduledAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,        // soft delete
    val isVault: Boolean = false,       // Requires Biometric verification and is encrypted
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
) {
    val isDeleted: Boolean get() = deletedAt != null
    val isScheduled: Boolean get() = scheduledAt != null && scheduledAt > System.currentTimeMillis()
    val isPublished: Boolean get() = !isScheduled && !isDeleted && !isArchived
}
