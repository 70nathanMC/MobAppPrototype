package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.ChatAdapter
import com.example.mobappprototype.databinding.ActivityChatBinding
import com.example.mobappprototype.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "ChatActivity"
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
                binding.rvChatMessages.smoothScrollToPosition(messages.size - 1)
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

}