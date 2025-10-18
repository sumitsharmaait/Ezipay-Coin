package com.app.ezipaycoin.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.app.ezipaycoin.R
import com.app.ezipaycoin.presentation.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle messages when the app is in foreground
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "Notification", it.body ?: "")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your backend if needed
        println("🔥 FCM Token: $token")
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_ezipay_coin_small)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O+
        val channel = NotificationChannel(
            channelId,
            "General Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }


}