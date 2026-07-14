package com.example.noteshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a mood check-in entry.
 * Each person can log their mood with 5 emoji levels.
 */
@Entity(tableName = "moods")
data class MoodEntry(
    @PrimaryKey
    val id: String = "",
    val pairId: String = "",
    val userId: String = "",
    val userName: String = "",
    val level: Int = 3,          // 1=very sad, 2=sad, 3=neutral, 4=happy, 5=very happy
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
) {
    val emoji: String get() = when (level) {
        1 -> "😢"
        2 -> "😕"
        3 -> "😐"
        4 -> "😊"
        5 -> "🥰"
        else -> "😐"
    }

    val label: String get() = when (level) {
        1 -> "Very Sad"
        2 -> "Sad"
        3 -> "Okay"
        4 -> "Happy"
        5 -> "Wonderful"
        else -> "Okay"
    }
}
