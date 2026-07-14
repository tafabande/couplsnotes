package com.example.noteshare.util

/**
 * Represents the current synchronization state of the app.
 */
sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data object Synced : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Represents the user's pairing state in the app.
 */
sealed class UserState {
    data object Loading : UserState()
    data object LoggedOut : UserState()
    data object NewUser : UserState()
    data object WaitingForPartner : UserState()
    data object Paired : UserState()
    data object Offline : UserState()
}

/**
 * Generic result wrapper for repository operations.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
