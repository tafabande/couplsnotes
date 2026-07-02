package com.example.noteshare.model

import com.google.firebase.firestore.DocumentId

data class Note(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
