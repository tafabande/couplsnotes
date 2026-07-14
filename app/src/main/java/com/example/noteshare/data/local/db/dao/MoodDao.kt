package com.example.noteshare.data.local.db.dao

import androidx.room.*
import com.example.noteshare.data.model.MoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Query("SELECT * FROM moods WHERE pairId = :pairId ORDER BY createdAt DESC")
    fun getAllMoods(pairId: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM moods WHERE pairId = :pairId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentMoods(pairId: String, limit: Int = 10): List<MoodEntry>

    @Query("SELECT * FROM moods WHERE pairId = :pairId AND userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestMood(pairId: String, userId: String): MoodEntry?

    @Query("SELECT * FROM moods WHERE pairId = :pairId AND createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun getMoodsBetween(pairId: String, start: Long, end: Long): List<MoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<MoodEntry>)

    @Delete
    suspend fun deleteMood(mood: MoodEntry)

    @Query("DELETE FROM moods WHERE pairId = :pairId")
    suspend fun deleteAllForPair(pairId: String)
}
