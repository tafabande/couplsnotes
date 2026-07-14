package com.example.noteshare.data.remote

import com.example.noteshare.data.model.*
import com.example.noteshare.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    // ═══════════════════════════════════
    // Users
    // ═══════════════════════════════════
    suspend fun createUser(user: User) {
        db.collection(Constants.COLLECTION_USERS)
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun getUser(userId: String): User? {
        return db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, fields: Map<String, Any?>) {
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .update(fields)
            .await()
    }

    // ═══════════════════════════════════
    // Pairs
    // ═══════════════════════════════════
    suspend fun createPair(pair: Pair): String {
        val docRef = db.collection(Constants.COLLECTION_PAIRS)
            .add(pair)
            .await()
        return docRef.id
    }

    suspend fun getPair(pairId: String): Pair? {
        return db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .get()
            .await()
            .toObject(Pair::class.java)
    }

    suspend fun findPairByInviteCode(code: String): Pair? {
        val result = db.collection(Constants.COLLECTION_PAIRS)
            .whereEqualTo("inviteCode", code)
            .whereEqualTo("status", Constants.PAIR_STATUS_PENDING)
            .get()
            .await()
        return result.documents.firstOrNull()?.toObject(Pair::class.java)
    }

    suspend fun updatePair(pairId: String, fields: Map<String, Any?>) {
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .update(fields)
            .await()
    }

    fun observePair(pairId: String): Flow<Pair?> = callbackFlow {
        val listener = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Pair::class.java))
            }
        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════
    // Notes
    // ═══════════════════════════════════
    suspend fun addNote(pairId: String, note: Note): String {
        val docRef = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_NOTES)
            .add(note)
            .await()
        return docRef.id
    }

    suspend fun updateNote(pairId: String, noteId: String, fields: Map<String, Any?>) {
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_NOTES)
            .document(noteId)
            .update(fields)
            .await()
    }

    suspend fun deleteNote(pairId: String, noteId: String) {
        // Soft delete: set deletedAt timestamp
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_NOTES)
            .document(noteId)
            .update("deletedAt", System.currentTimeMillis())
            .await()
    }

    fun observeNotes(pairId: String): Flow<List<Note>> = callbackFlow {
        val listener = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_NOTES)
            .whereEqualTo("deletedAt", null)
            .orderBy("isPinned", Query.Direction.DESCENDING)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull {
                    it.toObject(Note::class.java)
                } ?: emptyList()
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addNoteVersion(pairId: String, noteId: String, version: NoteVersion) {
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_NOTES)
            .document(noteId)
            .collection(Constants.COLLECTION_VERSIONS)
            .add(version)
            .await()
    }

    // ═══════════════════════════════════
    // Moods
    // ═══════════════════════════════════
    suspend fun addMood(pairId: String, mood: MoodEntry): String {
        val docRef = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_MOODS)
            .add(mood)
            .await()
        return docRef.id
    }

    fun observeMoods(pairId: String): Flow<List<MoodEntry>> = callbackFlow {
        val listener = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_MOODS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val moods = snapshot?.documents?.mapNotNull {
                    it.toObject(MoodEntry::class.java)
                } ?: emptyList()
                trySend(moods)
            }
        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════
    // Events
    // ═══════════════════════════════════
    suspend fun addEvent(pairId: String, event: Event): String {
        val docRef = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_EVENTS)
            .add(event)
            .await()
        return docRef.id
    }

    suspend fun updateEvent(pairId: String, eventId: String, fields: Map<String, Any?>) {
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_EVENTS)
            .document(eventId)
            .update(fields)
            .await()
    }

    suspend fun deleteEvent(pairId: String, eventId: String) {
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_EVENTS)
            .document(eventId)
            .delete()
            .await()
    }

    fun observeEvents(pairId: String): Flow<List<Event>> = callbackFlow {
        val listener = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_EVENTS)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull {
                    it.toObject(Event::class.java)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════
    // Memories
    // ═══════════════════════════════════
    suspend fun addMemory(pairId: String, memory: Memory): String {
        val docRef = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_MEMORIES)
            .add(memory)
            .await()
        return docRef.id
    }

    fun observeMemories(pairId: String): Flow<List<Memory>> = callbackFlow {
        val listener = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_MEMORIES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val memories = snapshot?.documents?.mapNotNull {
                    it.toObject(Memory::class.java)
                } ?: emptyList()
                trySend(memories)
            }
        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════
    // Daily Questions
    // ═══════════════════════════════════
    suspend fun addQuestion(pairId: String, question: DailyQuestion): String {
        val docRef = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_QUESTIONS)
            .add(question)
            .await()
        return docRef.id
    }

    suspend fun answerQuestion(
        pairId: String,
        questionId: String,
        field: String,
        answer: String
    ) {
        db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_QUESTIONS)
            .document(questionId)
            .update(field, answer)
            .await()
    }

    fun observeQuestions(pairId: String): Flow<List<DailyQuestion>> = callbackFlow {
        val listener = db.collection(Constants.COLLECTION_PAIRS)
            .document(pairId)
            .collection(Constants.COLLECTION_QUESTIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val questions = snapshot?.documents?.mapNotNull {
                    it.toObject(DailyQuestion::class.java)
                } ?: emptyList()
                trySend(questions)
            }
        awaitClose { listener.remove() }
    }
}
