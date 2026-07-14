package com.example.noteshare

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.noteshare.data.remote.AuthDataSource
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.service.SyncWorker
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class NoteShareApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var authDataSource: AuthDataSource

    @Inject
    lateinit var firestoreDataSource: FirestoreDataSource

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Firebase is auto-initialized via google-services plugin
        setupPeriodicSync()
        registerFcmToken()
        triggerImmediateSync()
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeriodicSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Register the current FCM token to Firestore on every app start.
     * This ensures token rotation and new installs are always captured.
     */
    private fun registerFcmToken() {
        val userId = authDataSource.currentUserId ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            appScope.launch {
                try {
                    firestoreDataSource.addFcmToken(userId, token)
                    Log.d("NoteShareApp", "FCM token registered on startup")
                } catch (e: Exception) {
                    Log.e("NoteShareApp", "Failed to register FCM token", e)
                }
            }
        }
    }

    /**
     * Trigger an immediate one-shot sync on cold start to catch up
     * on any changes that happened while the app was closed.
     */
    private fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(syncRequest)
    }
}
