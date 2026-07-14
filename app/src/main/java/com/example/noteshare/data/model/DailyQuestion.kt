package com.example.noteshare.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a daily question prompt for the couple.
 */
data class DailyQuestion(
    @DocumentId
    val id: String = "",
    val pairId: String = "",
    val text: String = "",
    val askedBy: String = "",
    val answeredByUser1: String? = null,
    val answeredByUser2: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isAnsweredBy(userId: String, user1Id: String): Boolean {
        return if (userId == user1Id) answeredByUser1 != null
        else answeredByUser2 != null
    }

    fun getAnswer(userId: String, user1Id: String): String? {
        return if (userId == user1Id) answeredByUser1 else answeredByUser2
    }

    val isBothAnswered: Boolean
        get() = answeredByUser1 != null && answeredByUser2 != null
}
