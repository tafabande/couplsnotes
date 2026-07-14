package com.example.noteshare.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.noteshare.MainActivity
import com.example.noteshare.R
import com.example.noteshare.data.remote.AuthDataSource
import com.example.noteshare.data.remote.FirestoreDataSource
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {

    @Inject lateinit var firestoreDataSource: FirestoreDataSource
    @Inject lateinit var authDataSource: AuthDataSource

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NotificationService", "New FCM token received")
        // Persist the token to Firestore so Cloud Functions can target this device
        val userId = authDataSource.currentUserId ?: return
        serviceScope.launch {
            try {
                firestoreDataSource.addFcmToken(userId, token)
                Log.d("NotificationService", "FCM token saved to Firestore for user $userId")
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to save FCM token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: message.data["title"] ?: "New Note"
        val body = message.notification?.body ?: message.data["body"] ?: "Your partner sent something."
        val type = message.data["type"] ?: "general"

        showNotification(title, body, type)
    }

    private fun showNotification(title: String, message: String, type: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "noteshare_channel"
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NoteShare Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from your partner"
            }
            manager.createNotificationChannel(channel)
        }

        manager.notify(Random.nextInt(), builder.build())
    }
}
