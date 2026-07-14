package com.example.noteshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user in the NoteShare app.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val favoriteColor: String? = null,
    val birthday: Long? = null,
    val pairId: String? = null,
    val partnerNickname: String? = null,
    val anniversary: Long? = null,
    val reminderHour: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
)
