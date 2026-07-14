package com.example.noteshare.data.repository

import com.example.noteshare.data.local.db.dao.MoodDao
import com.example.noteshare.data.local.db.dao.OutboxDao
import com.example.noteshare.data.model.EventType
import com.example.noteshare.data.model.MoodEntry
import com.example.noteshare.data.model.OutboxEventEntity
import com.example.noteshare.data.model.SyncStatus
import com.example.noteshare.data.model.SystemEvent
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.util.Result
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val moodDao: MoodDao,
    private val outboxDao: OutboxDao
) {
    private val gson = Gson()

    fun getLocalMoods(pairId: String): Flow<List<MoodEntry>> {
        return moodDao.getAllMoods(pairId)
    }

    suspend fun getLatestMood(pairId: String, userId: String): MoodEntry? {
        return moodDao.getLatestMood(pairId, userId)
    }

    suspend fun addMood(pairId: String, mood: MoodEntry): Result<String> {
        return try {
            // BR-401: Mood Volatility Cap implementation (only the latest in 24h updates the widget, 
            // others are historical). Here we enforce it by saving to DB (all are saved), but we can 
            // optionally mark them if needed. Room querying handles the "latest" logic.
            val latestMood = getLatestMood(pairId, mood.userId)
            val isWithin24h = latestMood != null && 
                (System.currentTimeMillis() - latestMood.createdAt) < TimeUnit.HOURS.toMillis(24)
            
            // If we strictly capped creation to 1 per 24h:
            // if (isWithin24h) return Result.Error("BR-401 Violation: Already logged mood in last 24h.")
            // However, the rule says "only the latest entry within a rolling 24-hour window updates the 
            // active dashboard... Historical entries are pushed silently." This means we allow the write.

            val moodId = if (mood.id.isEmpty()) UUID.randomUUID().toString() else mood.id
            val newMood = mood.copy(
                id = moodId,
                pairId = pairId,
                syncStatus = SyncStatus.PENDING,
                version = 1
            )
            
            moodDao.insertMood(newMood)

            val systemEvent = SystemEvent(
                eventId = UUID.randomUUID().toString(),
                pairId = pairId,
                actorId = mood.userId,
                eventType = EventType.MOOD_CHANGED,
                payload = mapOf("mood" to newMood)
            )

            outboxDao.insertEvent(
                OutboxEventEntity(
                    queueId = UUID.randomUUID().toString(),
                    action = "CREATE_MOOD",
                    payloadType = "MoodEntry",
                    payload = gson.toJson(systemEvent)
                )
            )
            
            Result.Success(moodId)
        } catch (e: Exception) {
            Result.Error("Failed to add mood locally: ${e.message}", e)
        }
    }
}
