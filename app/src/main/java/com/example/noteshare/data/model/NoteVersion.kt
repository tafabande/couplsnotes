package com.example.noteshare.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a version history entry for a note.
 * Every edit creates a new version for conflict resolution and audit.
 */
data class NoteVersion(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val editedBy: String = "",
    val editedByName: String = "",
    val editedAt: Long = System.currentTimeMillis()
)
