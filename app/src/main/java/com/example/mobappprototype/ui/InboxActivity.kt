package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.InboxAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityInboxBinding
import com.example.mobappprototype.model.ChatRoom
import com.example.mobappprototype.model.LastMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
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

        Log.d(TAG, "onCreate: started") // Add this log

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        inboxAdapter = InboxAdapter(chatRooms)

        binding.rvInbox.layoutManager = LinearLayoutManager(this)
        binding.rvInbox.adapter = inboxAdapter

        binding.linearLayout6.post {
            val rect = Rect()
            binding.btnHome.getHitRect(rect)
            rect.inset(-50, -50) // Expand the touch area by 50 pixels on each side
            binding.linearLayout6.touchDelegate = TouchDelegate(rect, binding.btnHome)
        }

        fetchChatRooms()
        listenForNewMessages()
        binding.bottomNavigationBar.selectedItemId = R.id.messages

        binding.btnHome.setOnClickListener{
            val currentUserId = auth.currentUser?.uid ?: return@setOnClickListener
            firestoreDb.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        val intent = if (role == "Student") {
                            Intent(this, TutorListActivity::class.java)
                        } else {
                            Intent(this, TutorMainActivity::class.java)
                        }
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting user role: ", exception)
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    getCurrentUserRole { userRole ->
                        val intent = if (userRole == "Student") {
                            Intent(this, StudentMainActivity::class.java)
                        } else {
                            Intent(this, TutorMainActivity::class.java)
                        }
                        startActivity(intent)
                    }
                    true
                }
                R.id.messages -> {
                    true
                }
                R.id.profile -> {
                    getCurrentUserRole { userRole ->
                        val intent = if (userRole == "Student") {
                            Intent(this, StudentMainProfileActivity::class.java)
                        } else {
                            Intent(this, TutorMainProfileActivity::class.java)
                        }
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }

    }

    private fun getCurrentUserRole(callback: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        firestoreDb.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.getString("role") ?: "")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user role", exception)
                callback("")
            }
    }


    private fun fetchChatRooms() {
        Log.d(TAG, "fetchChatRooms: started")
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG, "fetchChatRooms: ${documents.size()} chat rooms found")
                    chatRooms.clear()
                    documents.forEach { document ->
                        val chatRoom = document.toObject(ChatRoom::class.java)
                        if (chatRoom != null) {
                            fetchUnreadCountForChatRoom(currentUserId, chatRoom.meetingID) { unreadCount ->
                                chatRoom.unreadCount = unreadCount
                                chatRooms.add(chatRoom)
                                // Update lastMessage only once after adding the chatRoom
                                updateLastMessage(chatRoom.meetingID)
                                runOnUiThread {
                                    inboxAdapter.notifyDataSetChanged()
                                }
                            }
                        }
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
        Log.d(TAG, "listenForNewMessages: started")
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e(TAG, "Error listening for new messages", exception)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        for (documentChange in snapshot.documentChanges) {
                            if (documentChange.type == DocumentChange.Type.MODIFIED)
                            {
                                val updatedChatRoom = documentChange.document.toObject(ChatRoom::class.java)
                                val chatRoomIndex = chatRooms.indexOfFirst { it.meetingID == updatedChatRoom.meetingID }
                                if (chatRoomIndex != -1) {
                                    // Update the chat room in the list and notify the adapter
                                    chatRooms[chatRoomIndex] = updatedChatRoom
                                    runOnUiThread {
                                        inboxAdapter.notifyItemChanged(chatRoomIndex)
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
    private fun fetchUnreadCountForChatRoom(userId: String, meetingId: String, callback: (Long) -> Unit) {
        Log.d(TAG, "fetchUnreadCountForChatRoom: started for meeting $meetingId")
        firestoreDb.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val lastSeenChats = userDocument.get("lastSeenChats") as? Map<String, Any>
                    val unreadCount = (lastSeenChats?.get(meetingId) as? Map<String, Any>)?.get("unreadCount") as? Long ?: 0
                    callback(unreadCount)
                    Log.d(TAG, "fetchUnreadCountForChatRoom: unreadCount = $unreadCount for meeting $meetingId")
                } else {
                    callback(0) // User document not found, default to 0
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching unread count", exception)
                callback(0) // Error fetching, default to 0
            }
    }


}