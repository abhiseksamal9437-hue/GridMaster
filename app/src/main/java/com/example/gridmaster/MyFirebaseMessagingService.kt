package com.example.gridmaster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.gridmaster.MainActivity
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. Check if the message contains data
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "GridMaster Alert"
            val body = remoteMessage.data["body"] ?: "New event logged."
            showNotification(title, body)
        }

        // 2. Check if the message contains a notification payload
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Alert", it.body ?: "New Event")
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "grid_alerts"
        val notificationId = Random.nextInt()

        // Intent to open the app when clicking the notification
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_grid_logo) // Uses your new logo
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel (Required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Grid Fault Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Grid Trips and Critical Maintenance"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        // If you ever need to target a specific user, you'd save this token to Firestore.
        // For now, we are using Topic Messaging, so we don't strictly need to save it.
        super.onNewToken(token)
    }
}