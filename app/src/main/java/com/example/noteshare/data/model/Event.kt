package com.example.noteshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a calendar event (birthday, anniversary, date, custom).
 */
@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    val id: String = "",
    val pairId: String = "",
    val title: String = "",
    val description: String? = null,
    val date: Long = System.currentTimeMillis(),
    val type: String = "custom",   // "birthday", "anniversary", "date", "custom"
    val isRecurring: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
) {
    val typeEmoji: String get() = when (type) {
        "birthday" -> "🎂"
        "anniversary" -> "💕"
        "date" -> "🌹"
        else -> "📅"
    }
}
