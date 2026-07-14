package com.example.noteshare.data.local.db.dao

import androidx.room.*
import com.example.noteshare.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE pairId = :pairId AND deletedAt IS NULL ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveNotes(pairId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE pairId = :pairId AND deletedAt IS NULL AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getVisibleNotes(pairId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE pairId = :pairId AND isArchived = 1 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getArchivedNotes(pairId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE pairId = :pairId AND isPinned = 1 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getPinnedNotes(pairId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeNoteById(noteId: String): Flow<Note?>

    @Query("SELECT * FROM notes WHERE pairId = :pairId AND deletedAt IS NULL ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentNotes(pairId: String, limit: Int = 5): List<Note>

    @Query("SELECT * FROM notes WHERE pairId = :pairId AND deletedAt IS NULL AND title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(pairId: String, query: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE pairId = :pairId")
    suspend fun deleteAllForPair(pairId: String)

    @Query("SELECT COUNT(*) FROM notes WHERE pairId = :pairId AND deletedAt IS NULL")
    suspend fun getNoteCount(pairId: String): Int

    @Query("UPDATE notes SET syncStatus = 'SYNCED' WHERE id = :noteId")
    suspend fun markSynced(noteId: String)

    @Query("SELECT * FROM notes WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedNotes(): List<Note>
}
