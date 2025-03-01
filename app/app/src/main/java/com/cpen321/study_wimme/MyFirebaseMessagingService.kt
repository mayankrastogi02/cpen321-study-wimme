package com.cpen321.study_wimme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message recieved: ${remoteMessage.notification}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Data: ${remoteMessage.data}")
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, body, Toast.LENGTH_SHORT).show()
            }
        }
        //Un-comment if we want notification to pop up in task bar when app is in the foreground
//        remoteMessage.notification?.let {
//            showNotification(it.title, it.body)
//        }
    }

    private fun showNotification(title: String?, message: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "study_wimme_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Study Wimme",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.studywimme_logo)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }
}