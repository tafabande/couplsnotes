package com.example.noteshare.repository

import com.example.noteshare.config.EnvironmentConfig
import com.example.noteshare.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

open class NoteRepository(private val db: FirebaseFirestore? = null) {

    private val activeDb: FirebaseFirestore
        get() = db ?: FirebaseFirestore.getInstance()

    open fun getNotes(
        onSuccess: (List<Note>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        activeDb.collection(EnvironmentConfig.firestoreCollection)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val notes = result.mapNotNull { document ->
                    document.toObject(Note::class.java)
                }
                onSuccess(notes)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    open fun addNote(
        note: Note,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        activeDb.collection(EnvironmentConfig.firestoreCollection)
            .add(note)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    open fun deleteNote(
        noteId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        activeDb.collection(EnvironmentConfig.firestoreCollection)
            .document(noteId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
