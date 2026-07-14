package com.example.noteshare.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a shared memory (photo, note snapshot, or milestone).
 */
data class Memory(
    @DocumentId
    val id: String = "",
    val pairId: String = "",
    val type: String = "photo",    // "photo", "note_snapshot", "milestone"
    val title: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val linkedNoteId: String? = null,
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
