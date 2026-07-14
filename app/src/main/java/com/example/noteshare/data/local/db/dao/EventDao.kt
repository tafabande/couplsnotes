package com.example.noteshare.data.local.db.dao

import androidx.room.*
import com.example.noteshare.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events WHERE pairId = :pairId ORDER BY date ASC")
    fun getAllEvents(pairId: String): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE pairId = :pairId AND date >= :fromDate ORDER BY date ASC")
    fun getUpcomingEvents(pairId: String, fromDate: Long = System.currentTimeMillis()): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE pairId = :pairId AND date >= :fromDate ORDER BY date ASC LIMIT 1")
    suspend fun getNextEvent(pairId: String, fromDate: Long = System.currentTimeMillis()): Event?

    @Query("SELECT * FROM events WHERE pairId = :pairId AND date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getEventsBetween(pairId: String, start: Long, end: Long): List<Event>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE pairId = :pairId")
    suspend fun deleteAllForPair(pairId: String)

    @Query("UPDATE events SET syncStatus = 'SYNCED' WHERE id = :eventId")
    suspend fun markSynced(eventId: String)
}
