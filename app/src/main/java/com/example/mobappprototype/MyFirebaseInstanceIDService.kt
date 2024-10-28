package com.example.mobappprototype

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService

private const val TAG = "MyFirebaseInstanceIDService"
class MyFirebaseInstanceIDService : FirebaseMessagingService() {
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