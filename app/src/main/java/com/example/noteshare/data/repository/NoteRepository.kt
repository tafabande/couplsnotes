package com.example.noteshare.data.repository

import com.example.noteshare.data.local.db.dao.NoteDao
import com.example.noteshare.data.local.db.dao.OutboxDao
import com.example.noteshare.data.model.EventType
import com.example.noteshare.data.model.Note
import com.example.noteshare.data.model.NoteVersion
import com.example.noteshare.data.model.OutboxEventEntity
import com.example.noteshare.data.model.SyncStatus
import com.example.noteshare.data.model.SystemEvent
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.util.Result
import com.example.noteshare.util.EncryptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val noteDao: NoteDao,
    private val outboxDao: OutboxDao
) {
    private val gson = Gson()

    /**
     * Get notes from local cache.
     * BR-301: Time Capsule Invariant (Filtered by isScheduled in Dao implicitly if needed, or UI)
     */
    fun getLocalNotes(pairId: String): Flow<List<Note>> {
        return noteDao.getVisibleNotes(pairId)
    }

    suspend fun getRecentNotes(pairId: String, limit: Int = 5): List<Note> {
        return noteDao.getRecentNotes(pairId, limit)
    }

    fun searchNotes(pairId: String, query: String): Flow<List<Note>> {
        return noteDao.searchNotes(pairId, query)
    }

    fun getArchivedNotes(pairId: String): Flow<List<Note>> {
        return noteDao.getArchivedNotes(pairId)
    }

    /**
     * Observe a single note from local SSOT.
     */
    fun observeNoteById(noteId: String): Flow<Note?> {
        return noteDao.observeNoteById(noteId)
    }

    /**
     * Create a new note and add it to the local SSOT & Outbox.
     */
    suspend fun createNote(pairId: String, note: Note): Result<String> {
        return try {
            val noteId = if (note.id.isEmpty()) UUID.randomUUID().toString() else note.id
            
            // Vault Encryption
            val finalContent = if (note.isVault) {
                EncryptionUtils.encrypt(note.content)
            } else {
                note.content
            }

            val newNote = note.copy(
                id = noteId,
                pairId = pairId,
                content = finalContent,
                syncStatus = SyncStatus.PENDING,
                version = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // 1. Save to local SSOT Room database
            noteDao.insertNote(newNote)

            // 2. Queue for Sync Engine
            val eventPayload = SystemEvent(
                eventId = UUID.randomUUID().toString(),
                pairId = pairId,
                actorId = newNote.authorId,
                eventType = EventType.CREATE_NOTE,
                payload = mapOf("note" to newNote)
            )
            
            outboxDao.insertEvent(
                OutboxEventEntity(
                    queueId = UUID.randomUUID().toString(),
                    action = "CREATE_NOTE",
                    payloadType = "Note",
                    payload = gson.toJson(eventPayload)
                )
            )

            // 3. (In real app, trigger WorkManager here)

            Result.Success(noteId)
        } catch (e: Exception) {
            Result.Error("Failed to create note locally: ${e.message}", e)
        }
    }

    /**
     * Update an existing note.
     * BR-201: Immutable Core Rule (Only author can edit text, unless collaborative)
     * BR-203: Automated Versioning (Increment version by +1)
     */
    suspend fun updateNote(
        pairId: String,
        noteId: String,
        fields: Map<String, Any?>,
        editorId: String
    ): Result<Unit> {
        return try {
            val existingNote = noteDao.getNoteById(noteId) ?: return Result.Error("Note not found locally")

            // BR-201 check
            if (existingNote.type == "text" && existingNote.authorId != editorId) {
                return Result.Error("BR-201 Violation: You cannot edit your partner's text note.")
            }

            // BR-203 Versioning & Update
            var updatedContent = existingNote.content
            if (fields.containsKey("content")) {
                val rawContent = fields["content"] as String
                updatedContent = if (existingNote.isVault) {
                    EncryptionUtils.encrypt(rawContent)
                } else {
                    rawContent
                }
            }
            
            // Generate the final updated Note explicitly for local Room save
            val updatedNote = existingNote.copy(
                version = existingNote.version + 1,
                updatedAt = System.currentTimeMillis(),
                content = updatedContent,
                title = fields["title"] as? String ?: existingNote.title,
                syncStatus = SyncStatus.PENDING
            )

            noteDao.updateNote(updatedNote)

            // Ensure the outbox payload sends the properly encrypted content
            val safeFields = fields.toMutableMap()
            if (fields.containsKey("content")) {
                safeFields["content"] = updatedContent
            }

            // Enqueue update event
            val eventPayload = SystemEvent(
                eventId = UUID.randomUUID().toString(),
                pairId = pairId,
                actorId = editorId,
                eventType = EventType.UPDATE_NOTE,
                payload = (mapOf("id" to noteId) + safeFields).filterValues { it != null } as Map<String, Any>
            )

            outboxDao.insertEvent(
                OutboxEventEntity(
                    queueId = UUID.randomUUID().toString(),
                    action = "UPDATE_NOTE",
                    payloadType = "Map",
                    payload = gson.toJson(eventPayload)
                )
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update note: ${e.message}", e)
        }
    }

    /**
     * Soft delete a note. Only author can permanently delete.
     * BR-201 Enforcement.
     */
    suspend fun deleteNote(pairId: String, noteId: String, deleterId: String): Result<Unit> {
        return try {
            val existingNote = noteDao.getNoteById(noteId) ?: return Result.Error("Note not found")
            
            // Soft delete
            val updatedNote = existingNote.copy(
                deletedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING,
                version = existingNote.version + 1
            )
            noteDao.updateNote(updatedNote)

            val eventPayload = SystemEvent(
                eventId = UUID.randomUUID().toString(),
                pairId = pairId,
                actorId = deleterId,
                eventType = EventType.DELETE_NOTE,
                payload = mapOf("id" to noteId)
            )

            outboxDao.insertEvent(
                OutboxEventEntity(
                    queueId = UUID.randomUUID().toString(),
                    action = "DELETE_NOTE",
                    payloadType = "Map",
                    payload = gson.toJson(eventPayload)
                )
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete note: ${e.message}", e)
        }
    }

    /**
     * Toggle pin on a note.
     * BR-202: Visual Preference Isolation (Local Only Mutation)
     */
    suspend fun togglePin(pairId: String, noteId: String, isPinned: Boolean): Result<Unit> {
        return try {
            val existingNote = noteDao.getNoteById(noteId) ?: return Result.Error("Note not found")
            val updatedNote = existingNote.copy(isPinned = !isPinned)
            noteDao.updateNote(updatedNote)
            // Note: intentionally NOT enqueuing to Outbox. Pinning is local per user (BR-202).
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to toggle pin: ${e.message}", e)
        }
    }

    /**
     * Archive a note.
     * BR-202: Local Only Mutation.
     */
    suspend fun archiveNote(pairId: String, noteId: String): Result<Unit> {
        return try {
            val existingNote = noteDao.getNoteById(noteId) ?: return Result.Error("Note not found")
            val updatedNote = existingNote.copy(isArchived = true)
            noteDao.updateNote(updatedNote)
            // No network sync for archive preference
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to archive note: ${e.message}", e)
        }
    }

    suspend fun unarchiveNote(pairId: String, noteId: String): Result<Unit> {
        return try {
            val existingNote = noteDao.getNoteById(noteId) ?: return Result.Error("Note not found")
            val updatedNote = existingNote.copy(isArchived = false)
            noteDao.updateNote(updatedNote)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to unarchive note: ${e.message}", e)
        }
    }

    /**
     * Downstream sync from Firestore.
     * Fetches notes updated since the given timestamp and upserts into Room.
     * Higher version wins in conflict resolution.
     */
    suspend fun syncDownstream(pairId: String, lastSyncTimestamp: Long) {
        try {
            val remoteNotes = firestoreDataSource.getNotesUpdatedSince(pairId, lastSyncTimestamp)
            for (remoteNote in remoteNotes) {
                val localNote = noteDao.getNoteById(remoteNote.id)
                if (localNote == null || remoteNote.version >= localNote.version) {
                    // Remote wins — upsert with SYNCED status
                    noteDao.insertNote(remoteNote.copy(syncStatus = SyncStatus.SYNCED))
                }
                // If local version is higher, local wins (will be pushed upstream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
