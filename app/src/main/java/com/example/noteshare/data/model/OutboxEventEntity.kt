package com.example.noteshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a generic event for the Outbox Sync Engine.
 * Actions can be CREATE_NOTE, UPDATE_NOTE, DELETE_NOTE, CREATE_MOOD, etc.
 */
@Entity(tableName = "outbox_events")
data class OutboxEventEntity(
    @PrimaryKey
    val queueId: String,
    val action: String,
    val payloadType: String,
    val payload: String, // JSON payload representing the object state or partial update
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
