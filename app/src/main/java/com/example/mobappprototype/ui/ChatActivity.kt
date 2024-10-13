package com.example.mobappprototype.ui

import android.os.Bundle
import android.util.Log
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

        val meetingId = intent.getStringExtra("meetingId")
        if (meetingId != null) {
            fetchMessages(meetingId)
            val userUid = auth.currentUser?.uid
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

}