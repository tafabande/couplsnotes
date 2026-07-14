package com.example.noteshare.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.noteshare.data.local.db.dao.EventDao
import com.example.noteshare.data.local.db.dao.MoodDao
import com.example.noteshare.data.local.db.dao.NoteDao
import com.example.noteshare.data.local.db.dao.OutboxDao
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.data.repository.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.example.noteshare.data.model.SystemEvent
import com.example.noteshare.data.model.EventType
import com.example.noteshare.data.model.Note
import com.example.noteshare.data.model.MoodEntry
import com.example.noteshare.data.model.Event
import com.example.noteshare.data.model.SyncStatus

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val outboxDao: OutboxDao,
    private val noteDao: NoteDao,
    private val moodDao: MoodDao,
    private val eventDao: EventDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val noteRepository: NoteRepository,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private val KEY_LAST_SYNC = longPreferencesKey("last_sync_timestamp")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // ───────────────────────────────────
            // STEP 1: Upstream Sync — Drain Local Outbox to Firestore
            // ───────────────────────────────────
            val pendingEvents = outboxDao.getPendingEvents()
            for (event in pendingEvents) {
                val entityId = pushToFirestore(event.action, event.payload)
                if (entityId != null) {
                    outboxDao.deleteEvent(event)
                    // Mark the source entity as SYNCED in Room
                    markEntitySynced(event.action, entityId)
                } else {
                    // Exponential backoff handled by WorkManager retry policy
                    return@withContext Result.retry()
                }
            }

            // ───────────────────────────────────
            // STEP 2: Downstream Sync — Pull remote changes into Room
            // ───────────────────────────────────
            val pairId = inputData.getString("pairId")
            if (pairId != null) {
                val lastSync = dataStore.data.first()[KEY_LAST_SYNC] ?: 0L
                val now = System.currentTimeMillis()

                // Sync notes (with version conflict resolution)
                noteRepository.syncDownstream(pairId, lastSync)

                // Sync moods
                try {
                    val remoteMoods = firestoreDataSource.getMoodsUpdatedSince(pairId, lastSync)
                    for (mood in remoteMoods) {
                        moodDao.insertMood(mood.copy(syncStatus = SyncStatus.SYNCED))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Sync events
                try {
                    val remoteEvents = firestoreDataSource.getEventsUpdatedSince(pairId, lastSync)
                    for (event in remoteEvents) {
                        val localEvent = eventDao.getEventById(event.id)
                        if (localEvent == null || event.version >= localEvent.version) {
                            eventDao.insertEvent(event.copy(syncStatus = SyncStatus.SYNCED))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Update last sync timestamp
                dataStore.edit { prefs ->
                    prefs[KEY_LAST_SYNC] = now
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Push a single outbox event to Firestore.
     * Returns the entity ID if successful, null on failure.
     */
    private suspend fun pushToFirestore(action: String, payload: String): String? {
        try {
            val gson = Gson()
            val event = gson.fromJson(payload, SystemEvent::class.java)
            
            when (event.eventType) {
                EventType.CREATE_NOTE -> {
                    val noteJson = gson.toJson(event.payload["note"])
                    val note = gson.fromJson(noteJson, Note::class.java)
                    firestoreDataSource.addNote(event.pairId, note)
                    return note.id
                }
                EventType.UPDATE_NOTE -> {
                    val noteId = event.payload["id"] as String
                    firestoreDataSource.updateNote(event.pairId, noteId, event.payload)
                    return noteId
                }
                EventType.DELETE_NOTE -> {
                    val noteId = event.payload["id"] as String
                    firestoreDataSource.deleteNote(event.pairId, noteId)
                    return noteId
                }
                EventType.MOOD_CHANGED -> {
                    val moodJson = gson.toJson(event.payload["mood"])
                    val mood = gson.fromJson(moodJson, MoodEntry::class.java)
                    firestoreDataSource.addMood(event.pairId, mood)
                    return mood.id
                }
                EventType.CALENDAR_REMINDER -> {
                    val eventJson = gson.toJson(event.payload["event"])
                    val calEvent = gson.fromJson(eventJson, Event::class.java)
                    firestoreDataSource.addEvent(event.pairId, calEvent)
                    return calEvent.id
                }
                else -> {
                    // Unhandled event types — just remove from outbox
                    return "unknown"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Mark the source Room entity as SYNCED after successful Firestore push.
     */
    private suspend fun markEntitySynced(action: String, entityId: String) {
        try {
            when {
                action.contains("NOTE") -> noteDao.markSynced(entityId)
                action.contains("MOOD") -> moodDao.markSynced(entityId)
                action.contains("EVENT") -> eventDao.markSynced(entityId)
            }
        } catch (e: Exception) {
            // Non-critical — entity will be marked on next sync
            e.printStackTrace()
        }
    }
}
