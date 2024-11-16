package com.example.mobappprototype

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mobappprototype.ui.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelId = "notification_channel"
const val channelName = "com.example.mobappprototype"

private const val TAG = "MyFirebaseMessagingService"
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            val senderName = remoteMessage.data["senderName"] ?: ""
            val messageContent = remoteMessage.data["messageContent"] ?: ""
            val meetingId = remoteMessage.data["meetingId"] ?: ""

            generateNotification(senderName, messageContent, meetingId)
        }
    }

    fun generateNotification(senderName: String, messageContent: String, meetingId: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("meetingId", meetingId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_app_logo)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        // Use the custom layout
        val customContentView = RemoteViews(packageName, R.layout.notification)
        customContentView.setTextViewText(R.id.notificationTitle, senderName)
        customContentView.setTextViewText(R.id.notificationMessage, messageContent)
        builder = builder.setContent(customContentView)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, builder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Store the token in Firestore
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            FirebaseFirestore.getInstance().collection("users").document(userUid)
                .update("fcmToken", token)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "FCM token saved to Firestore")
                    } else {
                        Log.e(TAG, "Error saving FCM token to Firestore", task.exception)
                    }
                }
        }
    }
}