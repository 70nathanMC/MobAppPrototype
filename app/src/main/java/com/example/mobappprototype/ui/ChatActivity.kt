package com.example.mobappprototype.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.ChatAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityChatBinding
import com.example.mobappprototype.model.ChatMessage
import com.example.mobappprototype.model.MeetingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "ChatActivity"
private const val CHANNEL_ID = "channelID"
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        chatAdapter = ChatAdapter(messages)

        binding.rvChatMessages.layoutManager = LinearLayoutManager(this)
        binding.rvChatMessages.adapter = chatAdapter

        val tutorUid = intent.getStringExtra("TUTOR_UID")
        val userUid = auth.currentUser?.uid

        check(tutorUid, userUid)

        binding.btnHome.setOnClickListener{
            Intent(this, InboxActivity::class.java).also {
                startActivity(it)
            }
        }

        val meetingId = intent.getStringExtra("meetingId")
        if (meetingId != null) {
            fetchMessages(meetingId)
            val userRef = firestoreDb.collection("users").document(userUid!!)

            // Fetch the current unread count (if any)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val lastSeenChats = documentSnapshot.get("lastSeenChats") as? Map<String, Any>
                    val currentUnreadCount = (lastSeenChats?.get(meetingId) as? Map<String, Any>)?.get("unreadCount") as? Long ?: 0

                    // Reset the unread count for this meeting
                    val newUnreadCount = if (currentUnreadCount > 0) currentUnreadCount - 1 else 0
                    val updatedLastSeenData = mapOf("unreadCount" to newUnreadCount, "timestamp" to FieldValue.serverTimestamp())
                    userRef.update("lastSeenChats.${meetingId}", updatedLastSeenData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully updated last seen chat time for meeting: $meetingId")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating last seen chat time", e)
                        }
                }
            }

            binding.btnSendMessage.setOnClickListener {
                val messageContent = binding.etMessage.text.toString()
                if (messageContent.isNotBlank()) {
                    sendMessage(meetingId, messageContent)
                }
            }
        } else {
            Log.e(TAG, "Meeting ID not found in Intent")
        }
    }

    private fun fetchMessages(meetingId: String) {
        firestoreDb.collection("chats")
            .document(meetingId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error fetching messages", exception)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val message = change.document.toObject(ChatMessage::class.java)
                        messages.add(message)
                    }
                }
                chatAdapter.notifyDataSetChanged()
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
        }
    }
    private fun check(tutorUid: String?, userUid: String?) {
        val meetingId = intent.getStringExtra("meetingId")
        if (meetingId != null && userUid != null) {
            firestoreDb.collection("meetings").document(meetingId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val meeting = document.toObject(MeetingData::class.java)
                        if (meeting != null) {
                            if (tutorUid == userUid) {
                                val title = "Success!"
                                val content = "You have successfully joined your meeting: ${meeting.subject} - ${meeting.branch}"
                                sendNotifications(title, content, meetingId.hashCode()) // Unique ID for tutor
                            } else {
                                firestoreDb.collection("users").document(userUid).get()
                                    .addOnSuccessListener { studentDocument ->
                                        if (studentDocument != null && studentDocument.exists()) {
                                            val studentName = studentDocument.getString("firstName") ?: "Student"
                                            val title = "Congratulations!"
                                            val content = "$studentName has joined the meeting: ${meeting.subject} - ${meeting.branch}"
                                            sendNotifications(title, content, (meetingId + userUid).hashCode())
                                        } else {
                                            Log.e(TAG, "Student document not found")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, "Error getting student document", exception)
                                    }
                            }
                        }
                    }
                }
        }
    }

    private fun sendNotifications(title_: String, content: String, notificationId: Int) {
        Log.d(TAG, "going to createNotificationChannel")
        createNotificationChannel()
        Log.d(TAG, "notificationChannel done")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title_)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Notification failure to get permission")
                return
            }
            notify(0, builder.build())
        }
        Log.d(TAG, "notification finished")

    }
    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "First channel", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Test description"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}