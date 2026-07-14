package com.example.noteshare.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * Upload a file to Firebase Storage and return the download URL.
     */
    suspend fun uploadImage(
        pairId: String,
        fileName: String,
        fileUri: Uri
    ): String {
        val ref = storage.reference
            .child("pairs/$pairId/images/$fileName")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Delete an image from Firebase Storage.
     */
    suspend fun deleteImage(imageUrl: String) {
        try {
            val ref = storage.getReferenceFromUrl(imageUrl)
            ref.delete().await()
        } catch (e: Exception) {
            // Image might already be deleted
        }
    }

    /**
     * Upload a profile photo.
     */
    suspend fun uploadProfilePhoto(userId: String, fileUri: Uri): String {
        val ref = storage.reference
            .child("users/$userId/profile.jpg")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }
}
