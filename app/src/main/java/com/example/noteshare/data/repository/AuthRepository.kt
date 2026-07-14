package com.example.noteshare.data.repository

import com.example.noteshare.data.model.User
import com.example.noteshare.data.remote.AuthDataSource
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.util.Result
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) {
    val currentUser: FirebaseUser?
        get() = authDataSource.currentUser

    val isLoggedIn: Boolean
        get() = authDataSource.isLoggedIn

    val currentUserId: String?
        get() = authDataSource.currentUserId

    val authState: Flow<FirebaseUser?>
        get() = authDataSource.authState

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signInWithGoogle(idToken)
                ?: return Result.Error("Google sign-in failed")

            // Check if user exists in Firestore, create if not
            var user = firestoreDataSource.getUser(firebaseUser.uid)
            if (user == null) {
                user = User(
                    id = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                firestoreDataSource.createUser(user)
            }
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Sign-in failed: ${e.message}", e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signInWithEmail(email, password)
                ?: return Result.Error("Email sign-in failed")

            val user = firestoreDataSource.getUser(firebaseUser.uid)
                ?: return Result.Error("User profile not found")

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Sign-in failed: ${e.message}", e)
        }
    }

    suspend fun createAccount(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val firebaseUser = authDataSource.createAccount(email, password)
                ?: return Result.Error("Account creation failed")

            val user = User(
                id = firebaseUser.uid,
                displayName = displayName,
                email = email
            )
            firestoreDataSource.createUser(user)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Registration failed: ${e.message}", e)
        }
    }

    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val userId = authDataSource.currentUserId
                ?: return Result.Error("Not logged in")
            val user = firestoreDataSource.getUser(userId)
                ?: return Result.Error("User profile not found")
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error("Failed to get profile: ${e.message}", e)
        }
    }

    suspend fun updateProfile(fields: Map<String, Any?>): Result<Unit> {
        return try {
            val userId = authDataSource.currentUserId
                ?: return Result.Error("Not logged in")
            firestoreDataSource.updateUser(userId, fields)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update profile: ${e.message}", e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            authDataSource.sendPasswordReset(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to send reset email: ${e.message}", e)
        }
    }

    fun signOut() {
        authDataSource.signOut()
    }
}
