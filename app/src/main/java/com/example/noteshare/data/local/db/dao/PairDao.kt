package com.example.noteshare.data.local.db.dao

import androidx.room.*
import com.example.noteshare.data.model.Pair
import kotlinx.coroutines.flow.Flow

@Dao
interface PairDao {
    @Query("SELECT * FROM pairs WHERE id = :pairId")
    fun getPairFlow(pairId: String): Flow<Pair?>

    @Query("SELECT * FROM pairs WHERE id = :pairId")
    suspend fun getPair(pairId: String): Pair?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPair(pair: Pair)

    @Update
    suspend fun updatePair(pair: Pair)

    @Delete
    suspend fun deletePair(pair: Pair)
}
