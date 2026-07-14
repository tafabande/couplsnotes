package com.example.noteshare.data.local.db.dao

import androidx.room.*
import com.example.noteshare.data.model.OutboxEventEntity

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox_events ORDER BY timestamp ASC")
    suspend fun getPendingEvents(): List<OutboxEventEntity>

    @Query("SELECT COUNT(*) FROM outbox_events")
    fun observePendingEventCount(): kotlinx.coroutines.flow.Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: OutboxEventEntity)

    @Delete
    suspend fun deleteEvent(event: OutboxEventEntity)

    @Query("DELETE FROM outbox_events WHERE queueId = :queueId")
    suspend fun deleteEventById(queueId: String)
}
