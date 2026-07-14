package com.example.noteshare.data.model

/**
 * Represents the local synchronization state of a database entity.
 */
enum class SyncStatus {
    /** Entity is strictly local and has not been uploaded to the server yet. */
    PENDING,
    /** Entity is currently successfully synchronized with the remote server. */
    SYNCED,
    /** Entity has been marked for deletion locally but not yet propagated to the server. */
    DELETED_LOCALLY
}
