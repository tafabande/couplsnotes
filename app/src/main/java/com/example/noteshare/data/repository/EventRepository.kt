package com.example.noteshare.data.repository

import com.example.noteshare.data.local.db.dao.EventDao
import com.example.noteshare.data.local.db.dao.OutboxDao
import com.example.noteshare.data.model.Event
import com.example.noteshare.data.model.EventType
import com.example.noteshare.data.model.OutboxEventEntity
import com.example.noteshare.data.model.SyncStatus
import com.example.noteshare.data.model.SystemEvent
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.util.Result
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val eventDao: EventDao,
    private val outboxDao: OutboxDao
) {
    private val gson = Gson()

    fun getUpcomingEvents(pairId: String): Flow<List<Event>> {
        return eventDao.getUpcomingEvents(pairId)
    }

    suspend fun getNextEvent(pairId: String): Event? {
        return eventDao.getNextEvent(pairId)
    }

    suspend fun addEvent(pairId: String, event: Event): Result<String> {
        return try {
            // BR-302: Event Chronology Enforcement
            if (event.date < System.currentTimeMillis()) {
                return Result.Error("BR-302 Violation: Cannot create an event in the past.")
            }

            val eventId = if (event.id.isEmpty()) UUID.randomUUID().toString() else event.id
            val newEvent = event.copy(
                id = eventId,
                pairId = pairId,
                syncStatus = SyncStatus.PENDING,
                version = 1
            )
            
            eventDao.insertEvent(newEvent)

            val systemEvent = SystemEvent(
                eventId = UUID.randomUUID().toString(),
                pairId = pairId,
                actorId = event.createdBy,
                eventType = EventType.CALENDAR_REMINDER,
                payload = mapOf("event" to newEvent)
            )

            outboxDao.insertEvent(
                OutboxEventEntity(
                    queueId = UUID.randomUUID().toString(),
                    action = "CREATE_EVENT",
                    payloadType = "Event",
                    payload = gson.toJson(systemEvent)
                )
            )

            Result.Success(eventId)
        } catch (e: Exception) {
            Result.Error("Failed to add event: ${e.message}", e)
        }
    }

    suspend fun syncToLocal(events: List<Event>) {
        // ... handled via SyncWorker
    }
}
