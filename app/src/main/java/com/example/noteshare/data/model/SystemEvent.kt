package com.example.noteshare.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents an atomic event for the event-driven synchronization architecture.
 * This is what gets sent over the network (e.g., to Firestore or WebSocket).
 */
data class SystemEvent(
    @SerializedName("eventId")
    val eventId: String,
    @SerializedName("pairId")
    val pairId: String,
    @SerializedName("actorId")
    val actorId: String,
    @SerializedName("eventType")
    val eventType: EventType,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("payload")
    val payload: Map<String, Any>
)

enum class EventType {
    CREATE_NOTE,
    UPDATE_NOTE,
    DELETE_NOTE,
    MOOD_CHANGED,
    CALENDAR_REMINDER,
    CREATE_PAIR,
    UPDATE_USER_PROFILE
}
