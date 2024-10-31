package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.ChatAdapter
import com.example.mobappprototype.databinding.ActivityChatBinding
import com.example.mobappprototype.model.ChatMessage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private const val TAG = "ChatActivity"
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val participantDetailsMap = mutableMapOf<String, Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        chatAdapter = ChatAdapter(messages)

        binding.rvChatMessages.layoutManager = LinearLayoutManager(this)
        binding.rvChatMessages.adapter = chatAdapter


        val userUid = auth.currentUser?.uid

        binding.linearLayout6.post {
            val rect = Rect()
            binding.btnHome.getHitRect(rect)
            rect.inset(-50, -50) // Expand the touch area by 50 pixels on each side
            binding.linearLayout6.touchDelegate = TouchDelegate(rect, binding.btnHome)
        }

        binding.btnHome.setOnClickListener{
            Intent(this, InboxActivity::class.java).also {
                startActivity(it)
            }
        }


        val meetingId = intent.getStringExtra("meetingId")
        if (meetingId != null) {
            fetchMessages(meetingId)
            val userRef = firestoreDb.collection("users").document(userUid!!)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Reset the unread count for this meeting
                    val updatedLastSeenData = mapOf("unreadCount" to 0, "timestamp" to FieldValue.serverTimestamp()) // Change here
                    userRef.update("lastSeenChats.${meetingId}", updatedLastSeenData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully updated last seen chat time for meeting: $meetingId")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating last seen chat time", e)
                        }
                }
            }
            val updates = hashMapOf(
                "lastSeenChats.${meetingId}.unreadCount" to 0,
                "lastSeenChats.${meetingId}.timestamp" to FieldValue.serverTimestamp()
            )
            userRef.update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully reset unread count for meeting: $meetingId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error resetting unread count", e)
                }

            binding.btnSendMessage.setOnClickListener {
                val messageContent = binding.etMessage.text.toString()
                if (messageContent.isNotBlank()) {
                    sendMessage(meetingId, messageContent)
                }
            }
            firestoreDb.collection("meetings").document(meetingId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val subject = document.getString("subject")
                        val branch = document.getString("branch")
                        binding.tvTutorNameTitle.text = "$subject - $branch"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting meeting details", e)
                }
        } else {
            Log.e(TAG, "Meeting ID not found in Intent")
        }
    }

    private fun fetchMessages(meetingId: String) {
        Log.d(TAG, "Starting fetchMessages for meetingId: $meetingId") // Log entry point

        binding.rvChatMessages.adapter = chatAdapter

        val messagesFetchTask = firestoreDb.collection("chats")
            .document(meetingId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    snapshot?.documents?.forEach { document ->
                        val message = document.toObject(ChatMessage::class.java)
                        messages.add(message!!)
                    }
                    Log.d(TAG, "Messages fetched successfully: $messages") // Log fetched messages
                } else {
                    Log.e(TAG, "Error fetching messages", task.exception)
                }
                Tasks.forResult(null)
            }

        val participantDetailsTask = fetchParticipantDetails(meetingId)

        Tasks.whenAll(messagesFetchTask, participantDetailsTask).addOnSuccessListener {
            runOnUiThread {
                Log.d(TAG, "Both messages and participant details fetched") // Log completion of both tasks

                chatAdapter.updateParticipantDetails(participantDetailsMap)

                chatAdapter.notifyDataSetChanged()

                binding.rvChatMessages.postDelayed({
                    binding.rvChatMessages.scrollToPosition(messages.size - 1)
                    
                    runOnUiThread {
                        binding.loadingLayout.visibility = View.GONE
                        binding.layoutMainActivity.visibility = View.VISIBLE
                    }
                }, 1)
            }
        }
    }

    private fun fetchParticipantDetails(meetingId: String): Task<Map<String, Pair<String, String>>> {
        Log.d(TAG, "Starting fetchParticipantDetails for meetingId: $meetingId")

        return firestoreDb.collection("meetings").document(meetingId)
            .get()
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val participants = document.get("participants") as? List<String> ?: emptyList()
                        Log.d(TAG, "Participants found: $participants")

                        val participantDetailsMap = mutableMapOf<String, Pair<String, String>>()
                        val participantDetailsTasks = participants.map { participantId ->
                            Log.d(TAG, "Fetching details for participantId: $participantId")

                            firestoreDb.collection("users").document(participantId)
                                .get()
                                .continueWith { userTask ->
                                    if (userTask.isSuccessful) {
                                        val userDoc = userTask.result
                                        if (userDoc != null && userDoc.exists()) {
                                            val profilePicUrl = userDoc.getString("profilePic") ?: ""
                                            val senderFullName = userDoc.getString("fullName") ?: "Unknown User"
                                            participantDetailsMap[participantId] = Pair(senderFullName, profilePicUrl)
                                            Log.d(TAG, "Participant details fetched successfully for $participantId: $senderFullName, $profilePicUrl")
                                        } else {
                                            Log.w(TAG, "Participant details not found for ID: $participantId")
                                        }
                                    } else {
                                        Log.e(TAG, "Error fetching participant details for ID: $participantId", userTask.exception)
                                    }
                                }
                        }

                        // Return the map after all tasks are complete
                        Tasks.whenAll(participantDetailsTasks).continueWith {
                            this.participantDetailsMap.clear()
                            this.participantDetailsMap.putAll(participantDetailsMap)
                            Log.d(TAG, "All participant details fetched and map updated: $participantDetailsMap")
                            this.participantDetailsMap // Return the map
                        }
                    } else {
                        Log.w(TAG, "Meeting document not found for ID: $meetingId")
                        Tasks.forResult(mutableMapOf()) // Return an empty map
                    }
                } else {
                    Log.e(TAG, "Error fetching meeting participants", task.exception)
                    Tasks.forResult(mutableMapOf()) // Return an empty map
                }
            }
    }

    private fun sendMessage(meetingId: String, messageContent: String) {
        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            val message = ChatMessage(
                senderUID = userUID,
                content = messageContent.trim(),
            )

            firestoreDb.collection("chats")
                .document(meetingId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener {
                    Log.d(TAG, "Message sent successfully")
                    binding.etMessage.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error sending message", e)
                }
            val currentUserId = auth.currentUser?.uid ?: return
            firestoreDb.collection("chats").document(meetingId)
                .get()
                .addOnSuccessListener { chatDocument ->
                    if (chatDocument.exists()) {
                        val participants = chatDocument.get("participants") as? List<String> ?: emptyList()
                        val otherParticipants = participants.toMutableList().also { it.remove(currentUserId) }

                        for (participantId in otherParticipants) {
                            incrementUnreadCountForParticipant(participantId, meetingId)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting chat room participants", exception)
                }
            getCurrentUserName(currentUserId) { currentUserName ->
                sendNotificationToParticipants(meetingId, messageContent, currentUserName)
            }
        }
    }

    private fun getCurrentUserName(userId: String, callback: (String) -> Unit) {
        firestoreDb.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val fullName = document.getString("fullName") ?: "Unknown User"
                    callback(fullName)
                } else {
                    callback("Unknown User")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user's full name", e)
                callback("Unknown User")
            }
    }

    private fun incrementUnreadCountForParticipant(participantId: String, meetingId: String) {
        val userRef = firestoreDb.collection("users").document(participantId)

        firestoreDb.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)

            if (userDoc.exists()) {
                val lastSeenChats = userDoc.get("lastSeenChats") as? MutableMap<String, Any> ?: mutableMapOf()
                val meetingData = lastSeenChats[meetingId] as? MutableMap<String, Any>

                if (meetingData != null) {
                    val currentUnreadCount = meetingData["unreadCount"] as? Long ?: 0
                    meetingData["unreadCount"] = currentUnreadCount + 1
                } else {
                    lastSeenChats[meetingId] = mapOf("unreadCount" to 1)
                }

                transaction.update(userRef, "lastSeenChats", lastSeenChats)
            } else {
                // User document doesn't exist, create lastSeenChats with initial unread count
                val lastSeenChats = mapOf(meetingId to mapOf("unreadCount" to 1))
                transaction.set(userRef, mapOf("lastSeenChats" to lastSeenChats))
            }
        }.addOnSuccessListener {
            Log.d(TAG, "Successfully updated unread count for participant $participantId")
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error updating unread count for participant $participantId", exception)
        }
    }

    private fun sendNotificationToParticipants(meetingId: String, messageContent: String, senderName: String) {
        firestoreDb.collection("meetings").document(meetingId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val participants = document.get("participants") as? List<String> ?: emptyList()
                    val currentUserId = auth.currentUser?.uid ?: return@addOnSuccessListener

                    for (participantId in participants) {
                        if (participantId != currentUserId) {
                            firestoreDb.collection("users").document(participantId)
                                .get()
                                .addOnSuccessListener { document ->
                                    val recipientToken = document.getString("fcmToken")
                                    if (recipientToken != null) {
                                        // Use Retrofit to send the notification data to your server
                                        val retrofit = Retrofit.Builder()
                                            .baseUrl("https://sendnotification-oom3oimxmq-uc.a.run.app")
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build()

                                        val notificationService = retrofit.create(NotificationService::class.java)
                                        val notificationData = mapOf(
                                            "to" to recipientToken,
                                            "senderName" to senderName,
                                            "messageContent" to messageContent,
                                            "meetingId" to meetingId
                                        )
                                        val call = notificationService.sendNotification(notificationData)
                                        call.enqueue(object : Callback<Void> {
                                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                Log.d(TAG, "Notification request sent successfully")
                                            }

                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                Log.e(TAG, "Error sending notification request", t)
                                            }
                                        })
                                    } else {
                                        Log.d(TAG, "FCM token not found for participant $participantId")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error getting FCM token for participant $participantId", e)
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting meeting participants", exception)
            }
    }

    interface NotificationService {
        @POST("sendNotification")
        fun sendNotification(@Body notificationData: Map<String, String>): Call<Void>
    }
}