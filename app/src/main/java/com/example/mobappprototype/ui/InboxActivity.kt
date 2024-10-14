package com.example.mobappprototype.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.InboxAdapter
import com.example.mobappprototype.databinding.ActivityInboxBinding
import com.example.mobappprototype.model.ChatRoom
import com.example.mobappprototype.model.LastMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import java.util.Date

private const val TAG = "InboxActivity"

class InboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInboxBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var inboxAdapter: InboxAdapter
    private val chatRooms = mutableListOf<ChatRoom>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        inboxAdapter = InboxAdapter(chatRooms)

        binding.rvInbox.layoutManager = LinearLayoutManager(this)
        binding.rvInbox.adapter = inboxAdapter

        fetchChatRooms()
        listenForNewMessages()

        binding.btnHome.setOnClickListener{
            Intent(this, TutorSchedAndSubsListActivity::class.java).also {
                startActivity(it)
            }
        }

    }

    private fun fetchChatRooms() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener { documents ->
                    chatRooms.clear()
                    documents.forEach { document ->
                        val chatRoom = document.toObject(ChatRoom::class.java)
                        if (chatRoom != null) {
                            chatRooms.add(chatRoom)
                            Log.d(TAG, "Chat room: $chatRoom")
                        }
                    }

                    // Update lastMessage for each chat room
                    chatRooms.forEach { chatRoom ->
                        updateLastMessage(chatRoom.meetingID)
                    }

                    runOnUiThread {
                        inboxAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching chat rooms", exception)
                }
        }
    }

    private fun updateLastMessage(meetingID: String) {
        firestoreDb.collection("chats")
            .document(meetingID)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { messages ->
                if (!messages.isEmpty) {
                    val lastMessageContent = messages.documents[0].getString("content") ?: ""
                    val lastMessage = LastMessage(content = lastMessageContent)

                    val chatRoomIndex = chatRooms.indexOfFirst { it.meetingID == meetingID }
                    if (chatRoomIndex != -1) {
                        chatRooms[chatRoomIndex] = chatRooms[chatRoomIndex].copy(lastMessage = lastMessage)

                        runOnUiThread {
                            inboxAdapter.notifyItemChanged(chatRoomIndex)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching last message", exception)
            }
    }
    private fun listenForNewMessages() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e(TAG, "Error listening for new messages", exception)
                        return@addSnapshotListener
                    }

                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                            val chatRoomId = change.document.id
                            val lastMessage = change.document.toObject(ChatRoom::class.java).lastMessage

                            if (lastMessage != null && lastMessage.senderUID != currentUserId) {
                                firestoreDb.collection("users").document(currentUserId)
                                    .get()
                                    .addOnSuccessListener { userDocument ->
                                        if (userDocument.exists()) {
                                            val lastSeenChats = userDocument.get("lastSeenChats") as? Map<String, Any>
                                            val lastSeenChatData = lastSeenChats?.get(chatRoomId) as? Map<String, Any>
                                            val lastSeenTime = lastSeenChatData?.get("timestamp") as? Date
                                            val currentUnreadCount = lastSeenChatData?.get("unreadCount") as? Long ?: 0

                                            if (lastSeenTime == null || lastMessage.timestamp!!.after(lastSeenTime)) {
                                                // Increment unread count
                                                val newUnreadCount = currentUnreadCount + 1
                                                userDocument.reference.update("lastSeenChats.${chatRoomId}.unreadCount", newUnreadCount)
                                                    .addOnSuccessListener {
                                                        Log.d(TAG, "Successfully incremented unread count for chat room: $chatRoomId")
                                                        // Update the UI here for the number of unreadMessages
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e(TAG, "Error incrementing unread count", e)
                                                    }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }

        }
    }
}