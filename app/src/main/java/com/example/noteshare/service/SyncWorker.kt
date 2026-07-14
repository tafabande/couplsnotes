package com.example.noteshare.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.noteshare.data.local.db.dao.OutboxDao
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.data.repository.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.example.noteshare.data.model.SystemEvent
import com.example.noteshare.data.model.EventType
import com.example.noteshare.data.model.Note
import com.example.noteshare.data.model.MoodEntry

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val outboxDao: OutboxDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val noteRepository: NoteRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // STEP 1: Upstream Sync (Drain Local Outbox to Remote Server)
            val pendingEvents = outboxDao.getPendingEvents()
            for (event in pendingEvents) {
                val success = pushToFirestore(event.action, event.payload)
                if (success) {
                    outboxDao.deleteEvent(event)
                } else {
                    // Exponential backoff will be handled by WorkManager retry policy
                    return@withContext Result.retry()
                }
            }

            // STEP 2 & 3: Downstream Sync & Local Commit
            // Normally we'd query notes updated since lastSyncTime. 
            // For now, we delegate to the repository to fetch and update local state.
            noteRepository.syncDownstream()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun pushToFirestore(action: String, payload: String): Boolean {
        try {
            val gson = Gson()
            val event = gson.fromJson(payload, SystemEvent::class.java)
            
            when (event.eventType) {
                EventType.CREATE_NOTE -> {
                    val noteJson = gson.toJson(event.payload)
                    val note = gson.fromJson(noteJson, Note::class.java)
                    firestoreDataSource.addNote(event.pairId, note)
                }
                EventType.UPDATE_NOTE -> {
                    val noteId = event.payload["id"] as String
                    firestoreDataSource.updateNote(event.pairId, noteId, event.payload)
                }
                EventType.DELETE_NOTE -> {
                    val noteId = event.payload["id"] as String
                    firestoreDataSource.deleteNote(event.pairId, noteId)
                }
                EventType.MOOD_CHANGED -> {
                    val moodJson = gson.toJson(event.payload)
                    val mood = gson.fromJson(moodJson, MoodEntry::class.java)
                    firestoreDataSource.addMood(event.pairId, mood)
                }
                else -> {
                    // Handle other events
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
