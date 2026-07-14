package com.example.noteshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a paired connection between two users.
 * A pair is the core relationship unit — all notes, moods, events, etc. belong to a pair.
 */
@Entity(tableName = "pairs")
data class Pair(
    @PrimaryKey
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val inviteCode: String = "",
    val status: String = "pending", // "pending", "active", "dissolved"
    val anniversary: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
) {
    fun getPartnerId(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Id else user1Id
    }

    fun containsUser(userId: String): Boolean {
        return user1Id == userId || user2Id == userId
    }

    val isActive: Boolean get() = status == "active"
    val isPending: Boolean get() = status == "pending"
}
